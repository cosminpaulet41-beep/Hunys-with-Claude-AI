# Semafor

**Autor / Author:** Hunys with Claude AI · **Licență / License:** MIT (open source) · **v1.0**

<img src="icon.png" width="96" alt="Semafor">

---

## 🇷🇴 Română

Semafor te pune să rezolvi o întrebare pentru permisul auto (categoria B) **de fiecare dată când deblochezi telefonul**. La deblocare îți deschide aplicația **Școala Rutieră** exact unde ai rămas în mediul de învățare. Răspunzi la o întrebare, apoi apeși Acasă. Dacă greșești, răspunsul corect ți-l arată chiar Școala Rutieră, ca de obicei.

Semafor nu conține întrebările. Le ia din aplicația pe care o folosești deja, așa că progresul tău rămâne acolo.

### Ce face
- La fiecare deblocare deschide aplicația aleasă (o găsește singur pe Școala Rutieră, sau o alegi tu din listă).
- **Cât de des:** la fiecare deblocare, la a 2-a, la a 3-a, sau cel mult o dată la 10 / 30 / 60 de minute.
- **Liniște noaptea** (opțional): nimic între 23:00 și 07:00.
- Nu se deschide în timpul unui apel.
- **Notificare cu butoane:** Pauză 1 h / Reia și Oprește.
- **Progres:** câte întrebări ai deschis azi și în total, și ora ultimei deschideri.
- Pornește singur după repornirea telefonului.
- Interfață în **română și engleză**. Fără reclame, fără internet.

### Instalare
1. Descarcă `Semafor-v1.0.apk` de la [Releases](https://github.com/cosminpaulet41-beep/Hunys-with-Claude-AI/releases) și deschide-l. Dacă telefonul îți cere, permite instalarea din surse necunoscute.
2. Deschide Semafor și apasă **OPRIT — atinge ca să pornești**. Telefonul te duce la „Afișare peste alte aplicații”: pornește permisiunea pentru Semafor și revino. Fără ea, Android nu lasă o aplicație să deschidă alta din fundal, și primești doar o notificare pe care o atingi.
3. Verifică la „Aplicația cu întrebări” că scrie **Școala Rutieră**. Dacă nu, apasă „Alege aplicația”.
4. Pe **Huawei** (EMUI): Setări › Baterie › Lansare aplicații › Semafor › gestionare manuală, cu toate cele trei opțiuni pornite. Altfel sistemul îl poate opri în fundal. Butonul „Lansare aplicații” din Semafor te duce acolo.
5. Blochează telefonul și deblochează-l. Ar trebui să se deschidă Școala Rutieră.

### Compilare
Vezi `build.sh`. Nu are nevoie de Android Studio sau Gradle. Folosește aapt2, javac, dx, uber-apk-signer și android.jar API 34. Iconițele se fac cu `tools/make_icons.py` (Python + Pillow).
Cheia de semnare (`keystore/semafor.keystore`, parola `semafor`) e inclusă, ca versiunile noi să se instaleze peste cea veche.

---

## 🇬🇧 English

Semafor makes you answer one driving-licence question (category B, Romania) **every time you unlock your phone**. On unlock it opens the **Școala Rutieră** app exactly where you left off in the learning environment. Answer one question, then press Home. If you get it wrong, Școala Rutieră itself shows the correct answer, as usual.

Semafor doesn't contain the questions. It uses the app you already study with, so your progress stays there.

### What it does
- Opens the chosen app on every unlock (it finds Școala Rutieră by itself, or you pick the app from a list).
- **How often:** every unlock, every 2nd, every 3rd, or at most once every 10 / 30 / 60 minutes.
- **Quiet at night** (optional): nothing between 23:00 and 07:00.
- Doesn't open during a call.
- **Notification with buttons:** Pause 1 h / Resume and Turn off.
- **Progress:** how many questions you opened today and in total, and when the last one opened.
- Starts by itself after a reboot.
- Interface in **Romanian and English**. No ads, no internet.

### Install
1. Download `Semafor-v1.0.apk` from [Releases](https://github.com/cosminpaulet41-beep/Hunys-with-Claude-AI/releases) and open it. If the phone asks, allow installs from unknown sources.
2. Open Semafor and tap **OFF — tap to turn on**. The phone takes you to "Display over other apps": turn it on for Semafor and come back. Without it, Android won't let an app open another one from the background, and you only get a notification to tap.
3. Check that "Quiz app" says **Școala Rutieră**. If not, tap "Choose the app".
4. On **Huawei** (EMUI): Settings › Battery › App launch › Semafor › manage manually, with all three options on. Otherwise the system may stop it in the background. The "App launch" button in Semafor takes you there.
5. Lock the phone and unlock it. Școala Rutieră should open.

### Building
See `build.sh`. No Android Studio or Gradle needed. It uses aapt2, javac, dx, uber-apk-signer and android.jar API 34. The icons are made with `tools/make_icons.py` (Python + Pillow).
The signing key (`keystore/semafor.keystore`, password `semafor`) is included so new versions install over the old one.
