/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.semafor;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Serviciul din prim-plan: ascultă deblocarea și arată notificarea cu butoane.
 * Android nu mai trimite deblocarea aplicațiilor oprite, așa că serviciul trebuie să ruleze.
 *
 * The foreground service: listens for the unlock and shows the notification with buttons.
 * Android no longer sends the unlock to stopped apps, so the service has to keep running.
 */
public class UnlockService extends Service {
    static final String ACTION_PAUSE = "ro.hunys.semafor.PAUSE";
    static final String ACTION_RESUME = "ro.hunys.semafor.RESUME";
    static final String ACTION_OFF = "ro.hunys.semafor.OFF";

    static final long PAUSE_MS = 60L * 60L * 1000L;

    private static final String CHANNEL = "semafor_on";
    private static final int NOTIF_ID = 51;
    /** O mică pauză, ca ecranul de blocare să dispară întâi / A short delay so the lock screen goes away first. */
    private static final long OPEN_DELAY_MS = 350;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private BroadcastReceiver unlockReceiver;
    /** Fereastra de 1 pixel care convinge Android să lase deschiderea / The 1-pixel window that lets Android allow the opening. */
    private View trampoline;
    private long lastTriggerAt;

    private final Runnable onUnlock = new Runnable() {
        @Override
        public void run() {
            trigger();
        }
    };

    /**
     * Ecranul s-a aprins: dacă nu e blocat (s-a aprins prea repede ca să se blocheze),
     * Android nu trimite „deblocat”, așa că îl tratăm noi ca deblocare.
     * The screen turned on: if it isn't locked (turned on too soon to lock), Android sends no
     * "unlocked", so we treat it as an unlock ourselves.
     */
    private final Runnable onScreenOn = new Runnable() {
        @Override
        public void run() {
            try {
                KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (pm.isInteractive() && !km.isKeyguardLocked()) trigger();
            } catch (RuntimeException ignored) {
            }
        }
    };

    private final Runnable hideTrampolineLater = new Runnable() {
        @Override
        public void run() {
            hideTrampoline();
        }
    };

    private final Runnable endPause = new Runnable() {
        @Override
        public void run() {
            updateNotification();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.addLog(Prefs.get(this), Prefs.LOG_SERVICE);
        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String a = intent.getAction();
                if (Intent.ACTION_USER_PRESENT.equals(a)) {
                    handler.removeCallbacks(onScreenOn);
                    handler.removeCallbacks(onUnlock);
                    handler.postDelayed(onUnlock, OPEN_DELAY_MS);
                } else if (Intent.ACTION_SCREEN_ON.equals(a)) {
                    handler.removeCallbacks(onScreenOn);
                    handler.postDelayed(onScreenOn, 700);
                } else if (Intent.ACTION_SCREEN_OFF.equals(a)) {
                    handler.removeCallbacks(onScreenOn);
                    handler.removeCallbacks(onUnlock);
                }
            }
        };
        IntentFilter f = new IntentFilter(Intent.ACTION_USER_PRESENT);
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                // Vin de la sistem, deci ajung și așa / They come from the system, so they still arrive.
                registerReceiver(unlockReceiver, f, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(unlockReceiver, f);
            }
        } catch (RuntimeException e) {
            unlockReceiver = null;
        }
    }

    // ---------------------------------------------------------------- deblocare / unlock

    private void trigger() {
        long now = SystemClock.elapsedRealtime();
        // Aprinderea ecranului și deblocarea pot veni amândouă / Screen-on and unlock may both arrive.
        if (lastTriggerAt != 0 && now - lastTriggerAt < 3000) return;
        lastTriggerAt = now;

        final SharedPreferences sp = Prefs.get(this);
        String skip = Gate.decide(this);
        if (skip != null) {
            if (!Prefs.LOG_OFF.equals(skip)) Prefs.addLog(sp, skip);
            updateNotification();
            return;
        }
        final Intent launch = Gate.launchIntent(this);
        if (launch == null) {
            Prefs.addLog(sp, Prefs.LOG_NO_APP);
            updateNotification();
            return;
        }
        if (!Gate.canDrawOverlays(this)) {
            fallback(launch);
            return;
        }

        // Android lasă o aplicație să deschidă o fereastră din fundal dacă are deja ceva vizibil
        // pe ecran: punem un pixel invizibil, deschidem, apoi îl scoatem.
        // Android lets an app open a window from the background if it already has something visible
        // on screen: add an invisible pixel, open, then remove it.
        showTrampoline();
        final long t0 = now;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSafely(launch);
            }
        }, 120);

        if (Prefs.getMode(sp) != Prefs.MODE_WINDOW) {
            // Altă aplicație: nu putem verifica dacă s-a deschis / Another app: we can't check it opened.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideTrampoline();
                    Prefs.recordOpen(sp);
                    Prefs.addLog(sp, Prefs.LOG_APP);
                    updateNotification();
                }
            }, 1500);
            return;
        }

        // Verificăm că fereastra chiar a apărut; dacă nu, încă o dată, apoi notificare.
        // Check the window really appeared; if not, once more, then a notification.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (QuestionActivity.lastShownAt >= t0) {
                    opened(sp, Prefs.LOG_OPEN);
                    return;
                }
                startSafely(launch);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (QuestionActivity.lastShownAt >= t0) {
                            opened(sp, Prefs.LOG_OPEN_RETRY);
                        } else {
                            fallback(launch);
                        }
                    }
                }, 1500);
            }
        }, 1600);
    }

    private void opened(SharedPreferences sp, String code) {
        hideTrampoline();
        Prefs.recordOpen(sp);
        Prefs.addLog(sp, code);
        updateNotification();
    }

    /** Android n-a lăsat deschiderea: notificare de atins / Android blocked the opening: a notification to tap. */
    private void fallback(Intent launch) {
        hideTrampoline();
        SharedPreferences sp = Prefs.get(this);
        if (Gate.notifyQuestion(this, launch)) Prefs.recordOpen(sp);
        Prefs.addLog(sp, Prefs.LOG_BLOCKED);
        updateNotification();
    }

    private void startSafely(Intent launch) {
        try {
            startActivity(launch);
        } catch (RuntimeException ignored) {
        }
    }

    @SuppressWarnings("deprecation")
    private void showTrampoline() {
        handler.removeCallbacks(hideTrampolineLater);
        handler.postDelayed(hideTrampolineLater, 8000); // siguranță / safety
        if (trampoline != null) return;
        try {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            View v = new View(this);
            v.setBackgroundColor(Color.argb(1, 0, 0, 0));
            int type = Build.VERSION.SDK_INT >= 26
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(1, 1, type,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.START;
            wm.addView(v, lp);
            trampoline = v;
        } catch (RuntimeException e) {
            trampoline = null;
        }
    }

    private void hideTrampoline() {
        handler.removeCallbacks(hideTrampolineLater);
        if (trampoline == null) return;
        try {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(trampoline);
        } catch (RuntimeException ignored) {
        }
        trampoline = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Mereu întâi notificarea, cum cere Android / Always the notification first, as Android requires.
        if (!goForeground()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        SharedPreferences sp = Prefs.get(this);
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_PAUSE.equals(action)) {
            Prefs.setPauseUntil(sp, System.currentTimeMillis() + PAUSE_MS);
        } else if (ACTION_RESUME.equals(action)) {
            Prefs.setPauseUntil(sp, 0L);
        } else if (ACTION_OFF.equals(action)) {
            Prefs.setActive(sp, false);
        }

        if (!Prefs.isActive(sp)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        updateNotification();
        return START_STICKY;
    }

    void updateNotification() {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIF_ID, buildNotification());
        } catch (RuntimeException ignored) {
        }
        // Când se termină pauza, notificarea revine singură / When the pause ends, the notification updates itself.
        handler.removeCallbacks(endPause);
        long left = Prefs.getPauseUntil(Prefs.get(this)) - System.currentTimeMillis();
        if (left > 0) handler.postDelayed(endPause, left + 1000);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        hideTrampoline();
        if (unlockReceiver != null) {
            try {
                unregisterReceiver(unlockReceiver);
            } catch (RuntimeException ignored) {
            }
            unlockReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ---------------------------------------------------------------- notificare / notification

    private boolean goForeground() {
        try {
            Notification n = buildNotification();
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(NOTIF_ID, n);
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private Notification buildNotification() {
        Txt txt = Txt.of(this);
        SharedPreferences sp = Prefs.get(this);

        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, txt.channelName(), NotificationManager.IMPORTANCE_LOW);
            ch.setShowBadge(false);
            ch.setSound(null, null);
            ch.enableVibration(false);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
            b = new Notification.Builder(this, CHANNEL);
        } else {
            b = new Notification.Builder(this);
            b.setPriority(Notification.PRIORITY_LOW);
        }

        boolean paused = Prefs.isPaused(sp);
        String label = Gate.targetLabel(this, txt);
        String title = paused ? txt.notifPausedTitle(Prefs.getPauseUntil(sp), this) : txt.notifTitle(Prefs.getToday(sp));
        String text = label.length() == 0
                ? txt.notifNoApp()
                : txt.notifText(label, Prefs.getFreq(sp));

        Intent open = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        b.setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(PendingIntent.getActivity(this, 0, open, piFlags()));
        if (paused) {
            b.addAction(0, txt.actionResume(), serviceIntent(ACTION_RESUME, 2));
        } else {
            b.addAction(0, txt.actionPause(), serviceIntent(ACTION_PAUSE, 1));
        }
        b.addAction(0, txt.actionOff(), serviceIntent(ACTION_OFF, 3));
        if (Build.VERSION.SDK_INT >= 31) {
            b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        return b.build();
    }

    private PendingIntent serviceIntent(String action, int code) {
        Intent i = new Intent(this, UnlockService.class).setAction(action);
        return PendingIntent.getService(this, code, i, piFlags());
    }

    static int piFlags() {
        int f = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) f |= PendingIntent.FLAG_IMMUTABLE;
        return f;
    }
}
