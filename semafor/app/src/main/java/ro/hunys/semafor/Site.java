/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.semafor;

import android.content.Context;
import android.net.Uri;

import java.util.Locale;

/** Adresele de pe scoalarutiera.ro / The scoalarutiera.ro addresses. */
final class Site {
    static final String HOME = "https://www.scoalarutiera.ro/";
    private static final String CATEGORY_PREFIX = "https://www.scoalarutiera.ro/intrebari-posibile-drpciv-categoria-";
    private static final String CATEGORY_PATH = "/intrebari-posibile-drpciv-categoria-";

    /** Ca în meniul site-ului: A, B, C, D, E, Redobândire, Tr / As in the site's menu. */
    static final String[] SLUGS = {"a", "b", "c", "d", "e", "r", "tr"};
    static final int DEFAULT_CATEGORY = 1; // B

    /**
     * Apasă singur „Continuă mediul GENERAL” (sau „Începe…”) și spune dacă ești în cont.
     * Taps "Continuă mediul GENERAL" (or "Începe…") by itself and says whether you're signed in.
     */
    static final String CONTINUE_JS = "(function(){"
            + "var b=document.body;var txt=b?(b.textContent||''):'';"
            + "var logged=txt.indexOf('Contul t')>=0?'1':'0';"
            + "var els=document.querySelectorAll('a,button');"
            + "for(var i=0;i<els.length;i++){var e=els[i];var t=(e.textContent||'').toLowerCase();"
            + "if(t.indexOf('mediul general')>=0){if(e.href){location.href=e.href;}else{e.click();}return 'ok'+logged;}}"
            + "return 'no'+logged;})()";

    private Site() {}

    static String categoryUrl(Context c) {
        return CATEGORY_PREFIX + SLUGS[Prefs.getCategory(Prefs.get(c))];
    }

    /** Pagina mediului de învățare pentru o categorie (fără o întrebare anume) / The category's learning page. */
    static boolean isCategoryPage(String url) {
        if (url == null) return false;
        try {
            Uri u = Uri.parse(url);
            if (!isSiteHost(u.getHost())) return false;
            String path = u.getPath();
            if (path == null) return false;
            while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            for (String s : SLUGS) {
                if (path.equals(CATEGORY_PATH + s)) return true;
            }
        } catch (RuntimeException ignored) {
        }
        return false;
    }

    static boolean isSiteHost(String host) {
        if (host == null) return false;
        String h = host.toLowerCase(Locale.ROOT);
        return h.equals("scoalarutiera.ro") || h.endsWith(".scoalarutiera.ro");
    }
}
