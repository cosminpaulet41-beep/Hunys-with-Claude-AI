package ro.hunys.tn2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Desenează formele vectoriale (butoanele) cu Java2D, netezite, la fel ca pe Android. */
final class VecAwt {
    private VecAwt() {
    }

    static void draw(Graphics2D g, Vec v) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (int k = 0; k < v.n; k++) {
            int i = k * 7;
            float[] f = v.f;
            g.setColor(new Color(v.col[k], true));
            switch (v.op[k]) {
                case Vec.CIRCLE:
                    g.fill(new Ellipse2D.Float(f[i] - f[i + 2], f[i + 1] - f[i + 2], f[i + 2] * 2, f[i + 2] * 2));
                    break;
                case Vec.RING:
                    g.setStroke(new BasicStroke(f[i + 3]));
                    g.draw(new Ellipse2D.Float(f[i] - f[i + 2], f[i + 1] - f[i + 2], f[i + 2] * 2, f[i + 2] * 2));
                    break;
                case Vec.RRECT:
                    g.fill(new RoundRectangle2D.Float(f[i], f[i + 1], f[i + 2], f[i + 3], f[i + 4] * 2, f[i + 4] * 2));
                    break;
                case Vec.RRECT_STROKE:
                    g.setStroke(new BasicStroke(f[i + 5]));
                    g.draw(new RoundRectangle2D.Float(f[i], f[i + 1], f[i + 2], f[i + 3], f[i + 4] * 2, f[i + 4] * 2));
                    break;
                case Vec.LINE:
                    g.setStroke(new BasicStroke(f[i + 4], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(new Line2D.Float(f[i], f[i + 1], f[i + 2], f[i + 3]));
                    break;
                case Vec.POLY: {
                    float[] xy = (float[]) v.obj[k];
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(xy[0], xy[1]);
                    for (int j = 2; j < xy.length; j += 2) {
                        p.lineTo(xy[j], xy[j + 1]);
                    }
                    p.closePath();
                    g.fill(p);
                    break;
                }
                case Vec.ARC:
                    g.setStroke(new BasicStroke(f[i + 5], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Java2D: unghiuri în sens trigonometric, față de axa x; pe ecran y e în jos
                    g.draw(new Arc2D.Float(f[i] - f[i + 2], f[i + 1] - f[i + 2], f[i + 2] * 2, f[i + 2] * 2,
                            -f[i + 3], -f[i + 4], Arc2D.OPEN));
                    break;
                case Vec.TEXT: {
                    String s = (String) v.obj[k];
                    g.setFont(new Font("SansSerif", Font.BOLD, Math.round(f[i + 2])));
                    FontMetrics fm = g.getFontMetrics();
                    int w = fm.stringWidth(s);
                    float x = f[i];
                    int al = (int) f[i + 3];
                    if (al == 1) {
                        x -= w / 2f;
                    } else if (al == 2) {
                        x -= w;
                    }
                    g.setColor(new Color(0, 0, 0, Math.min(160, (v.col[k] >>> 24))));
                    g.drawString(s, x + f[i + 2] * 0.06f, f[i + 1] + f[i + 2] * 0.06f);
                    g.setColor(new Color(v.col[k], true));
                    g.drawString(s, x, f[i + 1]);
                    break;
                }
                default:
                    break;
            }
        }
    }
}
