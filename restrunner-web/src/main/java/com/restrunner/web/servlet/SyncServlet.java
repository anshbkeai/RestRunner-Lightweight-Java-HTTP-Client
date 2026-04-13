package com.restrunner.web.servlet;

import com.restrunner.web.db.WebDB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/sync", "/history"})
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getHeader("token");
        if (token != null && WebDB.getInstance().isValidToken(token)) {
            // Logic for syncing data from the client (SyncService)
            // For now, we just acknowledge receipt
            resp.getWriter().write("{\"status\":\"synced\"}");
        } else {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Unauthorized\"}");
        }
    }
}
