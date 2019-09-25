import javax.swing.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import java.io.Serializable;


/**
 * The main engine of the game that drives the computations, updates, and makes the correct calls to run the game
 * and to keep the game running
 * @author: Blake Wesel
 * @version: 3/30/19
 */

public class GameEngine extends JFrame implements ActionListener, KeyListener, MouseListener, Serializable {

    static GraphicsDevice getDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];

    DrawingEngine drawingEngine;
    Mario user;
    Timer t;
    Level currentLevel;
    ClientThread clientThread;

    boolean playMusic             = false;
    boolean printable             = false; // for debugging purposes
    boolean fullScreen            = false;
    boolean debugging             = true;

    boolean online                = true;

    int height                    = 720;
    int width                     = 1280;
    int TIMER_DELAY               = 10;              // ~90 ticks per second
    final int rateSpriteChanges   = 10;  // the rate at which the sprites should be switching
    final int width_of_images     = 22;
    final int height_of_images    = 44;
    final int boundryAmount       = 0;
    final String startingLevel    = "level0";

    int ticks;
    
    JPanel jpMenu;
    JPanel jpChat;
    JTextField jtfIP;
    JTextArea jtaChat;
    JTextField jtfEnter;

    public GameEngine() {
        super("Multiplayer Mario");

        this.addKeyListener(this);
        this.addMouseListener(this);
        setLocationByPlatform(true);

        // object initializations

        user          = new Mario(this.playMusic);
        currentLevel  = new Level(startingLevel, user, this.width, this.height, this.playMusic);
        drawingEngine = new DrawingEngine(this.width, this.height, this.t, this.currentLevel, this.user, this.online, this);
        currentLevel.setDrawingEngine(drawingEngine);
        this.t        = new Timer(TIMER_DELAY, this);

        user.getCurrentLevel(currentLevel);

        // mouse listener

        drawingEngine.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {

                int xLoc = e.getX();
                int yLoc = e.getY();
                // System.out.println("xLoc : "+ xLoc +"");
                // System.out.println("yLoc : "+ yLoc +"");

            }

            public void mouseReleased(MouseEvent e){
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

        });

        // GUI setup

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(this.drawingEngine, BorderLayout.CENTER);

        if(fullScreen) {
            getDevice.setFullScreenWindow(this);
            setPreferredSize(new Dimension(width, height));
        } else {
            getDevice.setFullScreenWindow(null);
            setSize(new Dimension(width, height));
        }

        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        setFocusable(true);
    }

    /**
     * Starts the timer which starts the program
     */
    public void start()
    {
        t.setActionCommand("timerFired");
        t.start();

    }

    /**
     * Every time something occurs, its handled by if statements below
     */
    public void actionPerformed (ActionEvent e)
    {
        if(e.getActionCommand().equals("timerFired"))  //timer has fired
        {
            update();
            drawingEngine.redraw();
            ticks++;

            if(debugging) {debug();}
        }
    }

    /**
     * This method can be modified to help debug certain elements of the game
     */
    public void debug() {

    }

    /**
     * Every object is updated and calculations are done every tick given off by the timer object
     */
    public void update() {

        if(drawingEngine.titleScreen) {
            updateTitleScreen();

        } else if(drawingEngine.playing){
            updateGame();
            if(online) {
                clientThread.sendUpdatedObjects();
            }
        } /*else if (drawingEngine.end_screen) {
            updateEndScreen();
        }*/

    }

    /**
     * Updates the title screen
     */
    public void updateTitleScreen() {

    }

    public void updateEndScreen() {
        //updated();
        //drawingEngine.drawEndScreen();
    }

    /**
     * Updates the game
     */
    public void updateGame() {
        currentLevel.update();
    }

    public void keyPressed(KeyEvent e)
    {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            if(fullScreen) {
                getDevice.setFullScreenWindow(null);
                setSize(new Dimension(width, height));
            }
        }
        if(e.getKeyChar() == 'w')
        {

        }
        if(e.getKeyChar() == 'a') // go to the left
        {
            if(drawingEngine.playing) {
                if(!user.dying && !user.ducking) {
                    user.holding_down_a_key = true;
                    user.movingBackward = true;
                    user.direction = "left";
                } else {
                    user.canMoveForward = false;
                    user.canMoveBackward = false;
                }
            }
        }
        if(e.getKeyChar() == 's') // duck
        {
            if(!user.ducking && !user.jumping && !user.type.equals("dead")) {
                user.ducking = true;
                user.movingBackward = false;
                user.movingForward = false;
                user.canMoveBackward = false;
                user.canMoveForward = false;
                user.canJump = false;
                user.updateImage(ticks);
                if(user.type.equals("little")) {user.yLoc += 17;}
                else                           {user.yLoc += 37;}
            }
        }
        if(e.getKeyChar() == 'd') // go to the right
        {
            if(drawingEngine.playing) {
                if(!user.dying && !user.ducking){
                    user.holding_down_d_key = true;
                    user.movingForward = true;
                    user.direction = "right";
                } else {
                    user.canMoveForward = false;
                    user.canMoveBackward = false;
                }
            }
        }
        if(e.getKeyChar() == 'r')
        {

        }
        if(e.getKeyChar() == 'e')
        {
            if(drawingEngine.playing && user.type.equals("fire") && !user.ducking && ticks - user.tickSpawnedFireball > user.fireBallCooldown) {
                user.fire_balls.add(new FireBall(user.xLoc, user.yLoc + 20, user.direction, user.fake_xLoc));
                user.tickSpawnedFireball = ticks;
                user.sound.playMusic("fireball_sound.wav");
            }
        }

        if(e.getKeyCode() == 32) { // spacebar
            if(drawingEngine.playing && !user.dying && user.canJump && !user.doubleJumping && !user.jumping) {
                user.jumping    = true;
                user.in_mid_air = true;
            } else if(user.jumping && !user.doubleJumping) {
                user.doubleJumping = true;
                user.jumping_velocity = 3.2;
            }
        }
    }

    public void keyReleased(KeyEvent e)
    {
        if(e.getKeyChar() == 'w') // go forward/up
        {

        }
        if(e.getKeyChar() == 'a') // go to the left
        {
            if(drawingEngine.playing) {
                user.holding_down_a_key = false;
                user.movingBackward     = false;
                if(!user.dying) user.faceBackward();
            }


        }
        if(e.getKeyChar() == 's') // go backward/down
        {
            if(user.ducking && !user.type.equals("dead")) {
                user.ducking         = false;
                user.canMoveBackward = true;
                user.canMoveForward  = true;
                user.canJump         = true;
                if(user.direction.equals("right")) {user.faceForward();}
                else                               {user.faceBackward();}
                if(user.type.equals("little")) {user.yLoc -= 17;}
                else                           {user.yLoc -= 37;}
            }
        }
        if(e.getKeyChar() == 'd') // go to the right
        {
            if(drawingEngine.playing) {
                user.holding_down_d_key = false;
                user.movingForward      = false;
                if(!user.dying) user.faceForward();

            }


        }
        if(e.getKeyChar() == 'b')
        {
            if(printable) {this.printable = false; System.out.println("Printable: "+this.printable+"");}
            else {this.printable = true; System.out.println("Printable: "+this.printable+"");}
        }

        if(e.getKeyCode() == 32) { // spacebar
            if(drawingEngine.playing) {

            }

        }
    }

    public void keyTyped(KeyEvent e){

    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e){

    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e)  {

    }
}
