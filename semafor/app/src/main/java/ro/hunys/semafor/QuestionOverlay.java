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
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.WindowManager;

final class QuestionOverlay implements QuestionView.Host {
    private final Context ctx;
    private final WindowManager wm;
    private QuestionView view;
    private boolean shown;

    QuestionOverlay(Service s) {
        ctx = new ContextThemeWrapper(s, android.R.style.Theme_DeviceDefault);
        wm = (WindowManager) s.getSystemService(Context.WINDOW_SERVICE);
    }

    /** Arată întrebarea și merge la următoarea / Shows the question and goes to the next one. */
    boolean show() {
        try {
            if (view == null) view = new QuestionView(ctx, this, true);
            if (!shown) {
                wm.addView(view, params());
                shown = true;
            }
            view.resume();
            view.load(null);
            view.requestFocus();
            return true;
        } catch (RuntimeException e) {
            shown = false;
            return false;
        }
    }

    void hide() {
        if (!shown || view == null) return;
        view.pause();
        try {
            wm.removeView(view);
        } catch (RuntimeException ignored) {
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
