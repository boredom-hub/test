import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The "Add New Direct Cost" / "Edit Indirect Cost" pop-up.
 * The quantity box accepts simple math such as 3/4 or 2*5, like the web app's math input.
 */
public class AddCostDialog extends JDialog {

    /** What the user entered. The caller turns this into a DirectCost or IndirectCost. */
    public static final class Result {
        public final String name;
        public final double quantity;
        public final String unitCode;
        public final double unitCost;

        Result(String name, double quantity, String unitCode, double unitCost) {
            this.name = name;
            this.quantity = quantity;
            this.unitCode = unitCode;
            this.unitCost = unitCost;
        }
    }

    private Result result;

    private final UiKit.RoundedTextField txtName = new UiKit.RoundedTextField(20);
    private final UiKit.RoundedTextField txtQuantity = new UiKit.RoundedTextField(8);
    private final UiKit.RoundedTextField txtUnitCost = new UiKit.RoundedTextField(8);
    private final JComboBox<Object> cboUnit = new JComboBox<Object>();
    private final JLabel lblPreview = UiKit.label(" ", Font.PLAIN, 11, Theme.FAINT);
    private final JLabel lblCostPer = UiKit.label("Cost per kg", Font.BOLD, 13, Theme.TEXT);
    private final JLabel errName = errorLabel();
    private final JLabel errQuantity = errorLabel();
    private final JLabel errUnitCost = errorLabel();

    /**
     * @param itemLabel "Direct Cost" or "Indirect Cost"
     * @param existing  the item being edited, or null when adding a new one
     */
    public AddCostDialog(Window owner, String itemLabel, CostItem existing) {
        super(owner, existing == null ? "Add New " + itemLabel : "Edit " + itemLabel,
                Dialog.ModalityType.APPLICATION_MODAL);
        buildUnitCombo();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        content.add(left(UiKit.label(existing == null ? "Add New " + itemLabel : "Edit " + itemLabel,
                Font.BOLD, 17, Theme.TEXT)));
        content.add(Box.createVerticalStrut(4));
        content.add(left(UiKit.label(existing == null
                ? "Enter the details for the new " + itemLabel.toLowerCase() + "."
                : "Change the details of this " + itemLabel.toLowerCase() + ".", Font.PLAIN, 12, Theme.MUTED)));
        content.add(Box.createVerticalStrut(16));

        content.add(left(UiKit.label(itemLabel, Font.BOLD, 13, Theme.TEXT)));
        content.add(Box.createVerticalStrut(6));
        content.add(left(txtName));
        content.add(left(errName));
        content.add(Box.createVerticalStrut(10));

        content.add(left(buildQuantityRow()));
        content.add(left(errQuantity));
        content.add(Box.createVerticalStrut(10));

        content.add(left(lblCostPer));
        content.add(Box.createVerticalStrut(6));
        JPanel costRow = UiKit.transparent(new BorderLayout(8, 0));
        costRow.add(UiKit.label(Fmt.PESO, Font.BOLD, 15, Theme.MUTED), BorderLayout.WEST);
        costRow.add(txtUnitCost, BorderLayout.CENTER);
        content.add(left(costRow));
        content.add(left(errUnitCost));
        content.add(Box.createVerticalStrut(18));

        UiKit.RoundedButton submit = UiKit.primaryButton(existing == null ? "Add " + itemLabel : "Save Changes", null);
        submit.addActionListener(e -> submit());
        content.add(left(submit));
        stretch(submit);

        UiKit.limitToNumber(txtUnitCost, true);
        wireEvents();
        fill(existing);

        setContentPane(content);
        getRootPane().setDefaultButton(submit);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setResizable(false);
        pack();
        setSize(Math.max(getWidth(), 440), getHeight());
        setLocationRelativeTo(owner);
    }

    /** Shows the dialog (it blocks until closed) and returns what was entered, or null if cancelled. */
    public Result showDialog() {
        setVisible(true);
        return result;
    }

    // ------------------------------------------------------------------ layout

    private JComponent buildQuantityRow() {
        JPanel row = UiKit.transparent(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        g.weightx = 0.5;

        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(0, 0, 6, 8);
        row.add(UiKit.label("Quantity", Font.BOLD, 13, Theme.TEXT), g);
        g.gridx = 1;
        g.insets = new Insets(0, 8, 6, 0);
        row.add(UiKit.label("Unit Type", Font.BOLD, 13, Theme.TEXT), g);

        g.gridx = 0;
        g.gridy = 1;
        g.insets = new Insets(0, 0, 0, 8);
        JPanel qty = UiKit.transparent(new BorderLayout(0, 3));
        qty.add(txtQuantity, BorderLayout.NORTH);
        qty.add(lblPreview, BorderLayout.CENTER);
        row.add(qty, g);
        g.gridx = 1;
        g.insets = new Insets(0, 8, 0, 0);
        row.add(UiKit.comboBox(cboUnit, new UnitRenderer()), g);
        return row;
    }

    private void buildUnitCombo() {
        DefaultComboBoxModel<Object> unitModel = new DefaultComboBoxModel<Object>() {
            @Override
            public void setSelectedItem(Object item) {
                if (item instanceof Units.Unit) {        // category headings can't be chosen
                    super.setSelectedItem(item);
                }
            }
        };
        String category = null;
        for (Units.Unit u : Units.all()) {
            if (!u.category.equals(category)) {
                category = u.category;
                unitModel.addElement(category);
            }
            unitModel.addElement(u);
        }
        cboUnit.setModel(unitModel);
        unitModel.setSelectedItem(Units.first());
    }

    private static JLabel errorLabel() {
        JLabel l = UiKit.label(" ", Font.PLAIN, 11, Theme.RED);
        l.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        return l;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private static void stretch(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }

    // ------------------------------------------------------------------ behaviour

    private void wireEvents() {
        cboUnit.addActionListener(e -> lblCostPer.setText("Cost per " + selectedUnit().code));
        txtQuantity.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { updatePreview(); }
            @Override public void removeUpdate(DocumentEvent e)  { updatePreview(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePreview(); }
        });
    }

    private Units.Unit selectedUnit() {
        Object o = cboUnit.getSelectedItem();
        return o instanceof Units.Unit ? (Units.Unit) o : Units.first();
    }

    /** Shows "= 0.75" under the quantity box when the user typed an expression such as 3/4. */
    private void updatePreview() {
        String text = txtQuantity.getText().trim();
        try {
            double v = MathExpression.evaluate(text);
            boolean plainNumber = text.matches("\\d*\\.?\\d*");
            lblPreview.setText(plainNumber ? " " : "= " + Fmt.num(round(v, 4)));
        } catch (IllegalArgumentException e) {
            lblPreview.setText(" ");
        }
    }

    private static double round(double v, int places) {
        return BigDecimal.valueOf(v).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    private void fill(CostItem existing) {
        if (existing == null) {
            txtQuantity.setText("1");
            txtUnitCost.setText("0");
            cboUnit.setSelectedItem(Units.first());
        } else {
            txtName.setText(existing.getName());
            txtQuantity.setText(Fmt.num(existing.getQuantity()));
            txtUnitCost.setText(Fmt.num(existing.getUnitCost()));
            cboUnit.setSelectedItem(Units.byCode(existing.getUnit()));
        }
        lblCostPer.setText("Cost per " + selectedUnit().code);
    }

    private void submit() {
        errName.setText(" ");
        errQuantity.setText(" ");
        errUnitCost.setText(" ");
        boolean ok = true;

        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            errName.setText("Item name is required");
            ok = false;
        }

        double quantity = 0;
        try {
            quantity = MathExpression.evaluate(txtQuantity.getText());
            if (quantity < 0) {
                errQuantity.setText("Quantity must be a non-negative number");
                ok = false;
            }
        } catch (IllegalArgumentException e) {
            errQuantity.setText("Enter a number, or simple math like 3/4");
            ok = false;
        }

        double unitCost = 0;
        try {
            unitCost = Double.parseDouble(txtUnitCost.getText().trim());
        } catch (NumberFormatException e) {
            errUnitCost.setText("Unit cost must be a non-negative number");
            ok = false;
        }

        if (ok) {
            result = new Result(name, round(quantity, 6), selectedUnit().code, unitCost);
            dispose();
        }
    }

    // ------------------------------------------------------------------ unit drop-down look

    /** Shows category names as small grey headings above their units. */
    private static final class UnitRenderer extends UiKit.DarkListRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean selected, boolean focus) {
            boolean heading = value instanceof String;
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected && !heading, focus);
            if (heading) {
                l.setText(((String) value).toUpperCase());
                l.setFont(Theme.font(Font.BOLD, 11));
                l.setForeground(Theme.FAINT);
                l.setBorder(BorderFactory.createEmptyBorder(8, 12, 3, 12));
            } else if (index >= 0) {
                l.setBorder(BorderFactory.createEmptyBorder(6, 22, 6, 12));
            }
            return l;
        }
    }
}
