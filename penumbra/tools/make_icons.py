#!/usr/bin/env python3
"""Generează iconițele / Generates the icons.
Penumbra — Hunys with Claude AI — MIT

Folosire / Usage: python3 tools/make_icons.py [app/src/main/res]
"""
import os, sys
from PIL import Image, ImageDraw, ImageFilter, ImageChops, ImageFont

RES = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/res"
BG = (11, 13, 19)
CARD = (24, 28, 38)
AMBER = (255, 168, 54)
AMBER_HI = (255, 214, 140)
WHITE = (236, 239, 244)
MUTED = (148, 158, 176)
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"


def disc_mask(S, cx, cy, r):
    m = Image.new("L", (S, S), 0)
    ImageDraw.Draw(m).ellipse([cx - r, cy - r, cx + r, cy + r], fill=255)
    return m


def penumbra(S, cx, cy, r):
    """Un soare chihlimbariu acoperit pe jumătate de o umbră cu margine moale.
    An amber sun half covered by a soft-edged shadow."""
    out = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    sun = disc_mask(S, cx, cy, r)
    # Gradient cald pe soare / Warm gradient on the sun
    grad = Image.new("RGBA", (S, S))
    gd = ImageDraw.Draw(grad)
    for y in range(S):
        t = max(0.0, min(1.0, (y - (cy - r)) / (2 * r)))
        c = tuple(int(AMBER_HI[i] * (1 - t) + AMBER[i] * t) for i in range(3))
        gd.line([(0, y), (S, y)], fill=c + (255,))
    out.paste(grad, (0, 0), sun)
    # Umbra: alt disc, deplasat, cu margine moale = penumbra / The shadow: another disc, offset, soft edge = penumbra
    shadow = disc_mask(S, cx + r * 0.62, cy - r * 0.38, r * 1.02).filter(ImageFilter.GaussianBlur(r * 0.16))
    shadow = ImageChops.multiply(shadow, sun)
    dark = Image.new("RGBA", (S, S), BG + (255,))
    out.paste(dark, (0, 0), Image.eval(shadow, lambda v: int(v * 0.93)))
    # Inel subțire / Thin ring
    ImageDraw.Draw(out).ellipse([cx - r, cy - r, cx + r, cy + r], outline=AMBER + (150,), width=max(1, int(r * 0.035)))
    return out


def legacy_icon(size):
    sc = 4
    S = size * sc
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(im).rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.24), fill=BG)
    im.alpha_composite(penumbra(S, S / 2, S / 2, S * 0.31))
    return im.resize((size, size), Image.LANCZOS)


def adaptive_fg(size=432):
    # 108dp, zona sigură e cercul de 66dp din mijloc / 108dp, the safe zone is the middle 66dp circle
    sc = 2
    S = size * sc
    return penumbra(S, S / 2, S / 2, S * 0.26).resize((size, size), Image.LANCZOS)


def crescent(size):
    """Semilună albă pentru panoul rapid și notificare / White crescent for the tile and the notification."""
    sc = 8
    S = size * sc
    r = S * 0.40
    cx = cy = S / 2
    m = disc_mask(S, cx, cy, r)
    cut = disc_mask(S, cx + r * 0.62, cy - r * 0.38, r * 0.98)
    m = ImageChops.subtract(m, cut)
    im = Image.new("RGBA", (S, S), (255, 255, 255, 0))
    im.putalpha(m)
    return im.resize((size, size), Image.LANCZOS)


def banner():
    sc = 4
    W, H = 320 * sc, 180 * sc
    im = Image.new("RGBA", (W, H), BG + (255,))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([int(W * 0.04), int(H * 0.08), int(W * 0.96), int(H * 0.92)], radius=int(H * 0.12), fill=CARD)
    sun = penumbra(max(W, H), W * 0.2, H / 2, H * 0.24).crop((0, 0, W, H))
    im.alpha_composite(sun)
    d = ImageDraw.Draw(im)
    d.text((W * 0.36, H * 0.26), "Penumbra", font=ImageFont.truetype(BOLD, int(H * 0.17)), fill=WHITE)
    d.text((W * 0.36, H * 0.50), "Estompare ecran", font=ImageFont.truetype(REG, int(H * 0.085)), fill=MUTED)
    d.text((W * 0.36, H * 0.62), "Screen dimmer", font=ImageFont.truetype(REG, int(H * 0.085)), fill=MUTED)
    return im.convert("RGB").resize((320, 180), Image.LANCZOS)


def save(img, *path):
    p = os.path.join(RES, *path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p, optimize=True)


save(legacy_icon(96), "mipmap-xhdpi", "ic_launcher.png")
save(legacy_icon(144), "mipmap-xxhdpi", "ic_launcher.png")
save(legacy_icon(192), "mipmap-xxxhdpi", "ic_launcher.png")
save(adaptive_fg(), "drawable-xxxhdpi", "ic_fg.png")
for folder, px in (("drawable-xhdpi", 48), ("drawable-xxhdpi", 72), ("drawable-xxxhdpi", 96)):
    save(crescent(px), folder, "ic_tile.png")
    save(crescent(px), folder, "ic_notif.png")
save(banner(), "drawable-xhdpi", "banner.png")
print("ok")
