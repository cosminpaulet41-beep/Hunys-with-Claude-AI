"""Iconița aplicației: capul unui pacient infectat (din z.png), mărit în pixeli mari, pe un fundal
roșu-închis, cu un „2” în colț. Scrie PNG-urile clasice (mipmap-*) și stratul din față al iconiței
adaptive (drawable-xxxhdpi/ic_fg.png). Rulează: python3 -I tools/make_icon.py assets android/app/src/main/res
"""
import os
import sys

from PIL import Image, ImageDraw

assets, res = sys.argv[1], sys.argv[2]
z = Image.open(os.path.join(assets, "z.png")).convert("RGBA")
# rândul 3 = pacientul; capul și umerii din primul cadru
head = z.crop((10, 3 * 64 + 2, 38, 3 * 64 + 30))


def digit2(size, col):
    g = ["11110", "00001", "00001", "01110", "10000", "10000", "11111"]
    im = Image.new("RGBA", (5 * size, 7 * size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    for j, row in enumerate(g):
        for i, b in enumerate(row):
            if b == "1":
                d.rectangle([i * size, j * size, (i + 1) * size - 1, (j + 1) * size - 1], fill=col)
    return im


def foreground(n):
    """Stratul din față (n x n), cu desenul în zona sigură (66/108 din mijloc)."""
    im = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    safe = int(n * 0.60)
    k = max(1, safe // head.width)
    h = head.resize((head.width * k, head.height * k), Image.NEAREST)
    im.alpha_composite(h, ((n - h.width) // 2, (n - h.height) // 2 + n // 40))
    dg = digit2(max(1, n // 40), (235, 52, 40, 255))
    sh = digit2(max(1, n // 40), (0, 0, 0, 200))
    x, y = n // 2 + safe // 2 - dg.width + n // 30, n // 2 + safe // 2 - dg.height + n // 30
    im.alpha_composite(sh, (x + n // 100, y + n // 100))
    im.alpha_composite(dg, (x, y))
    return im


def background(n):
    im = Image.new("RGBA", (n, n), (18, 10, 12, 255))
    px = im.load()
    for y in range(n):
        for x in range(n):
            dx, dy = (x - n / 2) / n, (y - n * 0.45) / n
            g = max(0.0, 1 - (dx * dx + dy * dy) * 5)
            px[x, y] = (int(18 + 90 * g), int(10 + 14 * g), int(12 + 16 * g), 255)
    return im


os.makedirs(os.path.join(res, "drawable-xxxhdpi"), exist_ok=True)
foreground(432).save(os.path.join(res, "drawable-xxxhdpi", "ic_fg.png"), optimize=True)
for name, n in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
    big = background(n * 4)
    fg = foreground(n * 4)
    big.alpha_composite(fg)
    mask = Image.new("L", big.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, big.width - 1, big.height - 1], radius=big.width // 5, fill=255)
    out = Image.new("RGBA", big.size, (0, 0, 0, 0))
    out.paste(big, (0, 0), mask)
    out = out.resize((n, n), Image.LANCZOS)
    d = os.path.join(res, "mipmap-" + name)
    os.makedirs(d, exist_ok=True)
    out.save(os.path.join(d, "ic_launcher.png"), optimize=True)
print("iconițe gata")
