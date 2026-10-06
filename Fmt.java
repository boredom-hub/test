import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Number and money formatting shared by all frames. */
public final class Fmt {

    /** The peso sign, written as an escape so it survives any source-file encoding. */
    public static final String PESO = "\u20B1";

    private static final DecimalFormat MONEY =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private Fmt() { }

    /** 1234.5 becomes the peso sign followed by 1,234.50. Negative amounts get a leading minus. */
    public static String money(double value) {
        if (Math.abs(value) < 0.005) {
            value = 0;
        }
        return (value < 0 ? "-" : "") + PESO + MONEY.format(Math.abs(value));
    }

    /** Fixed number of decimals, always with a dot. */
    public static String fixed(double value, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", value);
    }

    /** 2.50 -> "2.5", 3.0 -> "3": no trailing zeros. */
    public static String num(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
