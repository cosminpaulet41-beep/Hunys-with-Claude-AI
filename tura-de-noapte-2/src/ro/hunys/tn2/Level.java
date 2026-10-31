package ro.hunys.tn2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** O hartă citită din assets/maps/*.txt (formatul e descris în tools/mapgen.py). */
public final class Level {
    public String name;
    public int w, h;
    public char[] struct, zones, lights;
    /** Lucruri: x, y, cod (de ex. "P", "zg", "a", "n3"), unghi în grade (sau -1). */
    public float[] tx, ty;
    public String[] tcode;
    public int[] tang;
    public float[] dx, dy;
    public String[] dname;
    public int[] rx1, ry1, rx2, ry2;
    public String[] rid;

    public static Level load(Platform p, String file) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(p.open("maps/" + file), "UTF-8"));
        Level lv = new Level();
        String section = "";
        int row = 0;
        java.util.Vector things = new java.util.Vector(), decor = new java.util.Vector(), rooms = new java.util.Vector();
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("[")) {
                section = line.substring(1, line.indexOf(']'));
                row = 0;
                continue;
            }
            if (section.length() == 0) {
                String[] f = Art.split(line);
                if (f.length >= 2 && f[0].equals("name")) {
                    lv.name = f[1];
                } else if (f.length >= 3 && f[0].equals("size")) {
                    lv.w = Integer.parseInt(f[1]);
                    lv.h = Integer.parseInt(f[2]);
                    lv.struct = new char[lv.w * lv.h];
                    lv.zones = new char[lv.w * lv.h];
                    lv.lights = new char[lv.w * lv.h];
                }
                continue;
            }
            if (section.equals("struct") || section.equals("zones") || section.equals("lights")) {
                char[] g = section.equals("struct") ? lv.struct : (section.equals("zones") ? lv.zones : lv.lights);
                if (row < lv.h) {
                    for (int x = 0; x < lv.w; x++) {
                        g[row * lv.w + x] = x < line.length() ? line.charAt(x) : '#';
                    }
                }
                row++;
            } else if (section.equals("things")) {
                things.addElement(Art.split(line));
            } else if (section.equals("decor")) {
                decor.addElement(Art.split(line));
            } else if (section.equals("rooms")) {
                rooms.addElement(Art.split(line));
            }
        }
        r.close();
        int n = things.size();
        lv.tx = new float[n];
        lv.ty = new float[n];
        lv.tcode = new String[n];
        lv.tang = new int[n];
        for (int i = 0; i < n; i++) {
            String[] f = (String[]) things.elementAt(i);
            lv.tx[i] = Float.parseFloat(f[0]);
            lv.ty[i] = Float.parseFloat(f[1]);
            lv.tcode[i] = f[2];
            lv.tang[i] = f.length > 3 ? Integer.parseInt(f[3]) : -1;
        }
        n = decor.size();
        lv.dx = new float[n];
        lv.dy = new float[n];
        lv.dname = new String[n];
        for (int i = 0; i < n; i++) {
            String[] f = (String[]) decor.elementAt(i);
            lv.dx[i] = Float.parseFloat(f[0]);
            lv.dy[i] = Float.parseFloat(f[1]);
            lv.dname[i] = f[2];
        }
        n = rooms.size();
        lv.rx1 = new int[n];
        lv.ry1 = new int[n];
        lv.rx2 = new int[n];
        lv.ry2 = new int[n];
        lv.rid = new String[n];
        for (int i = 0; i < n; i++) {
            String[] f = (String[]) rooms.elementAt(i);
            lv.rx1[i] = Integer.parseInt(f[0]);
            lv.ry1[i] = Integer.parseInt(f[1]);
            lv.rx2[i] = Integer.parseInt(f[2]);
            lv.ry2[i] = Integer.parseInt(f[3]);
            lv.rid[i] = f[4];
        }
        return lv;
    }

    public int room(int x, int y) {
        for (int i = 0; i < rid.length; i++) {
            if (x >= rx1[i] && y >= ry1[i] && x <= rx2[i] && y <= ry2[i]) {
                return i;
            }
        }
        return -1;
    }
}
