#!/usr/bin/env python3
"""Generează iconița și bannerul / Generates the icon and the TV banner.
Pas Volum / Volume Step — Hunys with Claude AI — MIT"""
import os, sys
from PIL import Image, ImageDraw, ImageFont

RES = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/res"
BG = (16, 19, 26); CARD = (30, 35, 46); AMBER = (255, 179, 0); WHITE = (236, 239, 244); MUTED = (150, 160, 178)
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"

def speaker(d, x, y, s, color):
    # corp difuzor + con / speaker body + cone, s = height
    w = s * 0.32
    d.rectangle([x, y + s * 0.32, x + w, y + s * 0.68], fill=color)
    d.polygon([(x + w, y + s * 0.32), (x + w + s * 0.36, y), (x + w + s * 0.36, y + s), (x + w, y + s * 0.68)], fill=color)
    # unde / waves
    cx, cy = x + w + s * 0.42, y + s / 2
    for i, r in enumerate((s * 0.24, s * 0.42)):
        d.arc([cx - r, cy - r, cx + r, cy + r], start=-50, end=50, fill=color, width=max(2, int(s * 0.07)))

def icon(size):
    sc = 4
    im = Image.new("RGBA", (size * sc, size * sc), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    S = size * sc
    d.rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.22), fill=BG)
    speaker(d, S * 0.14, S * 0.14, S * 0.40, AMBER)
    f = ImageFont.truetype(BOLD, int(S * 0.30))
    d.text((S * 0.86, S * 0.84), "±2", font=f, fill=WHITE, anchor="rd")
    return im.resize((size, size), Image.LANCZOS)

def banner():
    sc = 4; W, H = 320 * sc, 180 * sc
    im = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([int(W * 0.04), int(H * 0.08), int(W * 0.96), int(H * 0.92)], radius=int(H * 0.12), fill=CARD)
    speaker(d, W * 0.08, H * 0.32, H * 0.36, AMBER)
    d.text((W * 0.33, H * 0.30), "Pas Volum", font=ImageFont.truetype(BOLD, int(H * 0.18)), fill=WHITE)
    d.text((W * 0.33, H * 0.54), "Volume Step  ±2", font=ImageFont.truetype(REG, int(H * 0.11)), fill=MUTED)
    return im.resize((320, 180), Image.LANCZOS)

os.makedirs(os.path.join(RES, "drawable-xxxhdpi"), exist_ok=True)
os.makedirs(os.path.join(RES, "drawable-xhdpi"), exist_ok=True)
icon(192).save(os.path.join(RES, "drawable-xxxhdpi", "ic_launcher.png"), optimize=True)
icon(96).save(os.path.join(RES, "drawable-xhdpi", "ic_launcher.png"), optimize=True)
banner().save(os.path.join(RES, "drawable-xhdpi", "banner.png"), optimize=True)
print("ok")
