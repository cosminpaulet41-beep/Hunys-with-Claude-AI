package ro.hunys.tn2;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Font bitmap pixelat (fișier .pxf făcut de tools/FontGen din fonturi OFL). Literele se desenează
 * mărite de un număr întreg de ori, deci rămân clare.
 */
public final class PixelFont {
    public final int lineH, base;
    private final char[] chars;
    private final byte[] adv, xoff, wid;
    private final byte[][] bits;
    private final int[] index = new int[0x2200];

    public PixelFont(InputStream raw) throws IOException {
        DataInputStream in = new DataInputStream(raw);
        byte[] m = new byte[4];
        in.readFully(m);
        if (m[0] != 'P' || m[1] != 'X' || m[2] != 'F' || m[3] != '1') {
            throw new IOException("font");
        }
        lineH = in.readUnsignedByte();
        base = in.readUnsignedByte();
        int n = in.readUnsignedShort();
        chars = new char[n];
        adv = new byte[n];
        xoff = new byte[n];
        wid = new byte[n];
        bits = new byte[n][];
        for (int i = 0; i < index.length; i++) {
            index[i] = -1;
        }
        for (int i = 0; i < n; i++) {
            chars[i] = in.readChar();
            adv[i] = in.readByte();
            xoff[i] = in.readByte();
            wid[i] = in.readByte();
            int bpr = (wid[i] + 7) / 8;
            bits[i] = new byte[bpr * lineH];
            in.readFully(bits[i]);
            if (chars[i] < index.length) {
                index[chars[i]] = i;
            }
        }
        in.close();
        alias('ş', 'ș');
        alias('ţ', 'ț');
        alias('Ş', 'Ș');
        alias('Ţ', 'Ț');
    }

    private void alias(char from, char to) {
        if (index[from] < 0 && index[to] >= 0) {
            index[from] = index[to];
        }
    }

    private int glyph(char c) {
        int g = c < index.length ? index[c] : -1;
        if (g < 0) {
            g = index['?'];
        }
        return g;
    }

    /** Lățimea textului în pixeli de font (fără mărire). */
    public int width(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            int g = glyph(s.charAt(i));
            if (g >= 0) {
                w += adv[g];
            }
        }
        return w;
    }

    /** Desenează textul cu colțul stânga-sus în (x, y), mărit de `scale` ori. */
    public int draw(Gfx g, String s, int x, int y, int color, int scale) {
        int cx = x;
        for (int i = 0; i < s.length(); i++) {
            int gi = glyph(s.charAt(i));
            if (gi < 0) {
                continue;
            }
            int w = wid[gi];
            int bpr = (w + 7) / 8;
            byte[] b = bits[gi];
            int gx = cx + xoff[gi] * scale;
            for (int row = 0; row < lineH; row++) {
                int o = row * bpr;
                int run = -1;
                for (int col = 0; col <= w; col++) {
                    boolean on = col < w && (b[o + (col >> 3)] & (0x80 >> (col & 7))) != 0;
                    if (on && run < 0) {
                        run = col;
                    } else if (!on && run >= 0) {
                        g.fill(gx + run * scale, y + row * scale, (col - run) * scale, scale, color);
                        run = -1;
                    }
                }
            }
            cx += adv[gi] * scale;
        }
        return cx;
    }

    /** Desenează cu umbră (un pixel mărit, jos-dreapta). */
    public int drawShadow(Gfx g, String s, int x, int y, int color, int scale) {
        draw(g, s, x + scale, y + scale, 0xC0000000, scale);
        return draw(g, s, x, y, color, scale);
    }

    /** Împarte textul în rânduri care încap în lățimea dată (în pixeli de ecran, la mărirea dată). */
    public String[] wrap(String text, int maxW, int scale) {
        java.util.Vector out = new java.util.Vector();
        int start = 0;
        while (start <= text.length()) {
            int nl = text.indexOf('\n', start);
            String para = nl < 0 ? text.substring(start) : text.substring(start, nl);
            wrapPara(para, maxW, scale, out);
            if (nl < 0) {
                break;
            }
            start = nl + 1;
        }
        String[] r = new String[out.size()];
        out.copyInto(r);
        return r;
    }

    private void wrapPara(String para, int maxW, int scale, java.util.Vector out) {
        if (para.length() == 0) {
            out.addElement("");
            return;
        }
        String line = "";
        int i = 0;
        while (i < para.length()) {
            int sp = para.indexOf(' ', i);
            String word = sp < 0 ? para.substring(i) : para.substring(i, sp);
            String cand = line.length() == 0 ? word : line + " " + word;
            if (width(cand) * scale <= maxW || line.length() == 0) {
                line = cand;
            } else {
                out.addElement(line);
                line = word;
            }
            if (sp < 0) {
                break;
            }
            i = sp + 1;
        }
        out.addElement(line);
    }
}
