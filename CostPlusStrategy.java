import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Price = unit cost plus a percentage markup. */
public class CostPlusStrategy implements PricingStrategy {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private final double markupPercent;

    public CostPlusStrategy(double markupPercent) {
        this.markupPercent = markupPercent;
    }

    @Override public String getName()        { return "Cost Plus"; }
    @Override public double getTargetValue() { return markupPercent; }

    @Override
    public BigDecimal recommendedPrice(BigDecimal unitCost) {
        BigDecimal factor = BigDecimal.ONE.add(
                BigDecimal.valueOf(markupPercent).divide(BigDecimal.valueOf(100), MC), MC);
        return unitCost.multiply(factor, MC).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String formula() {
        return "Unit Cost x (1 + Markup % / 100)";
    }

    @Override
    public String explain(BigDecimal unitCost, BigDecimal price) {
        return Fmt.money(unitCost.doubleValue()) + " x (1 + " + Fmt.num(markupPercent) + " / 100) = "
                + Fmt.money(price.doubleValue());
    }
}
