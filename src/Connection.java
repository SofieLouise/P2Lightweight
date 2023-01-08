import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connection {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream inHeavy;
    ObjectOutputStream heavyOut;
    private List<LightweightHandler> lightweights = new ArrayList<>();

    private String host = "localhost";
    private boolean connected = false;
    private int id;
    private int port;
    private int heavyPort;
    private int generalPort;

    public Connection(int myID, int myPort, int heavyPort, int generalPort) {
        this.id = myID;
        this.port = myPort;
        this.heavyPort = heavyPort;
        this.generalPort = generalPort;
    }

    void connectToLightWeights(int NUMBER_OF_LIGHTWEIGHTS) {
        int expectedNodes = NUMBER_OF_LIGHTWEIGHTS - id - 1;

        // Not the last one
        if (NUMBER_OF_LIGHTWEIGHTS - id != 1) {
            createLightWeightServer(port);
            acceptLightweightConnections(expectedNodes);
        }

        // Not the first one
        if (id != 0) {
            connectToLightweights();
        }
    }

    public void connectToHeavyweight() {
        do {
            try {
                clientSocket = new Socket("localhost", heavyPort);
                heavyOut = new ObjectOutputStream(clientSocket.getOutputStream());
                inHeavy = new ObjectInputStream(clientSocket.getInputStream());
                connected = true;
                System.out.println("connected to the heavyweight");

            } catch (IOException e) {
                System.out.println("Retrying to connect to Heavyweight");
            }
        } while (!connected);
    }

    public boolean waitHeavyweight() {
        Message input;
        try {
            input = (Message) inHeavy.readObject();
            System.out.println("heavyweight message");
            if (input.getTag().equals(Message.Tag.GO)) {
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createLightWeightServer(int myPort) {
        try {
            serverSocket = new ServerSocket(myPort);
            System.out.println("created server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptLightweightConnections(int EXPECTED_NODES) {
        new Thread(() -> {
            int nodesConnected = 0;
            System.out.println("accepting lightweights");
            try {
                while (nodesConnected < EXPECTED_NODES) {
                    System.out.println("notes connected " + nodesConnected);
                    Socket socket = serverSocket.accept();
                    System.out.println("accepted new connection on server from" + socket.getPort());
                    int id = socket.getPort() - generalPort;
                    LightweightHandler handler = new LightweightHandler(socket, this, id);
                    nodesConnected += 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void connectToLightweights() {
        int tempPort = port;
        for (int i = id; i > 0; i--) {
            tempPort = tempPort - 1;
            System.out.println("Connecting to " + tempPort);
            boolean succeeded = false;
            while (!succeeded) {
                try {
                    Socket socket = new Socket(host, tempPort);
                    System.out.println("Connected to " + tempPort);
                    int id = socket.getPort() - generalPort;
                    LightweightHandler handler = new LightweightHandler(socket, this, id);
                    succeeded = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean areLightweightsSetup(int expectedNumberOfNodes) {
        System.out.println("lightweights size: " + lightweights.size());
        return lightweights.size() - expectedNumberOfNodes == 0;
    }

    public void addLightweight(LightweightHandler lightweightHandler) {
        lightweights.add(lightweightHandler);
    }

    public Map<Integer, LightweightHandler> getLightweightMap() {
        // map of lightweight handler to their id in a map
        Map<Integer, LightweightHandler> lightweightMap = new HashMap<>();
        for (LightweightHandler handler : lightweights) {
            lightweightMap.put(handler.getId(), handler);
        }
        System.out.println("lightweight map size: " + lightweightMap.size());
        return lightweightMap;
    }

    public void notifyHeavyweight() {
        System.out.println("Notifying heavyweight");
        try {
            heavyOut.writeObject(new Message(Message.Tag.DONE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<LightweightHandler> getLightweights() {
        return lightweights;
    }
}