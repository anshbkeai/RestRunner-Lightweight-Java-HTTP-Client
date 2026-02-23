package com.restrunner.core.engine.executor;

import com.restrunner.core.pojo.ApiRequest;

import java.net.http.HttpRequest;

public class PostExecutor extends  AbstractHttpExecutor{
    @Override
    protected void applyMethod(HttpRequest.Builder builder, ApiRequest request) {
        builder.POST(HttpRequest.BodyPublishers.ofString(request.getBody()));
    }
}
