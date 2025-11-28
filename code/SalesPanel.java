// SalesPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.List;

public class SalesPanel extends JDialog {
    private DatabaseManager db;
    private User currentUser;
    private DefaultTableModel productTableModel;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel, itemsLabel;
    private JTable productTable, cartTable;
    private Cart cart = new Cart();

    public SalesPanel(JFrame parent, DatabaseManager db, User user) {
        super(parent, "New Sale", true);
        this.db = db; this.currentUser = user;
        setSize(1200, 750);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(POSApplication.BACKGROUND);
        setLayout(new BorderLayout());

        // header
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0, POSApplication.PRIMARY_DARK, getWidth(),0,POSApplication.PRIMARY_LIGHT);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0,80));
        header.setLayout(new BorderLayout()); header.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));
        JLabel title = new JLabel("New Sale"); title.setFont(POSApplication.FONT_HEADER); title.setForeground(POSApplication.TEXT_LIGHT);
        header.add(title, BorderLayout.WEST);
        JButton close = POSApplication.createModernButton("Close", POSApplication.PRIMARY_DARK); close.addActionListener(e->dispose());
        header.add(close, BorderLayout.EAST);

        // main content: two columns
        JPanel content = new JPanel(new GridLayout(1,2,20,0));
        content.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        content.setBackground(POSApplication.BACKGROUND);

        // Products panel
        JPanel productsPanel = new POSApplication.RoundedPanel(10, POSApplication.CARD_BACKGROUND);
        productsPanel.setLayout(new BorderLayout(8,8));
        productsPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JLabel pTitle = new JLabel("Available Products"); pTitle.setFont(POSApplication.FONT_SUBHEADER); pTitle.setForeground(POSApplication.PRIMARY_DARK);
        productsPanel.add(pTitle, BorderLayout.NORTH);

        productTableModel = new DefaultTableModel(new String[]{"ID","Product","Price","Stock"},0){ public boolean isCellEditable(int r,int c){return false;} };
        productTable = new JTable(productTableModel); styleTable(productTable);
        productsPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel addControls = new JPanel(new FlowLayout(FlowLayout.LEFT)); addControls.setOpaque(false);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1,1,100,1));
        qtySpinner.setPreferredSize(new Dimension(80,36));
        JButton addBtn = POSApplication.createModernButton("Add to Cart", POSApplication.PRIMARY_COLOR);
        addBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this,"Select product."); return; }
            int pid = (int) productTableModel.getValueAt(row,0);
            int q = (int) qtySpinner.getValue();
            addToCart(pid, q);
            qtySpinner.setValue(1);
        });
        addControls.add(new JLabel("Quantity:")); addControls.add(qtySpinner); addControls.add(addBtn);
        productsPanel.add(addControls, BorderLayout.SOUTH);

        // Cart panel
        JPanel cartPanel = new POSApplication.RoundedPanel(10, POSApplication.CARD_BACKGROUND);
        cartPanel.setLayout(new BorderLayout(8,8)); cartPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JLabel cTitle = new JLabel("Shopping Cart"); cTitle.setFont(POSApplication.FONT_SUBHEADER); cTitle.setForeground(POSApplication.PRIMARY_DARK);
        cartPanel.add(cTitle, BorderLayout.NORTH);

        cartTableModel = new DefaultTableModel(new String[]{"Product","Price","Qty","Total"},0){ public boolean isCellEditable(int r,int c){return false;} };
        cartTable = new JTable(cartTableModel); styleTable(cartTable);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JButton remove = POSApplication.createModernButton("Remove Selected", POSApplication.PRIMARY_LIGHT);
        JButton clear = POSApplication.createModernButton("Clear Cart", POSApplication.PRIMARY_DARK);
        remove.addActionListener(e -> {
            int r = cartTable.getSelectedRow();
            if (r >= 0) { cartTableModel.removeRow(r); cart.getItems().remove(r); updateCartSummary(); }
            else JOptionPane.showMessageDialog(this,"Select an item to remove.");
        });
        clear.addActionListener(e -> {
            if (cartTableModel.getRowCount() > 0) {
                int res = JOptionPane.showConfirmDialog(this,"Clear all items?","Confirm",JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) { cartTableModel.setRowCount(0); cart.getItems().clear(); updateCartSummary(); }
            }
        });
        actions.add(remove); actions.add(clear);

        JPanel checkout = new JPanel(new BorderLayout()); checkout.setOpaque(false);
        totalLabel = new JLabel("Total: Rs. 0.00", SwingConstants.CENTER); totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20)); totalLabel.setForeground(POSApplication.PRIMARY_DARK);
        itemsLabel = new JLabel("0 items"); itemsLabel.setFont(POSApplication.FONT_BODY); itemsLabel.setForeground(POSApplication.TEXT_SECONDARY);
        checkout.add(totalLabel, BorderLayout.NORTH);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setOpaque(false);
        JButton cancel = POSApplication.createModernButton("Cancel Sale", POSApplication.PRIMARY_DARK);
        JButton complete = POSApplication.createModernButton("Complete Sale", POSApplication.PRIMARY_COLOR);
        cancel.addActionListener(e -> dispose());
        complete.addActionListener(e -> completeSale());
        btns.add(cancel); btns.add(complete);
        checkout.add(btns, BorderLayout.SOUTH);

        JPanel bottomRight = new JPanel(new BorderLayout()); bottomRight.setOpaque(false);
        bottomRight.add(actions, BorderLayout.NORTH); bottomRight.add(checkout, BorderLayout.SOUTH);
        cartPanel.add(bottomRight, BorderLayout.SOUTH);

        content.add(productsPanel);
        content.add(cartPanel);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        loadProducts();
        updateCartSummary();
        setVisible(true);
    }

    private void loadProducts() {
        productTableModel.setRowCount(0);
        List<Product> list = db.getAllProducts();
        if (list != null) {
            for (Product p : list) {
                if (p.getStockQuantity() > 0) {
                    productTableModel.addRow(new Object[]{p.getProductId(), p.getName(), String.format("Rs. %.2f", p.getPrice()), p.getStockQuantity()});
                }
            }
        }
    }

    private void addToCart(int productId, int quantity) {
        Product p = db.getProductById(productId);
        if (p == null) return;
        if (p.getStockQuantity() < quantity) { JOptionPane.showMessageDialog(this,"Insufficient stock."); return; }
        double itemTotal = p.getPrice() * quantity;
        boolean found = false;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            if (cartTableModel.getValueAt(i,0).equals(p.getName())) {
                int newQty = Integer.parseInt(cartTableModel.getValueAt(i,2).toString()) + quantity;
                cartTableModel.setValueAt(newQty, i, 2);
                cartTableModel.setValueAt(String.format("Rs. %.2f", p.getPrice() * newQty), i, 3);
                found = true; break;
            }
        }
        if (!found) cartTableModel.addRow(new Object[]{p.getName(), String.format("Rs. %.2f", p.getPrice()), quantity, String.format("Rs. %.2f", itemTotal)});
        cart.addItem(p, quantity);
        updateCartSummary();
    }

    private void updateCartSummary() {
        double total = cart.getTotal();
        int itemCount = cart.getItems().size();
        totalLabel.setText(String.format("Total: Rs. %.2f", total));
        itemsLabel.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));
    }

    private void completeSale() {
        if (cart.getItems().isEmpty()) { JOptionPane.showMessageDialog(this,"Cart empty."); return; }
        int orderId = db.createOrder(cart, currentUser, null);
        if (orderId > 0) {
            JOptionPane.showMessageDialog(this, "<html><b>Sale Completed</b><br>Order ID: " + orderId + "<br>Total: " + String.format("Rs. %.2f", cart.getTotal()) + "</html>");
            dispose();
        } else JOptionPane.showMessageDialog(this,"Sale failed.");
    }

    private void styleTable(JTable t) {
        t.setFont(POSApplication.FONT_BODY);
        t.setRowHeight(34);
        t.setSelectionBackground(new Color(0xEE,0xE6,0xFF));
        JTableHeader h = t.getTableHeader(); h.setOpaque(true); h.setBackground(POSApplication.PRIMARY_COLOR); h.setForeground(POSApplication.TEXT_LIGHT); h.setFont(POSApplication.FONT_BUTTON);
        h.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                lbl.setBackground(POSApplication.PRIMARY_COLOR);
                lbl.setForeground(POSApplication.TEXT_LIGHT);
                lbl.setFont(POSApplication.FONT_BUTTON);
                lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
    }
}