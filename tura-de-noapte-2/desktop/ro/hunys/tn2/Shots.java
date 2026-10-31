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

        // ---------------------------------------------------------- episodul 1: parterul
        // 3. rampa ambulanțelor, la început (fără curent)
        start();
        place(3.0f, 4.8f, 350);
        g.world.radioN = 0;
        tick(0.1f);
        shot("03_rampa_start", only);

        // 4. urgențele: perdele, paturi, infectați
        place(14.0f, 9.5f, 330);
        moveZ(Art.Z_PATIENT, 17.5f, 6.0f, true);
        moveZ(Art.Z_NURSE, 19.5f, 7.0f, true);
        shot("04_urgente", only);

        // 5. coridorul: foc spre infectați, o butelie care explodează
        place(30.0f, 12.6f, 180);
        moveZ(Art.Z_PATIENT, 22.0f, 12.4f, true);
        moveZ(Art.Z_VECTRA, 24.0f, 13.4f, true);
        g.world.fire();
        g.world.flashT = 0.07f;
        press(Game.B_FIRE);
        joy(0.2f, -0.8f);
        shot("05_coridor_foc", only);
        release();
        g.world.flashT = 0f;

        // 6. holul mare, cu curent: inscripția spitalului
        power(true);
        place(16.0f, 26.0f, 330);
        shot("06_hol", only);

        // 7. radiologia: dr. Elena în camera de comandă
        power(false);
        place(36.7f, 4.6f, 300);
        shot("07_radiologie", only);

        // 8. capela cu lumânări
        moveZ(Art.Z_NURSE, 44.0f, 3.5f, false);
        place(41.7f, 10.0f, 285);
        shot("08_capela", only);

        // 9. farmacia
        moveZ(Art.Z_PATIENT, 36.0f, 23.0f, false);
        place(31.0f, 30.8f, 315);
        shot("09_farmacie", only);

        // 10. butelia explodează
        place(9.5f, 12.6f, 0);
        for (int i = 0; i < g.world.nz; i++) {
            if (g.world.zty[i] == World.T_O2 && Math.abs(g.world.zx[i] - 12.0f) < 0.5f) {
                g.world.explode(i);
                g.world.zt[i] = 0.18f;
            }
        }
        shot("10_explozie", only);

        // 11. harta parterului
        for (int i = 0; i < g.world.seen.length; i++) {
            g.world.seen[i] = true;
        }
        place(20.0f, 12.6f, 0);
        g.state = Game.ST_MAP;
        shot("11_harta_parter", only);

        // 12. o notiță
        g.noteTitle = Texts.NOTE_TITLES[0][2];
        g.noteText = Texts.NOTES[0][2];
        g.state = Game.ST_NOTE;
        place(25.0f, 8.0f, 0);
        shot("12_notita", only);

        // ---------------------------------------------------------- episodul 2: etajul
        g.startEpisode(1, null);
        start();
        // 13. ATI (circuitul de urgență), cu pușca
        place(14.5f, 9.5f, 270);
        g.world.weapon = 1;
        shot("13_ati", only);
        g.world.fire();
        g.world.flashT = 0.1f;
        g.world.weapon = 1;
        shot("13b_ati_pusca_foc", only);
        g.world.flashT = 0f;
        g.world.shootCd = 0f;

        // 14. sala de operație 2
        place(39.0f, 6.5f, 270);
        shot("14_sala_operatie", only);

        // 15. pediatria
        place(14.0f, 28.0f, 250);
        shot("15_pediatrie", only);

        // 16. laboratorul Vectra și Subiectul 9, cu pistolul-mitralieră
        g.world.weapon = 2;
        place(38.5f, 22.5f, 0);
        moveZ(World.T_BOSS9, 44.0f, 21.0f, true);
        shot("16_subiectul9", only);

        // ---------------------------------------------------------- episodul 3: acoperișul
        g.startEpisode(2, null);
        start();
        // 17. ieșirea pe acoperiș: cerul din zori, heliportul
        place(19.0f, 17.0f, 5);
        shot("17_acoperis", only);

        // 18. elicopterul de aproape
        place(26.5f, 20.5f, 325);
        shot("18_elicopter", only);

        // 19. creatura
        for (int i = 0; i < g.world.nz; i++) {
            if (g.world.zty[i] == World.T_CREATURE) {
                g.world.zst[i] = World.Z_CHASE;
            }
        }
        moveZ(World.T_CREATURE, 25.5f, 12.5f, true);
        place(27.5f, 18.0f, 250);
        shot("19_creatura", only);

        // 20. camera mașinilor liftului
        place(8.5f, 13.5f, 230);
        shot("20_camera_masini", only);

        // 21. setări (luminozitatea)
        g.state = Game.ST_SETTINGS;
        g.prevState = Game.ST_TITLE;
        g.titleView();
        shot("21_setari", only);

        // 22. luminozitatea: foarte întunecat / normal / foarte luminos
        if (only == null || only.startsWith("22")) {
            g.startEpisode(0, null);
            start();
            place(14.0f, 9.5f, 330);
            for (int b = 0; b <= 4; b += 2) {
                g.world.setBrightness(b);
                shot("22_luminozitate_" + b, null);
            }
            g.world.setBrightness(2);
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
