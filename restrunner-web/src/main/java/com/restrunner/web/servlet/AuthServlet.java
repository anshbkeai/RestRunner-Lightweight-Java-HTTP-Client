package com.restrunner.web.servlet;

import com.restrunner.web.db.WebDB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/api/login", "/api/signup", "/logout"})
public class AuthServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String username, password;

        if (req.getContentType() != null && req.getContentType().contains("application/json")) {
            String body = req.getReader().lines().collect(Collectors.joining());
            username = extractFromJson(body, "username");
            password = extractFromJson(body, "password");
        } else {
            username = req.getParameter("username");
            password = req.getParameter("password");
        }

        if ("/api/signup".equals(path)) {
            if (WebDB.getInstance().signup(username, password)) {
                if (req.getContentType() != null && req.getContentType().contains("application/json")) {
                    resp.getWriter().write("{\"message\":\"Signup successful\"}");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=Signup successful");
                }
            } else {
                resp.setStatus(400);
                if (req.getContentType() != null && req.getContentType().contains("application/json")) {
                    resp.getWriter().write("{\"error\":\"Username already exists\"}");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/signup.jsp?error=Username already exists");
                }
            }
        } else if ("/api/login".equals(path)) {
            String token = WebDB.getInstance().login(username, password);
            if (token != null) {
                HttpSession session = req.getSession();
                session.setAttribute("user", username);
                session.setAttribute("token", token);
                
                if (req.getContentType() != null && req.getContentType().contains("application/json")) {
                    resp.setContentType("application/json");
                    resp.getWriter().write("{\"token\":\"" + token + "\"}");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/history.jsp");
                }
            } else {
                resp.setStatus(401);
                if (req.getContentType() != null && req.getContentType().contains("application/json")) {
                    resp.getWriter().write("{\"error\":\"Invalid credentials\"}");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/login.jsp?error=Invalid credentials");
                }
            }
        }
    }

    private String extractFromJson(String json, String key) {
        // Simple manual extraction to avoid adding Jackson dependency to compile if not needed
        // but it is in pom.xml, so I could use it. However, AuthService uses a manual way too.
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return null;
            int colonIndex = json.indexOf(":", keyIndex);
            int startQuote = json.indexOf("\"", colonIndex);
            int endQuote = json.indexOf("\"", startQuote + 1);
            return json.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
}
