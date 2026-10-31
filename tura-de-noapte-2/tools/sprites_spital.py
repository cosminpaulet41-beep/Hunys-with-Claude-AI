"""Sprite-urile Spitalului Universitar (Tura de Noapte 2): personajele, bossii, buteliile de oxigen
și decorul (paturi, tărgi, perfuzii, ambulanța, elicopterul...). Folosește uneltele din sprites.py.

Ieșire (în directorul dat):
  z.png      rânduri de 48x64: paznic, Vectra, Ioana, pacient, asistentă, medic, chirurg, dr. Elena,
             pilotul Cristi, scuipătorul; cadre: mers A, mers B, căzut, (scuipă)
  boss.png   Subiectul 9 (64x88): mers A, mers B, căzut
  boss3.png  creatura din cutia cu probe (96x80): mers A, mers B, căzută, urlă
  barrel.png butelia de oxigen (24x40): întreagă, spartă      expl.png explozia
  decor.png + decor.txt   obiectele, împachetate pe rânduri: idx nume w h înălțime solid rază x y
"""
import math
import os
import random

from PIL import Image

import sprites as sp
from sprites import Spr, mul, BLOOD, BLOOD2

R = random.Random(9)

# ---------------------------------------------------------------- personaje
ORDER = ["paznic", "vectra", "ioana", "pacient", "asistenta", "medic", "chirurg", "elena", "cristi"]


def characters():
    types = {t["name"]: t for t in sp.TYPES}
    zs = []
    for n in ORDER:
        t = types[n]
        if n in sp.ALIVE:
            zs += [sp.humanoid(t, 0, True), sp.humanoid(t, 1, True), None, None]
        else:
            zs += [sp.humanoid(t, 0), sp.humanoid(t, 1), sp.humanoid_dead(t), None]
    zs += [sp.spitter(0), sp.spitter(1), sp.spitter_dead(), sp.spitter(2)]
    return zs


def subject9(frame):
    """Subiectul 9: uriașul din E72, acum cu brățara de pacient, branula și curelele rupte."""
    s = sp.boss(frame)
    sw = 0 if frame == 0 else 1
    # brățara albă de la încheietură
    s.rect(4 + sw, 60, 9, 2, (236, 236, 230))
    s.rect(7 + sw, 60, 3, 2, (60, 90, 170))
    # cureaua de imobilizare ruptă, peste piept
    s.poly([(10 + sw, 44), (54 + sw, 36), (54 + sw, 39), (10 + sw, 47)], (140, 110, 70), shade=0)
    s.rect(30 + sw, 39, 5, 5, (190, 190, 196))
    # branula și tubul de perfuzie atârnând
    s.rect(52 + sw, 50, 3, 2, (220, 220, 230))
    for k in range(10):
        s.set(54 + sw + (k % 2), 52 + k, (210, 220, 230))
    # numărul 9 scris cu marker pe umăr
    for (x, y) in [(17, 24), (18, 24), (19, 24), (17, 25), (19, 25), (17, 26), (18, 26), (19, 26), (19, 27), (19, 28), (18, 28)]:
        s.set(x + sw, y, (30, 40, 90))
    return s


def creature(frame):
    """Creatura din cutia cu probe: corp de carne vie pe membre lungi, ca de păianjen,
    cap alungit cu falca despicată vertical și mulți ochi mici care strălucesc."""
    s = Spr(96, 80)
    fl = (182, 100, 98)
    fl2 = (132, 58, 66)
    dark = (70, 24, 32)
    bone = (228, 218, 198)
    glow = (220, 255, 120)
    roar = frame == 3
    k = 0 if frame in (0, 3) else 1

    def limb(pts, w0, w1, col):
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            n = int(max(abs(x1 - x0), abs(y1 - y0))) + 1
            for i in range(n + 1):
                t = i / n
                x, y = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
                w = w0 + (w1 - w0) * ((pts.index((x0, y0)) + t) / (len(pts) - 1))
                s.ellipse(x, y, w / 2, w / 2, col, shade=1)

    # picioarele din spate, scurte, între brațe
    for side in (-1, 1):
        lift = 2 if (side == 1) == (k == 0) else 0
        limb([(48 + side * 10, 36), (48 + side * 20, 52 - lift), (48 + side * 18, 77 - lift)], 7, 4, fl2)
        for c in (-2, 0, 2):
            s.rect(48 + side * 18 + c, 77 - lift, 1, 3, bone)
    # brațele din față: cot sus, gheare departe, în lateral
    for side in (-1, 1):
        up = 2 if (side == 1) == (k == 0) else -1
        ex = 48 + side * 36
        cx2 = 48 + side * (44 if (side == 1) == (k == 0) else 41)
        limb([(48 + side * 16, 26), (ex, 8 + up), (cx2, 75)], 9, 4, fl)
        for c in (-3, -1, 1, 3):
            s.poly([(cx2 + c - 1, 74), (cx2 + c + 1, 74), (cx2 + c + side, 80)], bone)
    # corpul cocoșat, cu țepi și tentacule
    s.ellipse(48, 26, 25, 15, fl, shade=6, hi=2)
    s.ellipse(48, 33, 19, 9, mul(fl, 0.9), shade=3)
    for (x, y) in [(36, 22), (46, 18), (57, 21), (62, 29), (33, 31), (52, 29)]:
        s.ellipse(x, y, 3, 2.2, mul(fl, 1.12))
        s.set(x, y, mul(fl, 0.72))
    for i, x in enumerate(range(31, 68, 5)):
        h = 6 + (i * 5) % 6
        y0 = 13 + abs(48 - x) // 3
        s.poly([(x - 2, y0), (x + 2, y0), (x, y0 - h)], bone)
    for i, base in enumerate((36, 60)):
        for j in range(12):
            a = j * 0.5 + k * 1.3 + i * 2
            x = base + int(round(math.sin(a) * 3)) + (j if i else -j) // 2
            s.rect(x, 12 - j, 2, 2, fl2 if j % 3 else mul(fl, 1.1))
    # gâtul și capul alungit, atârnând în față
    s.poly([(42, 34), (54, 34), (53, 46), (43, 46)], fl2, shade=2)
    s.ellipse(48, 56, 13, 15, dark)
    s.ellipse(48, 56, 12, 14, fl, shade=3, hi=1)
    gap = 6 if roar else 2
    s.poly([(48 - gap, 50), (48 + gap, 50), (48 + gap + 1, 70), (48 - gap - 1, 70)], (40, 6, 10))
    for j in range(5):
        for side in (-1, 1):
            x = 48 + side * gap
            s.poly([(x, 51 + j * 4), (x, 53 + j * 4), (x - side * 3, 52 + j * 4)], bone)
    if roar:
        s.poly([(46, 56), (50, 56), (49, 68), (47, 68)], (160, 44, 64))
    for (x, y) in [(-8, 46), (-5, 44), (-9, 50), (8, 46), (5, 44), (9, 50), (-3, 47), (3, 47)]:
        s.rect(48 + x - (1 if x < 0 else 0), y, 2, 2, glow)
        s.set(48 + x, y, (255, 255, 230))
    s.rect(47, 70, 1, 5, (200, 220, 140))
    s.rect(50, 69, 1, 3, BLOOD)
    s.outline()
    return s


def creature_dead():
    s = Spr(96, 80)
    for y in range(64, 80):
        for x in range(96):
            dx = (x - 48) / 44.0
            dy = (y - 73) / 6.5
            if dx * dx + dy * dy <= 1:
                s.set(x, y, (70 + (x * 5 + y * 3) % 9, 12, 16))
    s.ellipse(48, 66, 30, 9, (150, 80, 82), shade=4, hi=2)
    for (x, w) in [(14, 18), (66, 20)]:
        s.poly([(x, 68), (x + w, 64), (x + w + 2, 68), (x + 2, 72)], (120, 56, 62), shade=1)
    for x in range(26, 70, 7):
        s.poly([(x - 2, 62), (x + 2, 62), (x + 1, 56)], (226, 216, 196))
    s.ellipse(36, 70, 7, 3, (200, 220, 140))
    s.outline()
    return s


def o2_cylinder(dead=False):
    """Butelia de oxigen medical: corp alb, ogivă albă, robinet, eticheta O2 și romb de pericol."""
    s = Spr(24, 40)
    if not dead:
        body = (226, 228, 224)
        s.poly([(5, 10), (19, 10), (19, 38), (5, 38)], body, shade=3, hi=2)
        s.ellipse(12, 10, 7, 4, body, shade=2, hi=1)
        s.rect(9, 3, 6, 5, (130, 134, 140))
        s.rect(7, 1, 10, 3, (90, 96, 100))
        s.rect(15, 4, 4, 2, (170, 140, 60))
        s.rect(5, 16, 14, 1, (40, 140, 70))
        s.rect(5, 30, 14, 1, (40, 140, 70))
        for (x, y) in [(8, 20), (9, 20), (10, 20), (8, 21), (10, 21), (8, 22), (9, 22), (10, 22),
                       (12, 20), (13, 20), (14, 20), (14, 21), (13, 21), (12, 22), (12, 23), (13, 23), (14, 23)]:
            s.set(x, y, (30, 30, 34))
        for j in range(4):
            s.rect(12 - j, 25 + j, 1 + 2 * j, 1, (230, 120, 30))
        for j in range(3):
            s.rect(10 + j, 29 - j, 5 - 2 * j, 1, (230, 120, 30))
        s.rect(4, 37, 16, 3, (70, 72, 76))
    else:
        for y in range(30, 40):
            for x in range(24):
                if ((x - 12) / 11.0) ** 2 + ((y - 36) / 3.5) ** 2 <= 1:
                    s.set(x, y, (40, 36, 34))
        s.poly([(2, 32), (16, 30), (18, 35), (4, 37)], (200, 200, 196), shade=1)
        s.poly([(16, 30), (22, 27), (21, 33), (18, 35)], (120, 120, 116))
        s.rect(4, 31, 3, 2, (60, 60, 62))
    s.outline()
    return s


# ---------------------------------------------------------------- decor de spital
CHROME = (176, 180, 186)
CHROME2 = (120, 124, 130)
SHEET = (226, 232, 236)


def wheels(s, xs, y, r=2.6):
    for x in xs:
        s.ellipse(x, y, r, r, (34, 34, 36))
        s.set(int(x), int(y), (120, 120, 120))


def d_bed(bloody=False):
    s = Spr(62, 34)
    s.rect(4, 22, 54, 3, CHROME2)                                   # cadrul
    for x in (8, 52):
        s.rect(x, 24, 2, 6, CHROME2)
    wheels(s, (9, 53), 31)
    s.poly([(2, 4), (8, 4), (8, 24), (2, 24)], (196, 200, 204), shade=1, hi=1)      # tăblia de la cap
    s.poly([(54, 10), (60, 10), (60, 24), (54, 24)], (196, 200, 204), shade=1, hi=1)  # tăblia de la picioare
    s.poly([(7, 14), (55, 14), (55, 22), (7, 22)], SHEET, shade=1, hi=0)            # saltea + cearșaf
    s.ellipse(14, 13, 6, 3, (244, 246, 248), shade=1)                               # perna
    s.rect(18, 17, 30, 1, (190, 200, 210))
    s.rect(20, 10, 26, 2, CHROME)                                                    # balustrada laterală
    for x in (22, 32, 42):
        s.rect(x, 10, 1, 5, CHROME)
    if bloody:
        s.poly([(16, 12), (50, 11), (54, 18), (14, 20)], (214, 218, 222), shade=1)
        for (x, y, r) in [(30, 16, 4), (38, 15, 3), (24, 18, 2)]:
            s.ellipse(x, y, r, r * 0.6, BLOOD)
        s.rect(46, 20, 2, 6, BLOOD2)
        s.rect(26, 21, 3, 8, (230, 236, 240))                                       # cearșaf atârnând
    s.outline()
    return s


def d_gurney():
    s = Spr(58, 28)
    s.rect(2, 14, 54, 2, CHROME)
    s.poly([(2, 8), (56, 8), (56, 14), (2, 14)], (54, 90, 120), shade=1, hi=1)        # salteaua subțire
    s.rect(2, 8, 54, 1, (90, 130, 160))
    s.rect(4, 6, 12, 3, (230, 234, 238))
    for x in (8, 48):
        s.rect(x, 16, 2, 7, CHROME2)
    s.rect(8, 19, 42, 1, CHROME2)
    wheels(s, (9, 49), 25)
    s.rect(30, 10, 2, 6, (60, 60, 64))                                              # curele
    s.outline()
    return s


def d_iv():
    s = Spr(16, 60)
    s.rect(7, 4, 2, 50, CHROME)
    s.rect(2, 4, 12, 1, CHROME)
    s.poly([(3, 6), (8, 6), (8, 16), (3, 16)], (210, 230, 240), shade=1)            # punga
    s.rect(4, 11, 3, 4, (190, 220, 236))
    for y in range(17, 34):
        s.set(5 + (y // 6) % 2, y, (200, 210, 220))
    s.rect(2, 54, 12, 2, CHROME2)
    wheels(s, (3, 13), 57, 2)
    s.outline()
    return s


def d_wheelchair():
    s = Spr(30, 32)
    s.ellipse(17, 22, 9, 9, (40, 40, 44))
    s.ellipse(17, 22, 7, 7, (120, 124, 130))
    s.ellipse(17, 22, 5.5, 5.5, (40, 40, 44))
    s.poly([(5, 2), (9, 2), (11, 18), (7, 18)], (40, 60, 110), shade=1)             # spătarul
    s.poly([(7, 15), (24, 15), (24, 19), (8, 19)], (40, 60, 110), shade=1)          # șezutul
    s.rect(5, 1, 6, 2, (60, 60, 64))
    s.rect(22, 19, 2, 9, CHROME2)
    s.rect(22, 27, 6, 2, CHROME2)
    s.ellipse(26, 30, 2, 2, (30, 30, 30))
    s.outline()
    return s


def d_med_cabinet():
    s = Spr(34, 58)
    s.poly([(1, 0), (33, 0), (33, 58), (1, 58)], (226, 228, 228), shade=2, hi=1)
    for k in range(2):
        x = 3 + k * 15
        s.rect(x, 3, 13, 30, (90, 116, 124))
        for y in (12, 22):
            s.rect(x, y, 13, 1, (190, 200, 204))
        for i, (bx, by) in enumerate([(1, 6), (5, 7), (9, 5), (2, 16), (7, 15), (3, 26), (8, 25)]):
            s.rect(x + bx, 3 + by, 3, 12 - by % 10 if by > 10 else 6, [(236, 236, 236), (150, 90, 50), (90, 160, 220), (230, 230, 220)][i % 4])
        s.rect(x + 1, 4, 1, 28, (170, 200, 210))
    s.rect(3, 36, 28, 9, (210, 212, 212))
    s.rect(3, 47, 28, 9, (210, 212, 212))
    s.rect(14, 40, 6, 1, (120, 120, 124))
    s.rect(14, 51, 6, 1, (120, 120, 124))
    s.outline()
    return s


def d_monitor_med():
    s = Spr(22, 56)
    s.poly([(1, 2), (21, 2), (21, 18), (1, 18)], (60, 64, 70), shade=1, hi=1)
    s.rect(3, 4, 16, 12, (6, 16, 10))
    pts = [0, 0, -1, -4, 2, 0, 0, 0, 0, 1, 0, 0, -3, 1, 0, 0]
    for i, v in enumerate(pts):
        s.set(3 + i, 9 + v, (70, 240, 120))
    s.rect(4, 13, 4, 2, (240, 90, 80))
    s.rect(12, 13, 5, 2, (90, 190, 240))
    s.rect(10, 18, 2, 32, CHROME)
    s.rect(3, 50, 16, 2, CHROME2)
    wheels(s, (4, 18), 53, 2)
    s.rect(12, 26, 5, 4, (220, 220, 214))
    s.outline()
    return s


def d_crash_cart():
    s = Spr(30, 40)
    s.poly([(2, 10), (28, 10), (28, 34), (2, 34)], (196, 40, 40), shade=2, hi=1)
    for y in (16, 22, 28):
        s.rect(2, y, 26, 1, (120, 20, 20))
        s.rect(11, y - 3, 8, 1, (230, 220, 210))
    s.poly([(6, 2), (24, 2), (24, 10), (6, 10)], (240, 200, 40), shade=1, hi=1)    # defibrilatorul
    s.rect(8, 4, 8, 4, (20, 30, 24))
    s.rect(9, 6, 6, 1, (60, 230, 110))
    s.rect(18, 4, 4, 4, (40, 40, 44))
    wheels(s, (5, 25), 36)
    s.outline()
    return s


def d_or_table():
    s = Spr(58, 30)
    s.poly([(2, 6), (56, 6), (56, 12), (2, 12)], (40, 50, 60), shade=1, hi=1)
    s.rect(2, 6, 54, 1, (80, 96, 110))
    s.rect(4, 3, 14, 3, (120, 160, 150))                                          # câmp chirurgical
    s.poly([(22, 12), (36, 12), (34, 24), (24, 24)], CHROME, shade=2, hi=1)
    s.poly([(14, 24), (44, 24), (46, 30), (12, 30)], CHROME2, shade=1)
    for (x, y) in [(30, 6), (34, 5), (40, 7)]:
        s.rect(x, y, 3, 2, BLOOD)
    s.outline()
    return s


def d_or_lamp():
    """Lampa scialitică, atârnată de tavan (se desenează de sus)."""
    s = Spr(44, 34)
    s.rect(21, 0, 2, 14, CHROME2)
    s.rect(16, 12, 12, 2, CHROME)
    s.ellipse(22, 22, 20, 8, (200, 204, 210), shade=2, hi=1)
    s.ellipse(22, 25, 16, 5, (250, 250, 236))
    for x in (12, 22, 32):
        s.ellipse(x, 25, 3, 2, (255, 255, 255))
    s.outline()
    return s


def d_ct():
    s = Spr(64, 52)
    s.ellipse(22, 22, 20, 21, (230, 232, 234), shade=3, hi=2)
    s.ellipse(22, 22, 11, 11, (40, 44, 50))
    for a in range(0, 360, 10):
        x = 22 + int(round(math.cos(math.radians(a)) * 12))
        y = 22 + int(round(math.sin(math.radians(a)) * 12))
        s.set(x, y, (150, 156, 164))
    s.rect(8, 6, 6, 3, (60, 130, 220))
    s.poly([(18, 30), (62, 28), (62, 34), (18, 36)], (220, 224, 228), shade=1, hi=1)  # masa glisantă
    s.poly([(36, 36), (56, 34), (56, 52), (38, 52)], (196, 200, 206), shade=2)
    s.rect(2, 42, 38, 10, (210, 214, 218))
    s.outline()
    return s


def d_waiting_chairs():
    s = Spr(62, 30)
    s.rect(2, 20, 58, 2, CHROME2)
    for x in (6, 54):
        s.rect(x, 22, 2, 8, CHROME2)
    for k in range(4):
        x = 2 + k * 15
        s.poly([(x + 1, 2), (x + 13, 2), (x + 13, 14), (x + 1, 14)], (50, 100, 170), shade=2, hi=1)
        s.poly([(x, 14), (x + 14, 14), (x + 14, 19), (x, 19)], (40, 86, 150), shade=1)
    s.rect(33, 12, 6, 3, (226, 222, 214))                                          # o revistă uitată
    s.outline()
    return s


def d_counter():
    s = Spr(64, 32)
    s.poly([(0, 6), (64, 6), (64, 32), (0, 32)], (210, 212, 214), shade=0)
    s.rect(0, 6, 64, 3, (120, 130, 140))
    s.rect(0, 9, 64, 1, (70, 76, 84))
    s.rect(0, 16, 64, 4, (40, 90, 170))
    s.rect(0, 16, 64, 1, (80, 130, 210))
    for x in range(0, 64, 16):
        s.rect(x, 10, 1, 22, (180, 182, 186))
    s.rect(0, 29, 64, 3, (120, 122, 126))
    s.rect(8, 0, 14, 7, (30, 30, 34))
    s.rect(9, 1, 12, 4, (60, 100, 150))
    s.rect(42, 3, 12, 3, (236, 232, 222))
    s.outline()
    return s


def d_lab_fridge():
    s = Spr(32, 58)
    s.poly([(1, 0), (31, 0), (31, 58), (1, 58)], (210, 214, 218), shade=2, hi=1)
    s.rect(3, 3, 26, 44, (120, 210, 220))
    for y in (13, 24, 35):
        s.rect(3, y, 26, 1, (200, 230, 236))
    for row, y in enumerate((5, 16, 27, 38)):
        for k in range(6):
            c = [(230, 70, 70), (240, 240, 240), (60, 160, 240), (250, 200, 50), (130, 220, 120)][(k + row) % 5]
            s.rect(5 + k * 4, y + 2, 2, 6, c)
    s.rect(3, 3, 1, 44, (230, 250, 255))
    s.rect(6, 49, 20, 6, (40, 44, 50))
    s.rect(8, 51, 8, 2, (60, 230, 120))
    s.outline()
    return s


def d_lab_bench():
    s = Spr(62, 36)
    s.poly([(0, 14), (62, 14), (62, 18), (0, 18)], (40, 44, 50), shade=0)
    s.rect(0, 14, 62, 1, (90, 96, 104))
    s.poly([(2, 18), (60, 18), (60, 36), (2, 36)], (214, 218, 222), shade=2, hi=1)
    for x in (20, 41):
        s.rect(x, 19, 1, 16, (170, 174, 180))
    s.rect(4, 2, 8, 12, (230, 232, 234))                                         # microscop
    s.rect(6, 0, 3, 6, (40, 40, 44))
    s.ellipse(30, 9, 7, 5, (220, 224, 228), shade=1)                              # centrifuga
    s.rect(26, 4, 8, 2, (60, 130, 220))
    for k, x in enumerate(range(44, 60, 4)):                                     # eprubete
        s.rect(x, 6, 2, 8, [(230, 60, 60), (120, 220, 120), (240, 200, 60), (90, 170, 240)][k % 4])
    s.rect(42, 12, 18, 2, (150, 150, 156))
    s.outline()
    return s


def d_teddy():
    s = Spr(16, 16)
    c = (164, 112, 66)
    s.ellipse(8, 10, 5, 5, c, shade=1, hi=1)
    s.ellipse(8, 4, 4, 3.6, c, shade=1, hi=1)
    s.ellipse(4, 1.5, 1.6, 1.6, c)
    s.ellipse(12, 1.5, 1.6, 1.6, c)
    s.set(6, 4, (20, 20, 20))
    s.set(10, 4, (20, 20, 20))
    s.rect(7, 6, 3, 1, (90, 50, 30))
    s.rect(5, 8, 7, 2, (200, 60, 70))                                            # fundiță
    s.outline()
    return s


def d_toybox():
    s = Spr(32, 24)
    s.poly([(1, 8), (31, 8), (31, 24), (1, 24)], (240, 180, 60), shade=2, hi=1)
    s.rect(1, 14, 30, 2, (60, 140, 220))
    s.ellipse(9, 6, 5, 5, (220, 60, 70), shade=1)                               # minge
    s.rect(16, 2, 6, 6, (60, 170, 90))                                          # cub
    s.rect(18, 4, 2, 2, (240, 240, 240))
    s.rect(24, 0, 3, 8, (90, 120, 230))
    s.outline()
    return s


def d_kids_table():
    s = Spr(44, 22)
    s.poly([(4, 8), (40, 8), (40, 11), (4, 11)], (240, 200, 90), shade=0)
    for x in (6, 36):
        s.rect(x, 11, 2, 11, (70, 150, 220))
    s.rect(10, 5, 10, 3, (250, 250, 246))                                       # desen
    for k, c in enumerate([(230, 60, 60), (60, 160, 230), (250, 200, 40), (70, 180, 80)]):
        s.rect(24 + k * 3, 6, 2, 2, c)
    s.poly([(0, 12), (6, 12), (6, 22), (0, 22)], (230, 90, 90), shade=1)        # scăunele
    s.poly([(38, 12), (44, 12), (44, 22), (38, 22)], (90, 190, 120), shade=1)
    s.outline()
    return s


def d_candles():
    s = Spr(22, 44)
    s.rect(10, 16, 2, 26, (150, 120, 50))
    s.rect(5, 40, 12, 3, (120, 96, 40))
    s.poly([(1, 14), (21, 14), (19, 18), (3, 18)], (180, 150, 60), shade=1, hi=1)
    for k, x in enumerate((3, 7, 11, 15, 18)):
        h = 6 + (k * 3) % 5
        s.rect(x, 14 - h, 2, h, (240, 232, 200))
        s.set(x, 13 - h, (255, 190, 70))
        s.set(x, 12 - h, (255, 240, 170))
    s.outline()
    return s


def d_ambulance():
    s = Spr(128, 60)
    white = (232, 234, 232)
    s.poly([(42, 4), (124, 4), (124, 48), (42, 48)], white, shade=3, hi=1)       # cutia
    s.poly([(8, 22), (18, 14), (44, 14), (44, 48), (6, 48), (6, 30)], white, shade=2, hi=1)  # cabina
    s.poly([(12, 22), (20, 16), (30, 16), (30, 28), (10, 28)], (40, 60, 90))     # parbrizul
    s.rect(14, 18, 10, 1, (120, 150, 190))
    s.rect(32, 17, 9, 11, (40, 60, 90))
    s.rect(6, 34, 118, 6, (200, 30, 30))                                         # banda roșie
    s.rect(6, 40, 118, 3, (240, 200, 40))
    for x in range(42, 124, 12):                                                 # carouri
        s.rect(x, 34, 6, 6, (240, 200, 40))
    s.rect(46, 0, 18, 5, (30, 70, 220))                                          # girofarul
    s.rect(48, 1, 5, 3, (120, 170, 255))
    s.rect(104, 1, 10, 4, (30, 70, 220))
    for (cx, cy) in [(84, 20)]:                                                  # steaua vieții
        s.rect(cx - 2, cy - 8, 4, 16, (30, 70, 200))
        for a in (60, 120):
            for r in range(-8, 9):
                x = cx + int(round(math.cos(math.radians(a)) * r))
                y = cy + int(round(math.sin(math.radians(a)) * r))
                s.rect(x - 1, y - 1, 3, 3, (30, 70, 200))
        s.rect(cx, cy - 6, 1, 12, (240, 240, 240))
    s.rect(100, 8, 1, 38, (180, 182, 186))                                       # ușa din spate
    s.rect(4, 44, 6, 4, (230, 210, 120))                                         # far
    s.rect(4, 48, 122, 4, (50, 52, 56))
    for x in (24, 102):
        s.ellipse(x, 52, 8, 8, (26, 26, 28))
        s.ellipse(x, 52, 4, 4, (150, 154, 160))
    s.outline()
    return s


def d_helicopter():
    s = Spr(192, 72)
    red = (196, 36, 36)
    white = (234, 236, 236)
    # rotorul principal (pale lungi, subțiri) și axul
    s.rect(6, 6, 180, 2, (40, 42, 46))
    s.rect(6, 6, 180, 1, (90, 94, 100))
    s.rect(76, 6, 6, 9, (70, 72, 78))
    s.ellipse(79, 6, 5, 3, (60, 62, 66))
    # carcasa motorului
    s.poly([(50, 14), (108, 14), (114, 22), (46, 22)], (90, 94, 100), shade=2, hi=1)
    s.rect(60, 16, 30, 2, (60, 62, 66))
    # coada
    s.poly([(108, 26), (172, 28), (172, 34), (108, 40)], red, shade=1, hi=0)
    s.poly([(166, 8), (180, 8), (184, 40), (168, 40)], red, shade=2, hi=1)       # derivă
    s.ellipse(176, 32, 9, 9, (60, 62, 66))
    s.ellipse(176, 32, 6, 6, (110, 114, 120))
    s.rect(140, 34, 18, 2, white)
    # cabina și corpul
    body = [(10, 40), (14, 30), (24, 22), (40, 20), (112, 20), (120, 28), (122, 44), (114, 52), (24, 52), (12, 48)]
    s.poly(body, white, shade=4, hi=1)
    s.poly([(10, 44), (122, 44), (114, 52), (24, 52), (12, 48)], red, shade=2)
    s.rect(12, 41, 110, 3, (250, 200, 40))
    # geamurile
    s.poly([(14, 34), (24, 24), (40, 22), (40, 36), (16, 38)], (34, 60, 96))
    s.poly([(20, 28), (26, 24), (32, 24), (22, 32)], (120, 160, 210))
    s.rect(46, 24, 20, 12, (34, 60, 96))
    s.rect(70, 24, 22, 12, (34, 60, 96))
    s.rect(47, 25, 8, 1, (110, 150, 200))
    s.rect(71, 25, 8, 1, (110, 150, 200))
    s.rect(68, 22, 1, 28, (170, 172, 176))                                       # ușa glisantă
    s.rect(100, 22, 1, 28, (170, 172, 176))
    # „112” pe coadă
    digits = {"1": ["010", "110", "010", "010", "111"], "2": ["110", "001", "010", "100", "111"]}
    x0 = 128
    for ch in "112":
        for j, row in enumerate(digits[ch]):
            for i, b in enumerate(row):
                if b == "1":
                    s.set(x0 + i, 28 + j, white)
        x0 += 4
    # patinele
    for x in (40, 96):
        s.rect(x, 52, 2, 10, (60, 62, 66))
    s.rect(22, 62, 98, 3, (70, 72, 76))
    s.rect(18, 61, 6, 2, (70, 72, 76))
    s.rect(30, 56, 90, 1, (50, 52, 56))
    s.outline()
    return s


def d_ac_unit():
    s = Spr(52, 38)
    s.poly([(1, 4), (51, 4), (51, 34), (1, 34)], (190, 194, 196), shade=3, hi=1)
    s.ellipse(17, 19, 12, 12, (60, 62, 66))
    s.ellipse(17, 19, 10, 10, (40, 42, 46))
    for a in range(0, 360, 45):
        for r in range(2, 10):
            x = 17 + int(round(math.cos(math.radians(a)) * r))
            y = 19 + int(round(math.sin(math.radians(a)) * r))
            s.set(x, y, (90, 94, 100))
    for y in range(8, 32, 3):
        s.rect(34, y, 14, 1, (150, 154, 158))
    s.rect(4, 34, 6, 4, (70, 72, 76))
    s.rect(42, 34, 6, 4, (70, 72, 76))
    s.outline()
    return s


def d_antenna():
    s = Spr(18, 70)
    s.rect(8, 6, 2, 62, (150, 154, 160))
    for y in range(10, 66, 6):
        s.set(7, y, (100, 104, 110))
        s.set(10, y + 3, (100, 104, 110))
    for (y, w) in [(14, 14), (24, 10), (34, 8)]:
        s.rect(9 - w // 2, y, w, 1, (130, 134, 140))
    s.rect(7, 2, 4, 4, (220, 30, 30))                                            # balizaj
    s.rect(8, 3, 2, 2, (255, 120, 100))
    s.rect(3, 66, 12, 4, (90, 92, 96))
    s.outline()
    return s


def d_windsock():
    s = Spr(36, 64)
    s.rect(3, 4, 2, 58, (150, 154, 160))
    s.rect(1, 60, 6, 4, (90, 92, 96))
    for k in range(5):                                                           # mâneca de vânt
        x = 5 + k * 6
        col = (240, 110, 30) if k % 2 == 0 else (240, 240, 236)
        s.poly([(x, 4 + k), (x + 6, 5 + k), (x + 6, 12 - k // 2), (x, 13 - k // 2)], col, shade=1)
    s.outline()
    return s


def d_barricade():
    s = Spr(64, 46)
    s.poly([(2, 22), (60, 14), (62, 22), (4, 30)], (196, 200, 204), shade=1, hi=1)   # un pat răsturnat
    s.rect(6, 28, 54, 3, CHROME2)
    for x in (10, 30, 50):
        s.rect(x, 30, 2, 12, CHROME2)
    s.poly([(10, 0), (24, 4), (22, 22), (8, 18)], (40, 60, 110), shade=2)              # scaune
    s.poly([(40, 4), (54, 2), (56, 16), (42, 18)], (50, 100, 170), shade=2)
    s.poly([(24, 8), (40, 6), (42, 20), (26, 22)], (226, 228, 228), shade=2)           # un dulap
    s.rect(2, 40, 60, 6, (90, 70, 50))
    s.rect(14, 34, 30, 2, (230, 200, 40))                                              # bandă
    for x in range(14, 44, 6):
        s.rect(x, 34, 3, 2, (30, 30, 30))
    s.outline()
    return s


def d_cyl_rack():
    s = Spr(36, 46)
    for k in range(3):
        x = 3 + k * 10
        s.poly([(x, 8), (x + 8, 8), (x + 8, 44), (x, 44)], (226, 228, 224), shade=2, hi=1)
        s.rect(x + 2, 3, 4, 5, (130, 134, 140))
        s.rect(x, 18, 8, 1, (40, 140, 70))
    s.rect(1, 20, 34, 2, (60, 62, 66))
    s.rect(1, 36, 34, 2, (60, 62, 66))
    s.outline()
    return s


def d_bio_bin():
    s = Spr(18, 22)
    s.poly([(2, 4), (16, 4), (15, 22), (3, 22)], (240, 200, 40), shade=2, hi=1)
    s.rect(1, 2, 16, 3, (200, 40, 40))
    s.rect(7, 10, 4, 4, (20, 20, 20))
    s.outline()
    return s


DECOR = [
    # nume, funcție, înălțime în lume (1 = cât peretele), solid (0 nu, 1 da, 2 atârnă de tavan), rază (0 = automat)
    ("birou", sp.d_desk, 0.62, 1, 0),
    ("scaun", sp.d_chair, 0.55, 0, 0),
    ("canapea", sp.d_sofa, 0.42, 1, 0),
    ("planta", sp.d_plant, 0.8, 1, 0),
    ("ficus", sp.d_ficus, 1.0, 1, 0),
    ("automat", sp.d_vending, 1.0, 1, 0),
    ("apa", sp.d_cooler, 0.76, 1, 0),
    ("cutii", sp.d_boxes, 0.62, 1, 0),
    ("cos", sp.d_bin, 0.36, 0, 0),
    ("masa", sp.d_table, 0.5, 1, 0),
    ("carucior", sp.d_cart, 0.6, 1, 0),
    ("cearsaf", sp.d_sheet_body, 0.24, 0, 0),
    ("lada", sp.d_case, 0.42, 1, 0),
    ("pat", lambda: d_bed(False), 0.56, 1, 0.45),
    ("pat_sange", lambda: d_bed(True), 0.56, 1, 0.45),
    ("targa", d_gurney, 0.46, 1, 0.42),
    ("perfuzie", d_iv, 1.0, 0, 0),
    ("rotile", d_wheelchair, 0.5, 1, 0.3),
    ("dulap", d_med_cabinet, 1.0, 1, 0.35),
    ("monitor_med", d_monitor_med, 0.95, 1, 0.22),
    ("urgenta", d_crash_cart, 0.62, 1, 0.3),
    ("masa_op", d_or_table, 0.5, 1, 0.45),
    ("lampa_op", d_or_lamp, 0.42, 2, 0),
    ("ct", d_ct, 0.86, 1, 0.5),
    ("scaune", d_waiting_chairs, 0.46, 1, 0.42),
    ("ghiseu", d_counter, 0.52, 1, 0),
    ("frigider", d_lab_fridge, 1.0, 1, 0.35),
    ("masa_lab", d_lab_bench, 0.58, 1, 0.45),
    ("ursulet", d_teddy, 0.18, 0, 0),
    ("jucarii", d_toybox, 0.34, 1, 0.3),
    ("masuta", d_kids_table, 0.32, 1, 0.35),
    ("sfesnic", d_candles, 0.74, 1, 0.22),
    ("ambulanta", d_ambulance, 1.06, 1, 1.05),
    ("elicopter", d_helicopter, 1.45, 1, 1.35),
    ("ac", d_ac_unit, 0.62, 1, 0.42),
    ("antena", d_antenna, 1.7, 1, 0.15),
    ("con_vant", d_windsock, 1.45, 1, 0.12),
    ("baricada", d_barricade, 0.78, 1, 0.5),
    ("butelii", d_cyl_rack, 0.76, 1, 0.35),
    ("cos_bio", d_bio_bin, 0.34, 1, 0.16),
]


def decor_sheet(out):
    """Împachetează obiectele pe rânduri (lățimea foii 512) și scrie coordonatele în decor.txt."""
    sprs = [(name, fn(), h, solid, rad) for (name, fn, h, solid, rad) in DECOR]
    W = 512
    x = y = rowh = 0
    pos = []
    for (name, s, h, solid, rad) in sprs:
        if x + s.w > W:
            x = 0
            y += rowh + 1
            rowh = 0
        pos.append((x, y))
        x += s.w + 1
        rowh = max(rowh, s.h)
    H = y + rowh
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    lines = []
    for i, ((name, s, h, solid, rad), (px, py)) in enumerate(zip(sprs, pos)):
        im.paste(s.image(), (px, py))
        lines.append("%d %s %d %d %.2f %d %.2f %d %d" % (i, name, s.w, s.h, h, solid, rad, px, py))
    im.save(os.path.join(out, "decor.png"), optimize=True)
    with open(os.path.join(out, "decor.txt"), "w") as f:
        f.write("\n".join(lines) + "\n")


def main(out):
    os.makedirs(out, exist_ok=True)
    sp.sheet(characters(), 4, 48, 64).save(os.path.join(out, "z.png"), optimize=True)
    sp.sheet([subject9(0), subject9(1), sp.boss_dead()], 3).save(os.path.join(out, "boss.png"), optimize=True)
    sp.sheet([creature(0), creature(1), creature_dead(), creature(3)], 4).save(os.path.join(out, "boss3.png"), optimize=True)
    sp.sheet([o2_cylinder(), o2_cylinder(True)], 2).save(os.path.join(out, "barrel.png"), optimize=True)
    sp.sheet([sp.explosion(0), sp.explosion(1), sp.explosion(2)], 3).save(os.path.join(out, "expl.png"), optimize=True)
    sp.acid().image().save(os.path.join(out, "acid.png"), optimize=True)
    items = [sp.item_ammo(), sp.item_med(), sp.item_card((40, 88, 216)), sp.item_card((200, 36, 36)), sp.item_note(),
             sp.item_shotgun(), sp.item_shells(), sp.item_card((226, 186, 34)), sp.item_card((36, 160, 70)),
             sp.item_smg(), sp.item_smg_ammo()]
    sp.sheet(items, 11).save(os.path.join(out, "items.png"), optimize=True)
    sp.gun_pistol2().image().save(os.path.join(out, "gun.png"), optimize=True)
    sp.scale2x(sp.gun_shotgun()).image().save(os.path.join(out, "gun2.png"), optimize=True)
    sp.scale2x(sp.gun_smg()).image().save(os.path.join(out, "gun3.png"), optimize=True)
    sp.scale2x(sp.flash(24, 20)).image().save(os.path.join(out, "flash.png"), optimize=True)
    sp.scale2x(sp.flash(32, 24)).image().save(os.path.join(out, "flash2.png"), optimize=True)
    sp.scale2x(sp.flash(20, 16)).image().save(os.path.join(out, "flash3.png"), optimize=True)
    decor_sheet(out)
    for f in ("boss2.png",):
        p = os.path.join(out, f)
        if os.path.exists(p):
            os.remove(p)
    print("ok")
