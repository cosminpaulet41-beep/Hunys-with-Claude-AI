/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/**
 * Serviciul din prim-plan: ține stratul pe ecran și arată notificarea cu butoane.
 * The foreground service: keeps the layer on screen and shows the notification with buttons.
 */
public class DimService extends Service {
    static DimService instance;

    static final String ACTION_LESS = "ro.hunys.penumbra.LESS";
    static final String ACTION_MORE = "ro.hunys.penumbra.MORE";
    static final String ACTION_OFF = "ro.hunys.penumbra.OFF";

    private static final String CHANNEL = "penumbra_on";
    private static final int NOTIF_ID = 41;
    private static final int STEP = 10;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Overlay overlay;

    private final Runnable refreshLater = new Runnable() {
        @Override
        public void run() {
            refresh();
        }
    };

    private final Runnable notifyLater = new Runnable() {
        @Override
        public void run() {
            try {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(NOTIF_ID, buildNotification());
            } catch (RuntimeException ignored) {
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        overlay = new Overlay(this, Overlay.appOverlayType());
        Dimmer.refreshTile(this);
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
        int max = Look.maxDim(this, Dimmer.fullCoverInUse(this));
        int dim = Math.min(Prefs.getDim(sp), max);
        if (ACTION_LESS.equals(action)) {
            Prefs.setDim(sp, Math.max(0, dim - STEP));
        } else if (ACTION_MORE.equals(action)) {
            Prefs.setDim(sp, Math.min(max, dim + STEP));
        } else if (ACTION_OFF.equals(action)) {
            Prefs.setActive(sp, false);
        }

        // Dimmer ne oprește dacă stratul nu mai trebuie / Dimmer stops us if the layer is no longer wanted.
        Dimmer.apply(this);
        return Prefs.isActive(sp) ? START_STICKY : START_NOT_STICKY;
    }

    /** Pune stratul cum cer setările / Puts the layer as the settings say. */
    void refresh() {
        boolean fullCover = Dimmer.fullCoverInUse(this);
        if (!fullCover && Dimmer.canDrawOverlays(this)) {
            overlay.show(Look.of(this, false));
        } else {
            // Îl desenează serviciul de accesibilitate sau lipsește permisiunea.
            // The accessibility service draws it, or the permission is missing.
            overlay.hide();
        }
        // Notificarea se actualizează rar, ca glisorul să nu o inunde.
        // The notification updates sparingly so the slider doesn't flood it.
        handler.removeCallbacks(notifyLater);
        handler.postDelayed(notifyLater, 300);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Ecranul s-a rotit: stratul își ia noua mărime / Screen rotated: the layer takes the new size.
        refresh();
        handler.removeCallbacks(refreshLater);
        handler.postDelayed(refreshLater, 400);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (overlay != null) overlay.hide();
        if (instance == this) instance = null;
        Dimmer.refreshTile(this);
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
        boolean fullCover = Dimmer.fullCoverInUse(this);
        int dim = Math.min(Prefs.getDim(sp), Look.maxDim(this, fullCover));

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

        String text = Dimmer.canDim(this)
                ? txt.notifText(dim, Prefs.getWarm(sp), Prefs.isMinLight(sp), fullCover)
                : txt.notifNoPermission();

        Intent open = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        b.setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(txt.notifTitle(dim))
                .setContentText(text)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(PendingIntent.getActivity(this, 0, open, piFlags()))
                .addAction(0, "−" + STEP + "%", serviceIntent(ACTION_LESS, 1))
                .addAction(0, "+" + STEP + "%", serviceIntent(ACTION_MORE, 2))
                .addAction(0, txt.actionOff(), serviceIntent(ACTION_OFF, 3));
        if (Build.VERSION.SDK_INT >= 31) {
            b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        return b.build();
    }

    private PendingIntent serviceIntent(String action, int code) {
        Intent i = new Intent(this, DimService.class).setAction(action);
        return PendingIntent.getService(this, code, i, piFlags());
    }

    static int piFlags() {
        int f = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) f |= PendingIntent.FLAG_IMMUTABLE;
        return f;
    }
}
