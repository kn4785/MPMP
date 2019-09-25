import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Draws the entire world, ranging from characters to buildings, etc.
 *
 * @author Blake Wesel
 * @version 3/30/19
 */
public class DrawingEngine extends JPanel implements MouseMotionListener, MouseListener, Serializable {

    final int leftDrawingBoundry = -50;    // the points where item's are being drawn because they're not on the screen
    final int rightDrawingBoundry = 1330;

    int width;
    int height;
    int ticks;
    Timer t;
    Level currentLevel;
    Mario user;
    GameEngine gameEngine;

    int map_xLoc = 400;
    int map_yLoc = 100;
    int map_width = 500;

    int stats_yLoc = 30;
    int stats_xLoc = 330;

    int screen_map_xLoc = 1200;  // this is the location of the screen on level. The use is to draw objects on other clients
    int screen_map_yLoc;

    boolean drawHitboxes      = false;
    boolean showOptions       = false;
    boolean showLoadingScreen = false;
    boolean playing           = false;
    boolean titleScreen       = true;
    boolean notDrawn          = true;

    boolean online;

    BufferedImage backgroundImage = null;
    BufferedImage speech_bubble   = null;
    BufferedImage titleImage      = null;
    BufferedImage loadingImage    = null;

    Font gameFont = new Font("Cooper Black", Font.PLAIN, 30);

    // these hold all the objects between all the clients which are only meant to be drawn
    ArrayList<SendableUser>    serverUsers      = new ArrayList<>();
    ArrayList<SendableKoopaAI> serverKoopaAIS   = new ArrayList<>();
    ArrayList<InanimateObject> serverMobs       = new ArrayList<>();
    ArrayList<InanimateObject> serverGreenTubes = new ArrayList<>();
    ArrayList<InanimateObject> serverItemBlocks = new ArrayList<>();

    JPanel jpMenu;
    JPanel jpChat;
    JTextField jtfIP;
    JTextArea jtaChat;
    JTextField jtfEnter;

    DrawingEngine drawingEngine;

    /**
     * Constructor for objects of class drawingEngine
     */
    public DrawingEngine(int width, int height, Timer t, Level currentLevel, Mario user, boolean online, GameEngine gameEngine) {
        this.width           = width;

        this.height          = height;
        this.gameEngine      = gameEngine;
        this.currentLevel = currentLevel;
        this.online = online;
        this.drawingEngine = this;

        // we don't have the loading image or the title screen image yet.
        /*
        try {
            this.loadingImage = ImageIO.read(new File("images/hud/loadingScreen.gif"));
        } catch (IOException e) {
            System.out.println("Can't find loading screen image");
        }
         */
        try {
            this.titleImage = ImageIO.read(new File("images/mainBackground.png"));
        } catch (IOException e) {
            System.out.println("Can't find titleScreen image");
        }


        setPreferredSize(new Dimension(this.width, this.height));

        this.t = t;
        this.user = user;

        this.setBackground(Color.WHITE);

        addMouseMotionListener(this);
    }

    /**
     * Draws the level with components from the currentLevel object
     */
    public void paintComponent(Graphics g) {
        // creating higher quality graphics object
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(4));

        if (titleScreen) {
            g2.drawImage(titleImage, 0, 0, null);
            if(notDrawn) {


                // North Menu Panel
                jpMenu = new JPanel(new FlowLayout());
                jpMenu.setBackground(new Color(15, 140, 250));
                add(jpMenu, BorderLayout.NORTH);

                // Offline and Connect Button
                JButton jbOnline = new JButton("Connect");
                JButton jbOffline = new JButton("Offline");
                jpMenu.add(jbOffline);
                jbOffline.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        titleScreen = false;
                        playing = true;
                        online = false;
                        setOnlineVariable(online);
                        remove(jbOffline);
                        remove(jbOnline);
                        remove(jpMenu);
                        revalidate();
                        repaint();
                    }
                });

                // Online Button

                jpMenu.add(jbOnline);
                jbOnline.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        if (!playing) {
                            playing = true;
                            titleScreen = false;
                            online = true;
                            gameEngine.online = online;
                            remove(jbOffline);
                            remove(jbOnline);
                            remove(jpMenu);
                            revalidate();
                            repaint();

                            String ip = jtfIP.getText();

                            gameEngine.clientThread = new ClientThread(drawingEngine, currentLevel, ip);
                            gameEngine.clientThread.start();
                        }

                    }
                });

                // Enter IP Address
                jtfIP = new JTextField(15);
                jpMenu.add(jtfIP);

                revalidate();
                repaint();
                notDrawn = false;
            }

        } else if (this.showLoadingScreen) {
            g2.drawImage(loadingImage, 0, 0, null);
            this.showLoadingScreen = false;
            this.playing = true;

        } else if (playing) {

            // background color
            g2.setColor(new Color(15, 140, 250)); // other RGB color: 15, 140, 250 Other color: 227, 203, 149
            g2.fillRect(0, 0, currentLevel.width, currentLevel.height);

            if (drawHitboxes) {
                g2.setColor(Color.BLUE);
                for (int i = 0; i < currentLevel.item_blocks.size(); i++) {
                    g2.fillPolygon(currentLevel.item_blocks.get(i).hitbox);
                }

                g2.setColor(Color.ORANGE);
                for (int i = 0; i < currentLevel.green_tubes.size(); i++) {
                    g2.fillPolygon(currentLevel.green_tubes.get(i).hitbox);
                }
            }

            g2.setFont(gameFont);

            // The drawing below are used for both multiplayer and singleplayer modes

            // background image
            g2.drawImage(currentLevel.mario_background, 0, 0, null);

            // grass
            g2.drawImage(currentLevel.grass_floor_for_mario.image.getImage(),
                    (int) currentLevel.grass_floor_for_mario.xLoc,
                    (int) currentLevel.grass_floor_for_mario.yLoc, null);

            // hit box for the grass
            if (drawHitboxes) {
                g2.setColor(Color.RED);
                g2.drawPolygon(currentLevel.grass_floor_for_mario.hitbox);
            }

            // clouds
            for (int i = 0; i < currentLevel.clouds.size(); i++) {
                Cloud cloud = currentLevel.clouds.get(i);
                if (cloud.xLoc < currentLevel.off_screen_location)
                    g2.drawImage(cloud.image, (int) cloud.xLoc, (int) cloud.yLoc, null);
            }

            if(!online) {
                drawForSinglePlayerSetting(g2);
            } else {
                drawForMultiplayerSetting(g2);
            }
        }
    }

    public void setOnlineVariable(boolean online) {
        this.online = online;
    }

    /**
     * Calls repaint which draws the world
     */
    public void redraw() {
        repaint();
    }

    /**
     * Draws the world for a single player
     * @param g2 - Graphics object used to draw objects on the screen
     */
    public void drawForSinglePlayerSetting(Graphics2D g2) {
        // landings
        for (int i = 0; i < currentLevel.landings.size(); i++) {
            InanimateObject landing = currentLevel.landings.get(i);
            if (landing.xLoc + landing.image.getIconWidth() > leftDrawingBoundry && landing.xLoc < rightDrawingBoundry) {
                g2.drawImage(landing.image.getImage(), (int) landing.xLoc,
                        (int) landing.yLoc, null);
                if (drawHitboxes) {
                    g2.setColor(Color.RED);
                    g2.drawPolygon(landing.hitbox);
                    g2.setColor(Color.GREEN);
                    g2.drawPolygon(landing.upper_hitbox);
                }
            }
        }


        // flowers
        for (int i = 0; i < currentLevel.flowers.size(); i++) {
            InanimateObject flower = currentLevel.flowers.get(i);
            if (flower.xLoc > leftDrawingBoundry && flower.xLoc < rightDrawingBoundry) {
                g2.drawImage(flower.image.getImage(), (int) flower.xLoc, (int) flower.yLoc, null);
            }
        }

        // hitboxes
        if (drawHitboxes) {
            g2.setColor(Color.RED);

            // below are the hitboxes for the boundaries that shift the camera (aka move the objects on the screen)

            g2.fillRect(user.left_boundry, 0, 1, 700);
            g2.fillRect(user.right_boundry + user.image.getWidth(),
                    0, 1, 700);
            g2.fillRect((int) user.xLoc - (int) user.fake_xLoc,
                    0, 1, 800);
            g2.drawPolygon(user.hitbox);
            g2.setColor(Color.BLUE);
            g2.drawPolygon(user.lower_hitbox);
            g2.setColor(Color.RED);
        }


        // flagpole
        g2.drawImage(currentLevel.flagpole.image.getImage(), (int) currentLevel.flagpole.xLoc,
                (int) currentLevel.flagpole.yLoc, null);

        // hitbox for the flagpole
        if (drawHitboxes) {
            g2.setColor(Color.RED);
            g2.drawPolygon(currentLevel.flagpole.hitbox);
            g2.setColor(Color.GREEN);
            g2.drawPolygon(currentLevel.flagpole.upper_hitbox);
        }

        // bowser's castle
        g2.drawImage(currentLevel.bowser_castle.image.getImage(), (int) currentLevel.bowser_castle.xLoc,
                (int) currentLevel.bowser_castle.yLoc, null);

        // hitbox for bowser's castle
        if (drawHitboxes) {
            g2.setColor(Color.RED);
            g2.drawPolygon(currentLevel.bowser_castle.hitbox);
            g2.setColor(Color.GREEN);
            g2.drawPolygon(currentLevel.bowser_castle.upper_hitbox);
        }

        // koopas
        g2.drawImage(currentLevel.koopaAI.image.getImage(), (int) currentLevel.koopaAI.xLoc,
                (int) currentLevel.koopaAI.yLoc, null);

        // mobs drawings:
        for (int i = 0; i < currentLevel.mobs.size(); i++) {
            InanimateObject mob = currentLevel.mobs.get(i);
            /*if(i == 1) {
                g2.setColor(Color.RED);
                g2.drawPolygon(mob.hitbox);
            }*/
            if (leftDrawingBoundry <= mob.xLoc && mob.xLoc <= rightDrawingBoundry && !mob.dead)
                g2.drawImage(mob.image.getImage(), (int) mob.xLoc, (int) mob.yLoc, null);
            if (drawHitboxes) {
                g2.setColor(Color.RED);
                g2.drawPolygon(mob.hitbox);
                g2.setColor(Color.GREEN);
                g2.drawPolygon(mob.upper_hitbox);
            }
        }


        // solid item blocks
        for (int i = 0; i < currentLevel.solid_item_blocks.size(); i++) {
            InanimateObject solid_item_block = currentLevel.solid_item_blocks.get(i);
            if (solid_item_block.xLoc > leftDrawingBoundry && solid_item_block.xLoc < rightDrawingBoundry) {
                g2.drawImage(solid_item_block.image.getImage(), (int) solid_item_block.xLoc, (int) solid_item_block.yLoc, null);
                if (drawHitboxes) {
                    g2.setColor(Color.YELLOW);
                    g2.drawPolygon(solid_item_block.hitbox);
                }
            }
        }

        // item blocks
        for (int i = 0; i < currentLevel.item_blocks.size(); i++) {
            InanimateObject item_block = currentLevel.item_blocks.get(i);
            if (item_block.xLoc > leftDrawingBoundry && item_block.xLoc < rightDrawingBoundry) {
                if (!item_block.item.collected) {
                    g2.drawImage(item_block.item.image.getImage(), (int) item_block.item.xLoc,
                            (int) item_block.item.yLoc, null);
                }
                g2.drawImage(item_block.image.getImage(), (int) item_block.xLoc, (int) item_block.yLoc, null);

                // hitboxes for item blocks
                if (drawHitboxes) {
                    g2.drawPolygon(item_block.hitbox);
                    g2.setColor(Color.GREEN);
                    g2.drawPolygon(item_block.item.hitbox);
                }
            }
        }

        // green tubes and fly traps
        for (int i = 0; i < currentLevel.green_tubes.size(); i++) {
            InanimateObject green_tube = currentLevel.green_tubes.get(i);
            if (green_tube.xLoc + green_tube.image.getIconWidth() > leftDrawingBoundry && green_tube.xLoc < rightDrawingBoundry) {

                if (green_tube.hasFlyTrap) {
                    g2.drawImage(green_tube.fly_trap.image.getImage(), (int) green_tube.fly_trap.xLoc, (int) green_tube.fly_trap.yLoc, null);
                    if (drawHitboxes) {
                        g2.setColor(Color.BLUE);
                        g2.drawPolygon(green_tube.fly_trap.hitbox);
                    }
                }
                g2.drawImage(green_tube.image.getImage(), (int) green_tube.xLoc,
                        (int) green_tube.yLoc, null);

                // hitboxes for green tubes
                if (drawHitboxes) {
                    g2.setColor(Color.RED);
                    g2.drawPolygon(green_tube.hitbox);
                    g2.setColor(Color.GREEN);
                    g2.drawPolygon(green_tube.upper_hitbox);
                }
            }
        }


        // Stats:

        // drawing the map on the right hand side of the screen:
        g2.setColor(Color.WHITE);

        // drawing map
        g2.fillRect(map_xLoc, map_yLoc, map_width + 10, 3);

        g2.drawImage(currentLevel.mini_flagpole, map_xLoc + map_width - (currentLevel.mini_flagpole.getWidth() / 2) + 10,
                map_yLoc - (currentLevel.mini_flagpole.getHeight() / 2), null);

        double mario_face_xLoc = user.fake_xLoc / currentLevel.length_of_level * map_width + map_xLoc;

        g2.drawImage(currentLevel.mario_face,
                (int) mario_face_xLoc,
                map_yLoc - (currentLevel.mario_face.getHeight() / 2), null);

        // drawing the stats at the top of the screen
        // coins
        g2.drawImage(currentLevel.mario_coin, stats_xLoc + 370, stats_yLoc, null);
        g2.setColor(Color.WHITE);
        g2.drawString("x " + user.number_of_coins_collected, stats_xLoc + 400, stats_yLoc + 23);
        if (user.extra_item != null) {
            g2.drawImage(user.extra_item.image.getImage(), stats_xLoc + 313, stats_yLoc, null);
        }

        // extra item holder
        g2.drawRect(stats_xLoc + 310, stats_yLoc - 3, 35, 35); // item holder
        g2.drawImage(currentLevel.mario_life_icon, stats_xLoc + 190, stats_yLoc - 2, null);
        g2.drawString("x " + user.number_of_lives, stats_xLoc + 240, stats_yLoc + 23);
        g2.drawString("Time: " + user.time_left_for_level + "", stats_xLoc + 470, stats_yLoc + 23);
        g2.drawString("Score: " + user.score, stats_xLoc, stats_yLoc + 23);

        // mario
        if (user.drawUser) {
            g2.drawImage(user.image, (int) user.xLoc, (int) user.yLoc, null);
        }

        if (user.fire_balls.size() != 0) {
            for (int i = 0; i < user.fire_balls.size(); i++) {
                FireBall fireBall = user.fire_balls.get(i);
                if (fireBall.xLoc <= rightDrawingBoundry && fireBall.xLoc >= leftDrawingBoundry) {
                    fireBall.draw(g2);
                    if (drawHitboxes) {
                        g2.setColor(Color.RED);
                        g2.drawPolygon(fireBall.hitbox);
                    }
                }
            }
        }
    }

    /**
     * Draws the map from a multiplayer standpoint
     * @param g2 - Graphics object used to draw objects on the screen
     */
    public void drawForMultiplayerSetting(Graphics2D g2) {

        // landings
        for (int i = 0; i < currentLevel.landings.size(); i++) {
            InanimateObject landing = currentLevel.landings.get(i);
            if (landing.xLoc + landing.image.getIconWidth() > leftDrawingBoundry && landing.xLoc < rightDrawingBoundry) {
                g2.drawImage(landing.image.getImage(), (int) landing.xLoc,
                        (int) landing.yLoc, null);
                if (drawHitboxes) {
                    g2.setColor(Color.RED);
                    g2.drawPolygon(landing.hitbox);
                    g2.setColor(Color.GREEN);
                    g2.drawPolygon(landing.upper_hitbox);
                }
            }
        }


        // flowers
        for (int i = 0; i < currentLevel.flowers.size(); i++) {
            InanimateObject flower = currentLevel.flowers.get(i);

            int xLoc = (int)(getDrawingXLoc(flower.map_location_xLoc));

            if (xLoc > leftDrawingBoundry && xLoc < rightDrawingBoundry) {
                g2.drawImage(flower.image.getImage(), xLoc, (int) flower.yLoc, null);
            }
        }

        // hitboxes
        if (drawHitboxes) {
            g2.setColor(Color.RED);

            // below are the hitboxes for the boundaries that shift the camera (aka move the objects on the screen)

            g2.fillRect(user.left_boundry, 0, 1, 700);
            g2.fillRect(user.right_boundry + user.image.getWidth(),
                    0, 1, 700);
            g2.fillRect((int) user.xLoc - (int) user.fake_xLoc,
                    0, 1, 800);
            g2.drawPolygon(user.hitbox);
            g2.setColor(Color.BLUE);
            g2.drawPolygon(user.lower_hitbox);
            g2.setColor(Color.RED);
        }

        // flagpole
        g2.drawImage(currentLevel.flagpole.image.getImage(), (int) currentLevel.flagpole.xLoc,
                (int) currentLevel.flagpole.yLoc, null);

        // hitbox for the flagpole
        if (drawHitboxes) {
            g2.setColor(Color.RED);
            g2.drawPolygon(currentLevel.flagpole.hitbox);
            g2.setColor(Color.GREEN);
            g2.drawPolygon(currentLevel.flagpole.upper_hitbox);
        }

        // bowser's castle
        g2.drawImage(currentLevel.bowser_castle.image.getImage(), (int) currentLevel.bowser_castle.xLoc,
                (int) currentLevel.bowser_castle.yLoc, null);

        // hitbox for bowser's castle
        if (drawHitboxes) {
            g2.setColor(Color.RED);
            g2.drawPolygon(currentLevel.bowser_castle.hitbox);
            g2.setColor(Color.GREEN);
            g2.drawPolygon(currentLevel.bowser_castle.upper_hitbox);
        }

        // koopas

        // a null pointer exception occurs here
        try {
            if (!serverKoopaAIS.isEmpty()) {
                for (SendableKoopaAI koopa : serverKoopaAIS) {
                    int xLoc = 0;
                    double difference1 = Math.abs(screen_map_xLoc - koopa.map_location_xLoc);
                    if(screen_map_xLoc > koopa.map_location_xLoc)    {xLoc = (int)(1250 - (difference1) - 50);}
                    else                                             {xLoc = (int)(1250 + (difference1) - 50);}
                    g2.drawImage(koopa.image.getImage(), xLoc, (int) koopa.map_location_yLoc, null);
                }
            }
        } catch(Exception e) {}

        // mobs drawings:
        for (InanimateObject mob : currentLevel.mobs) {
            int xLoc = (int)mob.xLoc;
            //int xLoc = (int)(getDrawingXLoc(mob.map_location_xLoc));
            //System.out.println(xLoc + " " + mob.yLoc);
            if (xLoc > leftDrawingBoundry && xLoc < rightDrawingBoundry && !mob.dead)
                g2.drawImage(mob.image.getImage(), xLoc, (int)mob.yLoc, null);

            if (drawHitboxes) {
                g2.setColor(Color.RED);
                g2.drawPolygon(mob.hitbox);
                g2.setColor(Color.GREEN);
                g2.drawPolygon(mob.upper_hitbox);
            }
        }


        // solid item blocks
        for (int i = 0; i < currentLevel.solid_item_blocks.size(); i++) {
            InanimateObject solid_item_block = currentLevel.solid_item_blocks.get(i);
            if (solid_item_block.xLoc > leftDrawingBoundry && solid_item_block.xLoc < rightDrawingBoundry) {
                g2.drawImage(solid_item_block.image.getImage(), (int) solid_item_block.xLoc, (int) solid_item_block.yLoc, null);
                if (drawHitboxes) {
                    g2.setColor(Color.YELLOW);
                    g2.drawPolygon(solid_item_block.hitbox);
                }
            }
        }

        // item blocks
        for (int i = 0; i < currentLevel.item_blocks.size(); i++) {
            InanimateObject item_block = currentLevel.item_blocks.get(i);
            if (item_block.xLoc > leftDrawingBoundry && item_block.xLoc < rightDrawingBoundry) {
                if (!item_block.item.collected) {
                    g2.drawImage(item_block.item.image.getImage(), (int) item_block.item.xLoc,
                            (int) item_block.item.yLoc, null);
                }
                g2.drawImage(item_block.image.getImage(), (int) item_block.xLoc, (int) item_block.yLoc, null);

                // hitboxes for item blocks
                if (drawHitboxes) {
                    g2.drawPolygon(item_block.hitbox);
                    g2.setColor(Color.GREEN);
                    g2.drawPolygon(item_block.item.hitbox);
                }
            }
        }

        // green tubes and fly traps
        for (int i = 0; i < currentLevel.green_tubes.size(); i++) {
            InanimateObject green_tube = currentLevel.green_tubes.get(i);
            if (green_tube.xLoc + green_tube.image.getIconWidth() > leftDrawingBoundry && green_tube.xLoc < rightDrawingBoundry) {

                if (green_tube.hasFlyTrap) {
                    g2.drawImage(green_tube.fly_trap.image.getImage(), (int) green_tube.fly_trap.xLoc, (int) green_tube.fly_trap.yLoc, null);
                    if (drawHitboxes) {
                        g2.setColor(Color.BLUE);
                        g2.drawPolygon(green_tube.fly_trap.hitbox);
                    }
                }
                g2.drawImage(green_tube.image.getImage(), (int) green_tube.xLoc,
                        (int) green_tube.yLoc, null);

                // hitboxes for green tubes
                if (drawHitboxes) {
                    g2.setColor(Color.RED);
                    g2.drawPolygon(green_tube.hitbox);
                    g2.setColor(Color.GREEN);
                    g2.drawPolygon(green_tube.upper_hitbox);
                }
            }
        }

        // Stats:

        // drawing the map on the right hand side of the screen:
        g2.setColor(Color.WHITE);

        // drawing map
        g2.fillRect(map_xLoc, map_yLoc, map_width + 10, 3);

        g2.drawImage(currentLevel.mini_flagpole, map_xLoc + map_width - (currentLevel.mini_flagpole.getWidth() / 2) + 10,
                map_yLoc - (currentLevel.mini_flagpole.getHeight() / 2), null);

        if(!serverUsers.isEmpty()) {
            for (SendableUser user : serverUsers) {
                try {
                    double mario_face_xLoc = user.map_location_xLoc / currentLevel.length_of_level * map_width + map_xLoc;

                    BufferedImage face = null;

                    try {
                        face = ImageIO.read(new File("images/mario/" + user.mario_type + "/super_mario_face.gif"));
                    } catch (IOException e) {
                        System.out.println("Can't find mario face icon image inside DrawingEngine");
                    }

                    g2.drawImage(face, (int) mario_face_xLoc, map_yLoc - (currentLevel.mario_face.getHeight() / 2), null);
                } catch(Exception e) {System.out.println("Coudn't load the mario icons because they haven't been recieved yet. Ignore me...");}
            }
        }

        // drawing the stats at the top of the screen
        // coins
        g2.drawImage(currentLevel.mario_coin, stats_xLoc + 370, stats_yLoc, null);
        g2.setColor(Color.WHITE);
        g2.drawString("x " + user.number_of_coins_collected, stats_xLoc + 400, stats_yLoc + 23);
        if (user.extra_item != null) {
            g2.drawImage(user.extra_item.image.getImage(), stats_xLoc + 313, stats_yLoc, null);
        }

        // extra item holder
        g2.drawRect(stats_xLoc + 310, stats_yLoc - 3, 35, 35); // item holder
        g2.drawImage(currentLevel.mario_life_icon, stats_xLoc + 190, stats_yLoc - 2, null);
        g2.drawString("x " + user.number_of_lives, stats_xLoc + 240, stats_yLoc + 23);
        g2.drawString("Time: " + user.time_left_for_level + "", stats_xLoc + 470, stats_yLoc + 23);
        g2.drawString("Score: " + user.score, stats_xLoc, stats_yLoc + 23);

        // other users
        for(SendableUser user : serverUsers) {
            try {
                if (user.drawUser && user.player_number != this.user.player_number) { //&& !(user.player_number == this.user.player_number)) {
                    int xLoc = 0;
                    double difference1 = Math.abs(screen_map_xLoc - user.map_location_xLoc);
                    if (screen_map_xLoc > user.map_location_xLoc) {
                        xLoc = (int) (1250 - (difference1) - 50);
                    } else {
                        xLoc = (int) (1250 + (difference1) - 50);
                    }
                    g2.drawImage(user.image.getImage(), xLoc, (int) user.yLoc, null);
                }
            } catch(Exception e) {System.out.println("Another loading error, the client hasn't received the list of users. Ignore me...");}
        }

        // client user
        if(user.drawUser) {
            g2.drawImage(user.image, (int)user.xLoc, (int)user.yLoc, null);
        }

        for(SendableUser user : serverUsers) {
            try {
                if (user.fire_balls.size() != 0) {
                    for (int i = 0; i < user.fire_balls.size(); i++) {
                        FireBall fireBall = user.fire_balls.get(i);

                        // converting from one user's fireballs to another's
                        int xLoc = 0;
                        double difference1 = Math.abs(screen_map_xLoc - fireBall.map_location_xLoc);
                        double difference2 = 600 - user.xLoc;
                        if (screen_map_xLoc > fireBall.map_location_xLoc) {
                            xLoc = (int) (1250 - (difference1 - difference2));
                        } else {
                            xLoc = (int) (1250 + (difference1 - difference2));
                        }

                        if (xLoc <= rightDrawingBoundry && xLoc >= leftDrawingBoundry) {
                            fireBall.draw(g2, xLoc);
                            if (drawHitboxes) {
                                g2.setColor(Color.RED);
                                g2.drawPolygon(fireBall.hitbox);
                            }
                        }
                    }
                }
            } catch(Exception e) {System.out.println("Another loading error, see above. Ignore me...");}
        }

    }

    /**
     * Converts the map location of a object to the drawing location on the screen
     * @param map_xLoc - the map location of the object
     * @return - the drawing location
     */
    public int getDrawingXLoc(int map_xLoc) {
        return this.screen_map_xLoc - (this.screen_map_xLoc - map_xLoc);
    }

    /**
     * Converts the map location of a object to the drawing location on the screen
     * @param map_xLoc - the map location of the object
     * @return - the drawing location
     */
    public double getDrawingXLoc(double map_xLoc) {
        return this.screen_map_xLoc - (this.screen_map_xLoc - map_xLoc);
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }
}

