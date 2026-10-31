package ro.hunys.tn2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.imageio.ImageIO;

/** Platforma de test: resurse din directorul assets, salvări în memorie, fără sunet. */
public final class DesktopPlatform implements Platform {
    final File dir;
    final HashMap<String, byte[]> store = new HashMap<>();
    public final java.util.ArrayList<Integer> sounds = new java.util.ArrayList<>();
    public boolean english;
    public int vibrations;

    public DesktopPlatform(File assets) {
        dir = assets;
    }

    public Img image(String name) throws IOException {
        BufferedImage b = ImageIO.read(new File(dir, name));
        if (b == null) {
            throw new IOException(name);
        }
        Img im = new Img(b.getWidth(), b.getHeight());
        b.getRGB(0, 0, im.w, im.h, im.px, 0, im.w);
        return im;
    }

    public InputStream open(String name) throws IOException {
        return new FileInputStream(new File(dir, name));
    }

    public void sound(int id, float volume) {
        sounds.add(id);
    }

    public void save(String key, byte[] data) {
        store.put(key, data.clone());
    }

    public byte[] load(String key) {
        byte[] b = store.get(key);
        return b == null ? null : b.clone();
    }

    public void vibrate(int ms) {
        vibrations++;
    }

    public boolean systemEnglish() {
        return english;
    }

    public void quit() {
    }
}
