"""Generează hărțile clădirii ZORI TV (assets/maps/*.txt) și o previzualizare de sus.

Fiecare hartă are straturile (grile de caractere, aceeași mărime):
  [struct]  pereți și uși:  . liber, # perete (textura vine din zona camerei), M piatră, 1-4 sigla,
            G geam, S burete studio, Q decor studio, V monitoare, P afiș, K faianță bucătărie, T faianță WC,
            A rafturi arhivă, R servere, C beton, E panoul generatorului, Y oglinzi machiaj, O ușă de garaj,
            H lift; uși: D simplă, b albastră, r roșie, y galbenă, g verde, s studio, x scara, e intrarea, q cifru
  [zones]   tipul camerei (podea, tavan, perete implicit): m hol, c coridor, o birou, s studio, k cantină,
            t WC, a arhivă, d tehnic/rampă, w lemn (machiaj, garderobă, invitați), r regie
  [lights]  surse de lumină: f neon (doar cu curent), F neon care pâlpâie, w spot cald (curent),
            s reflector studio (curent), r lumină roșie de avarie (fără curent), b lumină albastră de monitoare,
            u lumina zorilor de la geamuri, g semn verde de ieșire, o bec portocaliu
  [things]  listă: x y tip [unghi]   (P jucător, zombi, obiecte, notițe, personaje)
  [decor]   listă: x y nume
  [rooms]   listă: x1 y1 x2 y2 id  (camere pentru hartă și scene)
"""
import sys

from PIL import Image


class Map:
    def __init__(self, name, w, h):
        self.name, self.w, self.h = name, w, h
        self.st = [['#'] * w for _ in range(h)]
        self.zo = [['-'] * w for _ in range(h)]
        self.li = [['.'] * w for _ in range(h)]
        self.things = []
        self.decor = []
        self.rooms = []

    def carve(self, x1, y1, x2, y2, zone, room=None):
        for y in range(y1, y2 + 1):
            for x in range(x1, x2 + 1):
                self.st[y][x] = '.'
                self.zo[y][x] = zone
        if room:
            self.rooms.append((x1, y1, x2, y2, room))

    def wall(self, x, y, ch):
        self.st[y][x] = ch

    def hline(self, x1, x2, y, ch):
        for x in range(x1, x2 + 1):
            self.st[y][x] = ch

    def vline(self, x, y1, y2, ch):
        for y in range(y1, y2 + 1):
            self.st[y][x] = ch

    def door(self, x, y, ch, zone):
        self.st[y][x] = ch
        self.zo[y][x] = zone

    def light(self, x, y, ch):
        self.li[y][x] = ch

    def lights_grid(self, x1, y1, x2, y2, ch, sx, sy, ox=0, oy=0):
        for y in range(y1 + oy, y2 + 1, sy):
            for x in range(x1 + ox, x2 + 1, sx):
                if self.st[y][x] == '.':
                    self.li[y][x] = ch

    def thing(self, x, y, t, a=None):
        self.things.append((x, y, t, a))

    def put(self, x, y, d):
        self.decor.append((x, y, d))

    def write(self, path):
        with open(path, "w") as f:
            f.write("name %s\nsize %d %d\n" % (self.name, self.w, self.h))
            for tag, g in (("struct", self.st), ("zones", self.zo), ("lights", self.li)):
                f.write("[%s]\n" % tag)
                for row in g:
                    f.write("".join(row) + "\n")
            f.write("[things]\n")
            for (x, y, t, a) in self.things:
                f.write("%.2f %.2f %s%s\n" % (x, y, t, "" if a is None else " %d" % a))
            f.write("[decor]\n")
            for (x, y, d) in self.decor:
                f.write("%.2f %.2f %s\n" % (x, y, d))
            f.write("[rooms]\n")
            for r in self.rooms:
                f.write("%d %d %d %d %s\n" % r)
            f.write("[end]\n")

    def preview(self, path, scale=12):
        zc = {'m': (120, 110, 100), 'c': (100, 112, 100), 'o': (80, 90, 120), 's': (40, 40, 48), 'k': (190, 190, 180),
              't': (120, 150, 136), 'a': (100, 96, 90), 'd': (90, 90, 88), 'w': (130, 92, 60), 'r': (60, 60, 90),
              'u': (90, 120, 170), 'i': (90, 130, 150), 'p': (220, 200, 120), 'l': (210, 214, 220),
              'f': (120, 170, 120), 'n': (110, 70, 40), 'v': (100, 110, 130), 'x': (70, 70, 70),
              'e': (60, 64, 70), 'h': (230, 200, 60), '-': (0, 0, 0)}
        sc = {'#': (20, 20, 20), 'M': (60, 60, 64), 'G': (90, 130, 200), 'S': (30, 30, 34), 'Q': (40, 80, 160),
              'V': (60, 160, 220), 'P': (200, 120, 60), 'K': (220, 220, 220), 'T': (140, 170, 150), 'A': (150, 120, 80),
              'R': (60, 200, 90), 'C': (110, 110, 106), 'E': (230, 200, 40), 'Y': (250, 230, 160), 'O': (200, 160, 40),
              'H': (170, 170, 180), 'U': (150, 186, 206), 'X': (200, 220, 240), 'I': (90, 140, 160),
              'L': (230, 190, 30), 'B': (236, 196, 30), 'W': (40, 90, 160), 'Z': (130, 130, 130), 'N': (100, 116, 130),
              'F': (120, 80, 50), '1': (230, 140, 40), '2': (230, 140, 40), '3': (230, 140, 40), '4': (230, 140, 40)}
        dc = {'D': (160, 110, 60), 'b': (40, 100, 240), 'r': (230, 40, 40), 'y': (240, 200, 40), 'g': (40, 200, 80),
              's': (200, 30, 30), 'x': (40, 220, 90), 'e': (120, 200, 255), 'q': (180, 180, 190),
              'v': (255, 140, 0), 'o': (200, 200, 210)}
        im = Image.new("RGB", (self.w * scale, self.h * scale), (0, 0, 0))
        px = im.load()
        for y in range(self.h):
            for x in range(self.w):
                ch = self.st[y][x]
                if ch == '.':
                    col = zc.get(self.zo[y][x], (255, 0, 255))
                elif ch in dc:
                    col = dc[ch]
                else:
                    col = sc.get(ch, (255, 0, 255))
                for j in range(scale):
                    for i in range(scale):
                        px[x * scale + i, y * scale + j] = col
                l = self.li[y][x]
                if l != '.':
                    lc = {'f': (255, 255, 255), 'F': (200, 200, 255), 'w': (255, 220, 160), 's': (255, 255, 230),
                          'r': (255, 40, 40), 'b': (80, 160, 255), 'u': (200, 140, 220), 'g': (40, 255, 80),
                          'o': (255, 160, 40)}.get(l, (255, 0, 255))
                    for j in range(scale // 2 - 2, scale // 2 + 2):
                        for i in range(scale // 2 - 2, scale // 2 + 2):
                            px[x * scale + i, y * scale + j] = lc
        for (x, y, d) in self.decor:
            cx, cy = int(x * scale), int(y * scale)
            for j in range(-2, 3):
                for i in range(-2, 3):
                    if 0 <= cx + i < im.width and 0 <= cy + j < im.height:
                        px[cx + i, cy + j] = (180, 140, 255)
        for (x, y, t, a) in self.things:
            cx, cy = int(x * scale), int(y * scale)
            col = (255, 255, 0) if t == 'P' else ((255, 60, 60) if t[0] == 'z' else (60, 255, 255))
            for j in range(-3, 4):
                for i in range(-3, 4):
                    if abs(i) + abs(j) <= 3 and 0 <= cx + i < im.width and 0 <= cy + j < im.height:
                        px[cx + i, cy + j] = col
        im.save(path)


def parter():
    """Parterul ZORI TV: 48 x 30 celule (o celulă ~ 2 m, peretele = o celulă înălțime), 17 camere."""
    m = Map("parter", 48, 30)
    # ------------------------------------------------ coridorul principal (est-vest)
    m.carve(1, 14, 46, 15, 'c', "coridor")
    # ------------------------------------------------ nord: arhiva, coridorul A, montaj, regie
    m.carve(1, 1, 9, 12, 'a', "arhiva")
    for y in (3, 6, 9):
        m.hline(2, 7, y, 'A')
    m.door(5, 13, 'b', 'a')
    m.carve(11, 1, 11, 13, 'c', "coridor_a")
    m.wall(11, 0, 'x')
    m.carve(13, 1, 17, 5, 'o', "montaj")
    m.door(12, 3, 'D', 'o')
    m.carve(13, 7, 17, 12, 'r', "regie")
    m.door(12, 10, 'D', 'r')
    m.vline(18, 8, 11, 'V')
    # studioul 1
    m.carve(20, 1, 31, 12, 's', "studio1")
    m.hline(22, 29, 0, 'Q')
    m.door(25, 13, 's', 's')
    m.door(26, 13, 's', 's')
    # coridorul B, studioul 2, machiaj
    m.carve(33, 1, 33, 13, 'c', "coridor_b")
    m.carve(35, 1, 41, 6, 's', "studio2")
    m.hline(36, 40, 0, 'Q')
    m.door(34, 3, 's', 's')
    m.carve(35, 9, 41, 12, 'w', "machiaj")
    m.hline(35, 41, 8, 'Y')
    m.door(34, 10, 'D', 'w')
    # camera tehnică și rampa
    m.carve(43, 1, 46, 5, 'd', "tehnic")
    m.hline(43, 46, 0, 'R')
    m.wall(47, 2, 'R')
    m.wall(47, 3, 'E')
    m.wall(47, 4, 'R')
    m.door(45, 6, 'r', 'd')
    m.carve(43, 7, 46, 12, 'l', "rampa")
    m.vline(47, 8, 11, 'O')
    m.door(45, 13, 'y', 'l')
    # ------------------------------------------------ sud: cantina, bucătăria, WC, garderoba
    m.carve(1, 17, 9, 23, 'k', "cantina")
    m.door(5, 16, 'D', 'k')
    m.carve(1, 25, 9, 28, 'k', "bucatarie")
    m.door(5, 24, 'D', 'k')
    m.carve(11, 17, 13, 21, 't', "wc")
    m.door(12, 16, 'D', 't')
    m.carve(11, 23, 14, 28, 'w', "garderoba")
    m.door(15, 26, 'D', 'w')
    # holul de la intrare
    m.carve(16, 17, 29, 28, 'm', "hol")
    for x in (17, 18, 27, 28):
        m.carve(x, 16, x, 16, 'm')
    m.wall(16, 16, 'M')
    m.wall(19, 16, 'H')
    m.wall(20, 16, 'H')
    for k, x in enumerate(range(21, 25)):
        m.wall(x, 16, "1234"[k])
    m.wall(25, 16, 'M')
    m.wall(26, 16, 'M')
    m.wall(29, 16, 'M')
    m.hline(16, 29, 29, 'G')
    for x in range(21, 25):
        m.door(x, 29, 'e', 'm')
    m.vline(15, 18, 21, 'V')
    # paza, invitații, biroul directorului, magazia
    m.carve(31, 17, 36, 22, 'o', "paza")
    m.door(30, 20, 'D', 'o')
    m.vline(37, 18, 21, 'V')
    m.carve(31, 24, 36, 28, 'w', "invitati")
    m.door(30, 26, 'D', 'w')
    m.carve(39, 17, 46, 21, 'o', "director")
    m.door(42, 16, 'D', 'o')
    m.carve(39, 23, 46, 28, 'd', "magazie")
    m.door(43, 22, 'D', 'd')

    # ------------------------------------------------ lumini
    # coridoare: neoane (cu curent) + lumini roșii de avarie (fără curent)
    m.lights_grid(1, 14, 46, 15, 'f', 4, 2, 2, 0)
    for x in (3, 13, 22, 31, 40):
        m.light(x, 15, 'r')
    m.light(11, 2, 'g')
    m.lights_grid(11, 1, 11, 13, 'f', 1, 4, 0, 2)
    m.light(11, 8, 'r')
    m.lights_grid(33, 1, 33, 13, 'f', 1, 4, 0, 1)
    m.light(33, 11, 'F')
    m.light(33, 7, 'r')
    # hol: zorii prin geamuri, spoturi, sigla
    for x in range(17, 30, 3):
        m.light(x, 28, 'u')
    m.lights_grid(16, 17, 29, 28, 'w', 4, 4, 2, 1)
    m.light(22, 17, 'o')
    m.light(16, 20, 'b')
    m.light(27, 17, 'r')
    # studiouri: reflectoare doar deasupra platoului; fără curent doar monitoarele
    for (x, y) in [(23, 3), (26, 3), (29, 3), (24, 7), (28, 7), (26, 10)]:
        m.light(x, y, 's')
    m.light(26, 1, 'b')
    m.light(21, 11, 'r')
    m.light(30, 11, 'r')
    for (x, y) in [(37, 2), (40, 4)]:
        m.light(x, y, 's')
    m.light(36, 5, 'r')
    # restul camerelor
    m.lights_grid(1, 1, 9, 12, 'o', 4, 6, 1, 1)
    m.light(8, 12, 'r')
    m.lights_grid(13, 1, 17, 5, 'f', 3, 3, 1, 1)
    m.light(17, 3, 'b')
    m.lights_grid(13, 7, 17, 12, 'w', 3, 4, 1, 1)
    for y in (8, 10):
        m.light(17, y, 'b')
    m.lights_grid(35, 9, 41, 12, 'w', 3, 3, 1, 0)
    m.light(38, 9, 'o')
    m.lights_grid(43, 1, 46, 5, 'f', 3, 3, 1, 1)
    m.light(46, 2, 'g')
    m.light(44, 4, 'r')
    m.lights_grid(43, 7, 46, 12, 'f', 3, 4, 1, 1)
    m.light(46, 9, 'u')
    m.lights_grid(1, 17, 9, 23, 'f', 4, 3, 1, 1)
    m.light(9, 23, 'F')
    m.light(1, 19, 'o')
    m.lights_grid(1, 25, 9, 28, 'F', 4, 3, 2, 1)
    m.light(8, 27, 'r')
    m.lights_grid(11, 17, 13, 21, 'F', 3, 3, 1, 1)
    m.lights_grid(11, 23, 14, 28, 'w', 3, 3, 1, 1)
    m.lights_grid(31, 17, 36, 22, 'f', 3, 3, 1, 1)
    m.light(36, 19, 'b')
    m.light(36, 21, 'b')
    m.light(31, 22, 'r')
    m.lights_grid(31, 24, 36, 28, 'w', 3, 3, 1, 1)
    m.light(36, 26, 'b')
    m.lights_grid(39, 17, 46, 21, 'w', 3, 3, 1, 1)
    m.light(46, 19, 'o')
    m.lights_grid(39, 23, 46, 28, 'f', 4, 4, 1, 1)
    m.light(45, 27, 'r')

    # ------------------------------------------------ jucătorul, personaje
    m.thing(22.5, 27.0, 'P', 270)
    m.thing(34.0, 18.6, 'I')                   # Ioana, la monitoarele pazei
    m.thing(38.6, 1.8, 'M')                    # Mara, ascunsă în studioul 2
    # infectați: g paznic, v Vectra, r reporter, c cameraman, p prezentatoare, k bucătar
    for (x, y, t) in [(18.5, 20.5, 'zg'), (27.5, 23.5, 'zr'), (7.5, 14.6, 'zc'), (15.5, 15.4, 'zr'),
                      (38.5, 14.5, 'zv'), (3.5, 19.5, 'zk'), (7.5, 21.5, 'zk'), (5.5, 27.0, 'zk'),
                      (12.0, 19.5, 'zr'), (13.0, 25.5, 'zp'), (33.5, 26.5, 'zr'), (44.5, 26.0, 'zc'),
                      (42.5, 19.5, 'zv'), (4.5, 1.5, 'zr'), (8.5, 8.0, 'zc'), (1.5, 11.5, 'zv'),
                      (11.5, 6.5, 'zg'), (15.5, 2.5, 'zc'), (24.0, 4.5, 'zp'), (29.5, 8.5, 'zc'),
                      (22.5, 10.5, 'zr'), (33.5, 6.0, 'zv'), (39.5, 11.0, 'zp'), (44.5, 9.0, 'zv'),
                      (44.0, 11.5, 'zv'), (44.5, 2.5, 'zv')]:
        m.thing(x, y, t)
    # cadavre: Sorin (tehnicianul) în regie, paznicul, un reporter
    for (x, y, t) in [(15.6, 10.6, 'dr'), (24.5, 25.5, 'dg'), (32.5, 21.5, 'dg'), (27.0, 11.0, 'dc')]:
        m.thing(x, y, t)
    # obiecte: a muniție, h trusă, 1 card albastru, 2 card roșu, 3 cheia galbenă, s pușcă, e cartușe
    for (x, y, t) in [(19.0, 25.0, 'a'), (28.5, 21.5, 'h'), (35.5, 21.5, '1'), (31.5, 17.5, 'a'),
                      (1.5, 22.5, 'h'), (9.0, 17.5, 'a'), (35.5, 27.5, 'a'), (8.5, 1.5, 's'), (1.5, 4.5, 'e'),
                      (8.5, 10.5, 'a'), (15.6, 11.1, '2'), (16.5, 7.5, 'h'), (30.5, 1.5, 'e'), (20.5, 1.5, 'a'),
                      (40.5, 5.5, 'h'), (14.0, 27.5, '3'), (43.5, 12.0, 'e'), (46.0, 1.5, 'a'), (45.5, 27.5, 'h'),
                      (12.5, 17.5, 'a'), (11.5, 1.5, 'a'), (40.5, 12.0, 'a'), (46.0, 21.0, 'e'), (2.0, 27.5, 'a')]:
        m.thing(x, y, t)
    # notițe (numărul = indicele textului)
    for k, (x, y) in enumerate([(22.5, 18.4), (32.5, 18.0), (2.0, 17.5), (31.5, 27.5), (2.5, 7.5), (16.5, 1.5),
                                (14.0, 8.0), (26.5, 5.5), (40.5, 1.5), (44.0, 4.5), (36.0, 11.5), (46.0, 12.0)]):
        m.thing(x, y, 'n%d' % k)

    # ------------------------------------------------ decor
    # hol: recepția în fața siglei, canapele, plante
    for x in (20.5, 21.5, 22.5, 23.5, 24.5, 25.5):
        m.put(x, 19.2, 'receptie')
    m.put(22.5, 18.3, 'scaun')
    m.put(17.0, 23.5, 'canapea')
    m.put(17.0, 25.5, 'canapea')
    m.put(28.4, 25.0, 'canapea')
    for (x, y) in [(16.5, 17.5), (29.4, 17.5), (16.5, 28.4), (29.4, 28.4), (19.5, 28.4), (26.5, 28.4)]:
        m.put(x, y, 'ficus' if (int(x) + int(y)) % 2 == 0 else 'planta')
    m.put(28.5, 20.0, 'apa')
    m.put(26.5, 22.5, 'cos')
    # pază
    for (x, y) in [(32.5, 17.6), (34.5, 17.6), (36.3, 20.0)]:
        m.put(x, y, 'birou')
    m.put(32.5, 18.6, 'scaun')
    m.put(31.5, 22.4, 'cutii')
    # cantina și bucătăria
    for (x, y) in [(3.0, 19.5), (7.0, 19.5), (3.0, 22.0), (7.0, 22.0)]:
        m.put(x, y, 'masa')
    m.put(1.5, 17.6, 'automat')
    m.put(9.4, 22.5, 'apa')
    m.put(9.4, 18.0, 'cos')
    for x in (2.0, 4.0, 6.0, 8.0):
        m.put(x, 25.5, 'lada')
    m.put(9.4, 28.4, 'cutii')
    # wc, garderoba
    m.put(13.5, 21.4, 'cos')
    for (x, y) in [(11.6, 23.6), (11.6, 25.6), (14.4, 23.6), (11.6, 27.6)]:
        m.put(x, y, 'haine')
    # invitați, director, magazie
    m.put(33.5, 28.4, 'canapea')
    m.put(31.6, 25.5, 'canapea')
    m.put(33.8, 25.5, 'masa')
    m.put(36.4, 24.5, 'planta')
    m.put(42.5, 18.0, 'birou')
    m.put(42.5, 19.0, 'scaun')
    m.put(46.4, 17.5, 'ficus')
    m.put(39.6, 21.4, 'cutii')
    for (x, y) in [(39.6, 23.6), (46.4, 23.6), (39.6, 28.4), (42.0, 28.4), (46.4, 26.0)]:
        m.put(x, y, 'cutii')
    m.put(44.0, 25.5, 'carucior')
    # coridoare
    for (x, y) in [(2.0, 14.3), (20.0, 14.3), (30.0, 15.7), (46.4, 15.7)]:
        m.put(x, y, 'planta' if x < 25 else 'cos')
    m.put(35.5, 14.3, 'carucior')
    m.put(9.0, 15.7, 'cutii')
    m.put(33.6, 12.6, 'lada')
    # arhiva
    for (x, y) in [(1.5, 1.6), (8.4, 4.5), (1.5, 7.5), (8.4, 11.5)]:
        m.put(x, y, 'cutii' if y < 6 else 'carucior')
    # montaj, regie
    for (x, y) in [(14.0, 1.6), (16.5, 1.6), (13.6, 5.4), (16.5, 5.4)]:
        m.put(x, y, 'birou')
    for y in (8.5, 10.0, 11.5):
        m.put(17.4, y, 'birou')
    m.put(16.4, 10.0, 'scaun')
    m.put(13.6, 12.4, 'lada')
    # studioul 1: platoul de știri
    for x in (24.5, 25.5, 26.5, 27.5):
        m.put(x, 2.5, 'masa_stiri')
    for x in (25.0, 27.0):
        m.put(x, 1.6, 'scaun')
    for (x, y) in [(23.0, 7.0), (26.0, 8.0), (29.0, 7.0)]:
        m.put(x, y, 'camera')
    for (x, y) in [(20.6, 1.6), (31.4, 1.6), (20.6, 5.0), (31.4, 5.0)]:
        m.put(x, y, 'lumina')
    for (x, y) in [(20.6, 12.4), (31.4, 12.4), (31.4, 9.0)]:
        m.put(x, y, 'lada')
    m.put(29.5, 11.0, 'monitor')
    m.put(21.0, 9.0, 'monitor')
    m.put(27.5, 11.4, 'cearsaf')
    # studioul 2
    m.put(38.0, 2.6, 'canapea')
    m.put(36.0, 5.0, 'camera')
    m.put(41.4, 1.6, 'lumina')
    m.put(35.6, 1.6, 'lumina')
    m.put(41.4, 6.4, 'lada')
    # machiaj
    for x in (36.0, 38.0, 40.0):
        m.put(x, 9.6, 'scaun_machiaj')
    m.put(41.4, 12.4, 'cutii')
    # tehnic, rampa
    m.put(43.6, 5.4, 'lada')
    m.put(46.4, 5.4, 'cutii')
    for (x, y) in [(43.6, 7.6), (46.4, 7.6), (43.6, 10.0), (46.4, 12.4)]:
        m.put(x, y, 'cutii')
    return m


if __name__ == "__main__":
    # Tura de Noapte 2: hărțile spitalului sunt în mapgen_spital.py
    import os
    import runpy
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    runpy.run_module("mapgen_spital", run_name="__main__")
