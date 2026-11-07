/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.semafor;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Date;
import java.util.Locale;

/** Textele aplicației în română și engleză / App texts in Romanian and English. */
final class Txt {
    final boolean ro;

    private Txt(boolean ro) {
        this.ro = ro;
    }

    static Txt of(Context c) {
        String lang = Prefs.getLang(Prefs.get(c));
        if (Prefs.LANG_RO.equals(lang)) return new Txt(true);
        if (Prefs.LANG_EN.equals(lang)) return new Txt(false);
        return new Txt("ro".equalsIgnoreCase(Locale.getDefault().getLanguage()));
    }

    private String t(String roText, String enText) {
        return ro ? roText : enText;
    }

    static String time(Context c, long ms) {
        return DateFormat.getTimeFormat(c).format(new Date(ms));
    }

    // ---------------------------------------------------------------- ecran / screen

    String title() { return "Semafor"; }
    String subtitle() {
        return t("O întrebare din mediul de învățare la fiecare deblocare",
                "A learning-environment question every time you unlock");
    }

    String powerOn() { return t("PORNIT — atinge ca să oprești", "ON — tap to turn off"); }
    String powerOff() { return t("OPRIT — atinge ca să pornești", "OFF — tap to turn on"); }
    String startFailed() { return t("Nu am putut porni serviciul. Încearcă din nou.", "Couldn't start the service. Try again."); }

    String permTitle() { return t("Lipsește o permisiune", "A permission is missing"); }
    String permBody() {
        return t("Ca Semafor să poată deschide singur aplicația cu întrebări după deblocare, pornește „Afișare peste alte aplicații” pentru Semafor. "
                        + "Până atunci primești doar o notificare pe care o atingi.",
                "For Semafor to open the quiz app by itself after you unlock, turn on \"Display over other apps\" for Semafor. "
                        + "Until then you only get a notification to tap.");
    }
    String permButton() { return t("Deschide setarea", "Open the setting"); }

    String appTitle() { return t("Aplicația cu întrebări", "Quiz app"); }
    String noApp() { return t("Nicio aplicație aleasă", "No app chosen"); }
    String noAppHelp() {
        return t("Nu am găsit Școala Rutieră. Apasă „Alege aplicația” și alege-o din listă.",
                "Couldn't find Școala Rutieră. Tap \"Choose the app\" and pick it from the list.");
    }
    String appHelp() {
        return t("Se deschide de unde ai rămas. Răspunsul corect când greșești ți-l arată chiar aplicația, ca de obicei.",
                "It opens where you left off. When you get one wrong, the app itself shows the correct answer, as usual.");
    }
    String pickApp() { return t("Alege aplicația", "Choose the app"); }
    String tryNow() { return t("Încearcă acum", "Try it now"); }
    String openFailed() { return t("Nu am putut deschide aplicația.", "Couldn't open the app."); }
    String pickTitle() { return t("Alege aplicația cu întrebări", "Choose the quiz app"); }
    String suggested() { return t("potrivită", "likely match"); }

    String freqTitle() { return t("Cât de des", "How often"); }
    String change() { return t("Schimbă", "Change"); }
    String freqName(int idx) {
        switch (idx) {
            case 1: return t("La fiecare a 2-a deblocare", "Every 2nd unlock");
            case 2: return t("La fiecare a 3-a deblocare", "Every 3rd unlock");
            case 3: return t("Cel mult o dată la 10 minute", "At most once every 10 minutes");
            case 4: return t("Cel mult o dată la 30 de minute", "At most once every 30 minutes");
            case 5: return t("Cel mult o dată pe oră", "At most once an hour");
            default: return t("La fiecare deblocare", "Every unlock");
        }
    }
    String nightQuiet(boolean on) {
        return t("Liniște noaptea (23:00–07:00): ", "Quiet at night (23:00–07:00): ") + (on ? t("DA", "ON") : t("NU", "OFF"));
    }

    String pause() { return t("Pauză 1 oră", "Pause 1 hour"); }
    String resume() { return t("Reia acum", "Resume now"); }
    String pausedUntil(String hhmm) { return t("În pauză până la ", "Paused until ") + hhmm; }

    String statsTitle() { return t("Progres", "Progress"); }
    String stats(int today, int total) {
        return t("Azi: " + today + " · Total: " + total, "Today: " + today + " · Total: " + total);
    }
    String lastOpen(String hhmm) { return t("Ultima deschidere la deblocare: ", "Last opened on unlock: ") + hhmm; }
    String lastNever() {
        return t("Încă nicio deschidere. Blochează telefonul și deblochează-l ca să încerci.",
                "Nothing opened yet. Lock the phone and unlock it to try.");
    }

    String huaweiTitle() { return t("Pe Huawei / Honor", "On Huawei / Honor"); }
    String huaweiBody() {
        return t("Ca sistemul să nu oprească Semafor în fundal: Setări › Baterie › Lansare aplicații › Semafor › gestionare manuală, cu toate cele trei opțiuni pornite. "
                        + "Dacă tot nu se deschide la deblocare, vezi și Setări › Aplicații › Semafor › Permisiuni (ferestre pop-up / pornire în fundal).",
                "So the system doesn't stop Semafor in the background: Settings › Battery › App launch › Semafor › manage manually, with all three options on. "
                        + "If it still doesn't open on unlock, also check Settings › Apps › Semafor › Permissions (pop-up windows / background start).");
    }
    String launchSettings() { return t("Lansare aplicații", "App launch"); }
    String appSettings() { return t("Setările aplicației", "App settings"); }

    String helpTitle() { return t("Cum funcționează", "How it works"); }
    String helpBody() {
        return t("Când deblochezi telefonul, Semafor deschide aplicația aleasă (Școala Rutieră) exact unde ai rămas în mediul de învățare. "
                        + "Răspunzi la o întrebare, apoi apeși Acasă și îți vezi de treabă.\n"
                        + "Nu se deschide în timpul unui apel. Din notificare poți pune pauză o oră sau îl poți opri.\n"
                        + "Pornește singur după repornirea telefonului. Fără reclame, fără internet.",
                "When you unlock the phone, Semafor opens the chosen app (Școala Rutieră) exactly where you left off in the learning environment. "
                        + "Answer one question, then press Home and carry on.\n"
                        + "It doesn't open during a call. From the notification you can pause it for an hour or turn it off.\n"
                        + "It starts by itself after a reboot. No ads, no internet.");
    }

    String language() { return t("Limba / Language: Română", "Limba / Language: English"); }

    // ---------------------------------------------------------------- notificări / notifications

    String channelName() { return t("Semafor pornit", "Semafor running"); }
    String questionChannel() { return t("Întrebarea de la deblocare", "Unlock question"); }
    String questionTitle() { return t("Întrebarea ta te așteaptă", "Your question is waiting"); }
    String questionText(String app) { return t("Atinge ca să deschizi ", "Tap to open ") + app; }

    String notifTitle(int today) {
        return t("Semafor pornit · azi " + today + (today == 1 ? " întrebare" : " întrebări"),
                "Semafor on · " + today + (today == 1 ? " question" : " questions") + " today");
    }
    String notifPausedTitle(long until, Context c) { return pausedUntil(time(c, until)); }
    String notifText(String app, int freqIdx) { return app + " · " + freqName(freqIdx).toLowerCase(ro ? new Locale("ro") : Locale.ENGLISH); }
    String notifNoApp() { return t("Alege aplicația cu întrebări în Semafor", "Choose the quiz app in Semafor"); }
    String actionPause() { return t("Pauză 1 h", "Pause 1 h"); }
    String actionResume() { return t("Reia", "Resume"); }
    String actionOff() { return t("Oprește", "Turn off"); }
}
