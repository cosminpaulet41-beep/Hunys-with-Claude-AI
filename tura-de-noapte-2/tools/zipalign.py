"""zipalign în Python: aliniază la 4 octeți fișierele stocate necomprimat dintr-un APK (ca SoundPool și
resources.arsc să poată fi citite direct). Rulează: python3 -I tools/zipalign.py intrare.apk iesire.apk
"""
import sys
import zipfile

src, dst = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(src) as zin, zipfile.ZipFile(dst, "w") as zout:
    for info in zin.infolist():
        data = zin.read(info)
        ni = zipfile.ZipInfo(info.filename, date_time=info.date_time)
        ni.compress_type = info.compress_type
        ni.external_attr = info.external_attr
        ni.create_system = info.create_system
        if ni.compress_type == zipfile.ZIP_STORED:
            off = zout.fp.tell() + 30 + len(ni.filename.encode("utf-8"))
            align = 4096 if ni.filename.endswith(".so") else 4
            # câmpul extra „Android alignment” (0xD935): id, mărime, alinierea, apoi umplutură
            pad = (-(off + 6)) % align
            ni.extra = b"\x35\xd9" + (2 + pad).to_bytes(2, "little") + align.to_bytes(2, "little") + b"\x00" * pad
        zout.writestr(ni, data)
