package com.restrunner.core.pojo;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ApiResponse {
    private int statusCode;
    private Map<String, List<String>> headers;
    private Duration duration;
    private String body;
    private String error;

    public ApiResponse(int statusCode, Map<String, List<String>> headers, Duration duration, String body, String error) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.duration = duration;
        this.body = body;
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", duration=" + duration +
                ", body='" + body + '\'' +
                ", error='" + error + '\'' +
                '}';
    }

    public ApiResponse() {

    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
