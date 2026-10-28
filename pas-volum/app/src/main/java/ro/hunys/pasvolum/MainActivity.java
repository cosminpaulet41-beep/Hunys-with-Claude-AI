/*
 * Pas Volum / Volume Step
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Ecranul de setări, făcut pentru telecomandă (D-pad) și ecran de proiector 720p.
 * Settings screen, built for a remote (D-pad) and a 720p projector screen.
 */
package ro.hunys.pasvolum;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(16, 19, 26);
    private static final int CARD = Color.rgb(30, 35, 46);
    private static final int TEXT = Color.rgb(236, 239, 244);
    private static final int MUTED = Color.rgb(150, 160, 178);
    private static final int ACCENT = Color.rgb(255, 179, 0);
    private static final int GOOD = Color.rgb(76, 217, 123);
    private static final int BAD = Color.rgb(255, 107, 107);
    private static final int WARN = Color.rgb(255, 202, 87);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private AudioManager audio;
    private Txt txt;

    private TextView statusView;
    private TextView stepValueView;
    private TextView exampleView;
    private Button featureButton;
    private TextView volumeView;
    private TextView lastKeyView;
    private Button plusButton;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshLive();
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = Prefs.get(this);
        audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        buildUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Prefs.isEnabled(prefs)) {
            VolumeWatchService.start(this); // ne asigurăm că rulează / make sure it runs
        }
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(ticker);
        super.onPause();
    }

    // ---------------------------------------------------------------- UI

    private void buildUi() {
        txt = Txt.forLang(Prefs.getLang(prefs));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(40), dp(24), dp(40), dp(20));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView title = text(txt.title(), 34, TEXT, true);
        root.addView(title);
        TextView sub = text(txt.subtitle(), 17, MUTED, false);
        sub.setPadding(0, 0, 0, dp(16));
        root.addView(sub);

        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(cols, new LinearLayout.LayoutParams(-1, -2));

        // ---- Coloana stânga: controale / Left column: controls
        LinearLayout left = card();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.rightMargin = dp(12);
        cols.addView(left, lp);

        statusView = text("", 18, TEXT, true);
        left.addView(statusView);
        left.addView(spacer(14));

        left.addView(text(txt.stepLabel(), 18, MUTED, false));

        LinearLayout stepRow = new LinearLayout(this);
        stepRow.setOrientation(LinearLayout.HORIZONTAL);
        stepRow.setGravity(Gravity.CENTER_VERTICAL);
        left.addView(stepRow, new LinearLayout.LayoutParams(-1, -2));

        Button minus = button("−");
        minus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStep(-1);
            }
        });
        stepRow.addView(minus, new LinearLayout.LayoutParams(dp(84), dp(64)));

        stepValueView = text("", 44, ACCENT, true);
        stepValueView.setGravity(Gravity.CENTER);
        stepRow.addView(stepValueView, new LinearLayout.LayoutParams(dp(110), -2));

        plusButton = button("+");
        plusButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStep(1);
            }
        });
        stepRow.addView(plusButton, new LinearLayout.LayoutParams(dp(84), dp(64)));

        exampleView = text("", 17, MUTED, false);
        exampleView.setPadding(0, dp(8), 0, dp(14));
        left.addView(exampleView);

        featureButton = button("");
        featureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean on = !Prefs.isEnabled(prefs);
                Prefs.setEnabled(prefs, on);
                if (on) {
                    VolumeWatchService.start(MainActivity.this);
                } else {
                    VolumeWatchService.stop(MainActivity.this);
                }
                refreshStatic();
            }
        });
        left.addView(featureButton, fullWidthButtonParams());

        Button lang = button(txt.language());
        lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.setLang(prefs, txt.ro ? Prefs.LANG_EN : Prefs.LANG_RO);
                buildUi();
                if (Prefs.isEnabled(prefs)) VolumeWatchService.start(MainActivity.this);
                refreshLive();
            }
        });
        left.addView(lang, fullWidthButtonParams());

        // ---- Coloana dreaptă: informații / Right column: info
        LinearLayout right = card();
        cols.addView(right, new LinearLayout.LayoutParams(0, -2, 1f));

        volumeView = text("", 20, TEXT, true);
        right.addView(volumeView);
        right.addView(spacer(12));

        right.addView(text(txt.lastKeyLabel(), 17, MUTED, false));
        lastKeyView = text("", 19, TEXT, true);
        right.addView(lastKeyView);
        right.addView(spacer(16));

        right.addView(text(txt.helpTitle(), 19, ACCENT, true));
        TextView help = text(txt.helpBody(), 16, TEXT, false);
        help.setLineSpacing(0, 1.15f);
        right.addView(help);

        TextView footer = text(txt.footer(), 14, MUTED, false);
        footer.setPadding(0, dp(16), 0, 0);
        root.addView(footer);

        setContentView(scroll);
        refreshStatic();
        plusButton.requestFocus();
    }

    private void refreshStatic() {
        int step = Prefs.getStep(prefs);
        stepValueView.setText(String.valueOf(step));
        featureButton.setText(Prefs.isEnabled(prefs) ? txt.featureOn() : txt.featureOff());
        refreshLive();
    }

    private void refreshLive() {
        if (statusView == null) return;

        // Stare serviciu / Service state
        if (!Prefs.isEnabled(prefs)) {
            statusView.setText(txt.serviceLabel() + " " + txt.serviceOff());
            statusView.setTextColor(BAD);
        } else if (VolumeWatchService.running) {
            statusView.setText(txt.serviceLabel() + " " + txt.serviceOn());
            statusView.setTextColor(GOOD);
        } else {
            statusView.setText(txt.serviceLabel() + " " + txt.serviceStarting());
            statusView.setTextColor(WARN);
        }

        // Volum + exemplu / Volume + example
        int step = Prefs.getStep(prefs);
        int cur = 0;
        int max = 100;
        try {
            cur = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } catch (RuntimeException ignored) {
        }
        volumeView.setText(txt.volumeNow(cur, max));
        int a = Math.min(cur, max);
        int b = Math.min(a + step, max);
        int c = Math.min(b + step, max);
        exampleView.setText(txt.example(a, b, c));

        // Ultima tastă / Last key
        int key = Prefs.getLastKey(prefs);
        long when = Prefs.getLastKeyTime(prefs);
        if (key == 0 || when == 0) {
            lastKeyView.setText(txt.lastKeyNone());
        } else {
            long secs = Math.max(0, (System.currentTimeMillis() - when) / 1000);
            String name = key == KeyEvent.KEYCODE_VOLUME_UP ? txt.keyUp() : txt.keyDown();
            lastKeyView.setText(name + "  (" + txt.secondsAgo(secs) + ")");
        }
    }

    private void changeStep(int delta) {
        Prefs.setStep(prefs, Prefs.getStep(prefs) + delta);
        if (Prefs.isEnabled(prefs)) VolumeWatchService.start(this); // actualizează notificarea
        refreshStatic();
    }

    // ---------------------------------------------------------------- helpers

    private LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(22), dp(18), dp(22), dp(18));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(14));
        l.setBackground(bg);
        return l;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private View spacer(int heightDp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(heightDp)));
        return v;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setFocusable(true);
        b.setFocusableInTouchMode(false);
        b.setPadding(dp(16), dp(10), dp(16), dp(10));
        b.setMinHeight(dp(52));
        b.setMinimumHeight(dp(52));
        b.setStateListAnimator(null);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_pressed}, rounded(Color.rgb(214, 148, 0), 0));
        states.addState(new int[] {android.R.attr.state_focused}, rounded(ACCENT, Color.WHITE));
        states.addState(new int[] {}, rounded(Color.rgb(48, 55, 70), 0));
        b.setBackground(states);

        int[][] st = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused},
                new int[] {}
        };
        int[] colors = new int[] {Color.BLACK, Color.BLACK, TEXT};
        b.setTextColor(new ColorStateList(st, colors));
        return b;
    }

    private GradientDrawable rounded(int fill, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(12));
        if (stroke != 0) g.setStroke(dp(3), stroke);
        return g;
    }

    private LinearLayout.LayoutParams fullWidthButtonParams() {
        // Înălțime flexibilă, ca textele lungi să încapă pe 2 rânduri.
        // Flexible height so long labels can wrap to 2 lines.
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(10);
        return p;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
}
