package com.restrunner.core.user.pojo;


import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;
import com.restrunner.core.pojo.RequestMethod;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class History {

    private int id;
    private String uri;
    private RequestMethod method;

    // store as objects (better for your app)
    private ApiRequest request;
    private ApiResponse response;

    private LocalDateTime createdAt;
    private boolean synced = false;

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
}