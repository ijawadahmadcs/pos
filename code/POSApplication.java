//javac -cp "libs/mysql-connector-j-9.5.0.jar" -d out code\*.java
//java -cp "out;libs/mysql-connector-j-9.5.0.jar" POSApplication

// POSApplication.java
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class POSApplication {
    private static User currentUser;
    private static DatabaseManager db;
    private static JFrame mainFrame;

    // Theme (adapted to match the RideSharing reference look)
    public static final Color PRIMARY_COLOR = new Color(0xE9, 0x1E, 0x63); // #E91E63
    public static final Color PRIMARY_LIGHT = new Color(0xFF, 0x6F, 0xA5); // lighter pink
    public static final Color PRIMARY_DARK = new Color(0xB0, 0x00, 0x54); // darker pink
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
        // Admin-only quick actions (navigation lives in the main view)
        if (currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole())) {
            // keep header compact; full navigation available below
        }
        top.add(right, BorderLayout.EAST);

        // Center area uses CardLayout to host embedded views
        final JPanel cardContainer = new JPanel(new CardLayout());
        cardContainer.setBackground(BACKGROUND);

        // instantiate views
        SalesView salesView = new SalesView(db, currentUser);
        ProductsView productsView = new ProductsView(db);
        CustomersView customersView = new CustomersView(db);
        ReportsView reportsView = new ReportsView(db);
        UsersView usersView = new UsersView(db);
        CategoriesView categoriesView = new CategoriesView(db);

        cardContainer.add(salesView, "sales");
        cardContainer.add(productsView, "products");
        cardContainer.add(customersView, "customers");
        cardContainer.add(reportsView, "reports");
        cardContainer.add(usersView, "users");
        cardContainer.add(categoriesView, "categories");

        // navigation / dashboard area
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BACKGROUND);
        dashboard.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        nav.setOpaque(false);

        // helper to show cards
        Runnable showSales = () -> { CardLayout cl = (CardLayout) cardContainer.getLayout(); cl.show(cardContainer, "sales"); };
        Runnable showProducts = () -> { CardLayout cl = (CardLayout) cardContainer.getLayout(); cl.show(cardContainer, "products"); };
        Runnable showCustomers = () -> { CardLayout cl = (CardLayout) cardContainer.getLayout(); cl.show(cardContainer, "customers"); };
        Runnable showReports = () -> { CardLayout cl = (CardLayout) cardContainer.getLayout(); cl.show(cardContainer, "reports"); };
        Runnable showUsers = () -> { CardLayout cl = (CardLayout) cardContainer.getLayout(); cl.show(cardContainer, "users"); };
        Runnable showCategories = () -> { CardLayout cl = (CardLayout) cardContainer.getLayout(); cl.show(cardContainer, "categories"); };

        JButton salesBtn = createModernButton("Sales", PRIMARY_COLOR);
        salesBtn.addActionListener(e -> showSales.run());
        nav.add(salesBtn);
        // Admin gets full navigation
        if (currentUser != null && "Cashier".equalsIgnoreCase(currentUser.getRole())) {
            // Cashier: only sales
            dashboard.add(nav, BorderLayout.NORTH);
            dashboard.add(cardContainer, BorderLayout.CENTER);
            root.add(top, BorderLayout.NORTH);
            root.add(dashboard, BorderLayout.CENTER);
            // default show sales
            showSales.run();
        } else {
            JButton productsBtn = createModernButton("Products", PRIMARY_LIGHT); productsBtn.addActionListener(e -> showProducts.run()); nav.add(productsBtn);
            JButton customersBtn = createModernButton("Customers", PRIMARY_LIGHT); customersBtn.addActionListener(e -> showCustomers.run()); nav.add(customersBtn);
            JButton reportsBtn = createModernButton("Reports", PRIMARY_LIGHT); reportsBtn.addActionListener(e -> showReports.run()); nav.add(reportsBtn);
            JButton usersBtn = createModernButton("Users", PRIMARY_LIGHT); usersBtn.addActionListener(e -> showUsers.run()); nav.add(usersBtn);
            JButton categoriesBtn = createModernButton("Categories", PRIMARY_LIGHT); categoriesBtn.addActionListener(e -> showCategories.run()); nav.add(categoriesBtn);

            dashboard.add(nav, BorderLayout.NORTH);
            dashboard.add(cardContainer, BorderLayout.CENTER);

            root.add(top, BorderLayout.NORTH);
            root.add(dashboard, BorderLayout.CENTER);

            // default show dashboard/front (sales)
            showSales.run();
        }

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

    // ---------- Embedded Views for single-window routing ----------
    public static class SalesView extends JPanel {
        private DatabaseManager db;
        private User user;
        private DefaultTableModel productTableModel, cartTableModel;
        private JTable productTable, cartTable;
        private Cart cart = new Cart();
        private JComboBox<String> customerCombo;
        private java.util.List<Customer> customersList = new java.util.ArrayList<>();
        private JLabel totalLabel;

        public SalesView(DatabaseManager db, User user) {
            this.db = db; this.user = user;
            setLayout(new BorderLayout(12,12)); setBackground(BACKGROUND); setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

            JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
            JLabel title = new JLabel("New Sale"); title.setFont(FONT_HEADER); title.setForeground(TEXT_DARK);
            top.add(title, BorderLayout.WEST);
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setOpaque(false);
            customerCombo = new JComboBox<>(); customerCombo.setPreferredSize(new Dimension(240,30));
            JButton newCust = createModernButton("New Customer", PRIMARY_LIGHT);
            newCust.addActionListener(e -> { addCustomerDialog(); refreshCustomers(); });
            right.add(customerCombo); right.add(newCust);
            top.add(right, BorderLayout.EAST);

            add(top, BorderLayout.NORTH);

            JPanel content = new JPanel(new GridLayout(1,2,12,0)); content.setOpaque(false);

            // products
            JPanel left = new JPanel(new BorderLayout(8,8)); left.setBackground(CARD_BACKGROUND); left.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            productTableModel = new DefaultTableModel(new String[]{"ID","Name","Price","Stock"},0) { public boolean isCellEditable(int r,int c){return false;} };
            productTable = new JTable(productTableModel); productTable.setRowHeight(28);
            left.add(new JScrollPane(productTable), BorderLayout.CENTER);
            JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT)); addRow.setOpaque(false);
            JSpinner qty = new JSpinner(new SpinnerNumberModel(1,1,100,1)); qty.setPreferredSize(new Dimension(80,28));
            JButton addBtn = createModernButton("Add to Cart", PRIMARY_COLOR);
            addBtn.addActionListener(e -> {
                int r = productTable.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Select product."); return; }
                int pid = (int) productTableModel.getValueAt(r,0); int q = (int) qty.getValue(); addToCart(pid, q);
            });
            addRow.add(new JLabel("Qty:")); addRow.add(qty); addRow.add(addBtn);
            left.add(addRow, BorderLayout.SOUTH);

            // cart
            JPanel rightPanel = new JPanel(new BorderLayout(8,8)); rightPanel.setBackground(CARD_BACKGROUND); rightPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            cartTableModel = new DefaultTableModel(new String[]{"Product","Price","Qty","Total"},0){ public boolean isCellEditable(int r,int c){return false;} };
            cartTable = new JTable(cartTableModel); cartTable.setRowHeight(28);
            rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
            JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false);
            totalLabel = new JLabel("Total: Rs. 0.00"); totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); totalLabel.setForeground(TEXT_DARK);
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setOpaque(false);
            JButton complete = createModernButton("Complete Sale", PRIMARY_COLOR);
            complete.addActionListener(e -> completeSale());
            JButton clear = createModernButton("Clear", PRIMARY_DARK);
            clear.addActionListener(e -> { cart.clear(); cartTableModel.setRowCount(0); updateTotal(); });
            btns.add(clear); btns.add(complete);
            bottom.add(totalLabel, BorderLayout.WEST); bottom.add(btns, BorderLayout.EAST);
            rightPanel.add(bottom, BorderLayout.SOUTH);

            content.add(left); content.add(rightPanel);
            add(content, BorderLayout.CENTER);

            refresh();
        }

        public void refresh() { loadProducts(); refreshCustomers(); updateTotal(); }

        private void loadProducts() {
            productTableModel.setRowCount(0);
            java.util.List<Product> list = db.getAllProducts();
            if (list != null) for (Product p : list) productTableModel.addRow(new Object[]{p.getProductId(), p.getName(), p.getPrice(), p.getStockQuantity()});
        }

        private void refreshCustomers() {
            customerCombo.removeAllItems(); customersList.clear();
            customerCombo.addItem("Walk-in (no customer)");
            java.util.List<Customer> list = db.getAllCustomers(); if (list != null) for (Customer c : list) { customersList.add(c); customerCombo.addItem(c.getCustomerId() + " - " + c.getName()); }
        }

        private void addToCart(int productId, int quantity) {
            Product p = db.getProductById(productId); if (p == null) return;
            if (p.getStockQuantity() < quantity) { JOptionPane.showMessageDialog(this, "Insufficient stock."); return; }
            boolean found = false;
            for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                if (cartTableModel.getValueAt(i,0).equals(p.getName())) {
                    int newQty = Integer.parseInt(cartTableModel.getValueAt(i,2).toString()) + quantity;
                    cartTableModel.setValueAt(newQty, i, 2);
                    cartTableModel.setValueAt(String.format("Rs. %.2f", p.getPrice() * newQty), i, 3);
                    found = true; break;
                }
            }
            if (!found) cartTableModel.addRow(new Object[]{p.getName(), String.format("Rs. %.2f", p.getPrice()), quantity, String.format("Rs. %.2f", p.getPrice() * quantity)});
            cart.addItem(p, quantity); updateTotal();
        }

        private void updateTotal() { totalLabel.setText(String.format("Total: Rs. %.2f", cart.getTotal())); }

        private void addCustomerDialog() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Customer", true); d.setSize(400,220); d.setLocationRelativeTo(this);
            JPanel p = new JPanel(new GridBagLayout()); p.setBackground(POSApplication.CARD_BACKGROUND); p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;
            JTextField name = new JTextField(); styleTextField(name);
            JTextField phone = new JTextField(); styleTextField(phone);
            p.add(new JLabel("Name:"), gbc); gbc.gridy++; p.add(name, gbc); gbc.gridy++; p.add(new JLabel("Phone:"), gbc); gbc.gridy++; p.add(phone, gbc);
            gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(POSApplication.CARD_BACKGROUND);
            JButton save = createModernButton("Save", PRIMARY_COLOR); save.addActionListener(a -> { if (name.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d, "Enter name."); return; } db.addCustomer(name.getText().trim(), phone.getText().trim()); d.dispose(); refreshCustomers(); });
            JButton cancel = createModernButton("Cancel", PRIMARY_DARK); cancel.addActionListener(a -> d.dispose()); btns.add(save); btns.add(cancel); p.add(btns, gbc);
            d.add(p); d.setVisible(true);
        }

        private void completeSale() {
            if (cart.getItems().isEmpty()) { JOptionPane.showMessageDialog(this, "Cart empty."); return; }
            Customer selected = null; int idx = customerCombo.getSelectedIndex(); if (idx > 0 && idx -1 < customersList.size()) selected = customersList.get(idx-1);
            int orderId = db.createOrder(cart, user, selected);
            if (orderId > 0) { JOptionPane.showMessageDialog(this, "Sale complete. Order ID: " + orderId); cart.clear(); cartTableModel.setRowCount(0); updateTotal(); loadProducts(); }
            else JOptionPane.showMessageDialog(this, "Sale failed.");
        }
    }

    public static class ProductsView extends JPanel {
        private DatabaseManager db; private DefaultTableModel model; private JTable table;
        public ProductsView(DatabaseManager db) {
            this.db = db; setLayout(new BorderLayout(8,8)); setBackground(BACKGROUND); setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            JLabel title = new JLabel("Products"); title.setFont(FONT_HEADER); title.setForeground(TEXT_DARK); add(title, BorderLayout.NORTH);
            model = new DefaultTableModel(new String[]{"ID","Name","Category","Price","Stock"},0){ public boolean isCellEditable(int r,int c){return false;} };
            table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
            JButton add = createModernButton("Add Product", PRIMARY_COLOR); add.addActionListener(e -> showAddDialog());
            JButton stock = createModernButton("Update Stock", PRIMARY_LIGHT); stock.addActionListener(e -> showUpdateDialog());
            JButton refresh = createModernButton("Refresh", PRIMARY_DARK); refresh.addActionListener(e -> refresh());
            actions.add(add); actions.add(stock); actions.add(refresh); add(actions, BorderLayout.SOUTH);
            refresh();
        }
        public void refresh() { model.setRowCount(0); java.util.List<Product> list = db.getAllProducts(); if (list != null) for (Product p: list) model.addRow(new Object[]{p.getProductId(), p.getName(), db.getCategoryName(p.getCategoryId()), String.format("Rs. %.2f", p.getPrice()), p.getStockQuantity()}); }
        private void showAddDialog() { JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Product", true); d.setSize(420,360); d.setLocationRelativeTo(this); JPanel p = new JPanel(new GridBagLayout()); p.setBackground(CARD_BACKGROUND); p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0; JTextField name = new JTextField(); styleTextField(name); JTextField price = new JTextField(); styleTextField(price); JTextField stock = new JTextField(); styleTextField(stock); JComboBox<String> catCombo = new JComboBox<>(); java.util.List<Category> cats = db.getAllCategories(); if (cats != null) for (Category c: cats) catCombo.addItem(c.getCategoryId()+" - "+c.getName()); p.add(new JLabel("Name:"), gbc); gbc.gridy++; p.add(name, gbc); gbc.gridy++; p.add(new JLabel("Price:"), gbc); gbc.gridy++; p.add(price, gbc); gbc.gridy++; p.add(new JLabel("Initial Stock:"), gbc); gbc.gridy++; p.add(stock, gbc); gbc.gridy++; p.add(new JLabel("Category:"), gbc); gbc.gridy++; p.add(catCombo, gbc); gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(CARD_BACKGROUND); JButton save = createModernButton("Save", PRIMARY_COLOR); save.addActionListener(a -> { try { String n = name.getText().trim(); double pr = Double.parseDouble(price.getText().trim()); int st = Integer.parseInt(stock.getText().trim()); int catId = cats.get(catCombo.getSelectedIndex()).getCategoryId(); if (n.isEmpty()) { JOptionPane.showMessageDialog(d,"Name required"); return; } db.addProduct(n, catId, pr, st); d.dispose(); refresh(); } catch(Exception ex) { JOptionPane.showMessageDialog(d, "Invalid input"); } }); JButton cancel = createModernButton("Cancel", PRIMARY_DARK); cancel.addActionListener(a->d.dispose()); btns.add(save); btns.add(cancel); p.add(btns, gbc); d.add(p); d.setVisible(true); }
        private void showUpdateDialog() { int r = table.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Select product."); return; } int pid = (int) model.getValueAt(r,0); String name = (String) model.getValueAt(r,1); JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Stock", true); d.setSize(360,200); d.setLocationRelativeTo(this); JPanel p = new JPanel(new GridBagLayout()); p.setBackground(CARD_BACKGROUND); GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0; JTextField qty = new JTextField(); styleTextField(qty); p.add(new JLabel("Product: " + name), gbc); gbc.gridy++; p.add(new JLabel("Quantity to add:"), gbc); gbc.gridy++; p.add(qty, gbc); gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(CARD_BACKGROUND); JButton ok = createModernButton("Update", PRIMARY_COLOR); ok.addActionListener(a -> { try { int q = Integer.parseInt(qty.getText().trim()); if (q<=0) { JOptionPane.showMessageDialog(d,"Positive number required"); return; } db.addStock(pid, q); d.dispose(); refresh(); } catch(Exception ex) { JOptionPane.showMessageDialog(d,"Invalid number"); } }); JButton cancel = createModernButton("Cancel", PRIMARY_DARK); cancel.addActionListener(a->d.dispose()); btns.add(ok); btns.add(cancel); p.add(btns, gbc); d.add(p); d.setVisible(true); }
    }

    public static class CustomersView extends JPanel {
        private DatabaseManager db; private DefaultTableModel model; private JTable table;
        public CustomersView(DatabaseManager db) { this.db = db; setLayout(new BorderLayout(8,8)); setBackground(BACKGROUND); setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); JLabel title = new JLabel("Customers"); title.setFont(FONT_HEADER); title.setForeground(TEXT_DARK); add(title, BorderLayout.NORTH); model = new DefaultTableModel(new String[]{"ID","Name","Phone"},0){ public boolean isCellEditable(int r,int c){return false;} }; table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER); JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); JButton add = createModernButton("Add Customer", PRIMARY_COLOR); add.addActionListener(e -> showAdd()); JButton refresh = createModernButton("Refresh", PRIMARY_LIGHT); refresh.addActionListener(e -> refresh()); actions.add(add); actions.add(refresh); add(actions, BorderLayout.SOUTH); refresh(); }
        public void refresh() { model.setRowCount(0); java.util.List<Customer> list = db.getAllCustomers(); if (list != null) for (Customer c: list) model.addRow(new Object[]{c.getCustomerId(), c.getName(), c.getPhone()}); }
        private void showAdd() { JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Customer", true); d.setSize(380,220); d.setLocationRelativeTo(this); JPanel p = new JPanel(new GridBagLayout()); p.setBackground(CARD_BACKGROUND); p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0; JTextField name = new JTextField(); styleTextField(name); JTextField phone = new JTextField(); styleTextField(phone); p.add(new JLabel("Name:"), gbc); gbc.gridy++; p.add(name, gbc); gbc.gridy++; p.add(new JLabel("Phone:"), gbc); gbc.gridy++; p.add(phone, gbc); gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(CARD_BACKGROUND); JButton save = createModernButton("Save", PRIMARY_COLOR); save.addActionListener(a -> { if (name.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(d, "Enter name."); return; } db.addCustomer(name.getText().trim(), phone.getText().trim()); d.dispose(); refresh(); }); JButton cancel = createModernButton("Cancel", PRIMARY_DARK); cancel.addActionListener(a->d.dispose()); btns.add(save); btns.add(cancel); p.add(btns, gbc); d.add(p); d.setVisible(true); }
    }

    public static class ReportsView extends JPanel {
        private DatabaseManager db; private DefaultTableModel model; private JTable table;
        public ReportsView(DatabaseManager db) { this.db = db; setLayout(new BorderLayout(8,8)); setBackground(BACKGROUND); setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); JLabel title = new JLabel("Reports"); title.setFont(FONT_HEADER); title.setForeground(TEXT_DARK); add(title, BorderLayout.NORTH); model = new DefaultTableModel(new String[]{"Order ID","Customer","Cashier","Amount","Date"},0){ public boolean isCellEditable(int r,int c){return false;} }; table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER); JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false); JButton refresh = createModernButton("Refresh", PRIMARY_COLOR); refresh.addActionListener(e -> refresh()); actions.add(refresh); add(actions, BorderLayout.SOUTH); refresh(); }
        public void refresh() { model.setRowCount(0); java.util.List<Order> list = db.getRecentOrders(100); if (list != null) for (Order o: list) model.addRow(new Object[]{o.getOrderId(), o.getCustomerName(), o.getUserName(), String.format("Rs. %.2f", o.getTotalAmount()), o.getOrderDate()}); }
    }

    public static class UsersView extends JPanel {
        private DatabaseManager db; private DefaultTableModel model; private JTable table;
        public UsersView(DatabaseManager db) { this.db = db; setLayout(new BorderLayout(8,8)); setBackground(BACKGROUND); setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); JLabel title = new JLabel("Users"); title.setFont(FONT_HEADER); title.setForeground(TEXT_DARK); add(title, BorderLayout.NORTH); model = new DefaultTableModel(new String[]{"ID","Username","Role"},0){ public boolean isCellEditable(int r,int c){return false;} }; table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER); JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); JButton add = createModernButton("Add User", PRIMARY_COLOR); add.addActionListener(e -> showAdd()); JButton refresh = createModernButton("Refresh", PRIMARY_LIGHT); refresh.addActionListener(e -> refresh()); actions.add(add); actions.add(refresh); add(actions, BorderLayout.SOUTH); refresh(); }
        public void refresh() { model.setRowCount(0); java.util.List<User> list = db.getAllUsers(); if (list != null) for (User u: list) model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole()}); }
        private void showAdd() { JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add User", true); d.setSize(420,300); d.setLocationRelativeTo(this); JPanel p = new JPanel(new GridBagLayout()); p.setBackground(CARD_BACKGROUND); p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0; JTextField username = new JTextField(); styleTextField(username); JPasswordField password = new JPasswordField(); styleTextField(password); JComboBox<String> role = new JComboBox<>(new String[]{"Admin","Cashier"}); role.setPreferredSize(new Dimension(320,36)); p.add(new JLabel("Username:"), gbc); gbc.gridy++; p.add(username, gbc); gbc.gridy++; p.add(new JLabel("Password:"), gbc); gbc.gridy++; p.add(password, gbc); gbc.gridy++; p.add(new JLabel("Role:"), gbc); gbc.gridy++; p.add(role, gbc); gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(CARD_BACKGROUND); JButton save = createModernButton("Save", PRIMARY_COLOR); save.addActionListener(a -> { String u = username.getText().trim(); String ps = new String(password.getPassword()); String r = (String) role.getSelectedItem(); if (u.isEmpty() || ps.isEmpty()) { JOptionPane.showMessageDialog(d, "Fill username and password."); return; } if (db.addUser(u, ps, r)) { JOptionPane.showMessageDialog(this, "User added."); refresh(); d.dispose(); } else JOptionPane.showMessageDialog(this, "Failed to add user."); }); JButton cancel = createModernButton("Cancel", PRIMARY_DARK); cancel.addActionListener(a -> d.dispose()); btns.add(save); btns.add(cancel); p.add(btns, gbc); d.add(p); d.setVisible(true); }
    }

    public static class CategoriesView extends JPanel {
        private DatabaseManager db; private DefaultTableModel model; private JTable table;
        public CategoriesView(DatabaseManager db) { this.db = db; setLayout(new BorderLayout(8,8)); setBackground(BACKGROUND); setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); JLabel title = new JLabel("Categories"); title.setFont(FONT_HEADER); title.setForeground(TEXT_DARK); add(title, BorderLayout.NORTH); model = new DefaultTableModel(new String[]{"ID","Name"},0){ public boolean isCellEditable(int r,int c){return false;} }; table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER); JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); JButton add = createModernButton("Add", PRIMARY_COLOR); add.addActionListener(e -> showAdd()); JButton edit = createModernButton("Edit", PRIMARY_LIGHT); edit.addActionListener(e -> showEdit()); JButton del = createModernButton("Delete", PRIMARY_DARK); del.addActionListener(e -> delSelected()); JButton refresh = createModernButton("Refresh", PRIMARY_LIGHT); refresh.addActionListener(e -> refresh()); actions.add(add); actions.add(edit); actions.add(del); actions.add(refresh); add(actions, BorderLayout.SOUTH); refresh(); }
        public void refresh() { model.setRowCount(0); java.util.List<Category> list = db.getAllCategories(); if (list != null) for (Category c: list) model.addRow(new Object[]{c.getCategoryId(), c.getName()}); }
        private void showAdd() { String name = JOptionPane.showInputDialog(this, "Category name:"); if (name != null && !name.trim().isEmpty()) { db.addCategory(name.trim()); refresh(); } }
        private void showEdit() { int r = table.getSelectedRow(); if (r<0) { JOptionPane.showMessageDialog(this, "Select category."); return; } int id = (int) model.getValueAt(r,0); String cur = (String) model.getValueAt(r,1); String name = JOptionPane.showInputDialog(this, "Edit name:", cur); if (name!=null && !name.trim().isEmpty()) { db.updateCategory(id, name.trim()); refresh(); } }
        private void delSelected() { int r = table.getSelectedRow(); if (r<0) { JOptionPane.showMessageDialog(this, "Select category."); return; } int id = (int) model.getValueAt(r,0); if (JOptionPane.showConfirmDialog(this, "Delete?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { db.deleteCategory(id); refresh(); } }
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