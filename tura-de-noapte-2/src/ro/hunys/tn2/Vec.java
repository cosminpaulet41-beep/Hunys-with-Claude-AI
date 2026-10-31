package ro.hunys.tn2;

/**
 * Listă de forme vectoriale (în pixeli de ecran) pentru butoanele de pe ecran. Nu sunt pixelate:
 * platforma le desenează la rezoluția telefonului, cu margini netede (Canvas pe Android, Java2D la
 * capturile de pe calculator).
 */
public final class Vec {
    public static final int CIRCLE = 0, RING = 1, RRECT = 2, RRECT_STROKE = 3, LINE = 4, POLY = 5, ARC = 6, TEXT = 7;

    public int n;
    public int[] op = new int[256], col = new int[256];
    public float[] f = new float[256 * 7];
    public Object[] obj = new Object[256];

    public void clear() {
        n = 0;
        for (int i = 0; i < obj.length; i++) {
            obj[i] = null;
        }
    }

    private int add(int o, int c) {
        if (n == op.length) {
            int m = n * 2;
            int[] o2 = new int[m], c2 = new int[m];
            float[] f2 = new float[m * 7];
            Object[] ob2 = new Object[m];
            System.arraycopy(op, 0, o2, 0, n);
            System.arraycopy(col, 0, c2, 0, n);
            System.arraycopy(f, 0, f2, 0, n * 7);
            System.arraycopy(obj, 0, ob2, 0, n);
            op = o2;
            col = c2;
            f = f2;
            obj = ob2;
        }
        op[n] = o;
        col[n] = c;
        return n++;
    }

    public void circle(float cx, float cy, float r, int argb) {
        int i = add(CIRCLE, argb) * 7;
        f[i] = cx;
        f[i + 1] = cy;
        f[i + 2] = r;
    }

    public void ring(float cx, float cy, float r, float width, int argb) {
        int i = add(RING, argb) * 7;
        f[i] = cx;
        f[i + 1] = cy;
        f[i + 2] = r;
        f[i + 3] = width;
    }

    public void rrect(float x, float y, float w, float h, float rad, int argb) {
        int i = add(RRECT, argb) * 7;
        f[i] = x;
        f[i + 1] = y;
        f[i + 2] = w;
        f[i + 3] = h;
        f[i + 4] = rad;
    }

    public void rrectStroke(float x, float y, float w, float h, float rad, float width, int argb) {
        int i = add(RRECT_STROKE, argb) * 7;
        f[i] = x;
        f[i + 1] = y;
        f[i + 2] = w;
        f[i + 3] = h;
        f[i + 4] = rad;
        f[i + 5] = width;
    }

    public void line(float x0, float y0, float x1, float y1, float width, int argb) {
        int i = add(LINE, argb) * 7;
        f[i] = x0;
        f[i + 1] = y0;
        f[i + 2] = x1;
        f[i + 3] = y1;
        f[i + 4] = width;
    }

    /** Poligon plin; xy = perechi de coordonate de ecran. */
    public void poly(float[] xy, int argb) {
        int k = add(POLY, argb);
        obj[k] = xy;
    }

    /** Arc (grade, 0 = dreapta, sensul acelor de ceas pe ecran), cu capete rotunjite. */
    public void arc(float cx, float cy, float r, float startDeg, float sweepDeg, float width, int argb) {
        int i = add(ARC, argb) * 7;
        f[i] = cx;
        f[i + 1] = cy;
        f[i + 2] = r;
        f[i + 3] = startDeg;
        f[i + 4] = sweepDeg;
        f[i + 5] = width;
    }

    /** Text pe linia de bază y; align 0 stânga, 1 centru, 2 dreapta. */
    public void text(String s, float x, float y, float size, int argb, int align) {
        int k = add(TEXT, argb);
        int i = k * 7;
        f[i] = x;
        f[i + 1] = y;
        f[i + 2] = size;
        f[i + 3] = align;
        obj[k] = s;
    }
}
