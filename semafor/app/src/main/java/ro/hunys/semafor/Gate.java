/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.semafor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.text.Normalizer;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Decide dacă la deblocarea asta se deschide aplicația cu întrebări și o deschide.
 * Decides whether this unlock opens the quiz app, and opens it.
 */
final class Gate {
    private static final String CHANNEL_QUESTION = "semafor_question";
    private static final int NOTIF_QUESTION = 52;

    /** Cuvinte după care recunoaște aplicația / Words that identify the quiz app. */
    private static final String[] HINTS = {"scoala rutiera", "scoalarutiera", "rutier", "drpciv", "chestionar", "permis"};

    private Gate() {}

    // ---------------------------------------------------------------- deblocare / unlock

    /**
     * La deblocare: null dacă trebuie deschisă întrebarea, altfel motivul pentru care nu
     * (un cod pentru lista „Ultimele deblocări”).
     * On unlock: null if the question should open, otherwise why not (a code for the log).
     */
    static String decide(Context c) {
        SharedPreferences sp = Prefs.get(c);
        if (!Prefs.isActive(sp)) return Prefs.LOG_OFF;
        if (Prefs.isPaused(sp)) return Prefs.LOG_PAUSE;
        if (Prefs.isNightQuiet(sp) && isNight()) return Prefs.LOG_NIGHT;
        if (inCall(c)) return Prefs.LOG_CALL;

        int idx = Prefs.getFreq(sp);
        int every = Prefs.FREQ_EVERY[idx];
        if (every > 0) {
            int n = Prefs.getCounter(sp) + 1;
            if (n < every) {
                Prefs.setCounter(sp, n);
                return Prefs.LOG_FREQ;
            }
            Prefs.setCounter(sp, 0);
        } else {
            long last = Prefs.getLastOpen(sp);
            long now = System.currentTimeMillis();
            if (last > 0 && now >= last && now - last < Prefs.FREQ_MINUTES[idx] * 60000L) return Prefs.LOG_FREQ;
        }
        return null;
    }

    /** Ce se deschide: fereastra Semafor sau aplicația aleasă / What opens: the Semafor window or the chosen app. */
    static Intent launchIntent(Context c) {
        Intent launch;
        if (Prefs.getMode(Prefs.get(c)) == Prefs.MODE_WINDOW) {
            // Fereastra Semafor, direct la următoarea întrebare / The Semafor window, straight to the next question
            launch = new Intent(c, QuestionActivity.class);
        } else {
            String pkg = targetPackage(c);
            if (pkg.length() == 0) return null;
            launch = appIntent(c, pkg);
        }
        if (launch == null) return null;
        // Fără RESET_TASK_IF_NEEDED: aplicația vine în față exact cum ai lăsat-o.
        // No RESET_TASK_IF_NEEDED: the app comes to the front exactly as you left it.
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launch;
    }

    /** Deschide acum, din ecranul aplicației („Încearcă acum”) / Opens now, from the app screen ("Try it now"). */
    static boolean openNow(Context c) {
        Intent launch = launchIntent(c);
        if (launch == null) return false;
        try {
            c.startActivity(launch);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Pentru Școala Rutieră încearcă întâi pagina mediului de învățare (merge dacă aplicația
     * acceptă linkuri), altfel o deschide normal.
     * For Școala Rutieră it first tries the learning page (works if the app accepts links),
     * otherwise it opens the app normally.
     */
    private static Intent appIntent(Context c, String pkg) {
        PackageManager pm = c.getPackageManager();
        boolean site = pkg.toLowerCase(Locale.ROOT).contains("scoalarutiera");
        if (site) {
            Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(Site.categoryUrl(c)))
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setPackage(pkg);
            try {
                if (view.resolveActivity(pm) != null) return view;
            } catch (RuntimeException ignored) {
            }
        }
        try {
            return pm.getLaunchIntentForPackage(pkg);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Notificare de rezervă „Întrebarea ta te așteaptă” / Fallback notification "Your question is waiting". */
    @SuppressWarnings("deprecation")
    static boolean notifyQuestion(Context c, Intent launch) {
        try {
            Txt txt = Txt.of(c);
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder b;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(CHANNEL_QUESTION, txt.questionChannel(), NotificationManager.IMPORTANCE_HIGH);
                ch.setShowBadge(false);
                nm.createNotificationChannel(ch);
                b = new Notification.Builder(c, CHANNEL_QUESTION);
                b.setTimeoutAfter(2 * 60 * 1000L);
            } else {
                b = new Notification.Builder(c);
                b.setPriority(Notification.PRIORITY_HIGH);
                b.setDefaults(Notification.DEFAULT_VIBRATE);
            }
            b.setSmallIcon(R.drawable.ic_notif)
                    .setContentTitle(txt.questionTitle())
                    .setContentText(txt.questionText(targetLabel(c, txt)))
                    .setAutoCancel(true)
                    .setCategory(Notification.CATEGORY_REMINDER)
                    .setContentIntent(PendingIntent.getActivity(c, 7, launch, UnlockService.piFlags()));
            nm.notify(NOTIF_QUESTION, b.build());
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------- aplicația aleasă / chosen app

    /** Ce se deschide, în cuvinte / What opens, in words. */
    static String targetLabel(Context c, Txt txt) {
        SharedPreferences sp = Prefs.get(c);
        if (Prefs.getMode(sp) == Prefs.MODE_WINDOW) return txt.siteLabel(Prefs.getCategory(sp));
        String pkg = targetPackage(c);
        return pkg.length() == 0 ? "" : appLabel(c, pkg);
    }

    /** Pachetul ales, sau unul găsit singur dacă lipsește / The chosen package, or one found automatically if missing. */
    static String targetPackage(Context c) {
        SharedPreferences sp = Prefs.get(c);
        String pkg = Prefs.getPackage(sp);
        if (pkg.length() > 0 && isInstalled(c, pkg)) return pkg;
        String found = findQuizApp(c);
        if (found.length() > 0) Prefs.setPackage(sp, found);
        return found;
    }

    /** Caută Școala Rutieră (sau altă aplicație de chestionare) / Looks for Școala Rutieră (or another quiz app). */
    static String findQuizApp(Context c) {
        List<ResolveInfo> apps = launcherApps(c);
        PackageManager pm = c.getPackageManager();
        for (String hint : HINTS) {
            for (ResolveInfo r : apps) {
                String pkg = r.activityInfo.packageName;
                if (pkg.equals(c.getPackageName())) continue;
                String label = plain(String.valueOf(r.loadLabel(pm)));
                if (label.contains(hint) || pkg.toLowerCase(Locale.ROOT).contains(hint.replace(" ", ""))) return pkg;
            }
        }
        return "";
    }

    /** Este o aplicație cu întrebări probabilă? / Is it a likely quiz app? */
    static boolean looksLikeQuizApp(String label, String pkg) {
        String l = plain(label);
        String p = pkg.toLowerCase(Locale.ROOT);
        for (String hint : HINTS) {
            if (l.contains(hint) || p.contains(hint.replace(" ", ""))) return true;
        }
        return false;
    }

    static List<ResolveInfo> launcherApps(Context c) {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        return c.getPackageManager().queryIntentActivities(i, 0);
    }

    static boolean isInstalled(Context c, String pkg) {
        try {
            return c.getPackageManager().getLaunchIntentForPackage(pkg) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static String appLabel(Context c, String pkg) {
        if (pkg == null || pkg.length() == 0) return "";
        try {
            PackageManager pm = c.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            return String.valueOf(pm.getApplicationLabel(ai));
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        } catch (RuntimeException e) {
            return pkg;
        }
    }

    /** Fără diacritice și cu litere mici: „Școala Rutieră” → „scoala rutiera” / No diacritics, lower case. */
    static String plain(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toLowerCase(Locale.ROOT);
    }

    // ---------------------------------------------------------------- stări / states

    static boolean canDrawOverlays(Context c) {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(c);
    }

    static boolean isNight() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h >= 23 || h < 7;
    }

    /** În apel sau sună telefonul / In a call or the phone is ringing. */
    private static boolean inCall(Context c) {
        try {
            AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            int mode = am.getMode();
            return mode == AudioManager.MODE_IN_CALL
                    || mode == AudioManager.MODE_IN_COMMUNICATION
                    || mode == AudioManager.MODE_RINGTONE;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------- serviciul / the service

    /** Pornește sau oprește totul / Turns everything on or off. */
    static boolean setActive(Context c, boolean on) {
        Prefs.setActive(Prefs.get(c), on);
        return apply(c);
    }

    /** Pornește serviciul dacă trebuie, îl oprește dacă nu / Starts the service if needed, stops it if not. */
    static boolean apply(Context c) {
        Intent i = new Intent(c, UnlockService.class);
        if (!Prefs.isActive(Prefs.get(c))) {
            try {
                c.stopService(i);
            } catch (RuntimeException ignored) {
            }
            return true;
        }
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                c.startForegroundService(i);
            } else {
                c.startService(i);
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
