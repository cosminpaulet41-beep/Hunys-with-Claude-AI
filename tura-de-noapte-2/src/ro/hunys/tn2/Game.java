package ro.hunys.tn2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Jocul văzut de jucător: meniuri, poveste, HUD, comenzile de pe ecran, harta, pauza.
 * Lumea se desenează în worldBuf (W x H, pixeli mari, mărită de s ori), interfața în ui (UW x UH,
 * mărită de u ori). Platforma (Android sau calculatorul) doar le întinde pe ecran fără netezire.
 */
public final class Game {
    // ------------------------------------------------------------------ stări
    public static final int ST_TITLE = 0, ST_SETTINGS = 1, ST_STORY = 2, ST_PLAY = 3, ST_PAUSE = 4, ST_MAP = 5;
    public static final int ST_NOTE = 6, ST_SCENE = 7, ST_RADIO = 8, ST_DEAD = 9, ST_WIN = 10, ST_STATS = 11;
    public static final int ST_CONTROLS = 12, ST_EPISODES = 13;

    /** Ce înălțime are lumea (în pixeli mari) la fiecare nivel de pixelare. */
    static final int[] PIXEL_ROWS = {270, 180, 135};
    static final float[] SENS = {0.0065f, 0.0105f, 0.016f};

    public final Platform plat;
    public World world;
    PixelFont big, small;

    // ecran
    public int sw, sh;
    public int s, W, H;
    public int u, UW, UH;
    public int[] worldBuf;
    public Gfx ui;
    /** Butoanele de pe ecran, desenate neted de platformă peste interfață. */
    public final Vec vec = new Vec();

    // setări
    public int difficulty = 1, pixel = 1, sens = 1, bright = 2;
    /** Câte episoade s-au terminat (0..3): cele deblocate se pot alege din meniul de episoade. */
    public int unlocked;
    public boolean sound = true, vibra = true, fps;

    public int state = ST_TITLE;
    int prevState;
    int page;
    String[] pages;
    int afterPages;
    String noteText, noteTitle;
    float scroll, scrollMax;
    int radioId = -1;
    float time, stateT;
    boolean hasSave;
    String toast;
    float toastT;
    float objT;
    int lastObjective = -1;
    float fpsVal;
    int fpsFrames;
    float fpsT;
    int menuSel = -1;

    // ------------------------------------------------------------------ atingeri
    static final int MAXT = 10;
    static final int R_NONE = 0, R_JOY = 1, R_LOOK = 2, R_FIRE = 3, R_BTN = 4, R_UI = 5;
    final int[] tid = new int[MAXT], trole = new int[MAXT], tbtn = new int[MAXT];
    final float[] tx0 = new float[MAXT], ty0 = new float[MAXT], tx = new float[MAXT], ty = new float[MAXT];
    final float[] tlx = new float[MAXT], tly = new float[MAXT];
    final boolean[] tmoved = new boolean[MAXT];
    float joyCX, joyCY, joyX, joyY;
    boolean joyOn, fireHeld;
    float pendingTurn;
    int lastTapX = -1, lastTapY = -1;

    // butoanele din joc
    static final int B_FIRE = 0, B_USE = 1, B_WEAPON = 2, B_RELOAD = 3, B_MAP = 4, B_PAUSE = 5;
    final int[] bx = new int[6], by = new int[6], br = new int[6];
    final boolean[] bdown = new boolean[6];
    int joyR, joyBaseX, joyBaseY;

    // butoanele meniurilor de pe ecranul curent
    final int[] mx = new int[16], my = new int[16], mw = new int[16], mh = new int[16], mact = new int[16];
    final String[] mlabel = new String[16];
    int nm;

    public Game(Platform p) {
        plat = p;
    }

    // ================================================================== pornire

    public void init(int screenW, int screenH) throws java.io.IOException {
        Art.load(plat);
        big = new PixelFont(plat.open("big.pxf"));
        small = new PixelFont(plat.open("small.pxf"));
        boolean en = plat.systemEnglish();
        byte[] st = plat.load("settings");
        if (st != null) {
            loadSettings(st);
        } else {
            Texts.set(en);
        }
        world = new World(plat);
        world.difficulty = difficulty;
        world.setBrightness(bright);
        world.load(Level.load(plat, World.LEVELS[0] + ".txt"));
        hasSave = plat.load("save") != null;
        resize(screenW, screenH);
        titleView();
        for (int i = 0; i < MAXT; i++) {
            tid[i] = -1;
        }
    }

    public void resize(int screenW, int screenH) {
        sw = screenW;
        sh = screenH;
        s = Math.max(1, Math.round(sh / (float) PIXEL_ROWS[pixel]));
        W = (sw + s - 1) / s;
        H = (sh + s - 1) / s;
        if ((H & 1) == 1) {
            H++;
        }
        worldBuf = new int[W * H];
        world.setView(W, H);
        u = Math.max(2, Math.round(sh / 270f));
        UW = (sw + u - 1) / u;
        UH = (sh + u - 1) / u;
        ui = new Gfx(UW, UH);
        layout();
    }

    void layout() {
        joyR = 30;
        joyBaseX = 62;
        joyBaseY = UH - 60;
        bx[B_FIRE] = UW - 62;
        by[B_FIRE] = UH - 62;
        br[B_FIRE] = 30;
        bx[B_USE] = UW - 132;
        by[B_USE] = UH - 34;
        br[B_USE] = 19;
        bx[B_RELOAD] = UW - 122;
        by[B_RELOAD] = UH - 88;
        br[B_RELOAD] = 16;
        bx[B_WEAPON] = UW - 62;
        by[B_WEAPON] = UH - 118;
        br[B_WEAPON] = 17;
        bx[B_PAUSE] = UW - 20;
        by[B_PAUSE] = 19;
        br[B_PAUSE] = 14;
        bx[B_MAP] = UW - 54;
        by[B_MAP] = 19;
        br[B_MAP] = 14;
    }

    /** Camera din meniul principal: holul spitalului, privit spre inscripție. */
    void titleView() {
        if (!world.lv.name.equals(World.LEVELS[0])) {
            try {
                world.load(Level.load(plat, World.LEVELS[0] + ".txt"));
            } catch (java.io.IOException e) {
                // rămâne harta curentă
            }
        }
        world.reset();
        world.px = 24.0f;
        world.py = 27.2f;
        world.setAngle((float) Math.toRadians(300));
        // în meniu holul e luminat; jocul începe fără curent
        world.power = true;
        world.applyPower();
    }

    // ================================================================== setări

    byte[] saveSettings() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        try {
            o.writeByte(1);
            o.writeByte(difficulty);
            o.writeByte(pixel);
            o.writeByte(sens);
            o.writeBoolean(sound);
            o.writeBoolean(vibra);
            o.writeBoolean(fps);
            o.writeBoolean(Texts.EN);
            o.writeByte(bright);
            o.writeByte(unlocked);
            o.flush();
        } catch (java.io.IOException e) {
            // nu se întâmplă în memorie
        }
        return bo.toByteArray();
    }

    void loadSettings(byte[] b) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
            in.readByte();
            difficulty = clamp(in.readByte(), 0, 2);
            pixel = clamp(in.readByte(), 0, 2);
            sens = clamp(in.readByte(), 0, 2);
            sound = in.readBoolean();
            vibra = in.readBoolean();
            fps = in.readBoolean();
            Texts.set(in.readBoolean());
            bright = clamp(in.readByte(), 0, 4);
            unlocked = clamp(in.readByte(), 0, 3);
        } catch (java.io.IOException e) {
            Texts.set(plat.systemEnglish());
        }
    }

    static int clamp(int v, int a, int b) {
        return v < a ? a : (v > b ? b : v);
    }

    void storeSettings() {
        plat.save("settings", saveSettings());
    }

    // ================================================================== joc

    public void newGame() {
        startEpisode(0, null);
    }

    /** Pornește episodul ep; carry = ce aduce jucătorul din episodul dinainte (sau null). */
    void startEpisode(int ep, int[] carry) {
        try {
            world.load(Level.load(plat, World.LEVELS[ep] + ".txt"));
        } catch (java.io.IOException e) {
            toast(Texts.LOAD_FAIL);
            return;
        }
        world.difficulty = difficulty;
        world.reset();
        if (carry != null) {
            world.applyCarry(carry);
        } else {
            world.startGear();
        }
        lastObjective = -1;
        showPages(Texts.STORY[ep], ST_PLAY);
    }

    void showPages(String[] p, int after) {
        pages = p;
        page = 0;
        afterPages = after;
        setState(ST_STORY);
    }

    void setState(int st) {
        prevState = state;
        state = st;
        stateT = 0f;
        scroll = 0f;
        releaseAll();
        if (st == ST_PLAY) {
            world.resumeGrace();
        }
    }

    void releaseAll() {
        for (int i = 0; i < MAXT; i++) {
            tid[i] = -1;
            trole[i] = R_NONE;
        }
        joyOn = false;
        joyX = 0f;
        joyY = 0f;
        fireHeld = false;
        pendingTurn = 0f;
        for (int i = 0; i < bdown.length; i++) {
            bdown[i] = false;
        }
    }

    boolean saveGame() {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream o = new DataOutputStream(bo);
            world.writeState(o);
            o.flush();
            plat.save("save", bo.toByteArray());
            hasSave = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    boolean loadGame() {
        byte[] b = plat.load("save");
        if (b == null) {
            return false;
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
            String lvName = World.peekLevel(in);
            if (!lvName.equals(world.lv.name)) {
                world.load(Level.load(plat, lvName + ".txt"));
            }
            world.readState(in);
            difficulty = world.difficulty;
            lastObjective = world.objective;
            return true;
        } catch (Exception e) {
            world.reset();
            return false;
        }
    }

    // ================================================================== pas de timp

    public void update(float dt) {
        if (dt > 0.05f) {
            dt = 0.05f;
        }
        time += dt;
        stateT += dt;
        if (toastT > 0f) {
            toastT -= dt;
        }
        fpsFrames++;
        fpsT += dt;
        if (fpsT >= 0.5f) {
            fpsVal = fpsFrames / fpsT;
            fpsFrames = 0;
            fpsT = 0f;
        }
        if (state == ST_TITLE || state == ST_SETTINGS || state == ST_CONTROLS || state == ST_EPISODES) {
            if (prevStateIsTitle()) {
                world.setAngle(world.ang + dt * 0.06f * (float) Math.sin(time * 0.15f));
                world.updateFlicker(dt);
            }
        } else if (state == ST_PLAY) {
            float turn = pendingTurn;
            pendingTurn = 0f;
            float f = joyOn ? -joyY : 0f, sd = joyOn ? joyX : 0f;
            world.update(dt, f, sd, turn, fireHeld);
            if (world.objective != lastObjective) {
                lastObjective = world.objective;
                objT = 5f;
            }
            if (objT > 0f) {
                objT -= dt;
            }
            playSounds();
            if (world.dead) {
                if (vibra) {
                    plat.vibrate(300);
                }
                setState(ST_DEAD);
            } else if (world.won) {
                if (world.ep + 1 > unlocked) {
                    unlocked = world.ep + 1;
                    storeSettings();
                }
                showPages(Texts.WIN[world.ep], ST_STATS);
            } else if (world.pendingNote >= 0) {
                int n = world.pendingNote;
                world.pendingNote = -1;
                int e = world.ep;
                n = Math.max(0, Math.min(n, Texts.NOTES[e].length - 1));
                noteTitle = Texts.NOTE_TITLES[e][n];
                noteText = Texts.NOTES[e][n];
                setState(ST_NOTE);
            } else if (world.pendingScene >= 0) {
                int sc = world.pendingScene;
                world.pendingScene = -1;
                String t;
                if (sc == World.SCENE_ELENA) {
                    t = Texts.SCENE_ELENA;
                    world.radio(World.R_NPC);
                } else if (sc == World.SCENE_CRISTI) {
                    t = Texts.SCENE_CRISTI;
                    world.radio(World.R_NPC);
                } else if (sc == World.SCENE_CREATURE) {
                    t = Texts.SCENE_CREATURE;
                    world.radio(World.R_LIFT);
                } else if (sc == World.SCENE_IOANA) {
                    t = Texts.SCENE_IOANA;
                } else {
                    t = Texts.scene(world.lv.rid[sc]);
                }
                if (t != null) {
                    noteText = t;
                    noteTitle = null;
                    setState(ST_SCENE);
                }
            } else if (world.peekRadio() >= 0) {
                radioId = world.popRadio();
                setState(ST_RADIO);
            }
        } else {
            playSounds();
        }
    }

    boolean prevStateIsTitle() {
        return state == ST_TITLE || state == ST_EPISODES || prevState == ST_TITLE || prevState == ST_SETTINGS
                || prevState == ST_EPISODES;
    }

    int lastHurt;

    void playSounds() {
        int b = world.sfx;
        world.sfx = 0;
        if (world.hurtCount != lastHurt) {
            lastHurt = world.hurtCount;
            if (vibra) {
                plat.vibrate(60);
            }
        }
        if (!sound || b == 0) {
            return;
        }
        for (int i = 0; i < 23; i++) {
            if ((b & (1 << i)) != 0) {
                plat.sound(i, 1f);
            }
        }
    }

    // ================================================================== atingeri

    /** Acțiuni: 0 = apăsare, 1 = mișcare, 2 = ridicare (x, y în pixeli de ecran). */
    public void touch(int action, int id, float x, float y) {
        float ux = x / u, uy = y / u;
        if (action == 0) {
            int slot = -1;
            for (int i = 0; i < MAXT; i++) {
                if (tid[i] < 0) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) {
                return;
            }
            tid[slot] = id;
            tx0[slot] = ux;
            ty0[slot] = uy;
            tx[slot] = ux;
            ty[slot] = uy;
            tlx[slot] = ux;
            tly[slot] = uy;
            tmoved[slot] = false;
            trole[slot] = R_UI;
            if (state == ST_PLAY) {
                downPlay(slot, ux, uy);
            } else {
                menuSel = hitMenu((int) ux, (int) uy);
            }
            return;
        }
        int slot = -1;
        for (int i = 0; i < MAXT; i++) {
            if (tid[i] == id) {
                slot = i;
            }
        }
        if (slot < 0) {
            return;
        }
        if (action == 1) {
            float dx = ux - tlx[slot], dy = uy - tly[slot];
            tlx[slot] = ux;
            tly[slot] = uy;
            tx[slot] = ux;
            ty[slot] = uy;
            if (Math.abs(ux - tx0[slot]) + Math.abs(uy - ty0[slot]) > 6f) {
                tmoved[slot] = true;
            }
            int r = trole[slot];
            if (r == R_JOY) {
                float jx = ux - joyCX, jy = uy - joyCY;
                float d = (float) Math.sqrt(jx * jx + jy * jy);
                float max = joyR;
                if (d > max) {
                    jx *= max / d;
                    jy *= max / d;
                }
                joyX = jx / max;
                joyY = jy / max;
            } else if (r == R_LOOK || r == R_FIRE) {
                pendingTurn += dx * SENS[sens] * (r == R_FIRE ? 0.8f : 1f);
            } else if (r == R_UI && (state == ST_NOTE || state == ST_SCENE || state == ST_STORY || state == ST_CONTROLS)) {
                scroll -= dy;
                if (scroll < 0f) {
                    scroll = 0f;
                }
                if (scroll > scrollMax) {
                    scroll = scrollMax;
                }
            }
            return;
        }
        // ridicare
        int r = trole[slot];
        tid[slot] = -1;
        trole[slot] = R_NONE;
        if (r == R_JOY) {
            joyOn = false;
            joyX = 0f;
            joyY = 0f;
        } else if (r == R_FIRE) {
            fireHeld = false;
            bdown[B_FIRE] = false;
        } else if (r == R_BTN) {
            bdown[tbtn[slot]] = false;
            int b = tbtn[slot];
            if ((b == B_PAUSE || b == B_MAP) && dist(ux, uy, bx[b], by[b]) < br[b] + 10) {
                if (b == B_PAUSE) {
                    setState(ST_PAUSE);
                } else {
                    setState(ST_MAP);
                }
            }
        } else if (r == R_UI) {
            boolean tap = !tmoved[slot];
            int sel = hitMenu((int) ux, (int) uy);
            if (sel >= 0 && sel == menuSel) {
                menuAction(mact[sel]);
            } else if (tap && sel < 0) {
                tapScreen();
            }
            menuSel = -1;
        }
    }

    static float dist(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0, dy = y1 - y0;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    void downPlay(int slot, float ux, float uy) {
        // butoanele (puțin mai mari decât desenul, ca să le nimerești ușor)
        for (int b = 0; b < 6; b++) {
            if (dist(ux, uy, bx[b], by[b]) <= br[b] + 5) {
                if (b == B_FIRE) {
                    trole[slot] = R_FIRE;
                    fireHeld = true;
                    bdown[B_FIRE] = true;
                    world.fire();
                    return;
                }
                trole[slot] = R_BTN;
                tbtn[slot] = b;
                bdown[b] = true;
                if (b == B_USE) {
                    world.useAhead(true);
                } else if (b == B_RELOAD) {
                    world.reload();
                } else if (b == B_WEAPON) {
                    world.switchWeapon();
                }
                return;
            }
        }
        if (ux < UW * 0.42f && uy > UH * 0.30f && !joyOn) {
            trole[slot] = R_JOY;
            joyOn = true;
            joyCX = Math.max(joyR + 6, Math.min(ux, UW * 0.42f - joyR));
            joyCY = Math.max(UH * 0.30f + joyR, Math.min(uy, UH - joyR - 6));
            joyX = 0f;
            joyY = 0f;
            touch(1, tid[slot], ux * u, uy * u);
            return;
        }
        trole[slot] = R_LOOK;
    }

    /** Atingere oriunde (fără buton) în ecranele de text. */
    void tapScreen() {
        if (state == ST_STORY) {
            page++;
            scroll = 0f;
            if (page >= pages.length) {
                if (afterPages == ST_PLAY) {
                    setState(ST_PLAY);
                    if (world.playTime == 0f) {
                        world.radio(World.R_START);
                        saveGame();
                    }
                } else if (afterPages == ST_STATS) {
                    setState(ST_STATS);
                } else {
                    setState(afterPages);
                }
            }
        } else if (state == ST_NOTE || state == ST_SCENE || state == ST_MAP) {
            setState(ST_PLAY);
        } else if (state == ST_RADIO) {
            radioId = -1;
            setState(ST_PLAY);
        } else if (state == ST_CONTROLS) {
            setState(prevState == ST_PAUSE ? ST_PAUSE : ST_TITLE);
        }
    }

    /** Aplicația trece în fundal: jocul intră în pauză și se uită atingerile în curs. */
    public void pause() {
        if (state == ST_PLAY) {
            setState(ST_PAUSE);
        } else {
            releaseAll();
        }
    }

    public void back() {
        if (state == ST_PLAY) {
            setState(ST_PAUSE);
        } else if (state == ST_PAUSE) {
            setState(ST_PLAY);
        } else if (state == ST_SETTINGS || state == ST_CONTROLS) {
            setState(prevState == ST_PAUSE ? ST_PAUSE : ST_TITLE);
        } else if (state == ST_EPISODES) {
            setState(ST_TITLE);
        } else if (state == ST_TITLE) {
            plat.quit();
        } else {
            tapScreen();
        }
    }

    int hitMenu(int x, int y) {
        for (int i = 0; i < nm; i++) {
            if (x >= mx[i] - 4 && x < mx[i] + mw[i] + 4 && y >= my[i] - 3 && y < my[i] + mh[i] + 3) {
                return i;
            }
        }
        return -1;
    }

    static final int A_CONTINUE = 1, A_NEW = 2, A_SETTINGS = 3, A_EXIT = 4, A_BACK = 5, A_RESUME = 6, A_SAVE = 7;
    static final int A_MAIN = 8, A_DIFF = 9, A_PIXEL = 10, A_SENS = 11, A_SOUND = 12, A_VIBRA = 13, A_LANG = 14;
    static final int A_FPS = 15, A_LOAD = 16, A_RETRY = 17, A_CONTROLS = 18, A_STATS_DONE = 19;
    static final int A_BRIGHT = 20, A_NEXT = 21, A_EPISODES = 22, A_EP0 = 30;

    void menuAction(int a) {
        if (sound) {
            plat.sound(23, 0.6f);
        }
        switch (a) {
            case A_CONTINUE:
                if (loadGame()) {
                    setState(ST_PLAY);
                    toast(Texts.LOADED);
                } else {
                    toast(Texts.LOAD_FAIL);
                }
                break;
            case A_NEW:
                newGame();
                break;
            case A_SETTINGS:
                prevState = state;
                state = ST_SETTINGS;
                stateT = 0f;
                break;
            case A_CONTROLS:
                prevState = state;
                state = ST_CONTROLS;
                stateT = 0f;
                break;
            case A_EXIT:
                plat.quit();
                break;
            case A_BACK:
                state = prevState == ST_PAUSE ? ST_PAUSE : ST_TITLE;
                storeSettings();
                break;
            case A_RESUME:
                setState(ST_PLAY);
                break;
            case A_SAVE:
                toast(saveGame() ? Texts.SAVED : Texts.SAVE_FAIL);
                break;
            case A_MAIN:
                titleView();
                hasSave = plat.load("save") != null;
                setState(ST_TITLE);
                break;
            case A_DIFF:
                difficulty = (difficulty + 1) % 3;
                if (prevState != ST_PAUSE) {
                    world.difficulty = difficulty;
                }
                break;
            case A_PIXEL:
                pixel = (pixel + 1) % 3;
                resize(sw, sh);
                break;
            case A_SENS:
                sens = (sens + 1) % 3;
                break;
            case A_SOUND:
                sound = !sound;
                break;
            case A_VIBRA:
                vibra = !vibra;
                break;
            case A_LANG:
                Texts.set(!Texts.EN);
                break;
            case A_FPS:
                fps = !fps;
                break;
            case A_LOAD:
                if (loadGame()) {
                    setState(ST_PLAY);
                } else {
                    world.reset();
                    setState(ST_PLAY);
                }
                break;
            case A_RETRY:
                if (!loadGame()) {
                    world.reset();
                }
                lastObjective = -1;
                setState(ST_PLAY);
                break;
            case A_STATS_DONE:
                titleView();
                hasSave = plat.load("save") != null;
                setState(ST_TITLE);
                break;
            case A_NEXT:
                if (world.ep + 1 < World.LEVELS.length) {
                    startEpisode(world.ep + 1, world.carry());
                }
                break;
            case A_BRIGHT:
                bright = (bright + 1) % 5;
                world.setBrightness(bright);
                break;
            case A_EPISODES:
                setState(ST_EPISODES);
                break;
            case A_EP0:
            case A_EP0 + 1:
            case A_EP0 + 2:
                if (a - A_EP0 <= unlocked) {
                    startEpisode(a - A_EP0, null);
                } else {
                    toast(Texts.LOCKED_EP);
                }
                break;
            default:
                break;
        }
        storeSettings();
    }

    void toast(String t) {
        toast = t;
        toastT = 2.2f;
    }

    // ================================================================== desen

    static final int C_TEXT = 0xFFEDE6D6, C_DIM = 0xFFA79F90, C_TITLE = 0xFFF0922E, C_RED = 0xFFD8352E;
    static final int C_PANEL = 0xD0101014, C_BORDER = 0xFF5A5650, C_HI = 0xFFF0922E;

    /** Desenează cadrul curent în worldBuf și ui. */
    public void render() {
        Gfx g = ui;
        g.clear(0);
        vec.clear();
        nm = 0;
        boolean worldVisible = state != ST_STORY && state != ST_STATS;
        if (worldVisible) {
            world.render(worldBuf, state != ST_TITLE && state != ST_SETTINGS && state != ST_CONTROLS
                    && state != ST_EPISODES);
        }
        switch (state) {
            case ST_TITLE:
                drawTitle(g);
                break;
            case ST_SETTINGS:
                drawSettings(g);
                break;
            case ST_CONTROLS:
                drawControlsHelp(g);
                break;
            case ST_EPISODES:
                drawEpisodes(g);
                break;
            case ST_STORY:
                drawStory(g);
                break;
            case ST_PLAY:
                drawHud(g);
                drawControls(g);
                break;
            case ST_PAUSE:
                drawHud(g);
                drawPause(g);
                break;
            case ST_MAP:
                drawMap(g);
                break;
            case ST_NOTE:
            case ST_SCENE:
                drawHud(g);
                drawNote(g);
                break;
            case ST_RADIO:
                drawHud(g);
                drawRadio(g);
                break;
            case ST_DEAD:
                drawDead(g);
                break;
            case ST_STATS:
                drawStats(g);
                break;
            default:
                break;
        }
        if (toastT > 0f && toast != null) {
            int tw = big.width(toast) + 16;
            g.panel((UW - tw) / 2, UH / 2 - 14, tw, 26, 0xE0101014, C_HI);
            big.draw(g, toast, (UW - tw) / 2 + 8, UH / 2 - 11, C_TEXT, 1);
        }
        if (fps) {
            String f = (int) (fpsVal + 0.5f) + " FPS";
            small.drawShadow(g, f, UW - small.width(f) - (state == ST_PLAY ? 80 : 6), 4, 0xFF7CFC7C, 1);
        }
    }

    void button(Gfx g, String label, int x, int y, int w, int h, int action, boolean hilite) {
        if (nm < mx.length) {
            mx[nm] = x;
            my[nm] = y;
            mw[nm] = w;
            mh[nm] = h;
            mact[nm] = action;
            mlabel[nm] = label;
            nm++;
        }
        boolean pressed = menuSel == nm - 1;
        g.panel(x, y, w, h, pressed ? 0xE0503018 : 0xC0141418, hilite || pressed ? C_HI : C_BORDER);
        int tw = big.width(label);
        big.draw(g, label, x + (w - tw) / 2, y + (h - big.lineH) / 2 + 1, pressed ? 0xFFFFFFFF : C_TEXT, 1);
    }

    void darken(Gfx g, int a) {
        g.fill(0, 0, UW, UH, a << 24);
    }

    void vignette(Gfx g, int rgb, int strength) {
        int bands = 18;
        for (int k = 0; k < bands; k++) {
            int a = strength * (bands - k) * (bands - k) / (bands * bands);
            int col = Gfx.alpha(rgb, a);
            int t = 2;
            g.fill(0, k * t, UW, t, col);
            g.fill(0, UH - (k + 1) * t, UW, t, col);
            g.fill(k * t, bands * t, t, UH - 2 * bands * t, col);
            g.fill(UW - (k + 1) * t, bands * t, t, UH - 2 * bands * t, col);
        }
    }

    // ------------------------------------------------------------------ meniul principal
    void drawTitle(Gfx g) {
        // meniul stă în stânga; în dreapta se vede holul cu sigla
        for (int x = 0; x < UW * 6 / 10; x++) {
            int a = 215 - x * 215 / (UW * 6 / 10);
            g.fill(x, 0, 1, UH, a << 24);
        }
        vignette(g, 0x000000, 150);
        int lx = 26;
        String t = Texts.TITLE;
        int sc = 2;
        int tw = big.width(t) * sc;
        int y0 = 24;
        big.draw(g, t, lx + 2, y0 + 2, 0xC0000000, sc);
        big.draw(g, t, lx, y0, C_TEXT, sc);
        big.draw(g, Texts.TITLE2, lx + tw + 10 + 2, y0 + 2, 0xC0000000, sc);
        big.draw(g, Texts.TITLE2, lx + tw + 10, y0, C_RED, sc);
        int sy = y0 + big.lineH * sc - 2;
        String st = Texts.SUBTITLE.toUpperCase();
        small.drawShadow(g, st, lx + 2, sy, C_TITLE, 2);
        g.fill(lx + 2 + small.width(st) * 2 + 8, sy + 7, 60, 1, 0x80F0922E);
        int by0 = sy + 34;
        int bw = 170, bh = 24, gap = 6;
        int n = 0;
        if (hasSave) {
            button(g, Texts.M_CONTINUE, lx, by0 + n++ * (bh + gap), bw, bh, A_CONTINUE, true);
        }
        button(g, Texts.M_NEW, lx, by0 + n++ * (bh + gap), bw, bh, A_NEW, !hasSave);
        if (unlocked > 0) {
            button(g, Texts.M_EPISODES, lx, by0 + n++ * (bh + gap), bw, bh, A_EPISODES, false);
        }
        button(g, Texts.M_SETTINGS, lx, by0 + n++ * (bh + gap), bw, bh, A_SETTINGS, false);
        button(g, Texts.M_EXIT, lx, by0 + n * (bh + gap), bw, bh, A_EXIT, false);
        small.drawShadow(g, Texts.PLACE, lx, UH - 14, C_DIM, 1);
        String a = Texts.AUTHOR;
        small.drawShadow(g, a, UW - small.width(a) - 8, UH - 14, C_DIM, 1);
    }

    // ------------------------------------------------------------------ setări
    void drawSettings(Gfx g) {
        darken(g, 170);
        int cx = UW / 2;
        String t = Texts.M_SETTINGS.toUpperCase();
        big.drawShadow(g, t, cx - big.width(t) / 2, 12, C_TITLE, 1);
        String[][] rows = {
            {Texts.S_DIFF, Texts.DIFF_NAMES[difficulty]},
            {Texts.S_BRIGHT, Texts.BRIGHT_NAMES[bright]},
            {Texts.S_PIXEL, Texts.PIXEL_NAMES[pixel] + "  (" + W + "x" + H + ")"},
            {Texts.S_SENS, Texts.SENS_NAMES[sens]},
            {Texts.S_SOUND, sound ? Texts.ON : Texts.OFF},
            {Texts.S_VIBRA, vibra ? Texts.ON : Texts.OFF},
            {Texts.S_LANG, Texts.LANG_NAME},
            {Texts.S_FPS, fps ? Texts.ON : Texts.OFF},
        };
        int[] acts = {A_DIFF, A_BRIGHT, A_PIXEL, A_SENS, A_SOUND, A_VIBRA, A_LANG, A_FPS};
        int bw = 300, bh = 21, gap = 3;
        int y = 34;
        for (int i = 0; i < rows.length; i++) {
            int x = cx - bw / 2;
            if (nm < mx.length) {
                mx[nm] = x;
                my[nm] = y;
                mw[nm] = bw;
                mh[nm] = bh;
                mact[nm] = acts[i];
                nm++;
            }
            boolean pressed = menuSel == nm - 1;
            g.panel(x, y, bw, bh, pressed ? 0xE0503018 : 0xC0141418, C_BORDER);
            big.draw(g, rows[i][0], x + 8, y + 1, C_DIM, 1);
            String v = rows[i][1];
            big.draw(g, v, x + bw - 8 - big.width(v), y + 1, C_TEXT, 1);
            y += bh + gap;
        }
        button(g, Texts.M_CONTROLS, cx - bw / 2, y + 4, bw / 2 - 3, bh, A_CONTROLS, false);
        button(g, Texts.M_BACK, cx + 3, y + 4, bw / 2 - 3, bh, A_BACK, true);
    }

    /** Alegerea episodului (cele terminate și următorul). */
    void drawEpisodes(Gfx g) {
        darken(g, 170);
        int cx = UW / 2;
        String t = Texts.M_EPISODES.toUpperCase();
        big.drawShadow(g, t, cx - big.width(t) / 2, 20, C_TITLE, 1);
        int bw = 280, bh = 26, gap = 8, y = 56;
        for (int e = 0; e < World.LEVELS.length; e++) {
            boolean open = e <= unlocked;
            button(g, open ? Texts.EP_NAMES[e] : "? ? ?", cx - bw / 2, y, bw, bh, A_EP0 + e, open && e == Math.min(unlocked, 2));
            y += bh + gap;
        }
        small.drawShadow(g, Texts.DIFF_NAMES[difficulty], cx - small.width(Texts.DIFF_NAMES[difficulty]) / 2, y + 2, C_DIM, 1);
        button(g, Texts.M_BACK, cx - 60, UH - 40, 120, 24, A_STATS_DONE, false);
    }

    void drawControlsHelp(Gfx g) {
        darken(g, 200);
        int cx = UW / 2;
        String t = Texts.M_CONTROLS.toUpperCase();
        big.drawShadow(g, t, cx - big.width(t) / 2, 12, C_TITLE, 1);
        int y = 44;
        for (int i = 0; i < Texts.CONTROLS.length; i++) {
            big.draw(g, Texts.CONTROLS[i], 30, y, C_TEXT, 1);
            y += big.lineH + 4;
        }
        String h = Texts.TAP_CLOSE;
        small.drawShadow(g, h, cx - small.width(h) / 2, UH - 14, C_DIM, 1);
    }

    // ------------------------------------------------------------------ pagini de poveste
    void drawStory(Gfx g) {
        g.clear(0xFF050506);
        String text = pages[page];
        drawRichText(g, text, 40, 26, UW - 80, UH - 48);
        String h = Texts.TAP_CONTINUE + "  ·  " + (page + 1) + "/" + pages.length;
        int a = (int) (140 + 100 * Math.sin(time * 3f));
        small.draw(g, h, UW / 2 - small.width(h) / 2, UH - 16, Gfx.alpha(0xA79F90, a), 1);
    }

    /** Text cu titluri (#) și paragrafe, încadrat și derulabil în dreptunghiul dat. */
    int drawRichText(Gfx g, String text, int x, int y, int w, int h) {
        String[] lines = big.wrap(text, w, 1);
        int lh = big.lineH + 1;
        int total = 0;
        for (int i = 0; i < lines.length; i++) {
            total += lines[i].length() == 0 ? lh / 2 : lh;
        }
        scrollMax = Math.max(0, total - h);
        g.clip(x, y, w, h);
        int cy = y - (int) scroll;
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i];
            if (ln.length() == 0) {
                cy += lh / 2;
                continue;
            }
            boolean title = ln.startsWith("#");
            if (title) {
                ln = ln.substring(1);
            }
            big.draw(g, ln, x, cy, title ? C_TITLE : C_TEXT, 1);
            cy += lh;
        }
        g.noClip();
        if (scrollMax > 0) {
            int bh = h * h / (total);
            int byy = y + (int) ((h - bh) * scroll / scrollMax);
            g.fill(x + w + 6, y, 2, h, 0x40FFFFFF);
            g.fill(x + w + 6, byy, 2, bh, 0xC0F0922E);
        }
        return total;
    }

    // ------------------------------------------------------------------ HUD
    static final String[] IC_HEART = {
        ".##.##.",
        "#######",
        "#######",
        ".#####.",
        "..###..",
        "...#...",
    };

    void drawHud(Gfx g) {
        World w = world;
        // rănit / viață puțină
        if (w.hurtT > 0f) {
            g.fill(0, 0, UW, UH, Gfx.alpha(0xB00000, (int) (w.hurtT / 0.35f * 90)));
        }
        if (w.health <= 30) {
            int a = (int) (110 + 60 * Math.sin(time * 6f));
            vignette(g, 0x900000, a);
        }
        // viața
        int x = 8, y = 8;
        g.panel(x, y, 118, 17, 0xA0101014, 0xFF3A3632);
        g.icon(IC_HEART, x + 5, y + 5, 1, 0xFFD8352E);
        int bwid = 70;
        g.fill(x + 15, y + 6, bwid + 2, 6, 0xFF201010);
        int hp = Math.max(0, w.health);
        int fill = bwid * hp / 100;
        int col = hp > 60 ? 0xFF48B848 : (hp > 30 ? 0xFFE0B030 : 0xFFE03028);
        g.fill(x + 16, y + 7, fill, 4, col);
        g.fill(x + 16, y + 7, fill, 1, Gfx.alpha(0xFFFFFF, 90));
        String hs = String.valueOf(hp);
        small.draw(g, hs, x + 113 - small.width(hs), y + 5, C_TEXT, 1);
        // cardurile
        int kx = x;
        int ky = y + 20;
        if (w.keyBlue) {
            kx = drawCard(g, kx, ky, 0xFF3A78E8);
        }
        if (w.keyRed) {
            kx = drawCard(g, kx, ky, 0xFFE03A3A);
        }
        if (w.keyYellow) {
            kx = drawCard(g, kx, ky, 0xFFE8C030);
        }
        if (w.keyGreen) {
            drawCard(g, kx, ky, 0xFF3AB860);
        }
        // obiectivul
        if (objT > 0f || state == ST_PAUSE) {
            String[] ob = Texts.OBJ[w.ep];
            String o = ob[Math.min(w.objective, ob.length - 1)];
            int a = state == ST_PAUSE ? 255 : (int) (255 * Math.min(1f, objT));
            String lab = Texts.OBJ_LABEL.toUpperCase();
            int tw = Math.max(big.width(o), small.width(lab)) + 16;
            int ox = (UW - tw) / 2;
            g.fill(ox, 6, tw, 32, Gfx.alpha(0x101014, a * 170 / 255));
            small.draw(g, lab, (UW - small.width(lab)) / 2, 8, Gfx.alpha(0xF0922E, a), 1);
            big.draw(g, o, (UW - big.width(o)) / 2, 17, Gfx.alpha(0xEDE6D6, a), 1);
        }
        // mesaje
        if (w.msg != null && w.msgT > 0f) {
            int a = (int) (255 * Math.min(1f, w.msgT * 2f));
            int tw = big.width(w.msg) + 14;
            int my0 = objT > 0f ? 44 : 10;
            g.fill((UW - tw) / 2, my0, tw, 22, Gfx.alpha(0x101014, a * 150 / 255));
            big.draw(g, w.msg, (UW - tw) / 2 + 7, my0 + 1, Gfx.alpha(0xEDE6D6, a), 1);
        }
        if (w.reloadT > 0f) {
            String r = Texts.RELOADING;
            small.drawShadow(g, r, UW / 2 - small.width(r) / 2, UH / 2 + 14, C_HI, 1);
        }
        // ținta
        int cx = UW / 2, cy = UH / 2;
        int cc = 0xB0FFFFFF;
        g.fill(cx - 5, cy, 3, 1, cc);
        g.fill(cx + 3, cy, 3, 1, cc);
        g.fill(cx, cy - 5, 1, 3, cc);
        g.fill(cx, cy + 3, 1, 3, cc);
    }

    int drawCard(Gfx g, int x, int y, int col) {
        g.panel(x, y, 16, 11, col, 0xFF101010);
        g.fill(x + 2, y + 2, 5, 2, 0xC0FFFFFF);
        g.fill(x + 2, y + 6, 12, 1, 0x60000000);
        return x + 19;
    }

    // ------------------------------------------------------------------ comenzile de pe ecran
    static final String[] IC_HAND = {
        "...##.....",
        "...#.#....",
        "...#.#....",
        "...#.####.",
        ".#.#.#.#.#",
        "#.##.#.#.#",
        "#..#.....#",
        ".#.......#",
        "..#......#",
        "...#....#.",
        "....####..",
    };
    static final String[] IC_RELOAD = {
        "...####..",
        "..#....#.",
        ".#......#",
        ".#.....###",
        ".#......#.",
        "###.......",
        ".#......#.",
        "..#....#..",
        "...####...",
    };
    static final String[] IC_PAUSE = {
        "##..##",
        "##..##",
        "##..##",
        "##..##",
        "##..##",
        "##..##",
        "##..##",
    };
    static final String[] IC_MAP = {
        "##...##...",
        "#.##.#.##.",
        "#..##...##",
        "#...#...#.",
        "#...#...#.",
        "#...#...#.",
        "##..##..#.",
        ".##.#.###.",
        "...##...#.",
    };
    static final String[] IC_PISTOL = {
        "###########.",
        "############",
        "###########.",
        "....##.##...",
        "....##.##...",
        "....###.....",
        ".....##.....",
    };
    static final String[] IC_SHOTGUN = {
        "...............##..",
        "###################",
        "##################.",
        "..####...#####.....",
        "..####.............",
        "...###.............",
    };
    static final String[] IC_SMG = {
        "##############.",
        "###############",
        "..####..####...",
        "..##.###.......",
        "..##..##.......",
        "......##.......",
    };
    static final String[] IC_CROSS = {
        "....#....",
        "....#....",
        "..#####..",
        ".#..#..#.",
        "#####.###",
        ".#..#..#.",
        "..#####..",
        "....#....",
        "....#....",
    };

    // ---- butoanele, ca forme vectoriale netede (vec), în pixeli de ecran

    static final int V_FILL = 0x46000000, V_FILL_ON = 0x8CF0922E, V_LINE = 0xB4FFFFFF, V_LINE_ON = 0xF0FFC890;
    static final int V_ICON = 0xEBFFFFFF;

    /** Centrul și raza unui buton, în pixeli de ecran. */
    float sx(int b) {
        return bx[b] * u;
    }

    float sy(int b) {
        return by[b] * u;
    }

    float sr(int b) {
        return br[b] * u;
    }

    void vecButton(int b) {
        boolean d = bdown[b];
        float r = sr(b);
        vec.circle(sx(b), sy(b), r, d ? V_FILL_ON : V_FILL);
        vec.ring(sx(b), sy(b), r - r * 0.035f, r * 0.07f, d ? V_LINE_ON : V_LINE);
    }

    /** Poligon dat în unități de rază, față de centrul (cx, cy). */
    void vecPoly(float cx, float cy, float r, float[] pts, int col) {
        float[] xy = new float[pts.length];
        for (int i = 0; i < pts.length; i += 2) {
            xy[i] = cx + pts[i] * r;
            xy[i + 1] = cy + pts[i + 1] * r;
        }
        vec.poly(xy, col);
    }

    static final float[] P_PISTOL = {-0.64f, -0.32f, 0.64f, -0.32f, 0.64f, -0.06f, 0.58f, 0.03f, 0.10f, 0.03f,
        0.05f, 0.16f, -0.17f, 0.16f, -0.21f, 0.03f, -0.27f, 0.03f, -0.37f, 0.58f, -0.68f, 0.58f, -0.57f, 0.03f,
        -0.64f, -0.06f};
    static final float[] P_SHOTGUN = {-0.78f, -0.13f, 0.84f, -0.13f, 0.84f, -0.02f, 0.22f, -0.02f, 0.22f, 0.07f,
        -0.04f, 0.07f, -0.04f, 0.02f, -0.30f, 0.02f, -0.74f, 0.26f, -0.84f, 0.14f, -0.46f, -0.03f};
    static final float[] P_SMG = {-0.58f, -0.22f, 0.56f, -0.22f, 0.56f, -0.08f, 0.74f, -0.08f, 0.74f, 0.0f, 0.12f, 0.0f,
        0.12f, 0.46f, -0.02f, 0.46f, -0.02f, 0.0f, -0.18f, 0.0f, -0.25f, 0.34f, -0.42f, 0.34f, -0.35f, 0.0f, -0.58f, -0.02f};
    static final float[] P_THUMB = {-0.36f, 0.30f, -0.60f, -0.02f, -0.50f, -0.12f, -0.24f, 0.08f};

    void drawControls(Gfx g) {
        World w = world;
        // joystick
        float jx = (joyOn ? joyCX : joyBaseX) * u, jy = (joyOn ? joyCY : joyBaseY) * u, jr = joyR * u;
        vec.circle(jx, jy, jr, joyOn ? 0x30FFFFFF : 0x30000000);
        vec.ring(jx, jy, jr - jr * 0.03f, jr * 0.06f, joyOn ? 0xC8FFFFFF : 0x96FFFFFF);
        for (int k = 0; k < 4; k++) {
            float ang = (float) (k * Math.PI / 2);
            float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
            float tx0 = jx + ca * jr * 0.80f, ty0 = jy + sa * jr * 0.80f;
            float bxx = jx + ca * jr * 0.66f, byy = jy + sa * jr * 0.66f;
            vec.poly(new float[] {tx0, ty0, bxx - sa * jr * 0.09f, byy + ca * jr * 0.09f, bxx + sa * jr * 0.09f,
                byy - ca * jr * 0.09f}, 0x8CFFFFFF);
        }
        float kx = jx + joyX * jr, ky = jy + joyY * jr, kr = jr * 0.40f;
        vec.circle(kx, ky, kr, joyOn ? 0xC8F0922E : 0x8CFFFFFF);
        vec.ring(kx, ky, kr - kr * 0.05f, kr * 0.10f, 0xE6FFFFFF);
        // FOC: țintă
        vecButton(B_FIRE);
        float fx = sx(B_FIRE), fy = sy(B_FIRE) - sr(B_FIRE) * 0.10f, fr = sr(B_FIRE);
        vec.ring(fx, fy, fr * 0.36f, fr * 0.075f, V_ICON);
        for (int k = 0; k < 4; k++) {
            float ang = (float) (k * Math.PI / 2);
            float ca = (float) Math.cos(ang), sa = (float) Math.sin(ang);
            vec.line(fx + ca * fr * 0.18f, fy + sa * fr * 0.18f, fx + ca * fr * 0.54f, fy + sa * fr * 0.54f, fr * 0.075f, V_ICON);
        }
        vec.circle(fx, fy, fr * 0.06f, V_ICON);
        vec.text(Texts.BTN[0], sx(B_FIRE), sy(B_FIRE) + fr * 0.72f, fr * 0.24f, bdown[B_FIRE] ? 0xFFFFFFFF : 0xD2FFFFFF, 1);
        // FOLOSEȘTE: mâna
        vecButton(B_USE);
        float hx = sx(B_USE), hy = sy(B_USE) + sr(B_USE) * 0.06f, hr = sr(B_USE);
        vec.rrect(hx - hr * 0.36f, hy - hr * 0.06f, hr * 0.66f, hr * 0.56f, hr * 0.18f, V_ICON);
        float[] tops = {-0.52f, -0.66f, -0.62f, -0.46f};
        for (int k = 0; k < 4; k++) {
            float x0 = hx + (-0.36f + k * 0.17f) * hr;
            vec.rrect(x0, hy + tops[k] * hr, hr * 0.145f, (0.20f - tops[k]) * hr, hr * 0.072f, V_ICON);
        }
        vecPoly(hx, hy, hr, P_THUMB, V_ICON);
        // REÎNCARCĂ: săgeată circulară
        vecButton(B_RELOAD);
        float rx = sx(B_RELOAD), ry = sy(B_RELOAD), rr = sr(B_RELOAD);
        vec.arc(rx, ry, rr * 0.42f, 30f, 290f, rr * 0.12f, V_ICON);
        float ea = (float) Math.toRadians(30 + 290);
        float ex = rx + (float) Math.cos(ea) * rr * 0.42f, ey = ry + (float) Math.sin(ea) * rr * 0.42f;
        float tx1 = (float) -Math.sin(ea), ty1 = (float) Math.cos(ea);
        vec.poly(new float[] {ex + tx1 * rr * 0.26f, ey + ty1 * rr * 0.26f, ex + (float) Math.cos(ea) * rr * 0.20f,
            ey + (float) Math.sin(ea) * rr * 0.20f, ex - (float) Math.cos(ea) * rr * 0.20f, ey - (float) Math.sin(ea) * rr * 0.20f},
            V_ICON);
        // ARMA: silueta armei curente și muniția alături
        vecButton(B_WEAPON);
        float[] gp = w.weapon == 0 ? P_PISTOL : (w.weapon == 1 ? P_SHOTGUN : P_SMG);
        vecPoly(sx(B_WEAPON), sy(B_WEAPON), sr(B_WEAPON) * 0.82f, gp, V_ICON);
        String am = String.valueOf(w.curMag());
        String rs = "/ " + w.curRes();
        float ax = sx(B_WEAPON) - sr(B_WEAPON) * 1.25f, ay = sy(B_WEAPON) + sr(B_WEAPON) * 0.30f;
        vec.text(rs, ax, ay, sr(B_WEAPON) * 0.55f, 0xC8DCD2C0, 2);
        vec.text(am, ax - sr(B_WEAPON) * 1.25f, ay + sr(B_WEAPON) * 0.04f, sr(B_WEAPON) * 0.95f,
                w.curMag() == 0 ? 0xFFE8463C : 0xFFFFFFFF, 2);
        // sus dreapta: harta și pauza
        vecSquare(B_MAP);
        float mx0 = sx(B_MAP), my0 = sy(B_MAP), mr = sr(B_MAP) * 0.62f;
        float[] mp = {-0.80f, -0.55f, -0.27f, -0.72f, 0.27f, -0.55f, 0.80f, -0.72f, 0.80f, 0.60f, 0.27f, 0.77f, -0.27f, 0.60f,
            -0.80f, 0.77f};
        for (int k = 0; k < 8; k++) {
            int k2 = (k + 1) % 8;
            vec.line(mx0 + mp[k * 2] * mr, my0 + mp[k * 2 + 1] * mr, mx0 + mp[k2 * 2] * mr, my0 + mp[k2 * 2 + 1] * mr,
                    mr * 0.15f, V_ICON);
        }
        vec.line(mx0 - 0.27f * mr, my0 - 0.72f * mr, mx0 - 0.27f * mr, my0 + 0.60f * mr, mr * 0.12f, V_ICON);
        vec.line(mx0 + 0.27f * mr, my0 - 0.55f * mr, mx0 + 0.27f * mr, my0 + 0.77f * mr, mr * 0.12f, V_ICON);
        vecSquare(B_PAUSE);
        float qx = sx(B_PAUSE), qy = sy(B_PAUSE), qr = sr(B_PAUSE);
        vec.rrect(qx - qr * 0.36f, qy - qr * 0.42f, qr * 0.24f, qr * 0.84f, qr * 0.07f, V_ICON);
        vec.rrect(qx + qr * 0.12f, qy - qr * 0.42f, qr * 0.24f, qr * 0.84f, qr * 0.07f, V_ICON);
    }

    void vecSquare(int b) {
        boolean d = bdown[b];
        float r = sr(b);
        vec.rrect(sx(b) - r, sy(b) - r, r * 2, r * 2, r * 0.32f, d ? V_FILL_ON : 0x50000000);
        vec.rrectStroke(sx(b) - r, sy(b) - r, r * 2, r * 2, r * 0.32f, r * 0.08f, d ? V_LINE_ON : V_LINE);
    }

    // ------------------------------------------------------------------ pauză, harta, notițe, radio
    void drawPause(Gfx g) {
        darken(g, 150);
        int cx = UW / 2;
        String t = Texts.PAUSE_TITLE;
        big.drawShadow(g, t, cx - big.width(t) * 2 / 2, 44, C_TITLE, 2);
        int bw = 190, bh = 24, gap = 6, y = 92;
        button(g, Texts.M_RESUME, cx - bw / 2, y, bw, bh, A_RESUME, true);
        button(g, Texts.M_SAVE, cx - bw / 2, y + (bh + gap), bw, bh, A_SAVE, false);
        button(g, Texts.M_SETTINGS, cx - bw / 2, y + 2 * (bh + gap), bw, bh, A_SETTINGS, false);
        button(g, Texts.M_MAIN, cx - bw / 2, y + 3 * (bh + gap), bw, bh, A_MAIN, false);
        String n = Texts.NOTES_LABEL + ": " + world.notesFound() + "/" + world.notesTotal();
        small.drawShadow(g, n, cx - small.width(n) / 2, UH - 14, C_DIM, 1);
    }

    void drawMap(Gfx g) {
        g.fill(0, 0, UW, UH, 0xF00A0A0C);
        World w = world;
        String t = Texts.MAP_TITLES[w.ep];
        big.drawShadow(g, t, 10, 6, C_TITLE, 1);
        String hint = Texts.TAP_CLOSE;
        small.drawShadow(g, hint, UW - small.width(hint) - 10, 12, C_DIM, 1);
        int cs = Math.min((UW - 20) / w.mw, (UH - 40) / w.mh);
        int ox = (UW - cs * w.mw) / 2, oy = 30 + (UH - 34 - cs * w.mh) / 2;
        for (int y = 0; y < w.mh; y++) {
            for (int x = 0; x < w.mw; x++) {
                int i = y * w.mw + x;
                if (!w.seen[i]) {
                    continue;
                }
                char c = w.map[i];
                int col;
                if (c == '.') {
                    col = zoneColor(w.zone[i]);
                } else if (World.isDoor(c)) {
                    col = doorColor(c);
                } else {
                    col = 0xFF8A8478;
                    boolean edge = false;
                    for (int k = 0; k < 4 && !edge; k++) {
                        int nx = x + (k == 0 ? 1 : (k == 1 ? -1 : 0)), ny = y + (k == 2 ? 1 : (k == 3 ? -1 : 0));
                        if (nx >= 0 && ny >= 0 && nx < w.mw && ny < w.mh && w.map[ny * w.mw + nx] == '.'
                                && w.seen[ny * w.mw + nx]) {
                            edge = true;
                        }
                    }
                    if (!edge) {
                        continue;
                    }
                }
                g.fill(ox + x * cs, oy + y * cs, cs, cs, col);
            }
        }
        // numele camerelor văzute
        for (int r = 0; r < w.lv.rid.length; r++) {
            int x1 = w.lv.rx1[r], y1 = w.lv.ry1[r], x2 = w.lv.rx2[r], y2 = w.lv.ry2[r];
            int ccx = (x1 + x2) / 2, ccy = (y1 + y2) / 2;
            if (!w.seen[ccy * w.mw + ccx] || x2 - x1 < 3 || y2 - y1 < 3) {
                continue;
            }
            String nm2 = Texts.roomName(w.lv.rid[r]);
            if (nm2 == null) {
                continue;
            }
            int tw = small.width(nm2);
            small.drawShadow(g, nm2, ox + (x1 + x2 + 1) * cs / 2 - tw / 2, oy + (y1 + y2 + 1) * cs / 2 - 4, 0xFFFFFFFF, 1);
        }
        // jucătorul
        int pxx = ox + (int) (w.px * cs), pyy = oy + (int) (w.py * cs);
        g.disc(pxx, pyy, 3, 0xFFF0D040);
        for (int k = 3; k < 9; k++) {
            g.set(pxx + (int) (w.dirX * k), pyy + (int) (w.dirY * k), 0xFFF0D040);
        }
    }

    static int zoneColor(char z) {
        switch (z) {
            case 'm': return 0xFF5C554C;
            case 'u': case 'i': return 0xFF3E5470;
            case 's': return 0xFF3A5C52;
            case 'l': return 0xFF6A7078;
            case 'p': return 0xFF6E6440;
            case 'k': case 'n': return 0xFF5C4430;
            case 't': return 0xFF4C6458;
            case 'a': case 'd': case 'x': return 0xFF4A4844;
            case 'e': return 0xFF3C3E44;
            case 'h': return 0xFF6A5C28;
            case 'o': case 'r': case 'f': case 'v': return 0xFF3E4660;
            default: return 0xFF4A5448;
        }
    }

    static int doorColor(char c) {
        switch (c) {
            case 'b': return 0xFF3A78E8;
            case 'r': return 0xFFE03A3A;
            case 'y': return 0xFFE8C030;
            case 'g': return 0xFF3AB860;
            case 'x': return 0xFF40E070;
            case 'v': return 0xFFF08C00;
            case 'o': return 0xFFB8B8C0;
            case 'e': return 0xFF80C0FF;
            default: return 0xFFA07040;
        }
    }

    void drawNote(Gfx g) {
        darken(g, 120);
        boolean paper = noteTitle != null;
        int pw = Math.min(UW - 60, 470), ph = UH - 40;
        int x = (UW - pw) / 2, y = 20;
        if (paper) {
            g.panel(x, y, pw, ph, 0xF0D8CFB8, 0xFF6A5A40);
            g.fill(x + 1, y + 1, pw - 2, 2, 0xFFEAE2CC);
            String t = noteTitle;
            big.draw(g, t, x + (pw - big.width(t)) / 2, y + 8, 0xFF7A2A1C, 1);
            g.fill(x + 20, y + 30, pw - 40, 1, 0x806A5A40);
            String[] lines = big.wrap(noteText, pw - 44, 1);
            int lh = big.lineH;
            int total = lines.length * lh;
            int avail = ph - 52;
            scrollMax = Math.max(0, total - avail);
            g.clip(x, y + 36, pw, avail);
            int cy = y + 38 - (int) scroll;
            for (int i = 0; i < lines.length; i++) {
                big.draw(g, lines[i], x + 22, cy, 0xFF2A2420, 1);
                cy += lh;
            }
            g.noClip();
            String h = scrollMax > 0 ? Texts.SCROLL_HINT : Texts.TAP_CLOSE;
            small.draw(g, h, x + (pw - small.width(h)) / 2, y + ph - 12, 0xFF6A5A40, 1);
        } else {
            g.panel(x, y, pw, ph, 0xE8101014, C_BORDER);
            drawRichText(g, noteText, x + 16, y + 12, pw - 36, ph - 30);
            String h = scrollMax > 0 ? Texts.SCROLL_HINT : Texts.TAP_CLOSE;
            small.draw(g, h, x + (pw - small.width(h)) / 2, y + ph - 12, C_DIM, 1);
        }
    }

    static final String[] IC_RADIO = {
        "..#.......",
        "..#.......",
        "..#.......",
        "#########.",
        "#.......#.",
        "#.##.##.#.",
        "#.......#.",
        "#.#####.#.",
        "#.......#.",
        "#.#####.#.",
        "#.......#.",
        "#########.",
    };

    void drawRadio(Gfx g) {
        if (radioId < 0) {
            return;
        }
        String text = Texts.radio(world.ep, radioId);
        if (text == null) {
            return;
        }
        int pw = Math.min(UW - 40, 500);
        String[] lines = big.wrap(text, pw - 56, 1);
        int ph = 34 + lines.length * big.lineH + 14;
        int x = (UW - pw) / 2, y = UH - ph - 12;
        g.panel(x, y, pw, ph, 0xE8101418, 0xFF3C7A5A);
        g.icon(IC_RADIO, x + 12, y + 10, 2, 0xFF6CC890);
        small.draw(g, Texts.RADIO_NAME, x + 40, y + 8, 0xFF6CC890, 1);
        int cy = y + 22;
        for (int i = 0; i < lines.length; i++) {
            big.draw(g, lines[i], x + 40, cy, C_TEXT, 1);
            cy += big.lineH;
        }
        String h = Texts.TAP_CLOSE;
        small.draw(g, h, x + pw - small.width(h) - 10, y + ph - 12, C_DIM, 1);
    }

    void drawDead(Gfx g) {
        g.fill(0, 0, UW, UH, Gfx.alpha(0x400000, Math.min(200, (int) (stateT * 300))));
        int cx = UW / 2;
        String t = Texts.DEAD_TITLE;
        big.drawShadow(g, t, cx - big.width(t), 50, C_RED, 2);
        int bw = 220, bh = 24, y = 110;
        if (hasSave) {
            button(g, Texts.DEAD_LOAD, cx - bw / 2, y, bw, bh, A_LOAD, true);
            y += bh + 6;
        }
        button(g, Texts.DEAD_RETRY, cx - bw / 2, y, bw, bh, A_RETRY, !hasSave);
        button(g, Texts.DEAD_MENU, cx - bw / 2, y + bh + 6, bw, bh, A_MAIN, false);
    }

    void drawStats(Gfx g) {
        g.clear(0xFF050506);
        World w = world;
        int t = (int) w.playTime;
        String time2 = (t / 60) + ":" + (t % 60 < 10 ? "0" : "") + (t % 60);
        String text = Texts.STATS_TITLE + " · " + Texts.EP_NAMES[w.ep].toUpperCase() + "\n\n"
                + Texts.STAT_DIFF + Texts.DIFF_NAMES[w.difficulty] + "\n"
                + Texts.STAT_TIME + time2 + "\n" + Texts.STAT_KILLS + w.kills + " / " + w.totalZ + "\n"
                + Texts.STAT_SHOTS + w.shots + "\n" + Texts.STAT_NOTES + w.notesFound() + " / " + w.notesTotal();
        drawRichText(g, text, 60, 30, UW - 120, UH - 80);
        if (w.ep + 1 < World.LEVELS.length) {
            button(g, Texts.M_NEXT, UW / 2 - 196, UH - 40, 190, 24, A_NEXT, true);
            button(g, Texts.M_MAIN, UW / 2 + 6, UH - 40, 190, 24, A_STATS_DONE, false);
        } else {
            button(g, Texts.M_MAIN, UW / 2 - 95, UH - 40, 190, 24, A_STATS_DONE, true);
        }
    }
}
