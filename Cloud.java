import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Cloud implements Serializable{
    double xLoc;
    double yLoc;
    double default_xLoc;
    double speed;
    int left_boundry;
    BufferedImage image;

    public Cloud(double xLoc, double yLoc, int left_boundry)
    {
        this.left_boundry = left_boundry;
        speed             = Math.random() / 2 + .4;
        this.xLoc         = xLoc;
        this.default_xLoc = xLoc;
        this.yLoc         = yLoc;

        try{
            this.image = ImageIO.read(new File("images/mario/mario_cloud.gif"));
        } catch (IOException e) {
            System.out.println("Can't find the mario cloud image inside cloud class");
        }
    }

    public void move()  // default moving
    {
        if(xLoc > (left_boundry - image.getWidth())) this.xLoc -= speed;
        else this.xLoc = this.default_xLoc; // default location off the screen;
    }

    public void moveLeft()
    {
        if(xLoc > (left_boundry - image.getWidth())) this.xLoc -= speed * 2;
        else this.xLoc = this.default_xLoc; // default location off the screen;
    }

    public void moveRight()
    {
        if(xLoc > (left_boundry - image.getWidth())) this.xLoc += speed / 4;
        else this.xLoc = this.default_xLoc; // default location off the screen;
    }
}
