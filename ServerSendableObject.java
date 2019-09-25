import java.io.Serializable;
import java.util.ArrayList;

/**
 * A class that holds the objects that are being sent to the client
 * @author Blake Wesel
 * @version 4/27/19
 */
public class ServerSendableObject implements Serializable {

    final static long serialVersionUID = 20L;

    ArrayList<SendableUser> users     = null;
    ArrayList<SendableKoopaAI> koopas = null;
    ArrayList<InanimateObject> mobs   = new ArrayList<>();

    ArrayList<InanimateObject> itemsCollected     = new ArrayList<>();
    ArrayList<InanimateObject> itemsBlocksPunched = new ArrayList<>();
    ArrayList<InanimateObject> deadFlytraps       = new ArrayList<>();
    int x;

    public ServerSendableObject(ArrayList<SendableUser> users, ArrayList<SendableKoopaAI> koopas, ArrayList<InanimateObject> mobs,
                                ArrayList<InanimateObject> itemsBlocksPunched, ArrayList<InanimateObject> itemsCollected,
                                ArrayList<InanimateObject> deadFlytraps) {
        this.users  = users;
        this.koopas = koopas;
        this.mobs   = mobs;
        this.itemsBlocksPunched = itemsBlocksPunched;
        this.itemsCollected = itemsCollected;
        this.deadFlytraps = deadFlytraps;
    }

    public ServerSendableObject() {
        x = 5;
    }

    public String toString() {
        return "Users: " + users + " Koopas: " + koopas + " Mobs: " + mobs;
    }

}
