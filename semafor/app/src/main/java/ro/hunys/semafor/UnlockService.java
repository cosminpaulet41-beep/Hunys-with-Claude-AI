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
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

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

    private final Runnable onUnlock = new Runnable() {
        @Override
        public void run() {
            Gate.onUnlock(UnlockService.this);
            updateNotification();
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
        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                    handler.removeCallbacks(onUnlock);
                    handler.postDelayed(onUnlock, OPEN_DELAY_MS);
                }
            }
        };
        IntentFilter f = new IntentFilter(Intent.ACTION_USER_PRESENT);
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                // Deblocarea vine de la sistem, deci ajunge și așa / The unlock comes from the system, so it still arrives.
                registerReceiver(unlockReceiver, f, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(unlockReceiver, f);
            }
        } catch (RuntimeException e) {
            unlockReceiver = null;
        }
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
        String pkg = Gate.targetPackage(this);
        String title = paused ? txt.notifPausedTitle(Prefs.getPauseUntil(sp), this) : txt.notifTitle(Prefs.getToday(sp));
        String text = pkg.length() == 0
                ? txt.notifNoApp()
                : txt.notifText(Gate.appLabel(this, pkg), Prefs.getFreq(sp));

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
