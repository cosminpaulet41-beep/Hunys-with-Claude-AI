package ro.hunys.tn2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Lumea jocului: harta etajului, jucătorul, infectații, bossii, buteliile, obiectele, ușile, lumina și
 * randarea 3D (raycasting, ca în jocurile 3D vechi). Logica vine din Tura de Noapte (E72); randarea e nouă:
 * texturi 64x64, lumini colorate calculate din hartă, lanternă, ceață, cerul din zori pe acoperiș și
 * rezoluție internă la alegere. Cele trei episoade (parterul, etajul, acoperișul) au fiecare harta lui.
 * Nu depinde de Android, ca să poată fi testată și pe calculator.
 */
public final class World {
    // ------------------------------------------------------------------ constante
    /** Tangenta jumătății unghiului vertical de vedere. */
    public static final float TV = 0.45f;
    static final float PR = 0.22f;
    static final int FLOW_MAX = 48;
    public static final int MAXZ = 96, MAXI = 96, MAXP = 10, MAXD = 192;

    // dificultate: ușor, normal, greu
    static final float[] D_DMG = {0.6f, 1f, 1.5f};
    static final float[] D_SPEED = {0.85f, 1f, 1.2f};
    static final int[] D_HP = {2, 3, 4};
    static final int[] D_AMMO = {10, 8, 8};
    static final int[] D_SHELLS = {6, 4, 4};
    static final int[] D_SMGAMMO = {40, 30, 30};
    static final int[] D_START = {24, 12, 8};
    static final int[] D_MED = {50, 40, 30};
    static final float[] D_SPITCD = {3.0f, 2.4f, 2.0f};
    static final int[] D_BOSS9 = {14, 20, 28};
    static final int[] D_CREATURE = {28, 38, 50};

    // arme: 0 pistol, 1 pușcă, 2 pistol-mitralieră
    public static final int[] W_MAG = {12, 5, 30};
    static final float[] W_CD = {0.3f, 0.9f, 0.08f};
    public static final float[] W_RELOAD = {1.1f, 2.0f, 1.6f};

    // efecte sonore (biți), aceeași ordine ca Sfx.FILES
    public static final int S_SHOT = 1, S_EMPTY = 2, S_RELOAD = 4, S_DOOR = 8, S_LOCKED = 16, S_CARD = 32;
    public static final int S_PICKUP = 64, S_GROAN = 128, S_GROAN2 = 256, S_HURT = 512, S_ZDIE = 1024;
    public static final int S_PAPER = 2048, S_HEART = 4096, S_SHOTGUN = 8192, S_PUMP = 16384;
    public static final int S_ROAR = 32768, S_GEN = 65536, S_SMG = 1 << 17, S_EXPLODE = 1 << 18;
    public static final int S_SPIT = 1 << 19, S_THUNDER = 1 << 20, S_ALARM = 1 << 21, S_RADIO = 1 << 22;

    public static final int IT_AMMO = 0, IT_MED = 1, IT_BLUE = 2, IT_RED = 3, IT_NOTE = 4;
    public static final int IT_SHOTGUN = 5, IT_SHELLS = 6, IT_YELLOW = 7, IT_GREEN = 8, IT_SMG = 9, IT_SMGAMMO = 10;
    /** Z_HIDDEN: încă nu a apărut (creatura de pe acoperiș). */
    static final int Z_IDLE = 0, Z_CHASE = 1, Z_DYING = 2, Z_DEAD = 3, Z_HIDDEN = 4;
    /** Tipuri care nu sunt rânduri din z.png. */
    public static final int T_BOSS9 = 20, T_CREATURE = 21, T_O2 = 22;
    public static final int SCENE_IOANA = 100, SCENE_ELENA = 101, SCENE_CRISTI = 102, SCENE_CREATURE = 103;
    public static final int SCENE_BOSS9 = 104;
    /** Mesajele radio ale Ioanei (Texts.RADIO[episod][id]; unele episoade nu le au pe toate). */
    public static final int R_START = 0, R_ROOM = 1, R_BLUE = 2, R_RED = 3, R_YELLOW = 4, R_GREEN = 5;
    public static final int R_SHOTGUN = 6, R_SMG = 7, R_POWER = 8, R_NPC = 9, R_BOSS_SEEN = 10, R_BOSS_DEAD = 11;
    public static final int R_ACID = 12, R_LIFT = 13, R_OUT = 14, R_O2 = 15;
    /** Episoadele: hărțile, în ordine. */
    public static final String[] LEVELS = {"parter", "etaj", "acoperis"};

    // ------------------------------------------------------------------ harta
    public final Platform plat;
    public Level lv;
    public int mw, mh;
    public char[] map, zone;
    public float[] door;
    boolean[] doorMoving;
    public boolean[] seen;
    byte[] floorT, ceilT;
    boolean[] lampCell;

    // ------------------------------------------------------------------ lumina
    /** Grila de lumină: puncte la fiecare jumătate de celulă, valori 8.8 (256 = 1.0). */
    int lw, lh;
    int[] lmR, lmG, lmB;
    int[] offR, offG, offB, onR, onG, onB;
    int nflick;
    int[] flx, fly, flI;
    float[] flT;
    int[][] flR, flG, flB;
    int[] flX0, flY0, flW, flH;
    byte[][] emis;
    /** Mod emisiv al texturii: 0 deloc, 1 mereu, 2 doar cu curent, 3 doar fără curent. */
    int[] emisMode;

    // ------------------------------------------------------------------ jucătorul și starea
    public float px, py, ang, dirX, dirY, planeX, planeY;
    public int health, kills, totalZ, shots, hurtCount;
    public int weapon;
    public boolean hasShotgun, hasSmg;
    public final int[] mag = new int[3], res = new int[3];
    public boolean keyBlue, keyRed, keyYellow, keyGreen;
    /** Episodul (0 parterul, 1 etajul, 2 acoperișul). */
    public int ep;
    public boolean power, metElena, foundIoana, metCristi, liftOn, bossDead;
    public boolean won, dead;
    public float walkPhase, shootCd, reloadT, flashT, hurtT, playTime, pumpT, boomT;
    public String msg;
    public float msgT;
    public int pendingNote = -1, pendingScene = -1;
    public int sceneSeen;
    public long radioSeen;
    final int[] radioQ = new int[8];
    int radioN;
    public int difficulty = 1;
    public int sfx;
    float heartT;
    public boolean flashlight = true;
    /** Obiectivul curent (indice în Texts.OBJ). */
    public int objective;

    // infectați și personaje
    public int nz;
    final float[] zx = new float[MAXZ], zy = new float[MAXZ], zt = new float[MAXZ];
    final float[] zhit = new float[MAXZ], zatk = new float[MAXZ], zlos = new float[MAXZ];
    final float[] zsp = new float[MAXZ], zlx = new float[MAXZ], zly = new float[MAXZ];
    final float[] zspit = new float[MAXZ], zanim = new float[MAXZ];
    final int[] zhp = new int[MAXZ], zst = new int[MAXZ], zty = new int[MAXZ], zcarry = new int[MAXZ];
    final boolean[] zhas = new boolean[MAXZ], zsee = new boolean[MAXZ], zdecor = new boolean[MAXZ];
    final float[] zgr = new float[MAXZ], zcharge = new float[MAXZ], zchargeCd = new float[MAXZ];

    // obiecte
    public int ni;
    final float[] ix = new float[MAXI], iy = new float[MAXI];
    final int[] it = new int[MAXI], inote = new int[MAXI];
    final boolean[] ion = new boolean[MAXI];

    // decor
    public int nd;
    final float[] dxp = new float[MAXD], dyp = new float[MAXD], drad = new float[MAXD];
    final int[] dty = new int[MAXD];

    // proiectile (acid)
    final float[] prx = new float[MAXP], pry = new float[MAXP], pvx = new float[MAXP], pvy = new float[MAXP];
    final float[] plife = new float[MAXP];
    final boolean[] pon = new boolean[MAXP];

    int[] flow;
    private int[] flowQ;
    float flowT;
    final Random rnd = new Random();

    // ------------------------------------------------------------------ randare
    public int W, H;
    float proj, plane;
    float[] zbuf;
    int[] cone;
    private final int[] sOrder = new int[MAXZ + MAXI + MAXP + MAXD];
    private final float[] sDist = new float[MAXZ + MAXI + MAXP + MAXD];
    int curFlR, curFlG, curFlB, curMuzzle;
    /** Armele HD micșorate pentru rezoluția curentă a lumii (alfa înmulțit) și cât au fost micșorate. */
    final Img[] gunHd = new Img[3];
    final float[] gunK = new float[3];
    /** Cât din înălțimea ecranului ocupă mâna cu pistolul. */
    public static float GUN_H = 0.40f;
    /** Coloana din panorama cerului pentru fiecare coloană de pe ecran (calculată la fiecare cadru). */
    int[] skyCol, skyRow;
    /** Celulele de afară (acoperișul): tavanul e cerul. */
    boolean[] outdoor;
    boolean anyOutdoor;
    /** Colțul heliportului (pentru bucățile texturii mari). */
    int heliX = -1, heliY = -1;
    /** Ceața: cât de repede se întunecă lumea cu distanța (afară mai puțin). */
    float fogK = 0.010f;
    /** Luminozitatea din setări (0 foarte întunecat ... 4 foarte luminos, 2 = normal) și tabelul ei. */
    int bright = 2;
    final int[] gammaLut = new int[256];

    public World(Platform p) {
        plat = p;
    }

    // ================================================================== pornire

    public void load(Level level) {
        lv = level;
        mw = lv.w;
        mh = lv.h;
        int cells = mw * mh;
        map = new char[cells];
        zone = new char[cells];
        door = new float[cells];
        doorMoving = new boolean[cells];
        seen = new boolean[cells];
        floorT = new byte[cells];
        ceilT = new byte[cells];
        lampCell = new boolean[cells];
        flow = new int[cells];
        flowQ = new int[cells];
        outdoor = new boolean[cells];
        wallC = null;
        ep = 0;
        for (int k = 0; k < LEVELS.length; k++) {
            if (LEVELS[k].equals(lv.name)) {
                ep = k;
            }
        }
        anyOutdoor = false;
        heliX = -1;
        heliY = -1;
        for (int i = 0; i < cells; i++) {
            char z = lv.zones[i];
            outdoor[i] = z == 'e' || z == 'h';
            anyOutdoor |= outdoor[i];
            if (z == 'h' && heliX < 0) {
                heliX = i % mw;
                heliY = i / mw;
            }
        }
        buildEmissive();
        reset();
        buildLights();
    }

    /** Echipamentul cu care începe un episod pornit direct din meniul de episoade. */
    public void startGear() {
        if (ep >= 1) {
            hasShotgun = true;
            mag[1] = W_MAG[1];
            res[1] = 8;
            res[0] = Math.max(res[0], 24);
        }
        if (ep >= 2) {
            hasSmg = true;
            mag[2] = W_MAG[2];
            res[2] = 60;
        }
    }

    /** Ce trece dintr-un episod în altul: viața, armele și muniția. */
    public int[] carry() {
        return new int[] {health, weapon, hasShotgun ? 1 : 0, hasSmg ? 1 : 0, mag[0], res[0], mag[1], res[1], mag[2], res[2]};
    }

    public void applyCarry(int[] c) {
        health = Math.max(c[0], 70);
        hasShotgun = c[2] != 0;
        hasSmg = c[3] != 0;
        for (int w = 0; w < 3; w++) {
            mag[w] = c[4 + w * 2];
            res[w] = c[5 + w * 2];
        }
        weapon = owned(c[1]) ? c[1] : 0;
    }

    /** Pornește etajul de la început, cu echipamentul de start. */
    public void reset() {
        int cells = mw * mh;
        for (int i = 0; i < cells; i++) {
            map[i] = lv.struct[i];
            zone[i] = lv.zones[i];
            door[i] = 0f;
            doorMoving[i] = false;
            seen[i] = false;
            int ft = floorTex(zone[i]);
            if (zone[i] == 'h' && heliX >= 0) {
                int hx = (i % mw - heliX) % 5, hy = (i / mw - heliY) % 5;
                ft = Art.t("f_heli" + (hy * 5 + hx));
            }
            floorT[i] = (byte) ft;
            char l = lv.lights[i];
            int ct = ceilTex(zone[i], l);
            ceilT[i] = (byte) ct;
        }
        nz = 0;
        ni = 0;
        nd = 0;
        for (int k = 0; k < MAXP; k++) {
            pon[k] = false;
        }
        for (int k = 0; k < lv.tcode.length; k++) {
            String c = lv.tcode[k];
            float x = lv.tx[k], y = lv.ty[k];
            char c0 = c.charAt(0);
            if (c0 == 'P') {
                px = x;
                py = y;
                setAngle(lv.tang[k] < 0 ? 0f : (float) Math.toRadians(lv.tang[k]));
            } else if (c0 == 'z' && c.length() > 1) {
                addZombie(x, y, ztype(c.charAt(1)));
                // „za>2”: infectatul are asupra lui un obiect, care cade când moare
                int gt = c.indexOf('>');
                if (gt > 0 && gt + 1 < c.length()) {
                    int t = "ah12nse34up".indexOf(c.charAt(gt + 1));
                    if (t >= 0 && t != IT_NOTE) {
                        addItem(x, y, t, 0);
                        ion[ni - 1] = false;
                        zcarry[nz - 1] = ni - 1;
                    }
                }
            } else if (c0 == 'd' && c.length() > 1) {
                addZombie(x, y, ztype(c.charAt(1)));
                zst[nz - 1] = Z_DEAD;
                zdecor[nz - 1] = true;
            } else if (c0 == 'I') {
                addZombie(x, y, Art.Z_IOANA);
            } else if (c0 == 'E') {
                addZombie(x, y, Art.Z_ELENA);
            } else if (c0 == 'C') {
                addZombie(x, y, Art.Z_CRISTI);
            } else if (c0 == 'B') {
                addZombie(x, y, T_BOSS9);
            } else if (c0 == 'X') {
                addZombie(x, y, T_CREATURE);
                zst[nz - 1] = Z_HIDDEN;
            } else if (c0 == 'O') {
                addZombie(x, y, T_O2);
            } else if (c0 == 'n') {
                addItem(x, y, IT_NOTE, Integer.parseInt(c.substring(1)));
            } else {
                int t = "ah12nse34up".indexOf(c0);
                if (t >= 0 && t != IT_NOTE) {
                    addItem(x, y, t, 0);
                }
            }
        }
        for (int k = 0; k < lv.dname.length && nd < MAXD; k++) {
            int t = Art.decorIndex(lv.dname[k]);
            if (t < 0) {
                continue;
            }
            dxp[nd] = lv.dx[k];
            dyp[nd] = lv.dy[k];
            dty[nd] = t;
            float wd = Art.decorH[t] * Art.decor[t].w / Art.decor[t].h;
            drad[nd] = Art.decorSolid[t] != 1 ? 0f : (Art.decorR[t] > 0f ? Art.decorR[t] : Math.min(0.42f, wd * 0.42f));
            nd++;
        }
        totalZ = 0;
        for (int i = 0; i < nz; i++) {
            if (counts(i)) {
                totalZ++;
            }
        }
        health = 100;
        weapon = 0;
        hasShotgun = false;
        hasSmg = false;
        mag[0] = W_MAG[0];
        res[0] = D_START[difficulty];
        mag[1] = 0;
        res[1] = 0;
        mag[2] = 0;
        res[2] = 0;
        kills = 0;
        shots = 0;
        hurtCount = 0;
        keyBlue = keyRed = keyYellow = keyGreen = false;
        power = false;
        metElena = false;
        foundIoana = false;
        metCristi = false;
        liftOn = false;
        bossDead = false;
        boomT = 0f;
        won = false;
        dead = false;
        walkPhase = 0f;
        shootCd = 0f;
        reloadT = 0f;
        flashT = 0f;
        hurtT = 0f;
        pumpT = 0f;
        playTime = 0f;
        msg = null;
        msgT = 0f;
        pendingNote = -1;
        pendingScene = -1;
        sceneSeen = 0;
        radioSeen = 0;
        radioN = 0;
        sfx = 0;
        heartT = 0f;
        flowT = 0f;
        objective = 0;
        if (offR != null) {
            applyPower();
        }
    }

    static int ztype(char c) {
        switch (c) {
            case 'g': return Art.Z_GUARD;
            case 'v': return Art.Z_VECTRA;
            case 'p': return Art.Z_PATIENT;
            case 'a': return Art.Z_NURSE;
            case 'm': return Art.Z_MEDIC;
            case 'c': return Art.Z_SURGEON;
            case 's': return Art.Z_SPIT;
            default: return Art.Z_PATIENT;
        }
    }

    /** Se numără la statistici (infectații și bossii, nu oamenii, cadavrele sau buteliile). */
    boolean counts(int i) {
        return !zdecor[i] && !npc(i) && zty[i] != T_O2;
    }

    boolean npc(int i) {
        int t = zty[i];
        return t == Art.Z_IOANA || t == Art.Z_ELENA || t == Art.Z_CRISTI;
    }

    boolean isBoss(int i) {
        return zty[i] == T_BOSS9 || zty[i] == T_CREATURE;
    }

    void addZombie(float x, float y, int type) {
        if (nz >= MAXZ) {
            return;
        }
        int i = nz++;
        zx[i] = x;
        zy[i] = y;
        zt[i] = rnd.nextFloat();
        zhit[i] = 0f;
        zatk[i] = 0.6f;
        zlos[i] = rnd.nextFloat() * 0.3f;
        zsp[i] = (0.55f + rnd.nextFloat() * 0.25f) * D_SPEED[difficulty];
        zhp[i] = D_HP[difficulty];
        if (type == Art.Z_SPIT) {
            zsp[i] *= 0.8f;
            zhp[i] = D_HP[difficulty] + 1;
        } else if (type == T_BOSS9) {
            zsp[i] = 0.85f * D_SPEED[difficulty];
            zhp[i] = D_BOSS9[difficulty];
        } else if (type == T_CREATURE) {
            zsp[i] = 1.0f * D_SPEED[difficulty];
            zhp[i] = D_CREATURE[difficulty];
        } else if (type == T_O2) {
            zsp[i] = 0f;
            zhp[i] = 1;
        }
        zcharge[i] = 0f;
        zchargeCd[i] = 3f;
        zty[i] = type;
        zcarry[i] = -1;
        zdecor[i] = false;
        zgr[i] = 2f + rnd.nextFloat() * 3f;
        zspit[i] = 1.2f + rnd.nextFloat();
        zanim[i] = 0f;
        zst[i] = Z_IDLE;
        zhas[i] = false;
        zsee[i] = false;
    }

    void addItem(float x, float y, int type, int note) {
        if (ni >= MAXI) {
            return;
        }
        int i = ni++;
        ix[i] = x;
        iy[i] = y;
        it[i] = type;
        inote[i] = note;
        ion[i] = true;
    }

    public void setAngle(float a) {
        ang = a;
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);
        dirX = c;
        dirY = s;
        float pl = plane > 0f ? plane : 1f;
        planeX = -s * pl;
        planeY = c * pl;
    }

    /** Rezoluția internă a lumii (pixeli mari). */
    public void setView(int w, int h) {
        W = w;
        H = h;
        proj = h / (2f * TV);
        plane = TV * w / h;
        setAngle(ang);
        zbuf = new float[w];
        cone = new int[w * h];
        skyCol = new int[w];
        skyRow = new int[h];
        if (Art.sky != null) {
            // rândul din panoramă pentru fiecare rând de pe ecran (tangenta elevației: +0.5 sus ... -0.3 jos)
            int half = h >> 1;
            for (int y = 0; y < h; y++) {
                float t = (half - y - 0.5f) / proj;
                int v = (int) ((0.5f - t) / 0.8f * Art.sky.h);
                skyRow[y] = Math.max(0, Math.min(Art.sky.h - 1, v));
            }
        }
        for (int wp = 0; wp < 3; wp++) {
            Img src = Art.hand[wp];
            if (src == null) {
                gunHd[wp] = null;
                continue;
            }
            float k = Art.handFH[wp] > 0 ? (float) h / Art.handFH[wp] : h * GUN_H / src.h;
            gunK[wp] = k;
            gunHd[wp] = Art.downscale(src, Math.max(1, Math.round(src.w * k)), Math.max(1, Math.round(src.h * k)));
        }
        float cx = w * 0.5f, cy = h * 0.54f;
        float rx = w * 0.30f, ry = h * 0.62f;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float ex = (x + 0.5f - cx) / rx, ey = (y + 0.5f - cy) / ry;
                float e = (float) Math.sqrt(ex * ex + ey * ey);
                float v = 1f - e;
                if (v < 0f) {
                    v = 0f;
                }
                v = v * v * (3f - 2f * v);
                cone[y * w + x] = (int) ((0.10f + 0.90f * v) * 256f);
            }
        }
    }

    // ================================================================== texturi după zonă

    /** Indicii texturilor, căutați o singură dată după nume. */
    static int[] TX;
    static int TX_CORR, F_HELI0, TX_WIN2, TX_PH2, TX_MON1, TX_CONC;
    static final String SIGNS = "567890!@%&$";
    static final String[] SIGN_TEX = {"sign_urg", "sign_rad", "sign_farm", "sign_ati", "sign_bloc", "sign_ped",
        "sign_lab", "sign_scara", "sign_heli", "sign_int", "sign_cafe"};
    static final String[] FIXED = {
        "G", "window", "K", "tiles", "J", "tiles_blue", "T", "wc", "C", "concrete", "E", "panel", "L", "liftpanel",
        "H", "elevator", "O", "garage", "A", "pharmacy", "X", "xray", "V", "monitors0", "I", "icu", "U", "curtain",
        "B", "lab1", "Q", "lab_glass", "W", "lab2", "Z", "vent", "Y", "tank", "R", "parapet", "M", "marble",
        "1", "logo0", "2", "logo1", "3", "logo2", "4", "logo3", "S", "servers", "N", "lockers", "P", "hosp_c3",
        "F", "cafe", "D", "door", "b", "door_blue", "r", "door_red", "y", "door_yellow", "g", "door_green",
        "x", "door_exit", "e", "door_entrance", "v", "door_vectra", "o", "door_or", "k", "kitchen"};

    static void initTex() {
        if (TX != null && TX.length == 128) {
            return;
        }
        int[] tx = new int[128];
        for (int k = 0; k < FIXED.length; k += 2) {
            tx[FIXED[k].charAt(0)] = Art.t(FIXED[k + 1]);
        }
        for (int k = 0; k < SIGNS.length(); k++) {
            tx[SIGNS.charAt(k)] = Art.t(SIGN_TEX[k]);
        }
        TX_CORR = Art.t("hosp_c0");
        F_HELI0 = Art.t("f_heli0");
        TX_WIN2 = Art.t("window2");
        TX_PH2 = Art.t("pharmacy2");
        TX_MON1 = Art.t("monitors1");
        TX_CONC = Art.t("concrete");
        TX = tx;
    }

    static int floorTex(char z) {
        switch (z) {
            case 'm': return Art.t("f_terrazzo");
            case 'u': case 'i': return Art.t("f_vinyl_blue");
            case 'w': return Art.t("f_vinyl_green");
            case 'p': return Art.t("f_kids");
            case 's': return Art.t("f_or");
            case 'l': return Art.t("f_lab");
            case 'k': case 'n': return Art.t("f_wood");
            case 't': return Art.t("f_wc");
            case 'a': case 'x': return Art.t("f_concrete");
            case 'd': return Art.t("f_dock");
            case 'e': case 'h': return Art.t("f_roof");
            default: return Art.t("f_vinyl");
        }
    }

    /** Lumina din celulă are o lampă în tavan? */
    static boolean lampLight(char l) {
        return "fFwsocr".indexOf(l) >= 0;
    }

    static int ceilTex(char z, char light) {
        boolean lamp = lampLight(light);
        if (light == 'r') {
            return Art.t("c_red");
        }
        switch (z) {
            case 'm': return Art.t(lamp ? "c_lobby_l" : "c_lobby");
            case 's': return Art.t(lamp ? "c_or_l" : "c_or");
            case 'l': return Art.t(lamp ? "c_lab_l" : "c_lab");
            case 'a': case 'd': case 'x': return Art.t(lamp ? "c_concrete_l" : "c_concrete");
            default: return Art.t(lamp ? (light == 'o' ? "c_concrete_l" : "c_tiles_l") : "c_tiles");
        }
    }

    static int hash(int x, int y) {
        int h = x * 73856093 ^ y * 19349663;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        return (h ^ (h >>> 15)) & 0x7FFFFFFF;
    }

    /** Peretele obișnuit (#) văzut dintr-o cameră din zona z. */
    static int zoneWall(char z, int h) {
        switch (z) {
            case 'm': return Art.t("marble");
            case 'c': {
                int v = h % 14;
                return v < 9 ? TX_CORR : Art.t("hosp_c" + (v - 8));
            }
            case 'u': return Art.t(h % 5 == 0 ? "hosp_u1" : "hosp_u");
            case 'w': return Art.t(h % 6 == 0 ? "hosp_w1" : "hosp_w");
            case 'o': return Art.t(h % 5 == 0 ? "hosp_o1" : "hosp_o");
            case 'i': return Art.t(h % 4 == 0 ? "hosp_i1" : "hosp_i");
            case 'p': return Art.t(h % 3 == 0 ? "kids1" : (h % 3 == 1 ? "kids0" : "hosp_w"));
            case 's': return Art.t(h % 5 == 0 ? "or1" : (h % 5 == 1 ? "or2" : "or0"));
            case 'l': return Art.t(h % 6 == 0 ? "lab1" : "lab0");
            case 'k': return Art.t(h % 4 == 0 ? "hosp_k1" : "hosp_k");
            case 't': return Art.t("wc");
            case 'r': return Art.t(h % 4 == 0 ? "hosp_r1" : "hosp_r");
            case 'f': return Art.t(h % 4 == 0 ? "hosp_f1" : "hosp_f");
            case 'n': return Art.t(h % 4 == 0 ? "chapel1" : (h % 4 == 1 ? "chapel2" : "chapel0"));
            case 'v': return Art.t(h % 2 == 0 ? "lockers" : "hosp_c0");
            case 'e': case 'h': return Art.t("metal");
            default: return Art.t("concrete");
        }
    }

    /** Textura unui perete văzut dintr-o celulă din zona `z`. */
    int wallTex(char c, char z, int x, int y) {
        // inscripția și marmura holului se văd doar din hol
        if (z != 'm' && "1234Me".indexOf(c) >= 0) {
            c = '#';
        }
        if (c == '#') {
            return wallCache(z, x, y);
        }
        if (c == 'G') {
            return hash(x, y) % 3 == 0 ? TX_WIN2 : TX['G'];
        }
        if (c == 'A' && (hash(x, y) & 1) == 1) {
            return TX_PH2;
        }
        if (c == 'V' && (hash(x, y) & 1) == 1) {
            return TX_MON1;
        }
        if (c < 128 && TX[c] != 0) {
            return TX[c];
        }
        return TX_CONC;
    }

    /** Pereții obișnuiți (#), pe celule și zone: calculați o dată pe hartă, ca să nu căutăm la fiecare cadru. */
    int[] wallC;
    String wallZones = "mcuwoipslktrfnvehadx";

    int wallCache(char z, int x, int y) {
        int zi = wallZones.indexOf(z);
        if (zi < 0) {
            zi = wallZones.length() - 1;
        }
        int nzn = wallZones.length();
        if (wallC == null || wallC.length != mw * mh * nzn) {
            wallC = new int[mw * mh * nzn];
            for (int i = 0; i < wallC.length; i++) {
                wallC[i] = -1;
            }
        }
        int o = (y * mw + x) * nzn + zi;
        int v = wallC[o];
        if (v < 0) {
            v = zoneWall(wallZones.charAt(zi), hash(x, y));
            wallC[o] = v;
        }
        return v;
    }

    static boolean isDoor(char c) {
        return "DbrygxevoH".indexOf(c) >= 0;
    }

    /** Măștile de pixeli care luminează singuri (ecrane, geamuri, negatoscoape, lumânări, semne). */
    void buildEmissive() {
        initTex();
        int n = Art.tex.length;
        emis = new byte[n][];
        emisMode = new int[n];
        for (int t = 0; t < n; t++) {
            int[] p = Art.tex[t];
            String nm = Art.texName[t] == null ? "" : Art.texName[t];
            byte[] m = new byte[p.length];
            int mode = 0;
            for (int i = 0; i < p.length; i++) {
                int c = p[i];
                int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
                int mx = Math.max(r, Math.max(g, b)), mn = Math.min(r, Math.min(g, b));
                boolean on = false;
                if (nm.startsWith("window") || nm.equals("entrance") || nm.equals("door_entrance")) {
                    on = i < 50 * 64 && !(mn > 200 && mx - mn < 20) && !(r == g && g == b);
                    mode = 1;
                } else if (nm.equals("lab_glass")) {
                    on = i < 46 * 64 && b > 140;
                    mode = 1;
                } else if (nm.startsWith("monitors")) {
                    on = true;
                    mode = 1;
                } else if (nm.equals("xray")) {
                    on = mn > 120 || (b > 60 && i / 64 > 8 && i / 64 < 42 && (i % 64 > 4 && i % 64 < 30 || i % 64 > 34 && i % 64 < 60));
                    mode = 1;
                } else if (nm.equals("servers") || nm.equals("panel") || nm.equals("liftpanel") || nm.equals("icu")) {
                    on = mx > 150 && mx - mn > 90 || (nm.equals("icu") && g > 150 && r < 120);
                    mode = 1;
                } else if (nm.equals("door_exit")) {
                    on = g > 100 && g > r + 40;
                    mode = 1;
                } else if (nm.equals("door_vectra")) {
                    on = r > 200 && g < 130;
                    mode = 1;
                } else if (nm.equals("chapel1") || nm.equals("chapel2")) {
                    on = r > 240 && g > 150 && b < 200;
                    mode = 1;
                } else if (nm.startsWith("logo")) {
                    on = mx > 180;
                    mode = 2;
                } else if (nm.endsWith("_l")) {
                    on = mn > 180;
                    mode = 2;
                } else if (nm.equals("c_red")) {
                    on = r > 100 && g < 90;
                    mode = 3;
                }
                m[i] = (byte) (on ? 1 : 0);
            }
            emis[t] = m;
            emisMode[t] = mode;
        }
    }

    boolean emissiveOn(int t) {
        int m = emisMode[t];
        return m == 1 || (m == 2 && power) || (m == 3 && !power);
    }

    // ================================================================== lumina

    /**
     * Sursele: f neon, F neon care pâlpâie, w spot cald, s lampă puternică, r avarie roșie, b monitoare,
     * u zorii de la geamuri, g semn verde de ieșire, o bec portocaliu, c neon rece de pe circuitul de urgență
     * (ATI, blocul operator, laboratorul: merge mereu), n lumânări.
     */
    static final String LIGHT_CH = "fFwsrbugocn";
    static final int[] L_COL = {0xFFF6E8, 0xFFF6E8, 0xFFD49A, 0xFFF8EC, 0xFF2C1C, 0x4A8CFF, 0x9C78B4, 0x2CDC5A,
        0xFF9640, 0xE4F2FF, 0xFFA048};
    static final float[] L_RAD = {5.0f, 5.0f, 4.6f, 5.6f, 4.8f, 3.4f, 5.6f, 2.8f, 4.6f, 5.0f, 2.8f};
    static final float[] L_INT = {1.05f, 1.05f, 1.0f, 1.1f, 0.85f, 0.8f, 0.9f, 0.65f, 1.0f, 0.95f, 0.8f};
    /** 0 = doar cu curent, 1 = doar fără curent (avarie), 2 = mereu. */
    static final int[] L_MODE = {0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2};
    /** Lumina zorilor afară, pe acoperiș (8.8). */
    static final int DAWN_R = 168, DAWN_G = 140, DAWN_B = 150;
    static final int AMB_OFF = 28, AMB_ON = 46;
    /** Peste valoarea asta lumina crește mai încet (nu se „arde” culoarea când se adună multe surse). */
    static final int L_KNEE = 300;

    void buildLights() {
        lw = mw * 2 + 1;
        lh = mh * 2 + 1;
        int n = lw * lh;
        offR = new int[n];
        offG = new int[n];
        offB = new int[n];
        onR = new int[n];
        onG = new int[n];
        onB = new int[n];
        lmR = new int[n];
        lmG = new int[n];
        lmB = new int[n];
        for (int i = 0; i < n; i++) {
            offR[i] = AMB_OFF;
            offG[i] = AMB_OFF;
            offB[i] = AMB_OFF + 9;
            onR[i] = AMB_ON;
            onG[i] = AMB_ON;
            onB[i] = AMB_ON + 4;
        }
        // afară e zori: lumina vine de sus, la fel peste tot
        if (anyOutdoor) {
            for (int j = 0; j < lh; j++) {
                for (int i = 0; i < lw; i++) {
                    boolean out = false;
                    for (int k = 0; k < 4 && !out; k++) {
                        int cx = (i - 1 + (k & 1)) >> 1, cy = (j - 1 + (k >> 1)) >> 1;
                        if (cx >= 0 && cy >= 0 && cx < mw && cy < mh && outdoor[cy * mw + cx]
                                && lv.struct[cy * mw + cx] == '.') {
                            out = true;
                        }
                    }
                    if (out) {
                        int o = j * lw + i;
                        offR[o] = onR[o] = DAWN_R;
                        offG[o] = onG[o] = DAWN_G;
                        offB[o] = onB[o] = DAWN_B;
                    }
                }
            }
        }
        int nf = 0;
        for (int i = 0; i < mw * mh; i++) {
            if (lv.lights[i] == 'F') {
                nf++;
            }
        }
        nflick = nf;
        flx = new int[nf];
        fly = new int[nf];
        flI = new int[nf];
        flT = new float[nf];
        flR = new int[nf][];
        flG = new int[nf][];
        flB = new int[nf][];
        flX0 = new int[nf];
        flY0 = new int[nf];
        flW = new int[nf];
        flH = new int[nf];
        int fi = 0;
        for (int cy = 0; cy < mh; cy++) {
            for (int cx = 0; cx < mw; cx++) {
                char l = lv.lights[cy * mw + cx];
                int k = LIGHT_CH.indexOf(l);
                if (k < 0) {
                    continue;
                }
                if (l == 'F') {
                    int r = (int) Math.ceil(L_RAD[k] * 2);
                    int x0 = Math.max(0, cx * 2 + 1 - r), y0 = Math.max(0, cy * 2 + 1 - r);
                    int x1 = Math.min(lw - 1, cx * 2 + 1 + r), y1 = Math.min(lh - 1, cy * 2 + 1 + r);
                    flx[fi] = cx;
                    fly[fi] = cy;
                    flX0[fi] = x0;
                    flY0[fi] = y0;
                    flW[fi] = x1 - x0 + 1;
                    flH[fi] = y1 - y0 + 1;
                    int sz = flW[fi] * flH[fi];
                    flR[fi] = new int[sz];
                    flG[fi] = new int[sz];
                    flB[fi] = new int[sz];
                    flI[fi] = 256;
                    flT[fi] = rnd.nextFloat() * 3f;
                    addLight(cx, cy, k, flR[fi], flG[fi], flB[fi], x0, y0, flW[fi]);
                    fi++;
                    continue;
                }
                int m = L_MODE[k];
                if (m == 0 || m == 2) {
                    addLight(cx, cy, k, onR, onG, onB, 0, 0, lw);
                }
                if (m == 1 || m == 2) {
                    addLight(cx, cy, k, offR, offG, offB, 0, 0, lw);
                }
            }
        }
        knee(onR);
        knee(onG);
        knee(onB);
        knee(offR);
        knee(offG);
        knee(offB);
        applyPower();
    }

    static void knee(int[] a) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] > L_KNEE) {
                a[i] = L_KNEE + (a[i] - L_KNEE) / 3;
            }
        }
    }

    /** Adaugă lumina unei surse în grila dată (care începe la x0, y0 și are lățimea gw). */
    void addLight(int cx, int cy, int k, int[] R, int[] G, int[] B, int x0, int y0, int gw) {
        float lx = cx + 0.5f, ly = cy + 0.5f;
        float rad = L_RAD[k];
        int col = L_COL[k];
        float cr = ((col >> 16) & 255) / 255f * L_INT[k];
        float cg = ((col >> 8) & 255) / 255f * L_INT[k];
        float cb = (col & 255) / 255f * L_INT[k];
        int r2 = (int) Math.ceil(rad * 2);
        int gh = R.length / gw;
        for (int j = cy * 2 + 1 - r2; j <= cy * 2 + 1 + r2; j++) {
            for (int i = cx * 2 + 1 - r2; i <= cx * 2 + 1 + r2; i++) {
                if (i < 0 || j < 0 || i >= lw || j >= lh) {
                    continue;
                }
                int gi = i - x0, gj = j - y0;
                if (gi < 0 || gj < 0 || gi >= gw || gj >= gh) {
                    continue;
                }
                float sx = i * 0.5f, sy = j * 0.5f;
                float dx = sx - lx, dy = sy - ly;
                float d = (float) Math.sqrt(dx * dx + dy * dy + 0.35f);
                if (d >= rad) {
                    continue;
                }
                if (!lightSees(lx, ly, sx, sy)) {
                    continue;
                }
                float f = 1f - d / rad;
                f = f * f * (1.6f - 0.6f * f) * 1.15f;
                int o = gj * gw + gi;
                R[o] += (int) (cr * f * 256f);
                G[o] += (int) (cg * f * 256f);
                B[o] += (int) (cb * f * 256f);
            }
        }
    }

    /** Lumina ajunge de la sursă la punct fără să treacă printr-un perete? */
    boolean lightSees(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0, dy = y1 - y0;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if (d < 0.01f) {
            return true;
        }
        float ux = dx / d, uy = dy / d;
        // punctul țintă poate sta pe marginea unui perete: ne oprim puțin înainte
        float end = d - 0.04f;
        for (float s = 0.2f; s < end; s += 0.2f) {
            int cx = (int) (x0 + ux * s), cy = (int) (y0 + uy * s);
            if (cx < 0 || cy < 0 || cx >= mw || cy >= mh) {
                return false;
            }
            if (map[cy * mw + cx] != '.') {
                return false;
            }
        }
        // ultima celulă dinaintea țintei (verificare exactă la colțuri)
        float bx = x1 - ux * 0.03f, by = y1 - uy * 0.03f;
        int cx = (int) bx, cy = (int) by;
        if (cx < 0 || cy < 0 || cx >= mw || cy >= mh) {
            return false;
        }
        return map[cy * mw + cx] == '.';
    }

    /** Copiază harta de lumină pentru starea curentă a curentului și adaugă neoanele care pâlpâie. */
    void applyPower() {
        int[] R = power ? onR : offR, G = power ? onG : offG, B = power ? onB : offB;
        System.arraycopy(R, 0, lmR, 0, R.length);
        System.arraycopy(G, 0, lmG, 0, G.length);
        System.arraycopy(B, 0, lmB, 0, B.length);
        for (int i = 0; i < mw * mh; i++) {
            char l = lv.lights[i];
            int k = LIGHT_CH.indexOf(l);
            lampCell[i] = k >= 0 && lampLight(l) && (L_MODE[k] == 2 || (L_MODE[k] == 0) == power);
        }
        if (power) {
            for (int f = 0; f < nflick; f++) {
                addFlick(f, flI[f]);
            }
        }
    }

    void addFlick(int f, int amount) {
        int[] R = flR[f], G = flG[f], B = flB[f];
        int w = flW[f];
        for (int j = 0; j < flH[f]; j++) {
            int o = (flY0[f] + j) * lw + flX0[f];
            int s = j * w;
            for (int i = 0; i < w; i++) {
                lmR[o + i] += (R[s + i] * amount) >> 8;
                lmG[o + i] += (G[s + i] * amount) >> 8;
                lmB[o + i] += (B[s + i] * amount) >> 8;
            }
        }
    }

    void updateFlicker(float dt) {
        if (!power) {
            return;
        }
        for (int f = 0; f < nflick; f++) {
            flT[f] -= dt;
            if (flT[f] <= 0f) {
                int ni2;
                if (flI[f] > 128) {
                    ni2 = rnd.nextInt(3) == 0 ? 40 : 0;
                    flT[f] = 0.05f + rnd.nextFloat() * 0.18f;
                } else {
                    ni2 = 256;
                    flT[f] = rnd.nextInt(4) == 0 ? 0.04f + rnd.nextFloat() * 0.1f : 0.6f + rnd.nextFloat() * 3.5f;
                }
                if (ni2 != flI[f]) {
                    addFlick(f, ni2 - flI[f]);
                    flI[f] = ni2;
                    lampCell[fly[f] * mw + flx[f]] = ni2 > 128;
                }
            }
        }
    }

    /** Lumina (8.8) într-un punct al lumii, interpolată; rezultat în out[0..2]. */
    public void lightAt(float x, float y, int[] out) {
        float fx = x * 2f, fy = y * 2f;
        int i = (int) fx, j = (int) fy;
        if (i < 0) {
            i = 0;
        }
        if (j < 0) {
            j = 0;
        }
        if (i >= lw - 1) {
            i = lw - 2;
        }
        if (j >= lh - 1) {
            j = lh - 2;
        }
        int ax = (int) ((fx - i) * 256f), ay = (int) ((fy - j) * 256f);
        if (ax < 0) {
            ax = 0;
        }
        if (ax > 256) {
            ax = 256;
        }
        if (ay < 0) {
            ay = 0;
        }
        if (ay > 256) {
            ay = 256;
        }
        int o = j * lw + i;
        int w00 = (256 - ax) * (256 - ay), w10 = ax * (256 - ay), w01 = (256 - ax) * ay, w11 = ax * ay;
        out[0] = (lmR[o] * w00 + lmR[o + 1] * w10 + lmR[o + lw] * w01 + lmR[o + lw + 1] * w11) >> 16;
        out[1] = (lmG[o] * w00 + lmG[o + 1] * w10 + lmG[o + lw] * w01 + lmG[o + lw + 1] * w11) >> 16;
        out[2] = (lmB[o] * w00 + lmB[o + 1] * w10 + lmB[o + lw] * w01 + lmB[o + lw + 1] * w11) >> 16;
    }

    // ================================================================== logica

    public int curMag() {
        return mag[weapon];
    }

    public int curRes() {
        return res[weapon];
    }

    public boolean owned(int w) {
        return w == 0 || (w == 1 && hasShotgun) || (w == 2 && hasSmg);
    }

    /**
     * Un pas de joc. fwd/side în [-1, 1] (joystick), turn = rotire în radiani (tragere pe ecran),
     * fire = trăgaciul e ținut apăsat.
     */
    public void update(float dt, float fwd, float side, float turn, boolean fire) {
        if (dead || won) {
            return;
        }
        playTime += dt;
        updateFlicker(dt);
        if (msgT > 0f) {
            msgT -= dt;
        }
        if (shootCd > 0f) {
            shootCd -= dt;
        }
        if (flashT > 0f) {
            flashT -= dt;
        }
        if (hurtT > 0f) {
            hurtT -= dt;
        }
        if (pumpT > 0f) {
            pumpT -= dt;
            if (pumpT <= 0f) {
                sfx |= S_PUMP;
            }
        }
        if (boomT > 0f) {
            boomT -= dt;
        }
        if (reloadT > 0f) {
            reloadT -= dt;
            if (reloadT <= 0f) {
                reloadT = 0f;
                int w = weapon;
                int take = W_MAG[w] - mag[w];
                if (take > res[w]) {
                    take = res[w];
                }
                mag[w] += take;
                res[w] -= take;
            }
        }
        if (turn != 0f) {
            setAngle(ang + turn);
        }
        float sp = 2.6f * dt;
        if (reloadT > 0f) {
            sp *= 0.65f;
        }
        float f = fwd < 0f ? fwd * 0.75f : fwd;
        float mx = dirX * sp * f - dirY * sp * side * 0.85f;
        float my = dirY * sp * f + dirX * sp * side * 0.85f;
        if (mx != 0f || my != 0f) {
            float nx = px + mx;
            if (freeBox(nx, py, PR) && !zombieNear(nx, py, 0.5f, -1) && !decorNear(nx, py, PR)) {
                px = nx;
            }
            float ny = py + my;
            if (freeBox(px, ny, PR) && !zombieNear(px, ny, 0.5f, -1) && !decorNear(px, ny, PR)) {
                py = ny;
            }
            walkPhase += dt * 9f * Math.min(1f, (float) Math.sqrt(fwd * fwd + side * side));
        }
        if (fwd > 0.3f) {
            useAhead(false);
        }
        int cells = mw * mh;
        for (int i = 0; i < cells; i++) {
            if (doorMoving[i]) {
                door[i] += dt * 1.3f;
                if (door[i] >= 1f) {
                    door[i] = 1f;
                    doorMoving[i] = false;
                }
            }
        }
        if (fire && shootCd <= 0f && reloadT <= 0f && (weapon == 2 || mag[weapon] > 0)) {
            if (weapon == 2) {
                fire();
            }
        }
        flowT -= dt;
        if (flowT <= 0f) {
            flowT = 0.3f;
            buildFlow();
        }
        pickups();
        updateZombies(dt);
        updateShots(dt);
        checkScene();
        checkHelicopter();
        if (health <= 30 && health > 0) {
            heartT -= dt;
            if (heartT <= 0f) {
                heartT = 0.6f + health * 0.03f;
                sfx |= S_HEART;
            }
        }
        if (health <= 0) {
            health = 0;
            dead = true;
        }
    }

    /** Butonul de acțiune (sau mersul înainte): ușa, panoul, liftul din fața jucătorului. */
    public void useAhead(boolean button) {
        int fx = (int) (px + dirX * 0.75f);
        int fy = (int) (py + dirY * 0.75f);
        if (fx < 0 || fy < 0 || fx >= mw || fy >= mh) {
            return;
        }
        int idx = fy * mw + fx;
        char c = map[idx];
        if (c == 'E') {
            useGenerator();
        } else if (c == 'L') {
            useLiftPanel();
        } else if (c == 'H') {
            if (button) {
                lockedMsg(Texts.ELEVATOR_DEAD);
            }
        } else if (c == 'O') {
            if (button) {
                lockedMsg(Texts.GARAGE_LOCKED);
            }
        } else if (isDoor(c) && door[idx] == 0f && !doorMoving[idx]) {
            tryOpen(idx);
        } else if (button) {
            for (int i = 0; i < nz; i++) {
                if (npc(i)) {
                    float dx = zx[i] - px, dy = zy[i] - py;
                    if (dx * dx + dy * dy < 2.5f) {
                        int t = zty[i];
                        pendingScene = t == Art.Z_ELENA ? SCENE_ELENA : (t == Art.Z_CRISTI ? SCENE_CRISTI : SCENE_IOANA);
                    }
                }
            }
        }
    }

    /** Panoul liftului de serviciu (acoperișul): după ce Cristi îți spune ce să faci, liftul pornește. */
    void useLiftPanel() {
        if (liftOn) {
            if (msg != Texts.LIFT_DONE && msg != Texts.LIFT_ON) {
                setMsg(Texts.LIFT_DONE);
            }
            return;
        }
        if (!metCristi) {
            lockedMsg(Texts.LIFT_ERROR);
            return;
        }
        liftOn = true;
        sfx |= S_GEN;
        setMsg(Texts.LIFT_ON);
        objective = 2;
        // creatura iese din puțul liftului, pe acoperiș
        for (int i = 0; i < nz; i++) {
            if (zst[i] == Z_HIDDEN) {
                zst[i] = Z_CHASE;
                zhas[i] = true;
                zlx[i] = px;
                zly[i] = py;
                zt[i] = 0f;
                sfx |= S_ROAR;
            }
        }
        pendingScene = SCENE_CREATURE;
    }

    /** Pe acoperiș: când creatura e moartă, urci în elicopter și jocul se termină. */
    void checkHelicopter() {
        if (ep != 2) {
            return;
        }
        int h = Art.decorIndex("elicopter");
        for (int k = 0; k < nd; k++) {
            if (dty[k] != h) {
                continue;
            }
            float dx = dxp[k] - px, dy = dyp[k] - py;
            if (dx * dx + dy * dy > 2.4f * 2.4f) {
                continue;
            }
            if (bossDead) {
                won = true;
                sfx |= S_DOOR;
            } else if (!metCristi) {
                if (msg != Texts.HELI_NO_PILOT) {
                    lockedMsg(Texts.HELI_NO_PILOT);
                }
            } else if (msg != Texts.HELI_WAIT) {
                lockedMsg(Texts.HELI_WAIT);
            }
        }
    }

    /** Harta de distanțe până la jucător: infectații îl urmăresc pe după colțuri. */
    void buildFlow() {
        int cells = mw * mh;
        for (int i = 0; i < cells; i++) {
            flow[i] = -1;
        }
        int sx = (int) px, sy = (int) py;
        if (sx < 0 || sy < 0 || sx >= mw || sy >= mh) {
            return;
        }
        int start = sy * mw + sx;
        flow[start] = 0;
        int head = 0, tail = 0;
        flowQ[tail++] = start;
        while (head < tail) {
            int c = flowQ[head++];
            int d = flow[c];
            if (d >= FLOW_MAX) {
                continue;
            }
            int x = c % mw, y = c / mw;
            for (int k = 0; k < 4; k++) {
                int nx = x + (k == 0 ? 1 : (k == 1 ? -1 : 0));
                int ny = y + (k == 2 ? 1 : (k == 3 ? -1 : 0));
                if (nx < 0 || ny < 0 || nx >= mw || ny >= mh) {
                    continue;
                }
                int n = ny * mw + nx;
                if (flow[n] < 0 && passable(n)) {
                    flow[n] = d + 1;
                    flowQ[tail++] = n;
                }
            }
        }
    }

    /** Prima intrare într-o cameră afișează o scenă scurtă din poveste. */
    void checkScene() {
        if (pendingNote >= 0 || pendingScene >= 0) {
            return;
        }
        int r = lv.room((int) px, (int) py);
        if (r < 0 || r >= 31 || (sceneSeen & (1 << r)) != 0) {
            return;
        }
        sceneSeen |= 1 << r;
        int rr = Texts.roomRadio(lv.rid[r]);
        if (rr >= 0) {
            radio(rr);
        }
        if (Texts.scene(lv.rid[r]) != null) {
            pendingScene = r;
        }
    }

    /** Pune în coadă un mesaj radio de la Ioana (fiecare se aude o singură dată). */
    public void radio(int id) {
        if ((radioSeen & (1L << id)) != 0 || Texts.radio(ep, id) == null) {
            return;
        }
        radioSeen |= 1L << id;
        if (radioN >= radioQ.length) {
            radioN--;
        }
        radioQ[radioN++] = id;
        sfx |= S_RADIO;
    }

    public int peekRadio() {
        return radioN == 0 ? -1 : radioQ[0];
    }

    public int popRadio() {
        if (radioN == 0) {
            return -1;
        }
        int r = radioQ[0];
        for (int i = 1; i < radioN; i++) {
            radioQ[i - 1] = radioQ[i];
        }
        radioN--;
        return r;
    }

    /** După o pauză de citit, infectații din jur mai așteaptă puțin până lovesc. */
    public void resumeGrace() {
        for (int i = 0; i < nz; i++) {
            if (zatk[i] < 0.8f) {
                zatk[i] = 0.8f;
            }
        }
    }

    public int notesFound() {
        int n = 0;
        for (int i = 0; i < ni; i++) {
            if (it[i] == IT_NOTE && !ion[i]) {
                n++;
            }
        }
        return n;
    }

    public int notesTotal() {
        int n = 0;
        for (int i = 0; i < ni; i++) {
            if (it[i] == IT_NOTE) {
                n++;
            }
        }
        return n;
    }

    boolean passable(int idx) {
        char c = map[idx];
        if (c == '.') {
            return true;
        }
        return isDoor(c) && c != 'x' && c != 'H' && door[idx] >= 0.98f;
    }

    boolean blockedAt(float x, float y) {
        if (x < 0f || y < 0f) {
            return true;
        }
        int cx = (int) x, cy = (int) y;
        if (cx >= mw || cy >= mh) {
            return true;
        }
        return !passable(cy * mw + cx);
    }

    boolean freeBox(float x, float y, float r) {
        return !blockedAt(x - r, y - r) && !blockedAt(x + r, y - r)
                && !blockedAt(x - r, y + r) && !blockedAt(x + r, y + r);
    }

    boolean decorNear(float x, float y, float r) {
        for (int i = 0; i < nd; i++) {
            float rr = drad[i];
            if (rr <= 0f) {
                continue;
            }
            float dx = dxp[i] - x, dy = dyp[i] - y;
            float lim = rr + r;
            if (dx * dx + dy * dy < lim * lim) {
                return true;
            }
        }
        return false;
    }

    boolean zombieNear(float x, float y, float r, int skip) {
        for (int i = 0; i < nz; i++) {
            if (i == skip || zst[i] >= Z_DYING) {
                continue;
            }
            float rr = isBoss(i) ? r + 0.25f : r;
            float dx = zx[i] - x, dy = zy[i] - y;
            if (dx * dx + dy * dy < rr * rr) {
                return true;
            }
        }
        return false;
    }

    boolean playerNear(float x, float y, float r) {
        float dx = x - px, dy = y - py;
        return dx * dx + dy * dy < r * r;
    }

    void lockedMsg(String s) {
        if (msg != s || msgT < 1.5f) {
            sfx |= S_LOCKED;
        }
        setMsg(s);
    }

    void tryOpen(int idx) {
        char c = map[idx];
        if (c == 'x') {
            if (ep == 0 && !power) {
                lockedMsg(Texts.STAIRS_NO_POWER);
            } else if (ep == 0 && !metElena) {
                lockedMsg(Texts.NEED_ELENA);
            } else if (ep == 2) {
                lockedMsg(Texts.NO_WAY_BACK);
            } else {
                won = true;
                sfx |= S_DOOR;
            }
            return;
        }
        if (c == 'e') {
            lockedMsg(Texts.ENTRANCE_LOCKED);
            return;
        }
        if (c == 'v') {
            lockedMsg(Texts.VECTRA_SEALED);
            return;
        }
        if (c == 'b') {
            if (!keyBlue) {
                lockedMsg(Texts.LOCK_BLUE);
                return;
            }
            setMsg(Texts.USE_BLUE);
            sfx |= S_CARD;
        } else if (c == 'r') {
            if (!keyRed) {
                lockedMsg(Texts.LOCK_RED[ep]);
                return;
            }
            setMsg(Texts.USE_RED);
            sfx |= S_CARD;
        } else if (c == 'y') {
            if (!keyYellow) {
                lockedMsg(Texts.LOCK_YELLOW);
                return;
            }
            setMsg(Texts.USE_YELLOW);
        } else if (c == 'g') {
            if (!keyGreen) {
                lockedMsg(Texts.LOCK_GREEN);
                return;
            }
            setMsg(Texts.USE_GREEN);
            sfx |= S_CARD;
        } else if (c == 'H') {
            lockedMsg(Texts.ELEVATOR_DEAD);
            return;
        }
        sfx |= S_DOOR;
        doorMoving[idx] = true;
    }

    /** Maneta generatorului: aprinde luminile și trezește infectații din jur. */
    void useGenerator() {
        if (power) {
            if (msg != Texts.GEN_DONE && msg != Texts.GEN_ON) {
                setMsg(Texts.GEN_DONE);
            }
            return;
        }
        power = true;
        sfx |= S_GEN;
        setMsg(Texts.GEN_ON);
        applyPower();
        objective = 3;
        radio(R_POWER);
        for (int i = 0; i < nz; i++) {
            if (zst[i] == Z_IDLE && counts(i)) {
                float dx = zx[i] - px, dy = zy[i] - py;
                if (dx * dx + dy * dy < 144f) {
                    zst[i] = Z_CHASE;
                    zlx[i] = px;
                    zly[i] = py;
                    zhas[i] = true;
                }
            }
        }
    }

    public void setMsg(String s) {
        msg = s;
        msgT = 2.6f;
    }

    void pickups() {
        for (int i = 0; i < ni; i++) {
            if (!ion[i]) {
                continue;
            }
            float dx = ix[i] - px, dy = iy[i] - py;
            if (dx * dx + dy * dy > 0.36f) {
                continue;
            }
            int t = it[i];
            boolean took = true;
            if (t == IT_AMMO) {
                res[0] += D_AMMO[difficulty];
                setMsg(Texts.GOT_AMMO + D_AMMO[difficulty]);
            } else if (t == IT_SHELLS) {
                res[1] += D_SHELLS[difficulty];
                setMsg(Texts.GOT_SHELLS + D_SHELLS[difficulty]);
            } else if (t == IT_SMGAMMO) {
                res[2] += D_SMGAMMO[difficulty];
                setMsg(Texts.GOT_SMGAMMO + D_SMGAMMO[difficulty]);
            } else if (t == IT_SHOTGUN) {
                hasShotgun = true;
                mag[1] = W_MAG[1];
                if (reloadT <= 0f) {
                    weapon = 1;
                    shootCd = 0.5f;
                }
                setMsg(Texts.GOT_SHOTGUN);
                radio(R_SHOTGUN);
            } else if (t == IT_SMG) {
                hasSmg = true;
                mag[2] = W_MAG[2];
                if (reloadT <= 0f) {
                    weapon = 2;
                    shootCd = 0.4f;
                }
                setMsg(Texts.GOT_SMG);
                radio(R_SMG);
            } else if (t == IT_MED) {
                if (health < 100) {
                    health += D_MED[difficulty];
                    if (health > 100) {
                        health = 100;
                    }
                    setMsg(Texts.GOT_MED + D_MED[difficulty]);
                } else {
                    took = false;
                }
            } else if (t == IT_BLUE) {
                keyBlue = true;
                setMsg(Texts.GOT_BLUE);
                radio(R_BLUE);
            } else if (t == IT_RED) {
                keyRed = true;
                setMsg(Texts.GOT_RED);
                radio(R_RED);
                if (ep == 0 && metElena && objective < 2) {
                    objective = 2;
                }
            } else if (t == IT_YELLOW) {
                keyYellow = true;
                setMsg(Texts.GOT_YELLOW);
                radio(R_YELLOW);
                if (ep == 1 && objective < 1) {
                    objective = 1;
                }
            } else if (t == IT_GREEN) {
                keyGreen = true;
                setMsg(Texts.GOT_GREEN);
                radio(R_GREEN);
                if (ep == 1 && objective < 2) {
                    objective = 2;
                }
            } else if (t == IT_NOTE) {
                pendingNote = inote[i];
                ion[i] = false;
                sfx |= S_PAPER;
                return;
            }
            if (took) {
                ion[i] = false;
                sfx |= S_PICKUP;
            }
        }
    }

    void updateZombies(float dt) {
        for (int i = 0; i < nz; i++) {
            int st = zst[i];
            int ty = zty[i];
            if (st == Z_DEAD || st == Z_HIDDEN || zdecor[i]) {
                continue;
            }
            if (ty == T_O2) {
                // butelia lovită: arde o clipă, apoi explodează
                if (st == Z_DYING) {
                    zt[i] += dt;
                    if (zt[i] > 0.5f) {
                        zst[i] = Z_DEAD;
                    }
                } else if (zhit[i] > 0f) {
                    zhit[i] -= dt;
                    if (zhit[i] <= 0f) {
                        explode(i);
                    }
                }
                continue;
            }
            if (st == Z_DYING) {
                zt[i] += dt;
                if (zt[i] > (isBoss(i) ? 0.7f : 0.4f)) {
                    zst[i] = Z_DEAD;
                }
                continue;
            }
            float dx = px - zx[i], dy = py - zy[i];
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (npc(i)) {
                zt[i] += dt;
                if (d < 1.4f) {
                    if (ty == Art.Z_ELENA && !metElena) {
                        metElena = true;
                        pendingScene = SCENE_ELENA;
                        if (objective < 1) {
                            objective = keyRed ? 2 : 1;
                        }
                    } else if (ty == Art.Z_CRISTI && !metCristi) {
                        metCristi = true;
                        keyRed = true;
                        pendingScene = SCENE_CRISTI;
                        if (objective < 1) {
                            objective = 1;
                        }
                    } else if (ty == Art.Z_IOANA && !foundIoana) {
                        foundIoana = true;
                        pendingScene = SCENE_IOANA;
                    }
                }
                continue;
            }
            boolean boss = isBoss(i);
            if (zanim[i] > 0f) {
                zanim[i] -= dt;
            }
            zlos[i] -= dt;
            if (zlos[i] <= 0f) {
                zlos[i] = 0.25f;
                zsee[i] = d < 9f && los(zx[i], zy[i], px, py);
                if (zsee[i]) {
                    zlx[i] = px;
                    zly[i] = py;
                    zhas[i] = true;
                    if (st == Z_IDLE) {
                        zst[i] = Z_CHASE;
                        st = Z_CHASE;
                        sfx |= boss ? S_ROAR : ((i & 1) == 0 ? S_GROAN : S_GROAN2);
                        zgr[i] = 3f + rnd.nextFloat() * 4f;
                    }
                    if (boss) {
                        radio(R_BOSS_SEEN);
                    }
                }
            }
            if (st != Z_CHASE) {
                continue;
            }
            zt[i] += dt;
            if (d < 7f) {
                zgr[i] -= dt;
                if (zgr[i] <= 0f) {
                    zgr[i] = 4f + rnd.nextFloat() * 5f;
                    sfx |= boss ? S_ROAR : (rnd.nextInt(2) == 0 ? S_GROAN : S_GROAN2);
                }
            }
            if (zhit[i] > 0f) {
                zhit[i] -= dt;
                continue;
            }
            if (ty == Art.Z_SPIT && zsee[i] && d > 1.6f && d < 8.5f) {
                zspit[i] -= dt;
                if (zspit[i] <= 0f) {
                    spawnAcid(i, dx / d, dy / d);
                    zspit[i] = D_SPITCD[difficulty] + rnd.nextFloat() * 0.6f;
                    zanim[i] = 0.45f;
                }
                if (d < 5f) {
                    continue;
                }
            }
            float speed = zsp[i];
            if (ty == T_CREATURE) {
                // creatura se ghemuiește și sare spre tine
                if (zcharge[i] > 0f) {
                    zcharge[i] -= dt;
                    speed = 2.8f * D_SPEED[difficulty];
                } else {
                    zchargeCd[i] -= dt;
                    if (zchargeCd[i] <= 0f && zsee[i] && d > 2.4f && d < 8f) {
                        zcharge[i] = 0.9f;
                        zchargeCd[i] = 3.8f + rnd.nextFloat() * 2f;
                        zanim[i] = 0.5f;
                        sfx |= S_ROAR;
                    }
                }
            }
            float reach = boss ? 1.05f : 0.75f;
            if (d < reach) {
                zcharge[i] = 0f;
                zatk[i] -= dt;
                if (zatk[i] <= 0f) {
                    if (boss) {
                        zatk[i] = ty == T_CREATURE ? 1.2f : 1.5f;
                        hurt((int) ((14 + rnd.nextInt(7)) * D_DMG[difficulty]));
                        zanim[i] = 0.35f;
                    } else {
                        zatk[i] = 1.15f;
                        hurt((int) ((9 + rnd.nextInt(7)) * D_DMG[difficulty]));
                    }
                }
                continue;
            }
            zatk[i] = boss ? 0.7f : 0.55f;
            float tx, ty2;
            if (zsee[i]) {
                tx = px;
                ty2 = py;
            } else if (zhas[i]) {
                int cx = (int) zx[i], cy = (int) zy[i];
                int fd = flow[cy * mw + cx];
                int best = -1;
                if (fd > 0) {
                    int bv = fd;
                    for (int k = 0; k < 4; k++) {
                        int nx = cx + (k == 0 ? 1 : (k == 1 ? -1 : 0));
                        int ny = cy + (k == 2 ? 1 : (k == 3 ? -1 : 0));
                        if (nx < 0 || ny < 0 || nx >= mw || ny >= mh) {
                            continue;
                        }
                        int fv = flow[ny * mw + nx];
                        if (fv >= 0 && fv < bv) {
                            bv = fv;
                            best = ny * mw + nx;
                        }
                    }
                }
                if (best >= 0) {
                    tx = best % mw + 0.5f;
                    ty2 = best / mw + 0.5f;
                } else {
                    tx = zlx[i];
                    ty2 = zly[i];
                }
            } else {
                continue;
            }
            float ex = tx - zx[i], ey = ty2 - zy[i];
            float ed = (float) Math.sqrt(ex * ex + ey * ey);
            if (ed < 0.05f) {
                continue;
            }
            float s = speed * dt / ed;
            float rad = boss ? 0.4f : 0.25f;
            float stop = boss ? 0.85f : 0.6f;
            float nx = zx[i] + ex * s;
            if (freeBox(nx, zy[i], rad) && !zombieNear(nx, zy[i], 0.55f, i) && !playerNear(nx, zy[i], stop)
                    && !decorNear(nx, zy[i], 0.2f)) {
                zx[i] = nx;
            }
            float ny = zy[i] + ey * s;
            if (freeBox(zx[i], ny, rad) && !zombieNear(zx[i], ny, 0.55f, i) && !playerNear(zx[i], ny, stop)
                    && !decorNear(zx[i], ny, 0.2f)) {
                zy[i] = ny;
            }
        }
    }

    void spawnAcid(int i, float ux, float uy) {
        int slot = -1;
        for (int k = 0; k < MAXP; k++) {
            if (!pon[k]) {
                slot = k;
                break;
            }
        }
        if (slot < 0) {
            return;
        }
        prx[slot] = zx[i] + ux * 0.45f;
        pry[slot] = zy[i] + uy * 0.45f;
        pvx[slot] = ux * 4.2f;
        pvy[slot] = uy * 4.2f;
        plife[slot] = 3f;
        pon[slot] = true;
        sfx |= S_SPIT;
        radio(R_ACID);
    }

    void updateShots(float dt) {
        for (int k = 0; k < MAXP; k++) {
            if (!pon[k]) {
                continue;
            }
            plife[k] -= dt;
            float nx = prx[k] + pvx[k] * dt, ny = pry[k] + pvy[k] * dt;
            if (plife[k] <= 0f || blockedAt(nx, ny)) {
                pon[k] = false;
                continue;
            }
            prx[k] = nx;
            pry[k] = ny;
            float dx = px - nx, dy = py - ny;
            if (dx * dx + dy * dy < 0.16f) {
                hurt((int) (12 * D_DMG[difficulty]));
                pon[k] = false;
            }
        }
    }

    void hurt(int n) {
        sfx |= S_HURT;
        health -= n;
        hurtT = 0.35f;
        hurtCount++;
    }

    boolean los(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0, dy = y1 - y0;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        int n = (int) (d * 4f) + 1;
        for (int k = 1; k < n; k++) {
            float t = (float) k / n;
            if (blockedAt(x0 + dx * t, y0 + dy * t)) {
                return false;
            }
        }
        return true;
    }

    public void switchWeapon() {
        if (reloadT > 0f || dead || won) {
            return;
        }
        for (int k = 1; k < 3; k++) {
            int nw = (weapon + k) % 3;
            if (owned(nw)) {
                weapon = nw;
                shootCd = 0.45f;
                if (nw == 1) {
                    sfx |= S_PUMP;
                }
                return;
            }
        }
    }

    public void fire() {
        if (dead || won || reloadT > 0f || shootCd > 0f) {
            return;
        }
        int w = weapon;
        if (mag[w] <= 0) {
            if (res[w] > 0) {
                reload();
            } else {
                for (int k = 1; k < 3; k++) {
                    int nw = (w + k) % 3;
                    if (owned(nw) && (mag[nw] > 0 || res[nw] > 0)) {
                        weapon = nw;
                        shootCd = 0.45f;
                        return;
                    }
                }
                setMsg(Texts.NO_AMMO);
                sfx |= S_EMPTY;
            }
            return;
        }
        mag[w]--;
        shots++;
        shootCd = W_CD[w];
        if (w == 0) {
            sfx |= S_SHOT;
            flashT = 0.08f;
        } else if (w == 1) {
            sfx |= S_SHOTGUN;
            flashT = 0.12f;
            pumpT = 0.5f;
        } else {
            sfx |= S_SMG;
            flashT = 0.05f;
        }
        float wall = 1e9f;
        if (zbuf != null && W > 0) {
            wall = zbuf[W >> 1];
        }
        float invDet = 1f / (planeX * dirY - dirX * planeY);
        int best = -1;
        float bestD = wall;
        for (int i = 0; i < nz; i++) {
            if (zst[i] >= Z_DYING || zdecor[i] || npc(i)) {
                continue;
            }
            float rx = zx[i] - px, ry = zy[i] - py;
            float tX = invDet * (dirY * rx - dirX * ry);
            float tY = invDet * (-planeY * rx + planeX * ry);
            if (tY < 0.2f || tY >= wall) {
                continue;
            }
            float off = (W * 0.5f) * (tX / tY);
            if (off < 0f) {
                off = -off;
            }
            Img sp = sprite(i);
            float h = proj * spriteH(i) / tY;
            float wdt = h * sp.w / sp.h;
            if (w == 1) {
                if (off < wdt * 0.5f + W * 0.05f && tY < 8f) {
                    damage(i, tY < 2.5f ? 4 : (tY < 5f ? 3 : 2), true);
                }
            } else if (off < wdt * 0.4f && tY < bestD) {
                best = i;
                bestD = tY;
            }
        }
        if (best >= 0) {
            damage(best, 1, false);
        }
        wakeNear(px, py, w == 1 ? 100f : 64f);
    }

    /** Zgomotul trezește infectații din apropiere care au linie de vedere. */
    void wakeNear(float x, float y, float r2) {
        for (int i = 0; i < nz; i++) {
            if (zst[i] != Z_IDLE || !counts(i)) {
                continue;
            }
            float dx = zx[i] - x, dy = zy[i] - y;
            if (dx * dx + dy * dy < r2 && los(zx[i], zy[i], px, py)) {
                zst[i] = Z_CHASE;
                zsee[i] = true;
                zlx[i] = px;
                zly[i] = py;
                zhas[i] = true;
            }
        }
    }

    void damage(int i, int dmg, boolean heavy) {
        if (zty[i] == T_O2) {
            if (zst[i] < Z_DYING && zhit[i] <= 0f) {
                explode(i);
            }
            return;
        }
        boolean boss = isBoss(i);
        zhp[i] -= dmg;
        zst[i] = Z_CHASE;
        zsee[i] = true;
        zhas[i] = true;
        zlx[i] = px;
        zly[i] = py;
        if (boss) {
            zhit[i] = heavy ? 0.3f : 0.05f;
        } else {
            zhit[i] = 0.3f;
            float kb = heavy ? 0.3f : 0.15f;
            float kx = zx[i] + dirX * kb, ky = zy[i] + dirY * kb;
            if (freeBox(kx, ky, 0.25f)) {
                zx[i] = kx;
                zy[i] = ky;
            }
        }
        if (zhp[i] <= 0) {
            zst[i] = Z_DYING;
            zt[i] = 0f;
            kills++;
            sfx |= boss ? S_ROAR : S_ZDIE;
            int c = zcarry[i];
            if (c >= 0) {
                ix[c] = zx[i];
                iy[c] = zy[i];
                ion[c] = true;
            }
            if (boss) {
                bossDead = true;
                radio(R_BOSS_DEAD);
                objective = 3;
                if (ep == 1) {
                    openVectra();
                }
            }
        }
    }

    /** Subiectul 9 a murit: ușile sigilate ale laboratorului Vectra se deblochează. */
    void openVectra() {
        int cells = mw * mh;
        for (int i = 0; i < cells; i++) {
            if (map[i] == 'v' && door[i] == 0f) {
                doorMoving[i] = true;
            }
        }
        setMsg(Texts.VECTRA_OPEN);
        sfx |= S_DOOR | S_ALARM;
    }

    /** Butelia explodează: rănește tot ce e în jur (și pe tine) și aprinde buteliile vecine. */
    void explode(int i) {
        zst[i] = Z_DYING;
        zt[i] = 0f;
        zhit[i] = 0f;
        sfx |= S_EXPLODE;
        boomT = 0.35f;
        radio(R_O2);
        float ex = zx[i], ey = zy[i];
        for (int j = 0; j < nz; j++) {
            if (j == i || zdecor[j] || zst[j] >= Z_DYING || npc(j)) {
                continue;
            }
            float dx = zx[j] - ex, dy = zy[j] - ey;
            if (dx * dx + dy * dy < 2.3f * 2.3f) {
                if (zty[j] == T_O2) {
                    if (zhit[j] <= 0f) {
                        zhit[j] = 0.2f;
                    }
                } else {
                    damage(j, isBoss(j) ? 6 : 5, true);
                }
            }
        }
        float dx = px - ex, dy = py - ey;
        float d2 = dx * dx + dy * dy;
        if (d2 < 2.3f * 2.3f) {
            hurt((int) ((d2 < 1f ? 35 : 18) * D_DMG[difficulty]));
        }
        wakeNear(ex, ey, 100f);
    }

    public void reload() {
        int w = weapon;
        if (dead || won || reloadT > 0f || mag[w] >= W_MAG[w] || res[w] <= 0) {
            return;
        }
        reloadT = W_RELOAD[w];
        sfx |= S_RELOAD;
    }

    // ================================================================== salvare

    static final int SAVE_V1 = 0x544E3232;

    static void wf(DataOutputStream o, float f) throws IOException {
        o.writeInt(Float.floatToIntBits(f));
    }

    static float rf(DataInputStream in) throws IOException {
        return Float.intBitsToFloat(in.readInt());
    }

    public void writeState(DataOutputStream o) throws IOException {
        o.writeInt(SAVE_V1);
        o.writeUTF(lv.name);
        o.writeByte(difficulty);
        wf(o, px);
        wf(o, py);
        wf(o, ang);
        o.writeInt(health);
        o.writeByte(weapon);
        o.writeByte((hasShotgun ? 1 : 0) | (hasSmg ? 2 : 0));
        for (int w = 0; w < 3; w++) {
            o.writeInt(mag[w]);
            o.writeInt(res[w]);
        }
        o.writeInt(kills);
        o.writeInt(shots);
        o.writeByte((keyBlue ? 1 : 0) | (keyRed ? 2 : 0) | (keyYellow ? 4 : 0) | (keyGreen ? 8 : 0));
        o.writeByte((power ? 1 : 0) | (metElena ? 2 : 0) | (foundIoana ? 4 : 0) | (metCristi ? 8 : 0)
                | (liftOn ? 16 : 0) | (bossDead ? 32 : 0));
        wf(o, playTime);
        o.writeInt(sceneSeen);
        o.writeLong(radioSeen);
        o.writeByte(objective);
        int cells = mw * mh;
        for (int i = 0; i < cells; i++) {
            o.writeByte((door[i] > 0f || doorMoving[i] ? 1 : 0) | (seen[i] ? 2 : 0));
        }
        o.writeByte(nz);
        for (int i = 0; i < nz; i++) {
            wf(o, zx[i]);
            wf(o, zy[i]);
            o.writeShort(zhp[i]);
            o.writeByte(zst[i] == Z_DYING ? Z_DEAD : zst[i]);
        }
        o.writeByte(ni);
        for (int i = 0; i < ni; i++) {
            o.writeBoolean(ion[i]);
            wf(o, ix[i]);
            wf(o, iy[i]);
        }
    }

    /** Numele hărții dintr-o salvare (ca să știm ce etaj încărcăm). */
    public static String peekLevel(DataInputStream in) throws IOException {
        if (in.readInt() != SAVE_V1) {
            throw new IOException("salvare");
        }
        return in.readUTF();
    }

    /** Reface starea (după peekLevel și load(harta potrivită)). */
    public void readState(DataInputStream in) throws IOException {
        difficulty = in.readByte();
        if (difficulty < 0 || difficulty > 2) {
            difficulty = 1;
        }
        reset();
        px = rf(in);
        py = rf(in);
        setAngle(rf(in));
        health = in.readInt();
        weapon = in.readByte();
        int ow = in.readByte();
        hasShotgun = (ow & 1) != 0;
        hasSmg = (ow & 2) != 0;
        for (int w = 0; w < 3; w++) {
            mag[w] = in.readInt();
            res[w] = in.readInt();
        }
        if (weapon < 0 || weapon > 2 || !owned(weapon)) {
            weapon = 0;
        }
        kills = in.readInt();
        shots = in.readInt();
        int k = in.readByte();
        keyBlue = (k & 1) != 0;
        keyRed = (k & 2) != 0;
        keyYellow = (k & 4) != 0;
        keyGreen = (k & 8) != 0;
        int f = in.readByte();
        power = (f & 1) != 0;
        metElena = (f & 2) != 0;
        foundIoana = (f & 4) != 0;
        metCristi = (f & 8) != 0;
        liftOn = (f & 16) != 0;
        bossDead = (f & 32) != 0;
        playTime = rf(in);
        sceneSeen = in.readInt();
        radioSeen = in.readLong();
        objective = in.readByte();
        int cells = mw * mh;
        for (int i = 0; i < cells; i++) {
            int b = in.readByte();
            if ((b & 1) != 0) {
                door[i] = 1f;
            }
            seen[i] = (b & 2) != 0;
        }
        int n = in.readByte() & 255;
        if (n != nz) {
            throw new IOException("nz");
        }
        for (int i = 0; i < nz; i++) {
            zx[i] = rf(in);
            zy[i] = rf(in);
            zhp[i] = in.readShort();
            zst[i] = in.readByte();
            zt[i] = 0f;
            if (zst[i] == Z_CHASE) {
                zhas[i] = true;
                zlx[i] = px;
                zly[i] = py;
            }
        }
        n = in.readByte() & 255;
        if (n != ni) {
            throw new IOException("ni");
        }
        for (int i = 0; i < ni; i++) {
            ion[i] = in.readBoolean();
            ix[i] = rf(in);
            iy[i] = rf(in);
        }
        applyPower();
    }

    // ================================================================== randare

    static int shadeCol(int c, int lr, int lg, int lb) {
        int r = (((c >> 16) & 255) * lr) >> 8;
        int g = (((c >> 8) & 255) * lg) >> 8;
        int b = ((c & 255) * lb) >> 8;
        if (r > 255) {
            r = 255;
        }
        if (g > 255) {
            g = 255;
        }
        if (b > 255) {
            b = 255;
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Cât de întunecată devine lumea cu distanța (8.8). */
    int fog(float d) {
        return (int) (256f / (1f + d * d * fogK));
    }

    /** Luminozitatea din setări: 0 foarte întunecat, 2 normal, 4 foarte luminos (curbă gamma). */
    public void setBrightness(int level) {
        bright = level < 0 ? 0 : (level > 4 ? 4 : level);
        float[] gm = {1.45f, 1.2f, 1f, 0.78f, 0.6f};
        float g = gm[bright];
        for (int i = 0; i < 256; i++) {
            gammaLut[i] = Math.min(255, (int) (255.0 * Math.pow(i / 255.0, g) + 0.5));
        }
    }

    private final int[] tmpL = new int[3];

    /** Desenează lumea în buf (W x H). */
    public void render(int[] buf, boolean withGun) {
        // lanterna: mai slabă când în jur e deja lumină
        lightAt(px, py, tmpL);
        int lum = (tmpL[0] * 3 + tmpL[1] * 5 + tmpL[2] * 2) / 10;
        float fl = flashlight ? 1f - lum / 300f : 0f;
        if (fl < 0.12f) {
            fl = flashlight ? 0.12f : 0f;
        }
        if (fl > 1f) {
            fl = 1f;
        }
        curFlR = (int) (255 * 1.55f * fl);
        curFlG = (int) (238 * 1.55f * fl);
        curFlB = (int) (210 * 1.55f * fl);
        curMuzzle = flashT > 0f ? (int) (Math.min(1f, flashT / 0.08f) * 170f) : 0;
        if (boomT > 0f) {
            curMuzzle = Math.max(curMuzzle, (int) (boomT / 0.35f * 240f));
        }
        int pc = (int) py * mw + (int) px;
        fogK = pc >= 0 && pc < mw * mh && outdoor[pc] ? 0.0035f : 0.010f;
        if (anyOutdoor && Art.sky != null) {
            int sw2 = Art.sky.w;
            for (int x = 0; x < W; x++) {
                float camX = (2f * x + 1f) / W - 1f;
                double a = ang + Math.atan(camX * plane);
                int u = (int) (a / (2 * Math.PI) * sw2) % sw2;
                skyCol[x] = u < 0 ? u + sw2 : u;
            }
        }
        castFloor(buf);
        castWalls(buf);
        drawSprites(buf);
        if (withGun) {
            drawGun(buf);
        }
        if (bright != 2) {
            int[] lut = gammaLut;
            for (int i = W * H - 1; i >= 0; i--) {
                int c = buf[i];
                buf[i] = 0xFF000000 | (lut[(c >> 16) & 255] << 16) | (lut[(c >> 8) & 255] << 8) | lut[c & 255];
            }
        }
    }

    /** Pixelul de cer pentru coloana și rândul de pe ecran. */
    int skyAt(int x, int y) {
        return Art.sky.px[skyRow[y] * Art.sky.w + skyCol[x]] | 0xFF000000;
    }

    private void castFloor(int[] buf) {
        int half = H >> 1;
        float rdx0 = dirX - planeX, rdy0 = dirY - planeY;
        float rdx1 = dirX + planeX, rdy1 = dirY + planeY;
        float posZ = proj * 0.5f;
        int w = W;
        int[][] tex = Art.tex;
        int[] lr = lmR, lg = lmG, lb = lmB;
        int lw2 = lw;
        for (int y = half; y < H; y++) {
            float rowDist = posZ / ((y - half) + 0.5f);
            float stepX = rowDist * (rdx1 - rdx0) / w;
            float stepY = rowDist * (rdy1 - rdy0) / w;
            float fx = px + rowDist * rdx0 + stepX * 0.5f;
            float fy = py + rowDist * rdy0 + stepY * 0.5f;
            int fg = fog(rowDist);
            float fd = 1f / (1f + rowDist * rowDist * 0.05f);
            int flr = (int) (curFlR * fd), flg = (int) (curFlG * fd), flb = (int) (curFlB * fd);
            int mz = (int) (curMuzzle / (1f + rowDist * rowDist * 0.6f));
            int ifx = (int) (fx * 65536f), ify = (int) (fy * 65536f);
            int isx = (int) (stepX * 65536f), isy = (int) (stepY * 65536f);
            int of = y * w;
            int cyr = 2 * half - 1 - y;
            int oc = cyr * w;
            int co = y * w;
            int coC = cyr >= 0 ? cyr * w : 0;
            for (int x = 0; x < w; x++) {
                int cx = ifx >> 16, cy = ify >> 16;
                int col = 0xFF000000, ccol = 0xFF000000;
                if (cx >= 0 && cy >= 0 && cx < mw && cy < mh) {
                    int cell = cy * mw + cx;
                    int ti = (((ify >> 10) & 63) << 6) | ((ifx >> 10) & 63);
                    // lumina din grilă (interpolare biliniară)
                    int gx = ifx >> 15, gy = ify >> 15;
                    int ax = (ifx >> 7) & 255, ay = (ify >> 7) & 255;
                    int o = gy * lw2 + gx;
                    int r0 = lr[o] + (((lr[o + 1] - lr[o]) * ax) >> 8);
                    int r1 = lr[o + lw2] + (((lr[o + lw2 + 1] - lr[o + lw2]) * ax) >> 8);
                    int g0 = lg[o] + (((lg[o + 1] - lg[o]) * ax) >> 8);
                    int g1 = lg[o + lw2] + (((lg[o + lw2 + 1] - lg[o + lw2]) * ax) >> 8);
                    int b0 = lb[o] + (((lb[o + 1] - lb[o]) * ax) >> 8);
                    int b1 = lb[o + lw2] + (((lb[o + lw2 + 1] - lb[o + lw2]) * ax) >> 8);
                    int Lr = r0 + (((r1 - r0) * ay) >> 8);
                    int Lg = g0 + (((g1 - g0) * ay) >> 8);
                    int Lb = b0 + (((b1 - b0) * ay) >> 8);
                    Lr = ((Lr * fg) >> 8) + mz;
                    Lg = ((Lg * fg) >> 8) + ((mz * 200) >> 8);
                    Lb = ((Lb * fg) >> 8) + ((mz * 130) >> 8);
                    int cn = cone[co + x];
                    int fr = Lr + ((flr * cn) >> 8), fgc = Lg + ((flg * cn) >> 8), fb = Lb + ((flb * cn) >> 8);
                    col = shadeCol(tex[floorT[cell] & 255][ti], fr, fgc, fb);
                    if (cyr >= 0 && outdoor[cell]) {
                        ccol = skyAt(x, cyr);
                    } else if (cyr >= 0) {
                        int ct = ceilT[cell] & 255;
                        int tc = tex[ct][ti];
                        if (lampCell[cell] && emis[ct][ti] != 0) {
                            ccol = tc;
                        } else {
                            int cn2 = cone[coC + x];
                            ccol = shadeCol(tc, ((Lr * 200) >> 8) + ((flr * cn2) >> 8), ((Lg * 200) >> 8) + ((flg * cn2) >> 8),
                                    ((Lb * 200) >> 8) + ((flb * cn2) >> 8));
                        }
                    }
                }
                buf[of + x] = col;
                if (cyr >= 0) {
                    buf[oc + x] = ccol;
                }
                ifx += isx;
                ify += isy;
            }
        }
    }

    private void castWalls(int[] buf) {
        int half = H >> 1;
        int[][] tex = Art.tex;
        for (int x = 0; x < W; x++) {
            float camX = (2f * x + 1f) / W - 1f;
            float rdx = dirX + planeX * camX;
            float rdy = dirY + planeY * camX;
            int mx = (int) px, my = (int) py;
            float ddx = rdx == 0f ? 1e30f : Math.abs(1f / rdx);
            float ddy = rdy == 0f ? 1e30f : Math.abs(1f / rdy);
            int sx, sy;
            float sdx, sdy;
            if (rdx < 0f) {
                sx = -1;
                sdx = (px - mx) * ddx;
            } else {
                sx = 1;
                sdx = (mx + 1f - px) * ddx;
            }
            if (rdy < 0f) {
                sy = -1;
                sdy = (py - my) * ddy;
            } else {
                sy = 1;
                sdy = (my + 1f - py) * ddy;
            }
            int side = 0;
            int t = -1;
            float perp = 1e30f, wx = 0f;
            int pcx = mx, pcy = my;
            if (mx >= 0 && my >= 0 && mx < mw && my < mh) {
                seen[my * mw + mx] = true;
            }
            for (int step = 0; step < 160; step++) {
                pcx = mx;
                pcy = my;
                if (sdx < sdy) {
                    sdx += ddx;
                    mx += sx;
                    side = 0;
                } else {
                    sdy += ddy;
                    my += sy;
                    side = 1;
                }
                if (mx < 0 || my < 0 || mx >= mw || my >= mh) {
                    break;
                }
                int idx = my * mw + mx;
                char ch = map[idx];
                seen[idx] = true;
                if (ch == '.') {
                    continue;
                }
                float pd, w;
                if (side == 0) {
                    pd = sdx - ddx;
                    w = py + pd * rdy;
                } else {
                    pd = sdy - ddy;
                    w = px + pd * rdx;
                }
                w -= (float) Math.floor(w);
                if (isDoor(ch) && door[idx] > 0f) {
                    float o = door[idx];
                    if (w < o) {
                        continue;
                    }
                    w -= o;
                }
                perp = pd;
                wx = w;
                t = wallTex(ch, zone[pcy * mw + pcx], mx, my);
                break;
            }
            zbuf[x] = perp;
            if (t < 0) {
                continue;
            }
            if (perp < 0.04f) {
                perp = 0.04f;
            }
            int lineH = (int) (proj / perp);
            if (lineH < 1) {
                lineH = 1;
            }
            int ds = half - (lineH >> 1);
            int tx = (int) (wx * Art.TS);
            if (tx >= Art.TS) {
                tx = Art.TS - 1;
            }
            if (tx < 0) {
                tx = 0;
            }
            // textura se citește de la stânga la dreapta din partea din care o privim
            if ((side == 0 && rdx < 0f) || (side == 1 && rdy > 0f)) {
                tx = Art.TS - 1 - tx;
            }
            // lumina peretelui: din grilă, puțin în fața lui (în celula din care venim)
            float hx = px + rdx * perp, hy = py + rdy * perp;
            float bx = hx - (side == 0 ? sx * 0.02f : 0f), by = hy - (side == 1 ? sy * 0.02f : 0f);
            lightAt(bx, by, tmpL);
            int fg = fog(perp);
            int shadeSide = side == 1 ? 232 : 256;
            int Lr = (tmpL[0] * fg >> 8) * shadeSide >> 8;
            int Lg = (tmpL[1] * fg >> 8) * shadeSide >> 8;
            int Lb = (tmpL[2] * fg >> 8) * shadeSide >> 8;
            int mz = (int) (curMuzzle / (1f + perp * perp * 0.6f));
            Lr += mz;
            Lg += (mz * 200) >> 8;
            Lb += (mz * 130) >> 8;
            float fd = 1f / (1f + perp * perp * 0.05f);
            int flr = (int) (curFlR * fd), flg = (int) (curFlG * fd), flb = (int) (curFlB * fd);
            int[] tp = tex[t];
            byte[] em = emis[t];
            boolean emOn = emisMode[t] != 0 && emissiveOn(t);
            int stepT = (Art.TS << 16) / lineH;
            int tpos = 0;
            int y0 = ds;
            if (y0 < 0) {
                tpos = -y0 * stepT;
                y0 = 0;
            }
            int y1 = ds + lineH;
            if (y1 > H) {
                y1 = H;
            }
            int o = y0 * W + x;
            for (int y = y0; y < y1; y++) {
                int ti = (((tpos >> 16) & 63) << 6) | tx;
                int c = tp[ti];
                tpos += stepT;
                if (c == 0) {
                    buf[o] = skyAt(x, y);       // balustrada acoperișului: printre bare se vede cerul
                } else if (emOn && em[ti] != 0) {
                    buf[o] = c;
                } else {
                    int cn = cone[o];
                    buf[o] = shadeCol(c, Lr + ((flr * cn) >> 8), Lg + ((flg * cn) >> 8), Lb + ((flb * cn) >> 8));
                }
                o += W;
            }
        }
    }

    private void drawSprites(int[] buf) {
        int n = 0;
        for (int i = 0; i < nz; i++) {
            if (zst[i] == Z_HIDDEN) {
                continue;
            }
            float dx = zx[i] - px, dy = zy[i] - py;
            sOrder[n] = i;
            sDist[n++] = dx * dx + dy * dy;
        }
        for (int i = 0; i < ni; i++) {
            if (!ion[i]) {
                continue;
            }
            float dx = ix[i] - px, dy = iy[i] - py;
            sOrder[n] = 1000 + i;
            sDist[n++] = dx * dx + dy * dy;
        }
        for (int i = 0; i < MAXP; i++) {
            if (!pon[i]) {
                continue;
            }
            float dx = prx[i] - px, dy = pry[i] - py;
            sOrder[n] = 2000 + i;
            sDist[n++] = dx * dx + dy * dy;
        }
        for (int i = 0; i < nd; i++) {
            float dx = dxp[i] - px, dy = dyp[i] - py;
            float d2 = dx * dx + dy * dy;
            if (d2 > 900f) {
                continue;
            }
            sOrder[n] = 3000 + i;
            sDist[n++] = d2;
        }
        for (int i = 1; i < n; i++) {
            int o = sOrder[i];
            float d = sDist[i];
            int j = i - 1;
            while (j >= 0 && sDist[j] < d) {
                sOrder[j + 1] = sOrder[j];
                sDist[j + 1] = sDist[j];
                j--;
            }
            sOrder[j + 1] = o;
            sDist[j + 1] = d;
        }
        float invDet = 1f / (planeX * dirY - dirX * planeY);
        int half = H >> 1;
        for (int k = 0; k < n; k++) {
            int id = sOrder[k];
            float wxp, wyp, hScale;
            Img spr;
            int minLight = 0;
            boolean floating = false, hanging = false;
            if (id < 1000) {
                wxp = zx[id];
                wyp = zy[id];
                spr = sprite(id);
                hScale = spriteH(id);
                if (npc(id)) {
                    minLight = 60;
                } else if (zty[id] == T_O2 && zst[id] == Z_DYING) {
                    minLight = 300;
                }
            } else if (id < 2000) {
                int i = id - 1000;
                wxp = ix[i];
                wyp = iy[i];
                spr = Art.item[it[i]];
                hScale = 0.3f;
                minLight = 110;
            } else if (id < 3000) {
                int i = id - 2000;
                wxp = prx[i];
                wyp = pry[i];
                spr = Art.acid;
                hScale = 0.22f;
                floating = true;
                minLight = 200;
            } else {
                int i = id - 3000;
                wxp = dxp[i];
                wyp = dyp[i];
                spr = Art.decor[dty[i]];
                hScale = Art.decorH[dty[i]];
                hanging = Art.decorSolid[dty[i]] == 2;
            }
            float rx = wxp - px, ry = wyp - py;
            float tX = invDet * (dirY * rx - dirX * ry);
            float tY = invDet * (-planeY * rx + planeX * ry);
            if (tY < 0.15f) {
                continue;
            }
            int scx = (int) ((W * 0.5f) * (1f + tX / tY));
            int h = (int) (proj * hScale / tY);
            if (h < 2) {
                continue;
            }
            int sw = spr.w, sh = spr.h;
            int w = h * sw / sh;
            if (w < 2) {
                w = 2;
            }
            int floorY = floating ? half + (int) (proj * 0.12f / tY) + (h >> 1)
                    : (hanging ? half - (int) (proj * 0.5f / tY) + h : half + (int) (proj * 0.5f / tY));
            int top = floorY - h;
            int left = scx - (w >> 1);
            int xs = Math.max(0, left), xe = Math.min(W, left + w);
            if (xs >= xe) {
                continue;
            }
            // lumina sprite-ului
            lightAt(wxp, wyp, tmpL);
            int fg = fog(tY);
            if (minLight >= 300) {
                tmpL[0] = tmpL[1] = tmpL[2] = 256;
                fg = 256;
            }
            int Lr = tmpL[0] * fg >> 8, Lg = tmpL[1] * fg >> 8, Lb = tmpL[2] * fg >> 8;
            float fd = 1f / (1f + tY * tY * 0.05f);
            int cxs = Math.min(W - 1, Math.max(0, scx));
            int cn = cone[Math.min(H - 1, Math.max(0, (top + floorY) >> 1)) * W + cxs];
            Lr += (int) (curFlR * fd) * cn >> 8;
            Lg += (int) (curFlG * fd) * cn >> 8;
            Lb += (int) (curFlB * fd) * cn >> 8;
            int mz = (int) (curMuzzle / (1f + tY * tY * 0.6f));
            Lr += mz;
            Lg += (mz * 200) >> 8;
            Lb += (mz * 130) >> 8;
            if (minLight > 0) {
                int lum = (Lr + Lg + Lb) / 3;
                if (lum < minLight) {
                    int add = minLight - lum;
                    Lr += add;
                    Lg += add;
                    Lb += add;
                }
            }
            int ys = top < 0 ? 0 : top;
            int ye = floorY > H ? H : floorY;
            int ustep = (sw << 16) / w;
            int vstep = (sh << 16) / h;
            int[] pix = spr.px;
            for (int x = xs; x < xe; x++) {
                if (tY >= zbuf[x]) {
                    continue;
                }
                int u = ((x - left) * ustep) >> 16;
                if (u < 0 || u >= sw) {
                    continue;
                }
                int v = (ys - top) * vstep;
                int o = ys * W + x;
                for (int y = ys; y < ye; y++) {
                    int vv = v >> 16;
                    if (vv >= sh) {
                        break;
                    }
                    int c = pix[vv * sw + u];
                    v += vstep;
                    if (c != 0) {
                        buf[o] = shadeCol(c, Lr, Lg, Lb);
                    }
                    o += W;
                }
            }
        }
    }

    /** Cadrul curent al unui personaj, boss sau al unei butelii. */
    Img sprite(int id) {
        int st = zst[id], ty = zty[id];
        if (ty == T_O2) {
            if (st == Z_DYING) {
                int f = (int) (zt[id] * 6f);
                return Art.expl[f > 2 ? 2 : f];
            }
            return Art.o2[st == Z_DEAD ? 1 : 0];
        }
        int frame;
        if (st == Z_DYING) {
            frame = zt[id] < 0.2f ? 2 : 3;
        } else if (st == Z_DEAD) {
            frame = 3;
        } else if (zhit[id] > 0f) {
            frame = 2;
        } else if ((ty == Art.Z_SPIT || ty == T_CREATURE) && (zanim[id] > 0f || zcharge[id] > 0f)) {
            frame = 4;
        } else if (st == Z_CHASE) {
            frame = ((int) (zt[id] * (isBoss(id) ? 2.2f : 3f))) & 1;
        } else if (npc(id)) {
            frame = ((int) (zt[id] * 1.2f)) & 1;
        } else {
            frame = 0;
        }
        if (ty == T_BOSS9) {
            return Art.boss9[frame > 3 ? 0 : frame];
        }
        if (ty == T_CREATURE) {
            return Art.creature[frame];
        }
        return Art.zombie[ty][frame];
    }

    /** Înălțimea în lume a sprite-ului (1 = cât peretele). */
    float spriteH(int id) {
        int ty = zty[id];
        if (ty == T_BOSS9) {
            return 1.1f;
        }
        if (ty == T_CREATURE) {
            return zst[id] >= Z_DYING ? 0.9f : 1.05f;
        }
        if (ty == T_O2) {
            return zst[id] == Z_DYING ? 1.0f : 0.62f;
        }
        return npc(id) ? 0.76f : 0.8f;
    }

    private void drawGun(int[] buf) {
        int wp = weapon;
        if (gunHd[wp] != null) {
            drawGunHd(buf, wp);
            return;
        }
        float k = H / 180f;
        Img g = Art.gun[wp];
        int gw = (int) (g.w * k), gh = (int) (g.h * k);
        float bob = (float) Math.sin(walkPhase * 0.5f);
        float abob = Math.abs(bob);
        int bx = (int) (bob * 3f * k);
        int by = (int) (abob * 3f * k);
        if (reloadT > 0f) {
            float p = 1f - reloadT / W_RELOAD[wp];
            by += (int) ((float) Math.sin(p * 3.14159f) * gh * 0.8f);
        }
        if (shootCd > W_CD[wp] - 0.1f) {
            by -= (int) (k * (wp == 1 ? 7 : (wp == 2 ? 2 : 4)));
        }
        int x0 = (W - gw) / 2 + bx + (wp == 0 ? 0 : W / 20);
        int y0 = H - gh + by + (int) (3 * k);
        int L = gunLight();
        if (flashT > 0f) {
            Img f = Art.flash[wp];
            int fw = (int) (f.w * k), fh = (int) (f.h * k);
            blit(buf, f, x0 + gw / 2 - fw / 2, y0 - fh + (int) (6 * k), fw, fh, 256, 256, 256);
        }
        blit(buf, g, x0, y0, gw, gh, L + Math.max(0, curMuzzle), L + curMuzzle * 3 / 4, L + curMuzzle / 3);
    }

    /** Lumina de pe armă: din camera în care stai, plus lanterna. */
    int gunLight() {
        lightAt(px, py, tmpL);
        int L = (tmpL[0] + tmpL[1] + tmpL[2]) / 3 + (flashlight ? 60 : 0);
        return L < 110 ? 110 : (L > 270 ? 270 : L);
    }

    /**
     * Mâna (mâinile) cu arma randată 3D: legănat la mers, recul, reîncărcare și flacăra la gura țevii.
     * Pistolul stă jos-dreapta; armele lungi stau exact cum au fost randate în cadrul întreg.
     */
    private void drawGunHd(int[] buf, int wp) {
        Img g = gunHd[wp];
        float k = gunK[wp];
        float bob = (float) Math.sin(walkPhase * 0.5f);
        float abob = Math.abs(bob);
        int bx = (int) (bob * H * 0.015f);
        int by = (int) (abob * H * 0.0135f);
        if (reloadT > 0f) {
            float p = 1f - reloadT / W_RELOAD[wp];
            by += (int) ((float) Math.sin(p * 3.14159f) * g.h * 0.75f);
        }
        boolean recoil = shootCd > W_CD[wp] - 0.1f;
        if (recoil) {
            if (wp == 1) {
                by -= (int) (H * 0.045f);
                bx += (int) (W * 0.012f);
            } else if (wp == 2) {
                by -= (int) (H * 0.012f);
                bx += (int) (W * ((shots & 1) == 0 ? 0.004f : -0.002f));
            } else {
                by -= (int) (H * 0.027f);
                bx += (int) (W * 0.0045f);
            }
        }
        if (wp == 1 && pumpT > 0f && pumpT < 0.4f) {
            // pompa: arma coboară puțin și revine
            by += (int) ((float) Math.sin(pumpT / 0.4f * 3.14159f) * H * 0.03f);
        }
        int x0, y0;
        if (Art.handFH[wp] > 0) {
            x0 = W / 2 + (int) ((Art.handFX[wp] - Art.handFW[wp] * 0.5f) * k) + bx;
            y0 = (int) (Art.handFY[wp] * k) + by;
        } else {
            x0 = (int) (W * 0.535f) + bx;
            y0 = H - g.h + by + (int) (H * 0.02f);
        }
        int L = gunLight();
        int Lr = L, Lg = L, Lb = L;
        if (curMuzzle > 0) {
            Lr += curMuzzle;
            Lg += curMuzzle * 3 / 4;
            Lb += curMuzzle / 3;
        }
        if (flashT > 0f) {
            Img f = Art.flash[wp];
            float fk = H / 180f * (wp == 1 ? 0.95f : (wp == 2 ? 0.6f : 0.72f));
            int fw = (int) (f.w * fk), fh = (int) (f.h * fk);
            // centrul flăcării chiar în fața gurii țevii (calculată din modelul 3D)
            int tipX = x0 + (int) (Art.handTipX[wp] * k);
            int tipY = y0 + (int) (Art.handTipY[wp] * k);
            blit(buf, f, tipX - fw / 2 - fw / 10, tipY - fh / 2 - fh / 8, fw, fh, 256, 256, 256);
        }
        for (int y = 0; y < g.h; y++) {
            int yy = y0 + y;
            if (yy < 0 || yy >= H) {
                continue;
            }
            int o = yy * W;
            int so = y * g.w;
            for (int x = 0; x < g.w; x++) {
                int xx = x0 + x;
                if (xx < 0 || xx >= W) {
                    continue;
                }
                int c = g.px[so + x];
                int a = c >>> 24;
                if (a == 0) {
                    continue;
                }
                int r = ((((c >> 16) & 255) * Lr) >> 8);
                int gg = ((((c >> 8) & 255) * Lg) >> 8);
                int b = (((c & 255) * Lb) >> 8);
                int d = buf[o + xx];
                int ia = 255 - a;
                r += (((d >> 16) & 255) * ia) / 255;
                gg += (((d >> 8) & 255) * ia) / 255;
                b += ((d & 255) * ia) / 255;
                if (r > 255) {
                    r = 255;
                }
                if (gg > 255) {
                    gg = 255;
                }
                if (b > 255) {
                    b = 255;
                }
                buf[o + xx] = 0xFF000000 | (r << 16) | (gg << 8) | b;
            }
        }
    }

    /** Desenează imaginea întinsă (fără netezire) în dreptunghiul dat, cu lumina dată. */
    private void blit(int[] buf, Img src, int x0, int y0, int dw, int dh, int lr, int lg, int lb) {
        if (dw <= 0 || dh <= 0) {
            return;
        }
        for (int y = 0; y < dh; y++) {
            int yy = y0 + y;
            if (yy < 0 || yy >= H) {
                continue;
            }
            int sy = y * src.h / dh;
            int o = yy * W;
            for (int x = 0; x < dw; x++) {
                int xx = x0 + x;
                if (xx < 0 || xx >= W) {
                    continue;
                }
                int c = src.px[sy * src.w + x * src.w / dw];
                if (c != 0) {
                    buf[o + xx] = shadeCol(c, lr, lg, lb);
                }
            }
        }
    }
}
