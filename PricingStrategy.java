import java.math.BigDecimal;

/**
 * A way of turning the cost of one unit into a selling price.
 * Implementations: CostPlusStrategy and FixedPriceStrategy.
 */
public interface PricingStrategy {

    /** Name shown in the Pricing Strategy drop-down. */
    String getName();

    /** The number the user typed: a markup percentage (cost plus) or a peso price (fixed price). */
    double getTargetValue();

    /** Recommended selling price per unit, rounded to 2 decimal places. */
    BigDecimal recommendedPrice(BigDecimal unitCost);

    /** The formula as text, shown in the detailed calculations. */
    String formula();

    /** The formula with the real numbers filled in, shown in the detailed calculations. */
    String explain(BigDecimal unitCost, BigDecimal price);
}
