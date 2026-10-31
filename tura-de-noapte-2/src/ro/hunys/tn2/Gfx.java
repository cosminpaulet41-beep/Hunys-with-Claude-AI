package ro.hunys.tn2;

/**
 * Desen în software pe un buffer ARGB (stratul de interfață, peste lume). Culorile au alfa; desenul
 * se amestecă peste ce e deja în buffer. Totul se face pe grila de pixeli a interfeței, deci arată
 * la fel pe telefon și în capturile făcute pe calculator.
 */
public final class Gfx {
    public final int w, h;
    public final int[] px;
    private int cx0, cy0, cx1, cy1;

    public Gfx(int w, int h) {
        this.w = w;
        this.h = h;
        px = new int[w * h];
        noClip();
    }

    public void clip(int x, int y, int cw, int ch) {
        cx0 = Math.max(0, x);
        cy0 = Math.max(0, y);
        cx1 = Math.min(w, x + cw);
        cy1 = Math.min(h, y + ch);
    }

    public void noClip() {
        cx0 = 0;
        cy0 = 0;
        cx1 = w;
        cy1 = h;
    }

    public void clear(int argb) {
        for (int i = 0; i < px.length; i++) {
            px[i] = argb;
        }
    }

    /** Amestecă src peste dst (ARGB neînmulțit cu alfa). */
    static int over(int dst, int src) {
        int sa = src >>> 24;
        if (sa == 255) {
            return src;
        }
        if (sa == 0) {
            return dst;
        }
        int da = dst >>> 24;
        if (da == 0) {
            return src;
        }
        int ia = 255 - sa;
        int oa = sa + da * ia / 255;
        int dw = da * ia / 255;
        int r = (((src >> 16) & 255) * sa + ((dst >> 16) & 255) * dw) / oa;
        int g = (((src >> 8) & 255) * sa + ((dst >> 8) & 255) * dw) / oa;
        int b = ((src & 255) * sa + (dst & 255) * dw) / oa;
        return (oa << 24) | (r << 16) | (g << 8) | b;
    }

    public static int alpha(int rgb, int a) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    public void fill(int x, int y, int fw, int fh, int argb) {
        int x0 = Math.max(cx0, x), y0 = Math.max(cy0, y);
        int x1 = Math.min(cx1, x + fw), y1 = Math.min(cy1, y + fh);
        if (x0 >= x1 || y0 >= y1) {
            return;
        }
        int a = argb >>> 24;
        for (int yy = y0; yy < y1; yy++) {
            int o = yy * w;
            if (a == 255) {
                for (int xx = x0; xx < x1; xx++) {
                    px[o + xx] = argb;
                }
            } else {
                for (int xx = x0; xx < x1; xx++) {
                    px[o + xx] = over(px[o + xx], argb);
                }
            }
        }
    }

    public void set(int x, int y, int argb) {
        if (x >= cx0 && y >= cy0 && x < cx1 && y < cy1) {
            px[y * w + x] = over(px[y * w + x], argb);
        }
    }

    public void rect(int x, int y, int rw, int rh, int t, int argb) {
        fill(x, y, rw, t, argb);
        fill(x, y + rh - t, rw, t, argb);
        fill(x, y + t, t, rh - 2 * t, argb);
        fill(x + rw - t, y + t, t, rh - 2 * t, argb);
    }

    /** Panou pixelat: fond, margine și colțuri teșite (un pixel). */
    public void panel(int x, int y, int pw, int ph, int bg, int border) {
        fill(x + 1, y + 1, pw - 2, ph - 2, bg);
        fill(x + 1, y, pw - 2, 1, border);
        fill(x + 1, y + ph - 1, pw - 2, 1, border);
        fill(x, y + 1, 1, ph - 2, border);
        fill(x + pw - 1, y + 1, 1, ph - 2, border);
    }

    /** Disc plin, pixelat. */
    public void disc(int cx, int cy, int r, int argb) {
        for (int j = -r; j <= r; j++) {
            int span = (int) Math.sqrt(r * r - j * j + r * 0.8f);
            fill(cx - span, cy + j, span * 2 + 1, 1, argb);
        }
    }

    /** Inel pixelat de grosime t. */
    public void ring(int cx, int cy, int r, int t, int argb) {
        int ri = r - t;
        for (int j = -r; j <= r; j++) {
            int so = (int) Math.sqrt(r * r - j * j + r * 0.8f);
            int si = ri > 0 && Math.abs(j) <= ri ? (int) Math.sqrt(ri * ri - j * j + ri * 0.8f) : -1;
            if (si < 0) {
                fill(cx - so, cy + j, so * 2 + 1, 1, argb);
            } else {
                fill(cx - so, cy + j, so - si, 1, argb);
                fill(cx + si + 1, cy + j, so - si, 1, argb);
            }
        }
    }

    public void line(int x0, int y0, int x1, int y1, int argb) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            set(x0, y0, argb);
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    /** Desenează o imagine mărită de `s` ori; alpha 0..255 înmulțește transparența. */
    public void img(Img im, int x, int y, int s, int alpha) {
        for (int j = 0; j < im.h; j++) {
            for (int i = 0; i < im.w; i++) {
                int c = im.px[j * im.w + i];
                if (c == 0) {
                    continue;
                }
                if (alpha < 255) {
                    c = ((((c >>> 24) * alpha) / 255) << 24) | (c & 0xFFFFFF);
                }
                fill(x + i * s, y + j * s, s, s, c);
            }
        }
    }

    /** Desenează o imagine folosită ca mască (orice pixel netransparent ia culoarea dată). */
    public void mask(Img im, int x, int y, int s, int argb) {
        for (int j = 0; j < im.h; j++) {
            for (int i = 0; i < im.w; i++) {
                if (im.px[j * im.w + i] != 0) {
                    fill(x + i * s, y + j * s, s, s, argb);
                }
            }
        }
    }

    /** Pictogramă din șiruri de caractere ('#' = plin), mărită de s ori. */
    public void icon(String[] rows, int x, int y, int s, int argb) {
        for (int j = 0; j < rows.length; j++) {
            String r = rows[j];
            int run = -1;
            for (int i = 0; i <= r.length(); i++) {
                boolean on = i < r.length() && r.charAt(i) == '#';
                if (on && run < 0) {
                    run = i;
                } else if (!on && run >= 0) {
                    fill(x + run * s, y + j * s, (i - run) * s, s, argb);
                    run = -1;
                }
            }
        }
    }
}
