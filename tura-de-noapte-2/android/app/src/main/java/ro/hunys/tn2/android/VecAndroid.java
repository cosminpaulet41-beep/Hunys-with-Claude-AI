package ro.hunys.tn2.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

import ro.hunys.tn2.Vec;

/** Desenează butoanele de pe ecran (formele din Vec) netezit, la rezoluția telefonului. */
final class VecAndroid {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Path path = new Path();

    VecAndroid() {
        fill.setStyle(Paint.Style.FILL);
        stroke.setStyle(Paint.Style.STROKE);
        text.setTypeface(Typeface.DEFAULT_BOLD);
    }

    void draw(Canvas c, Vec v) {
        float[] f = v.f;
        for (int k = 0; k < v.n; k++) {
            int i = k * 7;
            int col = v.col[k];
            switch (v.op[k]) {
                case Vec.CIRCLE:
                    fill.setColor(col);
                    c.drawCircle(f[i], f[i + 1], f[i + 2], fill);
                    break;
                case Vec.RING:
                    stroke.setColor(col);
                    stroke.setStrokeWidth(f[i + 3]);
                    stroke.setStrokeCap(Paint.Cap.BUTT);
                    c.drawCircle(f[i], f[i + 1], f[i + 2], stroke);
                    break;
                case Vec.RRECT:
                    fill.setColor(col);
                    r.set(f[i], f[i + 1], f[i] + f[i + 2], f[i + 1] + f[i + 3]);
                    c.drawRoundRect(r, f[i + 4], f[i + 4], fill);
                    break;
                case Vec.RRECT_STROKE:
                    stroke.setColor(col);
                    stroke.setStrokeWidth(f[i + 5]);
                    r.set(f[i], f[i + 1], f[i] + f[i + 2], f[i + 1] + f[i + 3]);
                    c.drawRoundRect(r, f[i + 4], f[i + 4], stroke);
                    break;
                case Vec.LINE:
                    stroke.setColor(col);
                    stroke.setStrokeWidth(f[i + 4]);
                    stroke.setStrokeCap(Paint.Cap.ROUND);
                    c.drawLine(f[i], f[i + 1], f[i + 2], f[i + 3], stroke);
                    break;
                case Vec.POLY: {
                    float[] xy = (float[]) v.obj[k];
                    path.reset();
                    path.moveTo(xy[0], xy[1]);
                    for (int j = 2; j < xy.length; j += 2) {
                        path.lineTo(xy[j], xy[j + 1]);
                    }
                    path.close();
                    fill.setColor(col);
                    c.drawPath(path, fill);
                    break;
                }
                case Vec.ARC:
                    stroke.setColor(col);
                    stroke.setStrokeWidth(f[i + 5]);
                    stroke.setStrokeCap(Paint.Cap.ROUND);
                    r.set(f[i] - f[i + 2], f[i + 1] - f[i + 2], f[i] + f[i + 2], f[i + 1] + f[i + 2]);
                    c.drawArc(r, f[i + 3], f[i + 4], false, stroke);
                    break;
                case Vec.TEXT: {
                    String s = (String) v.obj[k];
                    text.setTextSize(f[i + 2]);
                    float w = text.measureText(s);
                    float x = f[i];
                    int al = (int) f[i + 3];
                    if (al == 1) {
                        x -= w / 2f;
                    } else if (al == 2) {
                        x -= w;
                    }
                    int a = Math.min(160, col >>> 24);
                    text.setColor(a << 24);
                    c.drawText(s, x + f[i + 2] * 0.06f, f[i + 1] + f[i + 2] * 0.06f, text);
                    text.setColor(col);
                    c.drawText(s, x, f[i + 1], text);
                    break;
                }
                default:
                    break;
            }
        }
    }
}
