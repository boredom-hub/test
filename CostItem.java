import java.math.BigDecimal;

/**
 * Base class for every cost the user adds (a line item such as "Flour, 2 kg at 45.00 per kg").
 *
 * DirectCost and IndirectCost extend this class. They hold the same data and differ only in
 * how they are categorised, which is what the analyzer uses to separate variable costs from fixed costs.
 */
public abstract class CostItem {

    private String name;
    private double quantity;
    private String unit;      // unit code, e.g. "kg" (see Units)
    private double unitCost;  // price of ONE unit

    protected CostItem(String name, double quantity, String unit, double unitCost) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.unitCost = unitCost;
    }

    public String getName()      { return name; }
    public double getQuantity()  { return quantity; }
    public String getUnit()      { return unit; }
    public double getUnitCost()  { return unitCost; }

    public void setName(String name)         { this.name = name; }
    public void setQuantity(double quantity) { this.quantity = Math.max(0, quantity); }
    public void setUnit(String unit)         { this.unit = unit; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }

    /** quantity x unitCost, kept as BigDecimal so money math has no floating-point drift. */
    public BigDecimal getTotal() {
        return BigDecimal.valueOf(quantity).multiply(BigDecimal.valueOf(unitCost));
    }

    /** "Direct Cost" or "Indirect Cost". */
    public abstract String getCategory();

    /** A new, independent item of the same concrete type (used by the Duplicate button). */
    public abstract CostItem copy();
}
