/** An overhead cost that is not tied to any single unit (rent, utilities). The analyzer treats it as a fixed cost. */
public class IndirectCost extends CostItem {

    public IndirectCost(String name, double quantity, String unit, double unitCost) {
        super(name, quantity, unit, unitCost);
    }

    @Override
    public String getCategory() {
        return "Indirect Cost";
    }

    @Override
    public CostItem copy() {
        return new IndirectCost(getName(), getQuantity(), getUnit(), getUnitCost());
    }
}
