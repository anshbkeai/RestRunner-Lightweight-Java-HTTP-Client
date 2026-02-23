package com.restrunner.core.engine;

import com.restrunner.core.engine.executor.GetRequestExecutor;
import com.restrunner.core.engine.executor.PostExecutor;
import com.restrunner.core.engine.executor.RequestExecutor;
import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;

import java.util.concurrent.CompletableFuture;

public class HttpEngine {
    private static class Holder {
        private static final HttpEngine INSTANCE = new HttpEngine();
    }

    public static HttpEngine getInstance() {
        return Holder.INSTANCE;
    }

    public CompletableFuture<ApiResponse> execute(ApiRequest request) {
        RequestExecutor executor = resolveExecutor(request);
        return executor.execute(request);
    }

    private RequestExecutor resolveExecutor(ApiRequest request) {

        switch (request.getRequestMethod()) {
            case GET:
                return new GetRequestExecutor();
            case POST:
                return new PostExecutor();
            default:
                throw new IllegalArgumentException("Unsupported method");
        }
    }
}
