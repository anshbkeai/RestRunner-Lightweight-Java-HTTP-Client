package com.restrunner.core.engine.client;

import java.net.http.HttpClient;

public class HttpClientSingleton {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    public static HttpClient get() {
        return httpClient;
    }

}
