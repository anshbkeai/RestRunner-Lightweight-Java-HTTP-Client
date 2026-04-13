<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.restrunner.web.db.WebDB" %>
<%@ page import="java.time.LocalDateTime" %>
<html>
<head>
    <title>History - RestRunner Web</title>
    <style>
        body { font-family: sans-serif; padding: 2rem; background-color: #f4f4f9; }
        .container { max-width: 1000px; margin: 0 auto; background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        h2 { color: #333; }
        table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
        th, td { border: 1px solid #ddd; padding: 0.75rem; text-align: left; }
        th { background-color: #f8f9fa; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd; padding-bottom: 1rem; margin-bottom: 1rem; }
        .login-time { font-size: 0.9rem; color: #666; }
        .logout { color: #dc3545; text-decoration: none; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div>
                <h2>Welcome, <%= session.getAttribute("user") %></h2>
                <% 
                    LocalDateTime lastLogin = WebDB.getInstance().getLastLogin((String)session.getAttribute("user"));
                %>
                <p class="login-time">Your last login: <%= lastLogin != null ? lastLogin.toString() : "First time" %></p>
            </div>
            <a href="<%= request.getContextPath() %>/logout" class="logout">Logout</a>
        </div>

        <h3>API History</h3>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Method</th>
                    <th>URI</th>
                    <th>Created At</th>
                </tr>
            </thead>
            <tbody>
                <% 
                    List<Map<String, Object>> history = (List<Map<String, Object>>) request.getAttribute("history");
                    if (history == null) {
                        // In case accessed directly, redirect to servlet
                        response.sendRedirect(request.getContextPath() + "/history");
                        return;
                    }
                    for (Map<String, Object> row : history) {
                %>
                <tr>
                    <td><%= row.get("id") %></td>
                    <td><strong><%= row.get("method") %></strong></td>
                    <td><%= row.get("uri") %></td>
                    <td><%= row.get("created_at") %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</body>
</html>
