import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * Transforma un font TTF pixelat intr-un font bitmap al jocului (.pxf), la marimea lui nativa
 * (un pixel din design = un pixel), fara antialiasing. Jocul il deseneaza apoi marit de un numar
 * intreg de ori, deci literele raman clare pe orice ecran.
 *
 * Utilizare: java FontGen font.ttf marime iesire.pxf [previzualizare.png]
 *
 * Format (DataOutputStream): "PXF1", inaltime rand (byte), linia de baza (byte), numar caractere (short),
 * apoi pentru fiecare: caracter (char), avans (byte), decalaj x (byte), latime (byte),
 * inaltime-rand randuri de biti (ceil(latime/8) octeti pe rand).
 */
public class FontGen {
    static final String CHARS;

    static {
        StringBuilder b = new StringBuilder();
        for (char c = 32; c < 127; c++) {
            b.append(c);
        }
        b.append("ăâîșțĂÂÎȘȚşţŞŢ…–—„”“’‘«»°·×éèáàüöäÉÜÖÄçñ€");
        CHARS = b.toString();
    }

    public static void main(String[] a) throws Exception {
        Font f = Font.createFont(Font.TRUETYPE_FONT, new File(a[0])).deriveFont(Float.parseFloat(a[1]));
        FontRenderContext frc = new FontRenderContext(null, false, true);
        LineMetrics lm = f.getLineMetrics("ÂgjȘ", frc);
        int asc = Math.round(lm.getAscent());
        int desc = Math.round(lm.getDescent());
        int lh = asc + desc;
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(a[2])));
        out.writeBytes("PXF1");
        out.writeByte(lh);
        out.writeByte(asc);
        java.util.List<Character> ok = new java.util.ArrayList<>();
        for (char c : CHARS.toCharArray()) {
            if (f.canDisplay(c)) {
                ok.add(c);
            } else {
                System.out.println("lipseste: " + c + " (U+" + Integer.toHexString(c) + ")");
            }
        }
        out.writeShort(ok.size());
        int pad = 8;
        int prevW = 0;
        BufferedImage prev = new BufferedImage(16 * 40, ((ok.size() + 15) / 16) * (lh + 4), BufferedImage.TYPE_INT_RGB);
        Graphics2D pg = prev.createGraphics();
        pg.setColor(new Color(30, 30, 40));
        pg.fillRect(0, 0, prev.getWidth(), prev.getHeight());
        int gi = 0;
        for (char c : ok) {
            GlyphVector gv = f.createGlyphVector(frc, String.valueOf(c));
            float adv = gv.getGlyphMetrics(0).getAdvance();
            int advance = Math.round(adv);
            int iw = advance + pad * 2 + 8;
            BufferedImage img = new BufferedImage(iw, lh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setColor(Color.WHITE);
            g.setFont(f);
            g.drawString(String.valueOf(c), pad, asc);
            int minX = iw, maxX = -1;
            for (int y = 0; y < lh; y++) {
                for (int x = 0; x < iw; x++) {
                    if ((img.getRGB(x, y) & 0xFF) > 127) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                    }
                }
            }
            int w = maxX < 0 ? 0 : maxX - minX + 1;
            int xoff = maxX < 0 ? 0 : minX - pad;
            out.writeChar(c);
            out.writeByte(advance);
            out.writeByte(xoff);
            out.writeByte(w);
            int bpr = (w + 7) / 8;
            for (int y = 0; y < lh; y++) {
                for (int b = 0; b < bpr; b++) {
                    int v = 0;
                    for (int k = 0; k < 8; k++) {
                        int x = minX + b * 8 + k;
                        if (b * 8 + k < w && (img.getRGB(x, y) & 0xFF) > 127) {
                            v |= 0x80 >> k;
                        }
                    }
                    out.writeByte(v);
                }
            }
            int px = (gi % 16) * 40 + 4, py = (gi / 16) * (lh + 4) + 2;
            pg.setColor(new Color(60, 60, 80));
            pg.drawRect(px - 1, py - 1, advance + 1, lh + 1);
            for (int y = 0; y < lh; y++) {
                for (int x = 0; x < w; x++) {
                    if ((img.getRGB(minX + x, y) & 0xFF) > 127) {
                        prev.setRGB(px + xoff + x, py + y, 0xFFFFFF);
                    }
                }
            }
            gi++;
        }
        out.close();
        System.out.println(a[2] + ": " + ok.size() + " caractere, rand " + lh + " px, baza " + asc);
        if (a.length > 3) {
            BufferedImage big = new BufferedImage(prev.getWidth() * 3, prev.getHeight() * 3, BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = big.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            bg.drawImage(prev, 0, 0, big.getWidth(), big.getHeight(), null);
            ImageIO.write(big, "png", new File(a[3]));
        }
    }
}
