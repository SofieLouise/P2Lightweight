import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Connection {
    private ServerSocket serverSocket;
    private Socket clientSocketHeavyweight;
    private ObjectInputStream inHeavy;
    private ObjectOutputStream heavyOut;
    private final ConcurrentHashMap<Integer, LightweightHandler> lightweights = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Thread> lightweightThreads = new ConcurrentHashMap<>();

    private final String host = "localhost";
    private boolean connected = false;
    private final int id;
    private final int port;
    private final int heavyPort;
    private final int generalPort;

    public Connection(int myID, int myPort, int heavyPort, int generalPort) {
        this.id = myID;
        this.port = myPort;
        this.heavyPort = heavyPort;
        this.generalPort = generalPort;
    }

    public void listenForLightweightMessages(Mutex mutex) {
        if(lightweightThreads.isEmpty()){
            for (LightweightHandler lightweight : lightweights.values()) {
                Thread listeningThread = new Thread(() -> {
                    lightweight.listenForMessages(mutex);
                });
                lightweightThreads.put(lightweight.getId(), listeningThread);
                listeningThread.start();
            }
        }
    }

    public void joinExistingLightweightThreads(){
        for (Thread thread :
                lightweightThreads.values()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted during join");
            }
        }
        lightweightThreads = new ConcurrentHashMap<>();
    }

    public void connectToLightWeights(int numberOfLightweights) {
        int expectedNodes = numberOfLightweights - id - 1;

        // Not the last one
        if (numberOfLightweights - id != 1) {
            createLightWeightServer(port);
            acceptLightweightConnections(expectedNodes);
        }

        // Not the first one
        if (id != 0) {
            connectToLightweights();
        }
    }

    public void connectToHeavyweight() {
        while (!connected) {
            try {
                clientSocketHeavyweight = new Socket("localhost", heavyPort);
                heavyOut = new ObjectOutputStream(clientSocketHeavyweight.getOutputStream());
                inHeavy = new ObjectInputStream(clientSocketHeavyweight.getInputStream());
                connected = true;
                System.out.println("connected to the heavyweight");

            } catch (IOException e) {
                System.out.println("Retrying to connect to Heavyweight");
            }
        }
    }

    public boolean waitHeavyweight() {
        Message input;
        try {
            input = (Message) inHeavy.readObject();
            if (input.getTag().equals(Message.Tag.GO)) {
                System.out.println("Message from Heavyweight");
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

    public void acceptLightweightConnections(int expectedNodes) {
        new Thread(() -> {
            int nodesConnected = 0;
            System.out.println("accepting lightweight connections");
            try {
                while (nodesConnected < expectedNodes) {
                    Socket socket = serverSocket.accept();

                    LightweightHandler lightweight = new LightweightHandler(socket);

                    int clientId = lightweight.fetchId();
                    while (clientId == -1) {
                        clientId = lightweight.fetchId();
                        System.out.println("waiting for id from incoming connection");
                    }

                    System.out.println("accepted new connection from pid" + clientId);

                    addLightweight(lightweight);

                    nodesConnected += 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void connectToLightweights() {
        int portTemp = port;
        for (int i = id; i > 0; i--) {
            portTemp = portTemp - 1;
            System.out.println("Connecting to " + portTemp);
            boolean succeeded = false;
            while (!succeeded) {
                try {
                    Socket socket = new Socket(host, portTemp);

                    int socketId = getIdFromPortNumber(socket.getPort());
                    System.out.println("Connected to " + socketId);
                    LightweightHandler lightweight = new LightweightHandler(socket, socketId);

                    lightweight.sendId(id);

                    addLightweight(lightweight);

                    succeeded = true;
                } catch (IOException e) {
                    System.out.println("Retrying to connect to " + portTemp);
                }
            }
        }
    }

    public boolean areLightweightsSetup(int expectedNumberOfNodes) {
        System.out.println("lightweights size: " + lightweights.size());
        return lightweights.size() - expectedNumberOfNodes == 0;
    }

    public synchronized void addLightweight(LightweightHandler lightweightHandler) {
        int pid = lightweightHandler.getId();
        System.out.println("adding lightweight " + pid);
        lightweights.put(pid, lightweightHandler);
    }

    public ConcurrentHashMap<Integer, LightweightHandler> getLightweightMap() {
        return lightweights;
    }

    public void notifyHeavyweight() {
        System.out.println("Notifying heavyweight");
        try {
            heavyOut.writeObject(new Message(Message.Tag.DONE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getIdFromPortNumber(int port) {
        return port - generalPort;
    }
}