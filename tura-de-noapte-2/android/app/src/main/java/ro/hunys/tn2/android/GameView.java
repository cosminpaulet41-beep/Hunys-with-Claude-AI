package ro.hunys.tn2.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import ro.hunys.tn2.Game;

/**
 * Suprafața pe care se desenează jocul, cu un fir de execuție separat: lumea (pixeli mari) și interfața
 * se măresc fără netezire, iar butoanele (Vec) se desenează netezit, la rezoluția ecranului.
 */
final class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final Game game;
    private final AndroidPlatform plat;
    private final VecAndroid vec = new VecAndroid();
    private final Paint pixel = new Paint();
    private final Rect src = new Rect(), dst = new Rect();
    private Bitmap worldBmp, uiBmp;
    private Thread thread;
    private volatile boolean running;
    private boolean ready;
    private String error;
    /** Desenăm pe placa video (mult mai repede); dacă telefonul nu poate, trecem pe procesor. */
    private boolean hw = Build.VERSION.SDK_INT >= 23;

    GameView(Context c, Game g, AndroidPlatform p) {
        super(c);
        game = g;
        plat = p;
        pixel.setFilterBitmap(false);
        pixel.setAntiAlias(false);
        pixel.setDither(false);
        getHolder().addCallback(this);
        setFocusable(true);
        setKeepScreenOn(true);
    }

    public void surfaceCreated(SurfaceHolder h) {
    }

    public void surfaceChanged(SurfaceHolder h, int format, int w, int hgt) {
        synchronized (game) {
            try {
                if (!ready) {
                    game.init(w, hgt);
                    ready = true;
                } else if (w != game.sw || hgt != game.sh) {
                    game.resize(w, hgt);
                }
            } catch (Exception e) {
                error = String.valueOf(e);
            }
        }
        start();
    }

    public void surfaceDestroyed(SurfaceHolder h) {
        stop();
    }

    void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this, "joc");
        thread.start();
    }

    void stop() {
        running = false;
        Thread t = thread;
        thread = null;
        if (t != null) {
            try {
                t.join(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void run() {
        long last = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = (now - last) / 1e9f;
            last = now;
            Canvas c = null;
            boolean usedHw = false;
            Surface surf = getHolder().getSurface();
            try {
                if (hw) {
                    try {
                        c = surf.lockHardwareCanvas();
                        usedHw = c != null;
                    } catch (RuntimeException e) {
                        hw = false;
                        c = null;
                    }
                }
                if (c == null) {
                    c = getHolder().lockCanvas();
                }
                if (c == null) {
                    Thread.sleep(10);
                    continue;
                }
                synchronized (game) {
                    if (ready && error == null) {
                        game.update(dt);
                        game.render();
                        drawFrame(c);
                    } else {
                        c.drawColor(Color.BLACK);
                        if (error != null) {
                            Paint p = new Paint();
                            p.setColor(Color.WHITE);
                            p.setTextSize(36f);
                            c.drawText(error, 40, 80, p);
                        }
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (RuntimeException e) {
                error = String.valueOf(e);
            } finally {
                if (c != null) {
                    try {
                        if (usedHw) {
                            surf.unlockCanvasAndPost(c);
                        } else {
                            getHolder().unlockCanvasAndPost(c);
                        }
                    } catch (RuntimeException e) {
                        // suprafața a dispărut între timp
                    }
                }
            }
            // cel mult ~60 de cadre pe secundă, fără să ardem bateria degeaba
            long spent = (System.nanoTime() - now) / 1000000L;
            if (spent < 14) {
                try {
                    Thread.sleep(14 - spent);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void drawFrame(Canvas c) {
        Game g = game;
        if (worldBmp == null || worldBmp.getWidth() != g.W || worldBmp.getHeight() != g.H) {
            worldBmp = Bitmap.createBitmap(g.W, g.H, Bitmap.Config.ARGB_8888);
        }
        if (uiBmp == null || uiBmp.getWidth() != g.UW || uiBmp.getHeight() != g.UH) {
            uiBmp = Bitmap.createBitmap(g.UW, g.UH, Bitmap.Config.ARGB_8888);
        }
        worldBmp.setPixels(g.worldBuf, 0, g.W, 0, 0, g.W, g.H);
        uiBmp.setPixels(g.ui.px, 0, g.UW, 0, 0, g.UW, g.UH);
        c.drawColor(Color.BLACK);
        src.set(0, 0, g.W, g.H);
        dst.set(0, 0, g.W * g.s, g.H * g.s);
        c.drawBitmap(worldBmp, src, dst, pixel);
        src.set(0, 0, g.UW, g.UH);
        dst.set(0, 0, g.UW * g.u, g.UH * g.u);
        c.drawBitmap(uiBmp, src, dst, pixel);
        vec.draw(c, g.vec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!ready) {
            return true;
        }
        int act = e.getActionMasked();
        synchronized (game) {
            switch (act) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    int i = e.getActionIndex();
                    game.touch(0, e.getPointerId(i), e.getX(i), e.getY(i));
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                    for (int i = 0; i < e.getPointerCount(); i++) {
                        game.touch(1, e.getPointerId(i), e.getX(i), e.getY(i));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {
                    int i = e.getActionIndex();
                    game.touch(2, e.getPointerId(i), e.getX(i), e.getY(i));
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                    for (int i = 0; i < e.getPointerCount(); i++) {
                        game.touch(2, e.getPointerId(i), -1f, -1f);
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    void pauseGame() {
        synchronized (game) {
            if (ready) {
                game.pause();
            }
        }
    }

    void back() {
        synchronized (game) {
            if (ready) {
                game.back();
            }
        }
    }
}
