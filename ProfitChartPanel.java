import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Draws the Profit Analysis Chart with Java2D (Swing has no chart component).
 *
 * Every line on the chart is straight, so each one is computed directly from price, fixed cost and variable
 * cost per unit:
 *   Sales Revenue = price x q          Total Cost = fixed + variable x q      Profit = revenue - total cost
 *   Variable Cost = variable x q       Fixed Cost = fixed (flat)
 * The shaded band between the break-even point and the expected sales is the margin of safety.
 * Move the mouse over the chart to read the exact values at any quantity.
 */
public class ProfitChartPanel extends JPanel {

    private static final String[] NAMES = {"Sales Revenue", "Total Cost", "Profit", "Variable Cost", "Fixed Cost"};
    private static final Color[] COLORS = {
            new Color(0x22C55E), new Color(0xEF4444), new Color(0x3B82F6), new Color(0xF97316), new Color(0x9CA3AF)};
    private static final boolean[] DASHED = {false, false, false, true, true};

    private final double price;
    private final double fixed;
    private final double variable;
    private final double breakEven;
    private final int expected;
    private final double xMax;

    // value range of the Y axis
    private double dataHi;
    private double yLo;
    private double yHi;
    private double yStep;

    private Rectangle plot = new Rectangle();
    private int hoverQty = -1;

    public ProfitChartPanel(ProductPricingModel model, AnalysisResult result) {
        this.price = result.getRecommendedPrice();
        this.fixed = result.getTotalIndirectCost();
        this.variable = result.getUnitVariableCost();
        this.breakEven = result.getBreakEvenPoint();
        this.expected = model.getForecastedSalesQuantity();

        // same X range as the web app: the larger of production and sales, plus a 20% margin
        int max = Math.max(model.getPlannedProductionQuantity(), expected);
        this.xMax = Math.max(1, max + Math.ceil(max * 0.2));

        computeYRange();
        setBackground(Theme.BG);
        setOpaque(true);
        setPreferredSize(new Dimension(900, 520));

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (plot.contains(e.getPoint())) {
                    double q = (e.getX() - plot.x) / (double) plot.width * xMax;
                    hoverQty = (int) Math.max(0, Math.min(Math.round(q), (long) xMax));
                } else {
                    hoverQty = -1;
                }
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverQty = -1;
                repaint();
            }
        });
    }

    // ------------------------------------------------------------------ data

    private double value(int series, double q) {
        switch (series) {
            case 0:  return price * q;
            case 1:  return fixed + variable * q;
            case 2:  return price * q - (fixed + variable * q);
            case 3:  return variable * q;
            default: return fixed;
        }
    }

    private void computeYRange() {
        double lo = 0;
        double hi = 0;
        for (int s = 0; s < NAMES.length; s++) {
            lo = Math.min(lo, Math.min(value(s, 0), value(s, xMax)));
            hi = Math.max(hi, Math.max(value(s, 0), value(s, xMax)));
        }
        if (hi - lo < 1e-9) {          // nothing to show yet: use the same 0..4 axis as the web app
            lo = 0;
            hi = 4;
        }
        dataHi = hi;
        yStep = niceStep(hi - lo, 5);
        yLo = Math.floor(lo / yStep) * yStep;
        yHi = Math.ceil(hi / yStep) * yStep;
    }

    /** Rounds a step size up to 1, 2 or 5 times a power of ten, so axis labels look tidy. */
    private static double niceStep(double range, int ticks) {
        double raw = range / ticks;
        double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
        double n = raw / magnitude;
        double nice = n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10;
        return nice * magnitude;
    }

    // ------------------------------------------------------------------ painting

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UiKit.smooth(g2);

        Font tickFont = Theme.font(Font.PLAIN, 11);
        g2.setFont(tickFont);
        FontMetrics fm = g2.getFontMetrics();

        int yLabelWidth = 0;
        for (double v = yLo; v <= yHi + yStep / 2; v += yStep) {
            yLabelWidth = Math.max(yLabelWidth, fm.stringWidth(Fmt.money(v)));
        }
        int left = 20 + yLabelWidth + 18;
        int top = 46;
        int right = 24;
        int bottom = 54;
        plot = new Rectangle(left, top, getWidth() - left - right, getHeight() - top - bottom);
        if (plot.width < 40 || plot.height < 40) {
            g2.dispose();
            return;
        }

        drawGrid(g2, fm);
        drawMarginOfSafety(g2);
        drawSeries(g2);
        drawReferenceLines(g2, fm);
        drawAxisTitles(g2, fm);
        drawLegend(g2);
        if (hoverQty >= 0) {
            drawHover(g2);
        }
        g2.dispose();
    }

    private double px(double q) {
        return plot.x + q / xMax * plot.width;
    }

    private double py(double v) {
        return plot.y + plot.height - (v - yLo) / (yHi - yLo) * plot.height;
    }

    private static final BasicStroke GRID = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10f, new float[]{3f, 3f}, 0f);

    private void drawGrid(Graphics2D g2, FontMetrics fm) {
        Color grid = new Color(0x3A3A40);
        g2.setStroke(GRID);
        g2.setColor(grid);

        // horizontal lines + money labels
        for (double v = yLo; v <= yHi + yStep / 2; v += yStep) {
            int y = (int) Math.round(py(v));
            g2.setColor(grid);
            g2.setStroke(GRID);
            g2.drawLine(plot.x, y, plot.x + plot.width, y);
            g2.setColor(Theme.MUTED);
            String label = Fmt.money(v);
            g2.drawString(label, plot.x - 10 - fm.stringWidth(label), y + fm.getAscent() / 2 - 1);
        }

        // vertical lines + quantity labels
        long xStep = Math.max(1, Math.round(niceStep(xMax, 8)));
        for (long q = 0; q <= xMax; q += xStep) {
            int x = (int) Math.round(px(q));
            g2.setColor(grid);
            g2.setStroke(GRID);
            g2.drawLine(x, plot.y, x, plot.y + plot.height);
            g2.setColor(Theme.MUTED);
            String label = String.valueOf(q);
            g2.drawString(label, x - fm.stringWidth(label) / 2, plot.y + plot.height + 18);
        }

        // right edge, then the solid Y axis line
        g2.setColor(grid);
        g2.setStroke(GRID);
        g2.drawLine(plot.x + plot.width, plot.y, plot.x + plot.width, plot.y + plot.height);
        g2.setColor(new Color(0xA1A1AA));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(plot.x, plot.y, plot.x, plot.y + plot.height);
    }

    private void drawMarginOfSafety(Graphics2D g2) {
        // A negative break-even point means the price is below the variable cost: it never breaks even,
        // so every unit up to the expected sales is a loss.
        boolean neverBreaksEven = breakEven < 0;
        boolean positive = !neverBreaksEven && expected > breakEven;
        double start = neverBreaksEven ? 0 : Math.min(breakEven, expected);
        double end = neverBreaksEven ? expected : Math.max(breakEven, expected);
        start = Math.max(0, Math.min(start, xMax));
        end = Math.max(0, Math.min(end, xMax));

        Color base = positive ? new Color(0x4ADE80) : new Color(0xF87171);
        double x1 = px(start);
        double x2 = px(end);
        double yTop = py(dataHi);
        double yBottom = py(0);

        if (x2 - x1 >= 1) {
            g2.setPaint(new GradientPaint(0, (float) yTop, new Color(base.getRed(), base.getGreen(), base.getBlue(), 55),
                    0, (float) yBottom, new Color(base.getRed(), base.getGreen(), base.getBlue(), 0)));
            g2.fill(new java.awt.geom.Rectangle2D.Double(x1, yTop, x2 - x1, yBottom - yTop));
        }

        g2.setFont(Theme.font(Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        String label = positive ? "Margin of Safety" : "Negative Margin";
        int lw = fm.stringWidth(label);
        int lx = (int) Math.round((x1 + x2) / 2 - lw / 2.0);
        lx = Math.max(plot.x + 4, Math.min(lx, plot.x + plot.width - lw - 4));
        g2.setColor(Theme.TEXT);
        g2.drawString(label, lx, (int) Math.round(yTop) + 22);
    }

    private void drawSeries(Graphics2D g2) {
        Rectangle oldClip = g2.getClipBounds();
        g2.setClip(plot.x, plot.y - 2, plot.width + 2, plot.height + 4);
        for (int s = 0; s < NAMES.length; s++) {
            g2.setColor(COLORS[s]);
            g2.setStroke(DASHED[s]
                    ? new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f)
                    : new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) Math.round(px(0)), (int) Math.round(py(value(s, 0))),
                    (int) Math.round(px(xMax)), (int) Math.round(py(value(s, xMax))));
        }
        g2.setClip(oldClip);
    }

    private void drawReferenceLines(Graphics2D g2, FontMetrics fm) {
        g2.setFont(Theme.font(Font.PLAIN, 11));
        fm = g2.getFontMetrics();
        if (breakEven >= 0 && breakEven <= xMax) {      // off the chart: don't draw it
            drawReference(g2, fm, breakEven, new Color(0x71717A),
                    "Break-even: " + Fmt.fixed(breakEven, 0) + " units", plot.y + plot.height - 12);
        }
        drawReference(g2, fm, expected, new Color(0x60A5FA),
                "Expected Sales: " + Fmt.fixed(expected, 2), plot.y + plot.height - 32);
    }

    private void drawReference(Graphics2D g2, FontMetrics fm, double q, Color color, String label, int baseline) {
        int x = (int) Math.round(px(q));
        g2.setColor(color);
        g2.setStroke(GRID);
        g2.drawLine(x, plot.y, x, plot.y + plot.height);
        g2.setColor(Theme.TEXT);
        int w = fm.stringWidth(label);
        int tx = x + 5;
        if (tx + w > plot.x + plot.width - 2) {
            tx = x - 5 - w;               // would run off the right edge, so put it on the left of the line
        }
        g2.drawString(label, tx, baseline);
    }

    private void drawAxisTitles(Graphics2D g2, FontMetrics fm) {
        g2.setFont(Theme.font(Font.PLAIN, 11));
        fm = g2.getFontMetrics();
        g2.setColor(Theme.TEXT);
        String x = "Quantity Produced";
        g2.drawString(x, plot.x + (plot.width - fm.stringWidth(x)) / 2, plot.y + plot.height + 40);

        String y = "Amount (" + Fmt.PESO + ")";
        AffineTransform saved = g2.getTransform();
        g2.rotate(-Math.PI / 2, 14, plot.y + plot.height / 2.0);
        g2.drawString(y, 14 - fm.stringWidth(y) / 2, plot.y + plot.height / 2 + 4);
        g2.setTransform(saved);
    }

    private void drawLegend(Graphics2D g2) {
        g2.setFont(Theme.font(Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        int itemGap = 22;
        int markerW = 20;
        int total = 0;
        for (String n : NAMES) {
            total += markerW + 6 + fm.stringWidth(n) + itemGap;
        }
        total -= itemGap;
        int x = Math.max(plot.x, plot.x + (plot.width - total) / 2);
        int y = 20;
        for (int s = 0; s < NAMES.length; s++) {
            g2.setColor(COLORS[s]);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x, y - 4, x + markerW, y - 4);
            g2.draw(new Ellipse2D.Double(x + markerW / 2.0 - 3, y - 7, 6, 6));
            g2.drawString(NAMES[s], x + markerW + 6, y);
            x += markerW + 6 + fm.stringWidth(NAMES[s]) + itemGap;
        }
    }

    private void drawHover(Graphics2D g2) {
        double q = hoverQty;
        int x = (int) Math.round(px(q));

        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x, plot.y, x, plot.y + plot.height);

        for (int s = 0; s < NAMES.length; s++) {
            g2.setColor(COLORS[s]);
            g2.fill(new Ellipse2D.Double(x - 3.5, py(value(s, q)) - 3.5, 7, 7));
        }

        g2.setFont(Theme.font(Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        String[] lines = new String[NAMES.length + 1];
        lines[0] = "Quantity: " + hoverQty;
        int widest = fm.stringWidth(lines[0]);
        for (int s = 0; s < NAMES.length; s++) {
            lines[s + 1] = NAMES[s] + ": " + Fmt.money(value(s, q));
            widest = Math.max(widest, fm.stringWidth(lines[s + 1]));
        }
        int lineH = 18;
        int w = widest + 24;
        int h = lines.length * lineH + 14;
        int bx = x + 14;
        if (bx + w > plot.x + plot.width) {
            bx = x - 14 - w;
        }
        int by = plot.y + 8;

        g2.setColor(Theme.SURFACE);
        g2.fillRoundRect(bx, by, w, h, 8, 8);
        g2.setColor(Theme.BORDER_HI);
        g2.drawRoundRect(bx, by, w, h, 8, 8);
        for (int i = 0; i < lines.length; i++) {
            g2.setColor(i == 0 ? Theme.TEXT : COLORS[i - 1]);
            g2.setFont(Theme.font(i == 0 ? Font.BOLD : Font.PLAIN, 12));
            g2.drawString(lines[i], bx + 12, by + 8 + (i + 1) * lineH - 5);
        }
    }
}
