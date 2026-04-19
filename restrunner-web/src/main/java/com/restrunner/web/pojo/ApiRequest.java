package com.restrunner.web.pojo;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ApiRequest {

    private String uri;
    private RequestMethod requestMethod;
    private Map<String, List<String>> headers;
    private Duration timeout;
    private String body;

    public ApiRequest(String uri, String body, Duration timeout, Map<String, List<String>> headers, RequestMethod requestMethod) {
        this.uri = uri;
        this.body = body;
        this.timeout = timeout;
        this.headers = headers;
        this.requestMethod = requestMethod;
    }

    public  ApiRequest() {

    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
