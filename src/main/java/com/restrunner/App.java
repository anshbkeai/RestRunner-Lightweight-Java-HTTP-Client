package com.restrunner;



import com.restrunner.core.engine.HttpEngine;
import com.restrunner.core.engine.TestSend;
import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;
import com.restrunner.core.pojo.RequestMethod;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws URISyntaxException, IOException, InterruptedException {
         String uri = "http://localhost:8080/";
//        HttpClient httpClient = HttpClient.newHttpClient();
//
//        HttpRequest httpRequest = HttpRequest.newBuilder()
//                                                .uri(URI.create(uri))
//                                                .GET()
//
//                                        .build();
//        HttpResponse<String> response = httpClient.send(httpRequest,HttpResponse.BodyHandlers.ofString());
//
//
//
//
//
//
//        System.out.println( response.toString() );
//
//        System.out.println("Body  is " + response.body());

        ApiRequest apiRequest = new ApiRequest(uri, RequestMethod.GET,null, Duration.ofMinutes(2),null);

        TestSend.execute(apiRequest)
                .thenAccept(x -> System.out.println(x.toString()))
                .join();

       String get = uri+"test/";
       apiRequest = new ApiRequest(get, RequestMethod.GET,null, Duration.ofMinutes(2),null);
        TestSend.execute(apiRequest)
                .thenAccept(x -> System.out.println(x.toString()))
                .join();


         get = uri+"test/";
        apiRequest = new ApiRequest(get, RequestMethod.POST,null, Duration.ofMinutes(2),"This is teh Test");
        TestSend.execute(apiRequest)
                .thenAccept(x -> System.out.println(x.toString()))
                .join();


        get = uri+"test/error";
        apiRequest = new ApiRequest(get, RequestMethod.GET,null, Duration.ofSeconds(3),null);
        TestSend.execute(apiRequest)
                .thenAccept(x -> System.out.println(x.toString()))
                .join();


        apiRequest = new ApiRequest("https://jsonplaceholder.typicode.com/posts/1", RequestMethod.GET,null, Duration.ofSeconds(3),null);
        HttpEngine httpEngine = HttpEngine.getInstance();
        httpEngine.execute(apiRequest).thenAccept(x -> System.out.println(x.toString())).join();

//        get = uri+"task/run";
//        apiRequest = new ApiRequest(get, RequestMethod.POST,null, Duration.ofSeconds(25),null);
//        TestSend.execute(apiRequest)
//                .thenAccept(x -> System.out.println(x.toString()))
//                .join();



    }
}
