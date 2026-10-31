package ro.hunys.tn2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Grafica jocului, citită din resurse: texturile (tools/textures.py), personajele, obiectele, armele
 * și decorul (tools/sprites.py). Lumina se aplică la desen, deci nu mai ținem copii umbrite.
 */
public final class Art {
    public static final int TS = 64, TSH = 6, TSM = 63;

    // texturile (ordinea din tools/textures.py)
    public static final int T_OFFICE = 0, T_STONE = 5, T_LOGO = 6, T_GLASS = 10, T_FOAM = 11, T_BACKDROP = 12;
    public static final int T_MON = 13, T_POSTER = 16, T_KITCHEN = 19, T_WC = 20, T_ARCH = 21, T_SERV = 23;
    public static final int T_CONC = 24, T_PANEL = 25, T_MIRROR = 26, T_GARAGE = 27, T_ELEV = 28;
    public static final int T_DOOR = 29, T_DBLUE = 30, T_DRED = 31, T_DYEL = 32, T_DGREEN = 33;
    public static final int T_DSTUDIO = 34, T_DEXIT = 35, T_DENTR = 36, T_DCODE = 37;
    public static final int F_LOBBY = 38, F_LINO = 39, F_CARPET = 40, F_STUDIO = 41, F_KITCHEN = 42, F_WC = 43;
    public static final int F_CONC = 44, F_WOOD = 45, F_DOCK = 46;
    public static final int C_TILES = 47, C_TILES_L = 48, C_LOBBY = 49, C_LOBBY_L = 50, C_STUDIO = 51;
    public static final int C_STUDIO_L = 52, C_CONC = 53, C_CONC_L = 54, C_RED = 55;

    /** Personaje (rândurile din z.png). */
    public static final int Z_GUARD = 0, Z_VECTRA = 1, Z_IOANA = 2, Z_REPORTER = 3, Z_CAMERA = 4;
    public static final int Z_ANCHOR = 5, Z_COOK = 6, Z_MARA = 7, Z_SPIT = 8, ZTYPES = 9;
    public static final int ZW = 48, ZH = 64;
    public static final int IS = 24, NITEMS = 11;

    public static int[][] tex;
    /** [tip][cadru: mers A, mers B, lovit, căzut, scuipă] */
    public static Img[][] zombie;
    public static Img[] item;
    public static Img[] gun, flash;
    /** Mâna cu pistolul, randată 3D (alfa parțial, ARGB). */
    public static Img handPistol;
    public static Img acid;
    public static Img[] decor;
    public static String[] decorName;
    public static float[] decorH;
    public static boolean[] decorSolid;

    private Art() {
    }

    public static void load(Platform p) throws IOException {
        if (tex != null) {
            return;
        }
        Img atlas = p.image("tex.png");
        int cols = atlas.w / TS;
        int n = cols * (atlas.h / TS);
        tex = new int[n][];
        for (int i = 0; i < n; i++) {
            Img t = atlas.cut((i % cols) * TS, (i / cols) * TS, TS, TS);
            for (int k = 0; k < t.px.length; k++) {
                t.px[k] |= 0xFF000000;
            }
            tex[i] = t.px;
        }
        Img z = p.image("z.png");
        zombie = new Img[ZTYPES][];
        for (int t = 0; t < ZTYPES; t++) {
            Img[] f = new Img[5];
            f[0] = z.cut(0, t * ZH, ZW, ZH);
            f[1] = z.cut(ZW, t * ZH, ZW, ZH);
            f[2] = hit(f[0]);
            f[3] = z.cut(2 * ZW, t * ZH, ZW, ZH);
            f[4] = t == Z_SPIT ? z.cut(3 * ZW, t * ZH, ZW, ZH) : f[0];
            zombie[t] = f;
        }
        Img it = p.image("items.png");
        item = new Img[NITEMS];
        for (int i = 0; i < NITEMS; i++) {
            item[i] = it.cut(i * IS, 0, IS, IS);
        }
        String[] gn = {"gun.png", "gun2.png", "gun3.png"};
        String[] fn = {"flash.png", "flash2.png", "flash3.png"};
        gun = new Img[3];
        flash = new Img[3];
        for (int w = 0; w < 3; w++) {
            Img g = p.image(gn[w]);
            gun[w] = g.cut(0, 0, g.w, g.h);
            Img f = p.image(fn[w]);
            flash[w] = f.cut(0, 0, f.w, f.h);
        }
        handPistol = crop(p.image("hand_pistol.png"));
        Img a = p.image("acid.png");
        acid = a.cut(0, 0, a.w, a.h);

        Img ds = p.image("decor.png");
        BufferedReader r = new BufferedReader(new InputStreamReader(p.open("decor.txt"), "UTF-8"));
        java.util.Vector lines = new java.util.Vector();
        String line;
        while ((line = r.readLine()) != null) {
            if (line.trim().length() > 0) {
                lines.addElement(line.trim());
            }
        }
        r.close();
        int nd = lines.size();
        decor = new Img[nd];
        decorName = new String[nd];
        decorH = new float[nd];
        decorSolid = new boolean[nd];
        int dcols = ds.w / 64;
        for (int i = 0; i < nd; i++) {
            String[] f = split((String) lines.elementAt(i));
            int idx = Integer.parseInt(f[0]);
            int w = Integer.parseInt(f[2]), h = Integer.parseInt(f[3]);
            decorName[idx] = f[1];
            decorH[idx] = Float.parseFloat(f[4]);
            decorSolid[idx] = f[5].equals("1");
            int cx = (idx % dcols) * 64 + (64 - w) / 2;
            int cy = (idx / dcols) * 64 + 64 - h;
            decor[idx] = ds.cut(cx, cy, w, h);
        }
    }

    public static int decorIndex(String name) {
        for (int i = 0; i < decorName.length; i++) {
            if (decorName[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /** Taie marginile complet transparente. */
    static Img crop(Img im) {
        int x0 = im.w, y0 = im.h, x1 = -1, y1 = -1;
        for (int y = 0; y < im.h; y++) {
            for (int x = 0; x < im.w; x++) {
                if ((im.px[y * im.w + x] >>> 24) != 0) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        if (x1 < 0) {
            return im;
        }
        Img r = new Img(x1 - x0 + 1, y1 - y0 + 1);
        for (int y = 0; y < r.h; y++) {
            System.arraycopy(im.px, (y0 + y) * im.w + x0, r.px, y * r.w, r.w);
        }
        return r;
    }

    /**
     * Micșorează o imagine cu alfa prin medierea pixelilor (fără scări), rezultat cu alfa înmulțit
     * în culori (premultiplied), gata de amestecat peste lume.
     */
    public static Img downscale(Img src, int dw, int dh) {
        Img r = new Img(dw, dh);
        for (int y = 0; y < dh; y++) {
            int sy0 = y * src.h / dh, sy1 = Math.max(sy0 + 1, (y + 1) * src.h / dh);
            for (int x = 0; x < dw; x++) {
                int sx0 = x * src.w / dw, sx1 = Math.max(sx0 + 1, (x + 1) * src.w / dw);
                long a = 0, rr = 0, gg = 0, bb = 0;
                int n = 0;
                for (int j = sy0; j < sy1; j++) {
                    for (int i = sx0; i < sx1; i++) {
                        int c = src.px[j * src.w + i];
                        int ca = c >>> 24;
                        a += ca;
                        rr += ((c >> 16) & 255) * ca;
                        gg += ((c >> 8) & 255) * ca;
                        bb += (c & 255) * ca;
                        n++;
                    }
                }
                int aa = (int) (a / n);
                int pr = (int) (rr / 255 / n), pg = (int) (gg / 255 / n), pb = (int) (bb / 255 / n);
                r.px[y * dw + x] = (aa << 24) | (pr << 16) | (pg << 8) | pb;
            }
        }
        return r;
    }

    /** Cadrul „lovit”: mai luminos și roșiatic. */
    private static Img hit(Img src) {
        Img r = new Img(src.w, src.h);
        for (int i = 0; i < src.px.length; i++) {
            int c = src.px[i];
            if (c == 0) {
                continue;
            }
            int rr = Math.min(255, ((c >> 16) & 255) + 90);
            int gg = Math.min(255, ((c >> 8) & 255) + 30);
            int bb = Math.min(255, (c & 255) + 30);
            r.px[i] = 0xFF000000 | (rr << 16) | (gg << 8) | bb;
        }
        return r;
    }

    static String[] split(String s) {
        java.util.Vector v = new java.util.Vector();
        int i = 0;
        while (i < s.length()) {
            while (i < s.length() && s.charAt(i) == ' ') {
                i++;
            }
            int j = i;
            while (j < s.length() && s.charAt(j) != ' ') {
                j++;
            }
            if (j > i) {
                v.addElement(s.substring(i, j));
            }
            i = j;
        }
        String[] r = new String[v.size()];
        v.copyInto(r);
        return r;
    }
}
