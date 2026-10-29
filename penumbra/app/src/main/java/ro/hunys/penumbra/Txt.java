/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.content.Context;

import java.util.Locale;

/** Textele aplicației în română și engleză / App texts in Romanian and English. */
final class Txt {
    static final String VERSION = "1.0";

    final boolean ro;

    private Txt(boolean ro) {
        this.ro = ro;
    }

    static Txt of(Context c) {
        return forLang(Prefs.getLang(Prefs.get(c)));
    }

    static Txt forLang(String lang) {
        if (Prefs.LANG_RO.equals(lang)) return new Txt(true);
        if (Prefs.LANG_EN.equals(lang)) return new Txt(false);
        return new Txt("ro".equalsIgnoreCase(Locale.getDefault().getLanguage()));
    }

    private String t(String roText, String enText) {
        return ro ? roText : enText;
    }

    // ---- Ecranul principal / Main screen
    String title() { return "Penumbra"; }
    String subtitle() { return t("Întunecă ecranul mai mult decât luminozitatea minimă", "Dims the screen below the lowest brightness"); }

    String powerOn() { return t("●  PORNIT — atinge ca să oprești", "●  ON — tap to turn off"); }
    String powerOff() { return t("○  OPRIT — atinge ca să pornești", "○  OFF — tap to turn on"); }

    String permTitle() { return t("Lipsește o permisiune", "A permission is missing"); }
    String permBody() {
        return t("Penumbra are nevoie de „Afișare peste alte aplicații” ca să pună stratul întunecat peste ecran. Găsește Penumbra în listă și pornește permisiunea.",
                "Penumbra needs \"Display over other apps\" to put the dark layer over the screen. Find Penumbra in the list and turn the permission on.");
    }
    String permButton() { return t("Deschide setarea", "Open the setting"); }

    String dimLabel() { return t("Întunecare", "Dimming"); }
    String warmLabel() { return t("Filtru cald", "Warm filter"); }
    String warmHelp() { return t("Mai puțină lumină albastră, mai ușor pentru ochi seara.", "Less blue light, easier on the eyes at night."); }
    String dimLimit(int max) {
        return t("Maxim " + max + "%: Android nu lasă atingerile să treacă printr-un strat mai închis. Pentru mai întunecat, pornește „Lumină de fundal minimă” sau „Acoperire completă”.",
                "Max " + max + "%: Android won't let touches pass through a darker layer. For darker, turn on \"Minimum backlight\" or \"Full cover\".");
    }

    String optionsTitle() { return t("Opțiuni", "Options"); }
    String minLight(boolean on) { return t("Lumină de fundal minimă: " + (on ? "DA" : "NU"), "Minimum backlight: " + (on ? "YES" : "NO")); }
    String minLightHelp() {
        return t("Cât timp Penumbra e pornit, lumina ecranului coboară la minimul telefonului. Când îl oprești, revine cum era.",
                "While Penumbra is on, the screen's backlight drops to the phone's minimum. It goes back when you turn Penumbra off.");
    }

    String fullCover(boolean on) { return t("Acoperire completă: " + (on ? "DA" : "NU"), "Full cover: " + (on ? "YES" : "NO")); }
    String fullCoverHelp() {
        return t("Întunecă și bara de sus, bara de jos și panoul de notificări, până la 90%. Folosește un serviciu de accesibilitate care doar desenează stratul — nu citește nimic de pe ecran.",
                "Also dims the top bar, the bottom bar and the notification panel, down to 90%. Uses an accessibility service that only draws the layer — it reads nothing on the screen.");
    }
    String fullCoverServiceOn() { return t("Serviciul de accesibilitate: pornit ✓", "Accessibility service: on ✓"); }
    String fullCoverServiceOff() {
        return t("Serviciul de accesibilitate: oprit. Atinge butonul de mai sus și pornește „Penumbra” în Accesibilitate (uneori e la „Aplicații descărcate” / „Servicii instalate”).",
                "Accessibility service: off. Tap the button above and turn on \"Penumbra\" in Accessibility (sometimes under \"Downloaded apps\" / \"Installed services\").");
    }
    String fullCoverRestricted() {
        return t("Dacă „Penumbra” e gri și nu se poate porni: Setări › Aplicații › Penumbra › ⋮ › Permite setări restricționate, apoi încearcă din nou.",
                "If \"Penumbra\" is greyed out and can't be turned on: Settings › Apps › Penumbra › ⋮ › Allow restricted settings, then try again.");
    }
    String openA11yToast() { return t("Pornește „Penumbra” în lista de accesibilitate", "Turn on \"Penumbra\" in the accessibility list"); }

    String language() { return t("Limba / Language: Română", "Limba / Language: English"); }

    String tipsTitle() { return t("Sfaturi", "Tips"); }
    String tipsBody() {
        return t("• Buton rapid: trage în jos panoul de sus, apasă creionul (Editare) și adaugă „Penumbra”.\n"
                        + "• Din notificare: −10%, +10% și Oprește, fără să deschizi aplicația.\n"
                        + "• Huawei: Setări › Baterie › Lansare aplicații › Penumbra › gestionare manuală (toate pornite), ca să nu fie oprită în fundal.\n"
                        + "• După repornirea telefonului, Penumbra pornește singură dacă era pornită.",
                "• Quick button: pull down the top panel, tap the pencil (Edit) and add \"Penumbra\".\n"
                        + "• From the notification: −10%, +10% and Turn off, without opening the app.\n"
                        + "• Huawei: Settings › Battery › App launch › Penumbra › manage manually (all on), so it isn't stopped in the background.\n"
                        + "• After the phone restarts, Penumbra turns itself back on if it was on.");
    }

    String startFailed() {
        return t("Android nu a lăsat Penumbra să pornească acum. Deschide aplicația și încearcă din nou.",
                "Android didn't let Penumbra start right now. Open the app and try again.");
    }

    String footer() {
        return t("Open source (MIT) · Autor: Hunys with Claude AI · v" + VERSION,
                "Open source (MIT) · Author: Hunys with Claude AI · v" + VERSION);
    }

    // ---- Notificare / Notification
    String channelName() { return t("Penumbra pornit", "Penumbra on"); }
    String notifTitle(int dim) { return "Penumbra · " + dim + "%"; }
    String notifText(int dim, int warm, boolean minLight, boolean fullCover) {
        StringBuilder b = new StringBuilder();
        b.append(t("Întunecare ", "Dimming ")).append(dim).append('%');
        if (warm > 0) b.append(t(" · Filtru cald ", " · Warm ")).append(warm).append('%');
        if (minLight) b.append(t(" · Lumină minimă", " · Min. backlight"));
        if (fullCover) b.append(t(" · Complet", " · Full cover"));
        return b.toString();
    }
    String notifNoPermission() {
        return t("Lipsește permisiunea „Afișare peste alte aplicații” — atinge aici",
                "\"Display over other apps\" permission missing — tap here");
    }
    String actionOff() { return t("Oprește", "Turn off"); }
}
