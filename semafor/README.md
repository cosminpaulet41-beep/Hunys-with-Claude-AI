# Semafor

**Autor / Author:** Hunys with Claude AI · **Licență / License:** MIT (open source) · **v1.2**

<img src="icon.png" width="96" alt="Semafor">

---

## 🇷🇴 Română

Semafor te pune să rezolvi o întrebare pentru permisul auto (categoria B) **de fiecare dată când deblochezi telefonul**. La deblocare deschide o fereastră cu mediul de învățare GENERAL de pe **scoalarutiera.ro** și apasă singur „Continuă”, așa că ajungi direct la următoarea întrebare. Răspunzi, apoi apeși **Gata ✓** și ești înapoi unde erai. Dacă greșești, răspunsul corect ți-l arată site-ul, ca de obicei.

Semafor nu conține întrebările. Folosește contul tău Școala Rutieră, așa că progresul tău rămâne acolo.

### Ce face
- La fiecare deblocare deschide direct următoarea întrebare din mediul de învățare (categoria B; se poate schimba).
- Sau, dacă vrei, deschide o aplicație aleasă de tine (ex. aplicația Școala Rutieră).
- **Cât de des:** la fiecare deblocare, la a 2-a, la a 3-a, sau cel mult o dată la 10 / 30 / 60 de minute.
- **Liniște noaptea** (opțional): nimic între 23:00 și 07:00.
- Nu se deschide în timpul unui apel.
- **Notificare cu butoane:** Pauză 1 h / Reia și Oprește.
- **Progres:** câte întrebări ai deschis azi și în total, și ora ultimei deschideri.
- Pornește singur după repornirea telefonului.
- **Ultimele deblocări:** o listă cu ce s-a întâmplat la fiecare deblocare (deschisă, sărită și de ce, blocată de Android), ca să vezi dacă ceva îl oprește.
- Interfață în **română și engleză**. Fără reclame. Internetul îl folosește doar fereastra cu întrebarea.

### Instalare
1. Descarcă `Semafor-v1.2.apk` de la [Releases](https://github.com/cosminpaulet41-beep/Hunys-with-Claude-AI/releases) și deschide-l. Dacă telefonul îți cere, permite instalarea din surse necunoscute.
2. Deschide Semafor și apasă **OPRIT — atinge ca să pornești**. Telefonul te duce la „Afișare peste alte aplicații”: pornește permisiunea pentru Semafor și revino. Fără ea, Android nu lasă o aplicație să deschidă alta din fundal, și primești doar o notificare pe care o atingi.
3. Apasă **„Intră în cont (o singură dată)”** și conectează-te cu contul tău Școala Rutieră (iconița din dreapta sus a site-ului). Așa continui de unde ai rămas.
4. La „Ca să meargă la fiecare deblocare” apasă **„Scoate restricțiile de baterie”**. Pe **Huawei** (EMUI) fă și: Setări › Baterie › Lansare aplicații › Semafor › gestionare manuală, cu toate cele trei opțiuni pornite. Altfel sistemul îl poate opri în fundal. Butonul „Lansare aplicații” din Semafor te duce acolo.
5. Blochează telefonul și deblochează-l. Ar trebui să apară următoarea întrebare.

### Compilare
Vezi `build.sh`. Nu are nevoie de Android Studio sau Gradle. Folosește aapt2, javac, dx, uber-apk-signer și android.jar API 34. Iconițele se fac cu `tools/make_icons.py` (Python + Pillow).
Cheia de semnare (`keystore/semafor.keystore`, parola `semafor`) e inclusă, ca versiunile noi să se instaleze peste cea veche.

---

## 🇬🇧 English

Semafor makes you answer one driving-licence question (category B, Romania) **every time you unlock your phone**. On unlock it opens a window with the GENERAL learning environment on **scoalarutiera.ro** and taps "Continuă" by itself, so you land straight on the next question. Answer it, then tap **Done ✓** and you're back where you were. If you get it wrong, the site shows the correct answer, as usual.

Semafor doesn't contain the questions. It uses your Școala Rutieră account, so your progress stays there.

### What it does
- On every unlock it opens the next question of the learning environment directly (category B; can be changed).
- Or, if you prefer, it opens an app you choose (e.g. the Școala Rutieră app).
- **How often:** every unlock, every 2nd, every 3rd, or at most once every 10 / 30 / 60 minutes.
- **Quiet at night** (optional): nothing between 23:00 and 07:00.
- Doesn't open during a call.
- **Notification with buttons:** Pause 1 h / Resume and Turn off.
- **Progress:** how many questions you opened today and in total, and when the last one opened.
- Starts by itself after a reboot.
- **Recent unlocks:** a list of what happened on each unlock (opened, skipped and why, blocked by Android), so you can see if something stops it.
- Interface in **Romanian and English**. No ads. Only the question window uses the internet.

### Install
1. Download `Semafor-v1.2.apk` from [Releases](https://github.com/cosminpaulet41-beep/Hunys-with-Claude-AI/releases) and open it. If the phone asks, allow installs from unknown sources.
2. Open Semafor and tap **OFF — tap to turn on**. The phone takes you to "Display over other apps": turn it on for Semafor and come back. Without it, Android won't let an app open another one from the background, and you only get a notification to tap.
3. Tap **"Sign in (once)"** and sign in with your Școala Rutieră account (the icon top right of the site). That way you continue where you left off.
4. Under "So it works on every unlock" tap **"Remove battery restrictions"**. On **Huawei** (EMUI) also do: Settings › Battery › App launch › Semafor › manage manually, with all three options on. Otherwise the system may stop it in the background. The "App launch" button in Semafor takes you there.
5. Lock the phone and unlock it. The next question should appear.

### Building
See `build.sh`. No Android Studio or Gradle needed. It uses aapt2, javac, dx, uber-apk-signer and android.jar API 34. The icons are made with `tools/make_icons.py` (Python + Pillow).
The signing key (`keystore/semafor.keystore`, password `semafor`) is included so new versions install over the old one.
