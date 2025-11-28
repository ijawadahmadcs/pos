// POSApplication.java
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class POSApplication {
    private static User currentUser;
    private static DatabaseManager db;
    private static JFrame mainFrame;

    // Theme (adapted to match the RideSharing reference look)
    public static final Color PRIMARY_COLOR = new Color(0x6A, 0x1B, 0x9A);
    public static final Color PRIMARY_LIGHT = new Color(0x9C, 0x4D, 0xCC);
    public static final Color PRIMARY_DARK = new Color(0x4A, 0x14, 0x8C);
    public static final Color BACKGROUND = new Color(0xFA, 0xFA, 0xFB);
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color TEXT_DARK = new Color(51, 51, 51);
    public static final Color TEXT_SECONDARY = new Color(102, 102, 102);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        db = new DatabaseManager();
        SwingUtilities.invokeLater(POSApplication::showLoginScreen);
    }

    // ---------- Login screen ----------

    public static void showLoginScreen() {
        JFrame f = new JFrame("POS System - Login");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(480, 560);
        f.setLocationRelativeTo(null);
        f.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        // Gradient header
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 140));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JLabel title = new JLabel("POS System");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_LIGHT);

        JLabel subtitle = new JLabel("Sign in to manage sales and inventory");
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(new Color(255, 255, 255, 200));

        JPanel left = new JPanel(new GridLayout(2,1));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);
        header.add(left, BorderLayout.WEST);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 12, 0);
        c.weightx = 1.0;

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(FONT_BODY);
        userLabel.setForeground(TEXT_DARK);
        c.gridy = 0;
        content.add(userLabel, c);

        c.gridy = 1;
        JTextField user = new JTextField();
        styleTextField(user);
        content.add(user, c);

        c.gridy = 2;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(FONT_BODY);
        passLabel.setForeground(TEXT_DARK);
        content.add(passLabel, c);

        c.gridy = 3;
        JPasswordField pass = new JPasswordField();
        styleTextField(pass);
        content.add(pass, c);

        c.gridy = 4;
        JButton login = createLargePrimaryButton("SIGN IN", PRIMARY_COLOR);
        login.setPreferredSize(new Dimension(0, 46));
        content.add(login, c);

        c.gridy = 5;
        JLabel info = new JLabel("Enter credentials to access POS dashboard", SwingConstants.CENTER);
        info.setFont(FONT_BODY);
        info.setForeground(TEXT_SECONDARY);
        content.add(info, c);

        login.addActionListener(e -> {
            currentUser = db.authenticateUser(user.getText(), new String(pass.getPassword()));
            if (currentUser != null) {
                f.dispose();
                showMainScreen();
            } else {
                JOptionPane.showMessageDialog(f, "Invalid username or password. Please try again.");
                pass.setText("");
            }
        });

        root.add(header, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        f.setContentPane(root);
        f.setVisible(true);
        SwingUtilities.invokeLater(user::requestFocusInWindow);
    }

    // ---------- Main dashboard ----------

    public static void showMainScreen() {
        mainFrame = new JFrame("POS System - " + (currentUser != null ? currentUser.getUsername() : "User"));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        // Top header (gradient)
        JPanel top = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        top.setPreferredSize(new Dimension(0, 80));
        top.setLayout(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel welcome = new JLabel("Welcome back, " + (currentUser != null ? currentUser.getUsername() : "User"));
        welcome.setFont(FONT_HEADER);
        welcome.setForeground(TEXT_LIGHT);
        top.add(welcome, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        JButton logout = createModernButton("Logout", PRIMARY_DARK);
        logout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.dispose();
                showLoginScreen();
            }
        });
        right.add(logout);
        top.add(right, BorderLayout.EAST);

        // Dashboard grid
        JPanel dashboard = new JPanel(new GridLayout(2,2,20,20));
        dashboard.setBackground(BACKGROUND);
        dashboard.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));

        dashboard.add(createDashboardCard("New Sale", "Process new customer transactions", e -> new SalesPanel(mainFrame, db, currentUser)));
        dashboard.add(createDashboardCard("Products", "Manage inventory and products", e -> new ProductsPanel(mainFrame, db)));
        dashboard.add(createDashboardCard("Customers", "Manage customer database", e -> new CustomersPanel(mainFrame, db)));
        dashboard.add(createDashboardCard("Reports", "View sales and analytics", e -> new ReportsPanel(mainFrame, db)));

        root.add(top, BorderLayout.NORTH);
        root.add(dashboard, BorderLayout.CENTER);

        mainFrame.setContentPane(root);
        mainFrame.setVisible(true);
    }

    private static JPanel createDashboardCard(String title, String subtitle, java.util.function.Consumer<MouseEvent> onClick) {
        RoundedPanel card = new RoundedPanel(12, CARD_BACKGROUND);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0,0,0,20),1,true), BorderFactory.createEmptyBorder(18,18,18,18)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel t = new JLabel(title);
        t.setFont(FONT_SUBHEADER);
        t.setForeground(PRIMARY_DARK);

        JLabel s = new JLabel(subtitle);
        s.setFont(FONT_BODY);
        s.setForeground(TEXT_SECONDARY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(t, BorderLayout.NORTH);
        top.add(s, BorderLayout.CENTER);

        JButton action = createModernButton("Open", PRIMARY_COLOR);
        action.addActionListener(ae -> onClick.accept(null));

        card.add(top, BorderLayout.CENTER);
        card.add(action, BorderLayout.SOUTH);

        // hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(PRIMARY_COLOR,2,true), BorderFactory.createEmptyBorder(17,17,17,17)));
                card.setBackground(new Color(250, 250, 251));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0,0,0,20),1,true), BorderFactory.createEmptyBorder(18,18,18,18)));
                card.setBackground(CARD_BACKGROUND);
            }
            @Override public void mouseClicked(MouseEvent e) { onClick.accept(e); }
        });

        return card;
    }

    // ---------- Shared UI helpers ----------

    public static JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(color.darker());
                else if (getModel().isRollover()) g2.setColor(color.brighter());
                else g2.setColor(color);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_LIGHT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return button;
    }

    public static JButton createLargePrimaryButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,color,0,getHeight(),color.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        return btn;
    }

    public static void styleTextField(JTextField f) {
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0,0,0,40),1,true), BorderFactory.createEmptyBorder(8,10,8,10)));
        f.setPreferredSize(new Dimension(320,36));
        f.setBackground(Color.WHITE);
    }

    public static JPanel createStatCard(String title, String value, Color accent) {
        RoundedPanel c = new RoundedPanel(10, CARD_BACKGROUND);
        c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0,0,0,18),1,true), BorderFactory.createEmptyBorder(12,12,12,12)));
        c.setLayout(new BorderLayout(6,6));
        JLabel t = new JLabel(title);
        t.setFont(FONT_BODY);
        t.setForeground(TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(accent);
        c.add(t, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        return c;
    }

    // Public rounded panel so other panels can use it
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            super();
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}