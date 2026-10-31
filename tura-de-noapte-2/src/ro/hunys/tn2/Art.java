package ro.hunys.tn2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Grafica jocului, citită din resurse: texturile (tools/textures.py, tools/tex_spital.py), personajele,
 * bossii, obiectele, armele și decorul (tools/sprites_spital.py). Lumina se aplică la desen, deci nu mai
 * ținem copii umbrite.
 */
public final class Art {
    public static final int TS = 64, TSH = 6, TSM = 63;

    /** Personaje (rândurile din z.png). */
    public static final int Z_GUARD = 0, Z_VECTRA = 1, Z_IOANA = 2, Z_PATIENT = 3, Z_NURSE = 4, Z_MEDIC = 5;
    public static final int Z_SURGEON = 6, Z_ELENA = 7, Z_CRISTI = 8, Z_SPIT = 9, ZTYPES = 10;
    public static final int ZW = 48, ZH = 64;
    public static final int IS = 24, NITEMS = 11;

    public static int[][] tex;
    /** Numele texturilor (din tex.txt), în ordinea din atlas. */
    public static String[] texName;
    /** [tip][cadru: mers A, mers B, lovit, căzut, scuipă] */
    public static Img[][] zombie;
    /** Subiectul 9: mers A, mers B, lovit, căzut. */
    public static Img[] boss9;
    /** Creatura: mers A, mers B, lovită, căzută, urlă. */
    public static Img[] creature;
    /** Butelia de oxigen (întreagă, spartă) și cele 3 cadre ale exploziei. */
    public static Img[] o2, expl;
    public static Img[] item;
    public static Img[] gun, flash;
    /** Mâinile cu armele (pistol, pușcă, pistol-mitralieră), randate 3D (alfa parțial, ARGB). */
    public static Img[] hand = new Img[3];
    /** Gura țevii pe fiecare imagine (pixeli ai imaginii), ca flacăra să pornească de acolo. */
    public static float[] handTipX = new float[3], handTipY = new float[3];
    /**
     * Armele lungi sunt randate în tot cadrul, cu câmpul vizual al jocului: lățimea și înălțimea cadrului
     * și colțul imaginii tăiate în cadru (0 = arma se așază ca pistolul).
     */
    public static int[] handFW = new int[3], handFH = new int[3], handFX = new int[3], handFY = new int[3];
    public static Img acid;
    /** Cerul din zori: panoramă de 360 de grade (vezi tools/tex_spital.py). */
    public static Img sky;
    public static Img[] decor;
    public static String[] decorName;
    public static float[] decorH, decorR;
    /** 0 = se trece prin el, 1 = solid, 2 = atârnă de tavan. */
    public static int[] decorSolid;

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
        texName = new String[n];
        java.util.Vector names = readLines(p, "tex.txt");
        for (int i = 0; i < names.size(); i++) {
            String[] f = split((String) names.elementAt(i));
            int k = Integer.parseInt(f[0]);
            if (k < n) {
                texName[k] = f[1];
            }
        }
        for (int i = 0; i < n; i++) {
            Img t = atlas.cut((i % cols) * TS, (i / cols) * TS, TS, TS);
            boolean key = "parapet".equals(texName[i]);
            for (int k = 0; k < t.px.length; k++) {
                int c = t.px[k] | 0xFF000000;
                // magenta = transparent (pe acolo se vede cerul)
                t.px[k] = key && c == 0xFFFF00FF ? 0 : c;
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
        Img b = p.image("boss.png");
        int bw = b.w / 3;
        boss9 = new Img[] {b.cut(0, 0, bw, b.h), b.cut(bw, 0, bw, b.h), null, b.cut(2 * bw, 0, bw, b.h)};
        boss9[2] = hit(boss9[0]);
        Img c = p.image("boss3.png");
        int cw = c.w / 4;
        creature = new Img[] {c.cut(0, 0, cw, c.h), c.cut(cw, 0, cw, c.h), null, c.cut(2 * cw, 0, cw, c.h),
            c.cut(3 * cw, 0, cw, c.h)};
        creature[2] = hit(creature[0]);
        Img o = p.image("barrel.png");
        o2 = new Img[] {o.cut(0, 0, o.w / 2, o.h), o.cut(o.w / 2, 0, o.w / 2, o.h)};
        Img e = p.image("expl.png");
        expl = new Img[3];
        for (int k = 0; k < 3; k++) {
            expl[k] = e.cut(k * e.h, 0, e.h, e.h);
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
        String[] hn = {"pistol", "shotgun", "smg"};
        for (int w = 0; w < 3; w++) {
            try {
                hand[w] = crop(p.image("hand_" + hn[w] + ".png"));
            } catch (IOException ex) {
                hand[w] = null;       // rămâne arma desenată în pixeli
                continue;
            }
            handTipX[w] = hand[w].w * 0.07f;
            handTipY[w] = hand[w].h * 0.08f;
            try {
                java.util.Vector tl = readLines(p, "hand_" + hn[w] + ".txt");
                for (int k = 0; k < tl.size(); k++) {
                    String[] tf = split((String) tl.elementAt(k));
                    if (tf[0].equals("teava")) {
                        handTipX[w] = Float.parseFloat(tf[1]);
                        handTipY[w] = Float.parseFloat(tf[2]);
                    } else if (tf[0].equals("cadru")) {
                        handFW[w] = Integer.parseInt(tf[1]);
                        handFH[w] = Integer.parseInt(tf[2]);
                        handFX[w] = Integer.parseInt(tf[3]);
                        handFY[w] = Integer.parseInt(tf[4]);
                    }
                }
            } catch (Exception ex) {
                // rămâne poziția aproximativă
            }
        }
        Img a = p.image("acid.png");
        acid = a.cut(0, 0, a.w, a.h);
        Img sk = p.image("sky.png");
        sky = sk.cut(0, 0, sk.w, sk.h);

        // decorul: „idx nume w h înălțime solid rază x y” (împachetat pe rânduri în decor.png)
        Img ds = p.image("decor.png");
        java.util.Vector lines = readLines(p, "decor.txt");
        int nd = lines.size();
        decor = new Img[nd];
        decorName = new String[nd];
        decorH = new float[nd];
        decorR = new float[nd];
        decorSolid = new int[nd];
        for (int i = 0; i < nd; i++) {
            String[] f = split((String) lines.elementAt(i));
            int idx = Integer.parseInt(f[0]);
            int w = Integer.parseInt(f[2]), h = Integer.parseInt(f[3]);
            decorName[idx] = f[1];
            decorH[idx] = Float.parseFloat(f[4]);
            decorSolid[idx] = Integer.parseInt(f[5]);
            decorR[idx] = Float.parseFloat(f[6]);
            decor[idx] = ds.cut(Integer.parseInt(f[7]), Integer.parseInt(f[8]), w, h);
        }
    }

    static java.util.Vector readLines(Platform p, String name) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(p.open(name), "UTF-8"));
        java.util.Vector lines = new java.util.Vector();
        String line;
        while ((line = r.readLine()) != null) {
            if (line.trim().length() > 0) {
                lines.addElement(line.trim());
            }
        }
        r.close();
        return lines;
    }

    /** Indicele texturii cu numele dat (0 dacă lipsește, ca jocul să nu se oprească). */
    public static int t(String name) {
        for (int i = 0; i < texName.length; i++) {
            if (name.equals(texName[i])) {
                return i;
            }
        }
        return 0;
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
