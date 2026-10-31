#!/bin/sh
# Generează resursele jocului (texturi, personaje, decor, hartă, fonturi) în assets/.
set -e
cd "$(dirname "$0")"
export JAVA_TOOL_OPTIONS=
python3 -I tools/textures.py assets > /dev/null
python3 -I tools/sprites.py assets > /dev/null
python3 -I tools/mapgen.py assets > /dev/null
# mâinile cu armele sunt randate 3D (durează câteva minute); se refac doar dacă lipsesc
[ -f assets/hand_pistol.png ] || python3 -I tools/render_gun.py assets 400 330 pistol > /dev/null
[ -f assets/hand_shotgun.png ] || python3 -I tools/render_gun.py assets 400 300 shotgun,smg > /dev/null
python3 -I tools/make_icon.py assets android/app/src/main/res > /dev/null
mkdir -p build/tools
javac -d build/tools tools/FontGen.java
java -Djava.awt.headless=true -cp build/tools FontGen fonts/Jersey10-Regular.ttf 18.666667 assets/big.pxf > /dev/null
java -Djava.awt.headless=true -cp build/tools FontGen fonts/Tiny5.ttf 8 assets/small.pxf > /dev/null
echo "resurse gata"
