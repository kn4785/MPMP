import javax.swing.*;
import java.io.Serializable;

public class SendableKoopaAI implements Serializable {

    final static long serialVersionUID = 2L;

    ImageIcon image;
    double map_location_xLoc = 800; // this is where the AI is in the level
    double map_location_yLoc = 150;

    public SendableKoopaAI(ImageIcon image, double map_location_xLoc, double map_location_yLoc) {
        this.image = image;
        this.map_location_xLoc = map_location_xLoc;
        this.map_location_yLoc = map_location_yLoc;
    }
}
