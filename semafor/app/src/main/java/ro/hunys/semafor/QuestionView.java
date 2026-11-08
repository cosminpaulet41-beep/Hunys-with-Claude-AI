/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Întrebarea: bara cu „Gata ✓” și pagina mediului de învățare de pe scoalarutiera.ro, pe care
 * apasă singură „Continuă mediul GENERAL”. Folosită și peste ecran (la deblocare), și în fereastra
 * normală („Încearcă acum”, „Intră în cont”).
 *
 * The question: the bar with "Done ✓" and the scoalarutiera.ro learning page, on which it taps
 * "Continuă mediul GENERAL" by itself. Used both over the screen (on unlock) and in the normal
 * window ("Try it now", "Sign in").
 */
package ro.hunys.semafor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

final class QuestionView extends FrameLayout {
    /** Cine ține întrebarea: fereastra sau stratul de peste ecran / Who hosts it: the window or the overlay. */
    interface Host {
        void onDone();
    }

    private static final int BAR = Color.rgb(18, 21, 29);
    private static final int TEXT = Color.rgb(236, 239, 244);
    private static final int AMBER = Color.rgb(255, 183, 40);
    private static final int RED = Color.rgb(239, 83, 80);
    private static final int GREEN = Color.rgb(76, 200, 120);
    private static final int GREEN_DARK = Color.rgb(30, 110, 62);
    private static final int MAX_TRIES = 10;

    private final Host host;
    /** Peste ecran (fără fereastră de aplicație): dialogurile paginii nu pot fi arătate. */
    private final boolean overlay;
    private final Txt txt;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView web;
    private LinearLayout col;
    private ProgressBar progress;
    private TextView hint;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;

    /** Încă trebuie apăsat „Continuă” pe pagina asta / "Continuă" still needs tapping on this load. */
    private boolean autoContinue;
    private int tries;
    /** Când a început încărcarea și când a fost gata întrebarea (elapsedRealtime) / When loading started and the question was ready. */
    private long loadStartedAt;
    private long readyAt;
    private boolean loadFailed;

    private final Runnable tryContinue = new Runnable() {
        @Override
        public void run() {
            pressContinue();
        }
    };

    QuestionView(Context c, Host host, boolean overlay) {
        super(c);
        this.host = host;
        this.overlay = overlay;
        this.txt = Txt.of(c);
        build();
    }

    /** @param url null = mediul de învățare, direct la următoarea întrebare / null = the learning page, straight to the next question */
    void load(String url) {
        hideCustomView();
        autoContinue = url == null;
        tries = 0;
        loadStartedAt = SystemClock.elapsedRealtime();
        readyAt = 0;
        loadFailed = false;
        handler.removeCallbacks(tryContinue);
        hint.setVisibility(View.GONE);
        if (web == null) createWeb();
        String target = url == null ? Site.categoryUrl(getContext()) : url;
        Jurnal.log("pagina", "încarc / loading " + target + (autoContinue ? " (+ apăs Continuă / then tap Continue)" : ""));
        if (web != null) web.loadUrl(target);
    }

    /** Întrebarea e gata și nu e mai veche de maxAgeMs / The question is ready and not older than maxAgeMs. */
    boolean isReady(long maxAgeMs) {
        return web != null && !loadFailed && readyAt > 0 && SystemClock.elapsedRealtime() - readyAt < maxAgeMs;
    }

    /** Se încarcă chiar acum / Is loading right now. */
    boolean isLoading(long withinMs) {
        return web != null && !loadFailed && readyAt == 0 && loadStartedAt > 0
                && SystemClock.elapsedRealtime() - loadStartedAt < withinMs;
    }

    /** De cât timp e gata (ms) / How long it has been ready (ms). */
    long readyAge() {
        return readyAt == 0 ? -1 : SystemClock.elapsedRealtime() - readyAt;
    }

    private void markReady() {
        if (readyAt == 0) {
            readyAt = SystemClock.elapsedRealtime();
            Jurnal.log("pagina", "întrebarea e gata în / question ready in " + (readyAt - loadStartedAt) + " ms");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Jurnal.log("pagina", "atașată la fereastră / attached to window (" + (overlay ? "strat/overlay" : "fereastră/activity") + ")");
    }

    @Override
    protected void onDetachedFromWindow() {
        Jurnal.log("pagina", "desprinsă de fereastră / detached from window");
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        Jurnal.log("pagina", "vizibilitate fereastră / window visibility → " + QuestionOverlay.visibilityName(visibility));
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Jurnal.log("pagina", "focus → " + hasWindowFocus);
    }

    void resume() {
        if (web != null) web.onResume();
    }

    void pause() {
        if (web != null) web.onPause();
        // Contul rămâne ținut minte / The sign-in is remembered.
        try {
            CookieManager.getInstance().flush();
        } catch (RuntimeException ignored) {
        }
    }

    void destroy() {
        handler.removeCallbacksAndMessages(null);
        hideCustomView();
        if (web != null) {
            try {
                ((ViewGroup) web.getParent()).removeView(web);
                web.destroy();
            } catch (RuntimeException ignored) {
            }
            web = null;
        }
    }

    /** Înapoi: video, pagina anterioară, apoi închide / Back: video, previous page, then close. */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                if (customView != null) {
                    hideCustomView();
                } else if (web != null && web.canGoBack()) {
                    web.goBack();
                } else {
                    host.onDone();
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    // ---------------------------------------------------------------- „Continuă”

    private void pressContinue() {
        if (!autoContinue || web == null) return;
        web.evaluateJavascript(Site.CONTINUE_JS, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                String v = value == null ? "" : value;
                Jurnal.log("pagina", "Continuă: încercarea / try " + (tries + 1) + " → " + v
                        + " (ok=apăsat/tapped, no=negăsit/not found; 1=în cont/signed in, 0=nu/not)");
                boolean logged = v.endsWith("1\"");
                if (v.startsWith("\"ok")) {
                    autoContinue = false;
                    // Încă puțin cât se deschide întrebarea / A little longer while the question opens
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            markReady();
                        }
                    }, 1200);
                    if (!logged) showHint(txt.notLoggedHint(), AMBER);
                } else if (++tries < MAX_TRIES) {
                    handler.postDelayed(tryContinue, 500);
                } else {
                    autoContinue = false;
                    markReady();
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

    // ---------------------------------------------------------------- UI

    @SuppressLint("SetJavaScriptEnabled")
    private void build() {
        Context c = getContext();
        setBackgroundColor(Color.BLACK);

        col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        addView(col, new FrameLayout.LayoutParams(-1, -1));

        // ---- Bara de sus: semafor, titlu, Gata / Top bar: traffic light, title, Done
        LinearLayout bar = new LinearLayout(c);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(BAR);
        bar.setPadding(dp(12), dp(6), dp(8), dp(6));
        bar.addView(miniLights(), new LinearLayout.LayoutParams(-2, -2));
        TextView title = new TextView(c);
        title.setText(txt.title());
        title.setTextColor(TEXT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(dp(10), 0, 0, 0);
        bar.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        Button doneBtn = new Button(c);
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
                host.onDone();
            }
        });
        bar.addView(doneBtn, new LinearLayout.LayoutParams(-2, dp(42)));
        col.addView(bar, new LinearLayout.LayoutParams(-1, -2));

        progress = new ProgressBar(c, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(ColorStateList.valueOf(AMBER));
        col.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));

        hint = new TextView(c);
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

        createWeb();
    }

    /** Pagina web; se reface dacă motorul ei s-a oprit / The web page; rebuilt if its engine stopped. */
    @SuppressLint("SetJavaScriptEnabled")
    private void createWeb() {
        Context c = getContext();
        try {
            web = new WebView(c);
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
            cm.setAcceptThirdPartyCookies(web, true);

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
                    Jurnal.log("pagina", "începe / started " + url);
                    progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    if (request.isForMainFrame()) {
                        loadFailed = true;
                        Jurnal.log("pagina", "EROARE încărcare / load ERROR " + error.getErrorCode() + " " + error.getDescription() + " — " + request.getUrl());
                    }
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                    if (request.isForMainFrame()) {
                        Jurnal.log("pagina", "EROARE HTTP " + response.getStatusCode() + " — " + request.getUrl());
                    }
                }

                @Override
                public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                    Jurnal.log("pagina", "motorul paginii s-a oprit / renderer gone, crash=" + detail.didCrash());
                    try {
                        col.removeView(view);
                        view.destroy();
                    } catch (Throwable ignored) {
                    }
                    if (view == web) web = null;
                    host.onDone();
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Jurnal.log("pagina", "gata / finished " + url + " (titlu/title: " + view.getTitle() + ")");
                    progress.setVisibility(View.GONE);
                    if (autoContinue && Site.isCategoryPage(url)) {
                        handler.removeCallbacks(tryContinue);
                        handler.postDelayed(tryContinue, 150);
                    } else if (!autoContinue) {
                        markReady();
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
                    addView(view, new FrameLayout.LayoutParams(-1, -1));
                }

                @Override
                public void onHideCustomView() {
                    hideCustomView();
                }

                // Peste ecran nu se pot arăta dialoguri: le confirmăm singuri.
                // Over the screen no dialogs can be shown: confirm them ourselves.
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    if (!overlay) return super.onJsAlert(view, url, message, result);
                    result.confirm();
                    return true;
                }

                @Override
                public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                    if (!overlay) return super.onJsConfirm(view, url, message, result);
                    result.confirm();
                    return true;
                }

                @Override
                public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                    if (!overlay) return super.onJsPrompt(view, url, message, defaultValue, result);
                    result.confirm(defaultValue);
                    return true;
                }
            });
            col.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));
        } catch (Throwable e) {
            Jurnal.error("pagina", "nu am putut crea pagina web / couldn't create the WebView", e);
            web = null;
        }
    }

    /** Adresele web rămân aici; restul (mail, telefon, aplicații) se deschid afară. */
    private boolean openOutside(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return false;
        Jurnal.log("pagina", "deschid în afară / opening outside: " + uri);
        try {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            if (overlay) host.onDone();
        } catch (RuntimeException e) {
            Jurnal.error("pagina", "deschidere în afară", e);
        }
        return true;
    }

    private void hideCustomView() {
        if (customView == null) return;
        removeView(customView);
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
        Context c = getContext();
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(4), dp(4), dp(4), dp(4));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(40, 46, 60));
        bg.setCornerRadius(dp(7));
        box.setBackground(bg);
        int[] colors = {RED, AMBER, GREEN};
        for (int i = 0; i < colors.length; i++) {
            View dot = new View(c);
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
