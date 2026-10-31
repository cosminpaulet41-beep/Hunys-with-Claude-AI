"""Genereaza sprite-urile jocului (PNG) din forme geometrice + umbre + contur.

Iesire in directorul dat:
  z.png      6 personaje (randuri) x 3 cadre (mers A, mers B, cazut), 48x64 fiecare:
             pacient, asistenta, medic, paznic, lucrator Vectra, Ioana (fara cadru cazut)
  boss.png   Subiectul 0: 3 cadre (A, B, cazut), 64x88
  items.png  9 obiecte de 24x24: munitie, trusa, card albastru, card rosu, notita,
             pusca, cartuse, cheie galbena, card verde
  gun.png    mana cu pistolul 44x30      gun2.png  pusca 48x34
  flash.png  flacara pistol 24x20        flash2.png flacara pusca 32x24
"""
import sys
from PIL import Image

OUT = sys.argv[1] if len(sys.argv) > 1 else "."
OUTLINE = (16, 14, 16)


def mul(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def hexc(h):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255)


class Spr:
    def __init__(s, w, h):
        s.w, s.h = w, h
        s.c = [[None] * w for _ in range(h)]

    def set(s, x, y, col):
        if 0 <= x < s.w and 0 <= y < s.h:
            s.c[y][x] = col

    def get(s, x, y):
        if 0 <= x < s.w and 0 <= y < s.h:
            return s.c[y][x]
        return None

    def rect(s, x, y, w, h, col):
        for j in range(y, y + h):
            for i in range(x, x + w):
                s.set(i, j, col)

    def poly(s, pts, col, shade=0, hi=0, dark=0.72, light=1.18):
        """Umple poligonul; umbreste ultimii `shade` pixeli din dreapta fiecarui rand,
        lumineaza primii `hi` pixeli din stanga (lumina vine din stanga sus)."""
        ys = [p[1] for p in pts]
        rows = {}
        for y in range(int(min(ys)), int(max(ys)) + 1):
            yc = y + 0.5
            xs = []
            n = len(pts)
            for k in range(n):
                x1, y1 = pts[k]
                x2, y2 = pts[(k + 1) % n]
                if (y1 <= yc < y2) or (y2 <= yc < y1):
                    xs.append(x1 + (yc - y1) * (x2 - x1) / (y2 - y1))
            xs.sort()
            for a, b in zip(xs[0::2], xs[1::2]):
                xa, xb = int(round(a)), int(round(b)) - 1
                for x in range(xa, xb + 1):
                    s.set(x, y, col)
                rows.setdefault(y, []).append((xa, xb))
        for y, spans in rows.items():
            for xa, xb in spans:
                for x in range(max(xa, xb - shade + 1), xb + 1):
                    s.set(x, y, mul(col, dark))
                for x in range(xa, min(xb + 1, xa + hi)):
                    s.set(x, y, mul(col, light))

    def ellipse(s, cx, cy, rx, ry, col, shade=0, hi=0):
        pts = []
        import math
        for k in range(32):
            a = 2 * math.pi * k / 32
            pts.append((cx + rx * math.cos(a), cy + ry * math.sin(a)))
        s.poly(pts, col, shade, hi)

    def outline(s, col=OUTLINE):
        add = []
        for y in range(s.h):
            for x in range(s.w):
                if s.c[y][x] is not None:
                    continue
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if s.get(x + dx, y + dy) not in (None, col):
                        add.append((x, y))
                        break
        for x, y in add:
            s.c[y][x] = col

    def image(s):
        im = Image.new("RGBA", (s.w, s.h), (0, 0, 0, 0))
        for y in range(s.h):
            for x in range(s.w):
                if s.c[y][x] is not None:
                    im.putpixel((x, y), s.c[y][x] + (255,))
        return im


import math

# ---------------------------------------------------------------- personaje

SKIN = (168, 172, 154)       # piele cenusie, de mort
VEIN = (118, 126, 108)
EYE = (214, 218, 200)        # ochi tulburi
EYE_RIM = (52, 36, 36)
BLOOD = (118, 24, 24)
BLOOD2 = (84, 14, 14)

TYPES = [
    # haine sus, haine jos (None = picioare goale), incaltaminte, par / casca
    dict(name="pacient", top=(146, 178, 200), bottom=None, shoe=(206, 206, 198), hair=(46, 38, 30)),
    dict(name="asistenta", top=(62, 150, 146), bottom=(56, 138, 134), shoe=(226, 226, 218), hair=(74, 46, 28)),
    dict(name="medic", top=(226, 226, 218), bottom=(52, 52, 62), shoe=(28, 28, 30), hair=(150, 150, 146)),
    dict(name="paznic", top=(46, 62, 98), bottom=(34, 40, 56), shoe=(20, 20, 22), hair=(26, 26, 26)),
    dict(name="vectra", top=(222, 222, 212), bottom=(214, 214, 204), shoe=(216, 180, 40), hair=None),
    dict(name="ioana", top=(58, 146, 142), bottom=(52, 134, 130), shoe=(232, 232, 224), hair=(58, 34, 22)),
    # personalul televiziunii (Tura de Noapte 2)
    dict(name="reporter", top=(62, 64, 74), bottom=(44, 46, 54), shoe=(22, 22, 24), hair=(52, 40, 30)),
    dict(name="cameraman", top=(40, 40, 44), bottom=(118, 106, 76), shoe=(70, 52, 36), hair=(30, 26, 22)),
    dict(name="prezentatoare", top=(168, 36, 48), bottom=(28, 28, 32), shoe=(20, 20, 22), hair=(196, 164, 104)),
    dict(name="bucatar", top=(222, 222, 214), bottom=(96, 96, 104), shoe=(30, 30, 32), hair=(240, 240, 236)),
    dict(name="mara", top=(176, 150, 106), bottom=(46, 50, 64), shoe=(40, 30, 26), hair=(34, 28, 26)),
]
ALIVE = ("ioana", "mara")


def humanoid(t, frame, ioana=False):
    s = Spr(48, 64)
    skin = ((214, 168, 136) if t["name"] == "mara" else (226, 178, 144)) if ioana else SKIN
    top, shoe = t["top"], t["shoe"]
    bottom = t["bottom"] if t["bottom"] else skin
    sway = 0 if frame == 0 else 1
    hunch = 0 if ioana else 2          # infectatii stau aplecati, mai scunzi
    # picioare
    if ioana:
        ll, rl = 18, 18
    else:
        ll, rl = (17, 14) if frame == 0 else (14, 17)
    y0 = 42
    s.poly([(16, y0), (23, y0), (23, y0 + ll), (16, y0 + ll)], bottom, shade=2, hi=1)
    s.poly([(25, y0), (32, y0), (32, y0 + rl), (25, y0 + rl)], bottom, shade=2, hi=1)
    if t["name"] == "pacient":
        s.rect(16, y0 + ll - 3, 7, 2, (196, 196, 186))
        s.rect(25, y0 + rl - 3, 7, 2, (196, 196, 186))
        for (x, y) in [(18, 47), (29, 50), (20, 52)]:
            s.set(x, y, VEIN)
    s.poly([(15, y0 + ll), (24, y0 + ll), (24, y0 + ll + 3), (14, y0 + ll + 3)], shoe, shade=2)
    s.poly([(24, y0 + rl), (33, y0 + rl), (34, y0 + rl + 3), (24, y0 + rl + 3)], shoe, shade=2)
    # bazin
    s.poly([(15, 36), (33, 36), (33, 44), (15, 44)], bottom, shade=3, hi=1)
    if t["name"] == "paznic":
        s.rect(15, 36, 18, 2, (18, 18, 18))
        s.rect(23, 36, 3, 2, (190, 170, 70))
    # trunchi aplecat: umarul stang mai jos
    ls, rs = 19 + hunch, 18 + (0 if ioana else 1)
    torso = [(12, ls), (24, 16 + hunch), (36, rs), (35, 28), (34, 40), (14, 40), (13, 28)]
    if t["name"] == "pacient":
        torso = [(12, ls), (24, 16 + hunch), (36, rs), (35, 28), (35, 47), (13, 47), (13, 28)]
    if t["name"] == "medic":
        torso = [(11, ls), (24, 16 + hunch), (37, rs), (37, 30), (37, 52), (11, 52), (11, 30)]
    torso = [(x + sway, y) for x, y in torso]
    s.poly(torso, top, shade=4, hi=1)
    n = t["name"]
    if n == "pacient":
        for x in range(13, 36, 3):
            s.set(x + sway, 47, None)
            s.set(x + 1 + sway, 46 + (x % 2), None)
        s.rect(23 + sway, 20, 1, 26, mul(top, 0.8))
        for (x, y) in [(16, 26), (29, 24), (19, 38), (31, 34)]:
            s.set(x + sway, y, (176, 204, 222))
    if n in ("asistenta", "ioana"):
        s.poly([(20 + sway, 18 + hunch), (28 + sway, 18 + hunch), (24 + sway, 25)], mul(skin, 0.9))
        s.rect(29 + sway, 27, 4, 4, mul(top, 0.78))
        if n == "ioana":
            s.rect(14 + sway, 26, 5, 2, (230, 230, 230))  # ecuson
    if n == "medic":
        shirt = (74, 106, 154)
        s.poly([(19 + sway, 18 + hunch), (29 + sway, 18 + hunch), (28 + sway, 40), (20 + sway, 40)], shirt, shade=1)
        s.poly([(23 + sway, 19 + hunch), (25 + sway, 19 + hunch), (26 + sway, 32), (24 + sway, 35), (22 + sway, 32)], (130, 38, 38))
        s.rect(19 + sway, 20, 1, 32, mul(top, 0.7))
        s.rect(28 + sway, 20, 1, 32, mul(top, 0.7))
        s.rect(13 + sway, 31, 4, 5, mul(top, 0.84))
    if n == "paznic":
        s.rect(29 + sway, 24, 4, 4, (210, 180, 64))
        s.rect(24 + sway, 20, 1, 20, mul(top, 0.78))
        for y in (25, 30, 35):
            s.set(24 + sway, y, (196, 196, 196))
    if n == "reporter":
        shirt = (214, 214, 206)
        s.poly([(20 + sway, 18 + hunch), (28 + sway, 18 + hunch), (27 + sway, 39), (21 + sway, 39)], shirt, shade=1)
        s.poly([(23 + sway, 19 + hunch), (25 + sway, 19 + hunch), (26 + sway, 33), (24 + sway, 36), (22 + sway, 33)], (150, 24, 30))
        s.rect(19 + sway, 19 + hunch, 1, 20, mul(top, 0.7))
        s.rect(28 + sway, 19 + hunch, 1, 20, mul(top, 0.7))
        s.rect(29 + sway, 25, 4, 1, (230, 230, 230))      # batista din buzunar
    if n == "cameraman":
        vest = (88, 92, 78)
        s.poly([(13 + sway, 24), (20 + sway, 21), (20 + sway, 40), (14 + sway, 40)], vest, shade=1)
        s.poly([(28 + sway, 21), (35 + sway, 24), (34 + sway, 40), (28 + sway, 40)], vest, shade=1)
        s.rect(15 + sway, 30, 4, 4, mul(vest, 0.8))
        s.rect(29 + sway, 30, 4, 4, mul(vest, 0.8))
        s.rect(15 + sway, 35, 4, 3, mul(vest, 0.8))
    if n == "prezentatoare":
        s.poly([(21 + sway, 18 + hunch), (27 + sway, 18 + hunch), (24 + sway, 30)], (236, 232, 226))
        s.rect(21 + sway, 30, 1, 10, mul(top, 0.7))
        s.rect(27 + sway, 30, 1, 10, mul(top, 0.7))
        s.set(24 + sway, 33, (230, 200, 90))                # brosa
    if n == "bucatar":
        for y in (23, 28, 33, 38):
            s.set(20 + sway, y, (120, 120, 120))
            s.set(28 + sway, y, (120, 120, 120))
        s.rect(13 + sway, 36, 22, 1, mul(top, 0.8))
        s.rect(16 + sway, 37, 16, 4, (200, 196, 186))         # sort
    if n == "mara":
        s.poly([(21 + sway, 18), (27 + sway, 18), (24 + sway, 26)], (60, 60, 70))
        s.rect(13 + sway, 31, 22, 2, mul(top, 0.78))          # cordonul trenciului
        s.rect(29 + sway, 23, 4, 5, (226, 226, 220))          # legitimatia de presa
        s.rect(30 + sway, 24, 2, 2, (220, 100, 40))
    if n == "vectra":
        s.rect(13 + sway, 30, 22, 1, mul(top, 0.8))   # cusaturi combinezon
        s.rect(24 + sway, 19, 1, 21, mul(top, 0.82))
        s.rect(27 + sway, 22, 6, 3, (40, 90, 160))    # eticheta albastra
        s.rect(15 + sway, 24, 4, 1, mul(top, 0.8))
    if not ioana:
        s.poly([(26 + sway, 22), (31 + sway, 23), (30 + sway, 30), (28 + sway, 34), (27 + sway, 28)], BLOOD)
        s.poly([(16 + sway, 32), (19 + sway, 31), (19 + sway, 36), (17 + sway, 37)], BLOOD2)

    # brate
    sleeve = top
    bare = n in ("pacient", "asistenta", "ioana")
    glove = (216, 180, 40) if n == "vectra" else skin

    def arm(side, reach):
        if side == "L":
            if reach:
                up = [(9, ls), (15, ls), (14, ls + 7), (9, ls + 8)]
                fo = [(9, ls + 6), (14, ls + 5), (19, ls + 10), (14, ls + 13)]
                hand = (17.5, ls + 11.0)
            else:
                up = [(8, ls), (14, ls), (13, ls + 11), (8, ls + 12)]
                fo = [(8, ls + 11), (13, ls + 10), (12, ls + 21), (7, ls + 21)]
                hand = (9.5, ls + 22.5)
        else:
            if reach:
                up = [(33, rs), (39, rs), (39, rs + 8), (34, rs + 7)]
                fo = [(34, rs + 5), (39, rs + 6), (34, rs + 13), (29, rs + 10)]
                hand = (30.5, rs + 11.0)
            else:
                up = [(34, rs), (40, rs), (40, rs + 12), (35, rs + 11)]
                fo = [(35, rs + 10), (40, rs + 11), (41, rs + 21), (36, rs + 21)]
                hand = (38.5, rs + 22.5)
        up = [(x + sway, y) for x, y in up]
        fo = [(x + sway, y) for x, y in fo]
        s.poly(up, sleeve, shade=2, hi=1)
        s.poly(fo, skin if bare else sleeve, shade=2, hi=1)
        if bare and not ioana:
            vx = int(sum(p[0] for p in fo) / 4)
            vy = int(sum(p[1] for p in fo) / 4)
            s.set(vx, vy, VEIN)
            s.set(vx + 1, vy + 1, VEIN)
        hx, hy = hand[0] + sway, hand[1]
        big = 3.6 if reach else 3.1
        s.ellipse(hx, hy, big, big - 0.3, glove, shade=1)
        if not ioana:
            for k in (-2, 0, 2):
                s.set(int(hx) + k, int(hy) + 3, mul(glove, 0.72))
                s.set(int(hx) + k, int(hy) + 4, mul(glove, 0.72))

    if ioana and n == "mara":
        arm("L", False)
        arm("R", True)
        s.rect(29 + sway, rs + 2, 3, 9, (30, 30, 34))           # microfon
        s.ellipse(30.5 + sway, rs + 1, 2.4, 2.4, (90, 90, 96))
        s.rect(28 + sway, rs + 6, 5, 3, (220, 100, 40))         # cubul cu sigla
    elif ioana:
        arm("L", False)
        arm("R", False)
        # cutia cu probe tinuta la piept
        s.poly([(18 + sway, 28), (30 + sway, 28), (30 + sway, 37), (18 + sway, 37)], (236, 236, 228), shade=2, hi=1)
        s.rect(21 + sway, 31, 6, 3, (226, 120, 30))
        s.ellipse(17 + sway, 33, 2.6, 2.4, skin)
        s.ellipse(31 + sway, 33, 2.6, 2.4, skin, shade=1)
    else:
        arm("L", frame == 0)
        arm("R", frame == 1)

    # gat si cap (capul lasat in jos si inclinat)
    tilt = 0 if ioana else (1 if frame == 0 else -1)
    hy = 2 + hunch
    hx = sway + (0 if ioana else -1)
    s.poly([(21 + hx, hy + 12), (27 + hx, hy + 12), (27 + hx, 21), (21 + hx, 21)], mul(skin, 0.82))
    head = [(18 + hx, hy + 3 + tilt), (20 + hx, hy + 1), (28 + hx, hy + 1), (30 + hx, hy + 3 - tilt),
            (30 + hx, hy + 11 - tilt), (27 + hx, hy + 15), (21 + hx, hy + 15), (18 + hx, hy + 11 + tilt)]
    s.poly(head, skin, shade=3, hi=1)
    ey = hy + 6
    if n == "vectra":
        # gluga + masca de gaze
        s.poly([(16 + hx, hy + 1), (32 + hx, hy + 1), (32 + hx, hy + 15), (16 + hx, hy + 15)], top, shade=3, hi=1)
        s.poly([(19 + hx, hy + 4), (29 + hx, hy + 4), (29 + hx, hy + 9), (19 + hx, hy + 9)], (52, 58, 64))
        s.set(21 + hx, hy + 5, (140, 160, 170))
        s.set(27 + hx, hy + 6, EYE)
        s.set(26 + hx, hy + 6, EYE_RIM)
        s.ellipse(24 + hx, hy + 12.5, 3.2, 2.6, (60, 60, 64), shade=1)
        s.rect(23 + hx, hy + 12, 2, 2, (30, 30, 30))
        s.rect(20 + hx, hy + 10, 2, 2, BLOOD)
    else:
        if ioana and n == "mara":
            s.poly([(17 + hx, hy - 1), (31 + hx, hy - 1), (31 + hx, hy + 4), (24 + hx, hy + 3), (17 + hx, hy + 6)], t["hair"], shade=2)
            s.poly([(16 + hx, hy + 2), (19 + hx, hy + 2), (19 + hx, hy + 10), (16 + hx, hy + 9)], t["hair"], shade=1)
            for ex in (20, 26):
                s.rect(ex + hx, ey, 2, 2, (40, 30, 26))
                s.set(ex + hx, ey, (250, 250, 250))
            s.rect(22 + hx, ey + 6, 4, 1, (160, 84, 76))
        elif ioana:
            s.poly([(17 + hx, hy), (31 + hx, hy), (31 + hx, hy + 5), (17 + hx, hy + 6)], t["hair"], shade=2)
            s.poly([(29 + hx, hy + 3), (33 + hx, hy + 4), (33 + hx, hy + 14), (30 + hx, hy + 12)], t["hair"], shade=1)
            for ex in (20, 26):
                s.rect(ex + hx, ey, 2, 2, (44, 32, 26))
                s.set(ex + hx, ey, (250, 250, 250))
            s.rect(22 + hx, ey + 6, 4, 1, (170, 90, 80))
        else:
            if n == "paznic":
                s.poly([(17 + hx, hy - 1), (31 + hx, hy - 1), (31 + hx, hy + 4), (17 + hx, hy + 4)], (30, 40, 64), shade=2)
                s.poly([(16 + hx, hy + 4), (32 + hx, hy + 4), (30 + hx, hy + 6), (18 + hx, hy + 6)], (14, 18, 30))
                s.rect(22 + hx, hy, 4, 2, (210, 180, 64))
            elif n == "asistenta":
                s.poly([(18 + hx, hy), (30 + hx, hy), (30 + hx, hy + 4), (18 + hx, hy + 5)], t["hair"], shade=2)
                s.ellipse(24 + hx, hy - 1, 3.6, 2.0, t["hair"])
            elif n == "medic":
                s.poly([(18 + hx, hy), (30 + hx, hy), (30 + hx, hy + 3), (24 + hx, hy + 2), (18 + hx, hy + 4)], t["hair"], shade=1)
            elif n == "reporter":
                s.poly([(18 + hx, hy), (30 + hx, hy), (30 + hx, hy + 3), (20 + hx, hy + 3), (18 + hx, hy + 5)], t["hair"], shade=1)
            elif n == "cameraman":
                for x in range(18, 31):
                    if x % 2 == 0:
                        s.set(x + hx, hy, t["hair"])
                    s.set(x + hx, hy + 1, t["hair"])
                s.rect(16 + hx, hy + 13, 16, 2, (24, 24, 26))         # castile pe gat
                s.ellipse(17 + hx, hy + 13, 2.2, 2.4, (36, 36, 40))
                s.ellipse(31 + hx, hy + 13, 2.2, 2.4, (36, 36, 40))
                s.set(31 + hx, hy + 13, (200, 40, 40))
            elif n == "prezentatoare":
                s.poly([(17 + hx, hy - 1), (31 + hx, hy - 1), (32 + hx, hy + 5), (17 + hx, hy + 5)], t["hair"], shade=2, hi=1)
                s.poly([(15 + hx, hy + 3), (19 + hx, hy + 3), (19 + hx, hy + 18), (15 + hx, hy + 16)], t["hair"], shade=1)
                s.poly([(29 + hx, hy + 3), (33 + hx, hy + 3), (33 + hx, hy + 16), (29 + hx, hy + 18)], t["hair"], shade=1)
            elif n == "bucatar":
                s.poly([(18 + hx, hy - 6), (30 + hx, hy - 6), (31 + hx, hy + 2), (17 + hx, hy + 2)], (236, 236, 230), shade=2, hi=1)
                s.ellipse(24 + hx, hy - 6, 7.5, 3.4, (244, 244, 240), shade=1)
                s.rect(17 + hx, hy + 1, 14, 1, (200, 200, 194))
            else:
                for x in range(19, 30):
                    if x % 3 != 1:
                        s.set(x + hx, hy + 1, t["hair"])
                    if x % 2 == 0:
                        s.set(x + hx, hy + 2, t["hair"])
            for ex in (20, 25):
                s.rect(ex + hx, ey, 3, 3, EYE_RIM)
                s.rect(ex + hx, ey + 1, 2, 1, EYE)
            if n == "medic":
                s.rect(19 + hx, ey - 1, 10, 1, (20, 20, 20))
                s.rect(19 + hx, ey - 1, 1, 4, (20, 20, 20))
                s.rect(28 + hx, ey - 1, 1, 4, (20, 20, 20))
            s.set(19 + hx, ey + 4, VEIN)
            s.set(28 + hx, ey + 3, VEIN)
            s.rect(21 + hx, ey + 6, 6, 3, (40, 12, 12))
            for x in (22, 24):
                s.set(x + hx, ey + 6, (214, 204, 176))
            s.rect(25 + hx, ey + 9, 1, 3, BLOOD)
            s.rect(19 + hx, ey + 3, 2, 2, BLOOD2)
    s.outline()
    return s


def humanoid_dead(t):
    s = Spr(48, 64)
    top = t["top"]
    bottom = t["bottom"] if t["bottom"] else SKIN
    for y in range(53, 64):
        for x in range(48):
            dx = (x - 24) / 22.0
            dy = (y - 58.5) / 5.0
            if dx * dx + dy * dy <= 1:
                s.set(x, y, (64 + (x * 7 + y * 3) % 9, 10, 10))
    s.ellipse(8, 52, 5, 4.2, SKIN, shade=1)
    if t["hair"]:
        s.rect(4, 49, 3, 6, t["hair"])
    s.poly([(12, 47), (30, 47), (30, 57), (12, 57)], top)
    s.poly([(12, 54), (30, 54), (30, 57), (12, 57)], mul(top, 0.72))
    s.poly([(30, 49), (44, 48), (44, 56), (30, 56)], bottom)
    s.rect(44, 47, 3, 4, t["shoe"])
    s.rect(44, 53, 3, 4, t["shoe"])
    s.poly([(14, 56), (26, 56), (26, 59), (14, 59)], SKIN)
    s.poly([(18, 49), (24, 49), (23, 53), (19, 53)], BLOOD)
    s.outline()
    return s


def boss(frame):
    """Subiectul 0: urias, palid, cu brate lungi si gheare."""
    s = Spr(64, 88)
    sk = (206, 202, 190)
    sk2 = (176, 170, 160)
    vein = (120, 130, 160)
    raw = (150, 40, 40)
    sw = 0 if frame == 0 else 1
    # picioare groase
    l1, l2 = (24, 21) if frame == 0 else (21, 24)
    s.poly([(18, 60), (29, 60), (28, 60 + l1), (17, 60 + l1)], sk2, shade=3, hi=1)
    s.poly([(35, 60), (46, 60), (47, 60 + l2), (36, 60 + l2)], sk2, shade=3, hi=1)
    s.poly([(15, 60 + l1), (30, 60 + l1), (30, 64 + l1), (13, 64 + l1)], (120, 112, 104))
    s.poly([(34, 60 + l2), (49, 60 + l2), (51, 64 + l2), (34, 64 + l2)], (120, 112, 104))
    # pantaloni rupti de spital
    s.poly([(17, 52), (47, 52), (47, 66), (17, 66)], (150, 176, 196), shade=3, hi=1)
    for x in range(17, 47, 3):
        s.set(x, 66, None)
    # trunchi masiv, aplecat
    torso = [(8, 26), (32, 18), (56, 26), (54, 40), (48, 54), (16, 54), (10, 40)]
    torso = [(x + sw, y) for x, y in torso]
    s.poly(torso, sk, shade=5, hi=2)
    # coaste si muschi expusi
    for y in (34, 38, 42):
        s.rect(22 + sw, y, 8, 1, sk2)
        s.rect(34 + sw, y, 8, 1, sk2)
    s.poly([(36 + sw, 22), (48 + sw, 25), (46 + sw, 33), (38 + sw, 31)], raw)
    s.poly([(38 + sw, 24), (45 + sw, 26), (44 + sw, 30), (39 + sw, 29)], (184, 70, 60))
    for (x, y) in [(18, 30), (19, 31), (20, 32), (27, 46), (28, 47), (44, 44), (45, 45)]:
        s.set(x + sw, y, vein)
    # brate lungi pana aproape de genunchi, cu gheare
    def arm(side, fwd):
        if side == "L":
            up = [(6, 26), (14, 26), (12, 44), (4, 44)]
            fo = [(4, 43), (12, 43), (14 if fwd else 11, 64), (6 if fwd else 3, 64)]
            hx = 10 if fwd else 7
        else:
            up = [(50, 26), (58, 26), (60, 44), (52, 44)]
            fo = [(52, 43), (60, 43), (61 if not fwd else 58, 64), (53 if not fwd else 50, 64)]
            hx = 57 if not fwd else 54
        up = [(x + sw, y) for x, y in up]
        fo = [(x + sw, y) for x, y in fo]
        s.poly(up, sk, shade=2, hi=1)
        s.poly(fo, sk2, shade=2, hi=1)
        s.ellipse(hx + sw, 66, 4.5, 3.5, sk2, shade=1)
        for k in (-3, -1, 1, 3):
            s.rect(hx + sw + k, 69, 1, 4, (40, 36, 34))
        s.rect(up[2][0] - 1, 40, 9, 2, (110, 110, 118))   # catusa rupta
    arm("L", frame == 0)
    arm("R", frame == 1)
    # cap fara par, mic fata de corp, cu ochi aprinsi
    hx = sw + (1 if frame == 0 else -1)
    s.poly([(25 + hx, 8), (28 + hx, 4), (36 + hx, 4), (39 + hx, 8), (39 + hx, 18), (35 + hx, 23), (29 + hx, 23), (25 + hx, 18)], sk, shade=3, hi=1)
    for ex in (28, 34):
        s.rect(ex + hx, 11, 3, 3, (40, 10, 10))
        s.rect(ex + hx, 12, 2, 1, (250, 120, 40))
    s.rect(28 + hx, 17, 9, 4, (36, 8, 8))
    for x in (29, 31, 33, 35):
        s.set(x + hx, 17, (226, 214, 190))
        s.set(x + hx + 1, 20, (226, 214, 190))
    s.rect(30 + hx, 21, 1, 4, BLOOD)
    for (x, y) in [(27, 9), (37, 14), (26, 15)]:
        s.set(x + hx, y, vein)
    s.outline()
    return s


def boss_dead():
    s = Spr(64, 88)
    for y in range(74, 88):
        for x in range(64):
            dx = (x - 32) / 30.0
            dy = (y - 81) / 6.5
            if dx * dx + dy * dy <= 1:
                s.set(x, y, (70 + (x * 5 + y * 3) % 9, 12, 12))
    s.ellipse(32, 74, 24, 8, (206, 202, 190), shade=4, hi=2)
    s.ellipse(12, 71, 6, 5, (206, 202, 190), shade=2)
    s.ellipse(44, 70, 9, 4, (150, 40, 40))
    for k in range(4):
        s.rect(52 + k * 2, 76, 1, 4, (40, 36, 34))
    s.outline()
    return s


# ---------------------------------------------------------------- obiecte

def item_ammo():
    s = Spr(24, 24)
    s.poly([(3, 11), (21, 11), (21, 21), (3, 21)], (62, 78, 44), shade=3, hi=1)
    s.poly([(3, 9), (21, 9), (21, 11), (3, 11)], (86, 104, 62))
    s.rect(6, 14, 12, 4, (210, 184, 92))
    for k in range(4):
        x = 5 + k * 4
        s.rect(x, 4, 3, 5, (206, 158, 62))
        s.set(x + 2, 5, (160, 116, 40))
        s.rect(x, 2, 3, 2, (164, 112, 52))
    s.outline()
    return s


def item_med():
    s = Spr(24, 24)
    s.poly([(9, 3), (15, 3), (15, 6), (9, 6)], (120, 120, 120))
    s.poly([(2, 6), (22, 6), (22, 21), (2, 21)], (232, 232, 224), shade=3, hi=1)
    s.rect(2, 18, 21, 1, (170, 170, 160))
    s.rect(10, 8, 4, 10, (30, 160, 74))
    s.rect(7, 11, 10, 4, (30, 160, 74))
    s.outline()
    return s


def item_card(col):
    s = Spr(24, 24)
    s.poly([(2, 7), (22, 7), (22, 20), (2, 20)], col, shade=2, hi=1)
    s.rect(2, 10, 21, 3, (236, 236, 236))
    s.rect(4, 15, 5, 3, (216, 184, 72))
    s.rect(12, 15, 8, 1, mul(col, 1.4))
    s.rect(12, 17, 6, 1, mul(col, 1.4))
    s.outline()
    return s


def item_note():
    s = Spr(24, 24)
    s.poly([(4, 2), (18, 2), (21, 5), (21, 22), (4, 22)], (236, 230, 208), shade=2)
    s.poly([(18, 2), (21, 5), (18, 5)], (196, 190, 168))
    for y in range(7, 20, 3):
        s.rect(7, y, 11 if y < 18 else 7, 1, (110, 110, 104))
    s.rect(6, 1, 2, 6, (150, 150, 160))
    s.outline()
    return s


def item_shotgun():
    s = Spr(24, 24)
    s.poly([(1, 12), (17, 11), (17, 14), (1, 15)], (60, 60, 68), shade=1, hi=1)   # teava
    s.poly([(5, 15), (12, 15), (12, 17), (5, 17)], (120, 76, 40), shade=1)         # pompa
    s.poly([(15, 10), (19, 10), (23, 15), (23, 19), (19, 19), (16, 15)], (128, 82, 44), shade=2, hi=1)  # pat
    s.rect(14, 14, 3, 3, (40, 40, 46))
    s.outline()
    return s


def item_shells():
    s = Spr(24, 24)
    s.poly([(3, 10), (21, 10), (21, 21), (3, 21)], (168, 34, 30), shade=3, hi=1)
    s.rect(6, 13, 12, 4, (232, 222, 196))
    for k in range(4):
        x = 4 + k * 4
        s.rect(x, 3, 3, 7, (196, 40, 34))
        s.rect(x, 8, 3, 2, (214, 176, 70))
    s.outline()
    return s


def item_key_yellow():
    s = Spr(24, 24)
    s.ellipse(7, 12, 5, 5, (230, 196, 48), shade=2, hi=1)
    s.ellipse(7, 12, 2, 2, (40, 36, 20))
    s.poly([(11, 11), (22, 11), (22, 14), (11, 14)], (230, 196, 48), shade=1, hi=1)
    s.rect(17, 14, 2, 3, (230, 196, 48))
    s.rect(20, 14, 2, 4, (230, 196, 48))
    s.outline()
    return s


# ---------------------------------------------------------------- arme (vazute din spate)

def gun_pistol():
    s = Spr(44, 30)
    skin = (214, 160, 120)
    sleeve = (40, 58, 92)
    s.poly([(17, 22), (27, 22), (31, 30), (15, 30)], skin, shade=3, hi=1)
    s.poly([(15, 26), (29, 26), (32, 30), (14, 30)], sleeve, shade=3, hi=1)
    s.rect(14, 28, 18, 1, (214, 210, 96))
    s.poly([(14, 15), (29, 15), (30, 21), (27, 24), (16, 24), (13, 21)], skin, shade=3, hi=1)
    for y in (18, 21):
        s.rect(24, y, 5, 1, (168, 112, 80))
    s.rect(16, 21, 8, 1, (176, 120, 86))
    s.poly([(12, 12), (16, 12), (17, 17), (13, 17)], skin, shade=1, hi=1)
    gunc = (64, 64, 74)
    s.poly([(16, 3), (27, 3), (27, 14), (16, 14)], gunc, shade=3, hi=2)
    s.rect(16, 2, 12, 1, (98, 98, 110))
    for y in range(6, 13, 2):
        s.rect(19, y, 5, 1, (44, 44, 52))
    s.rect(25, 5, 2, 3, (24, 24, 28))
    s.poly([(17, 14), (26, 14), (25, 16), (18, 16)], (40, 40, 48))
    s.rect(16, 0, 3, 2, (30, 30, 34))
    s.rect(24, 0, 3, 2, (30, 30, 34))
    s.rect(21, 1, 2, 1, (238, 238, 226))
    s.rect(21, 0, 2, 1, (30, 30, 34))
    s.outline()
    return s


def gun_shotgun():
    s = Spr(48, 34)
    skin = (214, 160, 120)
    sleeve = (40, 58, 92)
    metal = (58, 58, 66)
    wood = (132, 84, 44)
    # teava se ingusteaza spre fata (perspectiva)
    s.poly([(22, 0), (26, 0), (28, 18), (20, 18)], metal, shade=1, hi=1)
    s.rect(23, 0, 2, 1, (238, 238, 226))
    s.poly([(19, 18), (29, 18), (30, 26), (18, 26)], (44, 44, 50), shade=2, hi=1)
    # pompa din lemn tinuta cu mana stanga
    s.poly([(18, 9), (30, 9), (31, 16), (17, 16)], wood, shade=2, hi=1)
    for y in (11, 13):
        s.rect(19, y, 11, 1, mul(wood, 0.75))
    s.poly([(8, 11), (18, 10), (19, 16), (9, 18)], skin, shade=2, hi=1)
    s.poly([(2, 16), (10, 14), (12, 22), (3, 24)], sleeve, shade=2, hi=1)
    s.rect(0, 22, 6, 12, sleeve)
    # mana dreapta pe pat, jos
    s.poly([(26, 24), (38, 23), (39, 29), (27, 31)], skin, shade=3, hi=1)
    s.poly([(30, 28), (42, 26), (46, 34), (32, 34)], sleeve, shade=3, hi=1)
    s.rect(33, 31, 12, 1, (214, 210, 96))
    s.poly([(20, 26), (28, 26), (28, 34), (20, 34)], wood, shade=2, hi=1)
    s.outline()
    return s


def flash(w, h):
    s = Spr(w, h)
    for y in range(h):
        for x in range(w):
            dx = (x - (w - 1) / 2) / (w / 2 - 0.5)
            dy = (y - h * 0.55) / (h / 2 - 1)
            r = math.sqrt(dx * dx + dy * dy)
            a = math.atan2(dy, dx)
            spike = 0.55 + 0.45 * abs(math.cos(a * 4))
            if r < spike:
                f = r / spike
                if f < 0.3:
                    c = (255, 255, 236)
                elif f < 0.55:
                    c = (255, 232, 120)
                elif f < 0.8:
                    c = (255, 176, 48)
                else:
                    c = (232, 104, 24)
                s.set(x, y, c)
    return s


def sheet(sprites, cols, w=None, h=None):
    w = w or sprites[0].w
    h = h or sprites[0].h
    rows = (len(sprites) + cols - 1) // cols
    im = Image.new("RGBA", (w * cols, h * rows), (0, 0, 0, 0))
    for i, sp in enumerate(sprites):
        if sp is not None:
            im.paste(sp.image(), ((i % cols) * w, (i // cols) * h))
    return im


# ---------------------------------------------------------------- episodul 3

def spitter(frame):
    """Scuipatorul: infectat umflat, cu un sac verde de acid la gat.
    frame 0/1 = mers, 2 = scuipa (gura larg deschisa)."""
    s = Spr(48, 64)
    skin = (176, 182, 120)
    skin2 = (140, 146, 92)
    over = (96, 104, 90)          # salopeta murdara
    sac = (120, 196, 70)
    sac2 = (70, 140, 40)
    sw = 1 if frame == 1 else 0
    # picioare scurte si groase
    ll, rl = (13, 11) if frame == 0 else (11, 13)
    if frame == 2:
        ll, rl = 12, 12
    y0 = 46
    s.poly([(15, y0), (23, y0), (23, y0 + ll), (15, y0 + ll)], over, shade=2, hi=1)
    s.poly([(26, y0), (34, y0), (34, y0 + rl), (26, y0 + rl)], over, shade=2, hi=1)
    s.poly([(13, y0 + ll), (24, y0 + ll), (24, y0 + ll + 3), (12, y0 + ll + 3)], (40, 36, 30), shade=1)
    s.poly([(25, y0 + rl), (36, y0 + rl), (37, y0 + rl + 3), (25, y0 + rl + 3)], (40, 36, 30), shade=1)
    # burta umflata
    s.ellipse(24 + sw, 37, 14, 12, over, shade=4, hi=1)
    s.ellipse(24 + sw, 40, 9, 7, skin, shade=3, hi=1)          # piele iesita prin salopeta rupta
    for (x, y) in [(20, 37), (27, 39), (22, 43), (29, 44)]:
        s.set(x + sw, y, (110, 160, 60))                       # vene verzi
    s.rect(14 + sw, 29, 21, 1, mul(over, 0.75))
    # brate subtiri, atarnand
    s.poly([(8 + sw, 28), (13 + sw, 27), (12 + sw, 46), (7 + sw, 46)], skin2, shade=1, hi=1)
    s.poly([(35 + sw, 27), (40 + sw, 28), (41 + sw, 46), (36 + sw, 46)], skin2, shade=2)
    s.ellipse(9 + sw, 48, 3, 2.6, skin2)
    s.ellipse(39 + sw, 48, 3, 2.6, skin2)
    # sacul de acid de la gat
    big = 8.5 if frame != 2 else 6.5
    s.ellipse(24 + sw, 22, big, 6.5, sac, shade=3, hi=2)
    s.ellipse(21 + sw, 20, 2.2, 1.6, (200, 250, 150))
    for (x, y) in [(19, 24), (28, 21), (25, 26)]:
        s.set(x + sw, y, sac2)
    # cap mic, aplecat inainte
    hx = 24 + sw
    s.poly([(hx - 6, 8), (hx - 4, 5), (hx + 4, 5), (hx + 6, 8), (hx + 6, 14), (hx + 3, 17), (hx - 3, 17), (hx - 6, 14)],
           skin, shade=2, hi=1)
    s.rect(hx - 5, 8, 4, 3, (52, 30, 26))
    s.rect(hx + 1, 9, 4, 2, (52, 30, 26))
    s.set(hx - 3, 9, (236, 236, 140))
    s.set(hx + 2, 9, (236, 236, 140))
    s.rect(hx - 5, 7, 4, 1, skin2)
    if frame == 2:
        s.rect(hx - 4, 12, 8, 6, (20, 40, 10))                 # gura larg deschisa
        s.rect(hx - 3, 13, 6, 4, (90, 200, 50))
        s.rect(hx - 1, 17, 2, 5, (120, 220, 70))               # acid curgand
    else:
        s.poly([(hx - 4, 13), (hx + 4, 12), (hx + 3, 16), (hx - 3, 16)], (34, 20, 16))
        s.set(hx - 2, 13, (200, 190, 150))
        s.set(hx + 1, 13, (200, 190, 150))
        s.rect(hx, 16, 1, 3, (120, 220, 70))
    s.outline()
    return s


def spitter_dead():
    s = Spr(48, 64)
    for y in range(54, 64):
        for x in range(48):
            dx = (x - 24) / 22.0
            dy = (y - 59) / 4.5
            if dx * dx + dy * dy <= 1:
                s.set(x, y, (60 + (x * 7 + y * 3) % 9, 110 + (x * 5) % 13, 30))   # balta de acid
    s.ellipse(24, 54, 13, 6, (96, 104, 90), shade=2)
    s.ellipse(12, 52, 5, 4, (176, 182, 120), shade=1)
    s.ellipse(30, 52, 6, 3, (70, 140, 40))
    s.outline()
    return s


def boss2(frame):
    """Directorul Marcu dupa injectia cu VN-9: costum rupt, brat urias cu gheare."""
    s = Spr(64, 88)
    suit = (70, 72, 82)
    shirt = (220, 214, 200)
    tie = (150, 24, 30)
    flesh = (178, 92, 96)
    flesh2 = (132, 56, 66)
    skin = (190, 176, 160)
    sw = 0 if frame == 0 else 1
    # picioare in pantaloni de costum
    l1, l2 = (22, 19) if frame == 0 else (19, 22)
    s.poly([(20, 62), (30, 62), (29, 62 + l1), (19, 62 + l1)], suit, shade=3, hi=1)
    s.poly([(34, 62), (44, 62), (45, 62 + l2), (35, 62 + l2)], suit, shade=3, hi=1)
    s.poly([(17, 62 + l1), (30, 62 + l1), (30, 66 + l1), (15, 66 + l1)], (24, 22, 22))
    s.poly([(34, 62 + l2), (47, 62 + l2), (49, 66 + l2), (34, 66 + l2)], (24, 22, 22))
    # trunchi: camasa rupta, sacou pe jumatate, carne umflata pe dreapta
    s.poly([(14, 28), (32, 22), (48, 28), (46, 50), (44, 64), (20, 64), (16, 50)], shirt, shade=3, hi=1)
    s.poly([(14, 28), (24, 25), (22, 64), (17, 64), (15, 48)], suit, shade=2, hi=1)     # pola stanga
    s.poly([(29 + sw, 26), (33 + sw, 26), (34 + sw, 44), (31 + sw, 48), (28 + sw, 44)], tie, shade=1)
    s.ellipse(41, 38, 10, 12, flesh, shade=4, hi=1)                                      # carnea mutanta
    s.ellipse(43, 36, 4, 5, flesh2)
    for (x, y) in [(36, 30), (37, 31), (45, 44), (46, 45), (39, 47)]:
        s.set(x, y, (210, 140, 140))
    # bratul stang normal, in maneca de sacou
    s.poly([(8, 30), (16, 28), (15, 50), (8, 50)], suit, shade=2, hi=1)
    s.ellipse(11, 52, 3.5, 3, skin, shade=1)
    # bratul drept urias: in jos (A) sau ridicat (B)
    if frame == 0:
        s.poly([(44, 26), (58, 28), (62, 52), (60, 70), (48, 70), (46, 50)], flesh, shade=4, hi=2)
        s.poly([(50, 40), (56, 41), (57, 52), (51, 51)], flesh2)
        for k in range(4):
            x = 48 + k * 4
            s.poly([(x, 70), (x + 3, 70), (x + 2, 79), (x + 1, 79)], (236, 226, 200))
    else:
        s.poly([(44, 28), (56, 22), (62, 6), (54, 2), (48, 14), (42, 24)], flesh, shade=3, hi=2)
        s.poly([(50, 12), (55, 8), (57, 14), (52, 17)], flesh2)
        for k in range(4):
            x = 50 + k * 3
            s.poly([(x, 4), (x + 3, 4), (x + 4, -4), (x + 2, -4)], (236, 226, 200))
    # cap: chel, cu par grizonat pe laterale, ochelari sparti, fata umflata pe dreapta
    hx = 31 + sw
    s.poly([(hx - 8, 8), (hx - 5, 4), (hx + 5, 4), (hx + 8, 8), (hx + 9, 17), (hx + 5, 23), (hx - 5, 23), (hx - 8, 17)],
           skin, shade=3, hi=1)
    s.ellipse(hx + 6, 15, 4, 6, flesh, shade=1)
    s.rect(hx - 8, 9, 2, 6, (150, 150, 150))
    s.rect(hx + 7, 8, 2, 4, (150, 150, 150))
    s.rect(hx - 6, 12, 5, 4, (30, 30, 30))
    s.rect(hx + 1, 12, 5, 4, (30, 30, 30))
    s.rect(hx - 5, 13, 3, 2, (240, 200, 60))
    s.rect(hx + 2, 13, 3, 2, (240, 200, 60))
    s.set(hx + 4, 12, (200, 220, 230))
    s.set(hx + 3, 15, (200, 220, 230))
    s.rect(hx - 1, 13, 2, 1, (30, 30, 30))
    s.rect(hx - 4, 18, 9, 3, (40, 8, 10))
    for x in (hx - 3, hx - 1, hx + 1, hx + 3):
        s.set(x, 18, (230, 220, 196))
    s.rect(hx - 2, 21, 1, 4, (120, 20, 24))
    s.outline()
    return s


def boss2_dead():
    s = Spr(64, 88)
    for y in range(74, 88):
        for x in range(64):
            dx = (x - 32) / 30.0
            dy = (y - 81) / 6.5
            if dx * dx + dy * dy <= 1:
                s.set(x, y, (72 + (x * 5 + y * 3) % 9, 12, 14))
    s.ellipse(26, 76, 16, 6, (70, 72, 82), shade=3, hi=1)
    s.ellipse(46, 74, 14, 7, (178, 92, 96), shade=3, hi=1)
    s.ellipse(12, 73, 6, 5, (190, 176, 160), shade=1)
    for k in range(4):
        s.rect(54 + k * 2, 78, 1, 5, (236, 226, 200))
    s.outline()
    return s


def barrel(dead=False):
    s = Spr(32, 40)
    if dead:
        for y in range(30, 40):
            for x in range(32):
                dx = (x - 16) / 15.0
                dy = (y - 35) / 4.5
                if dx * dx + dy * dy <= 1:
                    s.set(x, y, (30 + (x * 3 + y * 5) % 12, 28, 26))
        s.poly([(8, 28), (13, 24), (16, 30), (21, 23), (25, 30), (24, 36), (9, 36)], (60, 40, 36), shade=2)
        s.poly([(12, 30), (15, 27), (17, 32), (14, 34)], (230, 130, 40))
        s.outline()
        return s
    red = (176, 36, 32)
    s.poly([(6, 4), (26, 4), (27, 38), (5, 38)], red, shade=6, hi=3, dark=0.62, light=1.25)
    s.ellipse(16, 4, 10, 2.5, (140, 28, 26))
    s.ellipse(16, 4, 7, 1.5, (90, 18, 16))
    s.rect(21, 3, 3, 1, (60, 60, 60))
    for y in (12, 28):
        s.poly([(5, y), (27, y), (27, y + 2), (5, y + 2)], (120, 24, 22))
    # simbol de inflamabil
    s.poly([(16, 15), (21, 20), (16, 25), (11, 20)], (232, 196, 40))
    s.poly([(16, 17), (18, 21), (16, 23), (14, 21)], (30, 20, 10))
    s.outline()
    return s


def explosion(frame):
    s = Spr(48, 48)
    r_out = [9, 21, 23][frame]
    for y in range(48):
        for x in range(48):
            dx, dy = x - 24, y - 26
            a = math.atan2(dy, dx)
            r = math.sqrt(dx * dx + dy * dy)
            rr = r_out * (0.82 + 0.18 * math.sin(a * 7 + frame * 2))
            if r > rr:
                continue
            f = r / rr
            if frame == 2:
                h = (x * 2654435761 + y * 40503) & 0xFFFF
                g = 46 + (h % 22)
                c = (g + 8, g, g - 4) if f > 0.4 else (120 + h % 40, 60, 28)
                if h % 37 == 0:
                    c = (255, 190, 70)
            elif f < 0.3:
                c = (255, 255, 230)
            elif f < 0.55:
                c = (255, 230, 110)
            elif f < 0.8:
                c = (255, 150, 40)
            else:
                c = (210, 70, 20)
            s.set(x, y, c)
    return s


def acid():
    s = Spr(16, 16)
    s.ellipse(8, 8, 5.5, 5, (110, 220, 60), shade=2, hi=1)
    s.ellipse(6.5, 6.5, 2, 1.6, (220, 255, 170))
    s.rect(8, 13, 1, 2, (110, 220, 60))
    s.outline((20, 50, 10))
    return s


def item_smg():
    s = Spr(24, 24)
    s.poly([(2, 9), (19, 9), (19, 13), (2, 13)], (52, 54, 60), shade=1, hi=1)
    s.poly([(19, 10), (23, 10), (23, 12), (19, 12)], (40, 40, 44))
    s.poly([(9, 13), (13, 13), (12, 22), (8, 22)], (36, 36, 40), shade=1)
    s.poly([(15, 13), (18, 13), (19, 18), (16, 18)], (36, 36, 40))
    s.poly([(0, 10), (3, 10), (3, 15), (0, 15)], (36, 36, 40))
    s.outline()
    return s


def item_smg_ammo():
    s = Spr(24, 24)
    for k in range(2):
        x = 5 + k * 8
        s.poly([(x, 3), (x + 5, 3), (x + 6, 21), (x + 1, 21)], (52, 54, 60), shade=2, hi=1)
        s.rect(x + 1, 2, 3, 2, (206, 158, 62))
        s.rect(x + 1, 8, 4, 1, (80, 82, 90))
        s.rect(x + 1, 14, 4, 1, (80, 82, 90))
    s.outline()
    return s


def gun_smg():
    s = Spr(48, 34)
    skin = (214, 160, 120)
    sleeve = (40, 58, 92)
    metal = (52, 54, 62)
    # teava si corpul, vazute din spate
    s.poly([(21, 0), (27, 0), (28, 8), (20, 8)], (38, 38, 44), shade=1, hi=1)
    s.rect(23, 0, 2, 1, (238, 238, 226))
    s.poly([(17, 7), (31, 7), (32, 22), (16, 22)], metal, shade=3, hi=2)
    for y in range(10, 20, 3):
        s.rect(19, y, 10, 1, (36, 36, 42))
    s.rect(27, 9, 3, 4, (24, 24, 28))
    s.poly([(18, 4), (21, 4), (21, 8), (18, 8)], (30, 30, 34))       # inaltator
    s.poly([(27, 4), (30, 4), (30, 8), (27, 8)], (30, 30, 34))
    # incarcatorul lung, in jos-stanga, tinut cu mana stanga
    s.poly([(15, 20), (21, 20), (19, 30), (13, 30)], (30, 30, 34), shade=1)
    s.poly([(8, 21), (17, 19), (18, 26), (9, 28)], skin, shade=2, hi=1)
    s.poly([(1, 25), (10, 22), (12, 30), (2, 33)], sleeve, shade=2, hi=1)
    s.rect(0, 30, 7, 4, sleeve)
    # mana dreapta pe maner, jos-dreapta
    s.poly([(26, 22), (36, 21), (37, 28), (27, 30)], skin, shade=3, hi=1)
    s.poly([(30, 27), (42, 25), (46, 34), (32, 34)], sleeve, shade=3, hi=1)
    s.rect(33, 31, 12, 1, (214, 210, 96))
    s.outline()
    return s




# ---------------------------------------------------------------- armele la persoana întâi (Tura de Noapte 2)
SLEEVE2 = (38, 50, 80)        # geaca de ambulanțier, bleumarin
STRIPE2 = (206, 206, 176)     # banda reflectorizantă
SKIN2 = (214, 166, 132)


def gun_pistol2():
    """Pistolul ținut în mâna dreaptă, văzut din spate (84 x 72)."""
    s = Spr(84, 72)
    gm = (64, 66, 74)
    # mâneca, din colțul din dreapta jos
    s.poly([(44, 72), (50, 60), (66, 54), (84, 60), (84, 72)], SLEEVE2, shade=5, hi=2)
    s.poly([(58, 63), (76, 58), (78, 62), (60, 67)], STRIPE2)
    # mânerul (înclinat spre dreapta jos)
    s.poly([(33, 42), (51, 42), (56, 64), (38, 64)], (36, 36, 40), shade=2, hi=1)
    # închizătorul, în perspectivă
    s.poly([(35, 7), (49, 7), (53, 42), (31, 42)], gm, shade=4, hi=2)
    s.poly([(35, 7), (49, 7), (50, 11), (34, 11)], (96, 98, 108))
    for x in range(33, 52, 2):
        s.rect(x, 33, 1, 7, (40, 40, 46))
    s.rect(46, 15, 4, 8, (30, 30, 34))
    s.rect(47, 16, 2, 2, (196, 160, 70))
    # înălțătorul cu crestătură, cătarea și punctele luminoase
    s.rect(32, 3, 20, 5, (34, 34, 38))
    s.rect(40, 3, 4, 4, None)
    s.rect(41, 0, 2, 4, (40, 40, 44))
    s.set(41, 1, (120, 240, 140))
    s.set(42, 1, (120, 240, 140))
    s.set(35, 5, (120, 240, 140))
    s.set(48, 5, (120, 240, 140))
    # mâna: dosul palmei în dreapta, degetul mare pe stânga
    s.poly([(46, 40), (61, 45), (67, 58), (60, 68), (46, 66), (43, 52)], SKIN2, shade=5, hi=1)
    for (x, y, w) in [(50, 47, 5), (53, 50, 5), (56, 53, 5)]:
        s.rect(x, y, w, 1, mul(SKIN2, 0.76))
    s.poly([(29, 40), (35, 37), (37, 49), (31, 51)], SKIN2, shade=2, hi=1)
    s.set(33, 38, (236, 212, 196))
    s.set(34, 38, (236, 212, 196))
    # încheietura și manșeta
    s.poly([(44, 62), (58, 66), (56, 72), (44, 72)], mul(SKIN2, 0.9), shade=2)
    s.poly([(48, 66), (62, 60), (68, 66), (56, 72), (50, 72)], SLEEVE2, shade=3, hi=1)
    s.outline()
    return s


def scale2x(sp):
    """Mărire de 2 ori care rotunjește colțurile (algoritmul Scale2x / EPX)."""
    r = Spr(sp.w * 2, sp.h * 2)
    for y in range(sp.h):
        for x in range(sp.w):
            p = sp.get(x, y)
            a, b, c, d = sp.get(x, y - 1), sp.get(x + 1, y), sp.get(x - 1, y), sp.get(x, y + 1)
            e0 = e1 = e2 = e3 = p
            if c == a and c != d and a != b:
                e0 = a
            if a == b and a != c and b != d:
                e1 = b
            if d == c and d != b and c != a:
                e2 = c
            if b == d and b != a and d != c:
                e3 = d
            r.c[y * 2][x * 2] = e0
            r.c[y * 2][x * 2 + 1] = e1
            r.c[y * 2 + 1][x * 2] = e2
            r.c[y * 2 + 1][x * 2 + 1] = e3
    return r

# ---------------------------------------------------------------- decor (Tura de Noapte 2)
# Fiecare obiect e desenat în mărimea lui; în decor.png stă jos, centrat, într-o celulă de 64x64.
WOOD = (120, 84, 52)
DARKM = (52, 54, 58)
METAL = (150, 154, 160)


def d_desk():
    s = Spr(64, 40)
    s.poly([(2, 18), (62, 18), (62, 22), (2, 22)], (150, 120, 84), shade=0, hi=0)      # blat
    s.rect(2, 18, 60, 1, (186, 156, 116))
    s.poly([(4, 22), (22, 22), (22, 40), (4, 40)], (124, 96, 66), shade=2, hi=1)      # sertare
    for y in (26, 31, 36):
        s.rect(6, y, 14, 1, (90, 68, 46))
        s.rect(11, y + 2, 4, 1, (200, 196, 180))
    s.rect(56, 22, 4, 18, (100, 78, 54))
    s.rect(22, 22, 34, 12, (70, 56, 40))                                             # spate (umbră)
    # monitor
    s.poly([(30, 0), (54, 0), (54, 15), (30, 15)], (36, 36, 40), shade=1, hi=1)
    s.rect(32, 2, 20, 11, (40, 70, 120))
    s.rect(32, 2, 20, 2, (120, 170, 230))
    s.rect(34, 6, 12, 1, (170, 200, 240))
    s.rect(34, 9, 8, 1, (170, 200, 240))
    s.rect(40, 15, 4, 3, (30, 30, 34))
    s.rect(28, 16, 22, 2, (200, 200, 196))                                           # tastatură
    s.rect(8, 15, 12, 3, (236, 234, 226))                                            # hârtii
    s.rect(9, 14, 10, 1, (220, 218, 210))
    s.rect(56, 13, 4, 5, (180, 40, 40))                                              # cană
    s.outline()
    return s


def d_chair():
    s = Spr(28, 36)
    s.poly([(6, 2), (22, 2), (21, 16), (7, 16)], (40, 40, 46), shade=2, hi=1)
    s.poly([(4, 17), (24, 17), (24, 21), (4, 21)], (46, 46, 52), shade=1)
    s.rect(13, 21, 2, 8, (90, 90, 96))
    s.rect(5, 29, 18, 2, (70, 70, 76))
    for x in (5, 13, 21):
        s.rect(x, 31, 3, 2, (30, 30, 30))
    s.outline()
    return s


def d_reception():
    s = Spr(64, 32)
    s.poly([(0, 6), (64, 6), (64, 32), (0, 32)], (200, 196, 186), shade=0)
    s.rect(0, 6, 64, 3, (70, 64, 58))
    s.rect(0, 9, 64, 1, (40, 36, 32))
    for x in range(0, 64, 16):
        s.rect(x, 10, 1, 22, (170, 166, 156))
    s.rect(0, 17, 64, 4, (230, 130, 40))                                             # banda portocalie ZORI
    s.rect(0, 17, 64, 1, (250, 170, 70))
    s.rect(0, 29, 64, 3, (120, 116, 108))
    s.rect(8, 0, 14, 7, (30, 30, 34))                                                # monitor recepție
    s.rect(9, 1, 12, 4, (60, 100, 150))
    s.rect(40, 3, 14, 3, (236, 232, 222))                                            # registru
    s.outline()
    return s


def d_sofa():
    s = Spr(64, 30)
    col = (120, 46, 50)
    s.poly([(4, 0), (60, 0), (60, 14), (4, 14)], mul(col, 0.9), shade=2, hi=1)
    s.poly([(0, 8), (8, 8), (8, 26), (0, 26)], mul(col, 0.8), shade=1)
    s.poly([(56, 8), (64, 8), (64, 26), (56, 26)], mul(col, 0.8), shade=1)
    s.poly([(8, 13), (56, 13), (56, 24), (8, 24)], col, shade=1, hi=1)
    s.rect(31, 13, 1, 11, mul(col, 0.7))
    s.rect(4, 26, 3, 4, (30, 24, 20))
    s.rect(57, 26, 3, 4, (30, 24, 20))
    s.rect(20, 4, 4, 3, mul(col, 0.7))                                               # rupt
    s.outline()
    return s


def d_plant():
    s = Spr(32, 56)
    s.poly([(9, 42), (23, 42), (21, 56), (11, 56)], (170, 90, 60), shade=2, hi=1)
    s.rect(8, 41, 16, 3, (190, 110, 74))
    leaf = [(40, 110, 50), (60, 140, 60), (30, 90, 44)]
    for k in range(16):
        a = -math.pi / 2 + (k - 7.5) * 0.22
        ln = 14 + (k * 7) % 12
        x0, y0 = 16, 42
        for r in range(ln):
            x = int(x0 + math.cos(a) * r * 0.9)
            y = int(y0 + math.sin(a) * r * 1.6 + (r * r) * 0.025)
            s.set(x, y, leaf[k % 3])
            s.set(x + 1, y, leaf[(k + 1) % 3])
    s.outline()
    return s


def d_camera():
    s = Spr(40, 64)
    s.rect(18, 30, 4, 26, (70, 70, 74))                                              # coloana
    s.poly([(4, 56), (36, 56), (32, 60), (8, 60)], (60, 60, 64), shade=1)          # piedestal
    for x in (6, 18, 32):
        s.rect(x, 60, 4, 4, (24, 24, 24))
    s.poly([(6, 10), (30, 10), (30, 28), (6, 28)], (54, 56, 62), shade=2, hi=1)    # corpul camerei
    s.poly([(30, 13), (38, 12), (38, 24), (30, 24)], (30, 30, 34), shade=1)        # obiectiv
    s.ellipse(37, 18, 2.2, 5, (90, 110, 140))
    s.poly([(2, 4), (24, 4), (26, 12), (2, 12)], (34, 34, 38), shade=1)            # parasolar prompter
    s.rect(6, 14, 14, 9, (80, 120, 160))
    s.rect(7, 15, 12, 2, (180, 210, 240))
    s.rect(22, 15, 4, 3, (200, 30, 30))                                              # tally roșu
    s.rect(0, 28, 10, 2, (90, 90, 96))                                               # mânere
    s.rect(30, 28, 10, 2, (90, 90, 96))
    s.outline()
    return s


def d_softbox():
    s = Spr(36, 64)
    for (x0, y0, x1, y1) in [(18, 26, 6, 63), (18, 26, 30, 63), (18, 26, 18, 63)]:
        n = 40
        for k in range(n):
            x = x0 + (x1 - x0) * k // n
            y = y0 + (y1 - y0) * k // n
            s.set(x, y, (80, 80, 84))
    s.rect(17, 18, 2, 10, (90, 90, 94))
    s.poly([(2, 0), (34, 0), (30, 20), (6, 20)], (30, 30, 34), shade=2)
    s.poly([(5, 2), (31, 2), (28, 17), (8, 17)], (248, 244, 230), shade=0)
    s.rect(8, 4, 20, 2, (255, 255, 250))
    s.outline()
    return s


def d_newsdesk():
    s = Spr(64, 36)
    s.poly([(0, 6), (64, 6), (62, 36), (2, 36)], (24, 32, 60), shade=0)
    s.rect(0, 6, 64, 3, (190, 194, 200))
    s.rect(4, 14, 56, 12, (30, 70, 150))
    s.rect(4, 14, 56, 1, (110, 170, 240))
    for x in range(6, 58, 4):
        s.rect(x, 17, 2, 6, (60, 120, 210))
    s.rect(20, 0, 10, 6, (220, 220, 214))                                            # foi
    s.rect(44, 1, 4, 5, (40, 40, 44))                                                # cană / microfon
    s.outline()
    return s


def d_vending():
    s = Spr(40, 64)
    s.poly([(2, 0), (38, 0), (38, 64), (2, 64)], (150, 30, 34), shade=3, hi=2)
    s.rect(6, 6, 22, 44, (20, 24, 30))
    for r in range(5):
        for k in range(4):
            s.rect(8 + k * 5, 9 + r * 8, 4, 5, [(230, 200, 60), (60, 160, 220), (220, 60, 60), (120, 200, 90)][(r + k) % 4])
        s.rect(7, 15 + r * 8, 20, 1, (90, 96, 104))
    s.rect(30, 8, 6, 10, (40, 40, 44))
    for k in range(3):
        s.rect(31, 20 + k * 4, 4, 2, (200, 200, 196))
    s.rect(8, 54, 20, 6, (20, 20, 22))
    s.rect(8, 2, 22, 3, (250, 240, 230))
    s.outline()
    return s


def d_cooler():
    s = Spr(22, 48)
    s.ellipse(11, 7, 7, 7, (130, 180, 230), shade=2, hi=1)
    s.rect(5, 10, 12, 4, (130, 180, 230))
    s.poly([(3, 14), (19, 14), (19, 48), (3, 48)], (226, 226, 220), shade=2, hi=1)
    s.rect(6, 20, 3, 3, (60, 120, 220))
    s.rect(13, 20, 3, 3, (220, 60, 60))
    s.rect(5, 26, 12, 3, (150, 150, 146))
    s.outline()
    return s


def d_boxes():
    s = Spr(44, 40)
    c = (156, 120, 74)
    s.poly([(0, 16), (24, 16), (24, 40), (0, 40)], c, shade=2, hi=1)
    s.poly([(22, 20), (44, 20), (44, 40), (22, 40)], mul(c, 0.92), shade=2, hi=1)
    s.poly([(6, 0), (30, 0), (30, 17), (6, 17)], mul(c, 1.05), shade=2, hi=1)
    for (x, y, w) in [(0, 27, 24), (22, 29, 22), (6, 8, 24)]:
        s.rect(x, y, w, 2, (200, 180, 120))
    s.rect(9, 3, 10, 4, (236, 232, 220))
    s.rect(26, 23, 8, 3, (236, 232, 220))
    s.outline()
    return s


def d_rack():
    s = Spr(56, 56)
    s.rect(2, 6, 52, 2, (170, 172, 176))
    s.rect(4, 6, 2, 46, (150, 152, 156))
    s.rect(50, 6, 2, 46, (150, 152, 156))
    s.rect(0, 52, 56, 2, (120, 122, 126))
    cols = [(160, 40, 50), (40, 80, 150), (220, 200, 180), (40, 40, 44), (90, 140, 70), (200, 150, 40)]
    x = 7
    k = 0
    while x < 48:
        w = 5 + (k * 3) % 4
        c = cols[k % len(cols)]
        s.poly([(x + 1, 8), (x + w - 1, 8), (x + w, 40 + (k % 3) * 3), (x, 40 + (k % 3) * 3)], c, shade=1, hi=1)
        s.rect(x + w // 2, 6, 1, 3, (200, 200, 200))
        x += w
        k += 1
    for x in (2, 50):
        s.rect(x, 54, 4, 2, (30, 30, 30))
    s.outline()
    return s


def d_bin():
    s = Spr(22, 28)
    s.poly([(2, 4), (20, 4), (18, 28), (4, 28)], (70, 90, 80), shade=2, hi=1)
    s.rect(1, 2, 20, 3, (90, 110, 100))
    s.rect(6, 0, 8, 3, (230, 228, 220))
    s.rect(12, 1, 6, 2, (200, 60, 60))
    s.outline()
    return s


def d_table():
    s = Spr(60, 34)
    s.poly([(0, 10), (60, 10), (60, 15), (0, 15)], (210, 208, 200), shade=0)
    s.rect(0, 10, 60, 1, (240, 240, 236))
    s.rect(28, 15, 4, 15, (120, 122, 126))
    s.rect(18, 30, 24, 3, (100, 102, 106))
    for (x, flip) in [(2, False), (44, True)]:
        s.rect(x + 2, 2, 12, 10, (60, 80, 120))
        s.rect(x + 2, 15, 12, 3, (60, 80, 120))
        s.rect(x + 3, 18, 2, 14, (90, 90, 96))
        s.rect(x + 11, 18, 2, 14, (90, 90, 96))
    s.rect(22, 6, 6, 4, (236, 236, 230))
    s.rect(34, 7, 4, 3, (130, 60, 40))
    s.outline()
    return s


def d_case():
    s = Spr(40, 30)
    s.poly([(0, 4), (40, 4), (40, 30), (0, 30)], (40, 40, 44), shade=2, hi=1)
    for (x, y) in [(0, 4), (36, 4), (0, 26), (36, 26)]:
        s.rect(x, y, 4, 4, (170, 172, 176))
    s.rect(0, 15, 40, 2, (150, 152, 156))
    s.rect(14, 0, 12, 4, (60, 60, 64))
    s.rect(8, 8, 14, 4, (230, 200, 60))
    s.outline()
    return s


def d_monitor_stand():
    s = Spr(32, 56)
    s.rect(15, 24, 3, 28, (80, 80, 84))
    s.poly([(4, 52), (28, 52), (26, 56), (6, 56)], (60, 60, 64))
    s.poly([(0, 0), (32, 0), (32, 24), (0, 24)], (30, 30, 34), shade=1, hi=1)
    s.rect(2, 2, 28, 18, (12, 12, 14))
    for y in range(2, 20):
        for x in range(2, 30):
            if (x * 7 + y * 13) % 5 == 0:
                s.set(x, y, (60, 60, 70))
    s.rect(4, 4, 10, 4, (200, 30, 30))
    s.outline()
    return s


def d_makeup_chair():
    s = Spr(32, 44)
    s.poly([(6, 0), (26, 0), (26, 20), (6, 20)], (30, 30, 34), shade=2, hi=1)
    s.poly([(2, 20), (30, 20), (30, 26), (2, 26)], (40, 40, 46), shade=1)
    s.rect(0, 16, 4, 10, (60, 60, 64))
    s.rect(28, 16, 4, 10, (60, 60, 64))
    s.rect(14, 26, 4, 12, (170, 172, 176))
    s.ellipse(16, 40, 10, 3, (120, 122, 126))
    s.outline()
    return s


def d_ficus():
    s = Spr(40, 64)
    s.poly([(12, 50), (28, 50), (26, 64), (14, 64)], (60, 60, 64), shade=2, hi=1)
    s.rect(19, 24, 2, 27, (90, 66, 40))
    for k in range(70):
        a = (k * 2.399) % (2 * math.pi)
        r = 4 + (k * 37 % 15)
        x = int(20 + math.cos(a) * r)
        y = int(20 + math.sin(a) * r * 0.9)
        c = [(40, 100, 46), (56, 130, 58), (30, 80, 38), (70, 150, 70)][k % 4]
        s.rect(x, y, 3, 2, c)
    s.outline()
    return s


def d_cart():
    s = Spr(44, 40)
    s.rect(2, 8, 40, 2, (160, 162, 166))
    s.rect(2, 24, 40, 2, (160, 162, 166))
    s.rect(2, 8, 2, 28, (140, 142, 146))
    s.rect(40, 8, 2, 28, (140, 142, 146))
    for k in range(9):
        s.rect(5 + k * 4, 0 + (k % 2), 3, 8 - (k % 2), [(30, 30, 34), (200, 196, 180), (60, 60, 70)][k % 3])
        s.rect(5 + k * 4, 14 + (k % 3), 3, 10 - (k % 3), [(40, 40, 44), (170, 160, 140)][k % 2])
    s.ellipse(6, 37, 3, 3, (30, 30, 30))
    s.ellipse(38, 37, 3, 3, (30, 30, 30))
    s.outline()
    return s


def d_sheet_body():
    s = Spr(56, 18)
    for y in range(10, 18):
        for x in range(56):
            dx = (x - 28) / 27.0
            dy = (y - 14) / 3.8
            if dx * dx + dy * dy <= 1:
                s.set(x, y, (80, 12, 12))
    s.poly([(4, 6), (50, 4), (54, 12), (2, 14)], (210, 210, 204), shade=1, hi=1)
    s.ellipse(8, 8, 6, 4, (220, 220, 214), shade=1)
    s.rect(30, 6, 6, 3, (150, 30, 30))
    s.rect(52, 10, 3, 3, (150, 150, 140))
    s.outline()
    return s


DECOR = [
    # nume, funcție, înălțime în lume (1 = cât peretele), solid
    ("birou", d_desk, 0.62, 1),
    ("scaun", d_chair, 0.55, 0),
    ("receptie", d_reception, 0.52, 1),
    ("canapea", d_sofa, 0.42, 1),
    ("planta", d_plant, 0.8, 1),
    ("camera", d_camera, 1.0, 1),
    ("lumina", d_softbox, 1.08, 1),
    ("masa_stiri", d_newsdesk, 0.56, 1),
    ("automat", d_vending, 1.0, 1),
    ("apa", d_cooler, 0.76, 1),
    ("cutii", d_boxes, 0.62, 1),
    ("haine", d_rack, 0.92, 1),
    ("cos", d_bin, 0.36, 0),
    ("masa", d_table, 0.5, 1),
    ("lada", d_case, 0.42, 1),
    ("monitor", d_monitor_stand, 0.9, 1),
    ("scaun_machiaj", d_makeup_chair, 0.62, 1),
    ("ficus", d_ficus, 1.0, 1),
    ("carucior", d_cart, 0.6, 1),
    ("cearsaf", d_sheet_body, 0.24, 0),
]


def decor_sheet(out):
    import os
    cols = 8
    rows = (len(DECOR) + cols - 1) // cols
    im = Image.new("RGBA", (64 * cols, 64 * rows), (0, 0, 0, 0))
    lines = []
    for i, (name, fn, h, solid) in enumerate(DECOR):
        sp = fn()
        x = (i % cols) * 64 + (64 - sp.w) // 2
        y = (i // cols) * 64 + 64 - sp.h
        im.paste(sp.image(), (x, y))
        lines.append("%d %s %d %d %.2f %d" % (i, name, sp.w, sp.h, h, solid))
    im.save(os.path.join(out, "decor.png"), optimize=True)
    with open(os.path.join(out, "decor.txt"), "w") as f:
        f.write("\n".join(lines) + "\n")

if __name__ == "__main__":
    import os
    os.makedirs(OUT, exist_ok=True)
    zs = []
    for t in TYPES:
        if t["name"] in ("pacient", "asistenta", "medic"):
            continue
        if t["name"] in ALIVE:
            zs += [humanoid(t, 0, True), humanoid(t, 1, True), None, None]
        else:
            zs += [humanoid(t, 0), humanoid(t, 1), humanoid_dead(t), None]
    zs += [spitter(0), spitter(1), spitter_dead(), spitter(2)]
    sheet(zs, 4, 48, 64).save(os.path.join(OUT, "z.png"), optimize=True)
    sheet([boss2(0), boss2(1), boss2_dead()], 3).save(os.path.join(OUT, "boss2.png"), optimize=True)
    sheet([barrel(), barrel(True)], 2).save(os.path.join(OUT, "barrel.png"), optimize=True)
    sheet([explosion(0), explosion(1), explosion(2)], 3).save(os.path.join(OUT, "expl.png"), optimize=True)
    acid().image().save(os.path.join(OUT, "acid.png"), optimize=True)
    sheet([boss(0), boss(1), boss_dead()], 3).save(os.path.join(OUT, "boss.png"), optimize=True)
    items = [item_ammo(), item_med(), item_card((40, 88, 216)), item_card((200, 36, 36)), item_note(),
             item_shotgun(), item_shells(), item_key_yellow(), item_card((36, 160, 70)),
             item_smg(), item_smg_ammo()]
    sheet(items, 11).save(os.path.join(OUT, "items.png"), optimize=True)
    # armele noi: pistolul desenat din nou, restul mărite cu Scale2x
    gun_pistol2().image().save(os.path.join(OUT, "gun.png"), optimize=True)
    scale2x(gun_shotgun()).image().save(os.path.join(OUT, "gun2.png"), optimize=True)
    scale2x(gun_smg()).image().save(os.path.join(OUT, "gun3.png"), optimize=True)
    scale2x(flash(24, 20)).image().save(os.path.join(OUT, "flash.png"), optimize=True)
    scale2x(flash(32, 24)).image().save(os.path.join(OUT, "flash2.png"), optimize=True)
    scale2x(flash(20, 16)).image().save(os.path.join(OUT, "flash3.png"), optimize=True)
    decor_sheet(OUT)
    print("ok")
