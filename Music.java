import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.Serializable;

/**
 * plays theme song for this program
 *
 * @author Blake Wesel
 * @version 4/15/19
 */
public class Music implements Serializable {

    Clip clip;
    float volume = -10.0f; // -50.0f is the lowest, 0.0f is the highest
    boolean playMusic;

    public Music(boolean playMusic) {
        this.playMusic = playMusic;
    }

    /**
     * Grabs music file and starts to play the sound
     */
    public void playMusic(String music_name) {

        if(playMusic) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(this.getClass().getResource(music_name));
                this.clip = AudioSystem.getClip();
                this.clip.open(audioInputStream);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume); // Reduce volume by 10 decibels.
                this.clip.start();
            } catch (Exception ex) {
            }
        }
    }
    /**
     * Stops the music that's playing
     */
    public void stopMusic(String music_name)
    {
        try {
            this.clip.stop();
        } catch(Exception ex) {
        }
    }
}