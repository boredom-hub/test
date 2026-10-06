import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Small line icons drawn with Java2D (no image files needed). They are drawn on a 24x24 grid
 * and scaled to the requested size, in the style of the Lucide icons the web app used.
 */
public final class Icons {

    public enum Kind { PLUS, MINUS, EDIT, COPY, TRASH, COINS, BOX, TREND_UP, PERCENT, CHEVRON_DOWN }

    private Icons() { }

    public static Icon of(Kind kind, int size, Color color) {
        return new VectorIcon(kind, size, color);
    }

    private static final class VectorIcon implements Icon {
        private final Kind kind;
        private final int size;
        private final Color color;

        VectorIcon(Kind kind, int size, Color color) {
            this.kind = kind;
            this.size = size;
            this.color = color;
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double s = size / 24.0;
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(g2);
            g2.dispose();
        }

        private void draw(Graphics2D g) {
            switch (kind) {
                case PLUS:
                    g.draw(new Line2D.Double(12, 5, 12, 19));
                    g.draw(new Line2D.Double(5, 12, 19, 12));
                    break;
                case MINUS:
                    g.draw(new Line2D.Double(5, 12, 19, 12));
                    break;
                case CHEVRON_DOWN:
                    g.draw(poly(false, 6, 9, 12, 15, 18, 9));
                    break;
                case TREND_UP:
                    g.draw(poly(false, 22, 7, 13.5, 15.5, 8.5, 10.5, 2, 17));
                    g.draw(poly(false, 16, 7, 22, 7, 22, 13));
                    break;
                case PERCENT:
                    g.draw(new Line2D.Double(19, 5, 5, 19));
                    g.draw(new Ellipse2D.Double(4, 4, 5, 5));
                    g.draw(new Ellipse2D.Double(15, 15, 5, 5));
                    break;
                case EDIT:
                    g.draw(poly(true, 3, 21, 4.5, 15.5, 16.5, 3.5, 20.5, 7.5, 8.5, 19.5));
                    g.draw(new Line2D.Double(14, 6, 18, 10));
                    break;
                case COPY:
                    g.draw(new RoundRectangle2D.Double(9, 9, 12, 12, 3, 3));
                    Path2D back = new Path2D.Double();
                    back.moveTo(5, 15);
                    back.lineTo(4.5, 15);
                    back.quadTo(3, 15, 3, 13.5);
                    back.lineTo(3, 4.5);
                    back.quadTo(3, 3, 4.5, 3);
                    back.lineTo(13.5, 3);
                    back.quadTo(15, 3, 15, 4.5);
                    back.lineTo(15, 5);
                    g.draw(back);
                    break;
                case TRASH:
                    g.draw(new Line2D.Double(3, 6, 21, 6));
                    Path2D body = new Path2D.Double();
                    body.moveTo(19, 6);
                    body.lineTo(19, 19);
                    body.quadTo(19, 21, 17, 21);
                    body.lineTo(7, 21);
                    body.quadTo(5, 21, 5, 19);
                    body.lineTo(5, 6);
                    g.draw(body);
                    Path2D lid = new Path2D.Double();
                    lid.moveTo(8, 6);
                    lid.lineTo(8, 4);
                    lid.quadTo(8, 2.5, 9.5, 2.5);
                    lid.lineTo(14.5, 2.5);
                    lid.quadTo(16, 2.5, 16, 4);
                    lid.lineTo(16, 6);
                    g.draw(lid);
                    g.draw(new Line2D.Double(10, 11, 10, 17));
                    g.draw(new Line2D.Double(14, 11, 14, 17));
                    break;
                case COINS:
                    Shape front = new Ellipse2D.Double(2.5, 2.5, 12, 12);
                    Area outside = new Area(new Rectangle2D.Double(0, 0, 24, 24));
                    outside.subtract(new Area(front));
                    Shape oldClip = g.getClip();
                    g.clip(outside);
                    g.draw(new Ellipse2D.Double(9.5, 9.5, 12, 12));
                    g.setClip(oldClip);
                    g.draw(front);
                    g.draw(new Line2D.Double(8.5, 6.5, 8.5, 11.5));
                    g.draw(new Line2D.Double(7, 8, 8.5, 6.5));
                    break;
                case BOX:
                    g.draw(poly(true, 12, 2.5, 20.5, 7.3, 20.5, 16.7, 12, 21.5, 3.5, 16.7, 3.5, 7.3));
                    g.draw(poly(false, 3.5, 7.3, 12, 12, 20.5, 7.3));
                    g.draw(new Line2D.Double(12, 12, 12, 21.5));
                    break;
                default:
                    break;
            }
        }

        private static Path2D poly(boolean close, double... xy) {
            Path2D p = new Path2D.Double();
            p.moveTo(xy[0], xy[1]);
            for (int i = 2; i < xy.length; i += 2) {
                p.lineTo(xy[i], xy[i + 1]);
            }
            if (close) {
                p.closePath();
            }
            return p;
        }
    }
}
