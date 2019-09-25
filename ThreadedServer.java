import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;

/**
 * This is the server that's going to talk to the clients individual and listen for updates to come in
 * @author Blake Wesel
 * @version 2019-04-05
 */

public class ThreadedServer extends Thread {

    int user_number;
    boolean user_is_connected = true;

    Socket socket;
    Server server;
    ObjectInputStream objectReader;
    ObjectOutputStream objectWriter;
    PrintWriter printWriter;
    BufferedReader bufferedReader;

    SendableObject sendableObject = null;

    String current_level = "";

    SendableUser user;
    SendableKoopaAI koopaAI;
    ArrayList<InanimateObject> mobs = new ArrayList<>();

    ArrayList<InanimateObject> itemsCollected     = new ArrayList<>();
    ArrayList<InanimateObject> itemsBlocksPunched = new ArrayList<>();
    ArrayList<InanimateObject> deadFlytraps       = new ArrayList<>();

    public ThreadedServer(Socket socket, Server server, int user_number, String current_level) {
        this.socket = socket;
        this.server = server;
        this.user_number = user_number;

        this.current_level = current_level;

        // initializing the writers and readers
        try {
            objectReader   = new ObjectInputStream(socket.getInputStream());
            objectWriter   = new ObjectOutputStream(socket.getOutputStream());
            printWriter    = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            objectWriter.reset();
            objectWriter.writeObject(new String("" + user_number));
            objectWriter.flush();

            objectWriter.reset();
            objectWriter.writeObject(new String(current_level));
            objectWriter.flush();


        } catch(Exception e) {
            System.out.println("Can't initialize the reading and writing objects");
        }
    }

    /**
     * Listens for an update sent by the user. It processes the received object, updates the corresponding variables,
     * and tells the main server to recompile the lists with the new updated objects
     */
    public void run() {
        Object receivedObject = null;

        while(user_is_connected) {
            try {
                receivedObject = objectReader.readObject();
                //System.out.println("Object received: " + receivedObject);

                if(receivedObject instanceof SendableObject) {
                    if(receivedObject != null) {
                        sendableObject = (SendableObject)receivedObject;
                        //System.out.println("Obj received: " + sendableObject.user.map_location_xLoc);

                        user    = null;
                        koopaAI = null;
                        mobs    = null;

                        user    = sendableObject.user;
                        koopaAI = sendableObject.koopa;
                        mobs    = sendableObject.mobs;

                        itemsCollected     = sendableObject.itemsCollected;
                        itemsBlocksPunched = sendableObject.itemsBlocksPunched;
                        deadFlytraps       = sendableObject.deadFlytraps;

                    } else {
                        System.out.println("Received object was null");
                    }
                 } else if(receivedObject instanceof ArrayList<?>) {
                    ArrayList<FireBall> fire_balls = (ArrayList<FireBall>) receivedObject;
                    user.fire_balls = fire_balls;
                }

                server.recompileLists();
                server.sendListsToUsers();

            } catch(EOFException | java.net.SocketException a) {
                if(!this.socket.isClosed()) {
                    try {
                        System.out.println("Closing socket");
                        socket.close();
                        server.removeUser(user_number, this);
                        this.user_is_connected = false;
                    } catch(IOException io) {}
                }

            } catch(Exception e) {

                e.printStackTrace();
            }
        }
    }

}
