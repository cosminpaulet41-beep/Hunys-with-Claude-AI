package ro.hunys.tn2;

import java.io.File;

/**
 * Un jucător automat termină fiecare episod: merge pe hartă (cu ocolirea pereților și a ușilor încuiate),
 * deschide ușile, ia cardurile, vorbește cu personajele, folosește panourile, se luptă cu infectații și
 * cu bossii, până când episodul se termină. Viața îi e ținută plină (testăm logica, nu îndemânarea).
 * Raportează unde s-a împotmolit, dacă s-a împotmolit.
 */
public final class Play {
    static Game g;
    static World w;
    static int stuckCount, frame;
    static float simTime;

    public static void main(String[] a) throws Exception {
        DesktopPlatform p = new DesktopPlatform(new File(a[0]));
        g = new Game(p);
        g.init(1280, 720);
        boolean ok = true;
        for (int ep = 0; ep < 3; ep++) {
            g.difficulty = 1;
            g.startEpisode(ep, null);
            w = g.world;
            stuckCount = 0;
            simTime = 0f;
            boolean done = run(ep);
            System.out.printf("episodul %d: %s în %.0f s de joc, uciși %d/%d, împotmoliri %d, obiectiv %d%n", ep + 1,
                    done ? "TERMINAT" : "NETERMINAT", simTime, w.kills, w.totalZ, stuckCount, w.objective);
            ok &= done;
        }
        System.out.println(ok ? "PLAY OK" : "PLAY GRESIT");
        if (!ok) {
            System.exit(1);
        }
    }

    // -------------------------------------------------------------- scopurile fiecărui episod
    static boolean run(int ep) {
        if (ep == 0) {
            return item(World.IT_BLUE) && npc(Art.Z_ELENA) && item(World.IT_RED) && useWall('E') && exit();
        } else if (ep == 1) {
            return item(World.IT_YELLOW) && item(World.IT_GREEN) && killBoss() && exit();
        }
        return npc(Art.Z_CRISTI) && useWall('L') && killBoss() && heli();
    }

    static boolean item(int type) {
        for (int guard = 0; guard < 4; guard++) {
            int idx = -1;
            for (int i = 0; i < w.ni; i++) {
                if (w.it[i] == type && w.ion[i]) {
                    idx = i;
                }
            }
            if (idx >= 0) {
                final int k = idx;
                return goTo(w.ix[k], w.iy[k], 0.3f, () -> !w.ion[k], "obiectul " + type);
            }
            // obiectul îl are un infectat: îl vânăm
            int z = -1;
            for (int i = 0; i < w.nz; i++) {
                if (w.zcarry[i] >= 0 && w.it[w.zcarry[i]] == type && w.zst[i] < World.Z_DYING) {
                    z = i;
                }
            }
            if (z < 0) {
                System.out.println("  lipsește obiectul " + type);
                return false;
            }
            final int zz = z;
            if (!goTo(w.zx[zz], w.zy[zz], 2.5f, () -> w.zst[zz] >= World.Z_DYING, "purtătorul obiectului " + type)) {
                return false;
            }
            if (!stepUntil(() -> w.zst[zz] >= World.Z_DYING, 30f)) {
                return false;
            }
        }
        return false;
    }

    static boolean npc(int type) {
        for (int i = 0; i < w.nz; i++) {
            if (w.zty[i] == type) {
                final int k = i;
                boolean r = goTo(w.zx[k], w.zy[k], 1.2f, () -> type == Art.Z_ELENA ? w.metElena : w.metCristi,
                        "personajul " + type);
                return r && stepUntil(() -> type == Art.Z_ELENA ? w.metElena : w.metCristi, 5f);
            }
        }
        return false;
    }

    static boolean useWall(char c) {
        for (int i = 0; i < w.mw * w.mh; i++) {
            if (w.map[i] != c) {
                continue;
            }
            int cx = i % w.mw, cy = i / w.mw;
            for (int k = 0; k < 4; k++) {
                int nx = cx + (k == 0 ? 1 : (k == 1 ? -1 : 0)), ny = cy + (k == 2 ? 1 : (k == 3 ? -1 : 0));
                if (nx < 0 || ny < 0 || nx >= w.mw || ny >= w.mh || w.map[ny * w.mw + nx] != '.') {
                    continue;
                }
                if (!goTo(nx + 0.5f, ny + 0.5f, 0.35f, null, "panoul " + c)) {
                    return false;
                }
                w.setAngle((float) Math.atan2(cy - ny, cx - nx));
                w.useAhead(true);
                return c == 'E' ? w.power : w.liftOn;
            }
        }
        return false;
    }

    static boolean killBoss() {
        int b = -1;
        for (int i = 0; i < w.nz; i++) {
            if (w.isBoss(i)) {
                b = i;
            }
        }
        if (b < 0) {
            return false;
        }
        final int k = b;
        for (int t = 0; t < 6 && w.zst[k] < World.Z_DYING; t++) {
            if (!goTo(w.zx[k], w.zy[k], 3.0f, () -> w.zst[k] >= World.Z_DYING, "bossul")) {
                return false;
            }
            stepUntil(() -> w.zst[k] >= World.Z_DYING, 20f);
        }
        return w.bossDead;
    }

    static boolean exit() {
        for (int i = 0; i < w.mw * w.mh; i++) {
            if (w.map[i] == 'x' && w.lv.struct[i] == 'x') {
                int cx = i % w.mw, cy = i / w.mw;
                if (w.ep == 2) {
                    continue;
                }
                if (goTo(cx + 0.5f, cy + 0.5f, 0.2f, () -> w.won, "ieșirea") || w.won) {
                    return true;
                }
            }
        }
        return w.won;
    }

    static boolean heli() {
        int h = Art.decorIndex("elicopter");
        for (int k = 0; k < w.nd; k++) {
            if (w.dty[k] == h) {
                return goTo(w.dxp[k] + 0.5f, w.dyp[k] + 2.0f, 0.4f, () -> w.won, "elicopterul") || w.won;
            }
        }
        return false;
    }

    // -------------------------------------------------------------- mersul
    interface Cond {
        boolean ok();
    }

    /** Merge până la (x, y) (la distanța r) sau până se îndeplinește condiția. */
    static boolean goTo(float x, float y, float r, Cond done, String what) {
        int tx = (int) x, ty = (int) y;
        float still = 0f, lx = w.px, ly = w.py;
        for (int it = 0; it < 60 * 240; it++) {
            if (done != null && done.ok()) {
                return true;
            }
            float dx = x - w.px, dy = y - w.py;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < r) {
                return true;
            }
            int[] path = path((int) w.px, (int) w.py, tx, ty);
            if (path == null) {
                System.out.printf("  nu există drum spre %s (%d,%d) de la (%.1f,%.1f)%n", what, tx, ty, w.px, w.py);
                return false;
            }
            float wx, wy;
            if (path.length >= 3) {
                int nxt = path[1];
                wx = nxt % w.mw + 0.5f;
                wy = nxt / w.mw + 0.5f;
                if (Math.abs(wx - w.px) + Math.abs(wy - w.py) < 0.35f) {
                    nxt = path[2];
                    wx = nxt % w.mw + 0.5f;
                    wy = nxt / w.mw + 0.5f;
                }
            } else {
                wx = x;
                wy = y;
            }
            step(wx, wy);
            // împotmolit (nu s-a mișcat de 3 secunde)? îl împingem puțin spre următorul punct
            if (Math.abs(w.px - lx) + Math.abs(w.py - ly) > 0.3f) {
                lx = w.px;
                ly = w.py;
                still = 0f;
            } else {
                still += 1f / 60f;
                if (still > 3f) {
                    stuckCount++;
                    int ax = (int) (w.px + w.dirX * 0.75f), ay = (int) (w.py + w.dirY * 0.75f);
                    System.out.printf("  împotmolit la (%.1f,%.1f) spre %s; în față '%c' %.2f, unghi %.0f%n", w.px, w.py, what,
                            w.map[ay * w.mw + ax], w.door[ay * w.mw + ax], Math.toDegrees(w.ang));
                    float ux = wx - w.px, uy = wy - w.py;
                    float ud = (float) Math.sqrt(ux * ux + uy * uy) + 1e-4f;
                    float nx = w.px + ux / ud * 0.5f, ny = w.py + uy / ud * 0.5f;
                    if (!w.blockedAt(nx, ny)) {
                        w.px = nx;
                        w.py = ny;
                    }
                    still = 0f;
                    lx = w.px;
                    ly = w.py;
                }
            }
            if (g.state == Game.ST_STATS || w.won) {
                return done == null || done.ok();
            }
        }
        System.out.println("  timp expirat spre " + what);
        return false;
    }

    static boolean stepUntil(Cond c, float secs) {
        for (int i = 0; i < (int) (secs * 60); i++) {
            if (c.ok()) {
                return true;
            }
            step(w.px + w.dirX, w.py + w.dirY);
        }
        return c.ok();
    }

    /** Un cadru: se apără de cel mai apropiat infectat, altfel merge spre (wx, wy). */
    static void step(float wx, float wy) {
        while (g.state != Game.ST_PLAY && g.state != Game.ST_STATS) {
            g.tapScreen();
        }
        if (g.state == Game.ST_STATS) {
            return;
        }
        w.health = 100;
        int tgt = -1;
        float best = 7f;
        for (int i = 0; i < w.nz; i++) {
            if (w.zst[i] >= World.Z_DYING || w.zdecor[i] || w.npc(i) || w.zty[i] == World.T_O2) {
                continue;
            }
            float dx = w.zx[i] - w.px, dy = w.zy[i] - w.py;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < best && (w.zst[i] == World.Z_CHASE || d < 3f) && w.los(w.px, w.py, w.zx[i], w.zy[i])) {
                best = d;
                tgt = i;
            }
        }
        float fwd, turn;
        boolean fire = false;
        if (tgt >= 0) {
            float a = (float) Math.atan2(w.zy[tgt] - w.py, w.zx[tgt] - w.px);
            turn = clampTurn(a - w.ang);
            fwd = best > 2.5f ? 0.4f : -0.3f;
            if (Math.abs(angDiff(a - w.ang)) < 0.08f) {
                if (w.curMag() == 0 && w.curRes() == 0) {
                    w.switchWeapon();
                }
                w.fire();
                fire = true;
            }
            // muniție nelimitată pentru test
            w.res[w.weapon] = Math.max(w.res[w.weapon], 30);
        } else {
            float a = (float) Math.atan2(wy - w.py, wx - w.px);
            float diff = angDiff(a - w.ang);
            turn = clampTurn(diff);
            fwd = Math.abs(diff) < 0.5f ? 1f : 0.2f;
        }
        g.pendingTurn = turn;
        g.joyOn = true;
        g.joyX = 0f;
        g.joyY = -fwd;
        g.fireHeld = fire;
        g.update(1f / 60f);
        simTime += 1f / 60f;
        // focul folosește distanța până la perete din ultimul cadru desenat
        if ((frame++ & 1) == 0) {
            g.render();
        }
    }

    static float angDiff(float a) {
        while (a > Math.PI) {
            a -= 2 * Math.PI;
        }
        while (a < -Math.PI) {
            a += 2 * Math.PI;
        }
        return a;
    }

    static float clampTurn(float a) {
        a = angDiff(a);
        return Math.max(-0.12f, Math.min(0.12f, a));
    }

    /** Drumul cel mai scurt pe celule (BFS), prin uși pe care le putem deschide acum. */
    static int[] path(int sx, int sy, int tx, int ty) {
        int n = w.mw * w.mh;
        int[] prev = new int[n];
        for (int i = 0; i < n; i++) {
            prev[i] = -2;
        }
        int[] q = new int[n];
        int h = 0, t = 0;
        int s = sy * w.mw + sx, goal = ty * w.mw + tx;
        prev[s] = -1;
        q[t++] = s;
        while (h < t) {
            int c = q[h++];
            if (c == goal) {
                break;
            }
            int x = c % w.mw, y = c / w.mw;
            for (int k = 0; k < 4; k++) {
                int nx = x + (k == 0 ? 1 : (k == 1 ? -1 : 0)), ny = y + (k == 2 ? 1 : (k == 3 ? -1 : 0));
                if (nx < 0 || ny < 0 || nx >= w.mw || ny >= w.mh) {
                    continue;
                }
                int o = ny * w.mw + nx;
                if (prev[o] != -2 || !walk(o, o == goal)) {
                    continue;
                }
                prev[o] = c;
                q[t++] = o;
            }
        }
        if (prev[goal] == -2) {
            return null;
        }
        int len = 0;
        for (int c = goal; c != -1; c = prev[c]) {
            len++;
        }
        int[] p = new int[len];
        for (int c = goal, k = len - 1; c != -1; c = prev[c], k--) {
            p[k] = c;
        }
        return p;
    }

    static boolean[] decorBlock;
    static Level decorLv;

    static boolean walk(int o, boolean goal) {
        if (decorBlock == null || decorBlock.length != w.mw * w.mh || decorLv != w.lv) {
            decorLv = w.lv;
            decorBlock = new boolean[w.mw * w.mh];
            for (int i = 0; i < decorBlock.length; i++) {
                float cx = i % w.mw + 0.5f, cy = i / w.mw + 0.5f;
                for (int k = 0; k < w.nd; k++) {
                    float r = w.drad[k];
                    if (r > 0f) {
                        float dx = w.dxp[k] - cx, dy = w.dyp[k] - cy;
                        if (dx * dx + dy * dy < (r + 0.25f) * (r + 0.25f)) {
                            decorBlock[i] = true;
                        }
                    }
                }
            }
        }
        if (decorBlock[o] && !goal) {
            return false;
        }
        char c = w.map[o];
        if (c == '.') {
            return true;
        }
        if (!World.isDoor(c)) {
            return false;
        }
        if (w.door[o] >= 0.98f || c == 'D' || c == 'o') {
            return true;
        }
        switch (c) {
            case 'b': return w.keyBlue;
            case 'r': return w.keyRed;
            case 'y': return w.keyYellow;
            case 'g': return w.keyGreen;
            case 'v': return w.bossDead;
            case 'x': return goal;
            default: return false;
        }
    }
}
