package pacman;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class BackgroundMusic {
    private boolean isPlaying;
    private InputStream audioSrc;
    Clip clip;
    String bgmUrl;
    public BackgroundMusic(String url){
        isPlaying = false;
        bgmUrl = url;
        try {
            clip = AudioSystem.getClip();
            InputStream audioSrc = getClass().getResourceAsStream("/audio/" + bgmUrl);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            clip.open(audioStream);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    public void play(){
        isPlaying = true;
        new Thread(new Runnable() {
            public void run() {
                while (isPlaying)
                    clip.loop(1);
            }
        }).start();
    }
    public void stop(){
        isPlaying = false;
        clip.stop();
    }

    public static synchronized void play(final String url){
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip_ = AudioSystem.getClip();
                    InputStream audioSrc = getClass().getResourceAsStream("/audio/" + url);
                    InputStream bufferedIn = new BufferedInputStream(audioSrc);
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

                    clip_.open(audioStream);
                    while (true){
                        if(!clip_.isRunning()) {
                            clip_.loop(1);
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }
}
