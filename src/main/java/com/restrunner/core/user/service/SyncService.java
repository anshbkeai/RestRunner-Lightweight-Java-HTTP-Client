package com.restrunner.core.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restrunner.AppConfig;
import com.restrunner.core.engine.HttpEngine;
import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;
import com.restrunner.core.pojo.RequestMethod;
import com.restrunner.core.user.db.HistoryDB;
import com.restrunner.core.user.db.UserDB;
import com.restrunner.core.user.pojo.History;
import com.restrunner.core.user.pojo.User;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncService {
    private final HistoryDB historyDB;
    private final UserDB userDB;
    private final ObjectMapper objectMapper;
    public SyncService() {
       this.historyDB = HistoryDB.getInstance();
       this.userDB = UserDB.getInstance();
       this.objectMapper  = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void SyncHistory() throws JsonProcessingException {
        String token = userDB.getUser().get().getToken();

        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setUri(AppConfig.SYNC_URL);
        apiRequest.setRequestMethod(RequestMethod.POST);
        apiRequest.setHeaders(Map.of("token", List.of(token)));
        apiRequest.setTimeout(Duration.ofMinutes(1));

        // 🔥 Collect unsynced history
        List<History> unsyncedHistory = new ArrayList<>();

        for (History history : historyDB.getLast30()) {
            if (!history.isSynced()) {
                unsyncedHistory.add(history);
            }
        }

        // 🔥 No data → skip call
        if (unsyncedHistory.isEmpty()) {
            System.out.println("No history to sync");
            return;
        }

        // 🔥 Proper JSON serialization (NO manual join)
        String requestBody = objectMapper.writeValueAsString(unsyncedHistory);
        apiRequest.setBody(requestBody);

        System.out.println("Sync payload: " + requestBody);

        try {
            // 🔥 Execute HTTP call
            ApiResponse response = HttpEngine.getInstance().execute(apiRequest).join();

            System.out.println("Sync response: " + response);

            // 🔥 Only mark synced if SUCCESS
            if (response.getStatusCode() == 200) {
                for (History history : unsyncedHistory) {
                    history.setSynced(true);
                    historyDB.save(history);
                }
            } else {
                System.out.println("Sync failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.out.println("Sync failed due to exception");
            e.printStackTrace();
        }
    }
}
