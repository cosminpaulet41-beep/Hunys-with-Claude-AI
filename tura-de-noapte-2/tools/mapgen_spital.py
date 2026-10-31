"""Hărțile Spitalului Universitar (Tura de Noapte 2): parterul, etajul 2 și acoperișul.

Legenda straturilor (vezi și mapgen.py):
  [struct] . liber; # perete (textura vine din zona din care îl privești); G geam; K faianță albă;
           J faianță albastră; T faianță WC; C beton; E generatorul; L panoul liftului de serviciu; H lift;
           O ușă de garaj; A rafturi de farmacie; X negatoscop; V monitoare; I peretele de ATI (gaze, monitor);
           U perdea; B semn de pericol biologic; Q geamul laboratorului; W sigla Vectra; Z grilă de ventilație;
           Y rezervor de apă; R balustrada acoperișului; M marmură; 1-4 inscripția spitalului; S servere;
           N dulapuri; P afișul de vaccinare; F meniul cafenelei;
           indicatoare: 5 urgențe, 6 radiologie, 7 farmacie, 8 ATI, 9 bloc operator, 0 pediatrie, ! laborator,
           @ scara, % heliport, & internări, $ cafenea;
           uși: D simplă, b albastră, r roșie, y galbenă, g verde, x scara, e intrarea, v Vectra (sigilată), o bloc
  [zones]  m hol, c coridor, u urgențe, o cabinet/birou, w salon, i ATI, p pediatrie, s bloc operator, l laborator,
           k cafenea/oficiu, t WC, r radiologie, f farmacie, n capelă, v vestiar, a tehnic/depozit, d rampă,
           x casa scării, e acoperiș (afară), h heliport (afară)
  [lights] f neon (curent), F neon care pâlpâie, w spot cald (curent), s lampă puternică (curent),
           r avarie roșie (fără curent), b monitoare, u zori la geam, g ieșire, o bec portocaliu (mereu),
           c neon rece pe circuitul de urgență (mereu), n lumânări
  [things] P jucătorul; zg paznic, zv Vectra, zp pacient, za asistentă, zm medic, zc chirurg, zs scuipător
           (zX>2 = are asupra lui cardul roșu, care cade când moare); dX cadavre; I Ioana, E dr. Elena,
           C pilotul Cristi; B Subiectul 9; X creatura (apare mai târziu); O butelie de oxigen;
           a muniție, h trusă, 1/2/3/4 carduri albastru/roșu/galben/verde, s pușcă, e cartușe, u pistol-mitralieră,
           p încărcătoare, nK notița K
"""
import sys

from mapgen import Map


def ring(m, x1, y1, x2, y2, ch):
    m.hline(x1, x2, y1, ch)
    m.hline(x1, x2, y2, ch)
    m.vline(x1, y1, y2, ch)
    m.vline(x2, y1, y2, ch)


# ====================================================================== episodul 1: parterul
def parter():
    m = Map("parter", 52, 33)
    # ------------------------------------------------ nord
    m.carve(1, 1, 11, 6, 'd', "rampa")                     # rampa ambulanțelor
    m.vline(0, 2, 5, 'O')
    m.carve(1, 8, 7, 10, 'o', "paza")                      # paza (Ioana)
    m.door(4, 7, 'D', 'o')
    m.vline(0, 8, 10, 'V')
    m.carve(9, 7, 10, 10, 'c')                             # trecerea rampă - coridor
    m.carve(13, 1, 27, 10, 'u', "urgente")                 # urgențele
    m.door(12, 2, 'D', 'u')
    m.door(12, 3, 'D', 'u')
    for x in (16, 19, 22, 25):                             # boxele cu perdele
        m.vline(x, 1, 3, 'U')
    m.hline(21, 27, 5, '#')
    m.vline(21, 5, 10, '#')
    m.door(24, 5, 'D', 'u')
    m.rooms.append((22, 6, 27, 10, "resuscitare"))
    m.door(16, 11, 'D', 'u')
    m.door(17, 11, 'D', 'u')
    m.wall(15, 11, '5')
    m.carve(29, 1, 39, 10, 'r', "radiologie")               # radiologia
    m.hline(29, 39, 6, '#')
    m.vline(35, 1, 5, 'X')
    m.door(31, 6, 'D', 'r')
    m.door(37, 6, 'D', 'r')
    m.rooms.append((36, 1, 39, 5, "camera_comanda"))
    m.rooms.append((29, 1, 34, 5, "ct"))
    m.door(32, 11, 'b', 'r')
    m.wall(31, 11, '6')
    m.carve(41, 1, 44, 10, 'n', "capela")                  # capela
    m.door(42, 11, 'D', 'n')
    m.carve(46, 1, 50, 10, 'a', "tehnic")                  # camera tehnică
    m.door(48, 11, 'r', 'a')
    m.wall(51, 3, 'S')
    m.wall(51, 4, 'E')
    m.wall(51, 5, 'S')
    m.hline(46, 50, 0, 'C')
    # ------------------------------------------------ coridorul principal
    m.carve(1, 12, 50, 13, 'c', "coridor")
    m.wall(25, 11, 'P')
    m.wall(9, 14, '@')
    # ------------------------------------------------ sud
    m.carve(8, 15, 9, 31, 'c', "coridor_vest")
    m.carve(8, 14, 9, 14, 'c')
    m.carve(1, 15, 6, 20, 'v', "vestiare")
    m.door(7, 17, 'D', 'v')
    m.carve(1, 22, 6, 25, 't', "wc")
    m.door(7, 23, 'D', 't')
    m.door(7, 29, 'x', 'c')                               # scara de incendiu
    m.wall(7, 28, '@')
    m.wall(8, 32, 'H')
    m.wall(9, 32, 'H')
    m.carve(11, 15, 28, 31, 'm', "hol")                    # holul mare
    m.carve(14, 14, 25, 14, 'm')
    for k, y in enumerate(range(21, 25)):
        m.wall(29, y, "1234"[k])
    m.hline(11, 16, 32, 'G')
    m.hline(22, 28, 32, 'G')
    for x in range(17, 22):
        m.door(x, 32, 'e', 'm')
    m.carve(30, 15, 37, 20, 'o', "internari")              # internări
    m.door(29, 17, 'D', 'o')
    m.wall(29, 16, '&')
    m.carve(30, 22, 37, 31, 'f', "farmacie")               # farmacia
    m.door(29, 28, 'D', 'f')
    m.wall(29, 27, '7')
    m.vline(38, 23, 30, 'A')
    m.hline(31, 36, 21, 'A')
    m.carve(39, 15, 50, 22, 'k', "cafenea")                 # cafeneaua
    m.door(44, 14, 'D', 'k')
    m.wall(43, 14, '$')
    m.vline(51, 16, 21, 'G')
    m.hline(40, 43, 14, '#')
    m.carve(39, 24, 50, 31, 'o', "cabinet")                 # cabinetul medicului de gardă
    m.door(44, 23, 'D', 'o')
    m.vline(51, 25, 30, 'G')

    # ------------------------------------------------ lumini (fără curent: doar avarie, monitoare, zori)
    m.lights_grid(1, 12, 50, 13, 'f', 4, 2, 2, 0)
    for x in (4, 14, 24, 34, 44):
        m.light(x, 13, 'r')
    m.light(8, 30, 'g')
    m.lights_grid(8, 15, 9, 31, 'f', 1, 4, 0, 2)
    m.light(9, 22, 'r')
    m.lights_grid(1, 1, 11, 6, 'f', 4, 3, 1, 1)
    m.light(1, 3, 'u')
    m.light(10, 2, 'r')
    m.light(3, 9, 'b')
    m.light(1, 9, 'b')
    m.lights_grid(1, 8, 7, 10, 'w', 4, 3, 2, 1)
    m.lights_grid(13, 1, 27, 10, 'f', 3, 3, 1, 1)
    for (x, y) in [(14, 4), (20, 8), (26, 2)]:
        m.light(x, y, 'r')
    m.light(25, 8, 'b')
    m.lights_grid(29, 1, 39, 10, 'f', 3, 3, 1, 1)
    m.light(38, 2, 'b')
    m.light(30, 9, 'r')
    m.light(32, 2, 'b')
    m.light(42, 3, 'n')
    m.light(43, 8, 'n')
    m.light(42, 6, 'F')
    m.lights_grid(46, 1, 50, 10, 'f', 3, 4, 1, 1)
    m.light(50, 3, 'g')
    m.light(47, 9, 'r')
    m.lights_grid(1, 15, 6, 20, 'F', 3, 3, 1, 1)
    m.light(6, 18, 'r')
    m.lights_grid(1, 22, 6, 25, 'F', 3, 3, 1, 1)
    m.lights_grid(11, 15, 28, 31, 'w', 4, 4, 1, 1)
    for x in range(12, 29, 3):
        m.light(x, 31, 'u')
    m.light(28, 22, 'o')
    m.light(11, 18, 'r')
    m.light(27, 16, 'r')
    m.lights_grid(30, 15, 37, 20, 'f', 3, 3, 1, 1)
    m.light(35, 19, 'r')
    m.lights_grid(30, 22, 37, 31, 'f', 3, 3, 1, 1)
    m.light(31, 30, 'r')
    m.lights_grid(39, 15, 50, 22, 'w', 4, 3, 1, 1)
    m.light(50, 18, 'u')
    m.light(40, 21, 'o')
    m.lights_grid(39, 24, 50, 31, 'f', 4, 4, 1, 1)
    m.light(50, 27, 'u')
    m.light(40, 30, 'r')

    # ------------------------------------------------ personaje și obiecte
    m.thing(3.0, 4.8, 'P', 0)
    m.thing(4.0, 8.6, 'I')                                 # Ioana, la monitoarele pazei
    m.thing(38.0, 2.4, 'E')                                # dr. Elena, în camera de comandă a radiologiei
    for (x, y, t) in [(14.5, 8.5, 'za>1'), (18.5, 6.5, 'zp'), (17.5, 2.0, 'zp'), (23.5, 2.0, 'zp'),
                      (26.5, 2.0, 'zm'), (24.0, 8.5, 'zp'), (26.0, 9.5, 'za'), (9.5, 5.0, 'zg'),
                      (9.5, 9.5, 'zv'), (20.0, 12.5, 'zp'), (36.0, 13.0, 'zv'), (46.0, 12.6, 'zp'),
                      (3.5, 12.6, 'zg'), (31.5, 3.0, 'zp'), (30.5, 8.5, 'zm'), (34.5, 9.5, 'zp'),
                      (43.0, 3.0, 'zp'), (42.5, 9.0, 'za'), (48.5, 3.0, 'zv'), (49.0, 8.5, 'zv'),
                      (2.5, 17.0, 'zg'), (4.5, 19.5, 'zp'), (3.5, 23.5, 'zp'), (8.5, 21.0, 'zm'),
                      (14.0, 18.5, 'zp'), (22.0, 20.5, 'zp'), (17.5, 26.0, 'za'), (25.5, 28.5, 'zp'),
                      (12.5, 29.5, 'zs'), (26.5, 17.5, 'zv'), (33.5, 17.5, 'zp'), (36.0, 19.5, 'za'),
                      (33.5, 26.5, 'za>2'), (31.5, 29.5, 'zp'), (42.0, 17.5, 'zp'), (47.5, 20.0, 'za'),
                      (45.0, 16.5, 'zs'), (41.5, 27.0, 'zm'), (48.0, 29.5, 'zp'), (8.5, 26.5, 'zp')]:
        m.thing(x, y, t)
    for (x, y, t) in [(21.5, 7.5, 'dg'), (10.0, 5.5, 'dp'), (33.0, 4.0, 'da'), (44.0, 9.5, 'dp'),
                      (19.0, 24.0, 'dv'), (36.5, 29.5, 'dm')]:
        m.thing(x, y, t)
    for (x, y) in [(23.0, 9.5), (27.0, 6.0), (5.0, 1.6), (11.0, 1.6), (47.0, 5.5), (40.5, 12.2), (12.0, 12.2)]:
        m.thing(x, y, 'O')
    for (x, y, t) in [(6.5, 10.4, 'a'), (14.0, 1.6, 'h'), (26.5, 6.5, 'a'), (5.5, 15.6, 's'), (1.6, 15.6, 'e'),
                      (1.6, 25.4, 'h'), (39.0, 4.5, 'a'), (34.0, 1.6, 'e'), (44.0, 1.6, 'h'), (50.0, 10.0, 'a'),
                      (12.0, 15.6, 'a'), (28.0, 30.5, 'h'), (37.0, 15.6, 'e'), (30.6, 22.6, 'h'), (50.0, 15.6, 'a'),
                      (39.6, 31.0, 'e'), (50.0, 31.0, 'h'), (9.0, 15.4, 'a'), (1.6, 1.6, 'a'), (27.5, 12.4, 'e')]:
        m.thing(x, y, t)
    for k, (x, y) in enumerate([(2.0, 10.4), (14.5, 6.0), (26.6, 10.4), (37.5, 4.5), (42.0, 1.6), (49.5, 1.6),
                                (6.4, 20.4), (20.0, 16.5), (36.0, 15.6), (49.5, 24.6)]):
        m.thing(x, y, 'n%d' % k)

    # ------------------------------------------------ decor
    m.put(7.0, 2.2, 'ambulanta')
    for (x, y) in [(10.4, 1.6), (10.4, 6.4), (1.6, 6.4)]:
        m.put(x, y, 'cutii')
    m.put(2.0, 1.6, 'targa')
    m.put(1.8, 8.5, 'birou')
    m.put(5.5, 8.5, 'birou')
    m.put(3.5, 9.3, 'scaun')
    # urgențe
    for x in (14.5, 15.5):
        m.put(x, 6.0, 'ghiseu')
    for (x, y) in [(17.5, 1.6), (20.5, 1.6), (23.5, 1.6), (26.5, 1.6)]:
        m.put(x, y, 'pat' if x != 20.5 else 'pat_sange')
    for (x, y) in [(18.6, 1.4), (24.6, 1.4)]:
        m.put(x, y, 'perfuzie')
    m.put(15.0, 9.5, 'scaune')
    m.put(19.0, 9.5, 'rotile')
    m.put(18.0, 4.8, 'targa')
    m.put(24.5, 8.0, 'targa')
    m.put(27.3, 7.0, 'monitor_med')
    m.put(22.6, 9.4, 'urgenta')
    m.put(26.0, 10.3, 'dulap')
    m.put(13.5, 1.5, 'dulap')
    # radiologie
    m.put(32.0, 3.0, 'ct')
    m.put(36.6, 1.6, 'birou')
    m.put(39.2, 4.5, 'birou')
    m.put(30.0, 8.0, 'scaune')
    m.put(34.0, 8.0, 'scaune')
    m.put(39.0, 9.5, 'planta')
    # capela
    m.put(42.5, 2.0, 'sfesnic')
    m.put(43.5, 7.5, 'sfesnic')
    m.put(41.6, 5.0, 'targa')
    m.put(43.0, 5.5, 'cearsaf')
    # tehnic
    for (x, y) in [(46.6, 1.6), (46.6, 9.4), (49.0, 9.5)]:
        m.put(x, y, 'cutii')
    m.put(46.5, 6.0, 'butelii')
    # coridoare
    for (x, y) in [(6.0, 12.4), (30.0, 13.6), (38.0, 12.4), (50.4, 13.6), (8.4, 19.0)]:
        m.put(x, y, 'scaune' if x not in (8.4,) else 'targa')
    m.put(15.0, 12.4, 'rotile')
    m.put(22.0, 13.6, 'targa')
    m.put(45.0, 13.6, 'cos')
    m.put(9.6, 27.5, 'rotile')
    # vestiare, wc
    for (x, y) in [(1.6, 18.0), (6.4, 20.4)]:
        m.put(x, y, 'cos')
    m.put(4.0, 18.5, 'masa')
    m.put(6.4, 22.6, 'cos')
    # holul
    for k, y in enumerate((21.5, 22.5, 23.5)):
        m.put(27.2, y, 'ghiseu')
    for (x, y) in [(14.5, 19.0), (14.5, 21.5), (18.5, 19.0), (18.5, 21.5), (14.5, 25.5), (18.5, 25.5),
                   (22.5, 25.5), (14.5, 28.0), (18.5, 28.0)]:
        m.put(x, y, 'scaune')
    for (x, y) in [(11.5, 15.5), (28.4, 15.5), (11.5, 31.4), (28.4, 31.4), (16.0, 31.4), (22.5, 31.4)]:
        m.put(x, y, 'ficus' if (int(x) + int(y)) % 2 == 0 else 'planta')
    m.put(11.6, 23.0, 'automat')
    m.put(11.6, 25.0, 'apa')
    m.put(24.0, 20.0, 'rotile')
    m.put(21.0, 29.0, 'cearsaf')
    m.put(24.5, 22.0, 'cos')
    # internări, farmacie
    for (x, y) in [(31.0, 16.0), (33.5, 16.0), (36.0, 16.0)]:
        m.put(x, y, 'birou')
    m.put(33.5, 19.5, 'scaune')
    m.put(37.4, 18.5, 'cutii')
    for x in (31.0, 32.0, 33.0, 34.0):
        m.put(x, 24.5, 'ghiseu')
    m.put(36.5, 27.0, 'cutii')
    m.put(30.6, 31.4, 'carucior')
    # cafenea
    for (x, y) in [(41.0, 17.5), (44.0, 17.5), (47.0, 17.5), (41.0, 20.5), (44.0, 20.5), (47.0, 20.5)]:
        m.put(x, y, 'masa')
    m.put(50.4, 15.6, 'automat')
    m.put(39.6, 15.6, 'apa')
    m.put(50.4, 22.4, 'cos')
    # cabinet
    m.put(42.0, 25.0, 'birou')
    m.put(42.0, 26.0, 'scaun')
    m.put(47.5, 25.2, 'pat')
    m.put(49.0, 30.5, 'dulap')
    m.put(39.6, 30.5, 'planta')
    m.put(45.0, 29.0, 'perfuzie')
    return m


# ====================================================================== episodul 2: etajul
def etaj():
    m = Map("etaj", 52, 33)
    # casa scării (început) și coridorul
    m.carve(1, 12, 5, 13, 'x', "scara")
    m.door(6, 12, 'D', 'x')
    m.wall(6, 13, '@')
    m.carve(7, 12, 50, 13, 'c', "coridor2")
    m.carve(1, 1, 5, 10, 'o', "garda")                      # camera de gardă ATI
    m.door(3, 11, 'D', 'o')
    m.carve(1, 15, 5, 31, 'k', "oficiu")                    # oficiul
    m.door(3, 14, 'D', 'k')
    # ------------------------------------------------ nord: ATI, blocul operator, depozitul
    m.carve(7, 1, 22, 10, 'i', "ati")
    m.hline(8, 21, 0, 'I')
    m.door(14, 11, 'D', 'i')
    m.door(15, 11, 'D', 'i')
    m.wall(13, 11, '8')
    m.vline(16, 4, 7, '#')                                  # postul asistentelor
    m.carve(24, 8, 42, 10, 's', "bloc")
    m.door(33, 11, 'y', 's')
    m.wall(32, 11, '9')
    m.carve(24, 1, 30, 6, 's', "sala1")
    m.door(27, 7, 'o', 's')
    m.carve(32, 1, 34, 6, 's', "spalator")
    m.door(33, 7, 'o', 's')
    m.vline(31, 1, 6, 'K')
    m.vline(35, 1, 6, 'K')
    m.carve(36, 1, 42, 6, 's', "sala2")
    m.door(39, 7, 'o', 's')
    m.carve(44, 1, 50, 10, 'a', "depozit")
    m.door(47, 11, 'D', 'a')
    # ------------------------------------------------ sud: pediatria, cabinetul, WC, laboratorul Vectra
    m.carve(7, 15, 20, 31, 'p', "pediatrie")
    m.door(13, 14, 'D', 'p')
    m.wall(12, 14, '0')
    m.hline(7, 20, 32, 'G')
    m.carve(30, 14, 31, 31, 'c', "coridor_lab")
    m.carve(22, 15, 28, 21, 'o', "cabinet2")
    m.door(29, 18, 'D', 'o')
    m.carve(22, 23, 28, 26, 't', "wc2")
    m.door(29, 24, 'D', 't')
    m.carve(22, 28, 28, 31, 'o', "asistente")
    m.door(29, 29, 'D', 'o')
    m.carve(33, 20, 36, 24, 'l', "anticamera")              # laboratorul Vectra
    m.door(32, 22, 'g', 'l')
    m.wall(32, 21, '!')
    m.carve(38, 15, 50, 26, 'l', "laborator")
    m.door(37, 22, 'D', 'l')
    for y in (17, 18, 19, 24, 25):
        m.wall(37, y, 'Q')
    m.wall(44, 14, 'W')
    m.wall(40, 14, 'B')
    m.wall(48, 14, 'B')
    m.carve(38, 28, 44, 31, 'l', "izolare")
    m.carve(41, 27, 41, 27, 'l')                            # ușa celulei, smulsă
    m.carve(46, 28, 50, 31, 'x', "scara_serviciu")
    m.door(48, 27, 'v', 'x')
    m.door(48, 32, 'x', 'x')
    m.hline(38, 44, 27, 'Q')
    m.wall(41, 27, '.')

    # ------------------------------------------------ lumini: fără curent; ATI, blocul și laboratorul au circuit de urgență
    for x in range(8, 50, 5):
        m.light(x, 12, 'r')
    m.light(29, 13, 'F')
    m.light(45, 13, 'F')
    m.light(2, 12, 'g')
    m.light(5, 13, 'r')
    m.light(3, 5, 'r')
    m.light(1, 2, 'b')
    for (x, y) in [(3, 18), (3, 26)]:
        m.light(x, y, 'r')
    m.lights_grid(7, 1, 22, 10, 'c', 4, 4, 1, 1)
    for x in range(8, 22, 3):
        m.light(x, 1, 'b')
    m.lights_grid(24, 8, 42, 10, 'c', 5, 2, 1, 1)
    m.light(27, 3, 'c')
    m.light(39, 3, 'c')
    m.light(33, 3, 'F')
    m.lights_grid(44, 1, 50, 10, 'r', 4, 5, 1, 2)
    m.light(49, 1, 'o')
    m.lights_grid(7, 15, 20, 31, 'u', 4, 16, 1, 16)
    m.lights_grid(7, 15, 20, 31, 'o', 5, 5, 2, 3)
    m.light(10, 20, 'r')
    m.light(17, 26, 'r')
    m.light(25, 17, 'r')
    m.light(25, 24, 'F')
    m.light(25, 30, 'r')
    m.lights_grid(30, 14, 31, 31, 'r', 1, 6, 0, 3)
    m.light(34, 22, 'c')
    m.lights_grid(38, 15, 50, 26, 'c', 6, 5, 2, 2)
    m.light(38, 16, 'b')
    m.light(41, 29, 'r')
    m.light(48, 30, 'g')

    # ------------------------------------------------ personaje și obiecte
    m.thing(4.0, 12.8, 'P', 0)
    m.thing(44.0, 20.5, 'B')                                # Subiectul 9
    for (x, y, t) in [(10.5, 3.0, 'zp'), (18.5, 3.0, 'zp'), (9.0, 8.0, 'zp'), (20.5, 8.5, 'za'), (13.0, 5.5, 'za'),
                      (21.0, 5.0, 'zm'), (12.0, 12.6, 'zp'), (24.0, 12.4, 'za'), (39.0, 12.8, 'zp'),
                      (46.0, 12.5, 'zv'), (3.0, 3.0, 'zm'), (2.5, 20.0, 'zp'), (3.5, 28.5, 'za'),
                      (27.5, 3.5, 'zc'), (25.0, 2.0, 'zc'), (38.5, 3.0, 'zc'), (41.0, 4.5, 'zm'), (36.0, 9.0, 'zc'),
                      (28.0, 9.5, 'za'), (46.0, 3.0, 'zv'), (49.0, 8.0, 'zs'), (15.0, 18.0, 'za'),
                      (25.0, 19.0, 'zm'), (24.0, 29.5, 'za'), (30.5, 26.0, 'zp'), (34.5, 23.5, 'zv'),
                      (40.0, 17.0, 'zv'), (49.0, 24.5, 'zs'), (42.0, 30.0, 'zp'), (17.0, 29.0, 'zp')]:
        m.thing(x, y, t)
    for (x, y, t) in [(40.5, 4.0, 'dv'), (35.0, 21.0, 'dv'), (11.0, 22.0, 'da'), (26.0, 4.5, 'dp'),
                      (4.0, 9.0, 'dm'), (47.0, 30.0, 'dv')]:
        m.thing(x, y, t)
    for (x, y) in [(7.6, 1.6), (22.4, 9.4), (10.0, 10.4), (24.6, 1.6), (42.4, 1.6), (44.6, 9.4), (45.6, 9.4),
                   (36.0, 13.6), (38.6, 25.4), (50.4, 15.6)]:
        m.thing(x, y, 'O')
    for (x, y, t) in [(14.5, 6.0, '3'), (41.0, 3.5, '4'), (35.5, 20.6, 'u'), (34.0, 24.4, 'p'),
                      (1.6, 1.6, 'a'), (5.0, 10.4, 'h'), (8.0, 4.0, 'h'), (22.0, 1.6, 'e'), (33.0, 1.6, 'h'),
                      (30.0, 1.6, 'a'), (42.0, 9.5, 'e'), (45.0, 1.6, 'p'), (50.0, 1.6, 'e'), (50.0, 5.0, 'a'),
                      (8.0, 15.6, 'h'), (19.6, 31.0, 'a'), (28.0, 15.6, 'e'), (22.6, 26.0, 'h'), (28.0, 31.0, 'a'),
                      (50.0, 16.0, 'p'), (38.6, 30.6, 'h'), (5.0, 31.0, 'e'), (16.0, 12.4, 'a'), (50.0, 13.6, 'h')]:
        m.thing(x, y, t)
    for k, (x, y) in enumerate([(4.4, 1.6), (12.5, 5.0), (25.0, 5.5), (41.5, 6.0), (49.6, 1.6), (19.0, 15.6),
                                (8.0, 30.6), (27.6, 16.0), (36.0, 20.6), (49.5, 22.0), (40.0, 30.6)]):
        m.thing(x, y, 'n%d' % k)

    # ------------------------------------------------ decor
    m.put(1.6, 12.6, 'baricada')
    m.put(2.0, 3.0, 'pat')
    m.put(4.6, 6.5, 'birou')
    m.put(4.0, 7.3, 'scaun')
    m.put(1.6, 9.5, 'dulap')
    for (x, y) in [(2.0, 17.0), (4.5, 17.0), (2.0, 22.0)]:
        m.put(x, y, 'masa')
    m.put(4.6, 30.6, 'automat')
    m.put(1.6, 30.6, 'apa')
    m.put(4.6, 24.5, 'cutii')
    # ATI: paturi lângă peretele cu gaze, monitoare
    for x in (8.5, 11.5, 18.0, 21.0):
        m.put(x, 1.7, 'pat_sange' if x in (11.5,) else 'pat')
        m.put(x + 1.1, 1.3, 'monitor_med')
    for x in (9.5, 19.5):
        m.put(x, 9.3, 'pat')
    m.put(12.0, 9.3, 'pat_sange')
    for (x, y) in [(10.3, 1.4), (20.0, 1.4), (13.0, 9.6)]:
        m.put(x, y, 'perfuzie')
    m.put(14.5, 5.3, 'ghiseu')
    m.put(15.5, 5.3, 'ghiseu')
    m.put(13.5, 4.5, 'scaun')
    m.put(7.6, 5.5, 'urgenta')
    m.put(22.4, 3.5, 'dulap')
    # blocul operator
    for (x, cx) in [(27.0, 27.0), (39.0, 39.0)]:
        m.put(x, 3.5, 'masa_op')
        m.put(x, 3.5, 'lampa_op')
        m.put(x + 2.5, 2.0, 'monitor_med')
        m.put(x - 2.5, 5.0, 'urgenta')
    m.put(29.6, 1.6, 'dulap')
    m.put(36.6, 1.6, 'dulap')
    m.put(33.0, 3.0, 'carucior')
    m.put(25.0, 9.5, 'targa')
    m.put(31.0, 8.5, 'rotile')
    m.put(42.0, 9.3, 'targa')
    m.put(39.0, 5.6, 'cearsaf')
    m.put(36.5, 8.6, 'cos_bio')
    # depozit
    for (x, y) in [(44.6, 1.6), (47.0, 1.6), (50.4, 3.5), (50.4, 9.4), (47.5, 6.0)]:
        m.put(x, y, 'cutii')
    m.put(44.6, 5.0, 'butelii')
    m.put(48.5, 9.4, 'carucior')
    # coridor
    for (x, y) in [(10.0, 13.6), (27.0, 12.4), (41.0, 13.6)]:
        m.put(x, y, 'targa')
    m.put(19.0, 12.4, 'rotile')
    m.put(33.5, 13.6, 'pat')
    m.put(48.0, 12.4, 'scaune')
    # pediatria: paturi mici, jucării, desene
    for (x, y) in [(8.5, 16.0), (11.5, 16.0), (16.5, 16.0), (19.5, 16.0), (8.5, 30.6), (11.5, 30.6),
                   (16.5, 30.6), (19.5, 30.6)]:
        m.put(x, y, 'pat')
    for (x, y) in [(13.5, 22.5), (12.0, 25.5)]:
        m.put(x, y, 'masuta')
    for (x, y) in [(9.0, 22.0), (18.5, 23.0)]:
        m.put(x, y, 'jucarii')
    for (x, y) in [(10.0, 17.2), (17.0, 29.4), (14.5, 24.0), (19.4, 19.0)]:
        m.put(x, y, 'ursulet')
    m.put(7.6, 26.0, 'planta')
    m.put(20.4, 26.5, 'dulap')
    # cabinet, wc, asistente
    m.put(25.0, 16.0, 'birou')
    m.put(25.0, 17.0, 'scaun')
    m.put(22.6, 20.4, 'pat')
    m.put(28.4, 20.4, 'dulap')
    m.put(22.6, 23.6, 'cos')
    m.put(23.5, 30.6, 'canapea')
    m.put(27.0, 29.0, 'masa')
    m.put(28.4, 31.4, 'apa')
    m.put(30.6, 20.0, 'targa')
    # laboratorul Vectra
    m.put(33.6, 20.4, 'cutii')
    m.put(36.4, 20.6, 'cos_bio')
    for (x, y) in [(40.5, 15.6), (44.0, 15.6), (47.5, 15.6)]:
        m.put(x, y, 'masa_lab')
    for (x, y) in [(42.0, 23.0), (46.0, 23.0)]:
        m.put(x, y, 'masa_lab')
    for (x, y) in [(50.4, 17.0), (50.4, 19.0), (50.4, 21.0)]:
        m.put(x, y, 'frigider')
    m.put(38.6, 21.0, 'monitor_med')
    m.put(44.0, 19.5, 'carucior')
    m.put(40.5, 25.4, 'cos_bio')
    m.put(39.0, 30.6, 'pat_sange')
    m.put(43.6, 28.6, 'cearsaf')
    return m


# ====================================================================== episodul 3: acoperișul
def acoperis():
    m = Map("acoperis", 44, 36)
    # ------------------------------------------------ etajul tehnic (în clădire)
    m.carve(1, 15, 4, 18, 'x', "scara3")
    m.door(2, 19, 'x', 'x')
    m.door(5, 16, 'D', 'a')
    m.wall(5, 17, '@')
    m.carve(6, 16, 16, 17, 'a', "coridor_tehnic")
    m.carve(5, 8, 12, 14, 'a', "masini")                    # camera mașinilor liftului
    m.door(8, 15, 'r', 'a')
    m.wall(4, 11, 'L')
    m.hline(5, 12, 7, 'S')
    m.carve(5, 19, 12, 26, 'a', "rezervoare")               # rezervoarele de apă
    m.door(8, 18, 'D', 'a')
    m.vline(4, 19, 26, 'Y')
    m.hline(5, 12, 27, 'Y')
    m.carve(14, 8, 16, 14, 'a', "ventilatie")              # centrala de ventilație
    m.door(15, 15, 'D', 'a')
    m.hline(14, 16, 7, 'Z')
    m.vline(17, 8, 14, 'Z')
    m.carve(14, 19, 16, 26, 'a', "electric")
    m.door(15, 18, 'D', 'a')
    m.door(17, 16, 'D', 'a')                               # ieșirea pe acoperiș
    m.wall(17, 17, '%')
    # ------------------------------------------------ acoperișul (afară)
    m.carve(18, 1, 42, 34, 'e', "acoperis")
    m.carve(1, 1, 17, 6, 'e')
    m.carve(1, 29, 17, 34, 'e')
    # casa liftului de serviciu pe acoperiș (plină), cu ușile spre sud
    for y in range(4, 9):
        m.hline(20, 24, y, '#')
    m.hline(21, 23, 8, 'H')
    # dispeceratul heliportului (camera pilotului)
    m.carve(36, 27, 41, 32, 'o', "dispecerat")
    ring(m, 35, 26, 42, 33, '#')
    m.carve(36, 27, 41, 32, 'o')
    m.door(38, 26, 'D', 'o')
    m.wall(37, 26, 'G')
    m.wall(40, 26, 'G')
    m.wall(35, 29, 'G')
    # heliportul 5x5
    m.carve(30, 13, 34, 17, 'h', "heliport")
    # balustrada pe marginea acoperișului
    for x in range(0, 44):
        for y in (0, 35):
            m.wall(x, y, 'R')
    for y in range(0, 36):
        m.wall(43, y, 'R')
        if y <= 7 or y >= 28:
            m.wall(0, y, 'R')
    # aripa de nord și de sud a clădirii tehnice: acoperișul continuă peste ele
    for y in range(7, 28):
        m.wall(0, y, '#')

    # ------------------------------------------------ lumini (fără curent: avarie; afară e zori)
    m.light(2, 16, 'g')
    m.light(3, 18, 'r')
    m.lights_grid(6, 16, 16, 17, 'r', 5, 2, 2, 0)
    m.light(12, 16, 'F')
    m.light(6, 9, 'r')
    m.light(11, 13, 'b')
    m.light(5, 11, 'o')
    m.light(8, 22, 'r')
    m.light(11, 25, 'F')
    m.light(15, 11, 'r')
    m.light(15, 22, 'r')
    m.light(16, 25, 'b')
    for (x, y) in [(29, 12), (35, 12), (29, 18), (35, 18)]:
        m.light(x, y, 'g')
    m.lights_grid(36, 27, 41, 32, 'o', 4, 4, 1, 1)
    m.light(41, 28, 'b')
    m.light(22, 9, 'o')

    # ------------------------------------------------ personaje și obiecte
    m.thing(2.5, 16.5, 'P', 0)
    m.thing(39.5, 31.0, 'C')                                # pilotul Cristi
    m.thing(22.0, 10.5, 'X')                                # creatura (apare când pornește liftul)
    for (x, y, t) in [(9.0, 11.0, 'zv'), (11.0, 9.0, 'zm'), (14.0, 16.5, 'zp'), (9.5, 22.5, 'zp'),
                      (15.0, 24.0, 'zv'), (15.0, 9.5, 'zs'), (21.0, 14.0, 'zp'), (26.0, 3.0, 'zv'),
                      (38.0, 4.0, 'za'), (40.5, 12.0, 'zp'), (26.0, 22.0, 'zm'), (21.0, 30.0, 'zg'),
                      (31.0, 26.0, 'zs'), (39.0, 21.0, 'zp'), (6.0, 3.0, 'zp'), (12.0, 32.0, 'za'),
                      (27.5, 31.5, 'zv'), (33.0, 6.0, 'zc'), (28.0, 10.0, 'zp')]:
        m.thing(x, y, t)
    for (x, y, t) in [(37.0, 29.0, 'dv'), (24.5, 17.5, 'dp'), (13.0, 20.0, 'dg')]:
        m.thing(x, y, t)
    for (x, y) in [(19.0, 2.0), (25.5, 9.5), (36.0, 22.0), (41.5, 2.0), (19.0, 33.0), (6.0, 25.5),
                   (16.0, 12.0), (28.0, 19.5), (36.0, 10.5)]:
        m.thing(x, y, 'O')
    for (x, y, t) in [(5.5, 13.5, 'a'), (12.0, 8.5, 'h'), (5.6, 19.6, 'e'), (12.0, 26.0, 'p'), (16.0, 19.6, 'h'),
                      (14.0, 13.5, 'a'), (19.0, 8.0, 'e'), (41.0, 8.0, 'h'), (25.0, 33.0, 'a'), (41.0, 33.5, 'p'),
                      (36.5, 32.0, 'h'), (41.0, 27.6, 'e'), (2.0, 2.0, 'h'), (15.0, 33.0, 'e'), (33.0, 2.0, 'p'),
                      (24.5, 26.0, 'h'), (41.5, 17.0, 'a'), (30.0, 33.0, 'u')]:
        m.thing(x, y, t)
    for k, (x, y) in enumerate([(4.0, 15.5), (11.5, 13.5), (5.6, 26.0), (16.0, 8.4), (16.0, 26.0), (22.0, 3.0),
                                (40.0, 27.6)]):
        m.thing(x, y, 'n%d' % k)

    # ------------------------------------------------ decor
    m.put(1.6, 18.4, 'cutii')
    for (x, y) in [(6.0, 8.6), (9.0, 8.6), (12.0, 10.0)]:
        m.put(x, y, 'lada')
    m.put(10.0, 13.5, 'cutii')
    for (x, y) in [(6.0, 21.0), (6.0, 24.0), (11.5, 20.0)]:
        m.put(x, y, 'cutii')
    m.put(14.6, 21.0, 'butelii')
    m.put(16.4, 23.0, 'lada')
    m.put(32.0, 15.0, 'elicopter')
    m.put(36.0, 14.5, 'con_vant')
    for (x, y) in [(20.0, 22.0), (23.0, 22.0), (40.0, 6.0), (40.0, 9.0), (20.0, 28.0), (6.0, 32.0), (10.0, 32.0)]:
        m.put(x, y, 'ac')
    for (x, y) in [(27.0, 5.0), (41.0, 23.5), (3.0, 4.0)]:
        m.put(x, y, 'antena')
    for (x, y) in [(37.0, 28.0), (40.0, 30.5)]:
        m.put(x, y, 'birou')
    m.put(38.0, 28.8, 'scaun')
    m.put(41.4, 32.4, 'dulap')
    m.put(36.6, 32.4, 'canapea')
    m.put(25.0, 26.0, 'lada')
    m.put(33.5, 23.5, 'cutii')
    m.put(19.5, 18.5, 'cos')
    return m


if __name__ == "__main__":
    import os
    out = sys.argv[1] if len(sys.argv) > 1 else "assets"
    os.makedirs(out + "/maps", exist_ok=True)
    for fn in (parter, etaj, acoperis):
        mp = fn()
        mp.write(out + "/maps/%s.txt" % mp.name)
        if len(sys.argv) > 2:
            mp.preview(os.path.join(sys.argv[2], "map_%s.png" % mp.name))
        print("harta %s: %dx%d, %d lucruri, %d decor, %d camere" % (mp.name, mp.w, mp.h, len(mp.things), len(mp.decor), len(mp.rooms)))
