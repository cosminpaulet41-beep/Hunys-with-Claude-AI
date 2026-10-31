"""Uneltele de desen pentru texturi (64x64 pixeli, desenate din cod) și atlasul jocului.
Texturile Spitalului Universitar sunt în tex_spital.py.

Ieșire: assets/tex.png = atlas de 8 coloane și assets/tex.txt cu numele fiecărei texturi (Art.java le caută
după nume), plus assets/sky.png (cerul). Rulează: python3 -I tools/textures.py assets [previzualizare.png]
"""
import math
import random
import sys

from PIL import Image, ImageDraw

S = 64
rnd = random.Random(2026)


def clamp(v):
    return max(0, min(255, int(v)))


def mul(c, f):
    return tuple(clamp(v * f) for v in c)


def mix(a, b, t):
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3))


class T:
    def __init__(s, base=(128, 128, 128), w=S, h=S):
        s.w, s.h = w, h
        s.im = Image.new("RGB", (w, h), base)
        s.px = s.im.load()
        s.d = ImageDraw.Draw(s.im)

    def get(s, x, y):
        return s.px[x % s.w, y % s.h]

    def set(s, x, y, c):
        if 0 <= x < s.w and 0 <= y < s.h:
            s.px[x, y] = tuple(clamp(v) for v in c)

    def rect(s, x, y, w, h, c):
        for j in range(y, y + h):
            for i in range(x, x + w):
                s.set(i, j, c)

    def frame(s, x, y, w, h, c):
        s.rect(x, y, w, 1, c)
        s.rect(x, y + h - 1, w, 1, c)
        s.rect(x, y, 1, h, c)
        s.rect(x + w - 1, y, 1, h, c)

    def bevel(s, x, y, w, h, hi, lo):
        s.rect(x, y, w, 1, hi)
        s.rect(x, y, 1, h, hi)
        s.rect(x, y + h - 1, w, 1, lo)
        s.rect(x + w - 1, y, 1, h, lo)

    def noise(s, amp, x=0, y=0, w=None, h=None, seed=None):
        r = random.Random(seed) if seed is not None else rnd
        w = s.w if w is None else w
        h = s.h if h is None else h
        for j in range(y, y + h):
            for i in range(x, x + w):
                c = s.get(i, j)
                n = r.randint(-amp, amp)
                s.set(i, j, (c[0] + n, c[1] + n, c[2] + n))

    def speckle(s, n, cols, x=0, y=0, w=None, h=None):
        w = s.w if w is None else w
        h = s.h if h is None else h
        for _ in range(n):
            s.set(x + rnd.randrange(w), y + rnd.randrange(h), rnd.choice(cols))

    def shade_v(s, top, bottom, x=0, y=0, w=None, h=None):
        """Înmulțește vertical cu un gradient (top..bottom)."""
        w = s.w if w is None else w
        h = s.h if h is None else h
        for j in range(y, y + h):
            f = top + (bottom - top) * (j - y) / max(1, h - 1)
            for i in range(x, x + w):
                s.set(i, j, mul(s.get(i, j), f))

    def grime(s, n, strength=0.82, size=3):
        for _ in range(n):
            cx, cy = rnd.randrange(s.w), rnd.randrange(s.h)
            r = rnd.randint(1, size)
            for j in range(-r, r + 1):
                for i in range(-r, r + 1):
                    if i * i + j * j <= r * r and rnd.random() < 0.7:
                        x, y = (cx + i) % s.w, (cy + j) % s.h
                        s.set(x, y, mul(s.get(x, y), strength + rnd.random() * 0.08))

    def text(s, x, y, txt, col, font=None):
        draw_text(s, x, y, txt, col)


# ---------------------------------------------------------------- mini-font 3x5 pentru inscripții
F35 = {
    'A': "010101111101101", 'B': "110101110101110", 'C': "011100100100011", 'D': "110101101101110",
    'E': "111100110100111", 'F': "111100110100100", 'G': "011100101101011", 'H': "101101111101101",
    'I': "111010010010111", 'J': "001001001101010", 'K': "101101110101101", 'L': "100100100100111",
    'M': "101111111101101", 'N': "110101101101101", 'O': "010101101101010", 'P': "110101110100100",
    'Q': "010101101110011", 'R': "110101110101101", 'S': "011100010001110", 'T': "111010010010010",
    'U': "101101101101111", 'V': "101101101101010", 'W': "101101111111101", 'X': "101101010101101",
    'Y': "101101010010010", 'Z': "111001010100111", '0': "111101101101111", '1': "010110010010111",
    '2': "110001010100111", '3': "110001010001110", '4': "101101111001001", '5': "111100110001110",
    '6': "011100110101010", '7': "111001010010010", '8': "010101010101010", '9': "010101011001110",
    '-': "000000111000000", '.': "000000000000010", ' ': "000000000000000", '!': "010010010000010",
    '/': "001001010100100", ':': "000010000010000", '+': "000010111010000",
}


# litere mai late, ca să se citească (N, M, W)
WIDE = {
    'N': ["1001", "1101", "1011", "1001", "1001"],
    'M': ["10001", "11011", "10101", "10001", "10001"],
    'W': ["10001", "10001", "10101", "11011", "10001"],
}


def glyph(ch):
    if ch in WIDE:
        return WIDE[ch]
    g = F35.get(ch, F35[' '])
    return [g[j * 3:j * 3 + 3] for j in range(5)]


def draw_text(t, x, y, txt, col):
    for ch in txt.upper():
        g = glyph(ch)
        for j in range(5):
            for i, b in enumerate(g[j]):
                if b == '1':
                    t.set(x + i, y + j, col)
        x += len(g[0]) + 1


def text_w(txt):
    return sum(len(glyph(c)[0]) + 1 for c in txt.upper()) - 1


# ---------------------------------------------------------------- pereți
WALL_UP = (176, 170, 152)
WALL_LO = (96, 112, 106)


def office_wall(variant=0):
    t = T(WALL_UP)
    t.noise(5)
    # jumătatea de jos vopsită închis + brâu
    t.rect(0, 38, S, 26, WALL_LO)
    t.noise(4, 0, 38, S, 26)
    t.rect(0, 36, S, 2, (204, 198, 180))
    t.rect(0, 38, S, 1, (70, 80, 76))
    t.rect(0, 59, S, 5, (44, 42, 40))
    t.rect(0, 59, S, 1, (70, 66, 60))
    # îmbinări de panou
    for x in (0, 32):
        t.rect(x, 0, 1, 36, mul(WALL_UP, 0.86))
    t.grime(10, 0.9, 2)
    for x in range(S):
        if rnd.random() < 0.5:
            t.set(x, 58, mul(t.get(x, 58), 0.8))
    if variant == 1:  # tablou înrămat
        t.rect(18, 8, 28, 20, (58, 44, 30))
        t.rect(20, 10, 24, 16, (96, 120, 150))
        for x in range(20, 44):
            h = 4 + int(3 * math.sin(x * 0.5)) + (x * 7 % 5)
            t.rect(x, 26 - h, 1, h, (60, 76, 70))
        t.rect(20, 10, 24, 5, (190, 140, 110))
        t.bevel(18, 8, 28, 20, (88, 70, 48), (34, 26, 18))
    elif variant == 2:  # stingător + semn
        t.rect(26, 8, 12, 9, (200, 40, 36))
        draw_text(t, 27, 10, "PSI", (240, 230, 220))
        t.rect(28, 40, 8, 17, (178, 28, 28))
        t.rect(29, 41, 2, 14, (220, 70, 60))
        t.rect(29, 37, 6, 3, (40, 40, 40))
        t.rect(35, 37, 3, 6, (30, 30, 30))
        t.rect(28, 50, 8, 3, (230, 220, 200))
    elif variant == 3:  # palmă de sânge și dâră
        blood = (110, 20, 18)
        for (x, y) in [(30, 20), (31, 20), (32, 19), (33, 20), (34, 21), (29, 23), (30, 22), (31, 22), (32, 22),
                       (33, 22), (34, 23), (30, 24), (31, 24), (32, 24), (33, 24), (31, 25), (32, 25), (33, 26),
                       (28, 21), (35, 19), (27, 22)]:
            t.set(x, y, blood)
        for y in range(25, 50):
            for k in range(3):
                if rnd.random() < 0.8:
                    t.set(31 + k + (y // 9), y, mul(blood, 0.8 + rnd.random() * 0.3))
    elif variant == 4:  # prize și cablu
        t.rect(10, 46, 8, 6, (214, 210, 196))
        t.set(12, 48, (40, 40, 40))
        t.set(15, 48, (40, 40, 40))
        t.rect(14, 52, 1, 7, (30, 30, 30))
    return t


def stone_panel():
    t = T((74, 74, 78))
    t.noise(6)
    # vene de marmură
    for k in range(5):
        x, y = rnd.randrange(S), rnd.randrange(S)
        for step in range(70):
            x += rnd.choice((-1, 0, 1, 1))
            y += rnd.choice((0, 1))
            t.set(x % S, y % S, (112, 112, 118) if rnd.random() < 0.6 else (96, 96, 102))
    # plăci 32x32
    t.rect(0, 0, S, 1, (40, 40, 44))
    t.rect(0, 32, S, 1, (40, 40, 44))
    t.rect(0, 0, 1, S, (40, 40, 44))
    t.rect(32, 0, 1, S, (40, 40, 44))
    t.rect(1, 1, S - 1, 1, (100, 100, 106))
    t.rect(1, 33, S - 1, 1, (100, 100, 106))
    t.rect(0, 60, S, 4, (30, 30, 32))
    return t


def logo_wall():
    """Peretele cu sigla ZORI TV: 4 celule (256x64), lamele de lemn închis + siglă luminoasă."""
    t = T((52, 36, 26), 256, 64)
    for x in range(256):
        if x % 8 == 0:
            t.rect(x, 0, 1, 64, (30, 20, 14))
        elif x % 8 == 1:
            t.rect(x, 0, 1, 64, (70, 50, 36))
    t.noise(6)
    # soarele care răsare (cerc portocaliu cu raze)
    cx, cy = 62, 30
    for j in range(-22, 1):
        for i in range(-22, 23):
            if i * i + j * j <= 18 * 18:
                f = 0.85 + 0.15 * (-j / 18)
                t.set(cx + i, cy + j, mul((246, 150, 46), f))
    for a in range(9):
        ang = math.pi * (a + 0.5) / 9
        for r in range(21, 29):
            x = cx - int(round(math.cos(ang) * r))
            y = cy - int(round(math.sin(ang) * r))
            t.set(x, y, (250, 196, 80))
            t.set(x + 1, y, (250, 196, 80))
    t.rect(30, 30, 64, 3, (232, 226, 210))
    # literele ZORI (bloc 5x7 mărit de 3)
    big = {
        'Z': ["11111", "00001", "00010", "00100", "01000", "10000", "11111"],
        'O': ["01110", "10001", "10001", "10001", "10001", "10001", "01110"],
        'R': ["11110", "10001", "10001", "11110", "10100", "10010", "10001"],
        'I': ["11111", "00100", "00100", "00100", "00100", "00100", "11111"],
        'T': ["11111", "00100", "00100", "00100", "00100", "00100", "00100"],
        'V': ["10001", "10001", "10001", "10001", "10001", "01010", "00100"],
    }
    x0 = 104
    for ch in "ZORI":
        g = big[ch]
        for j in range(7):
            for i in range(5):
                if g[j][i] == '1':
                    for yy in range(3):
                        for xx in range(3):
                            t.set(x0 + i * 3 + xx, 8 + j * 3 + yy, (236, 232, 220))
                    t.set(x0 + i * 3 + 3, 8 + j * 3 + 3, (24, 16, 10))
        x0 += 19
    x0 = 186
    for ch in "TV":
        g = big[ch]
        for j in range(7):
            for i in range(5):
                if g[j][i] == '1':
                    for yy in range(3):
                        for xx in range(3):
                            t.set(x0 + i * 3 + xx, 8 + j * 3 + yy, (246, 150, 46))
        x0 += 19
    draw_text(t, 104, 34, "STIRI DIN ZORI PANA IN ZORI", (190, 170, 140))
    t.rect(0, 58, 256, 6, (26, 18, 12))
    return [t.im.crop((k * 64, 0, k * 64 + 64, 64)) for k in range(4)]


def glass_facade():
    t = T((40, 50, 80))
    # cerul din zori, cu clădiri
    for y in range(S):
        f = y / 63
        c = mix((44, 52, 104), (214, 120, 96), max(0, (f - 0.25) / 0.75) ** 1.4)
        t.rect(0, y, S, 1, c)
    for (x, w, h) in [(0, 10, 22), (10, 8, 30), (19, 12, 18), (33, 7, 34), (41, 14, 24), (56, 8, 28)]:
        t.rect(x, 64 - h, w, h, (30, 30, 44))
        for yy in range(64 - h + 3, 62, 4):
            for xx in range(x + 2, x + w - 1, 3):
                if rnd.random() < 0.18:
                    t.set(xx, yy, (226, 196, 120))
    # reflexe diagonale în sticlă
    for k in range(-64, 64, 22):
        for i in range(10):
            for y in range(S):
                x = k + i + y // 2
                if 0 <= x < S:
                    t.set(x, y, mix(t.get(x, y), (200, 210, 230), 0.12))
    # montanți
    t.rect(0, 0, 3, S, (58, 60, 66))
    t.rect(61, 0, 3, S, (58, 60, 66))
    t.rect(0, 0, S, 3, (58, 60, 66))
    t.rect(0, 30, S, 2, (58, 60, 66))
    t.rect(0, 60, S, 4, (40, 40, 44))
    t.rect(3, 3, 1, 57, (90, 92, 100))
    return t


def studio_foam():
    t = T((30, 30, 34))
    for by in range(0, S, 16):
        for bx in range(0, S, 16):
            vertical = ((bx + by) // 16) % 2 == 0
            for j in range(16):
                for i in range(16):
                    if vertical:
                        v = abs((i % 8) - 3.5)
                    else:
                        v = abs((j % 8) - 3.5)
                    f = 0.75 + (3.5 - v) * 0.1
                    t.set(bx + i, by + j, mul((40, 40, 46), f))
            t.frame(bx, by, 16, 16, (16, 16, 18))
    t.noise(3)
    return t


def studio_backdrop():
    t = T((20, 40, 90))
    for y in range(S):
        c = mix((16, 30, 74), (40, 90, 170), (y / 63) ** 1.2)
        t.rect(0, y, S, 1, c)
    # siluete de blocuri luminate (decorul jurnalului)
    for (x, w, h) in [(0, 12, 26), (12, 9, 34), (22, 14, 20), (37, 8, 38), (46, 10, 28), (57, 7, 22)]:
        t.rect(x, 52 - h, w, h, (14, 24, 54))
        for yy in range(52 - h + 3, 50, 3):
            for xx in range(x + 1, x + w - 1, 2):
                if rnd.random() < 0.35:
                    t.set(xx, yy, (120, 170, 230))
    t.rect(0, 52, S, 12, (20, 26, 40))
    t.rect(0, 52, S, 1, (90, 140, 210))
    t.rect(0, 56, S, 1, (60, 100, 170))
    return t


def monitor_wall(v=0):
    t = T((18, 18, 20))
    screens = [(2, 3, 28, 22), (34, 3, 28, 22), (2, 30, 28, 22), (34, 30, 28, 22)]
    for k, (x, y, w, h) in enumerate(screens):
        t.rect(x - 1, y - 1, w + 2, h + 2, (40, 40, 44))
        kind = (k + v) % 4
        if kind == 0:  # bare color
            cols = [(200, 200, 200), (200, 200, 30), (30, 200, 200), (30, 200, 30), (200, 30, 200), (200, 30, 30), (30, 30, 200)]
            for i in range(w):
                t.rect(x + i, y, 1, h - 5, cols[i * 7 // w])
            t.rect(x, y + h - 5, w, 5, (20, 20, 20))
            t.rect(x + 3, y + h - 4, 8, 3, (230, 230, 230))
        elif kind == 1:  # purici
            for j in range(h):
                for i in range(w):
                    g = rnd.randint(30, 200)
                    t.set(x + i, y + j, (g, g, g + 6))
        elif kind == 2:  # fără semnal
            t.rect(x, y, w, h, (20, 40, 150))
            draw_text(t, x + 2, y + 8, "FARA", (220, 220, 240))
            draw_text(t, x + 2, y + 14, "SEMNAL", (220, 220, 240))
        else:  # sigla LIVE roșie pe negru
            t.rect(x, y, w, h, (12, 12, 14))
            t.rect(x + 4, y + 7, 20, 8, (190, 20, 20))
            draw_text(t, x + 6, y + 9, "LIVE", (250, 240, 240))
        t.rect(x, y, w, 1, mix(t.get(x, y), (255, 255, 255), 0.3))
    t.rect(0, 58, S, 6, (24, 24, 26))
    return t


def poster_wall(v=0):
    t = office_wall(0)
    if v == 0:  # afișul știrilor de dimineață
        t.rect(14, 4, 36, 30, (24, 30, 60))
        t.rect(14, 4, 36, 10, (230, 120, 40))
        draw_text(t, 17, 6, "STIRILE", (255, 255, 255))
        draw_text(t, 21, 16, "ZORII", (240, 230, 210))
        t.rect(17, 23, 30, 1, (200, 200, 220))
        draw_text(t, 21, 26, "06:00", (250, 190, 90))
        t.bevel(14, 4, 36, 30, (60, 70, 110), (10, 10, 20))
    elif v == 1:  # emisiune de gătit
        t.rect(16, 6, 32, 28, (230, 214, 170))
        for j in range(8):
            for i in range(12):
                if (i - 6) ** 2 / 36 + (j - 4) ** 2 / 9 < 1:
                    t.set(26 + i, 20 + j, (170, 60, 40))
        t.rect(24, 24, 16, 2, (90, 90, 96))
        draw_text(t, 19, 9, "GUSTUL", (120, 40, 30))
        draw_text(t, 19, 15, "CASEI", (120, 40, 30))
        t.bevel(16, 6, 32, 28, (250, 240, 210), (120, 100, 70))
    else:  # harta meteo ruptă
        t.rect(12, 6, 40, 28, (60, 120, 70))
        for j in range(28):
            for i in range(40):
                if (i - 20) ** 2 / 300 + (j - 14) ** 2 / 110 < 1:
                    t.set(12 + i, 6 + j, (120, 170, 90))
        draw_text(t, 16, 9, "METEO", (255, 255, 255))
        t.rect(34, 18, 6, 6, (250, 210, 60))
        for k in range(10):
            t.set(44 + k // 3, 6 + k, (176, 170, 152))
        t.bevel(12, 6, 40, 28, (90, 150, 100), (20, 50, 30))
    return t


def kitchen_tiles():
    t = T((214, 214, 206))
    t.noise(4)
    for k in range(0, S, 8):
        t.rect(0, k, S, 1, (150, 150, 146))
        t.rect(k, 0, 1, S, (150, 150, 146))
    t.rect(0, 30, S, 4, (60, 110, 160))
    t.rect(0, 30, S, 1, (90, 140, 190))
    t.grime(8, 0.86, 2)
    t.rect(0, 60, S, 4, (90, 90, 88))
    return t


def wc_tiles():
    t = T((132, 160, 146))
    t.noise(5)
    for k in range(0, S, 6):
        t.rect(0, k, S, 1, (92, 110, 100))
    for j in range(0, S, 6):
        off = 3 if (j // 6) % 2 else 0
        for k in range(off, S, 6):
            t.rect(k, j, 1, 6, (92, 110, 100))
    t.grime(14, 0.82, 3)
    return t


def archive_shelf():
    t = T((24, 22, 20))
    for y0 in (2, 22, 42):
        t.rect(0, y0 + 17, S, 3, (120, 122, 126))
        t.rect(0, y0 + 17, S, 1, (170, 172, 176))
        x = 1
        while x < 62:
            kind = rnd.random()
            if kind < 0.65:  # casete VHS / cutii de bandă
                w = rnd.choice((3, 4, 4, 5))
                h = rnd.randint(11, 16)
                col = rnd.choice([(30, 30, 34), (40, 40, 46), (170, 160, 140), (60, 60, 70), (200, 196, 180)])
                t.rect(x, y0 + 17 - h, w, h, col)
                t.rect(x, y0 + 17 - h + 3, w, 3, rnd.choice([(230, 230, 220), (220, 200, 120), (180, 200, 220)]))
                t.rect(x + w - 1, y0 + 17 - h, 1, h, mul(col, 0.6))
                x += w
            elif kind < 0.85:  # cutie de carton
                w = rnd.randint(9, 13)
                h = rnd.randint(9, 14)
                t.rect(x, y0 + 17 - h, w, h, (150, 116, 70))
                t.rect(x, y0 + 17 - h, w, 1, (180, 146, 96))
                t.rect(x + 2, y0 + 17 - h + 3, 6, 3, (230, 226, 210))
                x += w + 1
            else:
                x += rnd.randint(2, 5)
    t.rect(0, 0, 2, S, (90, 92, 96))
    t.rect(62, 0, 2, S, (70, 72, 76))
    t.rect(0, 62, S, 2, (30, 30, 30))
    return t


def server_rack():
    t = T((22, 24, 26))
    for x0 in (0, 32):
        t.rect(x0, 0, 32, S, (28, 30, 34))
        t.rect(x0, 0, 2, S, (60, 62, 68))
        t.rect(x0 + 30, 0, 2, S, (14, 14, 16))
        for y in range(3, 60, 7):
            t.rect(x0 + 3, y, 26, 6, (40, 42, 48))
            t.rect(x0 + 3, y, 26, 1, (70, 72, 80))
            for i in range(x0 + 5, x0 + 20, 2):
                t.set(i, y + 3, (16, 16, 18))
            led = rnd.choice([(60, 230, 90), (60, 230, 90), (250, 180, 40), (40, 200, 250), (230, 40, 40)])
            t.set(x0 + 24, y + 2, led)
            t.set(x0 + 26, y + 2, rnd.choice([(60, 230, 90), (20, 60, 30)]))
            t.set(x0 + 24, y + 4, rnd.choice([(250, 180, 40), (60, 50, 20)]))
    return t


def concrete_blocks():
    t = T((116, 116, 112))
    t.noise(9)
    for y in range(0, S, 16):
        t.rect(0, y, S, 1, (78, 78, 76))
        off = 16 if (y // 16) % 2 else 0
        for x in range(off, S + 32, 32):
            t.rect(x % S, y, 1, 16, (78, 78, 76))
    t.grime(18, 0.84, 3)
    return t


def electrical_panel():
    t = concrete_blocks()
    t.rect(6, 4, 52, 54, (128, 132, 128))
    t.bevel(6, 4, 52, 54, (170, 174, 170), (70, 72, 70))
    t.rect(10, 8, 44, 24, (60, 62, 60))
    for r in range(3):
        for k in range(10):
            on = rnd.random() < 0.3
            t.rect(12 + k * 4, 10 + r * 7, 3, 5, (220, 220, 210) if not on else (40, 40, 40))
            t.set(13 + k * 4, 11 + r * 7 + (0 if on else 3), (30, 30, 30))
    # triunghi de avertizare
    for j in range(9):
        for i in range(-j // 1, j + 1):
            if abs(i) <= j * 0.6:
                t.set(20 + i, 36 + j, (240, 200, 40))
    t.set(20, 39, (20, 20, 20))
    t.set(20, 40, (20, 20, 20))
    t.set(20, 42, (20, 20, 20))
    # maneta roșie
    t.rect(38, 36, 10, 18, (80, 80, 80))
    t.rect(41, 38, 4, 10, (190, 30, 30))
    t.rect(40, 37, 6, 3, (230, 60, 50))
    draw_text(t, 10, 50, "GENERATOR", (30, 30, 30))
    return t


def makeup_mirror():
    t = office_wall(0)
    t.rect(0, 0, S, 38, (150, 126, 118))
    t.noise(3, 0, 0, S, 38)
    t.rect(6, 4, 52, 32, (210, 200, 186))
    t.rect(10, 8, 44, 24, (70, 84, 96))
    for j in range(24):
        for i in range(44):
            if (i + j) % 17 < 3:
                t.set(10 + i, 8 + j, (110, 126, 140))
    for x in range(8, 58, 8):
        t.rect(x, 5, 3, 2, (255, 240, 190))
        t.rect(x, 33, 3, 2, (255, 240, 190))
    for y in range(9, 33, 8):
        t.rect(7, y, 2, 3, (255, 240, 190))
        t.rect(55, y, 2, 3, (255, 240, 190))
    t.rect(0, 38, S, 6, (230, 226, 220))
    t.rect(0, 43, S, 1, (140, 136, 130))
    t.rect(14, 35, 4, 4, (200, 60, 90))
    t.rect(40, 34, 6, 5, (40, 40, 44))
    return t


def garage_door():
    t = T((132, 136, 138))
    for y in range(S):
        f = 0.8 if y % 6 in (0, 5) else (1.08 if y % 6 == 2 else 1.0)
        t.rect(0, y, S, 1, mul((132, 136, 138), f))
    t.noise(5)
    t.grime(12, 0.82, 3)
    for x in range(S):
        for y in range(54, 60):
            t.set(x, y, (232, 190, 30) if ((x + y) // 5) % 2 == 0 else (30, 30, 30))
    t.rect(0, 60, S, 4, (60, 60, 62))
    return t


def elevator():
    t = T((150, 154, 160))
    for x in range(S):
        f = 1.0 + 0.12 * math.sin(x * 0.4)
        t.rect(x, 0, 1, S, mul((150, 154, 160), f))
    t.noise(4)
    t.rect(31, 6, 2, 54, (70, 72, 76))
    t.rect(0, 0, S, 6, (90, 92, 96))
    t.rect(0, 58, S, 6, (70, 72, 76))
    t.rect(24, 1, 16, 4, (20, 20, 20))
    draw_text(t, 27, 1, "P", (230, 80, 40))
    return t


# ---------------------------------------------------------------- uși
def door(stripe=None, window=True, base=(124, 92, 62)):
    t = T((70, 70, 72))
    t.rect(8, 2, 48, 62, base)
    for x in range(10, 54, 6):
        t.rect(x, 4, 1, 58, mul(base, 0.9))
    t.noise(4, 8, 2, 48, 62)
    t.bevel(8, 2, 48, 62, mul(base, 1.25), mul(base, 0.6))
    t.rect(4, 0, 4, S, (90, 92, 96))
    t.rect(56, 0, 4, S, (60, 62, 66))
    t.rect(4, 0, 56, 2, (100, 102, 106))
    if window:
        t.rect(20, 10, 24, 14, (40, 46, 56))
        t.rect(20, 10, 24, 1, (90, 100, 116))
        t.bevel(19, 9, 26, 16, mul(base, 0.7), mul(base, 1.2))
    t.rect(46, 34, 6, 2, (200, 196, 180))
    t.rect(46, 36, 2, 3, (160, 156, 140))
    if stripe:
        t.rect(8, 28, 48, 4, stripe)
        t.rect(48, 40, 5, 8, (40, 40, 44))
        t.rect(49, 41, 3, 2, stripe)
        t.set(50, 46, (60, 230, 90))
    t.grime(5, 0.85, 2)
    return t


def studio_door():
    t = door(None, False, (44, 44, 48))
    for y in range(6, 60, 6):
        for x in range(10, 54, 6):
            t.set(x + 2, y, (24, 24, 26))
    t.rect(16, 6, 32, 10, (40, 10, 10))
    t.rect(17, 7, 30, 8, (200, 20, 20))
    draw_text(t, 21, 9, "EMISIE", (255, 230, 220))
    return t


def exit_door():
    t = door(None, True, (110, 116, 112))
    t.rect(14, 40, 20, 8, (30, 140, 60))
    draw_text(t, 15, 41, "SCARA", (230, 255, 230))
    t.rect(8, 50, 48, 2, (80, 80, 84))
    return t


def entrance_door():
    t = glass_facade()
    t.rect(30, 0, 4, S, (58, 60, 66))
    t.rect(22, 36, 4, 14, (190, 194, 200))
    t.rect(38, 36, 4, 14, (190, 194, 200))
    t.rect(8, 26, 48, 3, (240, 150, 50))
    draw_text(t, 10, 20, "INTRARE", (240, 240, 240))
    return t


def keypad_door():
    t = door(None, False, (96, 100, 104))
    t.rect(44, 26, 10, 14, (30, 30, 34))
    for r in range(3):
        for k in range(3):
            t.set(46 + k * 3, 29 + r * 3, (200, 200, 190))
    t.rect(45, 27, 8, 1, (60, 200, 90))
    return t


# ---------------------------------------------------------------- podele
def lobby_floor():
    t = T((60, 58, 56))
    for by in range(0, S, 32):
        for bx in range(0, S, 32):
            dark = ((bx + by) // 32) % 2 == 0
            base = (52, 50, 50) if dark else (138, 134, 126)
            t.rect(bx, by, 32, 32, base)
            t.noise(5, bx, by, 32, 32)
            t.frame(bx, by, 32, 32, mul(base, 0.75))
    for k in range(3):
        x, y = rnd.randrange(S), rnd.randrange(S)
        for s in range(40):
            x += rnd.choice((-1, 1, 1))
            y += rnd.choice((0, 1))
            t.set(x % S, y % S, mix(t.get(x % S, y % S), (200, 200, 200), 0.25))
    return t


def linoleum():
    t = T((106, 116, 104))
    t.noise(7)
    t.speckle(160, [(80, 88, 78), (136, 144, 130), (96, 104, 92)])
    t.rect(0, 0, S, 1, (82, 90, 80))
    t.rect(0, 0, 1, S, (82, 90, 80))
    t.grime(10, 0.86, 3)
    return t


def carpet():
    t = T((66, 72, 90))
    for y in range(S):
        for x in range(S):
            v = ((x * 3 + y * 5) % 7) - 3
            n = rnd.randint(-6, 6)
            t.set(x, y, (66 + v + n, 72 + v + n, 90 + v + n))
    for k in range(0, S, 16):
        for x in range(S):
            if (x // 2) % 2 == 0:
                t.set(x, k, (58, 62, 78))
    t.grime(8, 0.88, 4)
    return t


def studio_floor():
    t = T((62, 62, 66))
    t.noise(4)
    for k in range(0, S, 32):
        t.rect(0, k, S, 1, (48, 48, 52))
        t.rect(k, 0, 1, S, (48, 48, 52))
    t.grime(10, 0.86, 4)
    return t


def kitchen_floor():
    t = T((176, 176, 168))
    for by in range(0, S, 16):
        for bx in range(0, S, 16):
            if ((bx + by) // 16) % 2:
                t.rect(bx, by, 16, 16, (92, 96, 100))
            t.frame(bx, by, 16, 16, (130, 130, 124))
    t.noise(5)
    t.grime(16, 0.84, 3)
    return t


def wc_floor():
    t = T((150, 150, 144))
    t.noise(4)
    for k in range(0, S, 4):
        t.rect(0, k, S, 1, (118, 118, 112))
        t.rect(k, 0, 1, S, (118, 118, 112))
    t.grime(14, 0.78, 3)
    return t


def concrete_floor(lines=False):
    t = T((96, 96, 94))
    t.noise(8)
    t.grime(20, 0.82, 4)
    t.rect(0, 0, S, 1, (76, 76, 74))
    if lines:
        t.rect(28, 0, 6, S, (196, 160, 40))
        t.noise(6, 28, 0, 6, S)
    return t


def wood_floor():
    t = T((120, 84, 54))
    for y in range(0, S, 8):
        off = (y * 13) % 40
        base = mul((120, 84, 54), 0.9 + rnd.random() * 0.2)
        t.rect(0, y, S, 8, base)
        t.rect(0, y, S, 1, (70, 48, 30))
        t.rect(off % S, y, 1, 8, (70, 48, 30))
        t.rect((off + 32) % S, y, 1, 8, (70, 48, 30))
        for x in range(S):
            if rnd.random() < 0.25:
                t.set(x, y + 1 + rnd.randrange(6), mul(base, 0.9))
    t.noise(3)
    return t


# ---------------------------------------------------------------- tavane
def ceiling_tiles(light=False):
    t = T((150, 150, 144))
    t.noise(5)
    t.speckle(80, [(130, 130, 124), (164, 164, 158)])
    for k in (0, 32):
        t.rect(0, k, S, 2, (110, 110, 106))
        t.rect(k, 0, 2, S, (110, 110, 106))
    if light:
        t.rect(10, 18, 44, 28, (90, 92, 96))
        t.rect(12, 20, 40, 24, (236, 240, 236))
        for x in range(12, 52, 5):
            t.rect(x, 20, 1, 24, (196, 200, 200))
        t.rect(12, 31, 40, 2, (206, 210, 210))
    else:
        t.grime(6, 0.85, 3)
    return t


def lobby_ceiling(light=False):
    t = T((42, 40, 40))
    t.noise(3)
    t.rect(0, 0, S, 2, (26, 24, 24))
    t.rect(0, 0, 2, S, (26, 24, 24))
    if light:
        for j in range(-6, 7):
            for i in range(-6, 7):
                d = i * i + j * j
                if d <= 36:
                    t.set(32 + i, 32 + j, (80, 78, 76) if d > 20 else (255, 236, 200))
    return t


def studio_ceiling(light=False):
    t = T((20, 20, 24))
    t.noise(3)
    t.rect(0, 30, S, 2, (52, 52, 58))
    t.rect(0, 30, S, 1, (74, 74, 80))
    for x in range(0, S, 8):
        t.set(x, 32, (34, 34, 38))
    if light:
        t.rect(22, 20, 20, 20, (40, 40, 44))
        t.rect(24, 22, 16, 16, (255, 250, 230))
        t.frame(22, 20, 20, 20, (90, 90, 96))
        t.rect(31, 30, 2, 2, (40, 40, 44))
    return t


def concrete_ceiling(light=False):
    t = T((70, 70, 70))
    t.noise(6)
    t.grime(10, 0.8, 4)
    t.rect(0, 30, S, 4, (56, 56, 56))
    if light:
        for j in range(-5, 6):
            for i in range(-5, 6):
                d = i * i + j * j
                if d <= 25:
                    t.set(32 + i, 20 + j, (255, 230, 170) if d < 10 else (60, 60, 60))
        for k in range(-5, 6, 2):
            t.rect(32 + k, 15, 1, 11, (40, 40, 40))
    return t


def red_light_ceiling():
    t = concrete_ceiling(False)
    for j in range(-7, 8):
        for i in range(-7, 8):
            d = i * i + j * j
            if d <= 49:
                t.set(32 + i, 32 + j, (255, 70, 50) if d < 18 else (120, 20, 16))
    return t


TEX = []


def add(name, t):
    TEX.append((name, t if isinstance(t, Image.Image) else t.im))


def build():
    add("office0", office_wall(0))        # 0
    add("office1", office_wall(1))        # 1
    add("office2", office_wall(2))        # 2
    add("office3", office_wall(3))        # 3
    add("office4", office_wall(4))        # 4
    add("stone", stone_panel())           # 5
    for k, im in enumerate(logo_wall()):  # 6..9
        add("logo%d" % k, im)
    add("glass", glass_facade())          # 10
    add("foam", studio_foam())            # 11
    add("backdrop", studio_backdrop())    # 12
    add("monitors0", monitor_wall(0))     # 13
    add("monitors1", monitor_wall(1))     # 14
    add("monitors2", monitor_wall(2))     # 15
    add("poster0", poster_wall(0))        # 16
    add("poster1", poster_wall(1))        # 17
    add("poster2", poster_wall(2))        # 18
    add("kitchen", kitchen_tiles())       # 19
    add("wc", wc_tiles())                 # 20
    add("archive", archive_shelf())       # 21
    add("archive2", archive_shelf())      # 22
    add("servers", server_rack())         # 23
    add("concrete", concrete_blocks())    # 24
    add("panel", electrical_panel())      # 25
    add("mirror", makeup_mirror())        # 26
    add("garage", garage_door())          # 27
    add("elevator", elevator())           # 28
    add("door", door())                   # 29
    add("door_blue", door((40, 100, 220)))   # 30
    add("door_red", door((210, 40, 40)))     # 31
    add("door_yellow", door((230, 190, 40)))  # 32
    add("door_green", door((40, 180, 80)))   # 33
    add("door_studio", studio_door())    # 34
    add("door_exit", exit_door())        # 35
    add("door_entrance", entrance_door())  # 36
    add("door_code", keypad_door())      # 37
    add("f_lobby", lobby_floor())         # 38
    add("f_lino", linoleum())             # 39
    add("f_carpet", carpet())             # 40
    add("f_studio", studio_floor())       # 41
    add("f_kitchen", kitchen_floor())     # 42
    add("f_wc", wc_floor())               # 43
    add("f_concrete", concrete_floor())   # 44
    add("f_wood", wood_floor())           # 45
    add("f_dock", concrete_floor(True))   # 46
    add("c_tiles", ceiling_tiles(False))  # 47
    add("c_tiles_l", ceiling_tiles(True))  # 48
    add("c_lobby", lobby_ceiling(False))  # 49
    add("c_lobby_l", lobby_ceiling(True))  # 50
    add("c_studio", studio_ceiling(False))  # 51
    add("c_studio_l", studio_ceiling(True))  # 52
    add("c_concrete", concrete_ceiling(False))  # 53
    add("c_concrete_l", concrete_ceiling(True))  # 54
    add("c_red", red_light_ceiling())     # 55


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else "assets"
    import os
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import tex_spital
    tex_spital.build(add)
    tex_spital.sky_panorama(out + "/sky.png")
    cols = 8
    rows = (len(TEX) + cols - 1) // cols
    atlas = Image.new("RGB", (cols * S, rows * S), (255, 0, 255))
    for i, (name, im) in enumerate(TEX):
        atlas.paste(im, ((i % cols) * S, (i // cols) * S))
    atlas.save(out + "/tex.png")
    with open(out + "/tex.txt", "w") as f:
        for i, (name, im) in enumerate(TEX):
            f.write("%d %s\n" % (i, name))
    print("%d texturi -> %s/tex.png" % (len(TEX), out))
    if len(sys.argv) > 2:
        atlas.resize((atlas.width * 3, atlas.height * 3), Image.NEAREST).save(sys.argv[2])


if __name__ == "__main__":
    main()
