"""Randează mâna cu pistolul (vedere la persoana întâi) dintr-un mic model 3D făcut din forme simple
(cutii rotunjite, capsule), cu lumini, umbre moi și ocluzie ambientală. Ca la jocurile clasice, imaginea
randată devine apoi sprite-ul armei (jocul o micșorează la pixelii lumii).

Ieșire: assets/hand_pistol.png, hand_shotgun.png, hand_smg.png (RGBA) și .txt cu gura țevii.
Rulează: python3 -I tools/render_gun.py assets [W H [pistol,shotgun,smg [supraeșantionare]]]
"""
import math
import sys

import numpy as np
from PIL import Image

# ------------------------------------------------------------------ primitive SDF (vectorizate)


def length(v):
    return np.sqrt(np.sum(v * v, axis=-1))


def sd_round_box(p, b, r):
    q = np.abs(p) - (np.array(b) - r)
    return length(np.maximum(q, 0.0)) + np.minimum(np.max(q, axis=-1), 0.0) - r


def sd_capsule(p, a, b, r):
    a = np.array(a)
    b = np.array(b)
    pa = p - a
    ba = b - a
    h = np.clip(np.sum(pa * ba, axis=-1) / np.dot(ba, ba), 0.0, 1.0)
    return length(pa - ba * h[:, None]) - r


def sd_capsule_tapered(p, a, b, ra, rb):
    a = np.array(a)
    b = np.array(b)
    pa = p - a
    ba = b - a
    h = np.clip(np.sum(pa * ba, axis=-1) / np.dot(ba, ba), 0.0, 1.0)
    return length(pa - ba * h[:, None]) - (ra + (rb - ra) * h)


def sd_cyl_z(p, c, r, h):
    q = p - np.array(c)
    d = np.stack([np.sqrt(q[:, 0] ** 2 + q[:, 1] ** 2) - r, np.abs(q[:, 2]) - h], axis=-1)
    return np.minimum(np.max(d, axis=-1), 0.0) + length(np.maximum(d, 0.0))


def smin(a, b, k):
    h = np.clip(0.5 + 0.5 * (b - a) / k, 0.0, 1.0)
    return b + (a - b) * h - k * h * (1.0 - h)


def rot_x(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])


def rot_y(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])


def rot_z(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])


# ------------------------------------------------------------------ modelul (metri, spațiul pistolului)
# x dreapta, y sus, z înainte (spre țintă). Originea: spatele mânerului, sus.

GRIP_TILT = math.radians(18)
RG = rot_x(-GRIP_TILT)          # rotește mânerul: partea de jos merge înapoi

M_SLIDE, M_FRAME, M_SKIN, M_SLEEVE, M_STRIPE, M_DARK, M_DOT, M_NAIL, M_GUN = range(9)


def finger(p, base, segs, radii):
    """Un deget din câteva capsule legate (încheieturi)."""
    d = None
    pts = [base] + segs
    for i in range(len(segs)):
        di = sd_capsule_tapered(p, pts[i], pts[i + 1], radii[i], radii[i + 1])
        d = di if d is None else smin(d, di, 0.004)
    return d


def right_hand(p, grip_tilt_extra=0.0):
    """Mâna dreaptă pe mâner (palma pe spate, degetele îl cuprind prin față, arătătorul întins pe lateral),
    încheietura și mâneca geacă de ambulanțier. Întoarce (mâna, unghiile, mâneca, capetele mânecii)."""
    palm = sd_round_box((p - np.array([0.008, -0.041, -0.024])) @ RG, [0.0185, 0.033, 0.0135], 0.010)
    thenar = sd_capsule_tapered(p, [-0.007, -0.050, -0.029], [-0.009, -0.026, -0.026], 0.0135, 0.0115)
    hand = smin(palm, thenar, 0.006)
    web = sd_capsule(p, [-0.003, -0.007, -0.027], [0.014, -0.011, -0.027], 0.0095)
    hand = smin(hand, web, 0.008)
    nails = None
    for k, (yk, r0) in enumerate([(-0.031, 0.0080), (-0.0475, 0.0078), (-0.0635, 0.0071)]):
        zb = -0.004 + 0.30 * yk
        # încheieturile: baza (dreapta), îndoit prin fața mânerului, vârful culcat înapoi pe stânga
        f = finger(p, [0.020, yk, zb - 0.013],
                   [[0.0205, yk - 0.001, zb + 0.014], [0.006, yk - 0.002, zb + 0.029], [-0.0135, yk - 0.003, zb + 0.022],
                    [-0.0190, yk - 0.003, zb + 0.004]],
                   [r0, r0 * 0.97, r0 * 0.92, r0 * 0.86, r0 * 0.78])
        hand = smin(hand, f, 0.0025)
        # unghia, pe dosul ultimei falange (spre exterior, adică spre stânga)
        nl = sd_round_box(p - np.array([-0.0190 - r0 * 0.80, yk - 0.003, zb + 0.008]),
                          [0.0010, r0 * 0.56, r0 * 0.78], 0.0009)
        nails = nl if nails is None else np.minimum(nails, nl)
    idx = finger(p, [0.018, -0.012, -0.012], [[0.0185, -0.010, 0.022], [0.0165, -0.011, 0.048], [0.0148, -0.012, 0.067]],
                 [0.0086, 0.0080, 0.0074, 0.0065])
    hand = smin(hand, idx, 0.003)
    # degetul mare: scurt și gros, ca la un om (cam 5 cm de la încheietură la vârf)
    th = finger(p, [-0.011, -0.015, -0.022], [[-0.0150, -0.008, -0.010], [-0.0160, -0.005, 0.000], [-0.0157, -0.003, 0.010]],
                [0.0118, 0.0108, 0.0097, 0.0086])
    hand = smin(hand, th, 0.005)
    nail = np.minimum(sd_round_box(p - np.array([-0.0238, -0.0010, 0.0055]), [0.0011, 0.0050, 0.0058], 0.0010), nails)
    wrist = sd_capsule_tapered(p, [0.008, -0.068, -0.044], [0.040, -0.170, -0.160], 0.0235, 0.029)
    hand = smin(hand, wrist, 0.010)

    # ---------------- mâneca (geaca de ambulanțier) cu banda reflectorizantă
    sa = np.array([0.028, -0.118, -0.105])
    sb = np.array([0.120, -0.340, -0.400])
    sleeve = sd_capsule_tapered(p, sa, sb, 0.036, 0.052)
    cuff = sd_capsule_tapered(p, sa, sa + (sb - sa) * 0.05, 0.0385, 0.0395)
    sleeve = smin(sleeve, cuff, 0.003)
    hand = np.minimum(hand, nail)
    return hand, nail, sleeve, sa, sb



def scene(p, want_mat=False):
    # ---------------- pistolul
    slide = sd_round_box(p - np.array([0.0, 0.019, 0.086]), [0.0125, 0.0145, 0.094], 0.0035)
    # crestăturile din spatele închizătorului (mici adâncituri pe lateral)
    serr = np.abs(np.mod(p[:, 2] + 0.002, 0.004) - 0.002) - 0.0007
    side = np.abs(p[:, 0]) - 0.0118
    cut = np.maximum(np.maximum(serr, -side), np.maximum(p[:, 2] - 0.03, -p[:, 2] - 0.006))
    cut = np.maximum(cut, np.abs(p[:, 1] - 0.016) - 0.009)
    slide = np.maximum(slide, -cut)
    # fereastra de aruncare (dreapta, sus)
    port = sd_round_box(p - np.array([0.006, 0.031, 0.095]), [0.007, 0.004, 0.022], 0.001)
    slide = np.maximum(slide, -port)
    rear = sd_round_box(p - np.array([0.0, 0.0365, 0.004]), [0.0112, 0.0042, 0.0045], 0.001)
    notch = sd_round_box(p - np.array([0.0, 0.0395, 0.004]), [0.0026, 0.0042, 0.006], 0.0005)
    rear = np.maximum(rear, -notch)
    front = sd_round_box(p - np.array([0.0, 0.0355, 0.172]), [0.0017, 0.0035, 0.0028], 0.0006)
    sights = np.minimum(rear, front)
    hole = sd_cyl_z(p, [0.0, 0.017, 0.181], 0.0047, 0.004)
    frame = sd_round_box(p - np.array([0.0, -0.003, 0.098]), [0.0108, 0.0082, 0.072], 0.003)
    rail = sd_round_box(p - np.array([0.0, -0.013, 0.135]), [0.0085, 0.003, 0.03], 0.001)
    frame = np.minimum(frame, rail)
    # garda trăgaciului: o buclă din capsule
    tg = sd_capsule(p, [0.0, -0.010, 0.026], [0.0, -0.030, 0.034], 0.0032)
    tg = np.minimum(tg, sd_capsule(p, [0.0, -0.030, 0.034], [0.0, -0.032, 0.066], 0.0032))
    tg = np.minimum(tg, sd_capsule(p, [0.0, -0.032, 0.066], [0.0, -0.011, 0.082], 0.0032))
    trig = sd_capsule(p, [0.0, -0.010, 0.044], [0.0, -0.022, 0.048], 0.0022)
    frame = np.minimum(np.minimum(frame, tg), trig)
    gp = (p - np.array([0.0, -0.004, -0.004])) @ RG
    grip = sd_round_box(gp - np.array([0.0, -0.046, 0.0]), [0.0118, 0.047, 0.0165], 0.0045)
    beaver = sd_round_box(gp - np.array([0.0, -0.002, -0.014]), [0.0105, 0.006, 0.008], 0.004)
    grip = smin(grip, beaver, 0.004)
    magbase = sd_round_box(gp - np.array([0.0, -0.095, 0.002]), [0.0125, 0.004, 0.018], 0.002)
    frame = np.minimum(frame, np.minimum(grip, magbase))

    hand, nail, sleeve, sa, sb = right_hand(p)
    d = np.minimum.reduce([slide, sights, hole, frame, hand, sleeve])
    if not want_mat:
        return d
    mat = np.full(p.shape[0], M_SLIDE)
    best = slide.copy()
    for m, dd in ((M_DARK, hole), (M_SLIDE, sights), (M_FRAME, frame), (M_SKIN, hand), (M_SLEEVE, sleeve), (M_NAIL, nail)):
        sel = dd < best
        mat[sel] = m
        best = np.minimum(best, dd)
    # banda reflectorizantă: o fâșie pe mânecă
    for (a0, b0) in ((sa, sb),):
        ab = b0 - a0
        h = np.sum((p - a0) * ab, axis=-1) / np.dot(ab, ab)
        near = length(p - (a0 + ab * np.clip(h, 0, 1)[:, None])) < 0.06
        mat[(mat == M_SLEEVE) & near & (h > 0.20) & (h < 0.27)] = M_STRIPE
    # punctele luminoase de pe cătare
    for c in ([-0.0068, 0.038, -0.0005], [0.0068, 0.038, -0.0005], [0.0, 0.0375, 0.1692]):
        dd = length(p - np.array(c)) - 0.0019
        mat[(dd < 0.0006) & (mat == M_SLIDE)] = M_DOT
    return d, mat


def left_hand(p, zc, yc, wrap_r=0.019):
    """Mâna stângă sub țeavă (pe pompă sau pe mânerul din față): palma dedesubt, degetele urcă pe partea
    dreaptă, degetul mare pe stânga; antebrațul și mâneca vin din stânga jos."""
    palm = sd_round_box(p - np.array([-0.004, yc - wrap_r - 0.010, zc]), [0.021, 0.010, 0.030], 0.009)
    hand = palm
    nails = None
    for k, dz in enumerate((-0.0235, -0.008, 0.008, 0.0225)):
        z = zc + dz
        r0 = 0.0076 - abs(dz) * 0.02
        f = finger(p, [0.010, yc - wrap_r - 0.011, z],
                   [[wrap_r + 0.006, yc - wrap_r * 0.7, z], [wrap_r + 0.009, yc - 0.002, z + 0.002],
                    [wrap_r + 0.002, yc + wrap_r * 0.62, z + 0.003]],
                   [r0, r0 * 0.96, r0 * 0.90, r0 * 0.82])
        hand = smin(hand, f, 0.003)
        nl = sd_round_box(p - np.array([wrap_r + 0.006, yc + wrap_r * 0.55, z + 0.003]), [0.0028, 0.0018, r0 * 0.7], 0.0009)
        nails = nl if nails is None else np.minimum(nails, nl)
    th = finger(p, [-0.016, yc - wrap_r - 0.008, zc - 0.034],
                [[-wrap_r - 0.004, yc - wrap_r * 0.55, zc - 0.014], [-wrap_r - 0.005, yc - wrap_r * 0.2, zc + 0.008],
                 [-wrap_r - 0.004, yc - 0.000, zc + 0.026]],
                [0.0110, 0.0100, 0.0090, 0.0080])
    hand = smin(hand, th, 0.005)
    wrist = sd_capsule_tapered(p, [-0.010, yc - wrap_r - 0.020, zc - 0.020], [-0.060, yc - 0.140, zc - 0.150], 0.024, 0.030)
    hand = smin(hand, wrist, 0.010)
    sa = np.array([-0.040, yc - 0.105, zc - 0.105])
    sb = np.array([-0.190, yc - 0.380, zc - 0.400])
    sleeve = sd_capsule_tapered(p, sa, sb, 0.037, 0.055)
    cuff = sd_capsule_tapered(p, sa, sa + (sb - sa) * 0.05, 0.0395, 0.0405)
    sleeve = smin(sleeve, cuff, 0.003)
    hand = np.minimum(hand, nails)
    return hand, nails, sleeve, sa, sb


def grip_frame(p):
    """Mânerul, garda și trăgaciul (la fel ca la pistol), pentru armele lungi."""
    tg = sd_capsule(p, [0.0, -0.010, 0.026], [0.0, -0.030, 0.034], 0.0032)
    tg = np.minimum(tg, sd_capsule(p, [0.0, -0.030, 0.034], [0.0, -0.032, 0.066], 0.0032))
    tg = np.minimum(tg, sd_capsule(p, [0.0, -0.032, 0.066], [0.0, -0.011, 0.082], 0.0032))
    trig = sd_capsule(p, [0.0, -0.010, 0.044], [0.0, -0.022, 0.048], 0.0022)
    gp = (p - np.array([0.0, -0.004, -0.004])) @ RG
    grip = sd_round_box(gp - np.array([0.0, -0.046, 0.0]), [0.0120, 0.047, 0.0170], 0.0045)
    return np.minimum(np.minimum(tg, trig), grip)


def mats(p, parts, sleeves):
    """Materialul fiecărui punct: cea mai apropiată piesă; dungile reflectorizante pe mâneci."""
    best = None
    mat = None
    for m, dd in parts:
        if best is None:
            best = dd.copy()
            mat = np.full(p.shape[0], m)
        else:
            sel = dd < best
            mat[sel] = m
            best = np.minimum(best, dd)
    for (a0, b0) in sleeves:
        ab = b0 - a0
        h = np.sum((p - a0) * ab, axis=-1) / np.dot(ab, ab)
        near = length(p - (a0 + ab * np.clip(h, 0, 1)[:, None])) < 0.065
        mat[(mat == M_SLEEVE) & near & (h > 0.20) & (h < 0.27)] = M_STRIPE
    return mat


def pins(p, pts, r=0.0022, side=-1):
    """Știfturi rotunde pe partea stângă (side=-1) a cutiei."""
    d = None
    for (y, z) in pts:
        di = length(p - np.array([side * 0.0148, y, z])) - r
        d = di if d is None else np.minimum(d, di)
    return d


def scene_shotgun(p, want_mat=False):
    """Pușca cu pompă: mâna dreaptă pe mâner, stânga pe pompă."""
    recv = sd_round_box(p - np.array([0.0, 0.016, 0.060]), [0.0145, 0.021, 0.086], 0.0045)
    # o linie adâncită de-a lungul cutiei și știfturile, ca să nu fie o cutie goală
    line = np.maximum(np.abs(p[:, 1] - 0.010) - 0.0008, np.abs(p[:, 2] - 0.06) - 0.07)
    line = np.maximum(line, 0.0135 - np.abs(p[:, 0]))
    recv = np.maximum(recv, -line)
    port = sd_round_box(p - np.array([0.012, 0.020, 0.075]), [0.006, 0.008, 0.026], 0.001)
    recv = np.maximum(recv, -port)
    barrel = sd_cyl_z(p, [0.0, 0.027, 0.385], 0.0105, 0.245)
    rib = sd_round_box(p - np.array([0.0, 0.0385, 0.385]), [0.0035, 0.0018, 0.24], 0.0008)
    tube = sd_cyl_z(p, [0.0, 0.004, 0.360], 0.0095, 0.215)
    cap = sd_cyl_z(p, [0.0, 0.004, 0.578], 0.0108, 0.008)
    band = sd_round_box(p - np.array([0.0, 0.016, 0.560]), [0.0115, 0.017, 0.006], 0.003)
    metal = np.minimum.reduce([recv, barrel, rib, tube, cap, band])
    hole = sd_cyl_z(p, [0.0, 0.027, 0.630], 0.0072, 0.004)
    bead = length(p - np.array([0.0, 0.0425, 0.612])) - 0.0028
    pump = sd_round_box(p - np.array([0.0, 0.008, 0.330]), [0.0195, 0.0185, 0.085], 0.006)
    groove = np.abs(np.mod(p[:, 2], 0.009) - 0.0045) - 0.0014
    gcut = np.maximum(groove, np.abs(p[:, 2] - 0.33) - 0.075)
    gcut = np.maximum(gcut, 0.0165 - np.abs(p[:, 0]))
    pump = np.maximum(pump, -gcut)
    stock = sd_round_box((p - np.array([0.0, 0.004, -0.025])) @ rot_x(-0.16) - np.array([0.0, -0.004, -0.13]),
                         [0.0175, 0.026, 0.13], 0.008)
    frame = np.minimum.reduce([grip_frame(p), pump, stock])
    pn = pins(p, [(0.002, 0.030), (0.002, 0.095), (0.026, 0.120)])
    hand, nail, sleeve, sa, sb = right_hand(p)
    lh, lnail, lsleeve, la, lb = left_hand(p, 0.322, 0.008, 0.0195)
    d = np.minimum.reduce([metal, hole, bead, frame, pn, hand, sleeve, lh, lsleeve])
    if not want_mat:
        return d
    mat = mats(p, [(M_GUN, metal), (M_DARK, hole), (M_DOT, bead), (M_FRAME, frame), (M_SLIDE, pn),
                   (M_SKIN, np.minimum(hand, lh)), (M_SLEEVE, np.minimum(sleeve, lsleeve)),
                   (M_NAIL, np.minimum(nail, lnail))], [(sa, sb), (la, lb)])
    return d, mat


def scene_smg(p, want_mat=False):
    """Pistolul-mitralieră compact: mâna dreaptă pe mâner, stânga pe apărătoarea din față."""
    recv = sd_round_box(p - np.array([0.0, 0.017, 0.080]), [0.0150, 0.0225, 0.112], 0.004)
    tube = sd_cyl_z(p, [0.0, 0.035, 0.090], 0.0105, 0.11)
    recv = smin(recv, tube, 0.004)
    drum = sd_cyl_z(p, [0.0, 0.050, 0.004], 0.0085, 0.0045)
    hood = sd_round_box(p - np.array([0.0, 0.049, 0.216]), [0.0075, 0.0075, 0.005], 0.002)
    hood = np.maximum(hood, -sd_cyl_z(p, [0.0, 0.050, 0.216], 0.0045, 0.01))
    barrel = sd_cyl_z(p, [0.0, 0.016, 0.340], 0.0085, 0.032)
    metal = np.minimum.reduce([recv, drum, hood, barrel])
    hole = sd_cyl_z(p, [0.0, 0.016, 0.372], 0.0055, 0.004)
    guard = sd_round_box(p - np.array([0.0, 0.010, 0.262]), [0.0185, 0.0205, 0.060], 0.008)
    rib = np.abs(np.mod(p[:, 2], 0.012) - 0.006) - 0.0016
    rcut = np.maximum(np.maximum(rib, np.abs(p[:, 2] - 0.262) - 0.05), 0.0160 - np.abs(p[:, 0]))
    guard = np.maximum(guard, -rcut)
    mp = (p - np.array([0.0, -0.004, 0.118])) @ rot_x(-0.22)
    mag = sd_round_box(mp - np.array([0.0, -0.048, 0.0]), [0.0085, 0.050, 0.0145], 0.0025)
    frame = np.minimum.reduce([grip_frame(p), guard, mag])
    hand, nail, sleeve, sa, sb = right_hand(p)
    lh, lnail, lsleeve, la, lb = left_hand(p, 0.262, 0.010, 0.0205)
    pn = pins(p, [(0.004, 0.025), (0.004, 0.150), (0.030, 0.165)])
    line = np.maximum(np.abs(p[:, 1] - 0.024) - 0.0009, np.abs(p[:, 2] - 0.09) - 0.09)
    line = np.maximum(line, 0.0140 - np.abs(p[:, 0]))
    metal = np.maximum(metal, -line)
    d = np.minimum.reduce([metal, hole, frame, pn, hand, sleeve, lh, lsleeve])
    if not want_mat:
        return d
    mat = mats(p, [(M_GUN, metal), (M_DARK, hole), (M_FRAME, frame), (M_SLIDE, pn), (M_SKIN, np.minimum(hand, lh)),
                   (M_SLEEVE, np.minimum(sleeve, lsleeve)), (M_NAIL, np.minimum(nail, lnail))], [(sa, sb), (la, lb)])
    return d, mat


# ------------------------------------------------------------------ cameră și randare

# camera pentru fiecare armă: unghiuri (grade), unde e arma față de ochi (m), cât cuprinde cadrul
# (SCALE, mai mic = mai aproape), încotro se uită cadrul (lens shift, în tangentă) și gura țevii
CAMS = {
    "pistol": dict(yaw=-9.0, pitch=-2.0, roll=-7.0, origin=(0.075, -0.098, 0.255), scale=0.66, sx=0.255, sy=-0.400,
                   tip=(0.0, 0.017, 0.186)),
    # armele lungi: cadrul întreg, cu câmpul vizual al jocului (tangenta 0.45), ca în joc să stea exact așa
    "shotgun": dict(yaw=-4.0, pitch=2.0, roll=-8.0, origin=(0.14, -0.09, 0.26), scale=1.0, sx=0.0, sy=0.0, fov=48.5,
                    tip=(0.0, 0.027, 0.636), frame=True),
    "smg": dict(yaw=-4.0, pitch=2.0, roll=-8.0, origin=(0.14, -0.09, 0.26), scale=1.0, sx=0.0, sy=0.0, fov=48.5,
                tip=(0.0, 0.016, 0.376), frame=True),
}


def render(W, H, scene=None, cam=None):
    if scene is None:
        scene = globals()["scene"]
    cam = cam or CAMS["pistol"]
    SCALE, SHIFT_X, SHIFT_Y = cam["scale"], cam["sx"], cam["sy"]
    # arma, așezată jos-dreapta, cu țeava spre centrul ecranului
    yaw = math.radians(cam["yaw"])
    pitch = math.radians(cam["pitch"])
    roll = math.radians(cam["roll"])
    R = rot_y(yaw) @ rot_x(pitch) @ rot_z(roll)
    origin = np.array(cam["origin"])
    fov = math.radians(cam.get("fov", 50.0))
    aspect = W / H
    ys, xs = np.mgrid[0:H, 0:W]
    # obiectiv deplasat (lens shift): cadrul cuprinde arma și mâinile, fără să schimbe perspectiva
    tf = math.tan(fov / 2)
    u = ((xs + 0.5) / W * 2 - 1) * tf * aspect * SCALE + SHIFT_X
    v = (1 - (ys + 0.5) / H * 2) * tf * SCALE + SHIFT_Y
    rd = np.stack([u, v, np.ones_like(u)], axis=-1).reshape(-1, 3)
    rd /= length(rd)[:, None]
    # în spațiul pistolului: p_gun = R^T (p - origin)
    ro_g = (np.zeros(3) - origin) @ R
    rd_g = rd @ R
    n = rd_g.shape[0]
    t = np.full(n, 0.05)
    hit = np.zeros(n, bool)
    alive = np.ones(n, bool)
    for it in range(160):
        idx = np.nonzero(alive)[0]
        if idx.size == 0:
            break
        pts = ro_g + rd_g[idx] * t[idx, None]
        d = scene(pts)
        t[idx] += d * 0.9
        h = d < 0.00015
        hit[idx[h]] = True
        alive[idx[h]] = False
        far = t[idx] > 1.6
        alive[idx[far]] = False
    col = np.zeros((n, 3))
    alpha = hit.astype(float)
    idx = np.nonzero(hit)[0]
    p = ro_g + rd_g[idx] * t[idx, None]
    e = 0.00012
    nx = scene(p + [e, 0, 0]) - scene(p - [e, 0, 0])
    ny = scene(p + [0, e, 0]) - scene(p - [0, e, 0])
    nz = scene(p + [0, 0, e]) - scene(p - [0, 0, e])
    nrm = np.stack([nx, ny, nz], axis=-1)
    nrm /= length(nrm)[:, None] + 1e-9
    _, mat = scene(p, True)
    # lumini (în spațiul camerei, transformate în spațiul pistolului)
    key = np.array([-0.55, 0.75, -0.35])
    key /= np.linalg.norm(key)
    key_g = key @ R
    fill = np.array([0.7, -0.1, -0.6])
    fill /= np.linalg.norm(fill)
    fill_g = fill @ R
    view = -rd_g[idx]
    # umbre moi spre lumina principală
    sh = np.ones(idx.size)
    ts = np.full(idx.size, 0.002)
    for k in range(48):
        q = p + nrm * 0.0004 + key_g * ts[:, None]
        dd = scene(q)
        sh = np.minimum(sh, 10.0 * dd / ts)
        ts += np.clip(dd, 0.0008, 0.01)
    sh = np.clip(sh, 0.0, 1.0)
    # ocluzie ambientală
    ao = np.zeros(idx.size)
    for k in range(1, 6):
        hh = 0.0025 * k
        dd = scene(p + nrm * hh)
        ao += (hh - dd) / (2 ** k)
    ao = np.clip(1.0 - 35.0 * ao, 0.25, 1.0)
    ndl = np.sum(nrm * key_g, axis=-1)
    ndf = np.clip(np.sum(nrm * fill_g, axis=-1), 0, 1)
    rim = np.clip(1.0 - np.sum(nrm * view, axis=-1), 0, 1) ** 3
    halfv = key_g + view
    halfv /= length(halfv)[:, None]
    ndh = np.clip(np.sum(nrm * halfv, axis=-1), 0, 1)

    base = np.zeros((idx.size, 3))
    spec_k = np.zeros(idx.size)
    shin = np.full(idx.size, 16.0)
    wrap = np.zeros(idx.size)
    pal = {
        M_SLIDE: ([0.15, 0.16, 0.19], 0.65, 40.0, 0.0),
        M_FRAME: ([0.075, 0.075, 0.085], 0.30, 18.0, 0.0),
        M_SKIN: ([0.74, 0.50, 0.38], 0.10, 10.0, 0.22),
        M_SLEEVE: ([0.12, 0.16, 0.28], 0.05, 6.0, 0.15),
        M_STRIPE: ([0.82, 0.82, 0.70], 0.30, 12.0, 0.1),
        M_DARK: ([0.02, 0.02, 0.02], 0.0, 4.0, 0.0),
        M_DOT: ([0.55, 1.0, 0.65], 0.0, 4.0, 0.0),
        M_NAIL: ([0.84, 0.66, 0.60], 0.45, 30.0, 0.1),
        M_GUN: ([0.075, 0.08, 0.095], 0.85, 46.0, 0.0),
    }
    for m, (c, sk, sn, wr) in pal.items():
        sel = mat == m
        base[sel] = c
        spec_k[sel] = sk
        shin[sel] = sn
        wrap[sel] = wr
    diff = np.clip((ndl + wrap) / (1 + wrap), 0, 1) * (0.25 + 0.75 * sh)
    light = 0.12 * ao + 1.15 * diff + 0.22 * ndf * ao
    spec = spec_k * ndh ** shin * sh * 1.4
    c = base * light[:, None] + spec[:, None] * np.array([1.0, 0.97, 0.9]) + base * rim[:, None] * 0.35
    # piele: variații mici de culoare, mai roșiatică spre degete
    skin = mat == M_SKIN
    q = p[skin] * 900.0
    nz = (np.sin(q[:, 0] * 1.7 + q[:, 1] * 0.9) * np.sin(q[:, 1] * 1.3 - q[:, 2] * 1.1) * np.sin(q[:, 2] * 0.8 + q[:, 0])) * 0.035
    c[skin] *= (1.0 + nz)[:, None]
    redden = np.clip((p[skin][:, 2] + 0.01) * 18.0, 0, 1) * 0.05
    c[skin] += np.stack([redden, -redden * 0.4, -redden * 0.5], axis=-1)
    # ușor „subsurface” pe piele: umbrele spre roșiatic
    c[skin] += (1 - light[skin])[:, None] * np.array([0.10, 0.02, 0.0]) * 0.6
    dots = mat == M_DOT
    c[dots] = [0.6, 1.0, 0.7]
    col[idx] = np.clip(c, 0, 1)
    img = np.concatenate([col, alpha[:, None]], axis=-1).reshape(H, W, 4)
    tip_cam = R @ np.array(cam["tip"]) + origin
    tu, tv = tip_cam[0] / tip_cam[2], tip_cam[1] / tip_cam[2]
    tx = ((tu - SHIFT_X) / (tf * aspect * SCALE) + 1) / 2 * W - 0.5
    ty = (1 - (tv - SHIFT_Y) / (tf * SCALE)) / 2 * H - 0.5
    return img, (tx, ty)


def main():
    out = sys.argv[1] if len(sys.argv) > 1 else "assets"
    W = int(sys.argv[2]) if len(sys.argv) > 2 else 640
    H = int(sys.argv[3]) if len(sys.argv) > 3 else 480
    which = sys.argv[4].split(",") if len(sys.argv) > 4 else ["pistol", "shotgun", "smg"]
    ss = int(sys.argv[5]) if len(sys.argv) > 5 else 2
    scenes = {"pistol": scene, "shotgun": scene_shotgun, "smg": scene_smg}
    for name in which:
        # armele lungi se randează în tot cadrul (lat cât ecranul telefonului, 2.22:1)
        cam = CAMS[name]
        w = W if not cam.get("frame") else int(H * 2.22)
        img, tip = render(w * ss, H * ss, scenes[name], cam)
        # supraeșantionare: media pe blocuri ss x ss (culoarea ponderată cu alfa)
        a = img[..., 3:4]
        rgb = img[..., :3] * a
        rgb = rgb.reshape(H, ss, w, ss, 3).mean(axis=(1, 3))
        al = a.reshape(H, ss, w, ss, 1).mean(axis=(1, 3))
        rgb = np.where(al > 0, rgb / np.maximum(al, 1e-6), 0)
        # gamma pentru afișare
        rgb = np.clip(rgb, 0, 1) ** (1 / 2.2)
        im = np.concatenate([rgb, al], axis=-1)
        im = (np.clip(im, 0, 1) * 255 + 0.5).astype(np.uint8)
        pil = Image.fromarray(im, "RGBA")
        bb = pil.getbbox()
        pil = pil.crop(bb)
        fn = "hand_" + name
        pil.save(out + "/" + fn + ".png")
        tx, ty = tip[0] / ss - bb[0], tip[1] / ss - bb[1]
        with open(out + "/" + fn + ".txt", "w") as f:
            f.write("teava %.1f %.1f\n" % (tx, ty))
            if cam.get("frame"):
                # unde stă imaginea tăiată în cadrul întreg (ca jocul să o pună la fel pe ecran)
                f.write("cadru %d %d %d %d\n" % (w, H, bb[0], bb[1]))
        print("%s.png %dx%d, teava la %.1f %.1f" % (fn, pil.width, pil.height, tx, ty))


if __name__ == "__main__":
    main()
