package ro.hunys.tn2;

import java.io.IOException;
import java.io.InputStream;

/** Ce are nevoie jocul de la telefon (sau de la calculator, la teste). */
public interface Platform {
    /** Citește o imagine PNG din resursele jocului, ca ARGB. */
    Img image(String name) throws IOException;

    /** Deschide o resursă a jocului (hărți, fonturi). */
    InputStream open(String name) throws IOException;

    /** Pornește un efect sonor (indice în Sfx.FILES), volum 0..1. */
    void sound(int id, float volume);

    /** Salvează / citește date (salvarea jocului, setările). */
    void save(String key, byte[] data);

    byte[] load(String key);

    /** Vibrație scurtă (ms); poate să nu facă nimic. */
    void vibrate(int ms);

    /** Limba telefonului e engleza (pentru prima pornire). */
    boolean systemEnglish();

    /** Închide aplicația. */
    void quit();
}
