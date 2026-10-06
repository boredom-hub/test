import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The selling price is simply the price the user typed in, regardless of cost.
 *
 * NOTE: the original web app (appweb) computed "fixed price" as unit cost + the typed value, even though
 * its own help text said "enter the fixed price for your product". Here the typed value IS the price.
 * To get the web app's behaviour back, change the return line of recommendedPrice() to
 * unitCost.add(BigDecimal.valueOf(price)).setScale(2, RoundingMode.HALF_UP).
 */
public class FixedPriceStrategy implements PricingStrategy {

    private final double price;

    public FixedPriceStrategy(double price) {
        this.price = price;
    }

    @Override public String getName()        { return "Fixed Price"; }
    @Override public double getTargetValue() { return price; }

    @Override
    public BigDecimal recommendedPrice(BigDecimal unitCost) {
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String formula() {
        return "Selling Price = the fixed price you entered";
    }

    @Override
    public String explain(BigDecimal unitCost, BigDecimal price) {
        return "Fixed price = " + Fmt.money(price.doubleValue());
    }
}
