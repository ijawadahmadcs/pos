// ReportsPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.List;

public class ReportsPanel extends JDialog {
    private DatabaseManager db;
    private DefaultTableModel tableModel;

    public ReportsPanel(JFrame parent, DatabaseManager db) {
        super(parent, "Sales Reports", true);
        this.db = db;
        setSize(1100, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(POSApplication.BACKGROUND);

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
        JLabel title = new JLabel("Sales Reports & Analytics"); title.setFont(POSApplication.FONT_HEADER); title.setForeground(POSApplication.TEXT_LIGHT);
        header.add(title, BorderLayout.WEST);
        JButton close = POSApplication.createModernButton("Close", POSApplication.PRIMARY_DARK); close.addActionListener(e->dispose());
        header.add(close, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(12,12));
        content.setBackground(POSApplication.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        SalesStats stats = db.getSalesStats();
        JPanel statsRow = new JPanel(new GridLayout(1,4,12,0)); statsRow.setOpaque(false);
        statsRow.add(POSApplication.createStatCard("Total Sales", String.format("Rs. %.2f", stats.totalSales), POSApplication.PRIMARY_COLOR));
        statsRow.add(POSApplication.createStatCard("Total Orders", String.valueOf(stats.totalOrders), POSApplication.PRIMARY_LIGHT));
        statsRow.add(POSApplication.createStatCard("Average Order", String.format("Rs. %.2f", stats.averageOrder), POSApplication.PRIMARY_DARK));
        statsRow.add(POSApplication.createStatCard("Today's Sales", String.format("Rs. %.2f", getTodaySales(stats)), POSApplication.PRIMARY_COLOR));
        content.add(statsRow, BorderLayout.NORTH);

        String[] cols = {"Order ID","Customer","Cashier","Amount","Date","Status"};
        tableModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable t = new JTable(tableModel); styleTable(t);
        JScrollPane sp = new JScrollPane(t); sp.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,18),1,true));
        content.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setBackground(POSApplication.BACKGROUND);
        JButton refresh = POSApplication.createModernButton("Refresh Data", POSApplication.PRIMARY_COLOR);
        refresh.addActionListener(e -> loadOrders());
        bottom.add(refresh);
        content.add(bottom, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        loadOrders();
        setVisible(true);
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = db.getRecentOrders(50);
        if (orders == null || orders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sales data available.");
        } else {
            for (Order o : orders) {
                tableModel.addRow(new Object[]{o.getOrderId(), o.getCustomerName(), o.getUserName(), String.format("Rs. %.2f", o.getTotalAmount()), o.getOrderDate(), "Completed"});
            }
        }
    }

    private double getTodaySales(SalesStats s) {
        if (s == null) return 0.0;
        try {
            java.lang.reflect.Method m = s.getClass().getMethod("getTodaySales");
            Object v = m.invoke(s);
            if (v instanceof Number) return ((Number) v).doubleValue();
        } catch (Exception ignored) {}
        try {
            java.lang.reflect.Field f = s.getClass().getDeclaredField("todaySales");
            f.setAccessible(true);
            Object v = f.get(s);
            if (v instanceof Number) return ((Number) v).doubleValue();
        } catch (Exception ignored) {}
        return 0.0;
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

        t.getColumnModel().getColumn(0).setPreferredWidth(80);
        t.getColumnModel().getColumn(1).setPreferredWidth(150);
        t.getColumnModel().getColumn(2).setPreferredWidth(120);
        t.getColumnModel().getColumn(3).setPreferredWidth(100);
    }
}