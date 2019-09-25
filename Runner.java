/**
 * Iniciates the launching of the project
 *
 * @author Blake Wesel
 * @version 3/26/19
 */
public class Runner
{
    /**
     * Creates a gameEngine object and starts the program
     */
    public static void main(String args[])
    {
        System.out.println('\f');

        GameEngine gameEngine = new GameEngine();
        gameEngine.start();
    }
}