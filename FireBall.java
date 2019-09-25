import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class FireBall implements Serializable {

    final static long serialVersionUID = 11L;

    final double fire_speed = 3;
    final int ticksBetweenOsilation = 10;
    final double osilationAmount = 1.5;
    final int degreeRotationIncrease = 3;

    ImageIcon image;
    double xLoc;
    double yLoc;

    double map_location_xLoc;
    double map_location_yLoc;

    String direction;
    Polygon hitbox;
    int degree_of_rotation = 0;

    boolean canOsilate  = true;
    boolean travelingUp = false; // this will cause the fire ball to slowly go up and down
    int tickStarted = 0;

    public FireBall(double xLoc, double yLoc, String direction, double map_location_xLoc) {
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.direction = direction;
        this.map_location_xLoc = map_location_xLoc;

        try{
            this.image = new ImageIcon(ImageIO.read(new File("images/mario/fire_ball.png")));
        } catch (IOException e) {
            System.out.println("Can't find the fire_ball image inside FireBall constructor");
        }

        updateHitbox();
    }

    public void updateMovement(int ticks) {
        if(degree_of_rotation >= 359) degree_of_rotation = 0;
        else                          degree_of_rotation += degreeRotationIncrease;
        if(direction.equals("right")) {
            xLoc += fire_speed;
            map_location_xLoc += fire_speed;
        } else if(direction.equals("left")) {
            xLoc -= fire_speed;
            map_location_xLoc -= fire_speed;
        }

        // will cause the fire ball to go up and down
        if(canOsilate) {
            if(ticks - tickStarted >= ticksBetweenOsilation) {
                if(travelingUp) { travelingUp = false; }
                else            { travelingUp = true;  }
                tickStarted = ticks;
            } else {
                if(travelingUp) {yLoc -= osilationAmount;}
                else            {yLoc += osilationAmount;}
            }
        }

    }

    public void updateHitbox() {
        int xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};

        hitbox = new Polygon(xLocs, yLocs, xLocs.length);
    }

    public boolean checkCollisionWithObject(InanimateObject object, int left_side_xLoc, int right_side_xLoc) {
        if(object.xLoc >= left_side_xLoc && object.xLoc <= right_side_xLoc) {
            return object.hitbox.intersects(xLoc, yLoc, image.getIconWidth(), image.getIconHeight());
        }
        return false;
    }

    /**
     * Draws the fireball if the user of this client threw it
     * @param g2
     */
    public void draw(Graphics2D g2) // this only rotates the image itself
    {
        double rotationRequired = Math.toRadians(this.degree_of_rotation);

        double locationX = this.image.getIconWidth() / 2;       // rotating about these points
        double locationY = this.image.getIconHeight() / 2;
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage bufferedImage = null;

        try {
            bufferedImage = ImageIO.read(new File("images/mario/fire_ball.png"));
        } catch(Exception e) {System.out.println("Can't load image inside draw method of FireBall class");}

        // Drawing the rotated image at the required drawing locations
        g2.drawImage(op.filter(bufferedImage, null), (int)xLoc, (int)yLoc, null);
    }

    /**
     * Draws the fireball if the fireball was thrown by another user on a different client
     * @param g2
     * @param xLoc
     */
    public void draw(Graphics2D g2, int xLoc) // this only rotates the image itself
    {
        double rotationRequired = Math.toRadians(this.degree_of_rotation);

        double locationX = this.image.getIconWidth() / 2;       // rotating about these points
        double locationY = this.image.getIconHeight() / 2;
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage bufferedImage = null;

        try {
            bufferedImage = ImageIO.read(new File("images/mario/fire_ball.png"));
        } catch(Exception e) {System.out.println("Can't load image inside draw method 2 of FireBall class");}


        // Drawing the rotated image at the required drawing locations
        g2.drawImage(op.filter(bufferedImage, null), xLoc, (int)yLoc, null);
    }

}
