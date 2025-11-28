// ProductsPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.List;

public class ProductsPanel extends JDialog {
    private DatabaseManager db;
    private DefaultTableModel tableModel;
    private JTable table;

    public ProductsPanel(JFrame parent, DatabaseManager db) {
        super(parent, "Product Management", true);
        this.db = db;
        setSize(1000, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(POSApplication.BACKGROUND);

        // header
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0, POSApplication.PRIMARY_DARK, getWidth(), 0, POSApplication.PRIMARY_LIGHT);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0,80));
        header.setLayout(new BorderLayout()); header.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));
        JLabel title = new JLabel("Product Management"); title.setFont(POSApplication.FONT_HEADER); title.setForeground(POSApplication.TEXT_LIGHT);
        header.add(title, BorderLayout.WEST);
        JButton close = POSApplication.createModernButton("Close", POSApplication.PRIMARY_DARK); close.addActionListener(e->dispose());
        header.add(close, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(12,12));
        content.setBackground(POSApplication.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        JPanel statsRow = new JPanel(new GridLayout(1,3,12,0)); statsRow.setOpaque(false);
        List<Product> products = db.getAllProducts();
        int total = products == null ? 0 : products.size();
        int low = products == null ? 0 : (int) products.stream().filter(p -> p.getStockQuantity() < 10).count();
        double value = products == null ? 0 : products.stream().mapToDouble(p -> p.getPrice() * p.getStockQuantity()).sum();
        statsRow.add(POSApplication.createStatCard("Total Products", String.valueOf(total), POSApplication.PRIMARY_COLOR));
        statsRow.add(POSApplication.createStatCard("Low Stock", String.valueOf(low), POSApplication.PRIMARY_LIGHT));
        statsRow.add(POSApplication.createStatCard("Inventory Value", String.format("Rs. %.2f", value), POSApplication.PRIMARY_DARK));
        content.add(statsRow, BorderLayout.NORTH);

        String[] cols = {"ID","Product Name","Category","Price","Stock","Status"};
        tableModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel); styleTable(table);
        JScrollPane sp = new JScrollPane(table); sp.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,18),1,true));
        content.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setBackground(POSApplication.BACKGROUND);
        JButton add = POSApplication.createModernButton("Add Product", POSApplication.PRIMARY_COLOR);
        JButton update = POSApplication.createModernButton("Update Stock", POSApplication.PRIMARY_LIGHT);
        JButton refresh = POSApplication.createModernButton("Refresh", POSApplication.PRIMARY_DARK);
        add.addActionListener(e -> showAddProductDialog());
        update.addActionListener(e -> showUpdateStockDialog());
        refresh.addActionListener(e -> loadProducts());
        actions.add(add); actions.add(update); actions.add(refresh);
        content.add(actions, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        loadProducts();
        setVisible(true);
    }

    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> list = db.getAllProducts();
        if (list != null) {
            for (Product p : list) {
                String cat = db.getCategoryName(p.getCategoryId());
                String status = p.getStockQuantity() == 0 ? "Out of Stock" : p.getStockQuantity() < 10 ? "Low Stock" : "In Stock";
                tableModel.addRow(new Object[]{p.getProductId(), p.getName(), cat, String.format("Rs. %.2f", p.getPrice()), p.getStockQuantity(), status});
            }
        }
    }

    private void showAddProductDialog() {
        JDialog d = new JDialog(this, "Add Product", true);
        d.setSize(440,460); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(POSApplication.CARD_BACKGROUND); p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;

        JTextField name = new JTextField(); POSApplication.styleTextField(name);
        JTextField price = new JTextField(); POSApplication.styleTextField(price);
        JTextField stock = new JTextField(); POSApplication.styleTextField(stock);

        p.add(new JLabel("Product Name:"), gbc); gbc.gridy++; p.add(name, gbc);
        gbc.gridy++; p.add(new JLabel("Price (Rs):"), gbc); gbc.gridy++; p.add(price, gbc);
        gbc.gridy++; p.add(new JLabel("Initial Stock:"), gbc); gbc.gridy++; p.add(stock, gbc);

        gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(POSApplication.CARD_BACKGROUND);
        JButton save = POSApplication.createModernButton("Save", POSApplication.PRIMARY_COLOR);
        JButton cancel = POSApplication.createModernButton("Cancel", POSApplication.PRIMARY_DARK);
        save.addActionListener(e -> {
            try {
                String n = name.getText().trim();
                double pr = Double.parseDouble(price.getText().trim());
                int st = Integer.parseInt(stock.getText().trim());
                if (n.isEmpty()) { JOptionPane.showMessageDialog(d,"Product name required."); return; }
                db.addProduct(n, 1, pr, st);
                loadProducts(); d.dispose(); JOptionPane.showMessageDialog(this,"Product added.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Enter valid numeric values."); }
        });
        cancel.addActionListener(e -> d.dispose());
        btns.add(save); btns.add(cancel); p.add(btns, gbc);

        d.add(p); d.setVisible(true);
    }

    private void showUpdateStockDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,"Select a product to update."); return; }
        int pid = (int) tableModel.getValueAt(row,0);
        String pname = (String) tableModel.getValueAt(row,1);
        int current = (int) tableModel.getValueAt(row,4);

        JDialog d = new JDialog(this, "Update Stock - " + pname, true);
        d.setSize(400,300); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(POSApplication.CARD_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;
        JTextField qty = new JTextField(); POSApplication.styleTextField(qty);
        p.add(new JLabel("Current Stock: " + current), gbc); gbc.gridy++; p.add(new JLabel("Quantity to Add:"), gbc); gbc.gridy++; p.add(qty, gbc);
        gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(POSApplication.CARD_BACKGROUND);
        JButton ok = POSApplication.createModernButton("Update", POSApplication.PRIMARY_COLOR);
        ok.addActionListener(e -> {
            try {
                int q = Integer.parseInt(qty.getText().trim());
                if (q <= 0) { JOptionPane.showMessageDialog(d,"Enter positive number."); return; }
                db.addStock(pid, q);
                loadProducts(); d.dispose(); JOptionPane.showMessageDialog(this,"Stock updated.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(d,"Invalid number."); }
        });
        JButton cancel = POSApplication.createModernButton("Cancel", POSApplication.PRIMARY_DARK); cancel.addActionListener(e->d.dispose());
        btns.add(ok); btns.add(cancel); p.add(btns, gbc);
        d.add(p); d.setVisible(true);
    }

    private void styleTable(JTable t) {
        t.setFont(POSApplication.FONT_BODY);
        t.setRowHeight(34);
        t.setSelectionBackground(new Color(0xEE,0xE6,0xFF));
        JTableHeader h = t.getTableHeader();
        h.setOpaque(true);
        h.setBackground(POSApplication.PRIMARY_COLOR);
        h.setForeground(POSApplication.TEXT_LIGHT);
        h.setFont(POSApplication.FONT_BUTTON);
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
        // status renderer
        t.getColumnModel().getColumn(5).setCellRenderer((javax.swing.table.TableCellRenderer)new javax.swing.table.DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if ("In Stock".equals(value)) setForeground(new Color(11,71,20));
                else if ("Low Stock".equals(value)) setForeground(new Color(193,125,0));
                else if ("Out of Stock".equals(value)) setForeground(new Color(200,0,0));
                else setForeground(POSApplication.TEXT_DARK);
                setFont(getFont().deriveFont(Font.BOLD));
                return c;
            }
        });
    }
}