import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;

/** Step 4 of 4: the profit analysis chart. */
public class ChartFrame extends StepFrame {

    public ChartFrame(ProductPricingModel model) {
        super(model, 4, 4, "Chart", 1040, 740);
        setBackLabel("Back");
        setNextLabel("New Product");

        AnalysisResult result = FinancialAnalyzer.analyze(model);

        UiKit.RoundedPanel card = new UiKit.RoundedPanel(new BorderLayout(0, 10), 14);
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JPanel head = UiKit.transparent(new BorderLayout(0, 6));
        head.add(UiKit.label("Profit Analysis Chart", Font.BOLD, 20, Theme.TEXT), BorderLayout.NORTH);
        head.add(UiKit.label("This chart shows the sales revenue, total cost, and profit for each quantity. "
                + "The shaded area represents the Margin of Safety.", Font.PLAIN, 12, Theme.MUTED),
                BorderLayout.CENTER);
        if (!result.isValid()) {
            JLabel warn = UiKit.label("Nothing to plot yet: " + result.getMessage(), Font.PLAIN, 12, Theme.AMBER);
            head.add(warn, BorderLayout.SOUTH);
        }
        card.add(head, BorderLayout.NORTH);
        card.add(new ProfitChartPanel(model, result), BorderLayout.CENTER);

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.BG);
        page.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        page.add(card, BorderLayout.CENTER);
        setBody(page);
        showFrame();
    }

    @Override
    protected void onBack() {
        dispose();
        new MetricsFrame(model);
    }

    /** "New Product" starts over with an empty form. */
    @Override
    protected void onNext() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Start a new product? The current product's details will be cleared.",
                "New product", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new Calculator();
        }
    }
}
