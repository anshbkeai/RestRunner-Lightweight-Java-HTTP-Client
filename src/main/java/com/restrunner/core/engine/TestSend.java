package com.restrunner.core.engine;

import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;
import com.restrunner.core.utils.JavaHttpTransport;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class TestSend {
    public  static  CompletableFuture<ApiResponse> execute(ApiRequest request){




        HttpClient httpClient = HttpClientSingleton.get();

        HttpRequest httpRequest = JavaHttpTransport.convertApiRequest(request);

        CompletableFuture<ApiResponse> future = httpClient.sendAsync(httpRequest , HttpResponse.BodyHandlers.ofString())
                .exceptionally(x -> {
                    System.out.println(x.getMessage());
                    return null;
                })

                .thenApply(JavaHttpTransport::convertHttpResponse)
                .exceptionally(x -> {
                    System.out.println( Arrays.stream(x.getStackTrace()).findFirst().toString());

                    return new ApiResponse(500,null, Duration.ZERO,null,x.toString());
                })
                .toCompletableFuture();

        return  future;
    }
}
