import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is the user object that's sent over to the server, simplified because of data transfer and because the original
 * class uses BufferedImage when BufferedImage isn't serializable, thus we use ImageIcon which is Serializable
 */

public class SendableUser implements Serializable {

    final static long serialVersionUID = 1L;

    ImageIcon image; // can't use BufferedImage because BufferedImage isn't serializable. Must use ImageIcon

    double xLoc;
    double yLoc;
    double map_location_xLoc;
    double map_location_yLoc;

    int player_number = 1;

    String mario_type = "";

    boolean drawUser = true;
    boolean won = false;
    int number_of_lives = 3;

    Polygon hitbox;
    Polygon lower_hitbox;

    ArrayList<FireBall> fire_balls = new ArrayList<>();

    public SendableUser(double xLoc, double yLoc, double map_location_xLoc, double map_location_yLoc) {
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.map_location_xLoc = map_location_xLoc;
        this.map_location_yLoc = map_location_yLoc;
    }

    public SendableUser(double xLoc, double yLoc, double map_location_xLoc, double map_location_yLoc, BufferedImage image,
                        boolean canDraw, ArrayList<FireBall> fire_balls, int player_number, boolean won, int number_of_lives) {
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.map_location_xLoc = map_location_xLoc;
        this.map_location_yLoc = map_location_yLoc;
        this.image = new ImageIcon(image);
        this.drawUser = canDraw;
        this.fire_balls = fire_balls;
        this.player_number = player_number;
        this.won = won;
        this.number_of_lives = number_of_lives;

        if(player_number == 1) {mario_type = "Red";}
        if(player_number == 2) {mario_type = "Blue";}
        if(player_number == 3) {mario_type = "Yellow";}
        if(player_number == 4) {mario_type = "Green";}
    }

    public void setImage(BufferedImage image) {
        this.image = new ImageIcon(image);
    }

    public void setPlayerNumber(int player_number) {
        this.player_number = player_number;
    }

    /**
     * Updates the hitbox for the user based upon the new locations
     */

    public void updateHitbox() {

        int xLocs[] = {(int) xLoc, (int) xLoc + image.getIconWidth(), (int) xLoc + image.getIconWidth(), (int) xLoc};
        int yLocs[] = {(int) yLoc, (int) yLoc, (int) yLoc + image.getIconHeight(), (int) yLoc + image.getIconHeight()};

        this.hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        int lower_xLocs[] = {(int) xLoc, (int) xLoc + image.getIconWidth(), (int) xLoc + image.getIconWidth(), (int) xLoc};
        int lower_yLocs[] = {(int) yLoc + image.getIconHeight(), (int) yLoc + image.getIconHeight(),
                (int) yLoc + image.getIconHeight() + 1, (int) yLoc + image.getIconHeight() + 1};

        this.lower_hitbox = new Polygon(lower_xLocs, lower_yLocs, lower_xLocs.length);
    }

    /**
     * Updates the hitbox for the user based upon the new locations
     */

    public void updateHitbox(int xLoc) {

        int xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};

        this.hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        int lower_xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
        int lower_yLocs[] = {(int)yLoc + image.getIconHeight(),     (int)yLoc + image.getIconHeight(),
                (int)yLoc + image.getIconHeight() + 1, (int)yLoc + image.getIconHeight() + 1};

        this.lower_hitbox = new Polygon(lower_xLocs, lower_yLocs, lower_xLocs.length);
    }

}
