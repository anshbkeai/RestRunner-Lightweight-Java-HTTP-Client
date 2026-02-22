package com.restrunner.core.engine;

import java.net.http.HttpClient;

public class HttpClientSingleton {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    public static HttpClient get() {
        return httpClient;
    }

}
