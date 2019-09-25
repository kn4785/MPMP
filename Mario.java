import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Creates a Mario entity
 * @author: Blake Wesel
 * @version: 3/30/19
 */
public class Mario {

    final int differenceInMarioSpriteHeights  = 22; // difference in the height between big mario and little mario
    final int default_cooldown_for_downsizing = 200;
    final int fireBallCooldown                = 50;

    double default_xLoc = 500;
    double default_yLoc = 544;

    double xLoc;
    double yLoc;

    double previous_yLoc = yLoc;

    int score = 0;
    int farthest_location_in_level = 0; // if mario is moving left, the
    int location_in_level_for_score; // current location for mario in the world, changing with the movement of mario

    double fake_xLoc; // these cords keep track of where mario is in the level. The cords above are the drawing
    double fake_yLoc; // places on the screen. think of fake_xLoc/yLoc as mario's location on the level while
    // xLoc and yLoc are the locations on the screen

    int number_of_mobs_killed = 0;

    int ticks_needed_to_change_image = 10;
    int cooldown_for_downsizing = 0;

    int tick_mario_started_dying = 0;

    String direction = "right"; // direction can either be right or left

    String type = "little";  // little = little mario, big = big mario, fire = fire mario, etc.
    
    String mario_type = "";

    int player_number = 2;

    double speed = 2;

    int left_boundry  = 450;       // if mario is in between these boundaries, mario moves. If he is on the edge of these
    int right_boundry = 600;       // boundaries, the objects move instead.

    int right_image_counter             = 1;
    int left_image_counter              = 1;
    int number_of_sprite_images         = 2;

    int tick_mario_won;
    int number_of_coins_collected       = 0;
    int number_of_lives                 = 3;
    int time_left_for_level             = 100;  // in seconds
    long old_time                       = 0;
    int number_of_times_won             = 0;

    boolean ducking                     = false;
    boolean movingForward               = false;
    boolean movingBackward              = false;
    boolean canMoveForward              = true;
    boolean canMoveBackward             = true;
    boolean canJump                     = true;
    boolean dying                       = false;
    boolean canDie                      = true;
    boolean jumping                     = false;
    boolean doubleJumping               = false;
    boolean playing_jump_sound_effect   = false;
    boolean on_a_block                  = false;
    boolean on_a_tube                   = false;
    boolean on_land                     = false;  // land is the grass
    boolean on_a_landing                = false;  // landing is the mushroom things mario stands on
    boolean on_a_solid_block            = false;

    boolean holding_down_a_key          = false;
    boolean holding_down_d_key          = false;
    boolean collisionDetected           = false;  // used to prevent other collisions overwriting a collision
                                                  // aka an item block can't allow mario to walk if it collided with a tube (except for grass)
    double jumping_velocity             = 4.7;
    double default_jumping_velocity     = 4.7; // 5.7
    double acceleration                 = .1;

    boolean moveObjects                 = false;
    boolean in_mid_air                  = false;
    boolean won                         = false;
    boolean playedLevelCompleteMusic    = false;
    boolean drawUser                    = true;

    int tickSpawnedFireball             = 0;

    Music sound;

    Level currentLevel;

    Polygon hitbox       = null;
    Polygon lower_hitbox = null; // only the bottom part of the image is the hitbox

    InanimateObject collided_item_box = null;
    InanimateObject extra_item        = null;
    InanimateObject mob_that_killed_mario = null;
    InanimateObject object_landed_on = null;

    SendableUser sendableUser;

    BufferedImage image;

    ArrayList<FireBall> fire_balls = new ArrayList<>();

    public Mario(boolean playMusic)
    {
        this.xLoc         = default_xLoc;
        this.yLoc         = default_yLoc;
        this.fake_xLoc    = default_xLoc;
        this.fake_yLoc    = default_yLoc;
        this.sound        = new Music(playMusic);

        if(player_number == 1) {mario_type = "Red";}
        if(player_number == 2) {mario_type = "Blue";}
        if(player_number == 3) {mario_type = "Yellow";}
        if(player_number == 4) {mario_type = "Green";}

        try{
            this.image =
                    ImageIO.read(new File("images/mario/"+ mario_type +"/little_mario_right_"+right_image_counter+".gif"));
        } catch (IOException e) {
            System.out.println("Can't find the mario_right image inside Mario constructor");
        }

        int xLocs[] = {(int)xLoc, (int)xLoc + image.getWidth(), (int)xLoc + image.getWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getHeight(), (int)yLoc + image.getHeight()};
        this.hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        // lower hitbox

        int lower_xLocs[] = {(int)xLoc, (int)xLoc + image.getWidth(), (int)xLoc + image.getWidth(), (int)xLoc};
        int lower_yLocs[] = {(int)yLoc + image.getHeight(),     (int)yLoc + image.getHeight(),
                (int)yLoc + image.getHeight() + 1, (int)yLoc + image.getHeight() + 1};

        this.lower_hitbox = new Polygon(lower_xLocs, lower_yLocs, lower_xLocs.length);

        sendableUser = new SendableUser(xLoc, yLoc, fake_xLoc, fake_yLoc, image, drawUser, fire_balls, player_number, won, number_of_lives);
        
    }

    public void getCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
        // change the default y location based off where the grass is
        this.default_yLoc = currentLevel.grass_floor_for_mario.yLoc - image.getHeight();
        this.yLoc = default_yLoc;
    }

    /**
     * Checks to see if mario has collided with any item blocks, green tubes, landings, or has reached the end. The
     * landing and jumping mechanics are handled here
     * @param ticks
     */
    public void move(int ticks) {

        // moving Mario left and right, NOT JUMPING

        checkCollisionWithGreenTubes();
        checkForCollisionOnLeftSide();
        checkForCollisionOnRightSide();

        // gravity if mario isn't standing on something

        ArrayList<InanimateObject> item_blocks = currentLevel.item_blocks;


        // updating booleans to see if mario is standing on something

        on_a_block       = false;
        on_a_tube        = false;
        on_land          = false;
        on_a_landing     = false;
        on_a_solid_block = false;

        for(int i = 0; i < item_blocks.size(); i++) {
            if(item_blocks.get(i).hitbox.intersects(xLoc, yLoc + image.getHeight(), image.getWidth(), 1)) {
                if(!on_a_block) on_a_block = true;
            }
        }
        for(int i = 0; i < currentLevel.solid_item_blocks.size(); i++) {
            if(currentLevel.solid_item_blocks.get(i).hitbox.intersects(xLoc, yLoc + image.getHeight(), image.getWidth(), 1)) {
                if(!on_a_solid_block) on_a_solid_block = true;
            }
        }
        for(int i = 0; i < currentLevel.green_tubes.size(); i++) {
            if(checkCollisionOnTopSideForGreenTubes()) {
                if(!on_a_tube) on_a_tube = true;
            }
        }
        for(int i = 0; i < currentLevel.landings.size(); i++) {
            if(checkCollisionOnTopSideForLandings()) {
                if(!on_a_landing) on_a_landing = true;
            }
        }

        boolean result = currentLevel.grass_floor_for_mario.hitbox.intersects(xLoc,
                yLoc + image.getHeight(), image.getWidth(), 3); // 3 so there is some leeway
        if(result) {on_land = true;}

        // based off if mario is standing on something or not, performs an action

        // freefall
        if(this.movingForward || this.movingBackward && !won) {
            if(!jumping && !on_land && !on_a_block && !on_a_tube && !on_a_landing && !on_a_solid_block) {

                in_mid_air = true;
                jumping_velocity = 0;
                jumping = true;
                playing_jump_sound_effect = true; // technically we aren't however the sound shouldn't play b/c if true
                // sound doesn't play cause its set to true when you play the sound
            }
            if (this.movingBackward && this.canMoveBackward && fake_xLoc - speed >= 0  && !won && !ducking) {
                this.canMoveForward = true;
                if (xLoc - speed < left_boundry) {
                    this.moveObjects = true;
                    this.canMoveBackward = true;
                } else {
                    xLoc -= speed;
                    this.moveObjects = false;
                }
            } else {this.moveObjects = false;}

            if (this.movingForward && this.canMoveForward  && !won && !ducking) {
                this.canMoveBackward = true;
                if (xLoc + speed > right_boundry) {
                    this.moveObjects = true;
                } else {
                    xLoc += speed;
                    this.moveObjects = false;
                }
            }
        }
        else {this.moveObjects = false;}


        // Mario jumping

        if(this.jumping && !won) {
            if(!playing_jump_sound_effect) {
                try{
                    sound.playMusic("jumping_sound_effect.wav");
                }catch(Exception a){System.out.println("Can't play jumping_sound_effect music...");}
                playing_jump_sound_effect = true;
            }

            boolean collided_on_top    = checkForCollisionOnTop(); // collided checks for collision on the top of a box and the ground
            boolean collided_on_bottom = checkForCollisionOnBottom();

            if(!collided_on_top) {
                collided_on_top = checkCollisionOnTopSideForGreenTubes();
            }
            if(!collided_on_top) {
                collided_on_top = checkCollisionOnTopSideForLandings();
            }

            if(collided_on_top && jumping_velocity < 0) { // landing ontop of something
                in_mid_air = false;
                playing_jump_sound_effect = false;
                this.jumping_velocity = default_jumping_velocity;
                this.jumping       = false;
                this.doubleJumping = false;
                if (this.direction.equals("right")) {
                    faceForward();
                } else {
                    faceBackward();
                }

                this.yLoc = object_landed_on.yLoc - image.getHeight();

            } else if(collided_on_bottom && jumping_velocity > 0) { // hitting an item block
                jumping_velocity = -jumping_velocity;
                if(!collided_item_box.type.equals("solid item block")) {
                    currentLevel.itemsBlocksPunched.add(collided_item_box);
                    collided_item_box.show_item = true;
                    collided_item_box.item.can_be_collected = true;
                    collided_item_box.turnIntoSolidBlock();
                    if(!collided_item_box.item.type.equals("coin")) {
                        collided_item_box.item.xLoc = collided_item_box.xLoc;
                        collided_item_box.item.yLoc = collided_item_box.yLoc - 10;
                    } else {
                        collided_item_box.item.xLoc = collided_item_box.xLoc + 6;
                        collided_item_box.item.yLoc = collided_item_box.yLoc - 10;
                    }

                    collided_item_box.item.updateHitbox();

                    try {
                        sound.playMusic("item_coming_up.wav");
                    } catch (Exception e) {
                        System.out.println("Can't play item_coming_up.wav in mario class...");
                    }
                }

            } else {
                jump();
            }
        }

        updateImage(ticks);

    }

    public void jump() {
        if (this.jumping_velocity >= -default_jumping_velocity || in_mid_air) {
            this.yLoc -= jumping_velocity;
            this.jumping_velocity -= acceleration;
            // rounding to 2 decimal places
            this.jumping_velocity = (double) Math.round(this.jumping_velocity * 100d) / 100d;
        } else {
            sound.stopMusic("jumping_sound_effect.wav");
            in_mid_air = false;
            playing_jump_sound_effect = false;
            this.jumping_velocity = default_jumping_velocity;
            this.jumping = false;
            this.doubleJumping = false;
            if (this.direction.equals("right")) {
                faceForward();
            } else {
                faceBackward();
            }
        }
    }

    public void updateImage(int ticks) {
        if (type.equals("dead")) {
            try {
                this.image =
                        ImageIO.read(new File("images/mario/"+ mario_type +"/mario_dies.png"));
            } catch (IOException e) {
                System.out.println("Can't find the mario dies image inside Mario class");
            }
        } else {
            if(this.ducking) {
                try {
                    this.image =
                            ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_ducking_" +
                                    this.direction + ".png"));
                } catch (IOException e) {
                    System.out.println("Can't find the mario " + type + "_" + left_image_counter + " image inside Mario class");
                }
            }
            else if (this.movingBackward && !this.jumping && ticks % ticks_needed_to_change_image == 0 && !this.movingForward) {
                this.left_image_counter++;
                if (this.left_image_counter == number_of_sprite_images + 1) this.left_image_counter = 1;
                try {
                    this.image =
                            ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_left_" +
                                    this.left_image_counter + ".gif"));
                } catch (IOException e) {
                    System.out.println("Can't find the mario " + type + "_" + left_image_counter + " image inside Mario class");
                }

            } else if (this.movingForward && !this.jumping && ticks % ticks_needed_to_change_image == 0 && !this.movingBackward) {
                this.right_image_counter++;
                if (this.right_image_counter == number_of_sprite_images + 1) this.right_image_counter = 1;
                try {
                    this.image =
                            ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_right_" +
                                    this.right_image_counter + ".gif"));
                } catch (IOException e) {
                    System.out.println("Can't find the mario " + type + "_" + this.right_image_counter + " image inside Mario class");
                }

            } else if (this.movingForward && this.jumping) {
                try {
                    this.image =
                            ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_jumping_right.gif"));
                } catch (IOException e) {
                    System.out.println("Can't find the mario " + type + " right jumping image inside Mario class");
                }
                this.right_image_counter = 0;
                this.left_image_counter = 0;

            } else if (this.movingBackward && this.jumping) {
                try {
                    this.image =
                            ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_jumping_left.gif"));
                } catch (IOException e) {
                    System.out.println("Can't find the mario " + type + " left jumping image inside Mario class");
                }
                this.right_image_counter = 0;
                this.left_image_counter = 0;

            } else if (this.jumping) {
                if (this.direction.equals("right")) {
                    try {
                        this.image =
                                ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_jumping_right.gif"));
                    } catch (IOException e) {
                        System.out.println("Can't find the mario " + type + " right jumping image inside Mario class");
                    }
                    this.right_image_counter = 0;
                    this.left_image_counter = 0;
                } else {
                    try {
                        this.image =
                                ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_jumping_left.gif"));
                    } catch (IOException e) {
                        System.out.println("Can't find the mario " + type + " left jumping image inside Mario class");
                    }
                    this.right_image_counter = 0;
                    this.left_image_counter = 0;
                }
            } else if (this.movingForward && this.movingBackward) {
                this.right_image_counter = 1;
                this.left_image_counter = 1;
                if (direction.equals("left")) {
                    try {
                        this.image =
                                ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_left_" +
                                        this.left_image_counter + ".gif"));
                    } catch (IOException e) {
                        System.out.println("Can't find the mario " + type + " left_" + left_image_counter + " image inside Mario class");
                    }
                } else {
                    try {
                        this.image =
                                ImageIO.read(new File("images/mario/"+ mario_type +"/" + type + "_mario_right_" +
                                        this.right_image_counter + ".gif"));
                    } catch (IOException e) {
                        System.out.println("Can't find the mario " + type + " right_" + this.right_image_counter + " image inside Mario class");
                    }
                }
            }
        }
    }


    public void die(int tick) {
        type = "dead";
        this.canMoveBackward = false;
        this.canMoveForward  = false;
        this.tick_mario_started_dying = tick;
        this.dying = true;
        this.canDie = false;
    }


    public void faceForward() {
        if(!this.movingBackward) {
            this.right_image_counter = 1;
            this.left_image_counter = 1;
            try {
                this.image =
                        ImageIO.read(new File("images/mario/"+ mario_type +"/"+type+"_mario_right_" +
                                "" + this.right_image_counter + ".gif"));

            } catch (IOException e) {
                System.out.println("Can't find the "+type+"_mario_right_1 image inside Mario class inside faceForward");
            }
            this.direction = "right";
        }

    }

    public void faceBackward() {
        if(!this.movingForward) {
            this.right_image_counter = 1;
            this.left_image_counter = 1;
            try {
                this.image =
                        ImageIO.read(new File("images/mario/"+ mario_type +"/"+type+"_mario_left_" +
                                "" + this.left_image_counter + ".gif"));
            } catch (IOException e) {
                System.out.println("Can't find the "+type+"_mario_left_1 image inside Mario class inside faceBackward");
            }
            this.direction = "left";
        }
    }

    /**
     * When Mario gets hit, by a mob but still is alive, this conducts the flashing animation
     * @param ticks
     */
    public void conductFlashingImages(int ticks) {
        if(!won && !type.equals("dead") && cooldown_for_downsizing > 0) {
            cooldown_for_downsizing--;
            if(ticks % 10 == 0) {
                if(drawUser) {
                    drawUser = false;
                } else {
                    drawUser = true;
                }
            }
        } else {
            drawUser = true;
        }
    }

    /**
     * Checks collision for item blocks on the top side. If mario does so, returns true, else, false
     * @return true = Collided, false = Not Collided
     */
    public boolean checkForCollisionOnTop() {
        boolean collided = false;
        ArrayList<InanimateObject> item_blocks = currentLevel.item_blocks;

        // if mario lands on top of the item block
        for(int i = 0; i < item_blocks.size(); i++) {
            if(item_blocks.get(i).hitbox.intersects(xLoc, yLoc + image.getHeight(), image.getWidth(), 1)) {
                collided = true;
                object_landed_on = item_blocks.get(i);
            }
        }
        if(!collided) {
            for(int i = 0; i < currentLevel.solid_item_blocks.size(); i++) {
                if(currentLevel.solid_item_blocks.get(i).hitbox.intersects(xLoc, yLoc + image.getHeight(), image.getWidth(), 1)) {
                    collided = true;
                    object_landed_on = currentLevel.solid_item_blocks.get(i);
                }
            }
        }

        // if mario lands on the ground
        if(!collided && currentLevel.grass_floor_for_mario.hitbox.intersects(xLoc,
                yLoc + image.getHeight(), image.getWidth(), 1)) {
            collided = true;
            object_landed_on = currentLevel.grass_floor_for_mario;
        }

        return collided;
    }

    /**
     * Checks collision for item blocks on the bottom side. If mario does so, returns true, else, false
     * @return true = Collided, false = Not Collided
     */
    public boolean checkForCollisionOnBottom() {
        boolean collided = false;
        for(int i = 0; i < currentLevel.item_blocks.size(); i++) {
            if(currentLevel.item_blocks.get(i).hitbox.intersects(xLoc, yLoc, image.getWidth(), 1)){
                collided = true;
                collided_item_box = currentLevel.item_blocks.get(i);
            }
        }
        if(!collided) {
            for(int i = 0; i < currentLevel.solid_item_blocks.size(); i++) {
                if(currentLevel.solid_item_blocks.get(i).hitbox.intersects(xLoc, yLoc, image.getWidth(), 1)){
                    collided = true;
                    collided_item_box = currentLevel.solid_item_blocks.get(i);
                }
            }
        }
        return collided;
    }

    /**
     * Checks collision for item blocks on the left side. If mario does so, returns true, else, false
     * @return true = Collided, false = Not Collided
     */
    public void checkForCollisionOnLeftSide() {

        for(int i = 0; i < currentLevel.item_blocks.size(); i++) {
            boolean moveLeftResult = currentLevel.item_blocks.get(i).hitbox.intersects(xLoc + image.getWidth(), yLoc, 5, image.getHeight());
            if(moveLeftResult && canMoveForward) {
                this.canMoveForward = false;
                this.movingForward  = false;
                collided_item_box   = null;
                collisionDetected   = true;
            } else if(!moveLeftResult && holding_down_d_key && !collisionDetected) {
                this.canMoveForward = true;
                this.movingForward  = true;
            }
        }

        if(this.canMoveForward) {
            for(int i = 0; i < currentLevel.solid_item_blocks.size(); i++) {
                boolean moveLeftResult = currentLevel.solid_item_blocks.get(i).hitbox.intersects(xLoc + image.getWidth(), yLoc, 5, image.getHeight());
                if(moveLeftResult && canMoveForward) {
                    this.canMoveForward = false;
                    this.movingForward  = false;
                    collided_item_box   = null;
                    collisionDetected   = true;
                } else if(!moveLeftResult && holding_down_d_key && !collisionDetected) {
                    this.canMoveForward = true;
                    this.movingForward  = true;
                }
            }
        }
    }

    /**
     * Checks collision for item blocks and solid item blocks on the right side. If mario does so, returns true, else, false
     * @return true = Collided, false = Not Collided
     */
    public void checkForCollisionOnRightSide() {
        boolean moveRightResult;
        for(int i = 0; i < currentLevel.item_blocks.size(); i++) {
            moveRightResult = currentLevel.item_blocks.get(i).hitbox.intersects(xLoc - 1, yLoc, 5, image.getHeight());
            if(moveRightResult && this.canMoveBackward) {
                this.canMoveBackward = false;
                this.movingBackward  = false;
                collided_item_box    = null;
                collisionDetected    = true;
            } else if(!moveRightResult && holding_down_a_key && !collisionDetected) {
                this.canMoveBackward = true;
                this.movingBackward = true;
            }
        }

        if(this.canMoveBackward) {
            for (int i = 0; i < currentLevel.solid_item_blocks.size(); i++) {
                moveRightResult = currentLevel.solid_item_blocks.get(i).hitbox.intersects(xLoc - 1, yLoc, 5, image.getHeight());
                if(moveRightResult && this.canMoveBackward) {
                    this.canMoveBackward = false;
                    this.movingBackward  = false;
                    collided_item_box    = null;
                    collisionDetected    = true;
                } else if(!moveRightResult && holding_down_a_key && !collisionDetected) {
                    this.canMoveBackward = true;
                    this.movingBackward  = true;
                }
            }
        }
    }

    public void checkCollisionWithGreenTubes() {
        this.canMoveForward  = true;
        this.canMoveBackward = true;
        for(int i = 0; i < currentLevel.green_tubes.size(); i++) {
            InanimateObject green_tube = currentLevel.green_tubes.get(i);
            boolean moveRightResult = green_tube.hitbox.intersects(this.xLoc + speed, this.yLoc, image.getWidth(), image.getHeight());
            boolean moveLeftResult  = green_tube.hitbox.intersects(this.xLoc - speed, this.yLoc, image.getWidth(), image.getHeight());

            if (moveRightResult && this.canMoveForward && !collisionDetected) {
                this.canMoveForward = false;
                collisionDetected   = true;
            }
            if (moveLeftResult && this.canMoveBackward && !collisionDetected) {
                this.canMoveBackward = false;
                collisionDetected    = true;
            }
        }
    }

    public boolean checkCollisionOnTopSideForGreenTubes()  {
        for(int i = 0; i < currentLevel.green_tubes.size(); i++){
            InanimateObject green_tube = currentLevel.green_tubes.get(i);
            if(green_tube.upper_hitbox.intersects(this.xLoc, this.yLoc + image.getHeight() - 10, image.getWidth(), 20)) {
                if(!jumping) this.yLoc = green_tube.yLoc - image.getHeight();
                object_landed_on = green_tube;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the user hit the top side of the landings
     * @return
     */
    public boolean checkCollisionOnTopSideForLandings()  {
        for(int i = 0; i < currentLevel.landings.size(); i++){
            InanimateObject landing = currentLevel.landings.get(i);
            if(landing.upper_hitbox.intersects(this.xLoc, this.yLoc + image.getHeight() - 10, image.getWidth(), 20)) {
                if(!jumping) this.yLoc = landing.yLoc - image.getHeight();
                object_landed_on = landing;
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the hitbox for the user based upon the new locations
     */
    public void updateHitbox() {
        int xLocs[] = {(int)xLoc, (int)xLoc + image.getWidth(), (int)xLoc + image.getWidth(), (int)xLoc};
        int yLocs[] = {(int)yLoc, (int)yLoc, (int)yLoc + image.getHeight(), (int)yLoc + image.getHeight()};

        this.hitbox = new Polygon(xLocs, yLocs, xLocs.length);

        int lower_xLocs[] = {(int)xLoc, (int)xLoc + image.getWidth(), (int)xLoc + image.getWidth(), (int)xLoc};
        int lower_yLocs[] = {(int)yLoc + image.getHeight(),     (int)yLoc + image.getHeight(),
                (int)yLoc + image.getHeight() + 1, (int)yLoc + image.getHeight() + 1};

        this.lower_hitbox = new Polygon(lower_xLocs, lower_yLocs, lower_xLocs.length);
    }

    public boolean checkCollisionWithFlyTrap(InanimateObject fly_trap) {
        return fly_trap.hitbox.intersects(this.xLoc, this.yLoc, this.image.getWidth(), this.image.getHeight());
    }

    
    public void triggerDeath(Music miscellaneousSounds, Music mario_theme_song, int ticks, InanimateObject mob, KoopaAI koopaAI) {
        if (cooldown_for_downsizing >= 0) {cooldown_for_downsizing--;}

        else if (type.equals("fire") && extra_item == null) {
            type = "big";
            cooldown_for_downsizing = default_cooldown_for_downsizing;

            // if the user isn't moving
            if(!movingForward && !movingBackward) {

                // update the image facing the direction the user is looking
                if(direction.equals("right"))     {faceForward();}
                else if(direction.equals("left")) {faceBackward();}
            }
            try {
                miscellaneousSounds.playMusic("mario_downsizing.wav");
            } catch (Exception e) {
                System.out.println("Cant play downsizing music in level class");
            }

        }
        else if (type.equals("fire") && extra_item != null) {
            cooldown_for_downsizing = default_cooldown_for_downsizing;
            extra_item = null;
            try {
                miscellaneousSounds.playMusic("extra_item_used.wav");
            } catch (Exception e) {
                System.out.println("Cant play extra item used sound effect in level class");
            }
        }

        else if (type.equals("big") && extra_item == null) {
            type = "little";
            cooldown_for_downsizing = default_cooldown_for_downsizing;

            // if the user isn't moving
            if(!movingForward && !movingBackward) {

                // update the image facing the direction the user is looking
                if(direction.equals("right"))     {faceForward();}
                else if(direction.equals("left")) {faceBackward();}
            }
            try {
                miscellaneousSounds.playMusic("mario_downsizing.wav");
            } catch (Exception e) {
                System.out.println("Cant play downsizing music in level class");
            }
            yLoc += differenceInMarioSpriteHeights;
        }
        else if (type.equals("big") && extra_item != null) {
            cooldown_for_downsizing = default_cooldown_for_downsizing;
            extra_item = null;
            try {
                miscellaneousSounds.playMusic("extra_item_used.wav");
            } catch (Exception e) {
                System.out.println("Cant play extra item used sound effect in level class");
            }
        }
        else if(type.equals("little")) {
            mario_theme_song.stopMusic("mario_theme_song.wav");
            die(ticks);
            if(number_of_lives >= 1) {
                try {
                    miscellaneousSounds.playMusic("mario_dies.wav");
                } catch (Exception e) {
                    System.out.println("Cant play downsizing sound in level class");
                }
                number_of_lives--;
                mob_that_killed_mario = mob;
                koopaAI.learnNewPlacement(mob, fake_xLoc);
            }
            else
            {
                try {
                    miscellaneousSounds.playMusic("mario_game_over.wav");
                } catch (Exception e) {
                    System.out.println("Cant play game over sound in level class");
                }
            }
        }
    }

}
