/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * „Acoperire completă”: un serviciu de accesibilitate care doar desenează stratul, deasupra barelor
 * de sistem. Nu ascultă evenimente și nu citește conținutul ecranului.
 * "Full cover": an accessibility service that only draws the layer, above the system bars.
 * It listens to no events and reads no screen content.
 */
public class FullCoverService extends AccessibilityService {
    static FullCoverService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Overlay overlay;

    private final Runnable refreshLater = new Runnable() {
        @Override
        public void run() {
            refresh();
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        if (Dimmer.fullCoverSupported()) {
            overlay = new Overlay(this, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        }
        // Preia stratul de la serviciul normal / Takes over the layer from the normal service.
        Dimmer.apply(this);
    }

    /** Arată stratul doar dacă e pornit și e aleasă acoperirea completă / Shows the layer only when on and full cover is chosen. */
    void refresh() {
        if (overlay == null) return;
        SharedPreferences sp = Prefs.get(this);
        if (Prefs.isActive(sp) && Prefs.isFullCover(sp)) {
            overlay.show(Look.of(this, true));
        } else {
            overlay.hide();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Nimic: nu avem nevoie de evenimente / Nothing: we need no events.
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refresh();
        handler.removeCallbacks(refreshLater);
        handler.postDelayed(refreshLater, 400);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        release();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }

    private void release() {
        if (instance != this) return;
        instance = null;
        handler.removeCallbacksAndMessages(null);
        if (overlay != null) overlay.hide();
        // Serviciul normal preia stratul înapoi / The normal service takes the layer back.
        Dimmer.apply(this);
    }
}
