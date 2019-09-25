import java.io.Serializable;
import java.util.ArrayList;

public class SendableObject implements Serializable {

    final static long serialVersionUID            = 12L;

    SendableUser user                             = null;
    SendableKoopaAI koopa                         = null;
    ArrayList<InanimateObject> mobs               = new ArrayList<>();
    ArrayList<InanimateObject> itemsCollected     = new ArrayList<>();
    ArrayList<InanimateObject> itemsBlocksPunched = new ArrayList<>();
    ArrayList<InanimateObject> deadFlytraps       = new ArrayList<>();

    int x;

    public SendableObject(SendableUser user, SendableKoopaAI koopa, ArrayList<InanimateObject> mobs,
                          ArrayList<InanimateObject> itemsCollected, ArrayList<InanimateObject> itemsBlocksPunched,
                          ArrayList<InanimateObject> deadFlytraps) {

        this.user               = user;
        this.koopa              = koopa;

        this.mobs               .addAll(mobs);
        this.itemsCollected     .addAll(itemsCollected);
        this.itemsBlocksPunched .addAll(itemsBlocksPunched);
        this.deadFlytraps       .addAll(deadFlytraps);
    }

    public SendableObject() {
        x = 5;
    } // sets an attribute so the resulting object isn't considered null

    public String toString() {
        return "User: " + user + " Koopa: " + koopa + " Mobs: " + mobs;
    }

}
