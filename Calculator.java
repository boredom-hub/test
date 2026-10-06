import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Step 1 of 4: product name, pricing strategy, production quantity and sales quantity.
 *
 * This is the frame that Login opens, so it has a no-argument constructor that starts a fresh product.
 */
public class Calculator extends StepFrame {

    private static final String COST_PLUS = "Cost Plus";
    private static final String FIXED_PRICE = "Fixed Price";

    private final UiKit.RoundedTextField txtName = new UiKit.RoundedTextField(20);
    private final JComboBox<String> cboStrategy = new JComboBox<String>(new String[]{COST_PLUS, FIXED_PRICE});
    private final JSlider sldMarkup = UiKit.slider(0, 1000, 0);   // tenths of a percent: 0.0% to 100.0%
    private final UiKit.RoundedTextField txtMarkup = new UiKit.RoundedTextField(5);
    private final UiKit.RoundedTextField txtFixedPrice = new UiKit.RoundedTextField(8);
    private final UiKit.RoundedTextField txtProduction = new UiKit.RoundedTextField(10);
    private final UiKit.RoundedTextField txtSales = new UiKit.RoundedTextField(10);

    private final JLabel lblTarget = UiKit.label(COST_PLUS, Font.BOLD, 13, Theme.TEXT);
    private final UiKit.InfoButton infoTarget = new UiKit.InfoButton("", "");
    private final CardLayout targetLayout = new CardLayout();
    private final JPanel targetCards = UiKit.transparent(targetLayout);

    private boolean syncing;

    /** Starts a brand-new product (this is what Login calls). */
    public Calculator() {
        this(new ProductPricingModel());
    }

    /** Re-opens the frame for an existing product, e.g. when pressing Back from the Costs frame. */
    public Calculator(ProductPricingModel model) {
        super(model, 1, 4, "Product Details", 760, 600);
        setBackLabel("Log out");
        setNextLabel("Next: Costs");
        setBody(buildBody());
        wireEvents();
        loadFromModel();
        showFrame();
    }

    // ------------------------------------------------------------------ layout

    private JComponent buildBody() {
        UiKit.limitToNumber(txtMarkup, true);
        UiKit.limitToNumber(txtFixedPrice, true);
        UiKit.limitToNumber(txtProduction, false);
        UiKit.limitToNumber(txtSales, false);
        for (UiKit.RoundedTextField f : new UiKit.RoundedTextField[]{txtMarkup, txtFixedPrice, txtProduction, txtSales}) {
            selectAllOnFocus(f);
        }

        UiKit.RoundedPanel card = new UiKit.RoundedPanel(new GridBagLayout(), 16);
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Product name
        place(card, labelRow("Product Name", new UiKit.InfoButton("Product Name",
                "The name of the product you are pricing.")), 0, 0, 2, 6);
        place(card, txtName, 0, 1, 2, 18);

        // Pricing strategy: left = the markup / price input, right = the drop-down
        targetCards.add(buildCostPlusPanel(), COST_PLUS);
        targetCards.add(buildFixedPricePanel(), FIXED_PRICE);

        place(card, labelRow(lblTarget, infoTarget), 0, 2, 1, 6);
        place(card, labelRow(UiKit.label("Pricing Strategy", Font.BOLD, 13, Theme.TEXT),
                new UiKit.InfoButton("Pricing Strategy",
                        "Choose a pricing strategy for your product. \"Cost Plus\" adds a markup to the cost of "
                                + "production, while \"Fixed Price\" sets a predetermined price.")), 1, 2, 1, 6);
        place(card, targetCards, 0, 3, 1, 18);
        place(card, UiKit.comboBox(cboStrategy, null), 1, 3, 1, 18, GridBagConstraints.NORTH);

        // Quantities
        place(card, labelRow("Production Quantity", new UiKit.InfoButton("Production Quantity",
                "The number of units you plan to produce.")), 0, 4, 1, 6);
        place(card, labelRow("Sales Quantity", new UiKit.InfoButton("Sales Quantity",
                "The number of units you plan to sell.")), 1, 4, 1, 6);

        place(card, UiKit.segment(txtProduction,
                UiKit.endCap(Icons.of(Icons.Kind.BOX, 18, Theme.MUTED))), 0, 5, 1, 0);
        place(card, UiKit.segment(txtSales,
                percentButton(50), percentButton(75), percentButton(100)), 1, 5, 1, 0);

        JPanel page = new UiKit.ScrollPage(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        page.add(card, BorderLayout.NORTH);
        return UiKit.scroll(page);
    }

    private JComponent buildCostPlusPanel() {
        JPanel p = UiKit.transparent(new BorderLayout(0, 8));
        p.add(sldMarkup, BorderLayout.NORTH);
        JPanel row = UiKit.transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
        txtMarkup.setColumns(5);
        row.add(txtMarkup);
        JLabel percent = UiKit.label("%", Font.PLAIN, 13, Theme.MUTED);
        percent.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        row.add(percent);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildFixedPricePanel() {
        JPanel row = UiKit.transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel peso = UiKit.label(Fmt.PESO, Font.BOLD, 15, Theme.MUTED);
        peso.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        row.add(peso);
        row.add(txtFixedPrice);
        JPanel p = UiKit.transparent(new BorderLayout());
        p.add(row, BorderLayout.NORTH);
        return p;
    }

    private UiKit.RoundedButton percentButton(final int percent) {
        UiKit.RoundedButton b = UiKit.segmentButton(percent + "%");
        b.addActionListener(e -> applySalesPercent(percent));
        return b;
    }

    private static JComponent labelRow(String text, UiKit.InfoButton info) {
        return labelRow(UiKit.label(text, Font.BOLD, 13, Theme.TEXT), info);
    }

    private static JComponent labelRow(JLabel label, UiKit.InfoButton info) {
        return UiKit.labelRow(label, info);
    }

    private static void place(JPanel parent, Component c, int x, int y, int width, int bottomGap) {
        place(parent, c, x, y, width, bottomGap, GridBagConstraints.CENTER);
    }

    private static void place(JPanel parent, Component c, int x, int y, int width, int bottomGap, int anchor) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = width;
        g.weightx = 0.5;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = anchor;
        int left = (x == 1 && width == 1) ? 12 : 0;
        int right = (x == 0 && width == 1) ? 12 : 0;
        g.insets = new Insets(0, left, bottomGap, right);
        parent.add(c, g);
    }

    // ------------------------------------------------------------------ behaviour

    private void wireEvents() {
        cboStrategy.addActionListener(e -> showStrategyPanel());

        sldMarkup.addChangeListener(e -> {
            if (!syncing) {
                syncing = true;
                txtMarkup.setText(Fmt.fixed(sldMarkup.getValue() / 10.0, 1));
                syncing = false;
            }
        });

        txtMarkup.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { markupTyped(); }
            @Override public void removeUpdate(DocumentEvent e)  { markupTyped(); }
            @Override public void changedUpdate(DocumentEvent e) { markupTyped(); }
        });
    }

    private void markupTyped() {
        if (syncing) {
            return;
        }
        try {
            double percent = Double.parseDouble(txtMarkup.getText());
            syncing = true;
            sldMarkup.setValue((int) Math.min(sldMarkup.getMaximum(), Math.round(percent * 10)));
            syncing = false;
        } catch (NumberFormatException ignored) {
            // empty or just "." while typing: leave the slider where it is
        }
    }

    private void showStrategyPanel() {
        boolean costPlus = COST_PLUS.equals(cboStrategy.getSelectedItem());
        String name = costPlus ? COST_PLUS : FIXED_PRICE;
        lblTarget.setText(name);
        infoTarget.setContent(costPlus ? "Cost Plus Strategy" : "Fixed Price Strategy",
                costPlus ? "This slider allows you to set the percentage markup on the cost of production."
                         : "Enter the fixed price for your product.");
        targetLayout.show(targetCards, name);
    }

    private void applySalesPercent(int percent) {
        Integer production = parseWholeNumber(txtProduction.getText());
        if (production == null) {
            JOptionPane.showMessageDialog(this, "Enter the production quantity first.",
                    "Sales quantity", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        txtSales.setText(String.valueOf(Math.round(production * (percent / 100.0))));
    }

    private static void selectAllOnFocus(final UiKit.RoundedTextField f) {
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.selectAll(); }
        });
    }

    /** Returns the whole number in the text, or null if it isn't one (empty, too big...). */
    private static Integer parseWholeNumber(String text) {
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDecimal(String text) {
        try {
            double v = Double.parseDouble(text.trim());
            return (Double.isNaN(v) || Double.isInfinite(v)) ? null : Double.valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void loadFromModel() {
        txtName.setText(model.getProductName());
        txtProduction.setText(String.valueOf(model.getPlannedProductionQuantity()));
        txtSales.setText(String.valueOf(model.getForecastedSalesQuantity()));

        syncing = true;
        PricingStrategy s = model.getStrategy();
        if (s instanceof FixedPriceStrategy) {
            cboStrategy.setSelectedItem(FIXED_PRICE);
            txtFixedPrice.setText(Fmt.num(s.getTargetValue()));
            txtMarkup.setText("0.0");
            sldMarkup.setValue(0);
        } else {
            cboStrategy.setSelectedItem(COST_PLUS);
            txtMarkup.setText(Fmt.fixed(s.getTargetValue(), 1));
            sldMarkup.setValue((int) Math.min(sldMarkup.getMaximum(), Math.round(s.getTargetValue() * 10)));
            txtFixedPrice.setText("0");
        }
        syncing = false;
        showStrategyPanel();
    }

    // ------------------------------------------------------------------ navigation

    @Override
    protected void onBack() {
        dispose();
        new MainFrame();
    }

    @Override
    protected void onNext() {
        List<String> problems = new ArrayList<String>();

        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            problems.add("Product name is required.");
        }

        Integer production = parseWholeNumber(txtProduction.getText());
        if (production == null || production <= 0) {
            problems.add("Production quantity must be a whole number greater than 0.");
        }

        Integer sales = parseWholeNumber(txtSales.getText());
        if (sales == null) {
            problems.add("Sales quantity must be a whole number (0 or more).");
        } else if (production != null && production > 0 && sales > production) {
            problems.add("Sales quantity cannot exceed production quantity.");
        }

        boolean costPlus = COST_PLUS.equals(cboStrategy.getSelectedItem());
        Double target = parseDecimal(costPlus ? txtMarkup.getText() : txtFixedPrice.getText());
        if (target == null || target < 0) {
            problems.add(costPlus ? "Cost plus markup must be a number of 0 or more."
                                  : "Fixed price must be a number of 0 or more.");
        }

        if (!problems.isEmpty()) {
            showProblems(problems);
            return;
        }

        model.setProductName(name);
        model.setPlannedProductionQuantity(production);
        model.setForecastedSalesQuantity(sales);
        model.setStrategy(costPlus ? new CostPlusStrategy(target) : new FixedPriceStrategy(target));

        dispose();
        new CostsFrame(model);
    }
}
