/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Fereastra cu întrebarea: deschide mediul de învățare de pe scoalarutiera.ro și apasă singur
 * „Continuă mediul GENERAL”, ca să ajungi direct la următoarea întrebare. „Gata” te întoarce
 * unde erai înainte de deblocare.
 *
 * The question window: opens the scoalarutiera.ro learning environment and taps
 * "Continuă mediul GENERAL" by itself, so you land straight on the next question. "Done" takes
 * you back to where you were before unlocking.
 */
package ro.hunys.semafor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class QuestionActivity extends Activity {
    /** Altă adresă decât mediul de învățare (ex. pentru intrarea în cont) / Another address (e.g. to sign in). */
    static final String EXTRA_URL = "ro.hunys.semafor.URL";
    /** Când a apărut ultima dată fereastra (SystemClock.elapsedRealtime) / When the window last appeared. */
    static volatile long lastShownAt;

    private static final int BAR = Color.rgb(18, 21, 29);
    private static final int TEXT = Color.rgb(236, 239, 244);
    private static final int AMBER = Color.rgb(255, 183, 40);
    private static final int RED = Color.rgb(239, 83, 80);
    private static final int GREEN = Color.rgb(76, 200, 120);
    private static final int GREEN_DARK = Color.rgb(30, 110, 62);
    private static final int MAX_TRIES = 10;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Txt txt;
    private FrameLayout rootFrame;
    private WebView web;
    private ProgressBar progress;
    private TextView hint;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;

    /** Încă trebuie apăsat „Continuă” pe pagina asta / "Continuă" still needs tapping on this load. */
    private boolean autoContinue;
    private int tries;

    private final Runnable tryContinue = new Runnable() {
        @Override
        public void run() {
            pressContinue();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txt = Txt.of(this);
        getWindow().setStatusBarColor(BAR);
        getWindow().setNavigationBarColor(Color.BLACK);
        buildUi();
        handle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        lastShownAt = SystemClock.elapsedRealtime();
        setIntent(intent);
        handle(intent);
    }

    /** La fiecare deschidere: înapoi la mediul de învățare și la următoarea întrebare / On every opening: back to the next question. */
    private void handle(Intent intent) {
        hideCustomView();
        String url = intent != null ? intent.getStringExtra(EXTRA_URL) : null;
        autoContinue = url == null;
        if (url == null) url = Site.categoryUrl(this);
        tries = 0;
        handler.removeCallbacks(tryContinue);
        hint.setVisibility(View.GONE);
        web.loadUrl(url);
    }

    private void pressContinue() {
        if (!autoContinue || web == null) return;
        web.evaluateJavascript(Site.CONTINUE_JS, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                String v = value == null ? "" : value;
                boolean logged = v.endsWith("1\"");
                if (v.startsWith("\"ok")) {
                    autoContinue = false;
                    if (!logged) showHint(txt.notLoggedHint(), AMBER);
                } else if (++tries < MAX_TRIES) {
                    handler.postDelayed(tryContinue, 500);
                } else {
                    autoContinue = false;
                    showHint(logged ? txt.noContinueHint() : txt.notLoggedHint(), logged ? RED : AMBER);
                }
            }
        });
    }

    private void showHint(String s, int color) {
        hint.setText(s);
        hint.setTextColor(color);
        hint.setVisibility(View.VISIBLE);
    }

    private void done() {
        if (Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                hideCustomView();
                return true;
            }
            if (web != null && web.canGoBack()) {
                web.goBack();
                return true;
            }
            done();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastShownAt = SystemClock.elapsedRealtime();
        if (web != null) web.onResume();
    }

    @Override
    protected void onPause() {
        if (web != null) web.onPause();
        // Contul rămâne ținut minte / The sign-in is remembered.
        try {
            CookieManager.getInstance().flush();
        } catch (RuntimeException ignored) {
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) {
            try {
                ((ViewGroup) web.getParent()).removeView(web);
                web.destroy();
            } catch (RuntimeException ignored) {
            }
            web = null;
        }
        super.onDestroy();
    }

    // ---------------------------------------------------------------- UI

    @SuppressLint("SetJavaScriptEnabled")
    private void buildUi() {
        rootFrame = new FrameLayout(this);
        rootFrame.setBackgroundColor(Color.BLACK);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        rootFrame.addView(col, new FrameLayout.LayoutParams(-1, -1));

        // ---- Bara de sus: semafor, titlu, Gata / Top bar: traffic light, title, Done
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(BAR);
        bar.setPadding(dp(12), dp(6), dp(8), dp(6));
        bar.addView(miniLights(), new LinearLayout.LayoutParams(-2, -2));
        TextView title = new TextView(this);
        title.setText(txt.title());
        title.setTextColor(TEXT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(dp(10), 0, 0, 0);
        bar.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        Button doneBtn = new Button(this);
        doneBtn.setText(txt.windowDone());
        doneBtn.setAllCaps(false);
        doneBtn.setTextColor(Color.WHITE);
        doneBtn.setTypeface(Typeface.DEFAULT_BOLD);
        doneBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        doneBtn.setStateListAnimator(null);
        doneBtn.setMinHeight(dp(42));
        doneBtn.setMinimumHeight(dp(42));
        doneBtn.setPadding(dp(18), 0, dp(18), 0);
        GradientDrawable g = new GradientDrawable();
        g.setColor(GREEN_DARK);
        g.setCornerRadius(dp(10));
        doneBtn.setBackground(g);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done();
            }
        });
        bar.addView(doneBtn, new LinearLayout.LayoutParams(-2, dp(42)));
        col.addView(bar, new LinearLayout.LayoutParams(-1, -2));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        if (Build.VERSION.SDK_INT >= 21) {
            progress.setProgressTintList(android.content.res.ColorStateList.valueOf(AMBER));
        }
        col.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));

        hint = new TextView(this);
        hint.setBackgroundColor(BAR);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hint.setPadding(dp(14), dp(8), dp(14), dp(10));
        hint.setVisibility(View.GONE);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hint.setVisibility(View.GONE);
            }
        });
        col.addView(hint, new LinearLayout.LayoutParams(-1, -2));

        // ---- Pagina / The page
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return openOutside(request.getUrl());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openOutside(Uri.parse(url));
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                if (autoContinue && Site.isCategoryPage(url)) {
                    handler.removeCallbacks(tryContinue);
                    handler.postDelayed(tryContinue, 150);
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
            }

            // Explicațiile video pe tot ecranul / The video explanations full screen
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                hideCustomView();
                customView = view;
                customCallback = callback;
                rootFrame.addView(view, new FrameLayout.LayoutParams(-1, -1));
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }
        });
        col.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(rootFrame);
    }

    /** Adresele de pe web rămân în fereastră; restul (mail, telefon, aplicații) se deschid afară. */
    private boolean openOutside(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return false;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (RuntimeException ignored) {
        }
        return true;
    }

    private void hideCustomView() {
        if (customView == null) return;
        rootFrame.removeView(customView);
        customView = null;
        if (customCallback != null) {
            try {
                customCallback.onCustomViewHidden();
            } catch (RuntimeException ignored) {
            }
            customCallback = null;
        }
    }

    private View miniLights() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(4), dp(4), dp(4), dp(4));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(40, 46, 60));
        bg.setCornerRadius(dp(7));
        box.setBackground(bg);
        int[] colors = {RED, AMBER, GREEN};
        for (int i = 0; i < colors.length; i++) {
            View dot = new View(this);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(colors[i]);
            dot.setBackground(d);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(8), dp(8));
            if (i > 0) p.topMargin = dp(2);
            box.addView(dot, p);
        }
        return box;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
}
