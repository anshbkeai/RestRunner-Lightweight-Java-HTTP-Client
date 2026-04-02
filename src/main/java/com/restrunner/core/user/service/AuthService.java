package com.restrunner.core.user.service;

import com.restrunner.AppConfig;
import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.RequestMethod;
import com.restrunner.core.user.db.UserDB;
import com.restrunner.core.user.pojo.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    public static  boolean login(String username, String password) {

        try {
            HttpClient client = HttpClient.newHttpClient();

            String json = "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.LOGIN_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.toString());

            if (response.statusCode() == 200) {

                String token = extractToken(response.body());

                if (token != null) {
                    User user = new User("user-123",username,token);
                    UserDB.getInstance().save(user);
                    return true;
                }

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private  static  String extractToken(String json) {
        json = json.replace("{", "")
                .replace("}", "")
                .replace("\"", "");

        String[] parts = json.split(":");

        if (parts.length == 2 && parts[0].trim().equals("token")) {
            return parts[1].trim();
        }

        return null;
    }

    public static void logout() {
        UserDB.getInstance().delete("user-123");
    }


}
