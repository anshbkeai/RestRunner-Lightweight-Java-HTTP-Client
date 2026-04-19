package com.restrunner.web.pojo;

import java.time.LocalDateTime;

public class History {


    private int id;
    private String uri;
    private RequestMethod method;

    // store as objects (better for your app)
    private ApiRequest request;
    private ApiResponse response;

    private LocalDateTime createdAt;
    private boolean synced = true;

    public History() {}

    public History(int id, String uri, RequestMethod method,
                   ApiRequest request, ApiResponse response,
                   LocalDateTime createdAt) {
        this.id = id;
        this.uri = uri;
        this.method = method;
        this.request = request;
        this.response = response;
        this.createdAt = createdAt;
        this.synced = false;
    }




    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", uri='" + uri + '\'' +
                ", method=" + method +
                ", request=" + request +
                ", response=" + response +
                ", createdAt=" + createdAt +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public ApiRequest getRequest() {
        return request;
    }

    public void setRequest(ApiRequest request) {
        this.request = request;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public ApiResponse getResponse() {
        return response;
    }

    public void setResponse(ApiResponse response) {
        this.response = response;
    }
}
