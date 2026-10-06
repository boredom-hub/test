/** A cost that is directly tied to producing the product (materials, ingredients). It varies with output. */
public class DirectCost extends CostItem {

    public DirectCost(String name, double quantity, String unit, double unitCost) {
        super(name, quantity, unit, unitCost);
    }

    @Override
    public String getCategory() {
        return "Direct Cost";
    }

    @Override
    public CostItem copy() {
        return new DirectCost(getName(), getQuantity(), getUnit(), getUnitCost());
    }
}
