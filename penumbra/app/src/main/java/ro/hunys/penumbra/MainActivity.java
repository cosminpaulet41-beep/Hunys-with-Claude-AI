/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Ecranul de setări. Merge cu degetul și cu telecomanda (D-pad).
 * The settings screen. Works by touch and with a remote (D-pad).
 */
package ro.hunys.penumbra;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(11, 13, 19);
    private static final int CARD = Color.rgb(24, 28, 38);
    private static final int BTN = Color.rgb(44, 50, 64);
    private static final int TEXT = Color.rgb(236, 239, 244);
    private static final int MUTED = Color.rgb(148, 158, 176);
    private static final int ACCENT = Color.rgb(255, 168, 54);
    private static final int ON = Color.rgb(92, 214, 140);
    private static final int WARN = Color.rgb(255, 120, 110);

    private static final int DIM_STEP = 5;
    private static final int WARM_STEP = 10;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private Txt txt;
    private boolean pendingStart;
    private int builtMax = -1;

    private Button powerButton;
    private LinearLayout permCard;
    private TextView dimValue;
    private SeekBar dimBar;
    private TextView warmValue;
    private SeekBar warmBar;
    private Button minLightButton;
    private Button fullCoverButton;
    private TextView fullCoverStatus;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshUi();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = Prefs.get(this);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        buildUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingStart && Dimmer.canDim(this)) {
            pendingStart = false;
            turnOn();
        } else if (Prefs.isActive(prefs)) {
            Dimmer.apply(this); // ne asigurăm că rulează / make sure it runs
        }
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(ticker);
        super.onPause();
    }

    // ---------------------------------------------------------------- acțiuni / actions

    private void togglePower() {
        if (Prefs.isActive(prefs)) {
            Dimmer.setActive(this, false);
        } else if (!Dimmer.canDim(this)) {
            pendingStart = true;
            openOverlaySettings();
        } else {
            turnOn();
        }
        refreshUi();
    }

    private void turnOn() {
        askNotificationsOnce();
        if (!Dimmer.setActive(this, true)) {
            Toast.makeText(this, txt.startFailed(), Toast.LENGTH_LONG).show();
        }
        refreshUi();
    }

    /** O schimbare de nivel pornește stratul, ca să se vadă efectul / A level change turns the layer on so the effect shows. */
    private void levelChanged() {
        if (Prefs.isActive(prefs)) {
            Dimmer.apply(this);
        } else if (Dimmer.canDim(this)) {
            turnOn();
        }
        refreshUi();
    }

    private void setDim(int v) {
        Prefs.setDim(prefs, Prefs.clamp(v, 0, maxDim()));
        levelChanged();
    }

    private void setWarm(int v) {
        Prefs.setWarm(prefs, v);
        levelChanged();
    }

    private void toggleMinLight() {
        Prefs.setMinLight(prefs, !Prefs.isMinLight(prefs));
        Dimmer.apply(this);
        refreshUi();
    }

    private void toggleFullCover() {
        boolean on = !Prefs.isFullCover(prefs);
        Prefs.setFullCover(prefs, on);
        if (on && !Dimmer.fullCoverServiceOn()) {
            openAccessibilitySettings();
        }
        Dimmer.apply(this);
        refreshUi();
    }

    private void toggleLanguage() {
        Prefs.setLang(prefs, txt.ro ? Prefs.LANG_EN : Prefs.LANG_RO);
        buildUi();
        Dimmer.apply(this); // notificarea în limba nouă / notification in the new language
    }

    private void openOverlaySettings() {
        if (Build.VERSION.SDK_INT < 23) return;
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        if (tryStart(i)) return;
        if (tryStart(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))) return;
        tryStart(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
    }

    private void openAccessibilitySettings() {
        if (tryStart(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) {
            Toast.makeText(this, txt.openA11yToast(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean tryStart(Intent i) {
        try {
            startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void askNotificationsOnce() {
        if (Build.VERSION.SDK_INT < 33 || Prefs.askedNotifications(prefs)) return;
        Prefs.setAskedNotifications(prefs);
        if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {"android.permission.POST_NOTIFICATIONS"}, 1);
        }
    }

    private int maxDim() {
        return Look.maxDim(this, Dimmer.fullCoverInUse(this));
    }

    // ---------------------------------------------------------------- UI

    private void buildUi() {
        txt = Txt.of(this);
        builtMax = maxDim();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(24));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        root.addView(text(txt.title(), 32, TEXT, true));
        TextView sub = text(txt.subtitle(), 15, MUTED, false);
        sub.setPadding(0, dp(2), 0, dp(16));
        root.addView(sub);

        powerButton = button("");
        powerButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        powerButton.setTypeface(Typeface.DEFAULT_BOLD);
        powerButton.setMinHeight(dp(68));
        powerButton.setMinimumHeight(dp(68));
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePower();
            }
        });
        root.addView(powerButton, new LinearLayout.LayoutParams(-1, -2));

        // ---- Permisiune lipsă / Missing permission
        permCard = card();
        permCard.addView(text(txt.permTitle(), 18, WARN, true));
        TextView pb = text(txt.permBody(), 15, TEXT, false);
        pb.setPadding(0, dp(4), 0, dp(4));
        permCard.addView(pb);
        Button permBtn = button(txt.permButton());
        permBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingStart = true;
                openOverlaySettings();
            }
        });
        permCard.addView(permBtn, topMargin(8));
        root.addView(permCard, cardParams());

        // ---- Întunecare / Dimming
        LinearLayout dimCard = card();
        dimValue = valueRow(dimCard, txt.dimLabel());
        dimBar = new SeekBar(this);
        dimBar.setMax(builtMax);
        dimBar.setKeyProgressIncrement(DIM_STEP);
        dimBar.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) setDim(progress);
            }
        });
        dimCard.addView(sliderRow(dimBar, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDim(currentDim() - DIM_STEP);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDim(currentDim() + DIM_STEP);
            }
        }));

        LinearLayout presets = new LinearLayout(this);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        int[] wanted = new int[] {30, 50, 70};
        java.util.ArrayList<Integer> levels = new java.util.ArrayList<Integer>();
        for (int w : wanted) {
            if (w < builtMax) levels.add(w);
        }
        levels.add(builtMax);
        for (int i = 0; i < levels.size(); i++) {
            final int level = levels.get(i);
            Button p = button(level + "%");
            p.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            p.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDim(level);
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
            if (i > 0) lp.leftMargin = dp(8);
            presets.addView(p, lp);
        }
        dimCard.addView(presets, topMargin(10));

        if (Look.touchLimited() && !Dimmer.fullCoverInUse(this)) {
            TextView note = text(txt.dimLimit(builtMax), 13, MUTED, false);
            note.setPadding(0, dp(10), 0, 0);
            dimCard.addView(note);
        }
        root.addView(dimCard, cardParams());

        // ---- Filtru cald / Warm filter
        LinearLayout warmCard = card();
        warmValue = valueRow(warmCard, txt.warmLabel());
        warmBar = new SeekBar(this);
        warmBar.setMax(100);
        warmBar.setKeyProgressIncrement(WARM_STEP);
        warmBar.setOnSeekBarChangeListener(new SimpleSeek() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) setWarm(progress);
            }
        });
        warmCard.addView(sliderRow(warmBar, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWarm(Prefs.getWarm(prefs) - WARM_STEP);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWarm(Prefs.getWarm(prefs) + WARM_STEP);
            }
        }));
        TextView wh = text(txt.warmHelp(), 13, MUTED, false);
        wh.setPadding(0, dp(8), 0, 0);
        warmCard.addView(wh);
        root.addView(warmCard, cardParams());

        // ---- Opțiuni / Options
        LinearLayout opt = card();
        opt.addView(text(txt.optionsTitle(), 18, ACCENT, true));

        minLightButton = button("");
        minLightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMinLight();
            }
        });
        opt.addView(minLightButton, topMargin(10));
        opt.addView(help(txt.minLightHelp()));

        if (Dimmer.fullCoverSupported()) {
            fullCoverButton = button("");
            fullCoverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFullCover();
                }
            });
            opt.addView(fullCoverButton, topMargin(14));
            opt.addView(help(txt.fullCoverHelp()));
            fullCoverStatus = text("", 14, TEXT, true);
            fullCoverStatus.setPadding(0, dp(6), 0, 0);
            opt.addView(fullCoverStatus);
            if (Build.VERSION.SDK_INT >= 33) opt.addView(help(txt.fullCoverRestricted()));
        } else {
            fullCoverButton = null;
            fullCoverStatus = null;
        }

        Button lang = button(txt.language());
        lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLanguage();
            }
        });
        opt.addView(lang, topMargin(16));
        root.addView(opt, cardParams());

        // ---- Sfaturi / Tips
        LinearLayout tips = card();
        tips.addView(text(txt.tipsTitle(), 18, ACCENT, true));
        TextView tb = text(txt.tipsBody(), 14, TEXT, false);
        tb.setLineSpacing(0, 1.2f);
        tb.setPadding(0, dp(6), 0, 0);
        tips.addView(tb);
        root.addView(tips, cardParams());

        TextView footer = text(txt.footer(), 13, MUTED, false);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        footer.setPadding(0, dp(18), 0, 0);
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));

        setContentView(scroll);
        refreshUi();
        powerButton.requestFocus();
    }

    private void refreshUi() {
        if (powerButton == null) return;
        if (maxDim() != builtMax) {
            // Limita s-a schimbat (acoperire completă pornită/oprită) / The limit changed (full cover on/off).
            buildUi();
            return;
        }

        boolean active = Prefs.isActive(prefs);
        powerButton.setText(active ? txt.powerOn() : txt.powerOff());
        paint(powerButton, active ? ON : BTN, active ? Color.BLACK : TEXT);

        permCard.setVisibility(Dimmer.canDim(this) ? View.GONE : View.VISIBLE);

        int dim = currentDim();
        dimValue.setText(dim + "%");
        if (dimBar.getProgress() != dim) dimBar.setProgress(dim);

        int warm = Prefs.getWarm(prefs);
        warmValue.setText(warm + "%");
        if (warmBar.getProgress() != warm) warmBar.setProgress(warm);

        boolean minLight = Prefs.isMinLight(prefs);
        minLightButton.setText(txt.minLight(minLight));
        paint(minLightButton, minLight ? ACCENT : BTN, minLight ? Color.BLACK : TEXT);

        if (fullCoverButton != null) {
            boolean full = Prefs.isFullCover(prefs);
            fullCoverButton.setText(txt.fullCover(full));
            paint(fullCoverButton, full ? ACCENT : BTN, full ? Color.BLACK : TEXT);
            boolean svc = Dimmer.fullCoverServiceOn();
            fullCoverStatus.setVisibility(full ? View.VISIBLE : View.GONE);
            fullCoverStatus.setText(svc ? txt.fullCoverServiceOn() : txt.fullCoverServiceOff());
            fullCoverStatus.setTextColor(svc ? ON : WARN);
        }
    }

    private int currentDim() {
        return Math.min(Prefs.getDim(prefs), builtMax);
    }

    // ---------------------------------------------------------------- helpers

    /** Eticheta în stânga, valoarea mare în dreapta / Label on the left, big value on the right. */
    private TextView valueRow(LinearLayout parent, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView l = text(label, 18, TEXT, true);
        row.addView(l, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView v = text("", 30, ACCENT, true);
        row.addView(v, new LinearLayout.LayoutParams(-2, -2));
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
        return v;
    }

    /** [−] glisor [+] / [−] slider [+] */
    private LinearLayout sliderRow(SeekBar bar, View.OnClickListener minus, View.OnClickListener plus) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, 0);

        Button m = button("−");
        m.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        m.setOnClickListener(minus);
        row.addView(m, new LinearLayout.LayoutParams(dp(56), dp(52)));

        bar.setProgressTintList(ColorStateList.valueOf(ACCENT));
        bar.setThumbTintList(ColorStateList.valueOf(ACCENT));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(MUTED));
        bar.setFocusable(true);
        bar.setPadding(dp(18), dp(14), dp(18), dp(14));
        row.addView(bar, new LinearLayout.LayoutParams(0, -2, 1f));

        Button p = button("+");
        p.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        p.setOnClickListener(plus);
        row.addView(p, new LinearLayout.LayoutParams(dp(56), dp(52)));
        return row;
    }

    private TextView help(String s) {
        TextView t = text(s, 13, MUTED, false);
        t.setPadding(0, dp(6), 0, 0);
        return t;
    }

    private LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(14), dp(16), dp(16));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(16));
        l.setBackground(bg);
        return l;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(12);
        return p;
    }

    private LinearLayout.LayoutParams topMargin(int marginDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(marginDp);
        return p;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        b.setFocusable(true);
        b.setPadding(dp(12), dp(8), dp(12), dp(8));
        b.setMinHeight(dp(50));
        b.setMinimumHeight(dp(50));
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setStateListAnimator(null);
        paint(b, BTN, TEXT);
        return b;
    }

    /** Culorile unui buton, cu stare apăsat și focus (telecomandă) / A button's colors, with pressed and focus (remote) states. */
    private void paint(Button b, int fill, int textColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_pressed}, rounded(darker(fill), 0));
        states.addState(new int[] {android.R.attr.state_focused}, rounded(fill, Color.WHITE));
        states.addState(new int[] {}, rounded(fill, 0));
        b.setBackground(states);
        b.setTextColor(textColor);
    }

    private static int darker(int c) {
        return Color.rgb(Color.red(c) * 3 / 4, Color.green(c) * 3 / 4, Color.blue(c) * 3 / 4);
    }

    private GradientDrawable rounded(int fill, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(12));
        if (stroke != 0) g.setStroke(dp(2), stroke);
        return g;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    /** Ascultător de glisor cu metode goale / Slider listener with empty methods. */
    private abstract static class SimpleSeek implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
