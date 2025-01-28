package com.learnjava.completablefuture;

import com.learnjava.service.HelloWorldService;

import java.util.concurrent.CompletableFuture;

import static com.learnjava.util.CommonUtil.*;
import static com.learnjava.util.LoggerUtil.log;

public class CompletableFutureHelloWorldException {

    private HelloWorldService hws;

    public CompletableFutureHelloWorldException(HelloWorldService hws) {
        this.hws = hws;
    }


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
                    log("Exception is :" + e.getMessage());
                    return "";
                })
                .thenCombine(world, (h, w) -> h+w) // " world!"
                .thenCombine(hiCompletableFuture, (previous,current)->previous+current)
                //   " world! Hi CompletableFuture!"
                .thenApply(String::toUpperCase) // " WORLD! HI COMPLETABLEFUTURE!"
                .join(); // " WORLD! HI COMPLETABLEFUTURE!"

        timeTaken();
        return hw;
    }
}
