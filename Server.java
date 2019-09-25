import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Moderates server the amount of threaded servers, allows users to connect to create a lobby, and sends updates
 * @author Blake Wesel
 * @version 2019-04-05
 */

public class Server {
    final int portNumber = 16789;
    final int maxUsers   = 4;

    boolean sendingObjects = false;

    ServerSocket serverSocket;
    ArrayList<ThreadedServer> servers = new ArrayList<>();
    int number_of_connected_users = 0;

    boolean keepAccepting = true; // true if the server is still taking users, false if it's full

    // client attributes. These are going to be updated when the clients sends over the most updated copy. Then
    // the complied list below will be recomplied with the most updated info and sent to all the users.

    // complied list to send to users

    ArrayList<SendableUser>    users       = new ArrayList<>();
    ArrayList<InanimateObject> mobs        = new ArrayList<>();
    ArrayList<SendableKoopaAI> koopaAIS    = new ArrayList<>();

    ArrayList<InanimateObject> itemsCollected     = new ArrayList<>();
    ArrayList<InanimateObject> itemsBlocksPunched = new ArrayList<>();
    ArrayList<InanimateObject> deadFlytraps       = new ArrayList<>();

    ServerSendableObject serverSendableObject = null;

    String list_of_users_text = "List of connected users: ";
    JLabel users_label = null;

    String current_level = "";

    public Server() {
        // Find public IP address
        String systemipaddress = "";
        try
        {
            URL url_name = new URL("http://bot.whatismyipaddress.com");

            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        }
        catch (Exception e)
        {
            systemipaddress = "Cannot Execute Properly";
        }
        System.out.println("Public IP Address: " + systemipaddress);


        int number = (int) (Math.random() * 3) + 1;
        if(number == 1) {current_level = "level1";}
        if(number == 2) {current_level = "level2";}
        if(number == 3) {current_level = "level3";}

        JFrame jFrame = new JFrame();
        JPanel panel = new JPanel();

        panel.add(new JLabel("IP Address: " + systemipaddress + "\n"), BorderLayout.NORTH);

        users_label = new JLabel(list_of_users_text);
        panel.add(users_label, BorderLayout.CENTER);

        jFrame.add(panel);

        jFrame.setVisible(true);
        jFrame.setResizable(false);
        jFrame.setSize(new Dimension(400, 100));
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        start();


    }

    public void start() {
        try {
            serverSocket = new ServerSocket(portNumber);

            while(keepAccepting) {
                if (number_of_connected_users < maxUsers) {
                    Socket socket = serverSocket.accept();
                    number_of_connected_users++;
                    System.out.println("Got a connection");
                    ThreadedServer threadedServer = new ThreadedServer(socket, this, number_of_connected_users, current_level);
                    servers.add(threadedServer);

                    if(number_of_connected_users == 4) {
                        list_of_users_text = list_of_users_text + "Player " + number_of_connected_users;
                        users_label.setText(list_of_users_text);
                        users_label.updateUI();
                    } else {
                        list_of_users_text = list_of_users_text + "Player " + number_of_connected_users + ", ";
                        users_label.setText(list_of_users_text);
                        users_label.updateUI();
                    }

                    threadedServer.start();
                } else {
                    keepAccepting = false;
                }
            }
        } catch (IOException e) {
            System.out.println("IOException occurred");
            e.printStackTrace();
        }

        System.out.println("I'm full");
    }

    /**
     * Recomplies the lists after an update was received from the clients
     */
    public synchronized void recompileLists() {

        if(!sendingObjects) {
            users.clear();
            koopaAIS.clear();
            mobs.clear();

            itemsBlocksPunched.clear();
            itemsCollected.clear();
            deadFlytraps.clear();

            for (ThreadedServer threadedServer : servers) {
                users.add(threadedServer.user);
                koopaAIS.add(threadedServer.koopaAI);
                for (int i = 0; i < threadedServer.mobs.size(); i++) {
                    {mobs.add(threadedServer.mobs.get(i));}
                }
                for (int i = 0; i < threadedServer.itemsBlocksPunched.size(); i++) {
                    itemsBlocksPunched.add(threadedServer.itemsBlocksPunched.get(i));
                }
                for (int i = 0; i < threadedServer.itemsCollected.size(); i++) {
                    itemsCollected.add(threadedServer.itemsCollected.get(i));
                }
                for (int i = 0; i < threadedServer.deadFlytraps.size(); i++) {
                    deadFlytraps.add(threadedServer.deadFlytraps.get(i));
                }
            }

            serverSendableObject = new ServerSendableObject(users, koopaAIS, mobs, itemsBlocksPunched, itemsCollected, deadFlytraps);
            //System.out.println("Obj sent: " + serverSendableObject.users.get(0).map_location_xLoc);

            sendListsToUsers();
        }
    }

    /**
     * Goes through each connected user by accessing the list of threaded servers and sends the ServerSendableObject
     * to each of them
     */
    public synchronized void sendListsToUsers() {
        sendingObjects = true;
        for (ThreadedServer threadedServer : servers) {
            try {
                //System.out.println("User fireballs: " + serverSendableObject.users.get(0).fire_balls.size());
                threadedServer.objectWriter.writeObject(serverSendableObject);
                threadedServer.objectWriter.flush();
                threadedServer.objectWriter.reset(); // forces object writer to send most updated version of objects

            } catch (Exception e) {
                System.out.println("Error when server sending object");
                e.printStackTrace();
            }
        }
        sendingObjects = false;
    }

    /**
     * Sends a message to all the connected clients
     * @param message - The message to send to all the clients
     */
    public void writeToAllServers(String message)
    {
        if(!message.equals(""))
        {
            for (int i = 0; i < servers.size(); i++)
            {
                ThreadedServer ts = servers.get(i);
                ts.printWriter.println(message);
                ts.printWriter.flush();
            }
        }
    }

    public void removeUser(int user_number, ThreadedServer threadedServer) {
        try {
            users.remove(user_number);
            servers.remove(threadedServer);
            threadedServer.interrupt();

            String updated_list = "List of users: ";

            for(int i = 0; i < servers.size(); i++) {
                if(i <= servers.size() - 2) {
                    updated_list = updated_list + "Player " + servers.get(i).user.player_number + ", ";
                } else {
                    updated_list = updated_list + "Player " + servers.get(i).user.player_number;
                }
            }

            users_label.setText(updated_list);

        } catch(Exception e) {
        }
    }

    public static void main(String[] args) {
        new Server();
    }

}
