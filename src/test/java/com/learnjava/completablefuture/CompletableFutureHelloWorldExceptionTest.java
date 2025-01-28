package com.learnjava.completablefuture;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.learnjava.service.HelloWorldService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompletableFutureHelloWorldExceptionTest {

    @Mock
    HelloWorldService helloWorldService = mock(HelloWorldService.class);


    @InjectMocks
    CompletableFutureHelloWorldException completableFutureHelloWorldException;

    @Test
    void helloworld_3_async_calls_handle() {
        //given
        when(helloWorldService.hello()).thenThrow(new RuntimeException("Exception Occurred"));
        when(helloWorldService.world()).thenCallRealMethod();

        //when
        String result = completableFutureHelloWorldException.helloworld_3_async_calls_handle();

        //then
        assertEquals(" WORLD! HI COMPLETABLEFUTURE!", result);
    }
}