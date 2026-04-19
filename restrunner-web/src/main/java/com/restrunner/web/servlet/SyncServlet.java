package com.restrunner.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restrunner.web.db.WebDB;
import com.restrunner.web.pojo.History;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/history")
public class SyncServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String token = req.getHeader("token");
        
        // Allow session for browser OR token for API
        if ((session != null && session.getAttribute("user") != null) || (token != null && WebDB.getInstance().isValidToken(token))) {
            List<Map<String, Object>> history = WebDB.getInstance().getHistory();
            req.setAttribute("history", history);
            req.getRequestDispatcher("/history.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }


}
