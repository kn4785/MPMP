import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * Creates all inanimate objects such as tubes, item blocks, landings, items, and mobs
 * @author: Blake Wesel
 * @version: 3/30/19
 */
public class InanimateObject implements Serializable {

    final static long serialVersionUID = 3L;

    String type;
    boolean show_item        = false; //if true, displays item and turns on gave_item
    boolean gave_item        = false;
    boolean can_be_collected = false;
    boolean collected        = false;
    boolean dead             = false;
    int ticks_started_to_show_item;
    InanimateObject item;

    int location_in_list = 0;

    double jumping_velocity             = 0;
    double acceleration                 = .03; //.03

    double standard_speed = .8;

    String direction;

    ImageIcon image = null;

    int number_of_sprite_images = 0;
    int necessary_ticks_to_change_image = 15;
    int image_counter = 1;
    double xLoc;
    double yLoc;

    double map_location_xLoc; // this is where the object is in the level
    double map_location_yLoc;

    int distanceTraveled = 0;

    int grass_yLoc = 694; // ground level assuming the dimensions are 1280x720

    InanimateObject objectCollidedWith = null;
    InanimateObject fly_trap           = null;

    boolean hasFlyTrap = false;

    Polygon hitbox;
    Polygon upper_hitbox;
    int upper_hitbox_length = 20;

    /**
     * This is the constructor for a general object for the level, mostly for aesthetics
     * @param type - Type of object
     */
    public InanimateObject(String type) {
        this.type = type;

        if(type.equals("grass")) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/new_grass.gif")));
            } catch (IOException e) {
                System.out.println("Can't find the grass floor image inside inanimate object class");
            }

            xLoc = 0;
            yLoc = 720 - image.getIconHeight() + 3; // assume the dimensions are 1280x720

            grass_yLoc = (int)yLoc;
        }

        map_location_xLoc = xLoc;
        map_location_yLoc = yLoc;

        int xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};
        hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        int upper_xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int upper_yLocs[] = {(int)yLoc - 2, (int)yLoc - 2, (int)yLoc + upper_hitbox_length, (int)yLoc + upper_hitbox_length};
        upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
    }

    public InanimateObject(String type, int length_of_level) {
        this.type = type;

        if(type.equals("flower")) {
            int type_of_flower = (int)(Math.random() * 4) + 1;
            try{
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/flower_"+type_of_flower+".png")));
            } catch (IOException e) {
                System.out.println("Can't find the flower_1 image inside InanimateObject class");
            }

            this.yLoc = grass_yLoc - image.getIconHeight();
            this.xLoc = Math.random() * length_of_level;
        }

        else if(type.equals("flagpole")) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/flagpole.png")));
            } catch (IOException e) {
                System.out.println("Can't find the flagpole image inside inanimate object class");
            }
            this.yLoc = grass_yLoc - image.getIconHeight();
            this.xLoc = length_of_level;
        }

        else if(type.equals("bowser_castle")) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/bowser_castle.png")));
            } catch (IOException e) {
                System.out.println("Can't find the bowser_castle image inside inanimate object class");
            }
            this.yLoc = grass_yLoc - image.getIconHeight();
            this.xLoc = length_of_level + 200;
        }

        map_location_xLoc = xLoc;
        map_location_yLoc = yLoc;

        int xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};
        hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        int upper_xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int upper_yLocs[] = {(int)yLoc - 2, (int)yLoc - 2, (int)yLoc + upper_hitbox_length, (int)yLoc + upper_hitbox_length};
        upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
    }

    /**
     * This is the constructor for the item blocks, green tubes, and landings where the locations are based upon level
     * @param type - Type of object (i.e., item block, green tube...)
     * @param xLoc - x Location
     * @param yLoc - y Location
     */
    public InanimateObject(String type, int xLoc, int yLoc) {

        this.type = type;

        if (type.equals("item_block")) {

            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/item_block_1.png")));
            } catch (IOException e) {
                System.out.println("Can't find the item_block_1 image inside InanimateObject class");
            }
            number_of_sprite_images = 4;
            this.xLoc = xLoc;
            this.yLoc = yLoc;

            int xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
            int yLocs[] = {yLoc, yLoc, yLoc + image.getIconHeight(), yLoc + image.getIconHeight()};

            hitbox = new Polygon(xLocs, yLocs, xLocs.length);
            necessary_ticks_to_change_image = 40;

            generateItem();

        } else if(type.equals("solid_item_block")) {

            this.type = "solid item block";

            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/solid_item_block.png")));
            } catch (IOException e) {
                System.out.println("Can't find the solid_item_block image inside InanimateObject class");
            }

            number_of_sprite_images = 1;
            this.xLoc = xLoc;
            this.yLoc = yLoc;

            int xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
            int yLocs[] = {yLoc, yLoc, yLoc + image.getIconHeight(), yLoc + image.getIconHeight()};

            hitbox = new Polygon(xLocs, yLocs, xLocs.length);
            necessary_ticks_to_change_image = 40;

        } else if(type.equals("green_fly_trap")) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/green_fly_trap1.png")));
            } catch (IOException e) {
                System.out.println("Can't find the solid_item_block image inside InanimateObject class");
            }

            number_of_sprite_images = 2;
            this.xLoc = xLoc + 12; // places it in the center of the tube
            this.yLoc = yLoc + 30; // places it inside the tube

            direction = "up";

            int xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
            int yLocs[] = {yLoc, yLoc, yLoc + image.getIconHeight(), yLoc + image.getIconHeight()};

            hitbox = new Polygon(xLocs, yLocs, xLocs.length);
            necessary_ticks_to_change_image = 40;
        }

        map_location_xLoc = xLoc;
        map_location_yLoc = yLoc;

        updateHitbox();
    }

    /**
     * For the items that come up out of the item block
     * @param type - Type of item
     * @param xLoc - The specific x location the item should be at
     * @param yLoc - The specific y location the item should be at
     */
    public InanimateObject(String type, double xLoc, double yLoc) {

        if(type.equals("coin")) {
            this.type = "coin";
            this.xLoc = xLoc;
            this.yLoc = yLoc;
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/coin.png")));
            } catch (IOException e) {
                System.out.println("Can't find the coin image inside inanimate object class");
            }

        } else if(type.equals("mushroom")) {
            this.type = "mushroom";
            this.xLoc = xLoc;
            this.yLoc = yLoc;
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/mushroom.gif")));
            } catch (IOException e) {
                System.out.println("Can't find the mushroom image inside inanimate object class");
            }

        } else if(type.equals("1 up")) {
            this.type = "1 up";
            this.xLoc = xLoc;
            this.yLoc = yLoc;
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/1_up.png")));
            } catch (IOException e) {
                System.out.println("Can't find the 1_up image inside inanimate object class");
            }
        } else if(type.equals("fire_flower")) {
            this.type = "fire_flower";
            this.xLoc = xLoc;
            this.yLoc = yLoc;
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/fire_flower.png")));
            } catch (IOException e) {
                System.out.println("Can't find the fire_flower image inside inanimate object class");
            }
        }

        map_location_xLoc = xLoc;
        map_location_yLoc = yLoc;

        int xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};
        hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        int upper_xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int upper_yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + upper_hitbox_length, (int)yLoc + upper_hitbox_length};
        upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
    }

    /**
     * This is the constructor for the mobs for the game
     * @param type - Type of mob
     * @param speed - Speed of the mob
     * @param xLoc - X location of the mob
     * @param yLoc - Y location of the mob
     * @param direction - Direction the mob is facing (left or right)
     */
    public InanimateObject(String type, double speed, double xLoc, double yLoc, String direction) {
        if(type.equals("green_koopa")) {
            this.type = "green_koopa";
            this.xLoc = xLoc;
            this.yLoc = yLoc;
            this.standard_speed = speed;
            this.direction = direction;
            number_of_sprite_images = 2;
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/green_koopa_"+direction+"_1.png")));
            } catch (IOException e) {
                System.out.println("Can't find the green koopa left 1 image inside inanimate object class");
            }

        } else if(type.equals("goomba")) {
            this.type = "goomba";
            this.xLoc = xLoc;
            this.yLoc = yLoc;
            this.standard_speed = speed;
            this.direction = direction;
            number_of_sprite_images = 2;
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/goomba_1 copy.png")));
            } catch (IOException e) {
                System.out.println("Can't find the goomba 1 image inside inanimate object class");
            }
        }

        map_location_xLoc = xLoc;
        map_location_yLoc = yLoc;

        int xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};
        hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        int upper_xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int upper_yLocs[] = {(int)yLoc - upper_hitbox_length, (int)yLoc - upper_hitbox_length, (int)yLoc, (int)yLoc};
        upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
    }

    /**
     * The constructor for green tubes and landings
     * @param type - type of object, either green_tube or landing
     * @param xLoc - x Location
     * @param yLoc - y Location
     * @param size - either 1, 2, or 3. 1 is small, 2 is medium, and 3 is big
     */
    public InanimateObject(String type, int xLoc, int yLoc, int size) {
        this.type = type;

        if(type.equals("landing")) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/landing_" + size + ".png")));
            } catch (IOException e) {
                System.out.println("Can't find the landing " + size + " image inside inanimate object class");
            }
            this.yLoc = yLoc;
            this.xLoc = xLoc;
        }
        updateHitbox();
    }

    public InanimateObject(String type, int xLoc, int yLoc, int size, boolean hasFlyTrap) {
        this.type = type;

        if (type.equals("green_tube")) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/green_tube_" + size + ".png")));
            } catch (IOException e) {
                System.out.println("Can't find the green tube " + size + " image inside inanimate object class");
            }
            this.yLoc = yLoc;
            this.xLoc = xLoc;
            if(hasFlyTrap) {
                this.hasFlyTrap = true;
                fly_trap = new InanimateObject("green_fly_trap", xLoc, yLoc - 5);
            }
        }
        updateHitbox();
    }

    /**
     * Updates the hitboxes of the objects based off their new x and y locations - FOR OFFLINE
     */
    public void updateHitbox() {
        int xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};

        hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        if(!this.type.equals("green_koopa") && !this.type.equals("goomba")) {
            int upper_xLocs[] = {(int) xLoc, (int) xLoc + image.getIconWidth(), (int) xLoc + image.getIconWidth(), (int) xLoc};
            int upper_yLocs[] = {(int) yLoc, (int) yLoc, (int) yLoc + 1, (int) yLoc + 1};
            upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
        } else {
            int upper_xLocs[] = {(int)xLoc, (int)xLoc + image.getIconWidth(), (int)xLoc + image.getIconWidth(), (int)xLoc};
            int upper_yLocs[] = {(int)yLoc - upper_hitbox_length, (int)yLoc - upper_hitbox_length, (int)yLoc, (int)yLoc};
            upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
        }

    }

    /**
     * Updates the hitboxes of the objects based off their new x and y locations - FOR OFFLINE
     */
    public void updateHitbox(int xLoc) {
        int xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getIconHeight(), (int)yLoc + image.getIconHeight()};

        hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        if(!this.type.equals("green_koopa") && !this.type.equals("goomba")) {
            int upper_xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
            int upper_yLocs[] = {(int) yLoc, (int) yLoc, (int) yLoc + 1, (int) yLoc + 1};
            upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
        } else {
            int upper_xLocs[] = {xLoc, xLoc + image.getIconWidth(), xLoc + image.getIconWidth(), xLoc};
            int upper_yLocs[] = {(int)yLoc - upper_hitbox_length, (int)yLoc - upper_hitbox_length, (int)yLoc, (int)yLoc};
            upper_hitbox = new Polygon(upper_xLocs, upper_yLocs, upper_xLocs.length);
        }

    }

    public void updateImage(int ticks) {
        if (ticks % necessary_ticks_to_change_image == 0) {
            image_counter++;
            if (image_counter == number_of_sprite_images + 1) image_counter = 1;
            if(type.equals("item_block")) {
                try {
                    this.image = new ImageIcon(ImageIO.read(new File("images/mario/item_block_" + image_counter + ".png")));
                } catch (IOException e) {
                    System.out.println("Can't find the item_block_" + image_counter + " image inside InanimateObject class");
                }
            }
            else if(type.equals("green_koopa")) {
                try {
                    this.image = new ImageIcon(ImageIO.read(new File("images/mario/green_koopa_"+direction+"_" + image_counter + ".png")));
                } catch (IOException e) {
                    System.out.println("Can't find the green_koopa_"+direction+"_" + image_counter + ".png image inside InanimateObject class");
                }
            }
            else if(type.equals("goomba")) {
                try {
                    this.image = new ImageIcon(ImageIO.read(new File("images/mario/goomba_" + image_counter + " copy.png")));
                } catch (IOException e) {
                    System.out.println("Can't find the goomba_" + image_counter + " copy.png image inside InanimateObject class");
                }
            }
            else if(type.equals("green_fly_trap")) {
                try {
                    this.image = new ImageIcon(ImageIO.read(new File("images/mario/green_fly_trap" + image_counter + ".png")));
                } catch (IOException e) {
                    System.out.println("Can't find the green_fly_trap" + image_counter + ".png image inside InanimateObject class");
                }
            }
        }
    }

    public void updateMovement(int ticks) {
        int travel_distance = 1;
        if(type.equals("green_fly_trap")) {
            if(ticks % 2 == 0) {
                if(direction.equals("up") && distanceTraveled < image.getIconHeight()) {
                    this.yLoc -= travel_distance;
                    distanceTraveled += travel_distance;
                }
                else if(distanceTraveled >= image.getIconHeight()) {
                    if(direction.equals("up"))        direction = "down";
                    else if(direction.equals("down")) direction = "up";
                    distanceTraveled = 0;

                } else if(direction.equals("down") && distanceTraveled < image.getIconHeight()) {
                    this.yLoc += travel_distance;
                    distanceTraveled += travel_distance;
                }
            }
        }
        updateHitbox();
    }

    public void turnIntoSolidBlock() {
        if(type.equals("item_block")) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/solid_item_block.png")));
            } catch (IOException e) {
                System.out.println("Can't find the solid_item_block image inside InanimateObject class");
            }
        }
        type = "solid item block";
    }

    public void moveLeft(double mario_speed) {
        if(direction.equals("right")) {
            double speed = standard_speed - mario_speed;
            xLoc += speed;
            map_location_xLoc += speed;
        }
        else if(direction.equals("left")) {
            double speed = standard_speed + mario_speed;
            xLoc -= speed;
            map_location_xLoc -= speed;
        }
    }

    public void moveRight(double mario_speed) {
        if(direction.equals("right")) {
            double speed = standard_speed + mario_speed;
            xLoc += speed;
            map_location_xLoc += speed;
        }
        else if(direction.equals("left")) {
            double speed = standard_speed - mario_speed;
            xLoc -= speed;
            map_location_xLoc -= speed;
        }
    }

    public void move() {
        if(direction.equals("right")) {
            xLoc += standard_speed;
            map_location_xLoc += standard_speed;
        }
        else if(direction.equals("left")) {
            xLoc -= standard_speed;
            map_location_xLoc -= standard_speed;
        }
    }


    public void setItemNumber(int number) {
        this.location_in_list = number;
    }

    /**
     * Generates an item for the item blocks to hold
     */
    public void generateItem() {
        int magic_number = (int) (Math.random() * 10);

        if (magic_number <= 4) this.item = new InanimateObject("coin", (double) xLoc, (double) yLoc - 1);
        if (magic_number == 5 || magic_number == 6)
            this.item = new InanimateObject("mushroom", (double) xLoc, (double) yLoc - 1);
        if (magic_number == 7) this.item = new InanimateObject("1 up", (double) xLoc, (double) yLoc - 1);
        if (magic_number == 8 || magic_number == 9) this.item = new InanimateObject("fire_flower", (double) xLoc, (double) yLoc - 1);
    }
}
