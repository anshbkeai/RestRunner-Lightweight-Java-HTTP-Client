package com.restrunner.core.utils;

import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class JavaHttpTransport {

    public static HttpRequest convertApiRequest(ApiRequest apiRequest) {
        HttpRequest httpRequest = null;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiRequest.getUri()))
                .timeout(apiRequest.getTimeout());

        if (apiRequest.getHeaders() != null) {
            for (Map.Entry<String, List<String>> entry : apiRequest.getHeaders().entrySet()) {
                String headerName = entry.getKey();
                List<String> values = entry.getValue();

                for (String value : values) {
                    builder.header(headerName, value);
                }
            }
        }

        switch (apiRequest.getRequestMethod()) {
            case GET:
                builder.GET();
                break;

            case POST:
                builder.POST(HttpRequest.BodyPublishers.ofString(apiRequest.getBody()));
                break;
        }

        httpRequest = builder.build();
        return  httpRequest;

    }

    public static ApiResponse convertHttpResponse(HttpResponse<String> response) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatusCode(response.statusCode());
        apiResponse.setHeaders(response.headers().map());
        apiResponse.setBody(response.body());
        //apiResponse.setDuration(duration);

        return apiResponse;
    }
}
