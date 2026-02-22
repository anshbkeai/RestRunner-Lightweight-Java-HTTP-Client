package com.restrunner.core.pojo;

import java.time.Duration;
import java.util.List;
import java.util.Map;


public class ApiRequest {
    private String uri;
    private RequestMethod requestMethod;
    private Map<String, List<String>> headers;
    private Duration timeout;
    private String body;

    public ApiRequest(String uri, RequestMethod requestMethod, Map<String, List<String>> headers, Duration timeout, String body) {
        this.uri = uri;
        this.requestMethod = requestMethod;
        this.headers = headers;
        this.timeout = timeout;
        this.body = body;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "ApiRequest{" +
                "uri='" + uri + '\'' +
                ", requestMethod=" + requestMethod +
                ", headers=" + headers +
                ", timeout=" + timeout +
                ", body='" + body + '\'' +
                '}';
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
