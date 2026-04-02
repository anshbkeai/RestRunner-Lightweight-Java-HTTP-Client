package com.restrunner.core.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restrunner.AppConfig;
import com.restrunner.core.engine.HttpEngine;
import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.RequestMethod;
import com.restrunner.core.user.db.HistoryDB;
import com.restrunner.core.user.db.UserDB;
import com.restrunner.core.user.pojo.History;
import com.restrunner.core.user.pojo.User;

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
        String token  = userDB.getUser().get().getToken();
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setUri(AppConfig.SYNC_URL);
        apiRequest.setRequestMethod(RequestMethod.POST);
        apiRequest.setHeaders(Map.of("token" , List.of(token) ));
        List<String> historyJsonList = new ArrayList<>();

        for (History history : historyDB.getLast30()) {
            if(!history.isSynced()){
                String hist = objectMapper.writeValueAsString(history);
                historyJsonList.add(hist);
            }
        }

        String requestBody = "[" + String.join(",", historyJsonList) + "]";

        apiRequest.setBody(requestBody);

        HttpEngine.getInstance().execute(apiRequest);

        for (History history : historyDB.getLast30()) {
            history.setSynced(true);
            historyDB.save(history);
        }
    }
}
