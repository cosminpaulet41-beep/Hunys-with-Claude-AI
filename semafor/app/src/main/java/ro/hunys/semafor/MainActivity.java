/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Ecranul de setări / The settings screen.
 */
package ro.hunys.semafor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(12, 14, 20);
    private static final int CARD = Color.rgb(25, 29, 39);
    private static final int BTN = Color.rgb(45, 51, 66);
    private static final int TEXT = Color.rgb(236, 239, 244);
    private static final int MUTED = Color.rgb(150, 160, 178);
    private static final int RED = Color.rgb(239, 83, 80);
    private static final int AMBER = Color.rgb(255, 183, 40);
    private static final int GREEN = Color.rgb(76, 200, 120);
    private static final int GREEN_DARK = Color.rgb(30, 110, 62);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private Txt txt;
    private boolean pendingStart;

    private Button powerButton;
    private Button pauseButton;
    private TextView pauseStatus;
    private LinearLayout permCard;
    private TextView modeValue;
    private LinearLayout windowBox;
    private TextView catValue;
    private LinearLayout appBox;
    private TextView appName;
    private TextView appHelp;
    private TextView freqValue;
    private Button nightButton;
    private TextView statsText;
    private TextView lastText;
    private TextView logText;
    private TextView overlayCheck;
    private TextView batteryCheck;
    private Button batteryButton;
    private TextView frozenCheck;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshUi();
            handler.postDelayed(this, 2000);
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
        if (pendingStart) {
            pendingStart = false;
            turnOn();
        } else if (Prefs.isActive(prefs)) {
            Gate.apply(this); // ne asigurăm că rulează / make sure it runs
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
        Jurnal.log("setări", "buton pornire / power button; era pornit/was active=" + Prefs.isActive(prefs));
        if (Prefs.isActive(prefs)) {
            Gate.setActive(this, false);
        } else if (!Gate.canDrawOverlays(this)) {
            // Întâi permisiunea, apoi pornește când revii / First the permission, then it starts when you come back.
            pendingStart = true;
            openOverlaySettings();
        } else {
            turnOn();
        }
        refreshUi();
    }

    private void turnOn() {
        askNotificationsOnce();
        if (!Gate.setActive(this, true)) {
            Toast.makeText(this, txt.startFailed(), Toast.LENGTH_LONG).show();
        }
        if (Prefs.getMode(prefs) == Prefs.MODE_APP && Gate.targetPackage(this).length() == 0) showPicker();
        refreshUi();
    }

    private void togglePause() {
        if (Prefs.isPaused(prefs)) {
            Prefs.setPauseUntil(prefs, 0L);
        } else {
            Prefs.setPauseUntil(prefs, System.currentTimeMillis() + UnlockService.PAUSE_MS);
        }
        Gate.apply(this); // notificarea se actualizează / the notification updates
        refreshUi();
    }

    private void tryNow() {
        Jurnal.log("încearcă", "apăsat / pressed; mod=" + Prefs.getMode(prefs) + " pornit/active=" + Prefs.isActive(prefs)
                + " serviciu/service=" + UnlockService.isRunning() + " overlay=" + Gate.canDrawOverlays(this));
        if (Prefs.getMode(prefs) == Prefs.MODE_APP && Gate.targetPackage(this).length() == 0) {
            showPicker();
        } else if (Prefs.getMode(prefs) == Prefs.MODE_WINDOW && Prefs.isActive(prefs) && UnlockService.showOverlayNow()) {
            // Exact ca la deblocare / Exactly as on unlock
        } else if (!Gate.openNow(this)) {
            Toast.makeText(this, txt.openFailed(), Toast.LENGTH_LONG).show();
        }
    }

    /** Fereastra pe prima pagină a site-ului, ca să intri în cont / The window on the site's home page, to sign in. */
    private void openSignIn() {
        tryStart(new Intent(this, QuestionActivity.class).putExtra(QuestionActivity.EXTRA_URL, Site.HOME));
    }

    private void showMode() {
        String[] names = {txt.modeName(Prefs.MODE_WINDOW), txt.modeName(Prefs.MODE_APP)};
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(txt.modeTitle())
                .setSingleChoiceItems(names, Prefs.getMode(prefs), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Prefs.setMode(prefs, which == 1 ? Prefs.MODE_APP : Prefs.MODE_WINDOW);
                        d.dismiss();
                        Gate.apply(MainActivity.this);
                        refreshUi();
                        if (which == 1 && Gate.targetPackage(MainActivity.this).length() == 0) showPicker();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showCategory() {
        int n = Site.SLUGS.length;
        String[] names = new String[n];
        for (int i = 0; i < n; i++) names[i] = txt.category(i);
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(txt.changeCategory())
                .setSingleChoiceItems(names, Prefs.getCategory(prefs), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Prefs.setCategory(prefs, which);
                        d.dismiss();
                        Gate.apply(MainActivity.this);
                        refreshUi();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void toggleNight() {
        Prefs.setNightQuiet(prefs, !Prefs.isNightQuiet(prefs));
        refreshUi();
    }

    private void toggleLanguage() {
        Prefs.setLang(prefs, txt.ro ? Prefs.LANG_EN : Prefs.LANG_RO);
        buildUi();
        Gate.apply(this); // notificarea în limba nouă / notification in the new language
    }

    private void showFrequency() {
        int n = Prefs.FREQ_EVERY.length;
        String[] names = new String[n];
        for (int i = 0; i < n; i++) names[i] = txt.freqName(i);
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(txt.freqTitle())
                .setSingleChoiceItems(names, Prefs.getFreq(prefs), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Prefs.setFreq(prefs, which);
                        d.dismiss();
                        Gate.apply(MainActivity.this);
                        refreshUi();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Lista aplicațiilor, cu cele de chestionare primele / The app list, quiz apps first. */
    private void showPicker() {
        final PackageManager pm = getPackageManager();
        final List<AppEntry> apps = new ArrayList<AppEntry>();
        HashSet<String> seen = new HashSet<String>();
        List<ResolveInfo> list;
        try {
            list = Gate.launcherApps(this);
        } catch (RuntimeException e) {
            list = new ArrayList<ResolveInfo>();
        }
        for (ResolveInfo r : list) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
            AppEntry e = new AppEntry();
            e.pkg = pkg;
            e.info = r;
            e.label = String.valueOf(r.loadLabel(pm));
            e.sortKey = Gate.plain(e.label);
            e.match = Gate.looksLikeQuizApp(e.label, pkg);
            apps.add(e);
        }
        Collections.sort(apps, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry a, AppEntry b) {
                if (a.match != b.match) return a.match ? -1 : 1;
                return a.sortKey.compareTo(b.sortKey);
            }
        });
        final String current = Prefs.getPackage(prefs);

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return apps.size();
            }

            @Override
            public Object getItem(int position) {
                return apps.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                AppRow row = convertView instanceof AppRow ? (AppRow) convertView : new AppRow();
                AppEntry e = apps.get(position);
                if (e.icon == null) {
                    try {
                        e.icon = e.info.loadIcon(pm);
                    } catch (RuntimeException ignored) {
                    }
                }
                row.icon.setImageDrawable(e.icon);
                row.label.setText(e.label);
                row.label.setTextColor(e.pkg.equals(current) ? AMBER : TEXT);
                row.tag.setText(e.match ? txt.suggested() : "");
                row.tag.setVisibility(e.match ? View.VISIBLE : View.GONE);
                return row;
            }
        };

        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(txt.pickTitle())
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        Prefs.setPackage(prefs, apps.get(which).pkg);
                        Gate.apply(MainActivity.this);
                        refreshUi();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openOverlaySettings() {
        if (Build.VERSION.SDK_INT < 23) return;
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        if (tryStart(i)) return;
        if (tryStart(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))) return;
        openAppSettings();
    }

    private void openAppSettings() {
        tryStart(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
    }

    /** Ecranul Huawei „Lansare aplicații” / The Huawei "App launch" screen. */
    private void openLaunchSettings() {
        String[][] screens = {
                {"com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},
                {"com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"},
                {"com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"},
                {"com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},
        };
        for (String[] s : screens) {
            Intent i = new Intent().setComponent(new ComponentName(s[0], s[1])).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (tryStart(i)) return;
        }
        if (Build.VERSION.SDK_INT >= 23 && tryStart(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))) return;
        openAppSettings();
    }

    private boolean tryStart(Intent i) {
        try {
            startActivity(i);
            return true;
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

    private boolean ignoresBattery() {
        if (Build.VERSION.SDK_INT < 23) return true;
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(getPackageName());
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Cere scoaterea restricțiilor de baterie / Asks to lift the battery restrictions. */
    private void requestBattery() {
        if (Build.VERSION.SDK_INT < 23) return;
        Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
        if (tryStart(i)) return;
        if (tryStart(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))) return;
        openAppSettings();
    }

    private static boolean isHuawei() {
        String m = (Build.MANUFACTURER + " " + Build.BRAND).toLowerCase(Locale.ROOT);
        return m.contains("huawei") || m.contains("honor");
    }

    // ---------------------------------------------------------------- UI

    private void buildUi() {
        txt = Txt.of(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(24));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        // ---- Titlu cu cele trei lumini / Title with the three lights
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(lights(), new LinearLayout.LayoutParams(-2, -2));
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(14), 0, 0, 0);
        titles.addView(text(txt.title(), 32, TEXT, true));
        titles.addView(text(txt.subtitle(), 15, MUTED, false));
        head.addView(titles, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(head, new LinearLayout.LayoutParams(-1, -2));

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
        root.addView(powerButton, topMargin(18));

        pauseButton = button("");
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePause();
            }
        });
        root.addView(pauseButton, topMargin(8));
        pauseStatus = text("", 14, AMBER, false);
        pauseStatus.setGravity(Gravity.CENTER_HORIZONTAL);
        pauseStatus.setPadding(0, dp(6), 0, 0);
        root.addView(pauseStatus, new LinearLayout.LayoutParams(-1, -2));

        // ---- Permisiune lipsă / Missing permission
        permCard = card();
        permCard.addView(text(txt.permTitle(), 18, RED, true));
        permCard.addView(help(txt.permBody(), TEXT, 15));
        Button permBtn = button(txt.permButton());
        permBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOverlaySettings();
            }
        });
        permCard.addView(permBtn, topMargin(10));
        root.addView(permCard, cardParams());

        // ---- Unde se deschide / Where it opens
        LinearLayout modeCard = card();
        modeCard.addView(text(txt.modeTitle(), 18, TEXT, true));
        modeValue = text("", 20, AMBER, true);
        modeValue.setPadding(0, dp(4), 0, 0);
        modeCard.addView(modeValue);
        Button modeBtn = button(txt.change());
        modeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMode();
            }
        });
        modeCard.addView(modeBtn, topMargin(10));

        // Fereastra Semafor / Semafor window
        windowBox = new LinearLayout(this);
        windowBox.setOrientation(LinearLayout.VERTICAL);
        catValue = text("", 17, TEXT, true);
        catValue.setPadding(0, dp(12), 0, 0);
        windowBox.addView(catValue);
        windowBox.addView(help(txt.windowHelp(), MUTED, 13));
        LinearLayout winRow = row();
        Button signIn = button(txt.signIn());
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignIn();
            }
        });
        winRow.addView(signIn, weight(0));
        Button cat = button(txt.changeCategory());
        cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategory();
            }
        });
        winRow.addView(cat, weight(8));
        windowBox.addView(winRow, topMargin(10));
        modeCard.addView(windowBox, new LinearLayout.LayoutParams(-1, -2));

        // Aplicația aleasă / Chosen app
        appBox = new LinearLayout(this);
        appBox.setOrientation(LinearLayout.VERTICAL);
        appName = text("", 22, TEXT, true);
        appName.setPadding(0, dp(12), 0, 0);
        appBox.addView(appName);
        appHelp = help("", MUTED, 13);
        appBox.addView(appHelp);
        Button pick = button(txt.pickApp());
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPicker();
            }
        });
        appBox.addView(pick, topMargin(10));
        modeCard.addView(appBox, new LinearLayout.LayoutParams(-1, -2));

        Button test = button(txt.tryNow());
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryNow();
            }
        });
        modeCard.addView(test, topMargin(8));
        root.addView(modeCard, cardParams());

        // ---- Cât de des / How often
        LinearLayout freqCard = card();
        freqCard.addView(text(txt.freqTitle(), 18, TEXT, true));
        freqValue = text("", 20, AMBER, true);
        freqValue.setPadding(0, dp(4), 0, 0);
        freqCard.addView(freqValue);
        Button change = button(txt.change());
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFrequency();
            }
        });
        freqCard.addView(change, topMargin(10));
        nightButton = button("");
        nightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNight();
            }
        });
        freqCard.addView(nightButton, topMargin(8));
        root.addView(freqCard, cardParams());

        // ---- Progres / Progress
        LinearLayout statsCard = card();
        statsCard.addView(text(txt.statsTitle(), 18, TEXT, true));
        statsText = text("", 20, GREEN, true);
        statsText.setPadding(0, dp(4), 0, 0);
        statsCard.addView(statsText);
        lastText = help("", MUTED, 13);
        statsCard.addView(lastText);
        root.addView(statsCard, cardParams());

        // ---- Ultimele deblocări / Recent unlocks
        LinearLayout logCard = card();
        logCard.addView(text(txt.logTitle(), 18, TEXT, true));
        logText = help("", TEXT, 14);
        logCard.addView(logText);
        logCard.addView(help(txt.logHelp(), MUTED, 12));
        Button journal = button(txt.logOpen());
        journal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryStart(new Intent(MainActivity.this, LogActivity.class));
            }
        });
        logCard.addView(journal, topMargin(10));
        root.addView(logCard, cardParams());

        // ---- Ca să meargă la fiecare deblocare / So it works on every unlock
        LinearLayout checkCard = card();
        checkCard.addView(text(txt.checkTitle(), 18, TEXT, true));
        overlayCheck = help("", TEXT, 15);
        checkCard.addView(overlayCheck);
        batteryCheck = help("", TEXT, 15);
        checkCard.addView(batteryCheck);
        batteryButton = button(txt.batteryButton());
        batteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBattery();
            }
        });
        checkCard.addView(batteryButton, topMargin(8));
        if (isHuawei()) {
            frozenCheck = help("", TEXT, 15);
            checkCard.addView(frozenCheck);
            TextView hwTitle = text(txt.huaweiTitle(), 16, AMBER, true);
            hwTitle.setPadding(0, dp(14), 0, 0);
            checkCard.addView(hwTitle);
            checkCard.addView(help(txt.huaweiBody(), TEXT, 14));
            LinearLayout hwRow = row();
            Button launch = button(txt.launchSettings());
            launch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLaunchSettings();
                }
            });
            hwRow.addView(launch, weight(0));
            Button details = button(txt.appSettings());
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAppSettings();
                }
            });
            hwRow.addView(details, weight(8));
            checkCard.addView(hwRow, topMargin(10));
        }
        root.addView(checkCard, cardParams());

        // ---- Ajutor / Help
        LinearLayout helpCard = card();
        helpCard.addView(text(txt.helpTitle(), 18, TEXT, true));
        helpCard.addView(help(txt.helpBody(), MUTED, 14));
        root.addView(helpCard, cardParams());

        Button lang = button(txt.language());
        lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLanguage();
            }
        });
        root.addView(lang, topMargin(14));

        TextView author = text("Hunys with Claude AI · MIT", 12, MUTED, false);
        author.setGravity(Gravity.CENTER_HORIZONTAL);
        author.setPadding(0, dp(14), 0, 0);
        root.addView(author, new LinearLayout.LayoutParams(-1, -2));

        setContentView(scroll);
        refreshUi();
    }

    private void refreshUi() {
        if (powerButton == null) return;
        boolean active = Prefs.isActive(prefs);
        boolean paused = Prefs.isPaused(prefs);

        powerButton.setText(active ? txt.powerOn() : txt.powerOff());
        paint(powerButton, active ? GREEN_DARK : BTN, TEXT);

        pauseButton.setVisibility(active ? View.VISIBLE : View.GONE);
        pauseButton.setText(paused ? txt.resume() : txt.pause());
        pauseStatus.setVisibility(active && paused ? View.VISIBLE : View.GONE);
        if (paused) pauseStatus.setText(txt.pausedUntil(Txt.time(this, Prefs.getPauseUntil(prefs))));

        permCard.setVisibility(Gate.canDrawOverlays(this) ? View.GONE : View.VISIBLE);

        boolean window = Prefs.getMode(prefs) == Prefs.MODE_WINDOW;
        modeValue.setText(txt.modeName(Prefs.getMode(prefs)));
        windowBox.setVisibility(window ? View.VISIBLE : View.GONE);
        appBox.setVisibility(window ? View.GONE : View.VISIBLE);
        catValue.setText(txt.category(Prefs.getCategory(prefs)));
        if (!window) {
            String pkg = Gate.targetPackage(this);
            if (pkg.length() == 0) {
                appName.setText(txt.noApp());
                appName.setTextColor(RED);
                appHelp.setText(txt.noAppHelp());
            } else {
                appName.setText(Gate.appLabel(this, pkg));
                appName.setTextColor(TEXT);
                appHelp.setText(txt.appHelp());
            }
        }

        freqValue.setText(txt.freqName(Prefs.getFreq(prefs)));
        nightButton.setText(txt.nightQuiet(Prefs.isNightQuiet(prefs)));

        statsText.setText(txt.stats(Prefs.getToday(prefs), Prefs.getTotal(prefs)));
        long last = Prefs.getLastOpen(prefs);
        lastText.setText(last > 0 ? txt.lastOpen(Txt.time(this, last)) : txt.lastNever());

        String[] log = Prefs.getLog(prefs);
        if (log.length == 0) {
            logText.setText(txt.logEmpty());
        } else {
            SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);
            StringBuilder b = new StringBuilder();
            for (String line : log) {
                int bar = line.indexOf('|');
                if (bar <= 0) continue;
                long t;
                try {
                    t = Long.parseLong(line.substring(0, bar));
                } catch (NumberFormatException e) {
                    continue;
                }
                if (b.length() > 0) b.append('\n');
                b.append(f.format(new Date(t))).append("  ").append(txt.logLine(line.substring(bar + 1)));
            }
            logText.setText(b.toString());
        }

        boolean overlay = Gate.canDrawOverlays(this);
        overlayCheck.setText(txt.overlayCheck(overlay));
        overlayCheck.setTextColor(overlay ? GREEN : RED);
        boolean battery = ignoresBattery();
        batteryCheck.setText(txt.batteryCheck(battery));
        batteryCheck.setTextColor(battery ? GREEN : RED);
        batteryButton.setVisibility(battery || Build.VERSION.SDK_INT < 23 ? View.GONE : View.VISIBLE);
        if (frozenCheck != null) {
            int frozen = Prefs.getFrozenCount(prefs);
            frozenCheck.setText(frozen > 0
                    ? txt.frozenBad(frozen, Txt.time(this, Prefs.getFrozenAt(prefs)))
                    : txt.frozenOk());
            frozenCheck.setTextColor(frozen > 0 ? RED : GREEN);
        }
    }

    // ---------------------------------------------------------------- helpers

    /** Trei lumini de semafor / Three traffic lights. */
    private View lights() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(7), dp(7), dp(7), dp(7));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(CARD);
        bg.setCornerRadius(dp(14));
        box.setBackground(bg);
        int[] colors = {RED, AMBER, GREEN};
        for (int i = 0; i < colors.length; i++) {
            View dot = new View(this);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(colors[i]);
            dot.setBackground(d);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(18), dp(18));
            if (i > 0) p.topMargin = dp(5);
            box.addView(dot, p);
        }
        return box;
    }

    private TextView help(String s, int color, int sp) {
        TextView t = text(s, sp, color, false);
        t.setPadding(0, dp(6), 0, 0);
        t.setLineSpacing(0f, 1.15f);
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

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    private LinearLayout.LayoutParams weight(int leftMarginDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
        p.leftMargin = dp(leftMarginDp);
        return p;
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

    /** Culorile unui buton, cu stare apăsat și focus / A button's colors, with pressed and focus states. */
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

    // ---------------------------------------------------------------- lista de aplicații / app list

    private static final class AppEntry {
        String pkg;
        String label;
        String sortKey;
        boolean match;
        ResolveInfo info;
        Drawable icon;
    }

    /** Un rând: iconiță, nume, eticheta „potrivită” / One row: icon, name, "likely match" tag. */
    private final class AppRow extends LinearLayout {
        final ImageView icon;
        final TextView label;
        final TextView tag;

        AppRow() {
            super(MainActivity.this);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp(20), dp(10), dp(20), dp(10));
            icon = new ImageView(MainActivity.this);
            addView(icon, new LayoutParams(dp(40), dp(40)));
            LinearLayout col = new LinearLayout(MainActivity.this);
            col.setOrientation(VERTICAL);
            col.setPadding(dp(14), 0, 0, 0);
            label = text("", 17, TEXT, false);
            col.addView(label);
            tag = text("", 12, GREEN, true);
            col.addView(tag);
            addView(col, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        }
    }
}
