// CustomersPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.List;

public class CustomersPanel extends JDialog {
    private DatabaseManager db;
    private DefaultTableModel tableModel;
    private JTable customerTable;

    public CustomersPanel(JFrame parent, DatabaseManager db) {
        super(parent, "Customer Management", true);
        this.db = db;
        setSize(920, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(POSApplication.BACKGROUND);

        // Header (gradient look)
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0, POSApplication.PRIMARY_DARK, getWidth(), 0, POSApplication.PRIMARY_LIGHT);
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0,80));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
        JLabel title = new JLabel("Customer Management");
        title.setFont(POSApplication.FONT_HEADER);
        title.setForeground(POSApplication.TEXT_LIGHT);
        header.add(title, BorderLayout.WEST);
        JButton close = POSApplication.createModernButton("Close", POSApplication.PRIMARY_DARK);
        close.addActionListener(e -> dispose());
        header.add(close, BorderLayout.EAST);

        // Info & actions
        JPanel content = new JPanel(new BorderLayout(12,12));
        content.setBackground(POSApplication.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        JPanel infoCard = POSApplication.createStatCard("Customer Database", "Manage and search customers", POSApplication.PRIMARY_COLOR);
        infoCard.setPreferredSize(new Dimension(0,70));
        content.add(infoCard, BorderLayout.NORTH);

        // Table center
        String[] cols = {"ID", "Name", "Phone"};
        tableModel = new DefaultTableModel(cols, 0) { 
            public boolean isCellEditable(int r, int c) { return false; } 
        };
        customerTable = new JTable(tableModel);
        styleTable(customerTable);
        JScrollPane sp = new JScrollPane(customerTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,18),1,true));
        content.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setBackground(POSApplication.BACKGROUND);
        JButton addBtn = POSApplication.createModernButton("Add Customer", POSApplication.PRIMARY_COLOR);
        JButton refreshBtn = POSApplication.createModernButton("Refresh", POSApplication.PRIMARY_LIGHT);
        addBtn.addActionListener(e -> showAddCustomerDialog());
        refreshBtn.addActionListener(e -> loadCustomers());
        actions.add(addBtn); actions.add(refreshBtn);
        content.add(actions, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        loadCustomers();
        setVisible(true);
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);
        List<Customer> list = db.getAllCustomers();
        if (list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customers found.");
        } else {
            for (Customer c : list) tableModel.addRow(new Object[]{c.getCustomerId(), c.getName(), c.getPhone()});
        }
    }

    private void showAddCustomerDialog() {
        JDialog d = new JDialog(this, "Add New Customer", true);
        d.setSize(420, 360);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(POSApplication.CARD_BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx=0; gbc.gridy=0;

        JTextField name = new JTextField(); POSApplication.styleTextField(name);
        JTextField phone = new JTextField(); POSApplication.styleTextField(phone);
        JTextField email = new JTextField(); POSApplication.styleTextField(email);

        p.add(new JLabel("Customer Name:"), gbc); gbc.gridy++; p.add(name, gbc);
        gbc.gridy++; p.add(new JLabel("Phone Number:"), gbc); gbc.gridy++; p.add(phone, gbc);
        gbc.gridy++; p.add(new JLabel("Email (optional):"), gbc); gbc.gridy++; p.add(email, gbc);

        gbc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER)); btns.setBackground(POSApplication.CARD_BACKGROUND);
        JButton save = POSApplication.createModernButton("Save", POSApplication.PRIMARY_COLOR);
        JButton cancel = POSApplication.createModernButton("Cancel", POSApplication.PRIMARY_DARK);
        save.addActionListener(e -> {
            if (name.getText().trim().isEmpty() || phone.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Please fill Name and Phone.");
                return;
            }
            db.addCustomer(name.getText().trim(), phone.getText().trim());
            loadCustomers();
            d.dispose();
            JOptionPane.showMessageDialog(this, "Customer added.");
        });
        cancel.addActionListener(e -> d.dispose());
        btns.add(save); btns.add(cancel);
        p.add(btns, gbc);

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
    }
}