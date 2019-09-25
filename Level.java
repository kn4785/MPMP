import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Stores all the information about the level that the drawing engine is going to load and display on the screen
 * @author Blake Wesel
 * @version: 3/30/19
 */

public class Level implements Serializable{

    int width;   // width and height of the screen
    int height;

    final int top_left_corner_xLoc  = 0;
    final int top_left_corner_yLoc  = 0;
    final int off_screen_location   = 1280; // should be the width of the screen size
    int object_moving_speed;                // the amount of pixel moved every tick for every object
    final int ticksBetweenMobSpawns = 200;

    int right_side_xLoc             = 1280; // 1280x720
    int left_side_xLoc              = 0;
    int top_side_yLoc               = 0;
    int bottom_side_yLoc            = 720;

    int current_index_for_item_blocks       = 0;
    int current_index_for_landings          = 0;
    int current_index_for_solid_item_blocks = 0;
    int current_index_for_green_tubes       = 0;

    int item_number = 0;

    String level;

    Scanner reader = null;

    boolean sendingObject = false;

    Mario user;
    KoopaAI koopaAI;
    DrawingEngine drawingEngine;
    Music miscellaneousSounds;
    Music mario_theme_song;
    boolean playMusic;

    InanimateObject grass_floor_for_mario;
    InanimateObject flagpole;
    InanimateObject bowser_castle;

    SendableObject sendableObject = new SendableObject();


    int number_of_clouds      = 12;
    int number_of_flowers     = 100;
    int number_of_item_blocks = 15;
    int number_of_green_tubes = 20;
    int number_of_landings    = 20;
    
    int ticks = 0;

    int length_of_level = 0;

    boolean testing = false; // used to test the KoopaAI

    ArrayList<Cloud>           clouds            = new ArrayList<>();
    ArrayList<InanimateObject> flowers           = new ArrayList<>();
    ArrayList<InanimateObject> item_blocks       = new ArrayList<>();
    ArrayList<InanimateObject> mobs              = new ArrayList<>();
    ArrayList<InanimateObject> green_tubes       = new ArrayList<>();
    ArrayList<InanimateObject> landings          = new ArrayList<>();
    ArrayList<InanimateObject> solid_item_blocks = new ArrayList<>(); // this doesn't count for item blocks that became solid

    // online attributes
    ArrayList<InanimateObject> itemsCollected     = new ArrayList<>();
    ArrayList<InanimateObject> itemsBlocksPunched = new ArrayList<>();
    ArrayList<InanimateObject> deadFlytraps       = new ArrayList<>();

    // images
    BufferedImage green_tube;
    BufferedImage mario_cloud;
    BufferedImage mario_coin;
    BufferedImage mario_life_icon;
    BufferedImage mario_face;
    BufferedImage mini_flagpole;
    BufferedImage mario_background;  // 897 x 550

    String gameMode = "logical";  // logical is where the KoopaAI uses Machine Learning to determine best attributes for
                                  // the mob. If the gameMode is set to random, then it randomly spawns mobs

    public Level(String level, Mario user, int width, int height, boolean playMusic) {
        this.level = level;
        this.user  = user;
        this.playMusic = playMusic;
        this.width = width;
        this.height = height;
        this.right_side_xLoc = width;       // establish what the dimensions of the screen are and where the boundaries
        this.bottom_side_yLoc = height;     // should be
        this.object_moving_speed = (int)user.speed;

        miscellaneousSounds = new Music(playMusic);
        mario_theme_song    = new Music(playMusic);

        this.koopaAI = new KoopaAI(left_side_xLoc, right_side_xLoc);

        loadLevelData(); // loads the data for level by reading the text file (the stuff dependent on the level)
        loadImages();    // loads all the images and stores them as class variables for the drawing engine to use

        // these items will be constant for each level (stuff not dependent on the level):

        for (int i = 0; i < number_of_clouds; i++) {
            clouds.add(new Cloud(Math.random() * 1000 + right_side_xLoc + 100, Math.random() * 450 + top_left_corner_yLoc,
                    top_left_corner_xLoc));
        }

        for (int i = 0; i < number_of_flowers; i++) {
            flowers.add(new InanimateObject("flower", length_of_level));
        }

        grass_floor_for_mario = new InanimateObject("grass");
        flagpole              = new InanimateObject("flagpole", length_of_level);
        bowser_castle         = new InanimateObject("bowser_castle", length_of_level);

        if(playMusic)
        {
            try{
                mario_theme_song.playMusic("mario_theme_song2.wav"); // plays theme music
            }catch(Exception e){System.out.println("Can't play music...");}
        }

    }

    /**
     * Looks at a file that contains all the information about the level, puts it in variables, and creates objects with
     * that information. All of data loaded is unique for that specific level
     */
    public void loadLevelData() {

        // reset the arrayLists
        item_blocks.clear();
        mobs.clear();
        green_tubes.clear();
        landings.clear();
        solid_item_blocks.clear();

        String type = "";
        int xLoc = 0;
        int yLoc = 0;
        int size = 0;
        boolean hasFlyTrap = false;

        // create the buffer reader

        try {
            reader = new Scanner(new File("levels/" + level + ".txt"));
        } catch(IOException io) {
            System.out.println("Couldn't find the level file in level class");
        }

        // Uncomment down below to read what the scanner is reading
        /*
        while(reader.hasNext()) {
            String string = reader.nextLine();
            if(string.equals("\n")) {System.out.println("Read: backslash n\n");}
            else                    {System.out.println("Read: " + string);}
        }*/

        // keep reading until you read the length of the level

        boolean doneReadingNotes = false;
        try {
            while(reader.hasNext() && !doneReadingNotes) {
                String string = reader.nextLine();
                try{
                    length_of_level = Integer.parseInt(string);
                    doneReadingNotes = true;
                } catch(Exception e) {
                }
            }
        } catch(Exception e) {
            System.out.println("Error in reading past notes");
        }

        // start reading the rest of the attributes for the level and create objects for them

        try {
            while (reader.hasNext()) {
                type = reader.nextLine();
                xLoc = Integer.parseInt(reader.nextLine());
                yLoc = Integer.parseInt(reader.nextLine());
                if(type.equals("green_tube") || type.equals("landing")) {
                    size = Integer.parseInt(reader.nextLine());
                    if(type.equals("green_tube")) {
                        if(reader.nextLine().equals("true")) {hasFlyTrap = true;}
                    }
                }

                if(type.equals("item_block"))            {createInanimateObjects(item_blocks,       type, xLoc, yLoc, size, hasFlyTrap);}
                else if(type.equals("green_tube"))       {createInanimateObjects(green_tubes,       type, xLoc, yLoc, size, hasFlyTrap);}
                else if(type.equals("landing"))          {createInanimateObjects(landings,          type, xLoc, yLoc, size, hasFlyTrap);}
                else if(type.equals("solid_item_block")) {createInanimateObjects(solid_item_blocks, type, xLoc, yLoc, size, hasFlyTrap);}
                size = 0; // resets for the next reading
                hasFlyTrap = false;
            }
        } catch(Exception io) {
            System.out.println("Error in grabbing the attributes from the level text file in the level class");
            io.printStackTrace();
        }
    }

    /**
     * Loads all the images that will be used
     */
    public void loadImages() {
        try {
            this.mario_cloud = ImageIO.read(new File("images/mario/mario_cloud.gif"));
        } catch (IOException e) {
            System.out.println("Can't find the mario cloud image inside level class");
        }

        try {
            this.green_tube = ImageIO.read(new File("images/mario/short_green_tube.gif"));
        } catch (IOException e) {
            System.out.println("Can't find the green tube image inside level class");
        }

        try {
            this.mario_background = ImageIO.read(new File("images/mario/mario_background.gif"));
        } catch (IOException e) {
            System.out.println("Can't find the mario background image inside level class");
        }

        try {
            this.mario_coin =
                    ImageIO.read(new File("images/mario/coin.png"));
        } catch (IOException e) {
            System.out.println("Can't find the coin image inside level class");
        }
        try {
            this.mario_life_icon =
                    ImageIO.read(new File("images/mario/mario_life_image.gif"));
        } catch (IOException e) {
            System.out.println("Can't find the mario life icon inside level class");
        }
        try {
            this.mini_flagpole =
                    ImageIO.read(new File("images/mario/mini_flagpole.png"));
        } catch (IOException e) {
            System.out.println("Can't find the mini flagpole image inside level class");
        }
        try {
            this.mario_face =
                    ImageIO.read(new File("images/mario/"+ user.mario_type + "/super_mario_face.gif"));
        } catch (IOException e) {
            System.out.println("Can't find the super_mario_face image inside level class");
        }
    }

    /**
     * Creates the objects that will be used in the level
     * @param size - 1 is the smallest, 2 is medium sized, and 3 is the largest (only for green tubes and landings)
     */
    public void createInanimateObjects(ArrayList<InanimateObject> list, String type, int xLoc, int yLoc, int size, boolean hasFlyTrap) {
        if(size == 0) {
            InanimateObject obj = new InanimateObject(type, xLoc, yLoc);
            obj.setItemNumber(item_number);
            list.add(obj);
        }
        else {
            if(type.equals("green_tube")) {
                InanimateObject obj = new InanimateObject(type, xLoc, yLoc, size, hasFlyTrap);
                obj.setItemNumber(item_number);
                list.add(obj);
            } else {
                InanimateObject obj = new InanimateObject(type, xLoc, yLoc, size);
                obj.setItemNumber(item_number);
                list.add(obj);
            }
        }

        item_number++;
    }

    /**
     * Updates the game performing calculations, checking for collisions of hitboxes,
     */
    public void update() {

        //time
        Date time = new Date();
        long new_time = time.getTime();
        long elapsed_time = new_time - user.old_time;
        if (elapsed_time >= 1000 && user.time_left_for_level > 0) {
            user.time_left_for_level--;
            user.old_time = new_time;
        }
        if (user.time_left_for_level == 0 && !user.dying && !user.won) {
            mario_theme_song.stopMusic("mario_theme_song2.wav");
            user.die(ticks);
            if (user.number_of_lives >= 1) {
                try {
                    miscellaneousSounds.playMusic("mario_dies.wav");
                } catch (Exception e) {
                    System.out.println("Cant play downsizing sound in level class");
                }
                user.number_of_lives--;
            }

        }

        // checks to see if mario won

        boolean won = flagpole.hitbox.intersects(user.xLoc, user.yLoc, 10, user.image.getHeight());
        if (won) {
            // if mario hits the top of the flagpole, then he get's a 1 up
            if (flagpole.upper_hitbox.intersects(user.xLoc, user.yLoc, 10, user.image.getHeight())) {
                try {
                    miscellaneousSounds.playMusic("1_up.wav"); // plays theme music
                } catch (Exception e) {
                    System.out.println("Can't play 1_up.wav in level class...");
                }
            }
            user.canDie = false;
            user.won = true;
            user.canJump = false;
            user.canMoveForward = false;
            user.canMoveBackward = false;
            if (!user.playedLevelCompleteMusic) {
                mario_theme_song.stopMusic("mario_theme_song2.wav");
                try {
                    miscellaneousSounds.playMusic("level_complete.wav");
                } catch (Exception e) {
                    System.out.println("Cant play level complete sound in level class");
                }
                user.playedLevelCompleteMusic = true;
                user.tick_mario_won = ticks;
                user.number_of_times_won++;
            }
            if (ticks - user.tick_mario_won == 1) {
                user.number_of_lives++;
            }

            boolean p1Finished = true;
            boolean p2Finished = true;
            boolean p3Finished = true;
            boolean p4Finished = true;

            for(SendableUser user : drawingEngine.serverUsers) {
                if(!(user.won || user.number_of_lives == 0)) {
                    if(user.player_number == 1) p1Finished = false;
                    if(user.player_number == 2) p2Finished = false;
                    if(user.player_number == 3) p3Finished = false;
                    if(user.player_number == 4) p4Finished = false;
                }
            }

            if (ticks - user.tick_mario_won > 800 && p1Finished && p2Finished && p3Finished && p4Finished) {
                restartGame();
                this.user.won = true; // allows other users to finish the level too
                drawingEngine.playing = false;
                drawingEngine.titleScreen = true;
            }

            if (user.number_of_times_won == 3) {
                koopaAI.clearKnownDecisions();
            }
        }

        //koopa AI throwing mobs
        if (gameMode.equals("random")) { // random game mode
            if (ticks % ticksBetweenMobSpawns == 0) {
                koopaAI.tick_started_throwing = ticks;
                double speed = Math.random() * .4 + .6;
                String direction;
                if (user.xLoc < koopaAI.xLoc) {
                    direction = "left";
                } else {
                    direction = "right";
                }

                koopaAI.mob = new InanimateObject("green_koopa", speed, koopaAI.xLoc, koopaAI.yLoc, direction);
                mobs.add(koopaAI.mob);
            }

        } else if (ticks % ticksBetweenMobSpawns == 0 && !sendingObject) { // logical gamemode using machine learning
            InanimateObject new_mob = koopaAI.makeMove(ticks, (int) user.fake_xLoc);
            mobs.add(new_mob);
            koopaAI.mobs_created.add(new_mob);
        }

        for (int j = 0; j < mobs.size(); j++) {
            // if the mob landed on grass
            InanimateObject mob = mobs.get(j);
            mob.objectCollidedWith = null;
            boolean result = false;
            if (grass_floor_for_mario.hitbox.intersects(mob.xLoc, mob.yLoc, mob.image.getIconWidth(), mob.image.getIconHeight())) {
                result = true;
                mob.objectCollidedWith = grass_floor_for_mario;
            }

            // if not, check to see if it landed on green tubes, landings, item blocks, or solid item blocks
            if (!result) {
                for (int i = 0; i < green_tubes.size(); i++) {
                    if (green_tubes.get(i).upper_hitbox.intersects(mob.xLoc, mob.yLoc + mob.image.getIconHeight(),
                            mob.image.getIconWidth(), 10)) {
                        result = true;
                        mob.objectCollidedWith = green_tubes.get(i);
                        break;
                    }
                }
            }

            if (!result) {
                for (int i = 0; i < landings.size(); i++) {
                    if (landings.get(i).upper_hitbox.intersects(mob.xLoc, mob.yLoc + mob.image.getIconHeight(),
                            mob.image.getIconWidth(), 10)) {
                        result = true;
                        mob.objectCollidedWith = landings.get(i);
                        break;
                    }
                }
            }

            if (!result) {
                for (int i = 0; i < item_blocks.size(); i++) {
                    if (item_blocks.get(i).upper_hitbox.intersects(mob.xLoc, mob.yLoc + mob.image.getIconHeight(),
                            mob.image.getIconWidth(), 10)) {
                        result = true;
                        mob.objectCollidedWith = item_blocks.get(i);
                        break;
                    }
                }
            }

            if (!result) {
                for (int i = 0; i < solid_item_blocks.size(); i++) {
                    if (solid_item_blocks.get(i).upper_hitbox.intersects(mob.xLoc, mob.yLoc + mob.image.getIconHeight(),
                            mob.image.getIconWidth(), 10)) {
                        result = true;
                        mob.objectCollidedWith = solid_item_blocks.get(i);
                        break;
                    }
                }
            }

            // if the mob goes off screen, there is no grass hitbox out that far
            // thus the mob keeps on falling. 607 is where the mob hits the ground
            if (!result) {
                if (mob.yLoc > 607) {
                    result = true;
                    mob.objectCollidedWith = grass_floor_for_mario;
                }
            }

            // if the mob didn't collide with anything
            if (!result) {
                mob.jumping_velocity += mob.acceleration;
                mob.yLoc += mob.jumping_velocity;
                mob.updateHitbox();
            } else {
                if (mob.objectCollidedWith.type.equals("grass")) {
                    mob.yLoc = grass_floor_for_mario.yLoc - 48;
                } else {
                    mob.yLoc = mob.objectCollidedWith.yLoc - mob.image.getIconHeight();
                }

                // reset mob's jumping velocities for when they fall off the item
                mob.jumping_velocity = 0;
            }
        }

        // koopa AI moving
        if (!testing) {
            if (koopaAI.xLoc > top_left_corner_xLoc) {
                koopaAI.moveLeft(user.xLoc);
            } else testing = true;
        } else {
            if (koopaAI.xLoc < right_side_xLoc - koopaAI.image.getIconWidth()) {
                koopaAI.moveRight(user.xLoc);
            } else testing = false;
        }

        // dying animation
        if (!user.dying) {
            user.move(ticks);
            user.updateHitbox();
        } else {
            user.canJump = false;
            user.canMoveForward = false;
            user.canMoveBackward = false;
            user.movingBackward = false;
            user.movingForward = false;
            int current_tick = ticks - user.tick_mario_started_dying;
            if (current_tick < 50) {
            }                               // beginning of animation, just stands there
            else if (current_tick >= 50 && current_tick <= 90) {
                user.yLoc -= 2;
            } else if (current_tick > 90 && user.yLoc <= 1200) {
                user.yLoc += 4;
            } else if (user.yLoc >= 1200) {
                if(user.number_of_lives != 0) {
                    restartGame();
                } else {
                    drawingEngine.titleScreen = true;
                    drawingEngine.playing     = false;
                }
            }
            user.updateImage(ticks);
        }

        // clouds
        for (int i = 0; i < clouds.size(); i++) {
            Cloud cloud = clouds.get(i);
            if (user.movingForward && user.moveObjects) {
                cloud.moveLeft();
            } else if (user.movingBackward && user.moveObjects) {
                cloud.moveRight();
            } else {
                cloud.move();
            }
        }

        // fly traps
        for (int i = 0; i < green_tubes.size(); i++) {
            if (green_tubes.get(i).hasFlyTrap) {
                InanimateObject fly_trap = green_tubes.get(i).fly_trap;
                fly_trap.updateImage(ticks);
                fly_trap.updateMovement(ticks);
            }
        }

        // collision detection for fireballs

        if (user.fire_balls.size() != 0) {
            for (int i = 0; i < user.fire_balls.size(); i++) {
                FireBall fireBall = user.fire_balls.get(i);

                // green tubes
                for (InanimateObject greenTube : green_tubes) {
                    // on screen
                    if (fireBall.checkCollisionWithObject(greenTube, left_side_xLoc, right_side_xLoc)) {
                        try {
                            user.fire_balls.remove(i);
                            i--;
                        } catch (Exception e) {
                        }
                    }
                }

                // solid item blocks
                for (InanimateObject solidBlock : solid_item_blocks) {
                    // on screen
                    if (fireBall.checkCollisionWithObject(solidBlock, left_side_xLoc, right_side_xLoc)) {
                        try {
                            user.fire_balls.remove(i);
                            i--;
                        } catch (Exception e) {
                        }
                    }
                }

                // item blocks
                for (InanimateObject itemBlock : item_blocks) {
                    // on screen
                    if (fireBall.checkCollisionWithObject(itemBlock, left_side_xLoc, right_side_xLoc)) {
                        try {
                            user.fire_balls.remove(i);
                            i--;
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }

        // mobs
        for (int i = 0; i < mobs.size(); i++) {
            InanimateObject mob = mobs.get(i);

            // collision detection for mobs

            boolean moveRightResult = false;
            boolean moveLeftResult = false;

            for (int j = 0; j < green_tubes.size(); j++) {
                InanimateObject green_tube = green_tubes.get(j);
                if (!moveRightResult) {
                    mob.objectCollidedWith = green_tube;
                    moveRightResult = green_tube.hitbox.intersects(mob.xLoc + mob.standard_speed + user.speed,
                            mob.yLoc, mob.image.getIconWidth(),
                            mob.image.getIconHeight() - 10);
                }
                if (!moveLeftResult) {
                    mob.objectCollidedWith = green_tube;
                    moveLeftResult = green_tube.hitbox.intersects(mob.xLoc - mob.standard_speed - user.speed,
                            mob.yLoc, mob.image.getIconWidth(),
                            mob.image.getIconHeight() - 10);
                }
            }

            for (int j = 0; j < item_blocks.size(); j++) {
                InanimateObject item_block = item_blocks.get(j);
                if (!moveRightResult) {
                    mob.objectCollidedWith = item_block;
                    moveRightResult = item_block.hitbox.intersects(mob.xLoc + mob.standard_speed + user.speed,
                            mob.yLoc, mob.image.getIconWidth(),
                            mob.image.getIconHeight() - 10);
                }

                if (!moveLeftResult) {
                    mob.objectCollidedWith = item_block;
                    moveLeftResult = item_block.hitbox.intersects(mob.xLoc - mob.standard_speed - user.speed,
                            mob.yLoc, mob.image.getIconWidth(),
                            mob.image.getIconHeight() - 10);
                }
            }

            for (int j = 0; j < solid_item_blocks.size(); j++) {
                InanimateObject solid_item_block = solid_item_blocks.get(j);
                if (!moveRightResult) {
                    mob.objectCollidedWith = solid_item_block;
                    moveRightResult = solid_item_block.hitbox.intersects(mob.xLoc + mob.standard_speed + user.speed,
                            mob.yLoc, mob.image.getIconWidth(),
                            mob.image.getIconHeight() - 10);
                }
                if (!moveLeftResult) {
                    mob.objectCollidedWith = solid_item_block;
                    moveLeftResult = solid_item_block.hitbox.intersects(mob.xLoc - mob.standard_speed - user.speed,
                            mob.yLoc, mob.image.getIconWidth(),
                            mob.image.getIconHeight() - 10);
                }
            }

            if (moveRightResult) {
                if (mob.direction.equals("right")) mob.direction = "left";
                else mob.direction = "right";
                user.number_of_mobs_killed++;

            } else if (moveLeftResult) {
                if (mob.direction.equals("right")) mob.direction = "left";
                else mob.direction = "right";
                user.number_of_mobs_killed++;
            }
            if (user.movingForward && user.moveObjects) {
                mob.moveLeft(user.speed);
            } else if (user.movingBackward && user.moveObjects) {
                mob.moveRight(user.speed);
            } else {
                mob.move();
            }
            mob.updateHitbox();
            mob.updateImage(ticks);

            // collision detection for mario

            boolean regular_intersection = mob.hitbox.intersects(user.xLoc, user.yLoc, user.image.getWidth(),
                    user.image.getHeight()); // regular intersection is of the "big" hitboxes, ones that surround the
            // character entirely
            boolean lower_intersection;
            boolean fireBallKilledMob = false;

            // bigger lower hitbox for big mario

            if (user.type.equals("big") || user.type.equals("fire")) {
                lower_intersection = mob.upper_hitbox.intersects(user.xLoc, user.yLoc + user.image.getWidth(),
                        user.image.getWidth(), 20);  // if the upper box of the mob intersects the lower hitbox of mario
            } else {
                lower_intersection = mob.upper_hitbox.intersects(user.xLoc, user.yLoc + user.image.getWidth(),
                        user.image.getWidth(), 4);  // if the upper box of the mob intersects the lower hitbox of mario
            }


            // check to see if the fireball hit the mob

            if (user.fire_balls.size() != 0 && !mob.dead) {
                for (int j = 0; j < user.fire_balls.size(); j++) {
                    FireBall fireBall = user.fire_balls.get(j);
                    fireBallKilledMob = mob.hitbox.intersects(fireBall.xLoc, fireBall.yLoc, fireBall.image.getIconWidth(),
                            fireBall.image.getIconHeight());
                    if (fireBallKilledMob) {
                        user.fire_balls.remove(j);
                        break;
                    }
                }
            }

            // if the user killed the mob by jumping or throwing a fireball

            if ((lower_intersection && user.jumping_velocity < 0 && !mob.dead && !user.type.equals("dead")) || fireBallKilledMob) {
                mob.dead = true;
                try {
                    miscellaneousSounds.playMusic("mario_killing_mob.wav");
                } catch (Exception e) {
                    System.out.println("Cant play mario_killing_mob sound in level class");
                }

                // if the user got hit by the mob and is going to die

            } else if (regular_intersection && user.canDie && !mob.dead) {
                user.triggerDeath(miscellaneousSounds, mario_theme_song, ticks, mob, koopaAI);
            }
        }

        // collision detection for fly traps

        for (int i = 0; i < green_tubes.size(); i++) {
            if (green_tubes.get(i).hasFlyTrap) {
                InanimateObject mob = green_tubes.get(i).fly_trap;
                if (user.checkCollisionWithFlyTrap(mob) && user.canDie && !mob.dead) {
                    user.triggerDeath(miscellaneousSounds, mario_theme_song, ticks, green_tubes.get(i).fly_trap, koopaAI);
                    break;
                }
            }
        }

        for (int i = 0; i < green_tubes.size(); i++) {
            if (green_tubes.get(i).hasFlyTrap) {
                InanimateObject mob = green_tubes.get(i).fly_trap;
                if (user.fire_balls.size() != 0 && !mob.dead) {
                    for (int j = 0; j < user.fire_balls.size(); j++) {
                        FireBall fireBall = user.fire_balls.get(j);
                        boolean fireBallKilledMob = mob.hitbox.intersects(fireBall.xLoc, fireBall.yLoc, fireBall.image.getIconWidth(),
                                fireBall.image.getIconHeight());
                        if (fireBallKilledMob) {
                            deadFlytraps.add(green_tubes.get(i));
                            green_tubes.get(i).hasFlyTrap = false;
                            mob = null;
                            user.fire_balls.remove(j);
                            i--;
                            break;
                        }
                    }
                }
            }
        }

        // if move the objects backwards...
        if (user.movingForward && user.moveObjects && !user.dying) {
            this.grass_floor_for_mario.xLoc -= object_moving_speed;  // shifts the image across. the picture itself is 8 pixels longer than the screen
            this.drawingEngine.screen_map_xLoc += object_moving_speed;

            if (grass_floor_for_mario.xLoc <= top_left_corner_xLoc - 16) {
                grass_floor_for_mario.xLoc = top_left_corner_xLoc - 8;
            }

            for (int i = 0; i < flowers.size(); i++) {
                InanimateObject flower = flowers.get(i);
                flower.xLoc -= object_moving_speed;
                flower.map_location_xLoc -= object_moving_speed;
            }
            for (int i = 0; i < item_blocks.size(); i++) {
                InanimateObject item_block = item_blocks.get(i);
                item_block.xLoc -= object_moving_speed;
                item_block.map_location_xLoc -= object_moving_speed;
                if (user.extra_item != item_block.item) {
                    item_block.item.xLoc -= object_moving_speed;
                    item_block.item.map_location_xLoc -= object_moving_speed;
                }
                item_block.updateHitbox();
                item_block.item.updateHitbox();
            }
            for (int i = 0; i < green_tubes.size(); i++) {
                InanimateObject green_tube = green_tubes.get(i);
                green_tube.xLoc -= object_moving_speed;
                green_tube.map_location_xLoc -= object_moving_speed;
                if (green_tube.hasFlyTrap) {
                    green_tube.fly_trap.xLoc -= object_moving_speed;
                    green_tube.fly_trap.map_location_xLoc -= object_moving_speed;
                    green_tube.fly_trap.updateHitbox();
                }
                green_tube.updateHitbox();
            }
            for (int i = 0; i < solid_item_blocks.size(); i++) {
                InanimateObject solid_item_block = solid_item_blocks.get(i);
                solid_item_block.xLoc -= object_moving_speed;
                solid_item_block.map_location_xLoc -= object_moving_speed;
                solid_item_block.updateHitbox();
            }

            for (int i = 0; i < landings.size(); i++) {
                InanimateObject landing = landings.get(i);
                landing.xLoc -= object_moving_speed;
                landing.map_location_xLoc -= object_moving_speed;
                landing.updateHitbox();
            }

            if (user.fire_balls.size() != 0) {
                for (int i = 0; i < user.fire_balls.size(); i++) {
                    user.fire_balls.get(i).xLoc -= object_moving_speed;
                    user.fire_balls.get(i).map_location_xLoc -= object_moving_speed;
                    user.fire_balls.get(i).updateHitbox();
                }
            }

            flagpole.xLoc -= object_moving_speed;
            flagpole.map_location_xLoc -= object_moving_speed;
            flagpole.updateHitbox();

            bowser_castle.xLoc -= object_moving_speed;
            bowser_castle.map_location_xLoc -= object_moving_speed;
            bowser_castle.updateHitbox();

            // scoring
            user.location_in_level_for_score++;
            if (user.location_in_level_for_score > user.farthest_location_in_level) {
                user.farthest_location_in_level = user.location_in_level_for_score;
                user.score = 2 * user.farthest_location_in_level;
            }
        }

        // if move the objects forwards...
        if (user.movingBackward && user.moveObjects && !user.dying) {

            grass_floor_for_mario.xLoc += object_moving_speed;  // shifts the image across. the picture itself is 8 pixels longer than the screen
            this.drawingEngine.screen_map_xLoc -= object_moving_speed;

            if (grass_floor_for_mario.xLoc >= top_left_corner_xLoc)
                grass_floor_for_mario.xLoc = top_left_corner_xLoc - 8;
            for (int i = 0; i < flowers.size(); i++) {
                InanimateObject flower = flowers.get(i);
                flower.xLoc += object_moving_speed;
                flower.map_location_xLoc += object_moving_speed;
            }
            for (int i = 0; i < item_blocks.size(); i++) {
                InanimateObject item_block = item_blocks.get(i);
                item_block.xLoc += object_moving_speed;
                item_block.map_location_xLoc += object_moving_speed;
                if (user.extra_item != item_block.item) {
                    item_block.item.xLoc += object_moving_speed;
                    item_block.item.map_location_xLoc += object_moving_speed;
                }
                item_block.updateHitbox();
                item_block.item.updateHitbox();
            }
            for (int i = 0; i < solid_item_blocks.size(); i++) {
                InanimateObject solid_item_block = solid_item_blocks.get(i);
                solid_item_block.xLoc += object_moving_speed;
                solid_item_block.map_location_xLoc += object_moving_speed;
                solid_item_block.updateHitbox();
            }
            for (int i = 0; i < green_tubes.size(); i++) {
                InanimateObject green_tube = green_tubes.get(i);
                green_tube.xLoc += object_moving_speed;
                green_tube.map_location_xLoc += object_moving_speed;
                if (green_tube.hasFlyTrap) {
                    green_tube.fly_trap.xLoc += object_moving_speed;
                    green_tube.fly_trap.map_location_xLoc += object_moving_speed;
                    green_tube.fly_trap.updateHitbox();
                }
                green_tube.updateHitbox();
            }
            for (int i = 0; i < landings.size(); i++) {
                InanimateObject landing = landings.get(i);
                landing.xLoc += object_moving_speed;
                landing.map_location_xLoc += object_moving_speed;
                landing.updateHitbox();
            }

            if (user.fire_balls.size() != 0) {
                for (int i = 0; i < user.fire_balls.size(); i++) {
                    user.fire_balls.get(i).xLoc += object_moving_speed;
                    user.fire_balls.get(i).map_location_xLoc += object_moving_speed;
                    user.fire_balls.get(i).updateHitbox();
                }
            }

            flagpole.xLoc += object_moving_speed;
            flagpole.map_location_xLoc += object_moving_speed;
            flagpole.updateHitbox();

            bowser_castle.xLoc += object_moving_speed;
            bowser_castle.map_location_xLoc += object_moving_speed;
            bowser_castle.updateHitbox();
            // scoring
            user.location_in_level_for_score--;
        }

        if (!user.won) {
            if (user.movingBackward && user.fake_xLoc - user.speed >= 0 && !user.collisionDetected) {
                user.fake_xLoc -= user.speed;
                koopaAI.map_location_xLoc -= user.speed;
            }
            if (user.movingForward && !user.collisionDetected) {
                user.fake_xLoc += user.speed;
                koopaAI.map_location_xLoc += user.speed;

            }
        }

        for (int i = 0; i < item_blocks.size(); i++) {
            InanimateObject item_block = item_blocks.get(i);
            item_block.updateImage(ticks);

            // moving the item up from the block

            if (item_block.show_item && item_block.ticks_started_to_show_item == 0) {
                item_block.ticks_started_to_show_item = ticks;
            } else if (item_block.show_item && ticks - item_block.ticks_started_to_show_item > 0 &&
                    ticks - item_block.ticks_started_to_show_item <= 32) {
                item_block.item.yLoc--;
                item_block.item.updateHitbox();
            } else if (item_block.show_item) {
                item_block.gave_item = true;
                item_block.show_item = false;
            }

            // collision detection and action for items
            if (item_block.item.hitbox.intersects(user.xLoc, user.yLoc, user.image.getWidth(),
                    user.image.getHeight()) && !item_block.item.collected && item_block.item.can_be_collected) {
                item_block.item.collected = true;
                itemsCollected.add(item_block.item);
                if (item_block.item.type.equals("coin")) {
                    user.number_of_coins_collected++;
                    try {
                        miscellaneousSounds.playMusic("getting_a_coin.wav"); // plays theme music
                    } catch (Exception e) {
                        System.out.println("Can't play getting_a_coin.wav in level class...");
                    }
                } else if (item_block.item.type.equals("mushroom")) {
                    if (user.type.equals("little")) {  // uses mushroom
                        user.type = "big";
                        try {
                            miscellaneousSounds.playMusic("upgrade_sound_effect.wav"); // plays theme music
                        } catch (Exception e) {
                            System.out.println("Can't play upgrade_sound_effect.wav in level class...");
                        }
                    } else if (user.extra_item == null) {  // puts mushroom in storage
                        user.extra_item = item_block.item;
                        item_block.item.xLoc = 642; // inside extra item box at the top of the screen
                        item_block.item.yLoc = 99;
                        try {
                            miscellaneousSounds.playMusic("1_up.wav"); // plays theme music
                        } catch (Exception e) {
                            System.out.println("Can't play 1_up.wav in level class...");
                        }
                    } else { // if mario is already big and a mushroom is in storage
                        user.farthest_location_in_level += 1000; // can't use score because user.score = farthest_
                        // location_in_level
                    }
                } else if (item_block.item.type.equals("1 up")) {
                    user.number_of_lives++;
                    try {
                        miscellaneousSounds.playMusic("1_up.wav"); // plays theme music
                    } catch (Exception e) {
                        System.out.println("Can't play 1_up.wav in level class...");
                    }
                } else if (item_block.item.type.equals("fire_flower")) {
                    if (user.type.equals("little") || user.type.equals("big")) {  // uses mushroom
                        user.type = "fire";
                        try {
                            miscellaneousSounds.playMusic("upgrade_sound_effect.wav"); // plays theme music
                        } catch (Exception e) {
                            System.out.println("Can't play upgrade_sound_effect.wav in level class...");
                        }
                    } else if (user.extra_item == null) {  // puts fire in storage
                        user.extra_item = item_block.item;
                        item_block.item.xLoc = 642; // inside extra item box at the top of the screen
                        item_block.item.yLoc = 99;
                        try {
                            miscellaneousSounds.playMusic("1_up.wav"); // plays theme music
                        } catch (Exception e) {
                            System.out.println("Can't play 1_up.wav in level class...");
                        }
                    } else { // if mario is already big and a mushroom is in storage
                        user.farthest_location_in_level += 1000; // can't use score because user.score = farthest_
                        // location_in_level
                    }
                }
            }
        }

        //System.out.println("User fake x: " + user.fake_xLoc + " User fake y: " + user.fake_yLoc);

        if (user.fire_balls.size() != 0) {
            for (int i = 0; i < user.fire_balls.size(); i++) {
                FireBall fireBall = user.fire_balls.get(i);
                if (fireBall.xLoc - user.xLoc > 2000 || fireBall.xLoc - user.xLoc < -500) {
                    user.fire_balls.remove(i);
                    i--;
                } else {
                    user.fire_balls.get(i).updateMovement(ticks);
                }
            }
        }

        user.conductFlashingImages(ticks);

        user.collisionDetected = false;
        ticks++;

        // updates the client's object so it can be sent to the server

        if(!sendingObject) {

            user.sendableUser       = null;
            user.sendableUser       = new SendableUser(user.xLoc, user.yLoc, user.fake_xLoc, user.fake_yLoc, user.image,
                                                       user.drawUser, user.fire_balls, user.player_number, user.won, user.number_of_lives);
            koopaAI.sendableKoopaAI = null;
            koopaAI.sendableKoopaAI = new SendableKoopaAI(koopaAI.image, koopaAI.map_location_xLoc, koopaAI.map_location_yLoc);
            sendableObject          = null;
            sendableObject          = new SendableObject(user.sendableUser, koopaAI.sendableKoopaAI, koopaAI.mobs_created, itemsCollected,
                                                         itemsBlocksPunched, deadFlytraps);

            itemsCollected.clear();
            itemsBlocksPunched.clear();
            deadFlytraps.clear();

        }
    }

    public void setDrawingEngine(DrawingEngine drawingEngine) {
        this.drawingEngine = drawingEngine;
    }

    /**
     * Restarts the attributes for the game to be replayed
     */
    public void restartGame() {
        clouds.clear();
        flowers.clear();
        item_blocks.clear();
        green_tubes.clear();
        landings.clear();
        flagpole      = null;
        bowser_castle = null;
        mobs.clear();
        koopaAI.mobs_created.clear();
        mario_theme_song.playMusic("mario_theme_song2.wav");


        user.xLoc = user.default_xLoc;
        user.yLoc = user.default_yLoc;
        user.fire_balls.clear();

        user.score = 0;
        user.farthest_location_in_level = 0; // if mario is moving left, the
        user.location_in_level_for_score = 0; // current location for mario in the world, changing with the movement of mario

        user.fake_xLoc = user.xLoc; // these cords keep track of where mario is in the level. The cords above are the drawing
        user.fake_yLoc = user.yLoc; // places on the screen. think of fake_xLoc/yLoc as mario's location on the level while
        // xLoc and yLoc are the locations on the screen

        user.number_of_mobs_killed = 0;

        user.ticks_needed_to_change_image = 10;
        user.cooldown_for_downsizing = 0;

        user.tick_mario_started_dying = 0;

        user.direction = "right";

        user.type = "little";  // little = little mario, big = big mario, fire = fire mario, etc.


        user.right_image_counter         = 1;
        user.left_image_counter          = 1;
        user.number_of_sprite_images     = 2;

        user.tick_mario_won              = 0;
        user.number_of_coins_collected   = 0;
        user.time_left_for_level         = 100;  // in seconds
        user.old_time                    = 0;

        user.movingForward               = false;
        user.movingBackward              = false;
        user.canMoveForward              = true;
        user.canMoveBackward             = true;
        user.canJump                     = true;
        user.doubleJumping               = false;
        user.dying                       = false;
        user.canDie                      = true;
        user.jumping                     = false;
        user.playing_jump_sound_effect   = false;
        user.on_a_block                  = false;
        user.on_a_tube                   = false;
        user.on_land                     = false;  // land is the grass
        user.on_a_landing                = false;  // landing is the mushroom things mario stands on
        user.holding_down_a_key          = false;
        user.holding_down_d_key          = false;
        user.collisionDetected           = false;

        user.jumping_velocity            = user.default_jumping_velocity;

        user.moveObjects                 = false;
        user.in_mid_air                  = false;
        user.won                         = false;
        user.playedLevelCompleteMusic    = false;

        user.updateHitbox();

        user.collided_item_box           = null;
        user.extra_item                  = null;
        user.mob_that_killed_mario       = null;

        for (int i = 0; i < number_of_clouds; i++) {
            clouds.add(new Cloud(Math.random() * 1000 + 1000.0, Math.random() * 250 + top_left_corner_yLoc,
                    top_left_corner_xLoc));
        }

        for (int i = 0; i < number_of_flowers; i++) {
            flowers.add(new InanimateObject("flower", length_of_level));
        }

        loadLevelData();

        koopaAI.clearKnownDecisions();

        flagpole      = new InanimateObject("flagpole", length_of_level);
        bowser_castle = new InanimateObject("bowser_castle", length_of_level);

        koopaAI.made_decisions_mob_type       = new ArrayList<>();
        koopaAI.made_decisions_mob_speed      = new ArrayList<>();
        koopaAI.made_decisions_mob_direction  = new ArrayList<>();
        koopaAI.made_decisions_location       = new ArrayList<>();

        koopaAI.known_decisions_mob_type      = new ArrayList<>();
        koopaAI.known_decisions_mob_speed     = new ArrayList<>();
        koopaAI.known_decisions_mob_direction = new ArrayList<>();
        koopaAI.known_decisions_location      = new ArrayList<>();
        koopaAI.mob = null;
        user.faceForward();
    }

}
