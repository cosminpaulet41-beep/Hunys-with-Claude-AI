package ro.hunys.tn2;

import java.io.File;
import java.util.Random;

/**
 * Test de fum: joacă la întâmplare câteva minute de joc (mers, rotire, foc, folosire) și
 * verifică salvarea/încărcarea. Orice excepție oprește testul cu eroare.
 */
public final class Smoke {
    public static void main(String[] a) throws Exception {
        DesktopPlatform p = new DesktopPlatform(new File(a[0]));
        Game g = new Game(p);
        g.init(2412, 1084);
        Random r = new Random(7);
        int fails = 0;
        for (int diff = 0; diff < 3; diff++) {
            g.difficulty = diff;
            g.startEpisode(diff, null);
            for (int k = 0; k < 10; k++) {
                g.tapScreen();
            }
            check(g.state == Game.ST_PLAY || g.state == Game.ST_RADIO, "start " + g.state);
            float fwd = 0, side = 0;
            int frames = 0, scenes = 0, notes = 0, radios = 0;
            for (int f = 0; f < 60 * 60 * 3; f++) {
                if (g.state != Game.ST_PLAY) {
                    if (g.state == Game.ST_SCENE) {
                        scenes++;
                    } else if (g.state == Game.ST_NOTE) {
                        notes++;
                    } else if (g.state == Game.ST_RADIO) {
                        radios++;
                    }
                    if (g.state == Game.ST_DEAD) {
                        g.menuAction(Game.A_RETRY);
                    } else if (g.state == Game.ST_STORY) {
                        g.tapScreen();
                    } else if (g.state == Game.ST_STATS) {
                        g.menuAction(Game.A_NEXT);
                    } else {
                        g.tapScreen();
                    }
                }
                if (f % 90 == 0) {
                    fwd = r.nextFloat() * 2 - 0.6f;
                    side = r.nextFloat() * 2 - 1;
                }
                g.joyOn = true;
                g.joyX = side;
                g.joyY = -fwd;
                g.pendingTurn += (r.nextFloat() - 0.5f) * 0.08f;
                g.fireHeld = r.nextInt(30) == 0;
                if (r.nextInt(40) == 0) {
                    g.world.fire();
                }
                if (r.nextInt(200) == 0) {
                    g.world.useAhead(true);
                }
                if (r.nextInt(300) == 0) {
                    g.world.switchWeapon();
                }
                if (r.nextInt(300) == 0) {
                    g.world.reload();
                }
                g.update(1f / 60f);
                if (f % 6 == 0) {
                    g.render();
                }
                frames++;
                if (f == 5000) {
                    check(g.saveGame(), "save");
                    float x = g.world.px, y = g.world.py;
                    int h = g.world.health;
                    check(g.loadGame(), "load");
                    check(Math.abs(g.world.px - x) < 1e-4 && Math.abs(g.world.py - y) < 1e-4 && g.world.health == h, "load state");
                }
                if (g.world.px < 0 || g.world.py < 0 || g.world.px >= g.world.mw || g.world.py >= g.world.mh) {
                    System.out.println("GRESIT: jucatorul a iesit din harta");
                    fails++;
                    break;
                }
                if (g.world.map[(int) g.world.py * g.world.mw + (int) g.world.px] != '.'
                        && !World.isDoor(g.world.map[(int) g.world.py * g.world.mw + (int) g.world.px])) {
                    System.out.println("GRESIT: jucatorul e in perete la " + g.world.px + "," + g.world.py);
                    fails++;
                    break;
                }
            }
            System.out.println("dificultate " + diff + ": " + frames + " cadre, ucisi " + g.world.kills + ", scene " + scenes
                    + ", notite " + notes + ", statie " + radios + ", viata " + g.world.health + ", sunete " + p.sounds.size());
        }
        // limba engleză: toate textele există
        for (int lang = 0; lang < 2; lang++) {
            Texts.set(lang == 1);
            check(Texts.STORY.length == 3 && Texts.WIN.length == 3 && Texts.OBJ.length == 3, "texte episoade");
            for (int e = 0; e < 3; e++) {
                check(Texts.NOTES[e].length == Texts.NOTE_TITLES[e].length, "notite " + e);
                check(Texts.OBJ[e].length == 4, "obiective " + e);
            }
        }
        Texts.set(false);
        System.out.println(fails == 0 ? "SMOKE OK" : "SMOKE GRESIT");
        if (fails > 0) {
            System.exit(1);
        }
    }

    static void check(boolean ok, String what) {
        if (!ok) {
            System.out.println("GRESIT: " + what);
            System.exit(1);
        }
    }
}
