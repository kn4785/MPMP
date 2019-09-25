import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.util.Scanner;

/**
 * Creates a Koopa entity with machine learning methods
 * @author: Blake Wesel
 * @version: 3/30/19
 */
public class KoopaAI {


    // format (goes in 5's): "mob type", "mob speed", "direction, "location", and "weight value"
    ArrayList<Integer> known_decisions_mob_type      = new ArrayList<>();
    ArrayList<Double>  known_decisions_mob_speed     = new ArrayList<>();
    ArrayList<String>  known_decisions_mob_direction = new ArrayList<>();
    ArrayList<Integer> known_decisions_location      = new ArrayList<>();
    ArrayList<Integer> made_decisions_mob_type       = new ArrayList<>();
    ArrayList<Double>  made_decisions_mob_speed      = new ArrayList<>();
    ArrayList<String>  made_decisions_mob_direction  = new ArrayList<>();
    ArrayList<Integer> made_decisions_location       = new ArrayList<>();

    ArrayList<InanimateObject> mobs_created          = new ArrayList<>();


    ImageIcon image;

    int tick_started_throwing;

    boolean printOutDecisionMaking = false;

    InanimateObject mob;
    SendableKoopaAI sendableKoopaAI;

    double speed = 1.2;

    int left_boundry;
    int right_boundry;

    double xLoc = 800;              // this is where the AI is drawn on the screen
    double yLoc = 150;

    double map_location_xLoc = 800; // this is where the AI is in the level
    double map_location_yLoc = 150;

    int types_of_mobs             = 2;

    double weight_for_goomba      = 0.5;
    double weight_for_koopa       = 0.5;
    double weight_for_high_speed  = 0.5;

    public KoopaAI(int left_boundry, int right_boundry) {
        this.left_boundry = left_boundry;
        this.right_boundry = right_boundry;
        try {
            this.image = new ImageIcon(ImageIO.read(new File("images/mario/flying_koopa_right.png")));
        } catch (IOException e) {
            System.out.println("Can't find the flying koopa image inside KoopaAI class");
        }
        sendableKoopaAI = new SendableKoopaAI(image, map_location_xLoc, map_location_yLoc);
        readMemoryFile();
    }

    public void moveLeft(double mario_xLoc) {
        if (this.xLoc > left_boundry) {
            this.xLoc -= speed;
            this.map_location_xLoc -= speed;
            updateImage(mario_xLoc);
        }
    }

    public void moveRight(double mario_xLoc) {
        if (this.xLoc < right_boundry - this.image.getIconWidth()) {
            this.xLoc += speed;
            this.map_location_xLoc += speed;
            updateImage(mario_xLoc);
        }
    }

    public void updateImage(double mario_xLoc) {
        if (mario_xLoc < this.xLoc) {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/flying_koopa_left.png")));
            } catch (IOException e) {
                System.out.println("Can't find the flying koopa image inside moveRight in KoopaAI class");
            }
        } else {
            try {
                this.image = new ImageIcon(ImageIO.read(new File("images/mario/flying_koopa_right.png")));
            } catch (IOException e) {
                System.out.println("Can't find the flying koopa image inside moveLeft in KoopaAI class");
            }
        }
    }

    public InanimateObject makeMove(int mario_ticks, int mario_fake_xLoc) {
        this.tick_started_throwing = mario_ticks;
        ArrayList<Double> mob_description = generateDecision(mario_fake_xLoc);

        String type = "";
        double speed;
        String direction = "";
        int integer_version_of_mob_type;

        double mob_type = mob_description.get(0);
        integer_version_of_mob_type = (int)(mob_type);
        if(mob_type == 1)      {type = "goomba";}
        else if(mob_type == 2) {type = "green_koopa";}

        speed = mob_description.get(1);
        if(mob_description.get(2) == 1.0) { direction = "left";}
        else                              { direction = "right";}
        this.mob = new InanimateObject(type, speed, this.xLoc, this.yLoc, direction);

        made_decisions_mob_type.add(integer_version_of_mob_type);
        made_decisions_mob_speed.add(speed);
        made_decisions_mob_direction.add(direction);
        made_decisions_location.add(mario_fake_xLoc);

        return this.mob;
    }

    public void readMemoryFile() {
        Scanner scanner = null;
        PrintWriter pw = null;
        try {
            File file = new File("ai_memory.txt");
            scanner = new Scanner(file);
            // read from file
            while (scanner.hasNext()) {
                try {
                    double mob_type = scanner.nextDouble();
                    double speed = scanner.nextDouble();
                    double direction = scanner.nextDouble();
                    double location = scanner.nextDouble();

                    int interger_version_of_mob_type  = (int)(mob_type);
                    int integer_version_of_location   = (int)(location);
                    known_decisions_mob_type.add(interger_version_of_mob_type);
                    known_decisions_mob_speed.add(speed);
                    if(direction == 1.0) {known_decisions_mob_direction.add("left"); } // 1.0 represents left
                    else                 {known_decisions_mob_direction.add("right");} // 2.0 represents right
                    known_decisions_location.add(integer_version_of_location);

                } catch (Exception a) {
                    System.out.println("Couldn't read the file");
                }
            }
        } catch (IOException e) {
            // create a blank file
            try {
                pw = new PrintWriter("ai_memory.txt", "UTF-8");
                pw.close();
            } catch (IOException eo) {
                System.out.println("I messed up creating the ai_memory.txt file");
            }
        }
    }

    public ArrayList<Double> generateDecision(int mario_fake_xLoc) {
        ArrayList<Double> decision = new ArrayList<>();
        // check to see if decision is already been appended to "made_decisions"

        if(made_decisions_location.size() > 0) {
            for (int i = 1; i < made_decisions_location.size(); i++) {
                int location = made_decisions_location.get(i);
                // has already been made
                if (location - mario_fake_xLoc < 10 && location - mario_fake_xLoc > -10) {
                    decision.add((double) made_decisions_mob_type.get(i)); // mob type
                    decision.add(made_decisions_mob_speed.get(i)); // mob speed
                    if (made_decisions_mob_direction.get(i).equals("right")) {
                        decision.add(2.0);  // right
                    } else {
                        decision.add(1.0);
                    }  // left
                    decision.add((double) made_decisions_location.get(i));     // location
                    if(printOutDecisionMaking) System.out.println("LOADED FROM MADE DECISIONS!");
                    return decision;
                }
            }
        }

        if(known_decisions_location.size() > 0) {
            // if the AI has it from its memories aka from the file...

            for (int i = 1; i < known_decisions_location.size() - 1; i++) {
                int location = known_decisions_location.get(i);
                if (location - mario_fake_xLoc < 10 && location - mario_fake_xLoc > -10) {
                    decision.add((double) known_decisions_mob_type.get(i)); // mob type
                    decision.add(known_decisions_mob_speed.get(i)); // mob speed
                    if (known_decisions_mob_direction.get(i).equals("right")) {
                        decision.add(2.0);  // right
                    } else {
                        decision.add(1.0);
                    }  // left
                    decision.add((double) known_decisions_location.get(i));     // location
                    if(printOutDecisionMaking) System.out.println("LOADED FROM FILE!");
                    return decision;
                }
            }
        }
        // generate decision

        //generate mob type
        double random_mob_type = (Math.random() * types_of_mobs);
        double mob_type = 0;  // 1 = goomba, 2 = koopa
        if (random_mob_type < types_of_mobs * weight_for_goomba) {
            mob_type = 1;
        } else if (random_mob_type < types_of_mobs * weight_for_goomba + types_of_mobs * weight_for_koopa) {
            mob_type = 2;
        }

        // mob speed

        double random_mob_speed = Math.random();
        double mob_speed = 0;
        if (random_mob_speed < weight_for_high_speed) {
            mob_speed = 1.3;
        } else {
            mob_speed = .7;
        }

        // mob direction
        int random_direction = (int) (Math.random() * 2);
        double direction = 0;
        if (random_direction == 1) {
            direction = 1;
        } // left
        else {
            direction = 2;
        }

        // location
        double location = mario_fake_xLoc;

        decision.add(mob_type);
        decision.add(mob_speed);
        decision.add(direction);
        decision.add(location);
        if(printOutDecisionMaking) System.out.println("GENERATED MY OWN DECISION!");
        return decision;
    }

    public void learnNewPlacement(InanimateObject mob, double mario_fake_xLoc){

        // type
        if(mob.type.equals("goomba")) {known_decisions_mob_type.add(1);}
        else if(mob.type.equals("green_koopa")) {known_decisions_mob_type.add(2);}
        //speed
        known_decisions_mob_speed.add(mob.standard_speed);
        // direction
        if(mob.direction.equals("left")) {known_decisions_mob_direction.add("left");}
        else                             {known_decisions_mob_direction.add("right");}
        // location
        known_decisions_location.add((int)mario_fake_xLoc);

        // make decision list for the writing method

        // write it to the file

        writeDecisionToFile();

        // adjust the weights

        if(mob.type.equals("goomba"))           {weight_for_goomba += 0.05; weight_for_koopa -= 0.05;}
        else if(mob.type.equals("green_koopa")) {weight_for_goomba -= 0.05; weight_for_koopa += 0.05;}
        //speed
        if(mob.standard_speed < weight_for_high_speed) // the mob had high speed
        {weight_for_high_speed -=  0.05;}

        // there is no weight for direction
    }

    public void writeDecisionToFile() {
        PrintWriter pw = null;
        try {
            File file = new File("ai_memory.txt");
            pw = new PrintWriter(file);
            for(int i = 0; i < known_decisions_location.size(); i++) {
                pw.println((double)known_decisions_mob_type.get(i));
                pw.println(known_decisions_mob_speed    .get(i));
                if(known_decisions_mob_direction.get(i).equals("left")) pw.println(1.0); // left
                else                                                    pw.println(2.0); // right
                pw.println((double)known_decisions_location.get(i));
            }
            pw.close();
        } catch(Exception e) {
            System.out.println("can't print decision to the file");
        }
    }

    public void clearKnownDecisions(){
        known_decisions_location.clear();
        known_decisions_mob_direction.clear();
        known_decisions_mob_speed.clear();
        known_decisions_mob_type.clear();
    }
}
