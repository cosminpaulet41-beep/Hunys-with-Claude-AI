# Pas Volum / Volume Step

**Autor / Author:** Hunys with Claude AI · **Licență / License:** MIT (open source) · **v1.2**

---

## 🇷🇴 Română

O aplicație mică pentru proiectoare și televizoare Android (făcută pentru un proiector Android 14, X1BR, 1 GB RAM) care face ca fiecare apăsare a tastelor **+ / −** de volum de pe telecomandă să mute volumul cu **mai multe trepte deodată**, de exemplu **câte 2** (1 → 3 → 5 …) în loc de câte 1.

### Cum funcționează (v1.2)
- Un **serviciu din fundal** verifică volumul media de ~33 de ori pe secundă. Când se schimbă cu exact 1 (o apăsare), adaugă imediat restul treptelor.
- v1.2: nu mai așteaptă mesajul de sistem `VOLUME_CHANGED_ACTION`, pe care Android 14 îl livrează cu ~500 ms întârziere aplicațiilor din fundal. Verificarea se oprește cât timp proiectorul e în standby.
- **Nu folosește accesibilitatea.** Versiunea 1.0 folosea un serviciu de accesibilitate, dar firmware-ul acestui proiector (`enableNfxAccessibility` din WindowManager) dezactivează accesibilitatea la fiecare schimbare de aplicație.
- Pornește singur după repornirea proiectorului (`BOOT_COMPLETED`).
- Pasul se alege din aplicație (1–20, implicit 2). Funcția se poate opri din aplicație.
- Interfață pentru telecomandă (D-pad), în **română și engleză**.

### Instalare (Termux + ADB)
```
adb install -r PasVolum-v1.2.apk
```
Apoi deschizi aplicația o dată pe proiector, ca să pornească serviciul.

### Compilare
Vezi `build.sh` (fără Android Studio/Gradle; folosește aapt2, javac, dx, uber-apk-signer și android.jar API 34).
Cheia de semnare (`keystore/pasvolum.keystore`, parola `pasvolum`) e inclusă, ca versiunile noi să se instaleze peste cea veche.

---

## 🇬🇧 English

A small app for Android projectors and TVs (built for an Android 14 projector, X1BR, 1 GB RAM) that makes every press of the remote's **+ / −** volume keys move the volume **several steps at once**, e.g. **2 at a time** (1 → 3 → 5 …) instead of 1.

### How it works (v1.2)
- A **background service** checks the media volume ~33 times per second. When it changes by exactly 1 (one press), it instantly adds the remaining steps.
- v1.2: no longer waits for the `VOLUME_CHANGED_ACTION` broadcast, which Android 14 delivers ~500 ms late to background apps. Polling pauses while the projector is in standby.
- **No accessibility service.** v1.0 used one, but this projector's firmware (`enableNfxAccessibility` in WindowManager) turns accessibility off on every app switch.
- Starts automatically after the projector boots (`BOOT_COMPLETED`).
- The step is set in the app (1–20, default 2). The feature can be turned off in the app.
- Remote-friendly (D-pad) UI in **Romanian and English**.

### Install (Termux + ADB)
```
adb install -r PasVolum-v1.2.apk
```
Then open the app once on the projector so the service starts.

### Build
See `build.sh` (no Android Studio/Gradle; uses aapt2, javac, dx, uber-apk-signer and an API 34 android.jar).
The signing key (`keystore/pasvolum.keystore`, password `pasvolum`) is included so new versions install over the old one.
