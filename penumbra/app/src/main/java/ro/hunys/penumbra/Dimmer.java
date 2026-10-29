/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.TileService;

/**
 * Locul unic care aplică setările: decide cine desenează stratul și pornește/oprește serviciul.
 * The single place that applies the settings: decides who draws the layer and starts/stops the service.
 */
final class Dimmer {
    private Dimmer() {}

    /** Permisiunea „Afișare peste alte aplicații” / The "Display over other apps" permission. */
    static boolean canDrawOverlays(Context c) {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(c);
    }

    /** Acoperirea completă e posibilă pe acest Android / Full cover is possible on this Android. */
    static boolean fullCoverSupported() {
        return Build.VERSION.SDK_INT >= 22;
    }

    /** Serviciul de accesibilitate e pornit în setări / The accessibility service is enabled in settings. */
    static boolean fullCoverServiceOn() {
        return FullCoverService.instance != null;
    }

    /** Stratul e desenat acum de serviciul de accesibilitate / The layer is drawn by the accessibility service right now. */
    static boolean fullCoverInUse(Context c) {
        return fullCoverSupported() && Prefs.isFullCover(Prefs.get(c)) && fullCoverServiceOn();
    }

    /** Se poate întuneca ecranul acum / The screen can be dimmed right now. */
    static boolean canDim(Context c) {
        return fullCoverInUse(c) || canDrawOverlays(c);
    }

    /**
     * Aplică setările peste tot. Întoarce false dacă serviciul nu a putut porni.
     * Applies the settings everywhere. Returns false if the service could not start.
     */
    static boolean apply(Context c) {
        SharedPreferences sp = Prefs.get(c);
        boolean ok = true;

        FullCoverService a11y = FullCoverService.instance;
        if (a11y != null) a11y.refresh();

        if (Prefs.isActive(sp)) {
            DimService s = DimService.instance;
            if (s != null) {
                s.refresh();
            } else {
                ok = startService(c);
            }
        } else if (DimService.instance != null) {
            c.stopService(new Intent(c, DimService.class));
        }
        return ok;
    }

    /** Pornește sau oprește și aplică / Turns on or off and applies. */
    static boolean setActive(Context c, boolean on) {
        Prefs.setActive(Prefs.get(c), on);
        boolean ok = apply(c);
        if (on && !ok && !fullCoverInUse(c)) {
            // Fără serviciu nu rămâne nimic pe ecran / Without the service nothing stays on screen.
            Prefs.setActive(Prefs.get(c), false);
        }
        refreshTile(c);
        return ok;
    }

    private static boolean startService(Context c) {
        Intent i = new Intent(c, DimService.class);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                c.startForegroundService(i);
            } else {
                c.startService(i);
            }
            return true;
        } catch (RuntimeException e) {
            // Android poate refuza pornirea din fundal / Android may refuse a start from the background.
            return false;
        }
    }

    static void refreshTile(Context c) {
        if (Build.VERSION.SDK_INT < 24) return;
        try {
            TileService.requestListeningState(c, new ComponentName(c, DimTile.class));
        } catch (RuntimeException ignored) {
        }
    }
}
