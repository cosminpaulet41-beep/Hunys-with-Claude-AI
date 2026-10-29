/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.os.Build;

/**
 * Cum arată stratul: opacitate, culoare și lumina de fundal.
 * What the layer looks like: opacity, color and backlight.
 *
 * Android 12+ blochează atingerile care trec printr-un strat de la altă aplicație mai opac de 80%,
 * așa că stratul normal se oprește la 80%. Stratul de accesibilitate („acoperire completă”) e de
 * încredere pentru sistem și poate merge până la 90%.
 * Android 12+ blocks touches that pass through another app's layer more than 80% opaque, so the
 * normal layer stops at 80%. The accessibility layer ("full cover") is trusted by the system and
 * can go down to 90%.
 */
final class Look {
    /** Întunecarea maximă cu acoperire completă / Maximum dimming with full cover. */
    static final int FULL_COVER_MAX_DIM = 90;

    // Chihlimbar: taie lumina albastră / Amber: cuts blue light.
    private static final int AMBER_R = 255;
    private static final int AMBER_G = 125;
    private static final int AMBER_B = 10;
    /** Opacitatea chihlimbarului la filtru cald 100% / Amber opacity at 100% warm filter. */
    private static final float WARM_STRENGTH = 0.55f;
    /** Opacitatea totală maximă cât timp atingerile nu sunt limitate / Max total opacity when touches aren't limited. */
    private static final float SAFE_MAX_ALPHA = 0.93f;
    /**
     * Lumina de fundal minimă. Puțin peste 0 (= 1/255), ca telefoanele vechi să nu stingă ecranul de tot;
     * sistemul o aduce oricum la minimul telefonului.
     * Minimum backlight. Slightly above 0 (= 1/255) so old phones never switch the screen fully off;
     * the system brings it to the phone's own minimum anyway.
     */
    private static final float MIN_BACKLIGHT = 0.004f;
    /** Fără schimbare de luminozitate / No brightness override. */
    static final float NO_BACKLIGHT_OVERRIDE = -1f;

    final float alpha;
    final int color;
    final float backlight;

    private Look(float alpha, int color, float backlight) {
        this.alpha = alpha;
        this.color = color;
        this.backlight = backlight;
    }

    /** @param fullCover stratul e desenat de serviciul de accesibilitate / the layer is drawn by the accessibility service */
    static Look of(Context c, boolean fullCover) {
        SharedPreferences sp = Prefs.get(c);
        float dim = Math.min(Prefs.getDim(sp), maxDim(c, fullCover)) / 100f;
        float warm = Prefs.getWarm(sp) / 100f * WARM_STRENGTH;

        // Întâi chihlimbarul, apoi negrul, adunate într-un singur strat:
        // First the amber, then the black, merged into one layer:
        //   pixel = ecran·(1−w)(1−d) + chihlimbar·w(1−d)
        float alpha = 1f - (1f - warm) * (1f - dim);
        int color = Color.BLACK;
        if (alpha > 0.0005f) {
            float k = warm * (1f - dim) / alpha;
            color = Color.rgb(Math.round(AMBER_R * k), Math.round(AMBER_G * k), Math.round(AMBER_B * k));
        } else {
            alpha = 0f;
        }
        alpha = Math.min(alpha, maxAlpha(c, fullCover));

        float backlight = Prefs.isMinLight(sp) ? MIN_BACKLIGHT : NO_BACKLIGHT_OVERRIDE;
        return new Look(alpha, color, backlight);
    }

    /** Cât de mult poate întuneca glisorul / How far the slider can dim. */
    static int maxDim(Context c, boolean fullCover) {
        if (fullCover) return FULL_COVER_MAX_DIM;
        return Math.min(FULL_COVER_MAX_DIM, (int) Math.floor(touchLimit(c) * 100f + 0.001f));
    }

    /** Dacă Android limitează opacitatea stratului normal / Whether Android limits the normal layer's opacity. */
    static boolean touchLimited() {
        return Build.VERSION.SDK_INT >= 31;
    }

    private static float maxAlpha(Context c, boolean fullCover) {
        if (fullCover || !touchLimited()) return SAFE_MAX_ALPHA;
        // Puțin sub limită, ca rotunjirile să nu blocheze atingerile.
        // Slightly under the limit so rounding never blocks touches.
        return Math.max(0f, touchLimit(c) - 0.005f);
    }

    private static float touchLimit(Context c) {
        if (!touchLimited()) return SAFE_MAX_ALPHA;
        try {
            InputManager im = (InputManager) c.getSystemService(Context.INPUT_SERVICE);
            float f = im.getMaximumObscuringOpacityForTouch();
            if (f > 0f && f <= 1f) return f;
        } catch (RuntimeException ignored) {
        }
        return 0.8f;
    }
}
