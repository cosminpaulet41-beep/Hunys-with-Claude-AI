"""Texturile Spitalului Universitar (Tura de Noapte 2): pereți, uși, podele, tavane, heliportul
și cerul din zori (panoramă). Folosește uneltele din textures.py.

Ordinea nu mai contează pentru joc: Art.java caută texturile după nume (assets/tex.txt).
"""
import math
import random

from PIL import Image

from textures import (S, T, mul, mix, draw_text, text_w, glyph, office_wall, stone_panel, kitchen_tiles,
                      wc_tiles, concrete_blocks, electrical_panel, garage_door, elevator, monitor_wall,
                      lobby_floor, wc_floor, concrete_floor, wood_floor, ceiling_tiles, lobby_ceiling,
                      concrete_ceiling, red_light_ceiling, server_rack)

rnd = random.Random(1906)
KEY = (255, 0, 255)       # pixel transparent (prin el se vede cerul)

# ---------------------------------------------------------------- pereți de spital
# sus vopsit deschis, jos vopsea lavabilă mai închisă, bară de protecție pentru paturi
PAL = {
    'c': ((206, 202, 184), (112, 142, 132)),    # coridor: crem + verde-albăstrui
    'u': ((214, 216, 220), (70, 108, 166)),     # urgențe: alb + albastru
    'w': ((216, 208, 178), (146, 166, 118)),    # saloane: crem + verde oliv
    'o': ((200, 194, 178), (132, 110, 90)),     # cabinete: bej + maro
    'i': ((210, 218, 222), (96, 140, 160)),     # ATI
    'f': ((220, 222, 220), (120, 160, 120)),    # farmacie
    'r': ((198, 200, 196), (150, 140, 96)),     # radiologie
    'k': ((214, 196, 160), (126, 86, 60)),      # cafenea
}


def hosp_wall(zone='c', variant=0, seed=None):
    up, lo = PAL[zone]
    t = T(up)
    t.noise(4)
    t.rect(0, 40, S, 24, lo)
    t.noise(4, 0, 40, S, 24)
    # bara de protecție (lemn/PVC) pe console
    t.rect(0, 35, S, 5, (178, 176, 170))
    t.rect(0, 35, S, 1, (214, 212, 206))
    t.rect(0, 39, S, 1, (110, 108, 104))
    for x in (6, 38):
        t.rect(x, 40, 3, 2, (90, 90, 88))
    t.rect(0, 60, S, 4, (62, 66, 64))
    t.rect(0, 60, S, 1, (92, 96, 94))
    # urme de cărucioare pe vopseaua de jos
    for _ in range(6):
        x, y = rnd.randrange(S), rnd.randint(44, 57)
        for k in range(rnd.randint(4, 12)):
            t.set((x + k) % S, y, mul(t.get((x + k) % S, y), 0.82))
    t.grime(8, 0.9, 2)
    if variant == 1:      # dozator de dezinfectant
        t.rect(26, 14, 12, 18, (232, 232, 228))
        t.rect(27, 15, 10, 9, (90, 170, 220))
        t.rect(28, 16, 3, 7, (150, 210, 240))
        t.rect(29, 26, 6, 3, (120, 120, 124))
        t.bevel(26, 14, 12, 18, (250, 250, 248), (150, 150, 146))
        t.rect(24, 33, 16, 1, mul(up, 0.85))
    elif variant == 2:    # hidrant (cutie roșie cu geam)
        t.rect(16, 4, 32, 30, (176, 30, 28))
        t.rect(20, 7, 24, 20, (60, 20, 20))
        for a in range(0, 360, 12):
            for r in (6, 7):
                x = 32 + int(round(math.cos(math.radians(a)) * r))
                y = 17 + int(round(math.sin(math.radians(a)) * r))
                t.set(x, y, (200, 50, 40))
        t.rect(31, 16, 3, 3, (120, 120, 120))
        t.rect(21, 7, 22, 2, (140, 80, 80))
        draw_text(t, 18, 28, "HIDRANT", (250, 236, 226))
        t.bevel(16, 4, 32, 30, (220, 60, 52), (110, 16, 14))
    elif variant == 3:    # afișul campaniei de vaccinare
        t.rect(14, 3, 36, 30, (236, 236, 232))
        t.rect(14, 3, 36, 9, (40, 90, 160))
        draw_text(t, 20, 5, "VACCIN", (255, 255, 255))
        draw_text(t, 21, 14, "VN-9", (40, 90, 160))
        draw_text(t, 21, 21, "06:00", (200, 40, 40))
        # seringă stilizată
        t.rect(38, 15, 9, 3, (180, 200, 220))
        t.rect(37, 16, 1, 1, (120, 120, 126))
        t.rect(47, 16, 2, 1, (120, 120, 126))
        t.rect(17, 28, 30, 1, (170, 170, 176))
        t.bevel(14, 3, 36, 30, (255, 255, 255), (150, 150, 146))
        # colț rupt
        for k in range(6):
            for j in range(6 - k):
                t.set(49 - j, 32 - k, up)
    elif variant == 4:    # palmă de sânge și dâră
        blood = (110, 20, 18)
        for (x, y) in [(30, 20), (31, 20), (32, 19), (33, 20), (34, 21), (29, 23), (30, 22), (31, 22), (32, 22),
                       (33, 22), (34, 23), (30, 24), (31, 24), (32, 24), (33, 24), (31, 25), (32, 25), (33, 26),
                       (28, 21), (35, 19), (27, 22)]:
            t.set(x, y, blood)
        for y in range(25, 58):
            for k in range(3):
                if rnd.random() < 0.8:
                    t.set(31 + k + (y // 9), y, mul(blood, 0.8 + rnd.random() * 0.3))
    elif variant == 5:    # ceas de perete
        for j in range(-8, 9):
            for i in range(-8, 9):
                d = i * i + j * j
                if d <= 64:
                    t.set(32 + i, 16 + j, (40, 40, 44) if d > 49 else (244, 242, 236))
        for k in range(12):
            a = k * math.pi / 6
            t.set(32 + int(round(math.cos(a) * 6)), 16 + int(round(math.sin(a) * 6)), (60, 60, 60))
        for r in range(5):
            t.set(32, 16 - r, (20, 20, 20))       # minutarul (6:40)
        for r in range(4):
            t.set(32 + r // 2, 16 + r, (20, 20, 20))
    return t


def sign_wall(text, icon, zone='c', col=(34, 76, 156)):
    """Panou indicator albastru deasupra barei: pictogramă + text (pe unul sau două rânduri)."""
    t = hosp_wall(zone, 0)
    lines = text.split("\n")
    w = max(max(text_w(l) for l in lines) + 16, 30)
    h = 15 if len(lines) == 1 else 19
    x0 = (S - w) // 2
    t.rect(x0, 6, w, h, col)
    t.bevel(x0, 6, w, h, mul(col, 1.4), mul(col, 0.6))
    t.rect(x0 + 2, 8 + (h - 15) // 2, 9, 11, (240, 240, 240))
    icon(t, x0 + 2, 8 + (h - 15) // 2)
    for k, l in enumerate(lines):
        draw_text(t, x0 + 13, 11 if len(lines) == 1 else 9 + k * 7, l, (250, 250, 250))
    t.rect(x0 + 6, 6 + h, 1, 3, (90, 90, 94))
    t.rect(x0 + w - 7, 6 + h, 1, 3, (90, 90, 94))
    return t


def ic_cross(t, x, y):
    t.rect(x + 3, y + 1, 3, 9, (200, 40, 40))
    t.rect(x + 1, y + 4, 7, 3, (200, 40, 40))


def ic_rad(t, x, y):
    t.rect(x, y, 9, 11, (240, 200, 40))
    for j in range(-4, 5):
        for i in range(-4, 5):
            a = math.degrees(math.atan2(j, i)) % 360
            d = i * i + j * j
            if d <= 2 or (6 <= d <= 18 and int(a // 60) % 2 == 0):
                t.set(x + 4 + i, y + 5 + j, (20, 20, 20))


def ic_pill(t, x, y):
    t.rect(x + 1, y + 3, 4, 4, (40, 150, 70))
    t.rect(x + 5, y + 3, 3, 4, (240, 240, 240))
    t.rect(x + 1, y + 3, 7, 1, mul((40, 150, 70), 1.2))


def ic_bed(t, x, y):
    t.rect(x + 1, y + 5, 7, 2, (34, 76, 156))
    t.rect(x + 1, y + 2, 1, 7, (34, 76, 156))
    t.rect(x + 2, y + 3, 2, 2, (34, 76, 156))
    t.rect(x + 7, y + 7, 1, 2, (34, 76, 156))


def ic_knife(t, x, y):
    for k in range(7):
        t.set(x + 1 + k, y + 8 - k, (60, 60, 64))
        t.set(x + 2 + k, y + 8 - k, (120, 120, 126))
    t.rect(x + 1, y + 7, 2, 3, (34, 76, 156))


def ic_bear(t, x, y):
    c = (150, 100, 60)
    t.rect(x + 2, y + 3, 5, 4, c)
    t.rect(x + 3, y + 6, 3, 4, c)
    t.set(x + 2, y + 2, c)
    t.set(x + 6, y + 2, c)
    t.set(x + 3, y + 4, (20, 20, 20))
    t.set(x + 5, y + 4, (20, 20, 20))


def ic_flask(t, x, y):
    c = (34, 76, 156)
    t.rect(x + 3, y + 1, 3, 4, c)
    for k in range(5):
        t.rect(x + 3 - k // 2, y + 5 + k, 3 + k, 1, c)
    t.rect(x + 2, y + 8, 5, 1, (60, 190, 100))


def ic_stairs(t, x, y):
    c = (34, 76, 156)
    for k in range(4):
        t.rect(x + 1 + k * 2, y + 8 - k * 2, 2, 2 + k * 2, c)


def ic_h(t, x, y):
    c = (34, 76, 156)
    t.rect(x + 2, y + 2, 2, 7, c)
    t.rect(x + 6, y + 2, 2, 7, c)
    t.rect(x + 4, y + 5, 2, 1, c)


def ic_cup(t, x, y):
    c = (120, 80, 50)
    t.rect(x + 2, y + 4, 5, 5, c)
    t.rect(x + 7, y + 5, 1, 2, c)
    t.set(x + 3, y + 2, (150, 150, 150))
    t.set(x + 5, y + 1, (150, 150, 150))


def er_curtain():
    t = T((150, 186, 206))
    for x in range(S):
        f = 0.82 + 0.22 * (0.5 + 0.5 * math.sin(x * 0.55)) + 0.05 * math.sin(x * 1.7)
        for y in range(4, 60):
            t.set(x, y, mul((150, 186, 206), f))
    t.noise(3, 0, 4, S, 56)
    # șina și inelele de sus
    t.rect(0, 0, S, 3, (170, 172, 176))
    for x in range(2, S, 6):
        t.rect(x, 2, 2, 3, (120, 122, 126))
    # partea de jos e plasă (aerisire) - se vede umbra
    for y in range(4, 12):
        for x in range(S):
            if (x + y) % 3 == 0:
                t.set(x, y, mul(t.get(x, y), 0.8))
    t.rect(0, 58, S, 6, (52, 54, 56))
    for _ in range(3):
        x = rnd.randint(4, 58)
        for y in range(rnd.randint(44, 52), 58):
            t.set(x + (y % 2), y, (118, 26, 24))
    return t


def tile_wall(base=(222, 224, 220), grout=(160, 164, 162), cell=8, band=None):
    t = T(base)
    t.noise(3)
    for k in range(0, S, cell):
        t.rect(0, k, S, 1, grout)
        t.rect(k, 0, 1, S, grout)
    if band:
        t.rect(0, 30, S, 4, band)
        t.rect(0, 30, S, 1, mul(band, 1.25))
    t.grime(6, 0.88, 2)
    t.rect(0, 60, S, 4, mul(grout, 0.6))
    return t


def or_wall(variant=0):
    base = (150, 192, 176)
    t = T(base)
    for x in range(S):
        f = 1.0 + 0.04 * math.sin(x * 0.3)
        for y in range(S):
            t.set(x, y, mul(base, f))
    t.noise(3)
    for x in (0, 31):
        t.rect(x, 0, 1, S, mul(base, 0.75))
        t.rect(x + 1, 0, 1, S, mul(base, 1.1))
    t.rect(0, 60, S, 4, (90, 110, 104))
    if variant == 1:     # dulap din inox cu uși de sticlă
        t.rect(6, 6, 52, 40, (170, 174, 178))
        for k in range(2):
            x = 8 + k * 25
            t.rect(x, 8, 23, 36, (60, 80, 84))
            for sh in (16, 26, 36):
                t.rect(x, 8 + sh - 8, 23, 1, (150, 160, 164))
            for (bx, by, c) in [(x + 2, 13, (220, 220, 230)), (x + 8, 12, (90, 160, 220)), (x + 14, 22, (230, 230, 220)),
                                (x + 4, 23, (220, 90, 80)), (x + 12, 32, (200, 220, 230)), (x + 17, 31, (240, 240, 240))]:
                t.rect(bx, by, 4, 6 if by < 20 else 5, c)
            t.rect(x + 1, 9, 1, 34, (150, 170, 180))
        t.bevel(6, 6, 52, 40, (220, 224, 228), (110, 112, 116))
    elif variant == 2:   # negatoscop mic + ceas
        t.rect(8, 8, 26, 22, (40, 44, 48))
        t.rect(10, 10, 22, 18, (210, 226, 236))
        for k in range(5):
            t.rect(13, 13 + k * 3, 16, 1, (150, 170, 186))
        t.rect(20, 12, 2, 14, (160, 180, 196))
        for j in range(-6, 7):
            for i in range(-6, 7):
                d = i * i + j * j
                if d <= 36:
                    t.set(46 + i, 16 + j, (30, 30, 34) if d > 26 else (236, 236, 230))
        t.rect(46, 11, 1, 5, (20, 20, 20))
        t.rect(46, 16, 4, 1, (20, 20, 20))
    return t


def icu_wall():
    up, lo = PAL['i']
    t = hosp_wall('i', 0)
    # rampa de gaze medicale
    t.rect(2, 22, 60, 9, (196, 200, 204))
    t.rect(2, 22, 60, 1, (232, 234, 236))
    t.rect(2, 30, 60, 1, (120, 124, 128))
    for (x, c) in [(8, (240, 240, 240)), (16, (40, 40, 40)), (24, (230, 200, 40)), (40, (240, 240, 240)),
                   (48, (40, 40, 40)), (56, (230, 200, 40))]:
        t.rect(x - 2, 24, 5, 5, mul(c, 0.9))
        t.rect(x - 1, 25, 3, 3, (80, 80, 84))
        t.set(x, 26, (20, 20, 20))
    # monitor pe braț
    t.rect(22, 3, 22, 16, (26, 28, 30))
    t.rect(24, 5, 18, 12, (6, 14, 10))
    pts = [0, 0, 0, -1, 3, -5, 2, 0, 0, 0, 1, 0, 0, 0, 0, 0, -1, -4, 2, 0]
    for i, v in enumerate(pts[:18]):
        t.set(24 + i, 11 + v, (60, 230, 110))
    draw_text(t, 25, 6, "72", (60, 230, 110))
    draw_text(t, 35, 6, "98", (90, 190, 240))
    t.rect(32, 19, 2, 3, (90, 92, 96))
    return t


def kids_wall(variant=0):
    up, lo = (236, 224, 172), (150, 192, 222)
    t = T(up)
    t.noise(3)
    t.rect(0, 42, S, 22, lo)
    t.noise(3, 0, 42, S, 22)
    # bordură ondulată
    for x in range(S):
        y = 41 + int(round(1.5 * math.sin(x * 2 * math.pi / 16)))
        t.rect(x, y, 1, 2, (240, 140, 120))
    t.rect(0, 60, S, 4, (70, 90, 110))

    def crayon(pts, c):
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            n = max(abs(x1 - x0), abs(y1 - y0), 1)
            for k in range(n + 1):
                t.set(int(round(x0 + (x1 - x0) * k / n)), int(round(y0 + (y1 - y0) * k / n)), c)

    def blob(cx, cy, r, c):
        for j in range(-r, r + 1):
            for i in range(-r, r + 1):
                if i * i + j * j <= r * r and rnd.random() < 0.9:
                    t.set(cx + i, cy + j, c)

    # foi de desen lipite cu bandă
    def paper(x, y, w, h):
        t.rect(x, y, w, h, (250, 250, 246))
        t.rect(x + w // 2 - 3, y - 1, 6, 2, (220, 220, 180))

    if variant == 0:
        paper(4, 6, 26, 22)
        blob(11, 12, 3, (250, 200, 40))                                  # soarele
        for a in range(0, 360, 45):
            crayon([(11 + int(4 * math.cos(math.radians(a))), 12 + int(4 * math.sin(math.radians(a)))),
                    (11 + int(6 * math.cos(math.radians(a))), 12 + int(6 * math.sin(math.radians(a))))], (250, 190, 30))
        t.rect(17, 18, 9, 8, (220, 70, 60))                              # casa
        crayon([(16, 18), (21, 13), (26, 18)], (150, 60, 40))
        t.rect(20, 21, 3, 5, (120, 80, 40))
        crayon([(5, 26), (29, 26)], (60, 170, 60))
        paper(34, 4, 26, 24)
        for k, c in enumerate([(230, 60, 60), (240, 150, 40), (240, 220, 50), (70, 180, 70), (60, 120, 220)]):
            for a in range(0, 181, 6):                                   # curcubeul
                x = 47 + int(round(math.cos(math.radians(a)) * (10 - k)))
                y = 22 - int(round(math.sin(math.radians(a)) * (10 - k)))
                t.set(x, y, c)
        draw_text(t, 37, 24, "MAMA", (60, 120, 220))
    else:
        paper(6, 4, 24, 26)
        for (x, y, c) in [(12, 10, (230, 60, 80)), (18, 8, (60, 140, 230)), (24, 11, (240, 200, 40))]:
            blob(x, y, 3, c)                                             # baloane
            crayon([(x, y + 3), (x + 1, y + 7), (16, 25)], (80, 80, 80))
        crayon([(8, 26), (28, 26)], (60, 170, 60))
        paper(36, 8, 24, 22)
        blob(48, 20, 5, (150, 150, 150))                                 # pisica
        blob(48, 13, 3, (150, 150, 150))
        t.set(46, 9, (150, 150, 150))
        t.set(50, 9, (150, 150, 150))
        t.set(47, 13, (30, 30, 30))
        t.set(49, 13, (30, 30, 30))
        crayon([(53, 22), (56, 18), (57, 14)], (150, 150, 150))
        draw_text(t, 38, 24, "ANA 6", (230, 60, 80))
    # flori pictate pe soclu
    for x in range(6, S, 16):
        crayon([(x, 58), (x, 50)], (70, 160, 80))
        blob(x, 49, 2, (240, 120, 160) if x % 32 == 6 else (250, 210, 60))
    return t


LAB_BLUE = (36, 86, 160)


def lab_wall(variant=0):
    base = (198, 204, 208)
    t = T(base)
    t.noise(2)
    for x in (0, 32):
        t.rect(x, 0, 1, S, (176, 180, 184))
        t.rect(x + 1, 0, 1, S, (238, 240, 242))
    t.rect(0, 44, S, 2, LAB_BLUE)
    t.rect(0, 60, S, 4, (120, 124, 130))
    if variant == 1:     # semn de pericol biologic
        t.rect(20, 8, 24, 24, (236, 196, 30))
        t.bevel(20, 8, 24, 24, (250, 220, 80), (150, 120, 10))
        for j in range(-9, 10):
            for i in range(-9, 10):
                d = math.hypot(i, j)
                a = (math.degrees(math.atan2(j, i)) + 90) % 120
                lobe = 3.2 <= d <= 7.6 and (a < 32 or a > 88)
                ring = 5.2 <= d <= 6.0
                if lobe or ring or d < 1.6:
                    t.set(32 + i, 20 + j, (20, 20, 20))
        draw_text(t, 21, 35, "BIOHAZARD", (190, 30, 30))
    elif variant == 2:   # sigla Vectra Bio
        t.rect(6, 10, 52, 22, (250, 250, 250))
        for j in range(-7, 8):
            for i in range(-7, 8):
                if abs(i) + abs(j) <= 7:
                    t.set(15 + i, 21 + j, LAB_BLUE if abs(i) + abs(j) > 3 else (90, 180, 220))
        draw_text(t, 25, 15, "VECTRA", LAB_BLUE)
        draw_text(t, 25, 22, "BIO", (90, 180, 220))
        t.rect(25, 28, 30, 1, (200, 204, 210))
    return t


def lab_glass():
    t = T((150, 220, 226))
    for y in range(S):
        f = 0.85 + 0.25 * (y / 63)
        t.rect(0, y, S, 1, mul((150, 220, 226), f))
    # interiorul camerei curate: hote și rafturi văzute prin geam
    t.rect(6, 20, 22, 18, (210, 236, 240))
    t.rect(8, 22, 18, 10, (120, 190, 200))
    t.rect(36, 14, 22, 30, (196, 226, 230))
    for k in range(4):
        t.rect(38, 17 + k * 7, 18, 1, (120, 170, 180))
        for i in range(4):
            t.rect(39 + i * 4, 13 + k * 7, 2, 4, [(230, 80, 80), (240, 240, 240), (80, 170, 230), (240, 200, 60)][(i + k) % 4])
    for k in range(-64, 64, 20):
        for i in range(6):
            for y in range(S):
                x = k + i + y // 2
                if 0 <= x < S:
                    t.set(x, y, mix(t.get(x, y), (255, 255, 255), 0.15))
    t.rect(0, 0, S, 4, (200, 204, 208))
    t.rect(0, 0, 3, S, (200, 204, 208))
    t.rect(61, 0, 3, S, (200, 204, 208))
    t.rect(0, 46, S, 18, (222, 226, 228))
    t.rect(0, 46, S, 2, LAB_BLUE)
    t.rect(0, 60, S, 4, (120, 124, 130))
    return t


def xray_wall():
    t = T((52, 56, 60))
    t.noise(3)
    for (x0, y0) in [(3, 8), (33, 8)]:
        t.rect(x0, y0, 28, 34, (30, 32, 34))
        t.rect(x0 + 2, y0 + 2, 24, 30, (226, 236, 244))
        # radiografie pulmonară: fond albăstrui, coaste
        t.rect(x0 + 4, y0 + 4, 20, 26, (40, 50, 70))
        for k in range(6):
            for i in range(9):
                yy = y0 + 8 + k * 4 + i // 3
                t.set(x0 + 5 + i, yy, (190, 200, 214))
                t.set(x0 + 22 - i, yy, (190, 200, 214))
        t.rect(x0 + 13, y0 + 5, 2, 24, (210, 220, 232))
        t.rect(x0 + 10, y0 + 20, 6, 5, (170, 180, 196))
    t.rect(0, 46, S, 2, (80, 84, 88))
    t.rect(0, 60, S, 4, (30, 30, 32))
    return t


def pharmacy_shelf():
    t = T((236, 236, 232))
    t.noise(2)
    cols = [(240, 240, 240), (90, 150, 220), (70, 170, 110), (230, 90, 80), (240, 200, 70), (200, 200, 210)]
    for row in range(4):
        y = 3 + row * 14
        t.rect(0, y + 11, S, 2, (190, 190, 186))
        x = 2
        while x < 60:
            w = rnd.randint(4, 8)
            h = rnd.randint(6, 10)
            c = rnd.choice(cols)
            if rnd.random() < 0.12:       # gol pe raft (au luat medicamentele)
                x += w + 2
                continue
            t.rect(x, y + 11 - h, w, h, c)
            t.rect(x, y + 11 - h, w, 1, mul(c, 1.15))
            t.rect(x + 1, y + 11 - h + 2, w - 2, 1, mul(c, 0.7))
            x += w + 1
    t.rect(0, 0, S, 2, (170, 170, 166))
    t.rect(0, 0, 2, S, (170, 170, 166))
    t.rect(62, 0, 2, S, (170, 170, 166))
    t.rect(0, 59, S, 5, (150, 150, 146))
    return t


def lockers():
    t = T((96, 110, 124))
    for k in range(3):
        x = k * 21 + 1
        c = mul((104, 120, 136), 0.95 + 0.05 * k)
        t.rect(x, 1, 20, 58, c)
        t.bevel(x, 1, 20, 58, mul(c, 1.25), mul(c, 0.6))
        for y in range(5, 15, 3):
            t.rect(x + 5, y, 10, 1, mul(c, 0.55))
        t.rect(x + 7, 18, 6, 4, (230, 230, 220))
        draw_text(t, x + 8, 18, str(12 + k)[-1], (30, 30, 30))
        t.rect(x + 15, 28, 2, 6, (200, 200, 196))
        if k == 1:      # ușă întredeschisă
            t.rect(x + 18, 2, 2, 56, (16, 16, 18))
    t.noise(3)
    t.grime(6, 0.85, 2)
    t.rect(0, 59, S, 5, (40, 44, 48))
    return t


def chapel_wall(variant=0):
    t = T((70, 44, 30))
    for x in range(S):
        if x % 16 == 0:
            t.rect(x, 0, 1, S, (40, 24, 16))
        elif x % 16 == 1:
            t.rect(x, 0, 1, S, (96, 64, 44))
    t.noise(5)
    t.rect(0, 40, S, 2, (110, 76, 50))
    t.rect(0, 60, S, 4, (30, 18, 12))
    if variant == 1:    # icoană (ramă aurie, aureolă) și o candelă
        t.rect(20, 4, 24, 30, (190, 150, 60))
        t.rect(22, 6, 20, 26, (60, 40, 50))
        for j in range(-6, 7):
            for i in range(-6, 7):
                if 25 <= i * i + j * j <= 36:
                    t.set(32 + i, 14 + j, (230, 190, 80))
        t.rect(29, 11, 6, 7, (196, 150, 120))
        t.rect(26, 20, 12, 12, (110, 40, 40))
        t.rect(31, 36, 2, 4, (150, 110, 40))
        t.rect(30, 34, 4, 3, (200, 40, 30))
        t.set(31, 33, (255, 220, 120))
        t.set(32, 32, (255, 240, 180))
    elif variant == 2:  # suport de lumânări
        t.rect(6, 44, 52, 4, (150, 120, 50))
        for k, x in enumerate(range(10, 56, 6)):
            h = 6 + (k * 5) % 7
            t.rect(x, 44 - h, 2, h, (236, 226, 190))
            t.set(x, 43 - h, (255, 200, 80))
            t.set(x + 1, 42 - h, (255, 240, 170))
            t.set(x, 42 - h, (255, 170, 60))
    return t


def cafe_wall():
    up, lo = PAL['k']
    t = T(up)
    t.noise(4)
    t.rect(0, 40, S, 24, lo)
    for x in range(0, S, 8):
        t.rect(x, 40, 1, 20, mul(lo, 0.8))
    t.rect(0, 40, S, 2, mul(lo, 1.3))
    # tabla cu meniul
    t.rect(8, 4, 48, 30, (36, 40, 36))
    t.bevel(8, 4, 48, 30, (130, 96, 60), (70, 46, 28))
    draw_text(t, 12, 7, "MENIU", (240, 236, 220))
    for k, s in enumerate(("CAFEA 7", "CEAI  5", "COVRIG 3")):
        draw_text(t, 12, 14 + k * 6, s, (220, 214, 190))
    t.rect(0, 60, S, 4, (50, 34, 24))
    return t


def window_dawn(blinds=False):
    t = T((40, 50, 80))
    for y in range(S):
        f = y / 63
        c = mix((52, 58, 112), (226, 132, 104), max(0, (f - 0.15) / 0.85) ** 1.3)
        t.rect(0, y, S, 1, c)
    for (x, w, h) in [(0, 9, 20), (9, 8, 28), (18, 12, 16), (31, 6, 32), (38, 13, 22), (52, 12, 26)]:
        t.rect(x, 52 - h + 12, w, h, (34, 32, 48))
        for yy in range(52 - h + 15, 62, 4):
            for xx in range(x + 2, x + w - 1, 3):
                if rnd.random() < 0.15:
                    t.set(xx, yy, (226, 196, 120))
    if blinds:
        for y in range(4, 40, 4):
            t.rect(4, y, 56, 2, (200, 196, 186))
            t.rect(4, y + 2, 56, 1, (150, 146, 140))
    t.rect(0, 0, S, 4, (226, 228, 228))
    t.rect(0, 0, 4, S, (226, 228, 228))
    t.rect(60, 0, 4, S, (226, 228, 228))
    t.rect(30, 0, 4, 52, (226, 228, 228))
    t.rect(0, 50, S, 14, PAL['c'][1])
    t.rect(0, 50, S, 3, (236, 236, 234))
    t.rect(0, 60, S, 4, (62, 66, 64))
    return t


def entrance_glass():
    t = window_dawn()
    t.rect(0, 50, S, 10, (60, 64, 70))
    t.rect(31, 0, 2, S, (110, 114, 120))
    t.rect(4, 20, 56, 2, (230, 220, 60))
    t.rect(4, 24, 56, 2, (30, 30, 30))
    t.rect(19, 31, 26, 13, (246, 246, 240))
    t.rect(19, 31, 26, 2, (200, 30, 30))
    draw_text(t, 21, 36, "INCHIS", (200, 30, 30))
    return t


def hospital_logo():
    """Inscripția din hol, pe 4 celule (256x64): H albastru + SPITALUL UNIVERSITAR."""
    t = T((40, 40, 46), 256, 64)
    t.noise(4)
    for x in range(0, 256, 32):
        t.rect(x, 0, 1, 64, (28, 28, 32))
    t.rect(8, 6, 240, 40, (24, 52, 110))
    t.rect(8, 6, 240, 2, (60, 100, 170))
    t.rect(8, 44, 240, 2, (14, 30, 70))
    # emblema
    t.rect(16, 10, 32, 32, (240, 240, 240))
    t.rect(22, 14, 6, 24, (24, 52, 110))
    t.rect(36, 14, 6, 24, (24, 52, 110))
    t.rect(28, 23, 8, 6, (24, 52, 110))
    x = 58
    for ch in "SPITALUL":
        g = glyph(ch)
        for j in range(5):
            for i, b in enumerate(g[j]):
                if b == '1':
                    t.rect(x + i * 3, 12 + j * 3, 3, 3, (250, 250, 250))
        x += len(g[0]) * 3 + 4
    x = 58
    for ch in "UNIVERSITAR":
        g = glyph(ch)
        for j in range(5):
            for i, b in enumerate(g[j]):
                if b == '1':
                    t.rect(x + i * 2, 31 + j * 2, 2, 2, (200, 220, 250))
        x += len(g[0]) * 2 + 3
    t.rect(0, 58, 256, 6, (22, 22, 26))
    return [t.im.crop((k * 64, 0, k * 64 + 64, 64)) for k in range(4)]


def lift_panel():
    t = T((120, 126, 132))
    t.noise(4)
    t.rect(4, 4, 56, 54, (80, 86, 92))
    t.bevel(4, 4, 56, 54, (150, 156, 162), (50, 54, 58))
    t.rect(10, 9, 30, 10, (20, 30, 20))
    draw_text(t, 12, 11, "LIFT 2", (230, 90, 60))
    t.rect(44, 9, 10, 10, (40, 40, 44))
    t.rect(46, 11, 6, 6, (230, 40, 30))
    for r in range(2):
        for k in range(4):
            t.rect(10 + k * 8, 24 + r * 8, 5, 5, (40, 140, 60) if (r + k) % 3 else (220, 180, 40))
    # maneta de resetare (galbenă)
    t.rect(46, 26, 6, 26, (40, 40, 44))
    t.rect(44, 26, 10, 6, (230, 190, 30))
    t.rect(10, 44, 30, 10, (200, 200, 190))
    draw_text(t, 12, 46, "RESET", (30, 30, 30))
    t.rect(0, 60, S, 4, (50, 52, 54))
    return t


def vent_wall():
    t = T((132, 136, 138))
    t.noise(3)
    t.rect(4, 4, 56, 52, (60, 62, 64))
    for y in range(6, 54, 4):
        t.rect(5, y, 54, 2, (156, 160, 162))
        t.rect(5, y + 2, 54, 1, (40, 42, 44))
    t.bevel(4, 4, 56, 52, (170, 174, 176), (70, 72, 74))
    t.grime(10, 0.82, 3)
    t.rect(0, 58, S, 6, (70, 72, 72))
    return t


def tank_wall():
    t = T((116, 132, 146))
    for by in (0, 32):
        for bx in (0, 32):
            for j in range(32):
                for i in range(32):
                    d = max(abs(i - 15.5), abs(j - 15.5))
                    f = 1.08 if d < 11 else (0.92 if d < 13 else 1.0)
                    t.set(bx + i, by + j, mul((116, 132, 146), f))
            t.frame(bx, by, 32, 32, (70, 80, 90))
            for k in range(2, 32, 6):
                t.set(bx + k, by + 1, (180, 190, 200))
                t.set(bx + 1, by + k, (180, 190, 200))
    t.noise(3)
    for _ in range(5):
        x = rnd.randrange(S)
        for y in range(rnd.randrange(30), S):
            if rnd.random() < 0.7:
                t.set(x, y, mul(t.get(x, y), 0.8))
    t.rect(0, 60, S, 4, (60, 66, 72))
    return t


def metal_cladding():
    t = T((170, 172, 170))
    for x in range(S):
        f = 0.82 if x % 16 < 3 else (1.06 if x % 16 < 6 else 1.0)
        t.rect(x, 0, 1, S, mul((170, 172, 170), f))
    t.noise(3)
    t.grime(12, 0.84, 3)
    t.rect(0, 60, S, 4, (80, 80, 78))
    return t


def parapet():
    """Marginea acoperișului: zid scund + balustradă; deasupra se vede cerul (KEY = transparent)."""
    t = T(KEY)
    t.rect(0, 50, S, 14, (132, 128, 120))
    t.noise(5, 0, 50, S, 14)
    t.rect(0, 48, S, 3, (166, 168, 170))            # glafului de tablă
    t.rect(0, 51, S, 1, (90, 90, 88))
    for y in range(52, 64):
        for x in range(S):
            if rnd.random() < 0.05:
                t.set(x, y, (100, 98, 92))
    # balustrada
    for x in (6, 38):
        t.rect(x, 33, 2, 15, (90, 96, 100))
        t.rect(x, 33, 1, 15, (150, 156, 160))
    t.rect(0, 33, S, 2, (110, 116, 120))
    t.rect(0, 33, S, 1, (180, 186, 190))
    t.rect(0, 41, S, 1, (100, 106, 110))
    return t


def stair_door():
    t = T((120, 124, 128))
    t.rect(8, 2, 48, 62, (128, 132, 134))
    t.bevel(8, 2, 48, 62, (170, 174, 176), (70, 72, 74))
    t.rect(4, 0, 4, S, (80, 82, 86))
    t.rect(56, 0, 4, S, (60, 62, 66))
    t.rect(14, 8, 36, 12, (24, 120, 60))
    draw_text(t, 18, 12, "IESIRE", (230, 255, 230))
    t.rect(10, 34, 44, 3, (200, 200, 196))
    t.rect(10, 37, 44, 1, (100, 100, 100))
    t.noise(3, 8, 2, 48, 62)
    t.grime(5, 0.85, 2)
    return t


def hosp_door(stripe=None, base=(206, 204, 196)):
    t = T((90, 92, 96))
    t.rect(8, 2, 48, 62, base)
    t.noise(3, 8, 2, 48, 62)
    t.bevel(8, 2, 48, 62, mul(base, 1.1), mul(base, 0.65))
    t.rect(4, 0, 4, S, (150, 152, 156))
    t.rect(56, 0, 4, S, (120, 122, 126))
    t.rect(4, 0, 56, 2, (160, 162, 166))
    t.rect(8, 50, 48, 12, (170, 174, 178))           # tablă de protecție jos
    t.rect(8, 50, 48, 1, (210, 212, 216))
    t.rect(28, 8, 8, 22, (40, 50, 60))               # geam îngust
    t.rect(28, 8, 8, 1, (110, 120, 136))
    t.bevel(27, 7, 10, 24, mul(base, 0.7), mul(base, 1.1))
    t.rect(46, 34, 6, 2, (180, 182, 186))
    if stripe:
        t.rect(8, 40, 48, 4, stripe)
        t.rect(46, 22, 6, 9, (34, 34, 38))
        t.rect(47, 23, 4, 2, stripe)
        t.set(48, 29, (230, 60, 50))
    t.grime(4, 0.86, 2)
    return t


def or_door():
    t = T((90, 92, 96))
    base = (182, 186, 190)
    for k in range(2):
        x = 6 + k * 26
        t.rect(x, 2, 26, 62, base)
        for y in range(2, 64):
            for i in range(26):
                t.set(x + i, y, mul(base, 0.95 + 0.1 * math.sin((i + y * 0.3) * 0.25)))
        t.bevel(x, 2, 26, 62, (220, 222, 226), (110, 112, 116))
        for j in range(-6, 7):
            for i in range(-6, 7):
                d = i * i + j * j
                if d <= 36:
                    t.set(x + 13 + i, 16 + j, (40, 54, 60) if d < 26 else (120, 124, 128))
        t.rect(x + 3, 34, 20, 8, (160, 164, 168))
    t.rect(4, 0, 2, S, (120, 122, 126))
    t.rect(58, 0, 2, S, (120, 122, 126))
    t.rect(20, 0, 24, 1, (100, 100, 100))
    return t


def vectra_door():
    t = T((90, 94, 98))
    t.rect(4, 2, 56, 62, (150, 156, 160))
    t.noise(3, 4, 2, 56, 62)
    for y in range(50, 62):
        for x in range(4, 60):
            t.set(x, y, (230, 190, 30) if ((x + y) // 5) % 2 == 0 else (30, 30, 30))
    t.rect(31, 2, 2, 48, (70, 74, 78))
    t.rect(14, 12, 36, 20, (236, 196, 30))
    for j in range(-6, 7):
        for i in range(-6, 7):
            d = math.hypot(i, j)
            a = (math.degrees(math.atan2(j, i)) + 90) % 120
            if (2.2 <= d <= 5.4 and (a < 32 or a > 88)) or 3.6 <= d <= 4.2:
                t.set(32 + i, 21 + j, (20, 20, 20))
    draw_text(t, 16, 34, "VECTRA", (36, 86, 160))
    t.rect(52, 6, 5, 5, (230, 30, 30))
    t.rect(53, 7, 2, 2, (255, 120, 100))
    t.bevel(4, 2, 56, 62, (190, 196, 200), (70, 74, 78))
    return t


# ---------------------------------------------------------------- podele
def vinyl(base=(176, 174, 162), chips=None, seams=True):
    t = T(base)
    t.noise(4)
    chips = chips or [mul(base, 0.78), mul(base, 1.12), (150, 150, 140)]
    t.speckle(260, chips)
    if seams:
        t.rect(0, 0, S, 1, mul(base, 0.86))
        t.rect(0, 0, 1, S, mul(base, 0.86))
    t.grime(6, 0.9, 3)
    return t


def terrazzo():
    t = T((152, 148, 140))
    t.noise(5)
    t.speckle(420, [(110, 106, 100), (190, 186, 178), (120, 90, 70), (80, 80, 84), (200, 196, 190)])
    t.rect(0, 0, S, 1, (180, 150, 80))
    t.rect(0, 0, 1, S, (180, 150, 80))
    t.rect(0, 32, S, 1, (170, 140, 76))
    t.rect(32, 0, 1, S, (170, 140, 76))
    return t


def kids_floor():
    t = T((200, 200, 200))
    cols = [(232, 196, 130), (150, 196, 226), (176, 214, 156), (232, 168, 168)]
    for j in range(4):
        for i in range(4):
            c = cols[(i + j * 2) % 4]
            t.rect(i * 16, j * 16, 16, 16, c)
    t.noise(3)
    for k in range(0, S, 16):
        t.rect(0, k, S, 1, (160, 160, 156))
        t.rect(k, 0, 1, S, (160, 160, 156))
    return t


def lab_floor():
    t = T((172, 178, 182))
    t.noise(2)
    t.rect(0, 0, S, 1, (180, 184, 186))
    t.rect(0, 0, 1, S, (180, 184, 186))
    t.grime(3, 0.92, 3)
    return t


def or_floor():
    t = T((112, 142, 130))
    t.noise(3)
    for _ in range(40):
        x, y = rnd.randrange(S), rnd.randrange(S)
        r = rnd.randint(2, 5)
        for j in range(-r, r + 1):
            for i in range(-r, r + 1):
                if i * i + j * j <= r * r:
                    xx, yy = (x + i) % S, (y + j) % S
                    t.set(xx, yy, mul(t.get(xx, yy), 1.03))
    return t


def roof_floor():
    t = T((76, 76, 74))
    t.noise(6)
    t.speckle(500, [(100, 98, 94), (60, 60, 58), (120, 116, 110), (88, 86, 80)])
    t.rect(0, 0, S, 2, (60, 60, 60))
    t.rect(0, 2, S, 1, (96, 96, 94))
    for _ in range(3):         # bălți de rouă
        x, y = rnd.randrange(S), rnd.randrange(S)
        for j in range(-3, 4):
            for i in range(-6, 7):
                if i * i / 36 + j * j / 9 <= 1:
                    xx, yy = (x + i) % S, (y + j) % S
                    t.set(xx, yy, mix(t.get(xx, yy), (130, 120, 150), 0.35))
    return t


def helipad(n=5):
    """Heliportul: n x n celule (pătrat de 64*n pixeli), tăiat în bucăți de 64x64."""
    N = 64 * n
    t = T((62, 66, 68), N, N)
    t.noise(5)
    t.speckle(N * N // 30, [(52, 56, 58), (74, 78, 80)])
    c = N / 2
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - c + 0.5, y - c + 0.5)
            if N * 0.36 <= d <= N * 0.385:
                t.set(x, y, (232, 196, 40))
    # perimetrul alb întrerupt
    for k in range(6, N - 6):
        if (k // 10) % 2 == 0:
            for w in range(3):
                t.set(k, 6 + w, (226, 226, 220))
                t.set(k, N - 9 + w, (226, 226, 220))
                t.set(6 + w, k, (226, 226, 220))
                t.set(N - 9 + w, k, (226, 226, 220))
    # H mare
    hw, hh = int(N * 0.30), int(N * 0.40)
    x0, y0 = int(c - hw / 2), int(c - hh / 2)
    bar = int(N * 0.075)
    t.rect(x0, y0, bar, hh, (238, 238, 232))
    t.rect(x0 + hw - bar, y0, bar, hh, (238, 238, 232))
    t.rect(x0, int(c - bar / 2), hw, bar, (238, 238, 232))
    # urme de cauciuc
    for _ in range(8):
        x, y = rnd.randrange(N), rnd.randrange(N)
        for k in range(rnd.randint(10, 30)):
            t.set(x + k, y + k // 4, (40, 40, 42))
    t.grime(30, 0.86, 5)
    return [t.im.crop((i * 64, j * 64, i * 64 + 64, j * 64 + 64)) for j in range(n) for i in range(n)]


# ---------------------------------------------------------------- tavane
def or_ceiling(light=False):
    t = T((214, 218, 216))
    t.noise(2)
    for k in (0, 32):
        t.rect(0, k, S, 1, (170, 174, 172))
        t.rect(k, 0, 1, S, (170, 174, 172))
    if light:   # lampa scialitică (vedere de jos)
        for j in range(-20, 21):
            for i in range(-20, 21):
                d = i * i + j * j
                if d <= 400:
                    t.set(32 + i, 32 + j, (150, 156, 160) if d > 300 else (60, 64, 68))
        for (ox, oy) in [(-9, -9), (9, -9), (-9, 9), (9, 9), (0, 0), (0, -12), (0, 12), (-12, 0), (12, 0)]:
            for j in range(-3, 4):
                for i in range(-3, 4):
                    if i * i + j * j <= 9:
                        t.set(32 + ox + i, 32 + oy + j, (255, 255, 246))
    return t


def lab_ceiling(light=False):
    t = T((220, 224, 226))
    t.noise(2)
    for k in (0, 32):
        t.rect(0, k, S, 1, (180, 184, 186))
        t.rect(k, 0, 1, S, (180, 184, 186))
    if light:
        t.rect(14, 14, 36, 36, (150, 154, 158))
        t.rect(16, 16, 32, 32, (250, 254, 255))
    else:
        t.rect(20, 20, 24, 24, (190, 194, 196))
        for k in range(22, 42, 3):
            t.rect(22, k, 20, 1, (150, 154, 156))
    return t


# ---------------------------------------------------------------- cerul din zori
def sky_panorama(path, w=1024, h=160):
    """Panoramă de 360° (u = unghiul, v = tangenta unghiului de elevație de la +0.5 sus la -0.3 jos).
    Orizontul e la v = 100. Soarele răsare la est (u ~ 0.75 * w, privind în jos pe hartă = +y)."""
    hor = 100
    im = Image.new("RGB", (w, h))
    px = im.load()
    r = random.Random(77)
    sun_u = int(w * 0.25)       # unghiul 90° = direcția +y pe hartă

    def ang_d(u):
        d = abs(u - sun_u) % w
        return min(d, w - d) / w * 360.0

    for y in range(h):
        tt = (hor - y) / hor          # 1 sus, 0 la orizont
        for x in range(w):
            ad = ang_d(x)
            glow = max(0.0, 1 - ad / 110.0) ** 1.6
            if y <= hor:
                top = mix((24, 26, 66), (40, 38, 92), glow * 0.5)
                midc = mix((150, 86, 120), (236, 128, 92), glow)
                lowc = mix((214, 132, 116), (255, 196, 120), glow)
                if tt > 0.45:
                    c = mix(midc, top, min(1.0, (tt - 0.45) / 0.55) ** 0.8)
                else:
                    c = mix(lowc, midc, tt / 0.45)
            else:
                c = mix((70, 52, 70), (26, 22, 34), min(1.0, (y - hor) / 40.0))
            px[x, y] = c
    # soarele, chiar la orizont
    for y in range(hor - 14, hor + 1):
        for x in range(sun_u - 16, sun_u + 17):
            d = math.hypot(x - sun_u, (y - hor) * 1.0)
            if d < 9:
                px[x % w, y] = (255, 236, 190)
            elif d < 15:
                px[x % w, y] = mix(px[x % w, y], (255, 210, 150), (15 - d) / 6 * 0.8)
    # nori subțiri, luminați de jos
    for _ in range(26):
        cx, cy = r.randrange(w), r.randint(20, 80)
        ln = r.randint(30, 120)
        for k in range(ln):
            for j in range(r.randint(1, 3)):
                x = (cx + k) % w
                y = cy + j + int(2 * math.sin(k * 0.05))
                glow = max(0.0, 1 - ang_d(x) / 120.0)
                px[x, y] = mix(px[x, y], mix((120, 80, 110), (250, 170, 130), glow), 0.55)
    # orașul: siluete de blocuri cu ferestre aprinse
    x = 0
    while x < w:
        bw = r.randint(6, 22)
        bh = r.choice([4, 6, 7, 9, 10, 12, 14]) if r.random() < 0.85 else r.randint(18, 30)
        base = mix((34, 30, 46), (52, 40, 56), r.random())
        for xx in range(x, min(w, x + bw)):
            for yy in range(hor - bh, h):
                px[xx, yy] = mix(base, (20, 18, 28), min(1.0, max(0, yy - hor) / 50.0))
        for yy in range(hor - bh + 2, h - 6, 3):
            for xx in range(x + 1, min(w, x + bw) - 1, 2):
                if r.random() < (0.10 if yy < hor + 10 else 0.05):
                    px[xx, yy] = (236, 190, 110) if r.random() < 0.8 else (180, 210, 240)
        if bh >= 18:
            px[x + bw // 2, hor - bh - 1] = (255, 40, 30)      # balizele roșii de pe turnuri
        x += bw + r.randint(0, 3)
    # fum în depărtare (orașul arde)
    for (sx, height) in [(int(w * 0.62), 50), (int(w * 0.86), 34), (int(w * 0.05), 40)]:
        for k in range(height):
            y = hor - 4 - k
            rad = 2 + k // 6
            for i in range(-rad, rad + 1):
                xx = (sx + i + int(6 * math.sin(k * 0.12))) % w
                if 0 <= y < h and r.random() < 0.8:
                    px[xx, y] = mix(px[xx, y], (60, 50, 58), 0.55)
    # ceață joasă peste oraș
    for y in range(hor - 3, h):
        a = 0.25 if y < hor + 6 else 0.12
        for x in range(w):
            px[x, y] = mix(px[x, y], (120, 90, 110), a)
    im.save(path)


def build(add):
    """Adaugă texturile spitalului în atlas (numele sunt cele căutate de Art.java)."""
    for k in range(6):
        add("hosp_c%d" % k, hosp_wall('c', k))
    for z in "uwoifrk":
        add("hosp_%s" % z, hosp_wall(z, 0))
        add("hosp_%s1" % z, hosp_wall(z, (1, 3, 5, 2, 1, 5, 3)["uwoifrk".index(z)]))
    signs = [("sign_urg", "URGENTE", ic_cross), ("sign_rad", "RADIOLOGIE", ic_rad), ("sign_farm", "FARMACIE", ic_pill),
             ("sign_ati", "ATI", ic_bed), ("sign_bloc", "BLOC\nOPERATOR", ic_knife), ("sign_ped", "PEDIATRIE", ic_bear),
             ("sign_lab", "LABORATOR", ic_flask), ("sign_scara", "SCARA", ic_stairs), ("sign_heli", "HELIPORT", ic_h),
             ("sign_int", "INTERNARI", ic_bed), ("sign_cafe", "CAFENEA", ic_cup)]
    for (name, text, icon) in signs:
        add(name, sign_wall(text, icon))
    add("curtain", er_curtain())
    add("tiles", tile_wall())
    add("tiles_blue", tile_wall((214, 226, 232), (150, 164, 172), 8, (60, 110, 170)))
    add("or0", or_wall(0))
    add("or1", or_wall(1))
    add("or2", or_wall(2))
    add("icu", icu_wall())
    add("kids0", kids_wall(0))
    add("kids1", kids_wall(1))
    add("lab0", lab_wall(0))
    add("lab1", lab_wall(1))
    add("lab2", lab_wall(2))
    add("lab_glass", lab_glass())
    add("xray", xray_wall())
    add("pharmacy", pharmacy_shelf())
    add("pharmacy2", pharmacy_shelf())
    add("lockers", lockers())
    add("chapel0", chapel_wall(0))
    add("chapel1", chapel_wall(1))
    add("chapel2", chapel_wall(2))
    add("cafe", cafe_wall())
    add("window", window_dawn(False))
    add("window2", window_dawn(True))
    add("entrance", entrance_glass())
    for k, im in enumerate(hospital_logo()):
        add("logo%d" % k, im)
    add("marble", stone_panel())
    add("monitors0", monitor_wall(0))
    add("monitors1", monitor_wall(1))
    add("panel", electrical_panel())
    add("liftpanel", lift_panel())
    add("garage", garage_door())
    add("elevator", elevator())
    add("concrete", concrete_blocks())
    add("wc", wc_tiles())
    add("servers", server_rack())
    add("vent", vent_wall())
    add("tank", tank_wall())
    add("metal", metal_cladding())
    add("parapet", parapet())
    add("kitchen", kitchen_tiles())
    # uși
    add("door", hosp_door())
    add("door_blue", hosp_door((40, 100, 220)))
    add("door_red", hosp_door((210, 40, 40)))
    add("door_yellow", hosp_door((230, 190, 40)))
    add("door_green", hosp_door((40, 180, 80)))
    add("door_exit", stair_door())
    add("door_or", or_door())
    add("door_vectra", vectra_door())
    add("door_entrance", entrance_glass())
    # podele
    add("f_vinyl", vinyl())
    add("f_vinyl_blue", vinyl((136, 156, 176)))
    add("f_vinyl_green", vinyl((150, 172, 150)))
    add("f_terrazzo", terrazzo())
    add("f_or", or_floor())
    add("f_kids", kids_floor())
    add("f_lab", lab_floor())
    add("f_wc", wc_floor())
    add("f_concrete", concrete_floor())
    add("f_dock", concrete_floor(True))
    add("f_wood", wood_floor())
    add("f_lobby", lobby_floor())
    add("f_roof", roof_floor())
    for k, im in enumerate(helipad(5)):
        add("f_heli%d" % k, im)
    # tavane
    add("c_tiles", ceiling_tiles(False))
    add("c_tiles_l", ceiling_tiles(True))
    add("c_lobby", lobby_ceiling(False))
    add("c_lobby_l", lobby_ceiling(True))
    add("c_concrete", concrete_ceiling(False))
    add("c_concrete_l", concrete_ceiling(True))
    add("c_red", red_light_ceiling())
    add("c_or", or_ceiling(False))
    add("c_or_l", or_ceiling(True))
    add("c_lab", lab_ceiling(False))
    add("c_lab_l", lab_ceiling(True))
