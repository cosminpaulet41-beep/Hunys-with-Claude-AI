/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Întrebarea într-o fereastră normală: pentru „Încearcă acum”, „Intră în cont” și notificarea
 * de rezervă. La deblocare întrebarea apare peste ecran (QuestionOverlay).
 *
 * The question in a normal window: for "Try it now", "Sign in" and the fallback notification.
 * On unlock the question appears over the screen (QuestionOverlay).
 */
package ro.hunys.semafor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

public class QuestionActivity extends Activity implements QuestionView.Host {
    /** Altă adresă decât mediul de învățare (ex. pentru intrarea în cont) / Another address (e.g. to sign in). */
    static final String EXTRA_URL = "ro.hunys.semafor.URL";

    private QuestionView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(18, 21, 29));
        getWindow().setNavigationBarColor(Color.BLACK);
        // Nu două pagini deodată: stratul de peste ecran se închide / Not two pages at once: the overlay closes.
        UnlockService.hideOverlay();
        Jurnal.log("fereastră", "onCreate");
        view = new QuestionView(this, this, false);
        setContentView(view);
        view.load(getIntent() != null ? getIntent().getStringExtra(EXTRA_URL) : null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        view.load(intent != null ? intent.getStringExtra(EXTRA_URL) : null);
    }

    @Override
    public void onDone() {
        Jurnal.log("fereastră", "Gata / Done");
        finishAndRemoveTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Jurnal.log("fereastră", "onResume");
        view.resume();
    }

    @Override
    protected void onPause() {
        Jurnal.log("fereastră", "onPause");
        view.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        view.destroy();
        // Contul poate fi altul acum: întrebarea ascunsă se pregătește din nou.
        // The sign-in may have changed: the hidden question is prepared again.
        UnlockService.refreshPreload();
        super.onDestroy();
    }
}
