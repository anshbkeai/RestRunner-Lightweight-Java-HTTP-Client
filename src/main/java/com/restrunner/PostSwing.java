package com.restrunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.restrunner.core.engine.HttpEngine;
import com.restrunner.core.pojo.ApiRequest;
import com.restrunner.core.pojo.ApiResponse;
import com.restrunner.core.pojo.RequestMethod;
import com.restrunner.core.user.db.HistoryDB;
import com.restrunner.core.user.db.UserDB;
import com.restrunner.core.user.pojo.History;
import com.restrunner.core.user.service.AuthService;
import com.restrunner.core.user.service.SyncService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * PostSwing — A Postman-like HTTP Client built with Java Swing + AWT
 * Features: Sidebar collections, method selector, URL bar, Headers/Body tabs,
 *           Response panel with status, time, and pretty output.
 */
public class PostSwing extends JFrame {

    // ── Palette ────────────────────────────────────────────────────────────
    static final Color C_BG         = new Color(24, 24, 27);
    static final Color C_SIDEBAR    = new Color(18, 18, 21);
    static final Color C_PANEL      = new Color(30, 30, 35);
    static final Color C_PANEL2     = new Color(36, 36, 42);
    static final Color C_BORDER     = new Color(50, 50, 60);
    static final Color C_TEXT       = new Color(225, 225, 235);
    static final Color C_TEXT_DIM   = new Color(120, 120, 140);
    static final Color C_TEXT_MUTED = new Color(70, 70, 85);
    static final Color C_ACCENT     = new Color(255, 108, 55);   // Postman orange
    static final Color C_BLUE       = new Color(66, 153, 225);
    static final Color C_GREEN      = new Color(72, 199, 142);
    static final Color C_RED        = new Color(252, 92, 101);
    static final Color C_YELLOW     = new Color(254, 202, 87);
    static final Color C_PURPLE     = new Color(162, 113, 255);
    static final Color C_INPUT      = new Color(20, 20, 24);

    // Method colors
    static final Map<String, Color> METHOD_COLORS = Map.of(
            "GET",    new Color(72, 199, 142),
            "POST",   new Color(255, 169, 64),
            "PUT",    new Color(66, 153, 225),
            "PATCH",  new Color(162, 113, 255),
            "DELETE", new Color(252, 92, 101),
            "HEAD",   new Color(120, 120, 140),
            "OPTIONS",new Color(120, 120, 140)
    );

    // ── State ──────────────────────────────────────────────────────────────
    private JComboBox<String> methodBox;
    private JTextField        urlField;
    private JTabbedPane       reqTabs;
    private JTextArea         bodyArea;
    private JTextArea         responseArea;
    private JLabel            statusLabel;
    private JLabel            timeLabel;
    private JLabel            sizeLabel;
    private JPanel            statusDot;
    private DefaultTableModel headersModel;
    private DefaultTableModel paramsModel;
    private JLabel            responseTabLabel;
    private JTabbedPane       resTabs;

    // Sidebar history
    private DefaultListModel<String> historyModel;
    private JList<String>            historyList;

    public PostSwing() {
        setTitle("PostSwing — HTTP Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(C_BG);

        add(buildTitleBar(),  BorderLayout.NORTH);
        add(buildSidebar(),   BorderLayout.WEST);
        add(buildMain(),      BorderLayout.CENTER);

        getHistoryfromDb();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TITLE BAR
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(15, 15, 18));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
        bar.setPreferredSize(new Dimension(0, 42));

        // LEFT SIDE
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        left.setOpaque(false);

        JLabel logo = new JLabel("◈ PostSwing");
        logo.setFont(new Font("Consolas", Font.BOLD, 15));
        logo.setForeground(C_ACCENT);

        JLabel version = new JLabel("v1.0");
        version.setFont(new Font("Consolas", Font.PLAIN, 11));
        version.setForeground(C_TEXT_MUTED);

        left.add(logo);
        left.add(version);

        // CENTER TABS
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 8));
        tabs.setOpaque(false);

        for (String t : new String[]{"Collections", "Environments", "History"}) {
            JLabel tab = new JLabel(t);
            tab.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tab.setForeground(C_TEXT_DIM);
            tab.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            tabs.add(tab);
        }

        // RIGHT USER PANEL
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        right.setOpaque(false);

        JLabel userLabel = new JLabel("👤 " + UserDB.getInstance().getUser().get().getEmail()); // <-- use your variable
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Popup menu
        JPopupMenu menu = new JPopupMenu();
        JMenuItem logout = new JMenuItem("Logout");

        logout.addActionListener(e -> {
            AuthService.logout();

            dispose(); // close main window
            new LoginFrame(); // back to login
        });

        menu.add(logout);

        // Click to open menu
        userLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                menu.show(userLabel, 0, userLabel.getHeight());
            }
        });

        right.add(userLabel);

        bar.add(left, BorderLayout.WEST);
        bar.add(tabs, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }
    // ══════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(C_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(350, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, C_BORDER));

        // Sidebar header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_SIDEBAR);
        header.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));

        JLabel title = new JLabel("History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(C_TEXT_DIM);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setForeground(C_TEXT_MUTED);
        clearBtn.setBackground(C_SIDEBAR);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> historyModel.clear());

        header.add(title,    BorderLayout.WEST);
        header.add(clearBtn, BorderLayout.EAST);

        // History list
        historyModel = new DefaultListModel<>();
        historyList  = new JList<>(historyModel);
        historyList.setBackground(C_SIDEBAR);
        historyList.setForeground(C_TEXT);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 11));
        historyList.setSelectionBackground(new Color(40, 40, 50));
        historyList.setSelectionForeground(C_TEXT);
        historyList.setFixedCellHeight(42);
        historyList.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        historyList.setCellRenderer((list, value, index, sel, focus) -> {
            JPanel cell = new JPanel(new BorderLayout(6, 0));
            cell.setBackground(sel ? new Color(40, 40, 52) : C_SIDEBAR);
            cell.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 42)),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
            String[] parts = value.split(" ", 2);
            String method = parts[0];
            String url    = parts.length > 1 ? parts[1] : "";

            JLabel mLabel = new JLabel(method);
            mLabel.setFont(new Font("Consolas", Font.BOLD, 10));
            mLabel.setForeground(METHOD_COLORS.getOrDefault(method, C_TEXT_DIM));
            mLabel.setPreferredSize(new Dimension(52, 16));

            JLabel uLabel = new JLabel(url);
            uLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            uLabel.setForeground(sel ? C_TEXT : C_TEXT_DIM);

            cell.add(mLabel, BorderLayout.WEST);
            cell.add(uLabel, BorderLayout.CENTER);
            return cell;
        });

        // Click history to restore
        historyList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String val = historyList.getSelectedValue();
                if (val != null) {
                    String[] parts = val.split(" ", 2);
                    methodBox.setSelectedItem(parts[0]);
                    if (parts.length > 1) urlField.setText(parts[1]);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setBorder(null);
        scroll.setBackground(C_SIDEBAR);
        scroll.getViewport().setBackground(C_SIDEBAR);
        scroll.getVerticalScrollBar().setBackground(C_SIDEBAR);

        // Quick examples
        JPanel quickPanel = new JPanel();
        quickPanel.setLayout(new BoxLayout(quickPanel, BoxLayout.Y_AXIS));
        quickPanel.setBackground(C_SIDEBAR);
        quickPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JLabel quickLabel = new JLabel("  Quick Examples");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quickLabel.setForeground(C_TEXT_MUTED);
        quickLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));
        quickPanel.add(quickLabel);

        String[][] examples = {
                {"GET",  "https://jsonplaceholder.typicode.com/posts/1"},
                {"GET",  "https://jsonplaceholder.typicode.com/users"},
                {"POST", "https://jsonplaceholder.typicode.com/posts"},
                {"GET",  "https://api.github.com/zen"},
        };

        for (String[] ex : examples) {
            JPanel ep = new JPanel(new BorderLayout(6, 0));
            ep.setBackground(C_SIDEBAR);
            ep.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            ep.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel ml = new JLabel(ex[0]);
            ml.setFont(new Font("Consolas", Font.BOLD, 10));
            ml.setForeground(METHOD_COLORS.getOrDefault(ex[0], C_TEXT_DIM));
            ml.setPreferredSize(new Dimension(40, 14));

            String shortUrl = ex[1].replace("https://", "");
            shortUrl = shortUrl.length() > 24 ? shortUrl.substring(0, 24) + "…" : shortUrl;
            JLabel ul = new JLabel(shortUrl);
            ul.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            ul.setForeground(C_TEXT_DIM);

            ep.add(ml, BorderLayout.WEST);
            ep.add(ul, BorderLayout.CENTER);

            String finalMethod = ex[0];
            String finalUrl    = ex[1];
            ep.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    methodBox.setSelectedItem(finalMethod);
                    urlField.setText(finalUrl);
                }
                public void mouseEntered(MouseEvent e) { ep.setBackground(new Color(35,35,45)); ul.setForeground(C_TEXT); }
                public void mouseExited(MouseEvent e)  { ep.setBackground(C_SIDEBAR); ul.setForeground(C_TEXT_DIM); }
            });

            quickPanel.add(ep);
        }

        sidebar.add(header,     BorderLayout.NORTH);
        sidebar.add(scroll,     BorderLayout.CENTER);
        sidebar.add(quickPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);

        JButton syncBtn = new JButton("Sync to Server");
        syncBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        syncBtn.setForeground(Color.BLACK);
        syncBtn.setBackground(new Color(50, 120, 200));
        syncBtn.setFocusPainted(false);
        syncBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

// 👉 YOU will call your method here
        syncBtn.addActionListener(e -> syncHistory());

        rightPanel.add(syncBtn);
        rightPanel.add(clearBtn);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return sidebar;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MAIN AREA
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(C_BG);

        main.add(buildRequestBar(),   BorderLayout.NORTH);
        main.add(buildWorkspace(),    BorderLayout.CENTER);

        return main;
    }

    // ── URL / Method Bar ──────────────────────────────────────────────────
    private JPanel buildRequestBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(C_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));

        // Method dropdown
        String[] methods = {"GET","POST","PUT","PATCH","DELETE","HEAD","OPTIONS"};
        methodBox = new JComboBox<>(methods);
        methodBox.setFont(new Font("Consolas", Font.BOLD, 13));
        methodBox.setBackground(C_PANEL);
        methodBox.setForeground(C_GREEN);
        methodBox.setPreferredSize(new Dimension(105, 38));
        methodBox.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        methodBox.setFocusable(false);

        // Update method color on change
        methodBox.addActionListener(e -> {
            String m = (String) methodBox.getSelectedItem();
            methodBox.setForeground(METHOD_COLORS.getOrDefault(m, C_TEXT));
        });

        // URL field
        urlField = new JTextField("https://jsonplaceholder.typicode.com/posts/1");
        urlField.setFont(new Font("Consolas", Font.PLAIN, 13));
        urlField.setBackground(C_INPUT);
        urlField.setForeground(C_TEXT);
        urlField.setCaretColor(C_ACCENT);
        urlField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        urlField.addActionListener(e -> sendRequest()); // Enter to send

        // Send button
        JButton sendBtn = new JButton("  Send  ");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setBackground(C_ACCENT);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorderPainted(false);
        sendBtn.setOpaque(true);
        sendBtn.setPreferredSize(new Dimension(90, 38));
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendRequest());
        sendBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { sendBtn.setBackground(C_ACCENT.darker()); }
            public void mouseExited(MouseEvent e)  { sendBtn.setBackground(C_ACCENT); }
        });

        bar.add(methodBox, BorderLayout.WEST);
        bar.add(urlField,  BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        right.setOpaque(false);
        right.add(sendBtn);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Workspace (Request Tabs + Response) ───────────────────────────────
    private JSplitPane buildWorkspace() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildRequestTabs(),
                buildResponsePanel()
        );
        split.setDividerLocation(260);
        split.setDividerSize(4);
        split.setBackground(C_BORDER);
        split.setBorder(null);
        split.setContinuousLayout(true);
        return split;
    }

    // ── Request Tabs (Params / Headers / Body / Auth) ─────────────────────
    private JPanel buildRequestTabs() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(C_PANEL);
        wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        reqTabs = new JTabbedPane();
        reqTabs.setBackground(C_PANEL);
        reqTabs.setForeground(C_TEXT_DIM);
        reqTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        styleTabPane(reqTabs);

        reqTabs.addTab("Params",  buildParamsTab());
        reqTabs.addTab("Headers", buildHeadersTab());
        reqTabs.addTab("Body",    buildBodyTab());
        reqTabs.addTab("Auth",    buildAuthTab());

        wrapper.add(reqTabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildParamsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        String[] cols = {"", "Key", "Value", "Description"};
        paramsModel = new DefaultTableModel(cols, 0) {
            public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };
        paramsModel.addRow(new Object[]{true, "userId", "1", "Filter by user"});
        paramsModel.addRow(new Object[]{false, "", "", ""});

        JTable table = buildStyledTable(paramsModel);
        table.getColumnModel().getColumn(0).setMaxWidth(30);

        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);

        JPanel addRow = buildAddRowBar(() -> paramsModel.addRow(new Object[]{true, "", "", ""}));

        p.add(scroll, BorderLayout.CENTER);
        p.add(addRow, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildHeadersTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        String[] cols = {"", "Key", "Value", "Description"};
        headersModel = new DefaultTableModel(cols, 0) {
            public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };
        headersModel.addRow(new Object[]{true, "Content-Type", "application/json", ""});
        headersModel.addRow(new Object[]{true, "Accept",       "application/json", ""});
        headersModel.addRow(new Object[]{false, "", "", ""});

        JTable table = buildStyledTable(headersModel);
        table.getColumnModel().getColumn(0).setMaxWidth(30);

        JScrollPane scroll = new JScrollPane(table);
        styleScroll(scroll);

        JPanel addRow = buildAddRowBar(() -> headersModel.addRow(new Object[]{true, "", "", ""}));

        p.add(scroll, BorderLayout.CENTER);
        p.add(addRow, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildBodyTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(C_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Body type selector
        JPanel typeBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        typeBar.setBackground(C_PANEL);
        ButtonGroup grp = new ButtonGroup();
        for (String type : new String[]{"none", "raw", "form-data", "x-www-form-urlencoded"}) {
            JRadioButton rb = new JRadioButton(type);
            rb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            rb.setForeground(C_TEXT_DIM);
            rb.setBackground(C_PANEL);
            rb.setFocusPainted(false);
            if (type.equals("raw")) { rb.setSelected(true); rb.setForeground(C_ACCENT); }
            grp.add(rb);
            typeBar.add(rb);
        }

        // Format selector
        JComboBox<String> fmt = new JComboBox<>(new String[]{"JSON", "Text", "XML", "HTML"});
        fmt.setFont(new Font("Consolas", Font.PLAIN, 11));
        fmt.setBackground(C_PANEL2);
        fmt.setForeground(C_ACCENT);
        fmt.setFocusable(false);
        fmt.setBorder(BorderFactory.createLineBorder(C_BORDER));
        typeBar.add(fmt);

        bodyArea = new JTextArea("{\n  \"title\": \"Hello World\",\n  \"body\": \"This is a test\",\n  \"userId\": 1\n}");
        bodyArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        bodyArea.setBackground(C_INPUT);
        bodyArea.setForeground(new Color(180, 220, 255));
        bodyArea.setCaretColor(C_ACCENT);
        bodyArea.setTabSize(2);
        bodyArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        bodyArea.setLineWrap(false);

        JScrollPane scroll = new JScrollPane(bodyArea);
        styleScroll(scroll);

        p.add(typeBar, BorderLayout.NORTH);
        p.add(scroll,  BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAuthTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_PANEL);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(C_PANEL);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel("Auth Type");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(C_TEXT_DIM);

        JComboBox<String> authType = new JComboBox<>(new String[]{"No Auth", "Bearer Token", "Basic Auth", "API Key"});
        authType.setFont(new Font("Consolas", Font.PLAIN, 12));
        authType.setBackground(C_PANEL2);
        authType.setForeground(C_TEXT);
        authType.setMaximumSize(new Dimension(260, 34));

        JLabel tokenLbl = new JLabel("Token");
        tokenLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tokenLbl.setForeground(C_TEXT_DIM);

        JTextField tokenField = new JTextField("your-bearer-token-here");
        tokenField.setFont(new Font("Consolas", Font.PLAIN, 12));
        tokenField.setBackground(C_INPUT);
        tokenField.setForeground(C_TEXT);
        tokenField.setCaretColor(C_ACCENT);
        tokenField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tokenField.setMaximumSize(new Dimension(400, 34));

        inner.add(lbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(authType);
        inner.add(Box.createVerticalStrut(14));
        inner.add(tokenLbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(tokenField);

        p.add(inner);
        return p;
    }

    // ── Response Panel ────────────────────────────────────────────────────
    private JPanel buildResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_BG);

        // Response toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(C_PANEL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        left.setOpaque(false);

        JLabel resTitle = new JLabel("Response");
        resTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resTitle.setForeground(C_TEXT);

        // Status indicator
        statusDot = new JPanel();
        statusDot.setPreferredSize(new Dimension(8, 8));
        statusDot.setBackground(C_TEXT_MUTED);
        statusDot.setBorder(BorderFactory.createLineBorder(C_TEXT_MUTED.darker(), 1));

        statusLabel = new JLabel("—");
        statusLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        statusLabel.setForeground(C_TEXT_MUTED);

        timeLabel = new JLabel("— ms");
        timeLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        timeLabel.setForeground(C_TEXT_MUTED);

        sizeLabel = new JLabel("— B");
        sizeLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        sizeLabel.setForeground(C_TEXT_MUTED);

        left.add(resTitle);
        left.add(statusDot);
        left.add(statusLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        right.setOpaque(false);
        right.add(timeLabel);
        right.add(sizeLabel);

        toolbar.add(left,  BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        // Response tabs
        resTabs = new JTabbedPane();
        styleTabPane(resTabs);

        responseArea = new JTextArea();
        responseArea.setEditable(false);
        responseArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        responseArea.setBackground(C_INPUT);
        responseArea.setForeground(new Color(160, 210, 180));
        responseArea.setCaretColor(C_ACCENT);
        responseArea.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        responseArea.setText("Hit  Send  to get a response...");

        JScrollPane resScroll = new JScrollPane(responseArea);
        styleScroll(resScroll);

        JTextArea headersRes = new JTextArea();
        headersRes.setEditable(false);
        headersRes.setFont(new Font("Consolas", Font.PLAIN, 12));
        headersRes.setBackground(C_INPUT);
        headersRes.setForeground(new Color(180, 180, 220));
        headersRes.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        headersRes.setText("Send a request to see response headers...");
        JScrollPane headScroll = new JScrollPane(headersRes);
        styleScroll(headScroll);

        resTabs.addTab("Body",    resScroll);
        resTabs.addTab("Headers", headScroll);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(resTabs, BorderLayout.CENTER);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HTTP REQUEST LOGIC
    // ══════════════════════════════════════════════════════════════════════
    private void sendRequest() {
        String method = (String) methodBox.getSelectedItem();
        String url    = urlField.getText().trim();
        if (url.isEmpty()) return;

        // Validate method is known to the engine's enum
        RequestMethod requestMethod;
        try {
            requestMethod = RequestMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            responseArea.setText("Method \"" + method + "\" is not supported yet.\nAdd it to the engine to enable.");
            statusLabel.setText("N/A");
            statusLabel.setForeground(C_TEXT_MUTED);
            statusDot.setBackground(C_TEXT_MUTED);
            return;
        }

        responseArea.setText("Sending request...");
        statusLabel.setText("...");
        statusDot.setBackground(C_YELLOW);
        timeLabel.setText("— ms");
        sizeLabel.setText("— B");

        // Collect enabled headers from the table
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (int i = 0; i < headersModel.getRowCount(); i++) {
            Object enabled = headersModel.getValueAt(i, 0);
            Object key     = headersModel.getValueAt(i, 1);
            Object val     = headersModel.getValueAt(i, 2);
            if (Boolean.TRUE.equals(enabled) && key != null && !key.toString().isEmpty()
                    && val != null && !val.toString().isEmpty()) {
                headers.computeIfAbsent(key.toString(), k -> new ArrayList<>())
                       .add(val.toString());
            }
        }

        String bodyText = bodyArea.getText().trim();
        ApiRequest apiRequest = new ApiRequest(
                url,
                requestMethod,
                headers.isEmpty() ? null : headers,
                Duration.ofSeconds(15),
                bodyText.isEmpty() ? null : bodyText
        );

        long start = System.currentTimeMillis();

        CompletableFuture<ApiResponse> future;
        try {
            future = HttpEngine.getInstance().execute(apiRequest);
        } catch (Exception e) {
            // Engine threw synchronously — method not yet implemented
            responseArea.setText("Engine error: " + e.getMessage()
                    + "\nThis method may not be implemented in the engine yet.");
            statusLabel.setText("Error");
            statusLabel.setForeground(C_RED);
            statusDot.setBackground(C_RED);
            return;
        }

        future.thenAccept(response -> {
            long elapsed = System.currentTimeMillis() - start;
            SwingUtilities.invokeLater(() -> {
                if (response == null) {
                    responseArea.setText("No response received — connection may have failed.");
                    statusLabel.setText("Error");
                    statusLabel.setForeground(C_RED);
                    statusDot.setBackground(C_RED);
                    timeLabel.setText(elapsed + " ms");
                    return;
                }
                if (response.getError() != null) {
                    responseArea.setText("Error: " + response.getError());
                    int sc = response.getStatusCode();
                    statusLabel.setText(sc > 0 ? sc + " " + getStatusText(sc) : "Error");
                    statusLabel.setForeground(C_RED);
                    statusDot.setBackground(C_RED);
                    timeLabel.setText(elapsed + " ms");
                    return;
                }

                int    status   = response.getStatusCode();
                String respBody = response.getBody() != null ? response.getBody() : "";
                String pretty   = prettyPrintJson(respBody);
                int    size     = respBody.getBytes().length;

                StringBuilder hdr = new StringBuilder();
                if (response.getHeaders() != null) {
                    response.getHeaders().forEach((k, vals) ->
                            vals.forEach(v -> hdr.append(k).append(": ").append(v).append("\n")));
                }

                responseArea.setText(pretty);
                responseArea.setCaretPosition(0);

                Component hComp = resTabs.getComponentAt(1);
                if (hComp instanceof JScrollPane sp
                        && sp.getViewport().getView() instanceof JTextArea ta) {
                    ta.setText(hdr.toString());
                }

                statusLabel.setText(status + " " + getStatusText(status));
                statusLabel.setForeground(status < 300 ? C_GREEN : status < 400 ? C_YELLOW : C_RED);
                statusDot.setBackground(status < 300 ? C_GREEN : status < 400 ? C_YELLOW : C_RED);
                timeLabel.setText(elapsed + " ms");
                sizeLabel.setText(formatSize(size));

                savetoDB(apiRequest,response);
                getHistoryfromDb();

                // Save the Api restquest and Api respose for the user
            });
        }).exceptionally(ex -> {
            long elapsed = System.currentTimeMillis() - start;
            SwingUtilities.invokeLater(() -> {
                responseArea.setText("Request failed: " + ex.getMessage());
                statusLabel.setText("Error");
                statusLabel.setForeground(C_RED);
                statusDot.setBackground(C_RED);
                timeLabel.setText(elapsed + " ms");
            });
            return null;
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ══════════════════════════════════════════════════════════════════════
    private String prettyPrintJson(String raw) {
        if (raw == null || raw.isEmpty()) return "(empty response)";
        try {
            StringBuilder sb  = new StringBuilder();
            int indent = 0;
            boolean inStr = false;
            char prev = 0;
            for (char c : raw.toCharArray()) {
                if (c == '"' && prev != '\\') inStr = !inStr;
                if (!inStr) {
                    if (c == '{' || c == '[') {
                        sb.append(c).append('\n');
                        indent += 2;
                        sb.append(" ".repeat(indent));
                        prev = c; continue;
                    } else if (c == '}' || c == ']') {
                        sb.append('\n');
                        indent -= 2;
                        sb.append(" ".repeat(Math.max(0, indent))).append(c);
                        prev = c; continue;
                    } else if (c == ',') {
                        sb.append(c).append('\n').append(" ".repeat(indent));
                        prev = c; continue;
                    } else if (c == ':') {
                        sb.append(": ");
                        prev = c; continue;
                    } else if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                        prev = c; continue;
                    }
                }
                sb.append(c);
                prev = c;
            }
            return sb.toString();
        } catch (Exception e) {
            return raw;
        }
    }

    private String getStatusText(int code) {
        return switch (code) {
            case 200 -> "OK"; case 201 -> "Created"; case 204 -> "No Content";
            case 301 -> "Moved Permanently"; case 302 -> "Found";
            case 400 -> "Bad Request"; case 401 -> "Unauthorized";
            case 403 -> "Forbidden"; case 404 -> "Not Found";
            case 500 -> "Internal Server Error"; case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default  -> "";
        };
    }

    private String formatSize(int bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(C_PANEL);
        table.setForeground(C_TEXT);
        table.setFont(new Font("Consolas", Font.PLAIN, 12));
        table.setGridColor(C_BORDER);
        table.setRowHeight(28);
        table.setSelectionBackground(new Color(50, 60, 80));
        table.setSelectionForeground(C_TEXT);
        table.getTableHeader().setBackground(C_PANEL2);
        table.getTableHeader().setForeground(C_TEXT_DIM);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        return table;
    }

    private JPanel buildAddRowBar(Runnable onAdd) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        bar.setBackground(C_PANEL);
        JButton btn = new JButton("+ Add Row");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(C_ACCENT);
        btn.setBackground(C_PANEL);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onAdd.run());
        bar.add(btn);
        return bar;
    }

    private void styleTabPane(JTabbedPane tabs) {
        tabs.setBackground(C_PANEL);
        tabs.setForeground(C_TEXT_DIM);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabs.setBorder(null);
        UIManager.put("TabbedPane.selected",         C_BG);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        sp.getViewport().setBackground(C_INPUT);
        sp.getVerticalScrollBar().setBackground(C_PANEL);
        sp.getHorizontalScrollBar().setBackground(C_PANEL);
    }

    private void syncHistory() {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                try {
                    // 🔹 TODO: Replace with your DB fetch
                    new SyncService().SyncHistory();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(null, "History Synced!");
            }

        }.execute();
    }
    private void getHistoryfromDb() {
        historyModel.clear();

         HistoryDB.getInstance().getLast30().stream().forEach(x -> {

            historyModel.addElement(x.getMethod()   + " " + x.getUri());
         });
//        if (historyModel.size() > 30) historyModel.remove(historyModel.size() - 1);
    }

    private void savetoDB(ApiRequest apiRequest,ApiResponse response) {
        History history = new History();
        history.setResponse(response);
        history.setRequest(apiRequest);
        history.setUri(apiRequest.getUri());
        history.setMethod(apiRequest.getRequestMethod());


        HistoryDB.getInstance().save(history);
    }
    // ══════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        // Use system look for native scroll bars etc., then override colors
        try {
            SwingUtilities.invokeLater(() -> {

                if (UserDB.getInstance().getUser().isPresent()) {
                    new PostSwing().setVisible(true);
                } else {
                    new LoginFrame();
                }

            });
        }
        catch (Exception ignored) {}


    }
}