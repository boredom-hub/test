import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Component;
import javax.swing.BorderFactory;

/** Step 2 of 4: the direct costs and indirect costs of the product. */
public class CostsFrame extends StepFrame {

    private static final String DIRECT_INFO =
            "Direct costs are expenses that can be directly attributed to the production of specific goods or "
            + "services. These costs typically vary with production volume. Examples include:\n"
            + "- Raw materials (e.g., wood for furniture, fabric for clothing)\n"
            + "- Manufacturing supplies (e.g., nails, glue, packaging materials)\n"
            + "- Commissions tied to specific sales";

    private static final String INDIRECT_INFO =
            "Indirect costs are overhead expenses that support the overall business but are not directly tied to "
            + "producing specific products or services. These costs often remain relatively constant regardless "
            + "of production volume. Examples include:\n"
            + "- Rent or mortgage for facilities\n"
            + "- Utilities (electricity, water, internet)\n"
            + "- Administrative staff salaries\n"
            + "- Marketing and advertising expenses\n"
            + "- Insurance premiums\n"
            + "- Equipment depreciation";

    public CostsFrame(ProductPricingModel model) {
        super(model, 2, 4, "Costs", 780, 700);
        setBackLabel("Back");
        setNextLabel("Next: Results");

        JPanel page = new UiKit.ScrollPage(null);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        CostSectionPanel direct = new CostSectionPanel("Direct Costs",
                "Costs that are directly related to the production of the product.", DIRECT_INFO,
                "Direct Cost", "Costs directly associated with product production (e.g., materials, ingredients)",
                model.getDirectCosts(), DirectCost::new);
        CostSectionPanel indirect = new CostSectionPanel("Indirect Costs",
                "Costs that are not directly tied to the production of the product.", INDIRECT_INFO,
                "Indirect Cost", "Overhead costs not directly tied to production (e.g., rent, utilities)",
                model.getIndirectCosts(), IndirectCost::new);
        direct.setAlignmentX(Component.LEFT_ALIGNMENT);
        indirect.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(direct);
        page.add(UiKit.vgap(26));
        page.add(indirect);

        setBody(UiKit.scroll(page));
        showFrame();
    }

    @Override
    protected void onBack() {
        dispose();
        new Calculator(model);
    }

    @Override
    protected void onNext() {
        if (!model.hasAnyCosts()) {
            JOptionPane.showMessageDialog(this, "Add at least one direct or indirect cost to continue.",
                    "No costs yet", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        dispose();
        new MetricsFrame(model);
    }
}
