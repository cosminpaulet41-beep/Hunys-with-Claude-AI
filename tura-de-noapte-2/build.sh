#!/usr/bin/env bash
# Tura de Noapte 2 — script de compilare a APK-ului / APK build script
# Autor / Author: Hunys with Claude AI — Licență / License: MIT
#
# Nu are nevoie de Android Studio sau Gradle. Are nevoie de:
# Needs no Android Studio or Gradle. It needs:
#   - JDK (javac, keytool)                - aapt2 (ex. din Apktool / e.g. from Apktool)
#   - android.jar (API 30 sau mai nou)    - dx.jar (ex. din dex2jar / e.g. from dex2jar)
#   - un semnător / a signer: SIGNER_JAR = uber-apk-signer (v1+v2+v3 și zipalign)
#                          sau APKSIGNER_JAR = apksigner (din build-tools / from build-tools)
#
# Resursele din assets/ se refac cu ./gen_assets.sh (sunt deja în depozit).
# The resources in assets/ are rebuilt with ./gen_assets.sh (they are already in the repository).
#
# Folosire / Usage:
#   AAPT2=... ANDROID_JAR=... DX_JAR=... SIGNER_JAR=... ./build.sh
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
APP="$HERE/android/app/src/main"
OUT="$HERE/build/apk"
KEYSTORE="$HERE/keystore/tura2.keystore"
KS_PASS="${KS_PASS:-tura2noapte}"
KS_ALIAS="${KS_ALIAS:-tura2}"
VERSION_CODE=1
VERSION_NAME=1.0

: "${AAPT2:?setează AAPT2 / set AAPT2}"
: "${ANDROID_JAR:?setează ANDROID_JAR / set ANDROID_JAR}"
: "${DX_JAR:?setează DX_JAR / set DX_JAR}"
if [ -z "${SIGNER_JAR:-}" ] && [ -z "${APKSIGNER_JAR:-}" ]; then
    echo "setează SIGNER_JAR sau APKSIGNER_JAR / set SIGNER_JAR or APKSIGNER_JAR" >&2
    exit 1
fi

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex" "$OUT/signed"

echo "== 1. Resurse / Resources (aapt2)"
"$AAPT2" compile --dir "$APP/res" -o "$OUT/res.zip"
# sunetele rămân necomprimate, ca să le poată citi SoundPool direct din APK
# sounds stay uncompressed so SoundPool can read them straight from the APK
"$AAPT2" link -o "$OUT/unsigned.apk" \
    -I "$ANDROID_JAR" \
    --manifest "$APP/AndroidManifest.xml" \
    --min-sdk-version 21 --target-sdk-version 34 \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
    -A "$HERE/assets" -0 wav \
    --java "$OUT/gen" \
    "$OUT/res.zip"

echo "== 2. Java (javac)"
find "$HERE/src" "$APP/java" "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 -nowarn -Xlint:-options \
    -bootclasspath "$ANDROID_JAR" -classpath "$ANDROID_JAR" \
    -d "$OUT/classes" @"$OUT/sources.txt"

echo "== 3. Dex (dx)"
java -cp "$DX_JAR" com.android.dx.command.Main --dex --min-sdk-version=21 \
    --output="$OUT/dex/classes.dex" "$OUT/classes"
(cd "$OUT/dex" && zip -q -X "$OUT/unsigned.apk" classes.dex)
# aliniere la 4 octeți a fișierelor necomprimate / 4-byte alignment of the uncompressed files
python3 -I "$HERE/tools/zipalign.py" "$OUT/unsigned.apk" "$OUT/aligned.apk"
mv "$OUT/aligned.apk" "$OUT/unsigned.apk"

echo "== 4. Semnare / Signing"
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$(dirname "$KEYSTORE")"
    keytool -genkeypair -keystore "$KEYSTORE" -storetype PKCS12 -alias "$KS_ALIAS" \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass "$KS_PASS" -keypass "$KS_PASS" \
        -dname "CN=Hunys with Claude AI, O=Hunys, C=RO"
fi
FINAL="$OUT/TuraDeNoapte2-v$VERSION_NAME.apk"
if [ -n "${SIGNER_JAR:-}" ]; then
    java -jar "$SIGNER_JAR" -a "$OUT/unsigned.apk" -o "$OUT/signed" \
        --ks "$KEYSTORE" --ksAlias "$KS_ALIAS" --ksPass "$KS_PASS" --ksKeyPass "$KS_PASS" \
        --allowResign
    cp "$(ls "$OUT"/signed/*.apk | head -n1)" "$FINAL"
else
    java -jar "$APKSIGNER_JAR" sign --ks "$KEYSTORE" --ks-key-alias "$KS_ALIAS" \
        --ks-pass "pass:$KS_PASS" --key-pass "pass:$KS_PASS" \
        --out "$FINAL" "$OUT/unsigned.apk"
fi
echo "== Gata / Done: $FINAL"
