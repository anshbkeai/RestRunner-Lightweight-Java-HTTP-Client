package com.restrunner.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restrunner.web.db.WebDB;
import com.restrunner.web.pojo.History;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/sync")
public class SyncServerletV2 extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = req.getHeader("token");

        System.out.println(token);

        if (token == null || !WebDB.getInstance().isValidToken(token)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        try {
            // Read request body
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            try (var reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            String requestBody = jsonBuilder.toString();

            System.out.println(requestBody);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            // Deserialize
            List<History> historyList = objectMapper.readValue(
                    requestBody,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, History.class)
            );

            // Store each
            for (History history : historyList) {
                WebDB.getInstance().saveHistory(history, objectMapper);
            }

            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"synced\"}");

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"Sync failed\"}");
            e.printStackTrace();
        }
    }
}
