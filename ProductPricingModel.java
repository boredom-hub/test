import java.util.ArrayList;
import java.util.List;

/**
 * Everything the user has entered about one product. A single instance is created when the calculator opens
 * and is handed from frame to frame, so each frame reads from and writes to the same data.
 */
public class ProductPricingModel {

    private String productName = "";
    private PricingStrategy strategy = new CostPlusStrategy(0);
    private int plannedProductionQuantity;
    private int forecastedSalesQuantity;
    private final List<CostItem> directCosts = new ArrayList<CostItem>();
    private final List<CostItem> indirectCosts = new ArrayList<CostItem>();

    public String getProductName()              { return productName; }
    public PricingStrategy getStrategy()        { return strategy; }
    public int getPlannedProductionQuantity()   { return plannedProductionQuantity; }
    public int getForecastedSalesQuantity()     { return forecastedSalesQuantity; }
    public List<CostItem> getDirectCosts()      { return directCosts; }
    public List<CostItem> getIndirectCosts()    { return indirectCosts; }

    public void setProductName(String productName)          { this.productName = productName; }
    public void setStrategy(PricingStrategy strategy)       { this.strategy = strategy; }
    public void setPlannedProductionQuantity(int quantity)  { this.plannedProductionQuantity = quantity; }
    public void setForecastedSalesQuantity(int quantity)    { this.forecastedSalesQuantity = quantity; }

    public boolean hasAnyCosts() {
        return !directCosts.isEmpty() || !indirectCosts.isEmpty();
    }

    /** Problems that would make the analysis meaningless. Empty list means the data is fine. */
    public List<String> validate() {
        List<String> problems = new ArrayList<String>();
        if (productName == null || productName.trim().isEmpty()) {
            problems.add("Product name is required.");
        }
        if (plannedProductionQuantity <= 0) {
            problems.add("Production quantity must be greater than 0.");
        }
        if (forecastedSalesQuantity < 0) {
            problems.add("Sales quantity cannot be negative.");
        }
        if (forecastedSalesQuantity > plannedProductionQuantity) {
            problems.add("Sales quantity cannot exceed production quantity.");
        }
        return problems;
    }
}
