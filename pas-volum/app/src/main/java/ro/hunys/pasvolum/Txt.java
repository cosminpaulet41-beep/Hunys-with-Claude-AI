/*
 * Pas Volum / Volume Step
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.pasvolum;

import java.util.Locale;

/** Textele aplicației în română și engleză / App texts in Romanian and English. */
final class Txt {
    final boolean ro;

    private Txt(boolean ro) {
        this.ro = ro;
    }

    static Txt forLang(String lang) {
        if (Prefs.LANG_RO.equals(lang)) return new Txt(true);
        if (Prefs.LANG_EN.equals(lang)) return new Txt(false);
        return new Txt("ro".equalsIgnoreCase(Locale.getDefault().getLanguage()));
    }

    private String t(String roText, String enText) {
        return ro ? roText : enText;
    }

    String title() { return t("Pas Volum", "Volume Step"); }
    String subtitle() { return t("Volumul sare mai multe trepte la o singură apăsare", "The volume jumps several steps per press"); }

    String serviceLabel() { return t("Serviciu:", "Service:"); }
    String serviceOn() { return t("ACTIV — funcționează", "ACTIVE — working"); }
    String serviceStarting() { return t("pornește…", "starting…"); }
    String serviceOff() { return t("OPRIT (volum normal)", "OFF (normal volume)"); }

    String stepLabel() { return t("Pas la fiecare apăsare", "Step per press"); }
    String example(int a, int b, int c) { return t("Exemplu: ", "Example: ") + a + " → " + b + " → " + c; }

    String featureOn() { return t("Funcția: PORNITĂ", "Feature: ON"); }
    String featureOff() { return t("Funcția: OPRITĂ (volum normal)", "Feature: OFF (normal volume)"); }

    String language() { return t("Limba / Language: Română", "Limba / Language: English"); }

    String volumeNow(int cur, int max) { return t("Volum acum: ", "Volume now: ") + cur + " / " + max; }

    String lastKeyLabel() { return t("Ultima apăsare detectată:", "Last press detected:"); }
    String lastKeyNone() { return t("niciuna încă — apasă + sau − pe telecomandă", "none yet — press + or − on the remote"); }
    String keyUp() { return t("VOLUM +", "VOLUME +"); }
    String keyDown() { return t("VOLUM −", "VOLUME −"); }
    String secondsAgo(long s) { return t("acum " + s + " s", s + " s ago"); }

    String helpTitle() { return t("Cum funcționează", "How it works"); }
    String helpBody() {
        return t("Apeși + sau − ca de obicei. Sistemul mută volumul cu 1, iar Pas Volum adaugă imediat restul treptelor.\n"
                        + "Rămâne pornit în fundal și pornește singur după repornirea proiectorului.\n"
                        + "Nu are nevoie de accesibilitate.",
                "Press + or − as usual. The system moves the volume by 1 and Volume Step instantly adds the remaining steps.\n"
                        + "It keeps running in the background and starts by itself after the projector reboots.\n"
                        + "No accessibility permission needed.");
    }

    String notificationText(int step) {
        return t("Fiecare apăsare pe + / − = " + step + " trepte", "Each + / − press = " + step + " steps");
    }

    String footer() { return t("Open source (MIT) · Autor: Hunys with Claude AI · v1.2", "Open source (MIT) · Author: Hunys with Claude AI · v1.2"); }
}
