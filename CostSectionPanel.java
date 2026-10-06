import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * One block of the Costs frame: a heading, an Add button, and either an empty-state box or the list of
 * cost rows. The same class is used for Direct Costs and Indirect Costs; a CostFactory decides which
 * subclass of CostItem is created when the user adds something.
 */
public class CostSectionPanel extends JPanel {

    /** Creates the right kind of CostItem (pass DirectCost::new or IndirectCost::new). */
    public interface CostFactory {
        CostItem create(String name, double quantity, String unit, double unitCost);
    }

    private final String itemLabel;
    private final String emptyText;
    private final List<CostItem> items;
    private final CostFactory factory;
    private final JPanel content = UiKit.transparent(new BorderLayout());

    public CostSectionPanel(String title, String subtitle, String infoText, String itemLabel, String emptyText,
                            List<CostItem> items, CostFactory factory) {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        this.itemLabel = itemLabel;
        this.emptyText = emptyText;
        this.items = items;
        this.factory = factory;

        add(buildHeader(title, subtitle, infoText), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        rebuild();
    }

    // ------------------------------------------------------------------ pieces

    private JComponent buildHeader(String title, String subtitle, String infoText) {
        JPanel header = UiKit.transparent(new BorderLayout());

        JPanel titleRow = UiKit.labelRow(UiKit.label(title, Font.BOLD, 18, Theme.TEXT),
                new UiKit.InfoButton(title, infoText));

        JPanel text = UiKit.transparent(new BorderLayout(0, 4));
        text.add(titleRow, BorderLayout.NORTH);
        JLabel sub = UiKit.label(subtitle, Font.PLAIN, 12, Theme.TEXT);
        sub.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        text.add(sub, BorderLayout.CENTER);

        header.add(text, BorderLayout.CENTER);
        JPanel east = UiKit.transparent(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        east.add(addButton());
        header.add(east, BorderLayout.EAST);
        return header;
    }

    private UiKit.RoundedButton addButton() {
        UiKit.RoundedButton add = UiKit.primaryButton("Add", Icons.of(Icons.Kind.PLUS, 14, Theme.ON_GREEN));
        add.addActionListener(e -> addNew());
        return add;
    }

    private JComponent emptyState() {
        UiKit.RoundedPanel box = new UiKit.RoundedPanel(new BorderLayout(), 14).dashed(true);
        box.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JPanel col = UiKit.transparent(null);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(Icons.of(Icons.Kind.COINS, 34, Theme.MUTED));
        JLabel name = UiKit.label(itemLabel + "s", Font.PLAIN, 13, Theme.TEXT);
        JLabel desc = new JLabel("<html><div style='width:340px;text-align:center'>" + UiKit.esc(emptyText)
                + "</div></html>");
        desc.setFont(Theme.font(Font.PLAIN, 11));
        desc.setForeground(Theme.FAINT);
        UiKit.RoundedButton add = addButton();

        for (JComponent c : new JComponent[]{icon, name, desc, add}) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        col.add(icon);
        col.add(Box.createVerticalStrut(8));
        col.add(name);
        col.add(Box.createVerticalStrut(6));
        col.add(desc);
        col.add(Box.createVerticalStrut(14));
        col.add(add);
        box.add(col, BorderLayout.CENTER);
        return box;
    }

    private JComponent itemList() {
        UiKit.RoundedPanel card = new UiKit.RoundedPanel(new BorderLayout(), 14);
        card.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        JPanel rows = UiKit.transparent(null);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        boolean first = true;
        for (CostItem item : items) {
            if (!first) {
                rows.add(UiKit.hLine());
            }
            rows.add(new CostRow(item));
            first = false;
        }
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private void rebuild() {
        content.removeAll();
        content.add(items.isEmpty() ? emptyState() : itemList(), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    // ------------------------------------------------------------------ actions

    private void addNew() {
        AddCostDialog dialog = new AddCostDialog(SwingUtilities.getWindowAncestor(this), itemLabel, null);
        AddCostDialog.Result r = dialog.showDialog();
        if (r != null) {
            items.add(factory.create(r.name, r.quantity, r.unitCode, r.unitCost));
            rebuild();
        }
    }

    // ------------------------------------------------------------------ one row

    private final class CostRow extends JPanel {
        private final CostItem item;
        private final UiKit.RoundedButton qtyButton = new UiKit.RoundedButton("", null, Theme.GREEN, Theme.BG,
                Theme.SURFACE, Theme.GREEN, 10, 0, 0);
        private final JLabel lblName = UiKit.label("", Font.BOLD, 13, Theme.TEXT);
        private final JLabel lblUnit = UiKit.label("", Font.PLAIN, 11, Theme.MUTED);
        // at least 120px wide so the totals of different rows line up
        private final JLabel lblTotal = new JLabel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, 120);
                return d;
            }
        };

        CostRow(CostItem item) {
            super(new BorderLayout(14, 0));
            this.item = item;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

            qtyButton.setPreferredSize(new Dimension(52, 44));
            qtyButton.setFont(Theme.font(Font.BOLD, 13));
            qtyButton.addActionListener(e -> showQuantityPopup());
            add(qtyButton, BorderLayout.WEST);

            JPanel text = UiKit.transparent(new BorderLayout(0, 2));
            text.add(lblName, BorderLayout.CENTER);
            text.add(lblUnit, BorderLayout.SOUTH);
            add(text, BorderLayout.CENTER);

            add(buildActions(), BorderLayout.EAST);
            refresh();
        }

        private JComponent buildActions() {
            UiKit.RoundedPanel group = new UiKit.RoundedPanel(new GridBagLayout(), 10);
            group.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            lblTotal.setFont(Theme.font(Font.PLAIN, 13));
            lblTotal.setForeground(Theme.TEXT);
            lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblTotal.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

            UiKit.RoundedButton edit = UiKit.iconButton(Icons.of(Icons.Kind.EDIT, 16, Theme.TEXT), "Edit");
            UiKit.RoundedButton copy = UiKit.iconButton(Icons.of(Icons.Kind.COPY, 16, Theme.TEXT), "Duplicate");
            UiKit.RoundedButton trash = UiKit.iconButton(Icons.of(Icons.Kind.TRASH, 16, Theme.TEXT), "Remove");
            edit.addActionListener(e -> editItem());
            copy.addActionListener(e -> {
                items.add(items.indexOf(item) + 1, item.copy());
                rebuild();
            });
            trash.addActionListener(e -> {
                items.remove(item);
                rebuild();
            });

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.VERTICAL;
            g.weighty = 1;
            Component[] parts = {lblTotal, UiKit.vLine(), edit, UiKit.vLine(), copy, UiKit.vLine(), trash};
            for (int i = 0; i < parts.length; i++) {
                g.gridx = i;
                group.add(parts[i], g);
            }
            return group;
        }

        private void refresh() {
            qtyButton.setText(Fmt.num(item.getQuantity()) + "x");
            lblName.setText(item.getName());
            lblUnit.setText(Fmt.money(item.getUnitCost()) + "/" + item.getUnit());
            lblTotal.setText(Fmt.money(item.getTotal().doubleValue()));
        }

        private void showQuantityPopup() {
            JPopupMenu popup = new JPopupMenu();
            popup.setBackground(Theme.SURFACE);
            popup.setBorder(BorderFactory.createLineBorder(Theme.BORDER_HI));
            JPanel box = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            box.setBackground(Theme.SURFACE);
            UiKit.RoundedButton minus = UiKit.iconButton(Icons.of(Icons.Kind.MINUS, 16, Theme.TEXT), "Less");
            UiKit.RoundedButton plus = UiKit.iconButton(Icons.of(Icons.Kind.PLUS, 16, Theme.TEXT), "More");
            minus.addActionListener(e -> step(-1));
            plus.addActionListener(e -> step(1));
            box.add(minus);
            box.add(plus);
            popup.add(box);
            popup.show(qtyButton, qtyButton.getWidth() + 6,
                    (qtyButton.getHeight() - popup.getPreferredSize().height) / 2);
        }

        /** Adds or removes one from the quantity (never below zero), using BigDecimal so 0.1 + 1 stays exact. */
        private void step(int delta) {
            double next = BigDecimal.valueOf(item.getQuantity()).add(BigDecimal.valueOf(delta)).doubleValue();
            item.setQuantity(next);
            refresh();
        }

        private void editItem() {
            AddCostDialog dialog = new AddCostDialog(SwingUtilities.getWindowAncestor(this), itemLabel, item);
            AddCostDialog.Result r = dialog.showDialog();
            if (r != null) {
                item.setName(r.name);
                item.setQuantity(r.quantity);
                item.setUnit(r.unitCode);
                item.setUnitCost(r.unitCost);
                refresh();
            }
        }
    }
}
