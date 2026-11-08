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

    String modeTitle() { return t("Unde se deschide întrebarea", "Where the question opens"); }
    String modeName(int mode) {
        return mode == Prefs.MODE_APP
                ? t("În aplicația aleasă", "In the chosen app")
                : t("În fereastra Semafor, direct la întrebare", "In the Semafor window, straight to the question");
    }
    String catName(int idx) {
        switch (idx) {
            case 0: return "A (A, A1, A2, AM)";
            case 2: return "C (C, C1)";
            case 3: return "D (D, D1, Tb, Tv)";
            case 4: return "E (BE, CE, DE, C1E, D1E)";
            case 5: return t("Redobândire", "Licence recovery");
            case 6: return "Tr";
            default: return "B (B, B1)";
        }
    }
    String category(int idx) { return t("Categoria ", "Category ") + catName(idx); }
    String changeCategory() { return t("Schimbă categoria", "Change category"); }
    String siteLabel(int idx) {
        String short_ = idx == 5 ? t("redobândire", "recovery") : catName(idx).split(" ")[0];
        return t("Școala Rutieră · cat. ", "Școala Rutieră · cat. ") + short_;
    }
    String windowHelp() {
        return t("La deblocare apare direct o întrebare de pe scoalarutiera.ro: următoarea dintre cele la care ai greșit, următoarea din mediul GENERAL sau linkul tău, cum alegi mai sus. "
                        + "Răspunsul corect ți-l arată site-ul când greșești. „Gata ✓” te întoarce unde erai.\n"
                        + "O singură dată: apasă „Intră în cont” și conectează-te cu contul tău Școala Rutieră (iconița din dreapta sus), ca să continui de unde ai rămas.",
                "On unlock a scoalarutiera.ro question appears straight away: the next one you got wrong, the next one in the GENERAL environment, or your link, as chosen above. "
                        + "The site shows the correct answer when you get one wrong. \"Done ✓\" takes you back to where you were.\n"
                        + "Once only: tap \"Sign in\" and sign in with your Școala Rutieră account (the icon top right) so you continue where you left off.");
    }
    String targetTitle() { return t("Ce se deschide", "What opens"); }
    String targetName(int target) {
        if (target == Prefs.TARGET_GENERAL) return t("Mediul GENERAL (apasă singur „Continuă”)", "GENERAL environment (taps \"Continue\" itself)");
        if (target == Prefs.TARGET_LINK) return t("Linkul meu, exact cum e", "My link, exactly as it is");
        return t("Întrebările la care ai greșit (următoarea)", "Questions you got wrong (the next one)");
    }
    String targetLine(int target) { return t("Se deschide: ", "Opens: ") + targetName(target); }
    String linkLine(String link, int target) {
        String l = link == null || link.length() == 0 ? t("(niciunul)", "(none)") : link;
        if (target == Prefs.TARGET_WRONG) return t("Dacă nu găsesc lista pe pagină, deschid linkul tău: ", "If I can't find the list on the page, I open your link: ") + l;
        return t("Linkul tău: ", "Your link: ") + l;
    }
    String editLink() { return t("Linkul meu", "My link"); }
    String linkHint() { return t("Lipește linkul (https://…)", "Paste the link (https://…)"); }
    String linkInvalid() { return t("Linkul trebuie să înceapă cu https://", "The link must start with https://"); }
    String linkReset() { return t("Linkul inițial", "Original link"); }

    String signIn() { return t("Intră în cont (o singură dată)", "Sign in (once)"); }
    String windowDone() { return t("Gata ✓", "Done ✓"); }
    String notLoggedHint() {
        return t("Nu ești în cont aici. Atinge iconița din dreapta sus și intră o dată în contul tău Școala Rutieră, ca să continui de unde ai rămas. (Atinge ca să ascunzi.)",
                "You're not signed in here. Tap the icon top right and sign in once with your Școala Rutieră account to continue where you left off. (Tap to hide.)");
    }
    String noContinueHint() {
        return t("Nu am găsit pe pagină ce trebuia apăsat. Alege tu din pagină. (Atinge ca să ascunzi.)",
                "Couldn't find what to tap on the page. Pick it on the page yourself. (Tap to hide.)");
    }

    String appTitle() { return t("Aplicația cu întrebări", "Quiz app"); }
    String noApp() { return t("Nicio aplicație aleasă", "No app chosen"); }
    String noAppHelp() {
        return t("Nu am găsit Școala Rutieră. Apasă „Alege aplicația” și alege-o din listă.",
                "Couldn't find Școala Rutieră. Tap \"Choose the app\" and pick it from the list.");
    }
    String appHelp() {
        return t("Se deschide aplicația aleasă. Dacă te trimite la ecranul ei de start, alege „În fereastra Semafor”, care merge direct la întrebare.",
                "The chosen app opens. If it sends you to its start screen, pick \"In the Semafor window\", which goes straight to the question.");
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

    String checkTitle() { return t("Ca să meargă la fiecare deblocare", "So it works on every unlock"); }
    String overlayCheck(boolean ok) {
        return ok ? t("✓ Afișare peste alte aplicații: permisă", "✓ Display over other apps: allowed")
                : t("✗ Afișare peste alte aplicații: lipsește (vezi sus)", "✗ Display over other apps: missing (see above)");
    }
    String batteryCheck(boolean ok) {
        return ok ? t("✓ Baterie: fără restricții", "✓ Battery: unrestricted")
                : t("✗ Baterie: sistemul poate opri Semafor când ecranul e stins", "✗ Battery: the system may stop Semafor while the screen is off");
    }
    String batteryButton() { return t("Scoate restricțiile de baterie", "Remove battery restrictions"); }

    String logTitle() { return t("Ultimele deblocări", "Recent unlocks"); }
    String logEmpty() { return t("Încă nimic. Blochează și deblochează telefonul.", "Nothing yet. Lock and unlock the phone."); }
    String logHelp() {
        return t("Dacă ai deblocat telefonul și ora nu apare aici, sistemul a oprit Semafor în fundal: fă setările de mai jos. "
                        + "„Semafor a pornit” fără să-l fi pornit tu înseamnă că sistemul l-a oprit între timp.",
                "If you unlocked the phone and the time isn't listed here, the system stopped Semafor in the background: do the settings below. "
                        + "\"Semafor started\" without you starting it means the system had stopped it.");
    }
    String logLine(String code) {
        if (Prefs.LOG_SERVICE.equals(code)) return t("Semafor a pornit", "Semafor started");
        if (Prefs.LOG_OPEN.equals(code)) return t("✓ întrebarea a apărut pe ecran", "✓ question shown on screen");
        if (Prefs.LOG_OPEN_RETRY.equals(code)) return t("✓ s-a deschis din a doua încercare", "✓ opened on the second try");
        if (Prefs.LOG_APP.equals(code)) return t("✓ aplicația a fost deschisă", "✓ app opened");
        if (Prefs.LOG_BLOCKED.equals(code)) return t("✗ nu s-a putut arăta → notificare", "✗ couldn't be shown → notification");
        if (Prefs.LOG_FREQ.equals(code)) return t("– sărită („Cât de des”)", "– skipped (\"How often\")");
        if (Prefs.LOG_PAUSE.equals(code)) return t("– sărită (pauză)", "– skipped (paused)");
        if (Prefs.LOG_NIGHT.equals(code)) return t("– sărită (liniște noaptea)", "– skipped (quiet at night)");
        if (Prefs.LOG_CALL.equals(code)) return t("– sărită (apel sau sunet de apel)", "– skipped (call or ringing)");
        if (Prefs.LOG_NO_APP.equals(code)) return t("✗ nicio aplicație aleasă", "✗ no app chosen");
        return code;
    }

    String logOpen() { return t("Jurnal pentru depanare", "Debug log"); }
    String logScreenTitle() { return t("Jurnal", "Log"); }
    String logHowTo() {
        return t("1. Apasă „Șterge”.  2. Blochează și deblochează telefonul de 3–4 ori (și apasă o dată „Încearcă acum”).  "
                        + "3. Revino aici și apasă „Trimite fișierul”, apoi alege Claude. Fișierul rămâne și în Descărcări.",
                "1. Tap \"Clear\".  2. Lock and unlock the phone 3–4 times (and tap \"Try it now\" once).  "
                        + "3. Come back here, tap \"Send the file\" and pick Claude. The file also stays in Downloads.");
    }
    String logSend() { return t("Trimite fișierul", "Send the file"); }
    String logCopy() { return t("Copiază", "Copy"); }
    String logRefresh() { return t("Reîmprospătează", "Refresh"); }
    String logClear() { return t("Șterge", "Clear"); }
    String logCopied() { return t("Jurnalul e copiat. Lipește-l în conversație.", "Log copied. Paste it into the chat."); }
    String logSaved(String name) { return t("Salvat în Descărcări: ", "Saved to Downloads: ") + name; }

    String frozenOk() { return t("✓ Huawei nu a înghețat Semafor (până acum)", "✓ Huawei hasn't frozen Semafor (so far)"); }
    String frozenBad(int n, String hhmm) {
        return t("✗ Huawei a înghețat Semafor de " + n + " ori (ultima dată la " + hhmm + "). Cât e înghețat, nu vede deblocările. Fă pașii de mai jos.",
                "✗ Huawei froze Semafor " + n + " times (last at " + hhmm + "). While frozen it can't see unlocks. Do the steps below.");
    }

    String huaweiTitle() { return t("Pe Huawei / Honor", "On Huawei / Honor"); }
    String huaweiBody() {
        return t("1. Setări › Baterie › Lansare aplicații › Semafor: oprește „Gestionare automată”, apoi în fereastra care apare pornește toate trei: "
                        + "Lansare automată, Lansare secundară și Rulare în fundal › OK. (Butonul „Lansare aplicații” de mai jos te duce acolo.)\n"
                        + "2. Deschide aplicațiile recente (pătratul de jos), trage în jos cardul Semafor până apare lacătul, ca sistemul să nu-l închidă.\n"
                        + "3. Dacă există: Setări › Aplicații › Semafor › Baterie / Consum de energie › permite activitatea în fundal.",
                "1. Settings › Battery › App launch › Semafor: turn off \"Manage automatically\", then in the window that appears turn on all three: "
                        + "Auto-launch, Secondary launch and Run in background › OK. (The \"App launch\" button below takes you there.)\n"
                        + "2. Open recent apps (the square at the bottom), drag the Semafor card down until a padlock appears, so the system doesn't close it.\n"
                        + "3. If present: Settings › Apps › Semafor › Battery / Power usage › allow background activity.");
    }

    String launchSettings() { return t("Lansare aplicații", "App launch"); }
    String appSettings() { return t("Setările aplicației", "App settings"); }

    String helpTitle() { return t("Cum funcționează", "How it works"); }
    String helpBody() {
        return t("Când deblochezi telefonul, Semafor îți arată următoarea întrebare din mediul de învățare Școala Rutieră. "
                        + "Răspunzi, apoi apeși „Gata ✓” și ești înapoi unde erai.\n"
                        + "Nu se deschide în timpul unui apel. Din notificare poți pune pauză o oră sau îl poți opri.\n"
                        + "Pornește singur după repornirea telefonului. Internetul îl folosește doar fereastra cu întrebarea.",
                "When you unlock the phone, Semafor shows the next question from the Școala Rutieră learning environment. "
                        + "Answer it, then tap \"Done ✓\" and you're back where you were.\n"
                        + "It doesn't open during a call. From the notification you can pause it for an hour or turn it off.\n"
                        + "It starts by itself after a reboot. Only the question window uses the internet.");
    }

    String language() { return t("Limba / Language: Română", "Limba / Language: English"); }

    // ---------------------------------------------------------------- notificări / notifications

    String channelName() { return t("Semafor pornit", "Semafor running"); }
    String questionChannel() { return t("Întrebarea de la deblocare", "Unlock question"); }
    String questionTitle() { return t("Întrebarea ta te așteaptă", "Your question is waiting"); }
    String questionText(String app) {
        if (app == null || app.length() == 0) return t("Atinge ca să răspunzi la o întrebare", "Tap to answer a question");
        return t("Atinge ca să deschizi ", "Tap to open ") + app;
    }

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
