package com.restrunner;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Modern Swing App — with Page Navigation using CardLayout
 * Think of CardLayout like a router (React Router / Vue Router)
 * Each "card" = a page/view
 */
public class ModernSwingApp extends JFrame {

    // ── Colors ─────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(15, 15, 30);
    private static final Color BG_HEADER    = new Color(18, 22, 45);
    private static final Color BG_CARD      = new Color(22, 27, 50);
    private static final Color TEXT_GREEN   = new Color(80, 220, 120);
    private static final Color TEXT_WHITE   = new Color(220, 220, 255);
    private static final Color TEXT_DIM     = new Color(90, 100, 130);
    private static final Color ACCENT_BLUE  = new Color(52, 152, 219);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color ACCENT_PURP  = new Color(155, 89, 182);
    private static final Color ACCENT_RED   = new Color(231, 76, 60);
    private static final Color BORDER_COLOR = new Color(30, 50, 100);
    private static final Color NAV_ACTIVE   = new Color(52, 152, 219);
    private static final Color NAV_INACTIVE = new Color(22, 27, 50);

    // CardLayout — the "router"
    private CardLayout cardLayout;
    private JPanel     pageContainer;

    // Nav buttons (to highlight active one)
    private JButton btnNavHome;
    private JButton btnNavAbout;
    private JButton btnNavConsole;

    // Console output
    private JTextArea outputArea;
    private int taskCounter = 0;

    // ── Constructor ────────────────────────────────────────────────────────
    public ModernSwingApp() {
        setTitle("Modern Swing App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 520);
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildPages(),     BorderLayout.CENTER);  // ← the "router outlet"
        add(buildStatusBar(), BorderLayout.SOUTH);

        navigateTo("home"); // default route
    }

    // ── Header + Nav Bar ──────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        JLabel title = new JLabel("  ⬡  MODERN SWING APP");
        title.setFont(new Font("Consolas", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));

        // Nav buttons — like <Link> tags in React Router
        btnNavHome    = createNavButton("🏠  Home");
        btnNavAbout   = createNavButton("👤  About");
        btnNavConsole = createNavButton("⚙️   Console");

        btnNavHome.addActionListener(e    -> navigateTo("home"));
        btnNavAbout.addActionListener(e   -> navigateTo("about"));
        btnNavConsole.addActionListener(e -> navigateTo("console"));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        nav.setBackground(BG_HEADER);
        nav.add(btnNavHome);
        nav.add(btnNavAbout);
        nav.add(btnNavConsole);

        header.add(title, BorderLayout.WEST);
        header.add(nav,   BorderLayout.EAST);
        return header;
    }

    // ── Page Container (CardLayout = Router) ──────────────────────────────
    private JPanel buildPages() {
        cardLayout    = new CardLayout();
        pageContainer = new JPanel(cardLayout);
        pageContainer.setBackground(BG_DARK);

        // Register pages — like defining routes
        pageContainer.add(buildHomePage(),    "home");
        pageContainer.add(buildAboutPage(),   "about");
        pageContainer.add(buildConsolePage(), "console");

        return pageContainer;
    }

    // ── Navigate — like router.push('/about') ─────────────────────────────
    private void navigateTo(String page) {
        cardLayout.show(pageContainer, page); // swap the visible card

        // Highlight active nav button
        btnNavHome.setBackground(page.equals("home")    ? NAV_ACTIVE : NAV_INACTIVE);
        btnNavAbout.setBackground(page.equals("about")  ? NAV_ACTIVE : NAV_INACTIVE);
        btnNavConsole.setBackground(page.equals("console") ? NAV_ACTIVE : NAV_INACTIVE);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PAGES — each method = a page component
    // ══════════════════════════════════════════════════════════════════════

    // ── Home Page ─────────────────────────────────────────────────────────
    private JPanel buildHomePage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));

        JLabel emoji = new JLabel("🏠");
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Welcome Home");
        heading.setFont(new Font("Consolas", Font.BOLD, 26));
        heading.setForeground(TEXT_WHITE);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Use the nav buttons above to switch pages.");
        sub.setFont(new Font("Consolas", Font.PLAIN, 13));
        sub.setForeground(TEXT_DIM);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton go = createButton("Go to Console →", ACCENT_BLUE);
        go.setAlignmentX(Component.CENTER_ALIGNMENT);
        go.addActionListener(e -> navigateTo("console"));

        card.add(emoji);
        card.add(Box.createVerticalStrut(14));
        card.add(heading);
        card.add(Box.createVerticalStrut(10));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(go);

        page.add(card);
        return page;
    }

    // ── About Page ────────────────────────────────────────────────────────
    private JPanel buildAboutPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));

        JLabel emoji = new JLabel("👤");
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("About This App");
        heading.setFont(new Font("Consolas", Font.BOLD, 26));
        heading.setForeground(TEXT_WHITE);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(emoji);
        card.add(Box.createVerticalStrut(14));
        card.add(heading);
        card.add(Box.createVerticalStrut(20));

        // Info rows — like a list of facts
        String[][] info = {
                { "Framework",  "Java Swing + AWT" },
                { "Navigation", "CardLayout  (like React Router)" },
                { "navigateTo()", "like router.push()" },
                { "JPanel page", "like a page/view component" },
                { "Threading",  "SwingUtilities.invokeLater()" },
        };

        for (String[] row : info) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
            rowPanel.setBackground(BG_CARD);
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel key = new JLabel(row[0] + "  ");
            key.setFont(new Font("Consolas", Font.BOLD, 13));
            key.setForeground(ACCENT_BLUE);

            JLabel val = new JLabel(row[1]);
            val.setFont(new Font("Consolas", Font.PLAIN, 13));
            val.setForeground(TEXT_DIM);

            rowPanel.add(key);
            rowPanel.add(val);
            card.add(rowPanel);
        }

        page.add(card);
        return page;
    }

    // ── Console Page ──────────────────────────────────────────────────────
    private JPanel buildConsolePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(BG_DARK);
        page.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

        JLabel outputLabel = new JLabel("OUTPUT CONSOLE");
        outputLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        outputLabel.setForeground(TEXT_DIM);
        outputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setBackground(new Color(10, 14, 20));
        outputArea.setForeground(TEXT_GREEN);
        outputArea.setCaretColor(TEXT_GREEN);
        outputArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(new Color(10, 14, 20));

        JButton btnHello = createButton("Say Hello",           ACCENT_GREEN);
        JButton btnTask  = createButton("Run Background Task", ACCENT_PURP);
        JButton btnClear = createButton("Clear",               ACCENT_RED);

        btnHello.addActionListener(e -> log("👋  Hello from the Console page!"));

        btnTask.addActionListener(e -> {
            taskCounter++;
            int id = taskCounter;
            btnTask.setEnabled(false);
            new Thread(() -> {
                SwingUtilities.invokeLater(() -> log("⚙️   Task #" + id + " started..."));
                for (int i = 1; i <= 3; i++) {
                    try { Thread.sleep(600); } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    final int step = i;
                    SwingUtilities.invokeLater(() -> log("   → Step " + step + "/3 done"));
                }
                SwingUtilities.invokeLater(() -> {
                    log("✅  Task #" + id + " complete!\n");
                    btnTask.setEnabled(true);
                });
            }).start();
        });

        btnClear.addActionListener(e -> outputArea.setText(""));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(BG_DARK);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(btnHello);
        btnRow.add(btnTask);
        btnRow.add(btnClear);

        page.add(outputLabel);
        page.add(Box.createVerticalStrut(6));
        page.add(scroll);
        page.add(Box.createVerticalStrut(12));
        page.add(btnRow);

        return page;
    }

    // ── Status Bar ────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Consolas", Font.PLAIN, 12));
        dot.setForeground(ACCENT_GREEN);
        JLabel status = new JLabel("Ready  |  CardLayout navigation active");
        status.setFont(new Font("Consolas", Font.PLAIN, 11));
        status.setForeground(TEXT_DIM);
        bar.add(dot);
        bar.add(status);
        return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void log(String msg) {
        outputArea.append(msg + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private JButton createButton(String text, Color base) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(base);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        Color hover = base.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(base); }
        });
        return btn;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(NAV_INACTIVE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 14, 6, 14));
        return btn;
    }

    // ── Entry Point ───────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ModernSwingApp().setVisible(true);
        });
    }
}