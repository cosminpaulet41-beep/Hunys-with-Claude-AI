package ro.hunys.tn2.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import ro.hunys.tn2.Img;
import ro.hunys.tn2.Platform;

/** Legătura jocului cu telefonul: resursele din APK, sunetele, salvările și vibrațiile. */
final class AndroidPlatform implements Platform {
    /** Efectele sonore, în ordinea biților din World.S_* (ultimul e clicul din meniu). */
    static final String[] SOUNDS = {"shot", "empty", "reload", "door", "locked", "card", "pickup", "groan", "groan2",
        "hurt", "zdie", "paper", "heart", "shotgun", "pump", "roar", "gen", "smg", "explode", "spit", "thunder",
        "alarm", "radio", "menu"};

    private final Activity act;
    private final SharedPreferences prefs;
    private SoundPool pool;
    private final int[] ids = new int[SOUNDS.length];
    private final long[] last = new long[SOUNDS.length];

    AndroidPlatform(Activity a) {
        act = a;
        prefs = a.getSharedPreferences("tura2", Context.MODE_PRIVATE);
    }

    /** Încarcă sunetele (WAV-urile sunt stocate necomprimat în APK, ca să le poată citi SoundPool). */
    @SuppressWarnings("deprecation")
    void loadSounds() {
        if (pool != null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes at = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            pool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(at).build();
        } else {
            pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        for (int i = 0; i < SOUNDS.length; i++) {
            try {
                AssetFileDescriptor fd = act.getAssets().openFd("s/" + SOUNDS[i] + ".wav");
                ids[i] = pool.load(fd, 1);
                fd.close();
            } catch (IOException e) {
                ids[i] = 0;
            }
        }
    }

    void release() {
        if (pool != null) {
            pool.release();
            pool = null;
        }
    }

    public Img image(String name) throws IOException {
        InputStream in = act.getAssets().open(name);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        o.inPremultiplied = false;
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap b = BitmapFactory.decodeStream(in, null, o);
        in.close();
        if (b == null) {
            throw new IOException(name);
        }
        Img im = new Img(b.getWidth(), b.getHeight());
        b.getPixels(im.px, 0, im.w, 0, 0, im.w, im.h);
        b.recycle();
        return im;
    }

    public InputStream open(String name) throws IOException {
        return act.getAssets().open(name);
    }

    public void sound(int id, float volume) {
        if (pool == null || id < 0 || id >= ids.length || ids[id] == 0) {
            return;
        }
        // același sunet de mai multe ori în același cadru se aude o singură dată
        long now = System.currentTimeMillis();
        if (now - last[id] < 40) {
            return;
        }
        last[id] = now;
        pool.play(ids[id], volume, volume, 1, 0, 1f);
    }

    public void save(String key, byte[] data) {
        prefs.edit().putString(key, Base64.encodeToString(data, Base64.NO_WRAP)).apply();
    }

    public byte[] load(String key) {
        String s = prefs.getString(key, null);
        if (s == null) {
            return null;
        }
        try {
            return Base64.decode(s, Base64.NO_WRAP);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public void vibrate(int ms) {
        Vibrator v = (Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(ms);
        }
    }

    public boolean systemEnglish() {
        return !"ro".equals(Locale.getDefault().getLanguage());
    }

    public void quit() {
        act.runOnUiThread(new Runnable() {
            public void run() {
                act.finish();
            }
        });
    }
}
