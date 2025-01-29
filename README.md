# parallel-asyncronous
This repo has the code for parallel and asynchronous programming in Java


https://ssginc.udemy.com/course/parallel-and-asynchronous-programming-in-modern-java

<table>
    <tr>    
        <th>메소드</th>
        <td>설명</td>
        <td>사용시점</td>
    </tr>
<tbody>
    <tr>
        <td>thenCombine</td>
        <td>두 개의 독립적인 CompletableFuture의 결과를 결합</td>
        <td>두 작업이 독립적으로 수행되고, 그 결과를 합쳐 새로운 값을 만들어야 할 때</td>
    </tr>
    <tr>
        <td>thenCompose</td>
        <td>하나의 CompletableFuture의 결과를 다음 CompletableFuture의 입력으로 사용</td>
        <td>한 작업의 결과를 바탕으로 다음 작업을 수행해야 할 때</td>
    </tr>
</tbody>
</table>

### 비동기로 실행되는 서비스를 테스트코드 작성시 주의사항
메인스레드가 먼저 끝나서 서브 스레드가 실행되지 않는 문제 발생
해결하기 위해 join 사용

```
public Product retrieveProductDetails(String productId) {
    stopWatch.start();

    CompletableFuture<ProductInfo> cfProductInfo = CompletableFuture
            .supplyAsync(()-> productInfoService.retrieveProductInfo(productId));
    CompletableFuture<Review> cfReview = CompletableFuture
            .supplyAsync(()-> reviewService.retrieveReviews(productId));

    Product product = cfProductInfo
            .thenCombine(cfReview, (productInfo,review)->new Product(productId, productInfo, review))
            .join(); //block the thread

    stopWatch.stop();
    log("Total Time Taken : "+ stopWatch.getTime());
    return product;
}
```

handle로 Exception 처리 할 경우 무조건 실행이 되므로
조건문을 추가 하여 return 하도록 하여 다음 스트림 체이닝으로 넘어갈 수 있도록 함

```
public String helloworld_3_async_calls_handle(){
    startTimer();

    CompletableFuture<String> hello = CompletableFuture.supplyAsync(()->hws.hello());
    CompletableFuture<String> world = CompletableFuture.supplyAsync(()->hws.world());
    CompletableFuture<String> hiCompletableFuture =  CompletableFuture.supplyAsync(()->{
        delay(1000);
        return " Hi CompletableFuture!";
    });

    String hw= hello
            .handle((res, e) -> {
                log("res is: "+ res);
                if(e != null) {
                    log("Exception is :" + e.getMessage());
                    return "";
                } else {
                    return res;
                }
            })
            .thenCombine(world, (h, w) -> h+w) // " world!"
            .handle((res, e) -> {
                log("Exception after world is :" + e.getMessage());
                return "";
            })
            .thenCombine(hiCompletableFuture, (previous,current)->previous+current)
            //   " world! Hi CompletableFuture!"
            .thenApply(String::toUpperCase) // " WORLD! HI COMPLETABLEFUTURE!"
            .join(); // " WORLD! HI COMPLETABLEFUTURE!"

    timeTaken();
    return hw;
}
```


#### anyOf를 사용할 경우 더 빠른 응답의 내용을 골라서 응답할수 있음
```
public String anyOf(){
    //DB
    CompletableFuture<String> db = CompletableFuture.supplyAsync(()->{
        delay(1000);
        log("response DB");
        return "hello world";
    });

    //REST CALL
    CompletableFuture<String> restcall = CompletableFuture.supplyAsync(()->{
        delay(2000);
        log("response from REST");
        return "hello world";
    });

    //SOAP call
    CompletableFuture<String> restCall = CompletableFuture.supplyAsync(() -> {
        delay(3000);
        log("response from SOAP");
        return "hello world";
    });

    List<CompletableFuture<String>> cfList = Arrays.asList(db, restcall, restCall);
    CompletableFuture<Object> cfAny = CompletableFuture.anyOf(cfList.toArray(new CompletableFuture[cfList.size()]));

    String result = (String) cfAny.thenApply(v -> {
        if(v instanceof String){
            return v;
        }
        return null;
    })
        .join();
    return result;
}
```

### Excutors 쓰레드 할당 메소드 설명
자바 Executors 메소드에 대한 설명
자바 Executors 클래스는 스레드 풀을 생성하고 관리하는 데 사용되는 다양한 정적 팩토리 메소드를 제공합니다. 스레드 풀은 애플리케이션에서 비동기 작업을 효율적으로 처리하는 데 필수적인 요소입니다. Executors를 사용하면 스레드 생성 및 관리의 부담을 줄이고, 애플리케이션의 성능을 향상시킬 수 있습니다.

<hr />

### 주요 Executors 메소드

### 각 메소드의 특징 및 사용 시나리오

newFixedThreadPool(int nThreads):
- 장점: 예측 가능한 스레드 수로 안정적인 작업 처리가 가능합니다.
- 단점: 스레드 수가 고정되어 있으므로, 많은 수의 작업이 동시에 제출되면 대기 시간이 길어질 수 있습니다.
- 사용 시나리오: 처리해야 할 작업의 수가 예측 가능하고, 안정적인 성능이 요구되는 경우에 적합합니다.

newCachedThreadPool():
- 장점: 필요에 따라 스레드를 생성하므로, 유연하게 작업을 처리할 수 있습니다.
- 단점: 스레드 수가 제한되지 않으므로, 과도한 수의 스레드가 생성될 수 있습니다.
- 사용 시나리오: 처리해야 할 작업의 수가 불규칙하고, 빠른 응답 속도가 중요한 경우에 적합합니다.

newSingleThreadExecutor():
- 장점: 모든 작업을 순차적으로 처리하므로, 작업 간의 의존성이 있는 경우에 유용합니다.
- 단점: 동시성을 제공하지 않으므로, 여러 작업을 동시에 처리해야 하는 경우에는 성능 저하가 발생할 수 있습니다.
- 사용 시나리오: 작업을 순차적으로 처리해야 하거나, 단일 스레드 환경에서 작업을 실행해야 하는 경우에 적합합니다.

newScheduledThreadPool(int corePoolSize):
- 장점: 작업을 예약하거나 주기적으로 실행할 수 있습니다.
- 단점: 코어 스레드 수가 제한되어 있으므로, 많은 수의 예약된 작업을 처리해야 하는 경우에는 대기 시간이 길어질 수 있습니다.
- 사용 시나리오: 특정 시간에 작업을 실행해야 하거나, 주기적으로 작업을 반복해야 하는 경우에 적합합니다.

newWorkStealingPool():
- 장점: 작업 훔치기 알고리즘을 사용하여 작업 부하를 효율적으로 분산시킵니다.
- 단점: Fork/Join 프레임워크와 함께 사용해야 합니다.
- 사용 시나리오: Fork/Join 프레임워크를 사용하여 병렬 작업을 처리하는 경우에 적합합니다.

Executors 사용 시 주의사항
- 스레드 풀 크기: 적절한 스레드 풀 크기를 설정하는 것이 중요합니다. 너무 작은 크기는 성능 저하를 유발할 수 있으며, 너무 큰 크기는 과도한 리소스 소비를 초래할 수 있습니다.
- 대기열: 작업 대기열의 크기를 고려해야 합니다. 너무 큰 대기열은 메모리 부족을 유발할 수 있으며, 너무 작은 대기열은 작업 손실을 초래할 수 있습니다.
- 스레드 풀 종료: 애플리케이션 종료 시 스레드 풀을 적절하게 종료해야 합니다. 그렇지 않으면 스레드가 계속 실행되어 리소스 누수가 발생할 수 있습니다.