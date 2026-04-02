package com.restrunner;

import com.restrunner.core.user.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private JLabel errorLabel;

    public LoginFrame() {
        setTitle("RestRunner");
        setSize(420, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(24, 24, 24));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(37, 37, 38));
        card.setBorder(new EmptyBorder(30, 35, 30, 35));

        // LOGO / TITLE
        JLabel title = new JLabel("RestRunner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 180, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // USERNAME
        JLabel userLabel = new JLabel("Username");
        styleLabel(userLabel);

        JTextField usernameField = new JTextField();
        styleField(usernameField);

        // PASSWORD
        JLabel passLabel = new JLabel("Password");
        styleLabel(passLabel);

        JPasswordField passwordField = new JPasswordField();
        styleField(passwordField);

        // ERROR LABEL
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(255, 80, 80));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // BUTTON
        JButton loginBtn = new JButton("Sign In");
        styleButton(loginBtn);

        addHoverEffect(loginBtn);

        // ACTION
        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please enter username and password");
                return;
            }

            loginBtn.setText("Signing in...");
            loginBtn.setEnabled(false);

            // Simulate async (better UX)
            SwingUtilities.invokeLater(() -> {
                if (AuthService.login(username, password)) {
                    dispose();
                    new PostSwing().setVisible(true);
                } else {
                    showError("Invalid username or password");
                    loginBtn.setText("Sign In");
                    loginBtn.setEnabled(true);
                }
            });
        });

        // LAYOUT
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 25)));

        card.add(userLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(usernameField);

        card.add(Box.createRigidArea(new Dimension(0, 15)));

        card.add(passLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(passwordField);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(errorLabel);

        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(loginBtn);

        root.add(card);
        add(root);

        setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setBackground(new Color(60, 60, 60));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void styleLabel(JLabel label) {
        label.setForeground(new Color(200, 200, 200));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(255, 108, 55)); // refined orange
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 0, 10, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void addHoverEffect(JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(255, 130, 80));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(255, 108, 55));
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}