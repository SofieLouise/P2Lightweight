import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class    Main { // myId, heavyPort, general port, numberOfLightweights
    private static int myID;
    private static int generalPort;
    private static int heavyPort;
    private static int NUMBER_OF_LIGHTWEIGHTS;
    private static String host = "localhost";
    List<Node> network = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Usage: java Main <myId> <heavyPort> <generalPort> <numberOfLightweights>");
            System.exit(1);
        }

        myID = Integer.parseInt(args[0]);
        heavyPort = Integer.parseInt(args[1]);
        generalPort = Integer.parseInt(args[2]);
        NUMBER_OF_LIGHTWEIGHTS = Integer.parseInt(args[3]);

        int myPort = generalPort + myID;

        Connection connection = new Connection(myID, myPort, heavyPort);

        connection.connectToLightWeights(NUMBER_OF_LIGHTWEIGHTS);

        while (!connection.areLightweightsSetup(NUMBER_OF_LIGHTWEIGHTS - 1)) {
            try {
                System.out.println("Waiting for lightweights to setup");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        connection.connectToHeavyweight();

        // LamportMutex lamportMutex = new LamportMutex(servers, clients, N, myID);

       /* while (true) {
            try {
                server.waitHeavyweight();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            requestCS();
            for (int i = 0; i < 10; i++) {
                System.out.println("I am process lightweight" + myID);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            releaseCS();
            try {
                client.notifyHeavyweight();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }

        }*/


    }

}
