import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * All of the pricing math. This is a Java translation of computation.ts from appweb.
 *
 * It uses BigDecimal with 20 significant digits and HALF_UP rounding, the same settings as decimal.js,
 * so the numbers match the web app.
 */
public final class FinancialAnalyzer {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    private FinancialAnalyzer() { }

    public static AnalysisResult analyze(ProductPricingModel model) {
        BigDecimal planned = BigDecimal.valueOf(model.getPlannedProductionQuantity());
        if (planned.signum() <= 0) {
            return AnalysisResult.empty("Production quantity must be greater than 0.");
        }
        BigDecimal forecast = BigDecimal.valueOf(model.getForecastedSalesQuantity());

        BigDecimal totalDirect = total(model.getDirectCosts());
        BigDecimal totalIndirect = total(model.getIndirectCosts());

        // Unit cost = (direct / planned) + (indirect / planned), each rounded to 2 dp
        BigDecimal unitDirect = div(totalDirect, planned).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unitIndirect = div(totalIndirect, planned).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unitCost = unitDirect.add(unitIndirect, MC).setScale(2, RoundingMode.HALF_UP);

        // Polymorphism: the strategy object decides how the price is worked out.
        BigDecimal price = model.getStrategy().recommendedPrice(unitCost);

        // Direct costs are the variable costs, indirect costs are the fixed costs.
        BigDecimal unitVariable = div(totalDirect, planned);

        BigDecimal marginPerUnit = price.subtract(unitVariable, MC);
        if (marginPerUnit.signum() == 0) {
            return AnalysisResult.empty("The price equals the variable cost per unit, so the contribution "
                    + "margin is zero and the break-even point cannot be calculated. Try a higher markup or price.");
        }
        if (price.signum() == 0) {
            return AnalysisResult.empty("The recommended price is zero. Enter a markup or a fixed price.");
        }

        BigDecimal breakEven = div(totalIndirect, marginPerUnit).setScale(0, RoundingMode.CEILING);
        BigDecimal contributionMargin = marginPerUnit.setScale(2, RoundingMode.HALF_UP);
        BigDecimal cmRatio = div(contributionMargin, price).setScale(4, RoundingMode.HALF_UP);

        // Operating leverage = total contribution margin / (total contribution margin - fixed costs)
        BigDecimal totalCm = forecast.multiply(contributionMargin, MC);
        BigDecimal leverage;
        if (totalCm.compareTo(totalIndirect) == 0) {
            leverage = BigDecimal.ZERO;
        } else {
            leverage = div(totalCm, totalCm.subtract(totalIndirect, MC)).abs().setScale(2, RoundingMode.HALF_UP);
        }

        // Profit at the expected sales volume, worked out directly.
        BigDecimal revenue = price.multiply(forecast, MC);
        BigDecimal cost = totalIndirect.add(unitVariable.multiply(forecast, MC), MC);
        BigDecimal profit = revenue.subtract(cost, MC);

        return new AnalysisResult(
                totalDirect.doubleValue(), totalIndirect.doubleValue(), unitCost.doubleValue(),
                unitVariable.doubleValue(), price.doubleValue(), breakEven.doubleValue(),
                contributionMargin.doubleValue(), cmRatio.doubleValue(), leverage.doubleValue(),
                profit.doubleValue());
    }

    /** Sum of quantity x unit cost over a list of costs. */
    public static BigDecimal total(List<CostItem> costs) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CostItem c : costs) {
            sum = sum.add(c.getTotal(), MC);
        }
        return sum;
    }

    private static BigDecimal div(BigDecimal a, BigDecimal b) {
        return a.divide(b, MC);
    }
}
