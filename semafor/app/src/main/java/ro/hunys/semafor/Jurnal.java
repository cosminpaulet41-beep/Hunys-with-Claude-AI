/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Jurnalul pentru depanare: tot ce se întâmplă, cu ora exactă, într-un fișier din aplicație.
 * The debugging log: everything that happens, with the exact time, in a file inside the app.
 */
package ro.hunys.semafor;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.PowerManager;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class Jurnal {
    private static final String FILE = "jurnal.txt";
    private static final long MAX_BYTES = 300 * 1024;
    private static final SimpleDateFormat TIME = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.ROOT);
    private static File file;

    private Jurnal() {}

    static synchronized void init(Context c) {
        if (file == null) file = new File(c.getApplicationContext().getFilesDir(), FILE);
    }

    /** Un rând în jurnal / One line in the log. */
    static synchronized void log(String tag, String msg) {
        String line = TIME.format(new Date()) + " [" + tag + "] " + msg + "\n";
        android.util.Log.i("Semafor", "[" + tag + "] " + msg);
        if (file == null) return;
        try {
            if (file.length() > MAX_BYTES) trim();
            Writer w = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
            try {
                w.write(line);
            } finally {
                w.close();
            }
        } catch (Exception ignored) {
        }
    }

    /** O eroare, cu tot traseul ei / An error, with its full stack trace. */
    static void error(String tag, String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        if (t != null) t.printStackTrace(new PrintWriter(sw));
        log(tag, "EROARE/ERROR: " + msg + (t != null ? "\n" + sw.toString().trim() : ""));
    }

    static synchronized String read() {
        if (file == null || !file.exists()) return "";
        StringBuilder b = new StringBuilder();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            try {
                char[] buf = new char[8192];
                int n;
                while ((n = r.read(buf)) > 0) b.append(buf, 0, n);
            } finally {
                r.close();
            }
        } catch (Exception e) {
            b.append("(nu am putut citi jurnalul: ").append(e).append(")\n");
        }
        return b.toString();
    }

    static synchronized void clear() {
        if (file != null) file.delete();
    }

    /** Păstrează doar a doua jumătate când fișierul e prea mare / Keeps only the second half when too big. */
    private static void trim() {
        String all = read();
        int cut = all.indexOf('\n', all.length() / 2);
        String keep = cut > 0 ? all.substring(cut + 1) : "";
        try {
            Writer w = new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8");
            try {
                w.write("…(începutul a fost tăiat / start trimmed)…\n");
                w.write(keep);
            } finally {
                w.close();
            }
        } catch (Exception ignored) {
        }
    }

    // ---------------------------------------------------------------- stare / state

    /** Starea telefonului și a aplicației, pentru capul jurnalului / Phone and app state, for the log header. */
    static String header(Context c) {
        StringBuilder b = new StringBuilder();
        b.append("===== Semafor — jurnal / log =====\n");
        try {
            PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            b.append("Versiune / Version: ").append(pi.versionName).append(" (").append(pi.versionCode).append(")\n");
        } catch (Exception e) {
            b.append("Versiune / Version: ?\n");
        }
        b.append("Acum / Now: ").append(TIME.format(new Date())).append('\n');
        b.append("Telefon / Phone: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                .append(" (").append(Build.DEVICE).append(")\n");
        b.append("Android: ").append(Build.VERSION.RELEASE).append(" / SDK ").append(Build.VERSION.SDK_INT).append('\n');
        b.append("Build: ").append(Build.DISPLAY).append('\n');
        b.append("EMUI: ").append(prop("ro.build.version.emui"))
                .append(" / HarmonyOS: ").append(prop("hw_sc.build.platform.version"))
                .append(" / Magic: ").append(prop("ro.build.version.magic")).append('\n');
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                PackageInfo wv = WebView.getCurrentWebViewPackage();
                b.append("WebView: ").append(wv == null ? "?" : wv.packageName + " " + wv.versionName).append('\n');
            } catch (Throwable t) {
                b.append("WebView: ").append(t).append('\n');
            }
        }
        b.append("Afișare peste aplicații / Overlay: ").append(Gate.canDrawOverlays(c)).append('\n');
        try {
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= 23) {
                b.append("Fără restricții baterie / Battery unrestricted: ")
                        .append(pm.isIgnoringBatteryOptimizations(c.getPackageName())).append('\n');
            }
            b.append("Ecran aprins / Screen on: ").append(pm.isInteractive()).append('\n');
        } catch (Exception ignored) {
        }
        try {
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 24) {
                b.append("Notificări / Notifications: ").append(nm.areNotificationsEnabled()).append('\n');
            }
        } catch (Exception ignored) {
        }
        try {
            KeyguardManager km = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= 23) {
                b.append("Blocare cu cod / Secure lock: ").append(km.isDeviceSecure()).append('\n');
            }
        } catch (Exception ignored) {
        }
        SharedPreferences sp = Prefs.get(c);
        b.append("Pornit / Active: ").append(Prefs.isActive(sp))
                .append(" · Mod / Mode: ").append(Prefs.getMode(sp) == Prefs.MODE_WINDOW ? "fereastra/window" : "aplicație/app")
                .append(" · Cât de des / Freq: ").append(Prefs.getFreq(sp))
                .append(" · Categoria: ").append(Site.SLUGS[Prefs.getCategory(sp)])
                .append(" · Noaptea / Night: ").append(Prefs.isNightQuiet(sp))
                .append(" · Pauză / Paused: ").append(Prefs.isPaused(sp))
                .append(" · Aplicație / App: ").append(Prefs.getPackage(sp)).append('\n');
        b.append("Serviciu activ / Service running: ").append(UnlockService.isRunning()).append('\n');
        b.append("==================================\n");
        return b.toString();
    }

    /** O proprietate de sistem prin „getprop” / A system property via "getprop". */
    private static String prop(String key) {
        try {
            Process p = Runtime.getRuntime().exec(new String[] {"getprop", key});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String v = r.readLine();
            r.close();
            p.destroy();
            return v == null || v.length() == 0 ? "-" : v;
        } catch (Exception e) {
            return "?";
        }
    }
}
