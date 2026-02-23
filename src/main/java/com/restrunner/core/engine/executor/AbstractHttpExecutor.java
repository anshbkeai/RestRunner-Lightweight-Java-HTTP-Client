package com.restrunner.core.engine.executor;

import com.restrunner.core.engine.client.HttpClientSingleton;
import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;
import com.restrunner.core.utils.JavaHttpTransport;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract  class AbstractHttpExecutor implements RequestExecutor{
    // this class is about adding ot building the request about the System
    private HttpClient httpClient;

    public AbstractHttpExecutor() {
        this.httpClient = HttpClientSingleton.get();
    }

    @Override
    public CompletableFuture<ApiResponse> execute(ApiRequest apiRequest) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiRequest.getUri()))
                .timeout(apiRequest.getTimeout());

        applyHeaders(builder,apiRequest);
        applyMethod(builder,apiRequest);

        HttpRequest httpRequest = builder.build();

        return httpClient.sendAsync(httpRequest , HttpResponse.BodyHandlers.ofString())
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

    }
    protected void applyHeaders(HttpRequest.Builder builder , ApiRequest apiRequest) {
        if (apiRequest.getHeaders() != null) {
            for (Map.Entry<String, List<String>> entry : apiRequest.getHeaders().entrySet()) {
                String headerName = entry.getKey();
                List<String> values = entry.getValue();

                for (String value : values) {
                    builder.header(headerName, value);
                }
            }
        }
    }

    protected abstract void applyMethod(HttpRequest.Builder builder, ApiRequest request);


}
