package ro.hunys.tn2;

/** O imagine ARGB simplă (pixel 0 = transparent). */
public final class Img {
    public final int w, h;
    public final int[] px;

    public Img(int w, int h) {
        this.w = w;
        this.h = h;
        this.px = new int[w * h];
    }

    public Img(int w, int h, int[] px) {
        this.w = w;
        this.h = h;
        this.px = px;
    }

    /** Decupează un dreptunghi; pixelii complet transparenți devin 0. */
    public Img cut(int x, int y, int cw, int ch) {
        Img r = new Img(cw, ch);
        for (int j = 0; j < ch; j++) {
            for (int i = 0; i < cw; i++) {
                int sx = x + i, sy = y + j;
                int c = 0;
                if (sx >= 0 && sy >= 0 && sx < w && sy < h) {
                    c = px[sy * w + sx];
                    if ((c >>> 24) < 128) {
                        c = 0;
                    } else {
                        c |= 0xFF000000;
                    }
                }
                r.px[j * cw + i] = c;
            }
        }
        return r;
    }
}
