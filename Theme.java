import java.awt.Color;
import java.awt.Font;

/** Colours and fonts for the calculator frames, taken from the appweb dark theme. */
public final class Theme {

    public static final Color BG        = new Color(0x09090B);
    public static final Color SURFACE   = new Color(0x18181B);
    public static final Color BORDER    = new Color(0x27272A);
    public static final Color BORDER_HI = new Color(0x52525B);
    public static final Color TEXT      = new Color(0xFAFAFA);
    public static final Color MUTED     = new Color(0xA1A1AA);
    public static final Color FAINT     = new Color(0x71717A);
    public static final Color GREEN     = new Color(0x22C55E);
    public static final Color GREEN_HI  = new Color(0x16A34A);
    public static final Color ON_GREEN  = new Color(0x052E16);
    public static final Color RED       = new Color(0xEF4444);
    public static final Color AMBER     = new Color(0xF59E0B);

    private Theme() { }

    public static Font font(int style, float size) {
        return new Font(Font.SANS_SERIF, style, Math.round(size));
    }
}
