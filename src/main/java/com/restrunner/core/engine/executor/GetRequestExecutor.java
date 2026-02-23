package com.restrunner.core.engine.executor;

import com.restrunner.core.pojo.ApiRequest;

import java.net.http.HttpRequest;

public class GetRequestExecutor extends AbstractHttpExecutor{

    @Override
    protected void applyMethod(HttpRequest.Builder builder, ApiRequest request) {
        builder.GET();

    }
}
