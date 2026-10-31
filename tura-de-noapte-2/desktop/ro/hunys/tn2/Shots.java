package ro.hunys.tn2;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Capturi de ecran exact ca pe telefon: lumea și interfața sunt compuse la rezoluția ecranului
 * (implicit 2412x1084, Huawei nova 13), mărite fără netezire, ca pe Android.
 *
 * Utilizare: java ro.hunys.tn2.Shots assets director_iesire [latime inaltime]
 */
public final class Shots {
    static Game g;
    static DesktopPlatform p;
    static File out;

    public static void main(String[] a) throws Exception {
        p = new DesktopPlatform(new File(a[0]));
        out = new File(a[1]);
        out.mkdirs();
        int w = a.length > 3 ? Integer.parseInt(a[2]) : 2412;
        int h = a.length > 3 ? Integer.parseInt(a[3]) : 1084;
        g = new Game(p);
        g.init(w, h);
        String only = a.length > 4 ? a[4] : null;
        long t0 = System.nanoTime();

        // 1. meniul principal
        g.state = Game.ST_TITLE;
        tick(0.5f);
        shot("01_meniu", only);

        // 2. povestea
        g.newGame();
        shot("02_poveste", only);

        // 3. holul, la început (fără curent: lumini de avarie, zorii prin geam)
        start();
        place(22.5f, 27.0f, 270);
        g.world.radioN = 0;
        tick(0.1f);
        shot("03_hol_start", only);

        // 4. holul spre geamuri și intrare
        place(23.0f, 20.6f, 95);
        shot("04_hol_geamuri", only);

        // 5. coridorul: infectați care vin, foc, joystick apăsat
        place(25.0f, 14.6f, 180);
        moveZ(Art.Z_REPORTER, 18.5f, 14.5f, true);
        moveZ(Art.Z_CAMERA, 20.5f, 15.4f, true);
        g.world.fire();
        g.world.flashT = 0.07f;
        press(Game.B_FIRE);
        joy(0.2f, -0.8f);
        shot("05_coridor_foc", only);
        release();
        g.world.flashT = 0f;

        // 6. studioul 1 fără curent (lanterna)
        place(26.0f, 12.0f, 270);
        moveZ(Art.Z_ANCHOR, 25.0f, 6.5f, true);
        moveZ(Art.Z_CAMERA, 28.0f, 5.5f, true);
        shot("06_studio1_intuneric", only);

        // 7. studioul 1 cu curent
        power(true);
        place(25.2f, 12.4f, 272);
        shot("07_studio1_lumini", only);
        power(false);

        // 8. cantina
        place(8.6f, 23.0f, 215);
        shot("08_cantina", only);

        // 9. regia studioului 1
        place(13.5f, 12.2f, 320);
        shot("09_regie", only);

        // 10. arhiva cu lanterna
        place(1.6f, 12.2f, 300);
        moveZ(Art.Z_VECTRA, 1.5f, 10.4f, false);
        shot("10_arhiva", only);

        // 11. machiaj
        place(38.0f, 12.4f, 265);
        moveZ(Art.Z_ANCHOR, 37.5f, 10.5f, true);
        shot("11_machiaj", only);

        // 12. paza, cu Ioana la monitoare
        place(31.6f, 22.3f, 320);
        shot("12_paza_ioana", only);

        // 13. harta
        for (int i = 0; i < g.world.seen.length; i++) {
            g.world.seen[i] = true;
        }
        place(26.0f, 12.0f, 270);
        g.state = Game.ST_MAP;
        shot("13_harta", only);

        // 14. o notiță
        g.noteTitle = Texts.NOTE_TITLES[1];
        g.noteText = Texts.NOTES[1];
        g.state = Game.ST_NOTE;
        place(32.0f, 21.0f, 300);
        shot("14_notita", only);

        // 15. stația radio
        g.state = Game.ST_RADIO;
        g.radioId = 1;
        place(36.0f, 14.6f, 0);
        shot("15_statie", only);

        // 16. pauză
        g.state = Game.ST_PAUSE;
        shot("16_pauza", only);

        // 17. setări
        g.state = Game.ST_SETTINGS;
        g.prevState = Game.ST_TITLE;
        g.world.reset();
        g.titleView();
        shot("17_setari", only);

        // 18. pixelarea: aceeași imagine la cele trei niveluri
        if (only == null || "18".startsWith(only) || only.startsWith("18")) {
            start();
            power(true);
            place(26.0f, 12.0f, 270);
            moveZ(Art.Z_ANCHOR, 25.0f, 6.5f, true);
            moveZ(Art.Z_CAMERA, 28.0f, 5.5f, true);
            for (int px = 0; px < 3; px++) {
                g.pixel = px;
                g.resize(g.sw, g.sh);
                shot("18_pixeli_" + px, null);
            }
            g.pixel = 1;
            g.resize(g.sw, g.sh);
        }

        System.out.printf("gata in %.1f s%n", (System.nanoTime() - t0) / 1e9);
    }

    static void start() {
        g.state = Game.ST_PLAY;
        g.world.reset();
        g.objT = 0f;
        g.lastObjective = 0;
    }

    static void place(float x, float y, float deg) {
        g.world.px = x;
        g.world.py = y;
        g.world.setAngle((float) Math.toRadians(deg));
    }

    static void power(boolean on) {
        g.world.power = on;
        g.world.applyPower();
    }

    static void moveZ(int type, float x, float y, boolean chase) {
        World w = g.world;
        int best = -1;
        float bd = 1e9f;
        for (int i = 0; i < w.nz; i++) {
            if (w.zty[i] != type || w.zdecor[i]) {
                continue;
            }
            float dx = w.zx[i] - x, dy = w.zy[i] - y;
            if (dx * dx + dy * dy < bd) {
                bd = dx * dx + dy * dy;
                best = i;
            }
        }
        if (best >= 0) {
            w.zx[best] = x;
            w.zy[best] = y;
            if (chase) {
                w.zst[best] = World.Z_CHASE;
                w.zt[best] = (float) Math.random();
            }
        }
    }

    static void press(int b) {
        g.bdown[b] = true;
    }

    static void joy(float x, float y) {
        g.joyOn = true;
        g.joyCX = g.joyBaseX;
        g.joyCY = g.joyBaseY;
        g.joyX = x;
        g.joyY = y;
    }

    static void release() {
        for (int i = 0; i < g.bdown.length; i++) {
            g.bdown[i] = false;
        }
        g.joyOn = false;
        g.joyX = 0f;
        g.joyY = 0f;
    }

    static void tick(float t) {
        int st = g.state;
        for (float k = 0; k < t; k += 0.05f) {
            g.update(0.05f);
        }
        g.state = st;
    }

    static void shot(String name, String only) throws Exception {
        if (only != null && !name.startsWith(only)) {
            return;
        }
        g.render();
        BufferedImage img = compose(g);
        ImageIO.write(img, "png", new File(out, name + ".png"));
        System.out.println(name);
    }

    static BufferedImage compose(Game g) {
        BufferedImage world = new BufferedImage(g.W, g.H, BufferedImage.TYPE_INT_RGB);
        world.setRGB(0, 0, g.W, g.H, g.worldBuf, 0, g.W);
        BufferedImage ui = new BufferedImage(g.UW, g.UH, BufferedImage.TYPE_INT_ARGB);
        ui.setRGB(0, 0, g.UW, g.UH, g.ui.px, 0, g.UW);
        BufferedImage img = new BufferedImage(g.sw, g.sh, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = img.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gr.drawImage(world, 0, 0, g.W * g.s, g.H * g.s, null);
        gr.drawImage(ui, 0, 0, g.UW * g.u, g.UH * g.u, null);
        VecAwt.draw(gr, g.vec);
        gr.dispose();
        return img;
    }
}
