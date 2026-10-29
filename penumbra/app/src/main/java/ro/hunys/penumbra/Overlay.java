/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Stratul colorat întins peste tot ecranul. Nu primește atingeri: totul trece prin el.
 * The colored layer stretched over the whole screen. It takes no touches: everything passes through.
 */
final class Overlay {
    private final Context ctx;
    private final WindowManager wm;
    private final int type;
    private View view;
    private WindowManager.LayoutParams lp;

    Overlay(Context ctx, int type) {
        this.ctx = ctx;
        this.type = type;
        this.wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
    }

    /** Stratul normal, de deasupra aplicațiilor / The normal layer, above apps. */
    @SuppressWarnings("deprecation")
    static int appOverlayType() {
        return Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
    }

    boolean isShown() {
        return view != null;
    }

    /** Arată sau actualizează stratul. Întoarce false dacă sistemul îl refuză. / Shows or updates the layer. Returns false if the system refuses it. */
    boolean show(Look look) {
        try {
            if (view == null) {
                View v = new View(ctx);
                lp = baseParams();
                applyLook(v, look);
                wm.addView(v, lp);
                view = v;
            } else {
                applyLook(view, look);
                wm.updateViewLayout(view, lp);
            }
            return true;
        } catch (RuntimeException e) {
            // Fără permisiune sau fereastră refuzată / No permission or window refused.
            view = null;
            return false;
        }
    }

    void hide() {
        if (view == null) return;
        try {
            wm.removeViewImmediate(view);
        } catch (RuntimeException ignored) {
        }
        view = null;
    }

    private void applyLook(View v, Look look) {
        v.setBackgroundColor(look.color);
        lp.alpha = look.alpha;
        lp.screenBrightness = look.backlight;
        // Mărimea reală a ecranului, în orientarea de acum / Real screen size, in the current orientation.
        Point size = realSize();
        lp.width = size.x;
        lp.height = size.y;
    }

    @SuppressWarnings("deprecation")
    private WindowManager.LayoutParams baseParams() {
        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type, flags, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.LEFT;
        p.x = 0;
        p.y = 0;
        p.setTitle("Penumbra");
        if (Build.VERSION.SDK_INT >= 30) {
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            p.setFitInsetsTypes(0);
            p.setFitInsetsSides(0);
        } else if (Build.VERSION.SDK_INT >= 28) {
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return p;
    }

    @SuppressWarnings("deprecation")
    private Point realSize() {
        Point p = new Point();
        Display d = null;
        try {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            d = dm.getDisplay(Display.DEFAULT_DISPLAY);
        } catch (RuntimeException ignored) {
        }
        if (d == null) d = wm.getDefaultDisplay();
        d.getRealSize(p);
        if (p.x <= 0 || p.y <= 0) {
            p.x = WindowManager.LayoutParams.MATCH_PARENT;
            p.y = WindowManager.LayoutParams.MATCH_PARENT;
        }
        return p;
    }
}
