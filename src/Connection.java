import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

    public Connection(int myID, int myPort, int heavyPort) {
        this.id = myID;
        this.port = myPort;
        this.heavyPort = heavyPort;
    }

    void connectToLightWeights(int NUMBER_OF_LIGHTWEIGHTS) {
        int expectedNodes = NUMBER_OF_LIGHTWEIGHTS - id;

        if (NUMBER_OF_LIGHTWEIGHTS - id != 0) {
            createLightWeightServer(port);
            acceptLightweightConnections(expectedNodes);
        }

        if (id != 1) {
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

        while (connected) {
            Message input;
            try {
                input = (Message) inHeavy.readObject();
                System.out.println("heavyweight message");
                if (input.getTag().equals(Message.Tag.GO)) {
                    System.out.println("DOING HARD WORK");
                    Thread.sleep(2000);
                    System.out.println("DONE HARD WORK");
                    heavyOut.writeObject(new Message(Message.Tag.DONE));
                }
            } catch (IOException | ClassNotFoundException | InterruptedException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void sendMessage(Msg msg) {
        System.out.println("sending message");
        try {
            heavyOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    LightweightHandler handler = new LightweightHandler(socket, this);
                    nodesConnected += 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void connectToLightweights() {
        int tempPort = port;
        for (int i = id; i > 1; i--) {
            tempPort = tempPort - 1;
            System.out.println("Connecting to " + tempPort);
            boolean succeeded = false;
            while (!succeeded) {
                try {
                    Socket socket = new Socket(host, tempPort);
                    System.out.println("Connected to " + tempPort);
                    LightweightHandler handler = new LightweightHandler(socket, this);
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
}