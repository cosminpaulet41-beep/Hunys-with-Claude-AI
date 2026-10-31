"""Sintetizeaza efectele sonore ale jocului ca WAV PCM 16 biti mono, 16 kHz
(format citit sigur de playerul Java de pe Nokia S60)."""
import os
import sys
import wave
import numpy as np
from scipy.signal import butter, lfilter

SR = 16000
OUT = sys.argv[1] if len(sys.argv) > 1 else "."
rng = np.random.default_rng(1977)


def t(sec):
    return np.arange(int(SR * sec)) / SR


def noise(sec):
    return rng.uniform(-1, 1, int(SR * sec))


def lp(x, f, order=2):
    b, a = butter(order, f / (SR / 2), "low")
    return lfilter(b, a, x)


def hp(x, f, order=2):
    b, a = butter(order, f / (SR / 2), "high")
    return lfilter(b, a, x)


def bp(x, lo, hi, order=2):
    b, a = butter(order, [lo / (SR / 2), hi / (SR / 2)], "band")
    return lfilter(b, a, x)


def env(n, attack, decay_tau):
    e = np.ones(n)
    a = max(1, int(SR * attack))
    e[:a] = np.linspace(0, 1, a)
    k = np.arange(n - a) / SR
    e[a:] = np.exp(-k / decay_tau)
    return e


def place(total, parts):
    out = np.zeros(int(SR * total))
    for start, sig in parts:
        i = int(SR * start)
        j = min(len(out), i + len(sig))
        out[i:j] += sig[: j - i]
    return out


def save(name, x, gain=0.9):
    x = np.asarray(x, dtype=float)
    peak = np.max(np.abs(x)) or 1.0
    x = x / peak * gain
    fade = min(len(x), int(SR * 0.01))
    x[-fade:] *= np.linspace(1, 0, fade)
    data = (x * 32767).astype("<i2").tobytes()
    with wave.open(os.path.join(OUT, name), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(data)


def click(sec=0.02, f=3000):
    n = noise(sec)
    return hp(n, f) * env(len(n), 0.0005, sec / 4)


def shot():
    n = noise(0.45)
    crack = hp(n, 1200) * env(len(n), 0.0005, 0.02)
    body = lp(n, 1800) * env(len(n), 0.001, 0.07)
    tt = t(0.45)
    thump = np.sin(2 * np.pi * (110 * np.exp(-tt * 9)) * tt) * env(len(tt), 0.001, 0.09)
    tail = lp(noise(0.45), 600) * env(len(tt), 0.02, 0.18) * 0.35
    return crack * 1.2 + body + thump * 1.4 + tail


def reload_snd():
    slide = bp(noise(0.12), 1500, 5000) * np.hanning(int(SR * 0.12))
    return place(0.7, [(0.0, click(0.03, 2500) * 1.0), (0.08, slide * 0.5),
                       (0.32, click(0.04, 1800) * 1.2), (0.5, slide * 0.7), (0.62, click(0.03, 2200))])


def door():
    tt = t(0.8)
    hiss = bp(noise(0.8), 2500, 6000) * env(len(tt), 0.02, 0.25) * 0.5
    roll = bp(noise(0.8), 120, 500) * (0.6 + 0.4 * np.sin(2 * np.pi * 13 * tt)) * np.hanning(len(tt))
    clank = place(0.8, [(0.7, lp(noise(0.1), 900) * env(int(SR * 0.1), 0.001, 0.02) * 2)])
    return hiss + roll + clank


def beep(freq, sec, square=False):
    tt = t(sec)
    s = np.sin(2 * np.pi * freq * tt)
    if square:
        s = np.sign(s) * 0.6
    return s * env(len(tt), 0.003, sec)


def locked():
    b = beep(180, 0.12, True)
    return place(0.34, [(0.0, b), (0.18, b)])


def card():
    return place(0.3, [(0.0, beep(880, 0.1)), (0.11, beep(1320, 0.16))])


def pickup():
    return place(0.32, [(0.0, beep(660, 0.12)), (0.07, beep(990, 0.14)), (0.14, beep(1480, 0.16) * 0.6)])


def groan(f0=92, sec=1.1, seed=0):
    tt = t(sec)
    vib = 1 + 0.05 * np.sin(2 * np.pi * 5.5 * tt) + 0.03 * np.sin(2 * np.pi * 2.1 * tt + seed)
    f = f0 * vib * (1 - 0.18 * tt / sec)
    ph = 2 * np.pi * np.cumsum(f) / SR
    saw = 2 * ((ph / (2 * np.pi)) % 1) - 1
    voice = bp(saw, 300, 520) * 1.0 + bp(saw, 650, 900) * 0.6 + lp(saw, 250) * 0.5
    breath = bp(noise(sec), 400, 1800) * 0.25
    e = np.sin(np.pi * np.clip(tt / sec, 0, 1)) ** 0.7
    rough = 1 + 0.35 * lp(noise(sec), 30) / 0.2
    return (voice + breath) * e * rough


def hurt():
    tt = t(0.32)
    thud = np.sin(2 * np.pi * (90 * np.exp(-tt * 6)) * tt) * env(len(tt), 0.001, 0.06) * 1.5
    grunt = groan(140, 0.32) * 0.7
    return thud + grunt


def zdie():
    g = groan(78, 0.7, 1.3)
    tt = t(1.0)
    thud = np.sin(2 * np.pi * (70 * np.exp(-tt * 5)) * tt) * env(len(tt), 0.001, 0.08)
    return place(1.0, [(0.0, g), (0.62, thud * 1.6 + lp(noise(1.0), 500) * env(len(tt), 0.001, 0.05))])


def paper():
    parts = []
    for k in range(5):
        seg = bp(noise(0.05), 2000, 7000) * np.hanning(int(SR * 0.05))
        parts.append((k * 0.05 + rng.uniform(0, 0.015), seg * rng.uniform(0.5, 1)))
    return place(0.32, parts)


def heart():
    tt = t(0.16)
    th = np.sin(2 * np.pi * (55 * np.exp(-tt * 4)) * tt) * env(len(tt), 0.004, 0.04)
    return place(0.5, [(0.0, th), (0.2, th * 0.7)])


def shotgun():
    n = noise(0.75)
    tt = t(0.75)
    crack = hp(n, 900) * env(len(n), 0.0005, 0.03) * 1.3
    body = lp(n, 1400) * env(len(n), 0.001, 0.12)
    thump = np.sin(2 * np.pi * (75 * np.exp(-tt * 6)) * tt) * env(len(tt), 0.001, 0.14) * 1.8
    tail = lp(noise(0.75), 500) * env(len(tt), 0.03, 0.3) * 0.45
    return crack + body + thump + tail


def pump():
    slide = bp(noise(0.12), 900, 4000) * np.hanning(int(SR * 0.12))
    return place(0.45, [(0.0, slide * 0.8), (0.1, click(0.04, 1500) * 1.4), (0.22, slide), (0.33, click(0.05, 1200) * 1.6)])


def roar():
    tt = t(1.4)
    g1 = groan(58, 1.4, 0.7)
    g2 = groan(87, 1.4, 2.4) * 0.6
    grit = bp(noise(1.4), 200, 1400) * np.sin(np.pi * np.clip(tt / 1.4, 0, 1)) * 0.5
    return g1 + g2 + grit


def generator():
    tt = t(2.2)
    rate = 6 + 22 * np.clip(tt / 1.4, 0, 1)
    ph = 2 * np.pi * np.cumsum(rate) / SR
    chug = (0.5 + 0.5 * np.sign(np.sin(ph))) * lp(noise(2.2), 300)
    hum = np.sin(2 * np.pi * 50 * tt) * np.clip((tt - 0.8) / 0.8, 0, 1) * 0.6
    hum += np.sin(2 * np.pi * 100 * tt) * np.clip((tt - 1.0) / 0.8, 0, 1) * 0.3
    start = place(2.2, [(0.0, click(0.06, 800) * 2)])
    return chug * 1.4 + hum + start


def smg():
    n = noise(0.16)
    tt = t(0.16)
    crack = hp(n, 1500) * env(len(n), 0.0005, 0.012)
    body = lp(n, 2200) * env(len(n), 0.001, 0.03)
    thump = np.sin(2 * np.pi * (140 * np.exp(-tt * 14)) * tt) * env(len(tt), 0.001, 0.04)
    return crack * 1.1 + body + thump


def explode():
    tt = t(1.6)
    n = noise(1.6)
    boom = np.sin(2 * np.pi * (48 * np.exp(-tt * 2.5)) * tt) * env(len(tt), 0.002, 0.35) * 2.0
    blast = lp(n, 900) * env(len(tt), 0.001, 0.25) * 1.6
    crackle = bp(noise(1.6), 2000, 6000) * env(len(tt), 0.05, 0.4) * (0.5 + 0.5 * (rng.uniform(0, 1, len(tt)) > 0.97))
    return boom + blast + crackle * 0.6


def spit():
    tt = t(0.45)
    gurgle = bp(noise(0.45), 200, 900) * (0.6 + 0.4 * np.sin(2 * np.pi * 22 * tt))
    splat = bp(noise(0.45), 1200, 4000) * env(len(tt), 0.18, 0.06) * 1.5
    return (gurgle * np.hanning(len(tt)) + place(0.45, [(0.2, splat[:int(SR * 0.2)])]))


def thunder():
    tt = t(3.0)
    n = lp(noise(3.0), 220, 3)
    rumble = n * env(len(tt), 0.05, 1.1)
    crack = hp(noise(3.0), 600) * env(len(tt), 0.002, 0.12) * 0.6
    roll = lp(noise(3.0), 90, 2) * (0.5 + 0.5 * np.sin(2 * np.pi * 1.3 * tt)) * env(len(tt), 0.3, 1.2)
    return rumble * 2 + crack + roll * 2


def alarm():
    tt = t(0.9)
    f = 620 + 280 * np.sin(2 * np.pi * 1.1 * tt)
    ph = 2 * np.pi * np.cumsum(f) / SR
    tone = np.sign(np.sin(ph)) * 0.5 + np.sin(ph) * 0.5
    return lp(tone, 3000) * np.minimum(1, np.minimum(tt / 0.02, (0.9 - tt) / 0.05))


def radio():
    tt = t(0.6)
    static = bp(noise(0.6), 1500, 5000) * (0.5 + 0.5 * (rng.uniform(0, 1, len(tt)) > 0.5))
    beep = np.sin(2 * np.pi * 1400 * tt) * ((tt > 0.42) & (tt < 0.52))
    return static * np.hanning(len(tt)) * 0.7 + beep * 0.6


def menu():
    return beep(1200, 0.04) * 0.6


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    save("shot.wav", shot())
    save("empty.wav", place(0.08, [(0, click(0.02, 3500))]), 0.6)
    save("reload.wav", reload_snd(), 0.7)
    save("door.wav", door(), 0.7)
    save("locked.wav", locked(), 0.6)
    save("card.wav", card(), 0.6)
    save("pickup.wav", pickup(), 0.6)
    save("groan.wav", groan(92, 1.1, 0.0), 0.85)
    save("groan2.wav", groan(74, 1.3, 2.0), 0.85)
    save("hurt.wav", hurt(), 0.85)
    save("zdie.wav", zdie(), 0.85)
    save("paper.wav", paper(), 0.6)
    save("heart.wav", heart(), 0.8)
    save("shotgun.wav", shotgun(), 0.95)
    save("pump.wav", pump(), 0.7)
    save("roar.wav", roar(), 0.9)
    save("gen.wav", generator(), 0.8)
    save("smg.wav", smg(), 0.85)
    save("explode.wav", explode(), 0.95)
    save("spit.wav", spit(), 0.8)
    save("thunder.wav", thunder(), 0.9)
    save("alarm.wav", alarm(), 0.55)
    save("radio.wav", radio(), 0.6)
    save("menu.wav", menu(), 0.4)
    print("ok")
