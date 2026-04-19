package com.restrunner.core.user.db;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.restrunner.core.pojo.*;
import com.restrunner.core.user.pojo.History;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDB {

    private static class Holder {
        private static final HistoryDB INSTANCE = new HistoryDB();
    }

    public static HistoryDB getInstance() {
        return Holder.INSTANCE;
    }

    private final Connection conn;
    private final ObjectMapper mapper;

    private HistoryDB() {
        this.conn = ConnectDB.getInstance().getConn();

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    // ✅ SAVE HISTORY
    public void save(History history) {
        String sql = "INSERT INTO api_history (uri, method, request_json, response_json) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, history.getUri());
            ps.setString(2, history.getMethod().name());

            ps.setString(3, mapper.writeValueAsString(history.getRequest()));
            ps.setString(4, mapper.writeValueAsString(history.getResponse()));

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to save history", e);
        }
    }

    // ✅ GET LAST 30 RECORDS
    public List<History> getLast30() {
        List<History> list = new ArrayList<>();

        String sql = "SELECT * FROM api_history ORDER BY created_at DESC LIMIT 30";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                History history = new History();

                history.setId(rs.getInt("id"));
                history.setUri(rs.getString("uri"));
                history.setMethod(RequestMethod.valueOf(rs.getString("method")));

                String reqJson = rs.getString("request_json");
                String resJson = rs.getString("response_json");

                history.setRequest(mapper.readValue(reqJson, ApiRequest.class));
                history.setResponse(mapper.readValue(resJson, ApiResponse.class));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    history.setCreatedAt(ts.toLocalDateTime());
                }

                list.add(history);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch history", e);
        }

        return list;
    }

    public void delete() {
        String sql = "Delete  from api_history";
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}