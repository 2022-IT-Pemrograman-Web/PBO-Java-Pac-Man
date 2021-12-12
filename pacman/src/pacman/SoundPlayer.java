package pacman;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundPlayer {
    public static synchronized void playSound(final String url) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    //read audio data from whatever source (file/classloader/etc.)
                    InputStream audioSrc = getClass().getResourceAsStream("/audio/" + url);
                    //add buffer for mark/reset support
                    InputStream bufferedIn = new BufferedInputStream(audioSrc);
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

                    clip.open(audioStream);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }
    public static synchronized void playContinuousSound(final String url){
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    //read audio data from whatever source (file/classloader/etc.)
                    InputStream audioSrc = getClass().getResourceAsStream("/audio/" + url);
                    //add buffer for mark/reset support
                    InputStream bufferedIn = new BufferedInputStream(audioSrc);
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

                    clip.open(audioStream);
                    clip.start();
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }
}
