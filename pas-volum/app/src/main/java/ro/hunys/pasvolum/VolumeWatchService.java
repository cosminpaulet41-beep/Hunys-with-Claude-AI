/*
 * Pas Volum / Volume Step
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * RO: Serviciu din fundal care verifică volumul de ~33 de ori pe secundă. Când
 *     volumul se schimbă cu exact 1 (o apăsare pe telecomandă), adaugă imediat
 *     restul treptelor, ca fiecare apăsare să mute volumul cu pasul ales.
 *     v1.2: verificare directă în loc de mesajul de sistem VOLUME_CHANGED_ACTION,
 *     pe care Android 14 îl livrează cu ~500 ms întârziere aplicațiilor din fundal.
 *     Nu are nevoie de accesibilitate.
 * EN: Background service that checks the volume ~33 times per second. When the
 *     volume changes by exactly 1 (one remote key press), it immediately adds the
 *     remaining steps, so every press moves the volume by the chosen step.
 *     v1.2: direct polling instead of the VOLUME_CHANGED_ACTION broadcast, which
 *     Android 14 delivers ~500 ms late to background apps.
 *     No accessibility permission needed.
 */
package ro.hunys.pasvolum;

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
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.view.KeyEvent;

public class VolumeWatchService extends Service {

    /** true cât timp serviciul rulează / true while the service runs. */
    static volatile boolean running = false;

    private static final int STREAM = AudioManager.STREAM_MUSIC;
    /** Cât de des verificăm volumul / How often we check the volume. */
    private static final long POLL_MS = 30;

    private static final String CHANNEL_ID = "pas_volum";
    private static final int NOTIFICATION_ID = 7;
    /** ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE (API 34). */
    private static final int FGS_TYPE_SPECIAL_USE = 0x40000000;
    /** Context.RECEIVER_EXPORTED (API 33). */
    private static final int RECEIVER_EXPORTED_FLAG = 0x2;

    private AudioManager audio;
    private SharedPreferences prefs;
    private HandlerThread thread;
    private Handler worker;
    private boolean screenReceiverRegistered = false;

    /** Ultimul volum văzut (folosit doar pe firul worker). / Last seen volume (worker thread only). */
    private int lastVolume = -1;
    private volatile boolean polling = false;

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            if (!polling) return;
            check();
            worker.postDelayed(this, POLL_MS);
        }
    };

    /** Oprim verificarea cât timp proiectorul e în standby. / Pause while the projector is in standby. */
    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                stopPolling();
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                startPolling();
            }
        }
    };

    static void start(Context c) {
        Intent i = new Intent(c, VolumeWatchService.class);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                c.startForegroundService(i);
            } else {
                c.startService(i);
            }
        } catch (RuntimeException ignored) {
            // Android poate refuza pornirea din fundal; se reîncearcă la deschiderea aplicației.
            // Android may refuse a background start; it is retried when the app is opened.
        }
    }

    static void stop(Context c) {
        c.stopService(new Intent(c, VolumeWatchService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        prefs = Prefs.get(this);
        goForeground();

        thread = new HandlerThread("PasVolum", Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();
        worker = new Handler(thread.getLooper());

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.addAction(Intent.ACTION_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, f, RECEIVER_EXPORTED_FLAG);
        } else {
            registerReceiver(screenReceiver, f);
        }
        screenReceiverRegistered = true;

        startPolling();
        running = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Prefs.isEnabled(prefs)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        goForeground(); // reîmprospătează textul notificării / refresh notification text
        startPolling();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        stopPolling();
        if (screenReceiverRegistered) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (RuntimeException ignored) {
            }
            screenReceiverRegistered = false;
        }
        if (thread != null) thread.quitSafely();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ---------------------------------------------------------------- logica / logic

    private void startPolling() {
        if (worker == null || polling) return;
        polling = true;
        worker.post(new Runnable() {
            @Override
            public void run() {
                lastVolume = readVolume(); // pornim de la volumul actual / start from current volume
                worker.removeCallbacks(poll);
                worker.postDelayed(poll, POLL_MS);
            }
        });
    }

    private void stopPolling() {
        polling = false;
        if (worker != null) worker.removeCallbacks(poll);
    }

    private int readVolume() {
        try {
            return audio.getStreamVolume(STREAM);
        } catch (RuntimeException e) {
            return -1;
        }
    }

    /** Rulează pe firul worker / Runs on the worker thread. */
    private void check() {
        int cur = readVolume();
        if (cur < 0) return;
        int prev = lastVolume;
        lastVolume = cur;
        if (prev < 0 || cur == prev) return;

        int delta = cur - prev;
        if (delta != 1 && delta != -1) return; // nu e o singură apăsare / not a single press
        if (!Prefs.isEnabled(prefs)) return;

        Prefs.recordKey(prefs, delta > 0 ? KeyEvent.KEYCODE_VOLUME_UP : KeyEvent.KEYCODE_VOLUME_DOWN);

        int step = Prefs.getStep(prefs);
        if (step <= 1) return;
        try {
            int max = audio.getStreamMaxVolume(STREAM);
            // Sistemul a mutat deja 1; adăugăm restul (pas - 1).
            // The system already moved 1; add the rest (step - 1).
            int target = cur + delta * (step - 1);
            if (target < 0) target = 0;
            if (target > max) target = max;
            if (target == cur) return;
            audio.setStreamVolume(STREAM, target, AudioManager.FLAG_SHOW_UI);
            lastVolume = target; // schimbarea noastră nu e o apăsare / our change isn't a press
        } catch (RuntimeException ignored) {
        }
    }

    // ---------------------------------------------------------------- notificare / notification

    private void goForeground() {
        Txt txt = Txt.forLang(Prefs.getLang(prefs));
        Notification n = buildNotification(txt);
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTIFICATION_ID, n, Build.VERSION.SDK_INT >= 34 ? FGS_TYPE_SPECIAL_USE : 0);
            } else {
                startForeground(NOTIFICATION_ID, n);
            }
        } catch (RuntimeException e) {
            // Dacă Android refuză, serviciul tot încearcă să funcționeze cât timp trăiește.
            // If Android refuses, the service still works for as long as it lives.
        }
    }

    @SuppressWarnings("deprecation")
    private Notification buildNotification(Txt txt) {
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, txt.title(), NotificationManager.IMPORTANCE_MIN);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
            b = new Notification.Builder(this, CHANNEL_ID);
        } else {
            b = new Notification.Builder(this);
            b.setPriority(Notification.PRIORITY_MIN);
        }
        Intent open = new Intent(this, MainActivity.class);
        int piFlags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
        b.setContentIntent(PendingIntent.getActivity(this, 0, open, piFlags));
        b.setSmallIcon(android.R.drawable.ic_lock_silent_mode_off);
        b.setContentTitle(txt.title());
        b.setContentText(txt.notificationText(Prefs.getStep(prefs)));
        b.setOngoing(true);
        return b.build();
    }
}
