import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;

/**
 * Step 3 of 4: the computed results. Shows the recommended price, then the five key figures
 * (break-even point, expected sales, profit, contribution margin ratio, operating leverage).
 * Click an (i) icon to see what a figure means, its formula, and the calculation with your own numbers.
 */
public class MetricsFrame extends StepFrame {

    private final AnalysisResult result;

    public MetricsFrame(ProductPricingModel model) {
        super(model, 3, 4, "Results", 720, 760);
        this.result = FinancialAnalyzer.analyze(model);
        setBackLabel("Back");
        setNextLabel("Next: Chart");
        setBody(buildBody());
        showFrame();
    }

    // ------------------------------------------------------------------ layout

    private JComponent buildBody() {
        JPanel page = new UiKit.ScrollPage(null);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JComponent summary = buildSummaryCard();
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(summary);

        if (!result.isValid()) {
            page.add(UiKit.vgap(12));
            JLabel warn = new JLabel("<html><body style='width:600px'>" + UiKit.esc(result.getMessage())
                    + "</body></html>");
            warn.setFont(Theme.font(Font.PLAIN, 12));
            warn.setForeground(Theme.AMBER);
            warn.setAlignmentX(Component.LEFT_ALIGNMENT);
            page.add(warn);
        }

        if (result.isValid() && result.getContributionMargin() < 0) {
            page.add(UiKit.vgap(12));
            JLabel warn = new JLabel("<html><body style='width:600px'>The price is below the variable cost per unit, "
                    + "so every unit sold loses money and the business can never break even. "
                    + "Try a higher markup or price.</body></html>");
            warn.setFont(Theme.font(Font.PLAIN, 12));
            warn.setForeground(Theme.AMBER);
            warn.setAlignmentX(Component.LEFT_ALIGNMENT);
            page.add(warn);
        }

        page.add(UiKit.vgap(16));
        JComponent kpis = buildKpiCard();
        kpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(kpis);

        return UiKit.scroll(page);
    }

    /** Product name, unit cost and the recommended price. */
    private JComponent buildSummaryCard() {
        UiKit.RoundedPanel card = new UiKit.RoundedPanel(new BorderLayout(0, 14), 14);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel head = UiKit.transparent(new GridLayout(2, 1, 0, 3));
        head.add(UiKit.label(model.getProductName(), Font.BOLD, 17, Theme.TEXT));
        head.add(UiKit.label(model.getStrategy().getName() + " strategy  -  "
                + model.getPlannedProductionQuantity() + " units produced", Font.PLAIN, 12, Theme.MUTED));
        card.add(head, BorderLayout.NORTH);

        JPanel figures = UiKit.transparent(new GridLayout(1, 2, 16, 0));
        figures.add(figure("Unit Cost", Fmt.money(result.getUnitCost()), Theme.TEXT,
                "The total expense to produce one unit of the product: all direct and indirect costs "
                        + "divided by the number of units produced.",
                "(Total Direct Costs + Total Indirect Costs) / Production Quantity", unitCostCalc()));
        figures.add(figure("Recommended Price", Fmt.money(result.getRecommendedPrice()), Theme.GREEN,
                "The selling price per unit that follows from your pricing strategy.",
                model.getStrategy().formula(), priceCalc()));
        card.add(figures, BorderLayout.CENTER);
        return card;
    }

    private JComponent figure(String title, String value, java.awt.Color color, String description,
                              String formula, String calculation) {
        JPanel box = UiKit.transparent(new BorderLayout(0, 4));
        JPanel top = UiKit.labelRow(UiKit.label(title, Font.PLAIN, 12, Theme.MUTED),
                new UiKit.InfoButton(title, popoverText(description, formula, calculation)));
        box.add(top, BorderLayout.NORTH);
        box.add(UiKit.label(value, Font.BOLD, 26, color), BorderLayout.CENTER);
        return box;
    }

    /** The five-row card from the reference design. */
    private JComponent buildKpiCard() {
        UiKit.RoundedPanel card = new UiKit.RoundedPanel(new BorderLayout(), 14);
        card.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        JPanel rows = UiKit.transparent(null);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        int expected = model.getForecastedSalesQuantity();

        rows.add(new KpiRow("Break-even Point", Fmt.fixed(result.getBreakEvenPoint(), 2) + " units",
                Icons.of(Icons.Kind.MINUS, 20, Theme.MUTED),
                popoverText("The number of units you need to sell to cover all costs. At this point, you're not "
                                + "making a profit, but you're not losing money either. For example, if your "
                                + "break-even point is 1000 units, selling 999 units results in a loss, while "
                                + "selling 1001 units generates a profit.",
                        "Fixed Costs / (Price per Unit - Variable Cost per Unit)", breakEvenCalc()), true));

        rows.add(new KpiRow("Expected Sales", expected + " units",
                Icons.of(Icons.Kind.TREND_UP, 20, Theme.MUTED),
                popoverText("The number of units you think you can sell based on your market research and "
                                + "business goals. This helps you plan your production and marketing efforts. For "
                                + "instance, if you expect to sell 1500 units, you might plan to produce slightly "
                                + "more to account for potential increased demand.",
                        "The sales quantity you entered in the Product Details step", expectedCalc()), true));

        rows.add(new KpiRow("Profit at Expected Sales", Fmt.money(result.getProfitAtExpectedSales()),
                Icons.of(Icons.Kind.TREND_UP, 20, Theme.MUTED),
                popoverText("The amount of money you expect to earn after all expenses if you achieve your "
                                + "expected sales. This shows if your business will be profitable at your sales "
                                + "target. For example, if this value is " + Fmt.PESO + "50,000, it means you'll "
                                + "earn " + Fmt.PESO + "50,000 in profit if you sell your expected number of units.",
                        "(Price x Expected Sales) - (Fixed Costs + Variable Cost per Unit x Expected Sales)",
                        profitCalc()), true));

        rows.add(new KpiRow("Contribution Margin Ratio", Fmt.fixed(result.getContributionMarginRatio() * 100, 2) + "%",
                Icons.of(Icons.Kind.PERCENT, 20, Theme.MUTED),
                popoverText("The percentage of each peso from sales that's left after paying variable costs. "
                                + "This money helps cover fixed costs and generate profit. A higher percentage is "
                                + "better for your business. For instance, if the ratio is 60%, it means for every "
                                + Fmt.PESO + "100 in sales, " + Fmt.PESO + "60 is available to cover fixed costs "
                                + "and contribute to profit.",
                        "(Contribution Margin per Unit / Price) x 100", cmRatioCalc()), true));

        rows.add(new KpiRow("Operating Leverage", Fmt.fixed(result.getOperatingLeverage(), 2),
                Icons.of(Icons.Kind.TREND_UP, 20, Theme.MUTED),
                popoverText("This shows how changes in sales affect your profit. A higher number means your "
                                + "profit is more sensitive to sales changes. For example, if your operating "
                                + "leverage is 3, a 10% increase in sales could lead to a 30% increase in profit. "
                                + "Conversely, a 10% decrease in sales could result in a 30% decrease in profit.",
                        "Total Contribution Margin / (Total Contribution Margin - Fixed Costs)",
                        leverageCalc()), false));

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    /** One row of the card: title with (i) icon, big value, and an icon on the right. */
    private static final class KpiRow extends JPanel {
        KpiRow(String title, String value, Icon icon, String info, boolean divider) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    divider ? BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER)
                            : BorderFactory.createEmptyBorder(),
                    BorderFactory.createEmptyBorder(14, 20, 14, 20)));

            JPanel text = UiKit.transparent(new BorderLayout(0, 4));
            text.add(UiKit.labelRow(UiKit.label(title, Font.PLAIN, 12, Theme.TEXT),
                    new UiKit.InfoButton(title, info)), BorderLayout.NORTH);
            text.add(UiKit.label(value, Font.BOLD, 22, Theme.TEXT), BorderLayout.CENTER);

            add(text, BorderLayout.CENTER);
            add(new JLabel(icon), BorderLayout.EAST);
        }
    }

    private static String popoverText(String description, String formula, String calculation) {
        return description + "\n# Formula\n" + formula + "\n# Calculation\n" + calculation;
    }

    // ------------------------------------------------------------------ worked calculations

    private String unitCostCalc() {
        if (!result.isValid()) {
            return "Not available yet.";
        }
        return "(" + Fmt.money(result.getTotalDirectCost()) + " + " + Fmt.money(result.getTotalIndirectCost())
                + ") / " + model.getPlannedProductionQuantity() + " = " + Fmt.money(result.getUnitCost());
    }

    private String priceCalc() {
        if (!result.isValid()) {
            return "Not available yet.";
        }
        return model.getStrategy().explain(BigDecimal.valueOf(result.getUnitCost()),
                BigDecimal.valueOf(result.getRecommendedPrice()));
    }

    private String breakEvenCalc() {
        if (!result.isValid()) {
            return "Not available yet.";
        }
        double margin = result.getRecommendedPrice() - result.getUnitVariableCost();
        double raw = result.getTotalIndirectCost() / margin;
        return Fmt.money(result.getTotalIndirectCost()) + " / (" + Fmt.money(result.getRecommendedPrice()) + " - "
                + Fmt.money(result.getUnitVariableCost()) + ") = " + Fmt.fixed(raw, 2)
                + ", rounded up to " + Fmt.fixed(result.getBreakEvenPoint(), 0) + " units";
    }

    private String expectedCalc() {
        int planned = model.getPlannedProductionQuantity();
        int expected = model.getForecastedSalesQuantity();
        double pct = planned > 0 ? expected * 100.0 / planned : 0;
        return expected + " units (" + Fmt.fixed(pct, 1) + "% of the " + planned + " units produced)";
    }

    private String profitCalc() {
        if (!result.isValid()) {
            return "Not available yet.";
        }
        int n = model.getForecastedSalesQuantity();
        return "(" + Fmt.money(result.getRecommendedPrice()) + " x " + n + ") - ("
                + Fmt.money(result.getTotalIndirectCost()) + " + " + Fmt.money(result.getUnitVariableCost())
                + " x " + n + ") = " + Fmt.money(result.getProfitAtExpectedSales());
    }

    private String cmRatioCalc() {
        if (!result.isValid()) {
            return "Not available yet.";
        }
        return "(" + Fmt.money(result.getContributionMargin()) + " / " + Fmt.money(result.getRecommendedPrice())
                + ") x 100 = " + Fmt.fixed(result.getContributionMarginRatio() * 100, 2) + "%";
    }

    private String leverageCalc() {
        if (!result.isValid()) {
            return "Not available yet.";
        }
        int n = model.getForecastedSalesQuantity();
        double totalCm = n * result.getContributionMargin();
        String cm = "(" + n + " x " + Fmt.money(result.getContributionMargin()) + ")";
        return cm + " / (" + cm + " - " + Fmt.money(result.getTotalIndirectCost()) + ") = "
                + Fmt.fixed(result.getOperatingLeverage(), 2)
                + (Math.abs(totalCm - result.getTotalIndirectCost()) < 0.005
                        ? "  (shown as 0 when sales exactly cover fixed costs)" : "");
    }

    // ------------------------------------------------------------------ navigation

    @Override
    protected void onBack() {
        dispose();
        new CostsFrame(model);
    }

    @Override
    protected void onNext() {
        dispose();
        new ChartFrame(model);
    }
}
