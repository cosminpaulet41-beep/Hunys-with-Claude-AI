#!/usr/bin/env bash
# Penumbra — script de compilare / build script
# Autor / Author: Hunys with Claude AI — Licență / License: MIT
#
# Nu are nevoie de Android Studio sau Gradle. Are nevoie de:
# Needs no Android Studio or Gradle. It needs:
#   - JDK (javac, jar, keytool)          - aapt2 (ex. din Apktool / e.g. from Apktool)
#   - android.jar (API 34)              - dx.jar (ex. din dex2jar / e.g. from dex2jar)
#   - uber-apk-signer.jar (semnare v1+v2+v3 și zipalign / signing v1+v2+v3 and zipalign)
#
# Folosire / Usage:
#   AAPT2=... ANDROID_JAR=... DX_JAR=... SIGNER_JAR=... ./build.sh
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
APP="$HERE/app/src/main"
OUT="$HERE/build"
KEYSTORE="$HERE/keystore/penumbra.keystore"
KS_PASS="${KS_PASS:-penumbra}"
KS_ALIAS="${KS_ALIAS:-penumbra}"
VERSION_CODE=1
VERSION_NAME=1.0

: "${AAPT2:?setează AAPT2 / set AAPT2}"
: "${ANDROID_JAR:?setează ANDROID_JAR / set ANDROID_JAR}"
: "${DX_JAR:?setează DX_JAR / set DX_JAR}"
: "${SIGNER_JAR:?setează SIGNER_JAR / set SIGNER_JAR}"

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex" "$OUT/signed"

echo "== 1. Resurse / Resources (aapt2)"
"$AAPT2" compile --dir "$APP/res" -o "$OUT/res.zip"
"$AAPT2" link -o "$OUT/unsigned.apk" \
    -I "$ANDROID_JAR" \
    --manifest "$APP/AndroidManifest.xml" \
    --min-sdk-version 21 --target-sdk-version 34 \
    --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
    --java "$OUT/gen" \
    "$OUT/res.zip"

echo "== 2. Java (javac)"
find "$APP/java" "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -Xlint:all -Xlint:-options -Xlint:-deprecation -encoding UTF-8 -source 8 -target 8 \
    -bootclasspath "$ANDROID_JAR" -classpath "$ANDROID_JAR" \
    -d "$OUT/classes" @"$OUT/sources.txt"

echo "== 3. Dex (dx)"
java -cp "$DX_JAR" com.android.dx.command.Main --dex --min-sdk-version=21 \
    --output="$OUT/dex/classes.dex" "$OUT/classes"
(cd "$OUT/dex" && zip -q -X "$OUT/unsigned.apk" classes.dex)

echo "== 4. Semnare / Signing"
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$(dirname "$KEYSTORE")"
    keytool -genkeypair -keystore "$KEYSTORE" -storetype PKCS12 -alias "$KS_ALIAS" \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass "$KS_PASS" -keypass "$KS_PASS" \
        -dname "CN=Hunys with Claude AI, O=Hunys, C=RO"
fi
java -jar "$SIGNER_JAR" -a "$OUT/unsigned.apk" -o "$OUT/signed" \
    --ks "$KEYSTORE" --ksAlias "$KS_ALIAS" --ksPass "$KS_PASS" --ksKeyPass "$KS_PASS" \
    --allowResign
SIGNED="$(ls "$OUT"/signed/*.apk | head -n1)"
cp "$SIGNED" "$OUT/Penumbra-v$VERSION_NAME.apk"
echo "== Gata / Done: $OUT/Penumbra-v$VERSION_NAME.apk"
