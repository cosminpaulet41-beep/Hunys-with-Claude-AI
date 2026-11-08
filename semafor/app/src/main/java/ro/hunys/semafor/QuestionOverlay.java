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
import android.os.PowerManager;
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

    /** Cât timp o întrebare pregătită e bună de arătat / How long a preloaded question is good to show. */
    private static final long READY_MAX_MS = 30L * 60L * 1000L;
    /** După cât timp o reîmprospătăm la stingerea/aprinderea ecranului / When to refresh it on screen off/on. */
    private static final long REFRESH_AFTER_MS = 5L * 60L * 1000L;
    private boolean destroyed;
    private PowerManager.WakeLock wakeLock;
    private int lastW;
    private int lastH;

    private void ensureView() {
        if (view == null) {
            long t = SystemClock.elapsedRealtime();
            view = new QuestionView(ctx, this, true);
            Jurnal.log("strat", "pagina creată / view created în " + (SystemClock.elapsedRealtime() - t) + " ms");
        }
    }

    /**
     * Pregătește următoarea întrebare ascunsă, ca la deblocare să apară imediat.
     * Prepares the next question hidden, so it appears instantly on unlock.
     *
     * @param force chiar dacă e deja una pregătită (ex. după ce ai răspuns) / even if one is ready (e.g. after answering)
     */
    void preload(String why, boolean force) {
        if (destroyed) return;
        try {
            ensureView();
            if (shown) return;
            if (view.isLoading(15000)) return;
            if (!force && view.isReady(REFRESH_AFTER_MS)) return;
            sizeOffscreen();
            holdAwake();
            view.resume();
            view.load(null);
            Jurnal.log("strat", "pregătesc întrebarea ascunsă / preloading hidden question (" + why + ")");
        } catch (Throwable e) {
            Jurnal.error("strat", "preload", e);
        }
    }

    /** Ține telefonul treaz 15 s, cât se încarcă pagina cu ecranul stins / Keeps the phone awake 15 s while loading with the screen off. */
    private void holdAwake() {
        try {
            if (wakeLock == null) {
                PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "semafor:preload");
                wakeLock.setReferenceCounted(false);
            }
            wakeLock.acquire(15000);
        } catch (Throwable e) {
            Jurnal.error("strat", "wakeLock", e);
        }
    }

    /** O pagină ascunsă trebuie să știe cât de mare e ecranul / A hidden page needs to know the screen size. */
    private void sizeOffscreen() {
        int w = lastW;
        int h = lastH;
        if (w <= 0 || h <= 0) {
            android.util.DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            w = dm.widthPixels;
            h = dm.heightPixels;
        }
        view.measure(android.view.View.MeasureSpec.makeMeasureSpec(w, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(h, android.view.View.MeasureSpec.EXACTLY));
        view.layout(0, 0, w, h);
    }

    /** Arată întrebarea (pregătită dinainte, dacă există) / Shows the question (preloaded, if there is one). */
    boolean show() {
        try {
            ensureView();
            boolean ready = view.isReady(READY_MAX_MS);
            boolean loading = view.isLoading(15000);
            if (!shown) {
                wm.addView(view, params());
                shown = true;
                Jurnal.log("strat", "addView OK (pus peste ecran / added over the screen)");
            } else {
                Jurnal.log("strat", "era deja pe ecran / was already shown");
            }
            view.resume();
            if (ready) {
                Jurnal.log("strat", "INSTANT: întrebare pregătită de / question prepared " + (view.readyAge() / 1000) + " s în urmă / ago");
            } else if (loading) {
                Jurnal.log("strat", "se pregătea deja, continuă / was already preparing, continuing");
            } else {
                view.load(null);
            }
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
                if (v.isAttachedToWindow() && v.getWidth() > 0) {
                    lastW = v.getWidth();
                    lastH = v.getHeight();
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
        // Următoarea întrebare se pregătește de acum / The next question starts preparing now
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                preload("după închidere / after closing", true);
            }
        }, 400);
    }

    void destroy() {
        destroyed = true;
        hide();
        handler.removeCallbacksAndMessages(null);
        try {
            if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        } catch (Throwable ignored) {
        }
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
        lp.windowAnimations = 0; // fără animație: apare imediat / no animation: appears at once
        return lp;
    }
}
