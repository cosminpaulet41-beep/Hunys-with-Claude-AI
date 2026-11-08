/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Ecranul jurnalului: îl vezi, îl trimiți ca fișier, îl copiezi sau îl ștergi.
 * The log screen: view it, send it as a file, copy it or clear it.
 */
package ro.hunys.semafor;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogActivity extends Activity {
    private static final int BG = Color.rgb(12, 14, 20);
    private static final int BTN = Color.rgb(45, 51, 66);
    private static final int TEXT = Color.rgb(236, 239, 244);
    private static final int MUTED = Color.rgb(150, 160, 178);
    /** Cât arătăm / copiem din coada jurnalului / How much of the log's tail we show / copy. */
    private static final int SHOW_CHARS = 60000;

    private Txt txt;
    private TextView body;
    private ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txt = Txt.of(this);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(14), dp(14), dp(8));

        TextView title = new TextView(this);
        title.setText(txt.logScreenTitle());
        title.setTextColor(TEXT);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView how = new TextView(this);
        how.setText(txt.logHowTo());
        how.setTextColor(MUTED);
        how.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        how.setPadding(0, dp(4), 0, dp(8));
        root.addView(how);

        LinearLayout row1 = new LinearLayout(this);
        row1.addView(button(txt.logSend(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        }), weight(0));
        row1.addView(button(txt.logCopy(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy();
            }
        }), weight(8));
        root.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.addView(button(txt.logRefresh(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        }), weight(0));
        row2.addView(button(txt.logClear(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Jurnal.clear();
                Jurnal.log("jurnal", "șters / cleared");
                refresh();
            }
        }), weight(8));
        LinearLayout.LayoutParams r2 = new LinearLayout.LayoutParams(-1, -2);
        r2.topMargin = dp(8);
        root.addView(row2, r2);

        scroll = new ScrollView(this);
        body = new TextView(this);
        body.setTextColor(TEXT);
        body.setTypeface(Typeface.MONOSPACE);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        body.setTextIsSelectable(true);
        body.setPadding(0, dp(10), 0, dp(10));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(root);
        refresh();
    }

    private String fullText() {
        return Jurnal.header(this) + Jurnal.read();
    }

    /** Capul + ultima parte a jurnalului / The header + the last part of the log. */
    private String shortText() {
        String log = Jurnal.read();
        if (log.length() > SHOW_CHARS) {
            int cut = log.indexOf('\n', log.length() - SHOW_CHARS);
            log = "…\n" + (cut > 0 ? log.substring(cut + 1) : log.substring(log.length() - SHOW_CHARS));
        }
        return Jurnal.header(this) + log;
    }

    private void refresh() {
        body.setText(shortText());
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void copy() {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("Semafor log", shortText()));
            Toast.makeText(this, txt.logCopied(), Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show();
        }
    }

    /** Salvează în Descărcări și deschide „Trimite” / Saves to Downloads and opens "Share". */
    private void sendFile() {
        String text = fullText();
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Semafor log");
        String name = "Semafor-jurnal-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date()) + ".txt";
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri != null) {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    try {
                        os.write(text.getBytes("UTF-8"));
                    } finally {
                        os.close();
                    }
                }
            } catch (Exception e) {
                Jurnal.error("jurnal", "salvare în Descărcări / saving to Downloads", e);
                uri = null;
            }
        }
        if (uri != null) {
            Toast.makeText(this, txt.logSaved(name), Toast.LENGTH_LONG).show();
            send.putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            send.putExtra(Intent.EXTRA_TEXT, shortText());
        }
        try {
            startActivity(Intent.createChooser(send, txt.logSend()));
        } catch (RuntimeException e) {
            Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show();
        }
    }

    private Button button(String label, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(TEXT);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setStateListAnimator(null);
        b.setMinHeight(dp(46));
        b.setMinimumHeight(dp(46));
        GradientDrawable g = new GradientDrawable();
        g.setColor(BTN);
        g.setCornerRadius(dp(12));
        b.setBackground(g);
        b.setOnClickListener(l);
        return b;
    }

    private LinearLayout.LayoutParams weight(int leftMarginDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
        p.leftMargin = dp(leftMarginDp);
        return p;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
}
