package com.restrunner.core.engine.executor;

import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;

import java.util.concurrent.CompletableFuture;

public interface RequestExecutor {
    CompletableFuture<ApiResponse> execute(ApiRequest request);
}
