<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login - RestRunner Web</title>
    <style>
        body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f4f4f9; }
        .login-box { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); width: 300px; }
        h2 { margin-top: 0; color: #333; }
        input { width: 100%; padding: 0.5rem; margin: 0.5rem 0; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        button { width: 100%; padding: 0.5rem; background-color: #007bff; border: none; color: white; border-radius: 4px; cursor: pointer; }
        button:hover { background-color: #0056b3; }
        .error { color: red; font-size: 0.8rem; }
        .msg { color: green; font-size: 0.8rem; }
    </style>
</head>
<body>
    <div class="login-box">
        <h2>Login</h2>
        <% if(request.getParameter("error") != null) { %>
            <p class="error"><%= request.getParameter("error") %></p>
        <% } %>
        <% if(request.getParameter("msg") != null) { %>
            <p class="msg"><%= request.getParameter("msg") %></p>
        <% } %>
        <form action="<%= request.getContextPath() %>/api/login" method="post">
            <input type="text" name="username" placeholder="Username" required>
            <input type="password" name="password" placeholder="Password" required>
            <button type="submit">Login</button>
        </form>
        <p style="font-size: 0.8rem;">Don't have an account? <a href="signup.jsp">Signup</a></p>
    </div>
</body>
</html>
