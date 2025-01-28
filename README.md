# parallel-asyncronous
This repo has the code for parallel and asynchronous programming in Java


CompletableFuture 사용중 
thenCombine()

join()을 사용하는경우 


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