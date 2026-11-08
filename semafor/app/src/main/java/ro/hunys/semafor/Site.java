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

    /**
     * Pe pagina categoriei găsește linkul spre întrebările la care ai greșit și îl deschide.
     * On the category page, finds the link to the questions you got wrong and opens it.
     */
    static String wrongJs(String slug) {
        String cat = "categoria-" + slug;
        return "(function(){"
                + "var b=document.body;var txt=b?(b.textContent||''):'';"
                + "var logged=txt.indexOf('Contul t')>=0?'1':'0';"
                + "var as=document.querySelectorAll('a[href*=\"raspuns-gresit\"]');var best=null;"
                + "for(var i=0;i<as.length&&!best;i++){var h=as[i].getAttribute('href')||'';"
                + "if(h.indexOf('" + cat + "')>=0&&/\\/\\d+/.test(h))best=as[i];}"
                + "for(var j=0;j<as.length&&!best;j++){var g=as[j].getAttribute('href')||'';"
                + "if(g.indexOf('" + cat + "')>=0)best=as[j];}"
                + "if(best){location.href=best.href;return 'ok'+logged+'|'+best.href;}"
                + "return 'no'+logged+'|'+as.length;})()";
    }

    /** Adresa de unde pornește fereastra / Where the window starts. */
    static String startUrl(Context c) {
        android.content.SharedPreferences sp = Prefs.get(c);
        if (Prefs.getTarget(sp) == Prefs.TARGET_LINK) {
            String link = Prefs.getLink(sp);
            if (link.length() > 0) return link;
        }
        return categoryUrl(c);
    }

    /** JavaScript-ul care apasă singur, sau null dacă se deschide doar linkul / The auto-tap script, or null for the plain link. */
    static String autoJs(Context c) {
        android.content.SharedPreferences sp = Prefs.get(c);
        int t = Prefs.getTarget(sp);
        if (t == Prefs.TARGET_LINK && Prefs.getLink(sp).length() > 0) return null;
        if (t == Prefs.TARGET_WRONG) return wrongJs(SLUGS[Prefs.getCategory(sp)]);
        return CONTINUE_JS;
    }

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
