import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Talks to the server, sending objects and receiving information from the server sent by other client. This class
 * updates variables received from the server.
 */

public class ClientThread extends Thread {

    String ip      = "129.21.94.212";
    final int portNumber = 16789;

    int count = 0; // every 5 objects received, there's an update that ensues. This prevents an update every time
                   // an object is received

    Socket socket;
    DrawingEngine drawingEngine;
    Level currentLevel;

    ObjectOutputStream objectWriter;
    ObjectInputStream  objectReader;
    PrintWriter printWriter;
    BufferedReader bufferedReader;

    public ClientThread(DrawingEngine drawingEngine, Level currentLevel, String ip) {

        this.drawingEngine = drawingEngine;
        this.currentLevel  = currentLevel;
        this.ip = ip;

        try {
            socket = new Socket(ip, portNumber);
        } catch(UnknownHostException uh) {
            System.out.println("Can't resolve host");
        } catch(IOException io) {
            System.out.println("IO exception occurred in ClientThread constructor");
            io.printStackTrace();
        }

        // initializing the writers and readers
        try {
            objectWriter   = new ObjectOutputStream(socket.getOutputStream());
            objectReader   = new ObjectInputStream(socket.getInputStream());
            printWriter    = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(Exception e) {
            System.out.println("Can't initialize the reading and writing objects inside ClientThread constructor");
        }
    }

    /**
     * Thread is always listening for updates sent by the server to the client. The thread interprets them and updates
     * various variables
     */
    public void run() {
        Object receivedObject = null;
        while(true) {
            try {
                receivedObject = objectReader.readObject();

                if(receivedObject instanceof ServerSendableObject) {
                    getContentsFromObject((ServerSendableObject)receivedObject);
                }
                else if(receivedObject instanceof String) {
                    // if the received object is the user number
                    try {
                        int player_number = Integer.parseInt((String) receivedObject);
                        currentLevel.user.player_number = player_number;

                        if (player_number == 1) {
                            currentLevel.user.mario_type = "Red";
                        }
                        if (player_number == 2) {
                            currentLevel.user.mario_type = "Blue";
                        }
                        if (player_number == 3) {
                            currentLevel.user.mario_type = "Yellow";
                        }
                        if (player_number == 4) {
                            currentLevel.user.mario_type = "Green";
                        }

                        currentLevel.user.faceForward();
                    } catch (Exception e) {
                        // if the received string is the current level
                        currentLevel.level = (String)receivedObject;
                        currentLevel.item_blocks.clear();
                        currentLevel.green_tubes.clear();
                        currentLevel.solid_item_blocks.clear();
                        currentLevel.landings.clear();
                        currentLevel.loadLevelData();

                    }
                }

            } catch(EOFException eof) {
                // this just signals that the readObject function has reached the end of reading

            } catch(Exception e) {
                System.out.println("Error reading object");
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the list of objects that the client has to the server
     */
    public void sendUpdatedObjects() {
        try {

            if(currentLevel.sendableObject != null) {

                currentLevel.sendingObject = true;

                objectWriter.writeObject(currentLevel.sendableObject);
                objectWriter.flush();
                objectWriter.reset();

                currentLevel.sendingObject = false;
            } else {
                System.out.println("Not sending because the object is null");
            }

        } catch(Exception e) {
            System.out.println("Couldn't send objects over to the server");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Updates the hitboxes specific for the clients for mobs, green tubes, and item blocks. Users and KoopaAi's don't
     * need to have their hitbox updated because there is no collision between the user and those objects
     */
    public void updateHitboxes() {

        for(InanimateObject mob : drawingEngine.serverMobs) {
            mob.xLoc = drawingEngine.getDrawingXLoc(mob.xLoc);
            mob.updateHitbox();
        }

        /*
        for(InanimateObject green_tube : drawingEngine.serverGreenTubes) {
            green_tube.updateHitbox((int)(drawingEngine.screen_map_xLoc - green_tube.map_location_xLoc));
        }
        for(InanimateObject item_block : drawingEngine.serverItemBlocks) {
            item_block.updateHitbox((int)(drawingEngine.screen_map_xLoc - item_block.map_location_xLoc));
        }*/
    }

    /**
     * Updates the total list of objects seen by every client. This is updating the total list of mobs, total list of users,
     * and the total list of KoopaAI's. These list are only meant to be drawn on the screen because the calculations/updates
     * are calculated individual on each client. The lists are complied list of all the objects shared between the clients
     */
    public void getContentsFromObject(ServerSendableObject object) {
        drawingEngine.serverMobs       = object.mobs;  // use this list because this list is used for collision detection and updating
        drawingEngine.serverKoopaAIS   = object.koopas;
        drawingEngine.serverUsers      = object.users;

        changeItemBlocksPunched(object.itemsBlocksPunched);
        //changeItemsCollected(object.itemsCollected);
        changeDeadFlytraps(object.deadFlytraps);

        //drawingEngine.serverGreenTubes = object.green_tubes;
        //drawingEngine.serverItemBlocks = object.item_blocks;

        updateHitboxes();
    }

    public void changeItemBlocksPunched(ArrayList<InanimateObject> itemBlocksPunched) {
        if(itemBlocksPunched.size() != 0) {
            InanimateObject collided_item_box = null;

            for(InanimateObject recievedBlock : itemBlocksPunched) {

                for (InanimateObject item_block : currentLevel.item_blocks) {
                    if (recievedBlock.location_in_list == item_block.location_in_list) {
                        collided_item_box = item_block;
                        break;
                    }
                }

                // convert item block to solid item block and show item
                collided_item_box.show_item = true;
                collided_item_box.item.can_be_collected = true;
                collided_item_box.turnIntoSolidBlock();
                if (!collided_item_box.item.type.equals("coin")) {
                    collided_item_box.item.xLoc = collided_item_box.xLoc;
                    collided_item_box.item.yLoc = collided_item_box.yLoc - 10;
                } else {
                    collided_item_box.item.xLoc = collided_item_box.xLoc + 6;
                    collided_item_box.item.yLoc = collided_item_box.yLoc - 10;
                }
                collided_item_box.item.updateHitbox();
            }
        }
        itemBlocksPunched.clear();
    }

    public void changeItemsCollected(ArrayList<InanimateObject> itemsCollected) {
        // the found item's item is null
        if(itemsCollected.size() != 0) {
            InanimateObject item_block_parent = null;
            for (InanimateObject item_block : itemsCollected) {
                for (InanimateObject block : currentLevel.item_blocks) {
                    System.out.println("Comparing: " + block.location_in_list + " and " + item_block.location_in_list);
                    if (block.location_in_list == item_block.location_in_list) {
                        item_block_parent = item_block;
                        System.out.println(item_block_parent.item);
                        break;
                    }
                }
                if (item_block_parent != null) {
                    // get rid of the item inside that item block
                    item_block_parent.item.collected = true;
                } else {
                    System.out.println("Couldn't find corresponding item inside changeItemsCollected");
                }
            }
            itemsCollected.clear();
        }
    }

    public void changeDeadFlytraps(ArrayList<InanimateObject> deadFlytraps) {
        if(deadFlytraps.size() != 0) {
            InanimateObject foundTube = null;
            for (InanimateObject green_tube : deadFlytraps) {
                for (InanimateObject tube : currentLevel.green_tubes) {
                    if (tube.location_in_list == green_tube.location_in_list) {
                        foundTube = tube;
                        break;
                    }
                }
                foundTube.hasFlyTrap = false;
                foundTube.fly_trap = null;
            }
            deadFlytraps.clear();
        }
    }

}
