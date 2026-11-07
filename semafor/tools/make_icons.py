#!/usr/bin/env python3
"""Generează iconițele / Generates the icons.
Semafor — Hunys with Claude AI — MIT

Folosire / Usage: python3 tools/make_icons.py [app/src/main/res]
"""
import os, sys
from PIL import Image, ImageDraw, ImageFilter, ImageFont

RES = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/res"
BG = (12, 14, 20)
HOUSING = (40, 46, 60)
RED = (239, 83, 80)
AMBER = (255, 183, 40)
GREEN = (76, 200, 120)
DARK = (12, 14, 20)
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"


def semafor(S, cx, cy, h):
    """Un semafor cu trei lumini; pe cea verde, un semn de întrebare.
    A traffic light with three lights; a question mark on the green one."""
    out = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    w = h * 0.44
    r = w * 0.34
    x0, y0, x1, y1 = cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2
    centers = [cy - h * 0.30, cy, cy + h * 0.30]
    colors = [RED, AMBER, GREEN]

    # Strălucire în jurul luminii verzi / Glow around the green light
    glow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    gx, gy = cx, centers[2]
    ImageDraw.Draw(glow).ellipse([gx - r * 1.9, gy - r * 1.9, gx + r * 1.9, gy + r * 1.9], fill=GREEN + (120,))
    out.alpha_composite(glow.filter(ImageFilter.GaussianBlur(r * 0.55)))

    d = ImageDraw.Draw(out)
    d.rounded_rectangle([x0, y0, x1, y1], radius=w * 0.42, fill=HOUSING)
    for i, (c, y) in enumerate(zip(colors, centers)):
        # Roșu și galben stinse puțin, verdele aprins / Red and amber slightly dim, green lit
        col = c if i == 2 else tuple(int(v * 0.62) for v in c)
        d.ellipse([cx - r, y - r, cx + r, y + r], fill=col)
    f = ImageFont.truetype(BOLD, int(r * 1.45))
    d.text((cx, centers[2] + r * 0.04), "?", font=f, fill=DARK, anchor="mm")
    return out


def legacy_icon(size):
    sc = 4
    S = size * sc
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(im).rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.24), fill=BG)
    im.alpha_composite(semafor(S, S / 2, S / 2, S * 0.74))
    return im.resize((size, size), Image.LANCZOS)


def adaptive_fg(size=432):
    # 108dp; zona sigură e cercul de 66dp din mijloc / 108dp; the safe zone is the middle 66dp circle
    sc = 2
    S = size * sc
    return semafor(S, S / 2, S / 2, S * 0.56).resize((size, size), Image.LANCZOS)


def notif(size):
    """Siluetă albă: carcasa cu trei găuri / White silhouette: the housing with three holes."""
    sc = 8
    S = size * sc
    h = S * 0.92
    w = h * 0.46
    r = w * 0.30
    cx = cy = S / 2
    m = Image.new("L", (S, S), 0)
    d = ImageDraw.Draw(m)
    d.rounded_rectangle([cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2], radius=w * 0.42, fill=255)
    for y in (cy - h * 0.30, cy, cy + h * 0.30):
        d.ellipse([cx - r, y - r, cx + r, y + r], fill=0)
    im = Image.new("RGBA", (S, S), (255, 255, 255, 0))
    im.putalpha(m)
    return im.resize((size, size), Image.LANCZOS)


def save(img, *parts):
    path = os.path.join(RES, *parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)


for folder, px in (("mipmap-mdpi", 48), ("mipmap-hdpi", 72), ("mipmap-xhdpi", 96), ("mipmap-xxhdpi", 144), ("mipmap-xxxhdpi", 192)):
    save(legacy_icon(px), folder, "ic_launcher.png")
save(adaptive_fg(), "drawable-xxxhdpi", "ic_fg.png")
for folder, px in (("drawable-mdpi", 24), ("drawable-hdpi", 36), ("drawable-xhdpi", 48), ("drawable-xxhdpi", 72), ("drawable-xxxhdpi", 96)):
    save(notif(px), folder, "ic_notif.png")
# Pentru README / For the README
save(legacy_icon(512), "..", "..", "..", "..", "icon.png")
print("ok")
