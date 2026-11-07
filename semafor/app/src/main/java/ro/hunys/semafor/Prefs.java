/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.semafor;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/** Setările și statisticile aplicației / App settings and stats. */
final class Prefs {
    static final String LANG_AUTO = "auto";
    static final String LANG_RO = "ro";
    static final String LANG_EN = "en";

    /**
     * Cât de des se deschide: la a câta deblocare, sau o dată la câte minute.
     * How often it opens: on which unlock, or once every how many minutes.
     */
    static final int[] FREQ_EVERY = {1, 2, 3, 0, 0, 0};
    static final int[] FREQ_MINUTES = {0, 0, 0, 10, 30, 60};

    private static final String FILE = "semafor";
    private static final String K_ACTIVE = "active";
    private static final String K_PKG = "pkg";
    private static final String K_FREQ = "freq";
    private static final String K_NIGHT = "night_quiet";
    private static final String K_PAUSE_UNTIL = "pause_until";
    private static final String K_COUNTER = "unlock_counter";
    private static final String K_LAST_OPEN = "last_open";
    private static final String K_DAY = "day";
    private static final String K_TODAY = "today";
    private static final String K_TOTAL = "total";
    private static final String K_LANG = "lang";
    private static final String K_ASKED_NOTIF = "asked_notif";

    private Prefs() {}

    static SharedPreferences get(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean isActive(SharedPreferences sp) {
        return sp.getBoolean(K_ACTIVE, false);
    }

    static void setActive(SharedPreferences sp, boolean on) {
        sp.edit().putBoolean(K_ACTIVE, on).apply();
    }

    /** Pachetul aplicației cu întrebări / The quiz app's package. */
    static String getPackage(SharedPreferences sp) {
        return sp.getString(K_PKG, "");
    }

    static void setPackage(SharedPreferences sp, String pkg) {
        sp.edit().putString(K_PKG, pkg == null ? "" : pkg).putInt(K_COUNTER, 0).apply();
    }

    static int getFreq(SharedPreferences sp) {
        return clamp(sp.getInt(K_FREQ, 0), 0, FREQ_EVERY.length - 1);
    }

    static void setFreq(SharedPreferences sp, int idx) {
        sp.edit().putInt(K_FREQ, clamp(idx, 0, FREQ_EVERY.length - 1)).putInt(K_COUNTER, 0).apply();
    }

    /** Fără întrebări între 23:00 și 07:00 / No questions between 23:00 and 07:00. */
    static boolean isNightQuiet(SharedPreferences sp) {
        return sp.getBoolean(K_NIGHT, false);
    }

    static void setNightQuiet(SharedPreferences sp, boolean on) {
        sp.edit().putBoolean(K_NIGHT, on).apply();
    }

    static long getPauseUntil(SharedPreferences sp) {
        return sp.getLong(K_PAUSE_UNTIL, 0L);
    }

    static void setPauseUntil(SharedPreferences sp, long t) {
        sp.edit().putLong(K_PAUSE_UNTIL, t).apply();
    }

    static boolean isPaused(SharedPreferences sp) {
        return System.currentTimeMillis() < getPauseUntil(sp);
    }

    /** Deblocări numărate de la ultima deschidere / Unlocks counted since the last opening. */
    static int getCounter(SharedPreferences sp) {
        return sp.getInt(K_COUNTER, 0);
    }

    static void setCounter(SharedPreferences sp, int n) {
        sp.edit().putInt(K_COUNTER, n).apply();
    }

    static long getLastOpen(SharedPreferences sp) {
        return sp.getLong(K_LAST_OPEN, 0L);
    }

    /** Câte întrebări s-au deschis azi / How many questions opened today. */
    static int getToday(SharedPreferences sp) {
        return sp.getInt(K_DAY, -1) == dayKey() ? sp.getInt(K_TODAY, 0) : 0;
    }

    static int getTotal(SharedPreferences sp) {
        return sp.getInt(K_TOTAL, 0);
    }

    /** Notează o deschidere la deblocare / Records an opening on unlock. */
    static void recordOpen(SharedPreferences sp) {
        int day = dayKey();
        int today = sp.getInt(K_DAY, -1) == day ? sp.getInt(K_TODAY, 0) : 0;
        sp.edit()
                .putLong(K_LAST_OPEN, System.currentTimeMillis())
                .putInt(K_DAY, day)
                .putInt(K_TODAY, today + 1)
                .putInt(K_TOTAL, getTotal(sp) + 1)
                .apply();
    }

    static String getLang(SharedPreferences sp) {
        return sp.getString(K_LANG, LANG_AUTO);
    }

    static void setLang(SharedPreferences sp, String lang) {
        sp.edit().putString(K_LANG, lang).apply();
    }

    static boolean askedNotifications(SharedPreferences sp) {
        return sp.getBoolean(K_ASKED_NOTIF, false);
    }

    static void setAskedNotifications(SharedPreferences sp) {
        sp.edit().putBoolean(K_ASKED_NOTIF, true).apply();
    }

    private static int dayKey() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR);
    }

    static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
