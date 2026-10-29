/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.content.Context;
import android.content.SharedPreferences;

/** Setările aplicației / App settings. */
final class Prefs {
    static final int DIM_DEFAULT = 50;
    static final int WARM_DEFAULT = 0;

    static final String LANG_AUTO = "auto";
    static final String LANG_RO = "ro";
    static final String LANG_EN = "en";

    private static final String FILE = "penumbra";
    private static final String K_ACTIVE = "active";
    private static final String K_DIM = "dim";
    private static final String K_WARM = "warm";
    private static final String K_MIN_LIGHT = "min_light";
    private static final String K_FULL_COVER = "full_cover";
    private static final String K_LANG = "lang";
    private static final String K_ASKED_NOTIF = "asked_notif";

    private Prefs() {}

    static SharedPreferences get(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Dacă stratul trebuie să fie pornit / Whether the layer should be on. */
    static boolean isActive(SharedPreferences sp) {
        return sp.getBoolean(K_ACTIVE, false);
    }

    static void setActive(SharedPreferences sp, boolean on) {
        sp.edit().putBoolean(K_ACTIVE, on).apply();
    }

    /** Întunecare cerută, 0–90 % (limita reală o pune Look) / Requested dimming, 0–90 % (Look applies the real limit). */
    static int getDim(SharedPreferences sp) {
        return clamp(sp.getInt(K_DIM, DIM_DEFAULT), 0, 90);
    }

    static void setDim(SharedPreferences sp, int dim) {
        sp.edit().putInt(K_DIM, clamp(dim, 0, 90)).apply();
    }

    /** Filtru cald, 0–100 % / Warm filter, 0–100 %. */
    static int getWarm(SharedPreferences sp) {
        return clamp(sp.getInt(K_WARM, WARM_DEFAULT), 0, 100);
    }

    static void setWarm(SharedPreferences sp, int warm) {
        sp.edit().putInt(K_WARM, clamp(warm, 0, 100)).apply();
    }

    /** Lumina de fundal coborâtă la minimum / Backlight lowered to minimum. */
    static boolean isMinLight(SharedPreferences sp) {
        return sp.getBoolean(K_MIN_LIGHT, false);
    }

    static void setMinLight(SharedPreferences sp, boolean on) {
        sp.edit().putBoolean(K_MIN_LIGHT, on).apply();
    }

    /** Acoperire completă prin serviciul de accesibilitate / Full cover through the accessibility service. */
    static boolean isFullCover(SharedPreferences sp) {
        return sp.getBoolean(K_FULL_COVER, false);
    }

    static void setFullCover(SharedPreferences sp, boolean on) {
        sp.edit().putBoolean(K_FULL_COVER, on).apply();
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

    static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
