/*
 * Pas Volum / Volume Step
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.pasvolum;

import android.content.Context;
import android.content.SharedPreferences;

/** Setările aplicației / App settings. */
final class Prefs {
    static final int STEP_MIN = 1;
    static final int STEP_MAX = 20;
    static final int STEP_DEFAULT = 2;

    static final String LANG_AUTO = "auto";
    static final String LANG_RO = "ro";
    static final String LANG_EN = "en";

    private static final String FILE = "pas_volum";
    private static final String K_STEP = "step";
    private static final String K_ENABLED = "enabled";
    private static final String K_LANG = "lang";
    private static final String K_LAST_KEY = "last_key";
    private static final String K_LAST_KEY_TIME = "last_key_time";

    private Prefs() {}

    static SharedPreferences get(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static int getStep(SharedPreferences sp) {
        return clampStep(sp.getInt(K_STEP, STEP_DEFAULT));
    }

    static void setStep(SharedPreferences sp, int step) {
        sp.edit().putInt(K_STEP, clampStep(step)).apply();
    }

    static int clampStep(int s) {
        if (s < STEP_MIN) return STEP_MIN;
        if (s > STEP_MAX) return STEP_MAX;
        return s;
    }

    static boolean isEnabled(SharedPreferences sp) {
        return sp.getBoolean(K_ENABLED, true);
    }

    static void setEnabled(SharedPreferences sp, boolean on) {
        sp.edit().putBoolean(K_ENABLED, on).apply();
    }

    static String getLang(SharedPreferences sp) {
        return sp.getString(K_LANG, LANG_AUTO);
    }

    static void setLang(SharedPreferences sp, String lang) {
        sp.edit().putString(K_LANG, lang).apply();
    }

    /** Ultima tastă de volum văzută de serviciu / Last volume key seen by the service. */
    static void recordKey(SharedPreferences sp, int keyCode) {
        sp.edit().putInt(K_LAST_KEY, keyCode).putLong(K_LAST_KEY_TIME, System.currentTimeMillis()).apply();
    }

    static int getLastKey(SharedPreferences sp) {
        return sp.getInt(K_LAST_KEY, 0);
    }

    static long getLastKeyTime(SharedPreferences sp) {
        return sp.getLong(K_LAST_KEY_TIME, 0L);
    }
}
