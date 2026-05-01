// SoundPlayer.java
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundPlayer {
    public static Clip loadClipFromResource(String resourcePath) throws Exception {
        InputStream is = SoundPlayer.class.getResourceAsStream(resourcePath);
        if (is == null) throw new IllegalArgumentException("Resource not found: " + resourcePath);
        BufferedInputStream bis = new BufferedInputStream(is);
        AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        return clip;
    }

    public static void setClipVolume(Clip clip, float volumeDb) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gain.getMinimum();
                float max = gain.getMaximum();
                float v = Math.max(min, Math.min(max, volumeDb));
                gain.setValue(v);
            } else {
                System.out.println("MASTER_GAIN nicht unterstützt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // lineare Lautstärke 0.0..1.0 in dB umwandeln und setzen
    public static void setClipVolumeLinear(Clip clip, double linearGain) {
        if (clip == null) return;
        double g = Math.max(0.0, Math.min(1.0, linearGain));
        float dB;
        if (g <= 0.0001) {
            dB = -80f;
        } else {
            dB = (float)(20.0 * Math.log10(g));
            if (dB < -80f) dB = -80f;
        }
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gain.getMinimum();
                float max = gain.getMaximum();
                float v = Math.max(min, Math.min(max, dB));
                gain.setValue(v);
            } else {
                System.out.println("MASTER_GAIN nicht unterstützt (linear). dB=" + dB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Clip loopClip(String resourcePath, float initialVolumeDb) {
        try {
            Clip clip = loadClipFromResource(resourcePath);
            setClipVolume(clip, initialVolumeDb);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void playOneShotNoOverlap(String resourcePath, float volumeDb, Clip[] activeClipHolder) {
        try {
            Clip active = activeClipHolder[0];
            if (active != null && active.isActive()) return;

            Clip clip = loadClipFromResource(resourcePath);
            setClipVolume(clip, volumeDb);
            clip.setFramePosition(0);
            clip.start();
            activeClipHolder[0] = clip;

            clip.addLineListener(ev -> {
                if (ev.getType() == LineEvent.Type.STOP) {
                    try { clip.close(); } catch (Exception ex) { ex.printStackTrace(); }
                    activeClipHolder[0] = null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playOneShotNoOverlapLinear(String resourcePath, double linearGain, Clip[] activeClipHolder) {
        try {
            Clip active = activeClipHolder[0];
            if (active != null && active.isActive()) return;

            Clip clip = loadClipFromResource(resourcePath);
            setClipVolumeLinear(clip, linearGain);
            clip.setFramePosition(0);
            clip.start();
            activeClipHolder[0] = clip;

            clip.addLineListener(ev -> {
                if (ev.getType() == LineEvent.Type.STOP) {
                    try { clip.close(); } catch (Exception ex) { ex.printStackTrace(); }
                    activeClipHolder[0] = null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopAndClose(Clip[] holder) {
        if (holder == null || holder[0] == null) return;
        try {
            Clip c = holder[0];
            if (c.isRunning()) c.stop();
            c.close();
            holder[0] = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
