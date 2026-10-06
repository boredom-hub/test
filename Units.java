import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The units a cost can be measured in, grouped by category (from base-units.ts in appweb). */
public final class Units {

    /** One selectable unit. */
    public static final class Unit {
        public final String code;      // "kg"
        public final String label;     // "Kilograms (kg)"
        public final String category;  // "Weight"

        Unit(String category, String code, String label) {
            this.category = category;
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final List<Unit> ALL = new ArrayList<Unit>();

    static {
        add("Weight", "kg", "Kilograms (kg)");
        add("Weight", "g", "Grams (g)");
        add("Weight", "mg", "Milligrams (mg)");
        add("Weight", "lb", "Pounds (lb)");
        add("Weight", "oz", "Ounces (oz)");

        add("Volume", "l", "Liters (L)");
        add("Volume", "ml", "Milliliters (mL)");
        add("Volume", "fl_oz", "Fluid Ounces (fl oz)");
        add("Volume", "cup", "Cups");
        add("Volume", "tbsp", "Tablespoons");
        add("Volume", "tsp", "Teaspoons");

        add("Count", "pcs", "Pieces");
        add("Count", "dozen", "Dozen");

        add("Packaging", "pack", "Pack");
        add("Packaging", "box", "Box");
        add("Packaging", "bag", "Bag");
        add("Packaging", "can", "Can");
        add("Packaging", "bottle", "Bottle");
        add("Packaging", "jar", "Jar");
    }

    private Units() { }

    private static void add(String category, String code, String label) {
        ALL.add(new Unit(category, code, label));
    }

    public static List<Unit> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static Unit first() {
        return ALL.get(0);
    }

    /** Looks a unit up by its code, falling back to the first unit if the code is unknown. */
    public static Unit byCode(String code) {
        for (Unit u : ALL) {
            if (u.code.equals(code)) {
                return u;
            }
        }
        return first();
    }
}
