/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Întrebarea desenată direct peste ecran, cu permisiunea „Afișare peste alte aplicații”.
 * Spre deosebire de deschiderea unei ferestre din fundal, un strat peste ecran nu poate fi
 * blocat de sistem (Huawei/EMUI blochează deschiderile din fundal).
 *
 * The question drawn straight over the screen, using "Display over other apps".
 * Unlike opening a window from the background, an overlay can't be blocked by the system
 * (Huawei/EMUI blocks background openings).
 */
package ro.hunys.semafor;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.WindowManager;

final class QuestionOverlay implements QuestionView.Host {
    private final Context ctx;
    private final WindowManager wm;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private QuestionView view;
    private boolean shown;

    QuestionOverlay(Service s) {
        ctx = new ContextThemeWrapper(s, android.R.style.Theme_DeviceDefault);
        wm = (WindowManager) s.getSystemService(Context.WINDOW_SERVICE);
    }

    /** Arată întrebarea și merge la următoarea / Shows the question and goes to the next one. */
    boolean show() {
        try {
            if (view == null) {
                long t = SystemClock.elapsedRealtime();
                view = new QuestionView(ctx, this, true);
                Jurnal.log("strat", "pagina creată / view created în " + (SystemClock.elapsedRealtime() - t) + " ms");
            }
            if (!shown) {
                wm.addView(view, params());
                shown = true;
                Jurnal.log("strat", "addView OK (pus peste ecran / added over the screen)");
            } else {
                Jurnal.log("strat", "era deja pe ecran / was already shown");
            }
            view.resume();
            view.load(null);
            view.requestFocus();
            checkLater(500);
            checkLater(2500);
            return true;
        } catch (Throwable e) {
            Jurnal.error("strat", "show() a eșuat / failed", e);
            if (shown && view != null) {
                try {
                    wm.removeView(view);
                } catch (Throwable ignored) {
                }
            }
            shown = false;
            return false;
        }
    }

    /** Notează dacă stratul chiar se vede / Records whether the overlay is really visible. */
    private void checkLater(final long ms) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                QuestionView v = view;
                if (v == null) {
                    Jurnal.log("strat", "+" + ms + " ms: nu mai există / gone");
                    return;
                }
                int[] loc = new int[2];
                try {
                    v.getLocationOnScreen(loc);
                } catch (RuntimeException ignored) {
                }
                Jurnal.log("strat", "+" + ms + " ms: arătat/shown=" + shown
                        + " atașat/attached=" + v.isAttachedToWindow()
                        + " vizibil fereastră/windowVisibility=" + visibilityName(v.getWindowVisibility())
                        + " isShown=" + v.isShown()
                        + " focus=" + v.hasWindowFocus()
                        + " mărime/size=" + v.getWidth() + "x" + v.getHeight()
                        + " poziție/pos=" + loc[0] + "," + loc[1]);
            }
        }, ms);
    }

    static String visibilityName(int v) {
        if (v == android.view.View.VISIBLE) return "VISIBLE";
        if (v == android.view.View.INVISIBLE) return "INVISIBLE";
        if (v == android.view.View.GONE) return "GONE";
        return String.valueOf(v);
    }

    void hide() {
        if (!shown || view == null) return;
        Jurnal.log("strat", "ascuns / hidden");
        view.pause();
        try {
            wm.removeView(view);
        } catch (RuntimeException e) {
            Jurnal.error("strat", "removeView", e);
        }
        shown = false;
    }

    void destroy() {
        hide();
        if (view != null) {
            view.destroy();
            view = null;
        }
    }

    boolean isShown() {
        return shown;
    }

    @Override
    public void onDone() {
        Jurnal.log("strat", "Gata / Done (sau Înapoi / or Back)");
        hide();
    }

    @SuppressWarnings("deprecation")
    private WindowManager.LayoutParams params() {
        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.OPAQUE);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        lp.windowAnimations = android.R.style.Animation_Dialog;
        return lp;
    }
}
