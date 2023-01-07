import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream inHeavy;
    ObjectOutputStream outHeavy;

    private final String host;
    private boolean connected = false;
    private int myId;
    private int port;

    public Client(String host, int myId, int port) {
        this.host = host;
        this.myId = myId;
        this.port = port;
    }

    // keep trying until succesfull (main)
    public void connectHeavyWeight(int port) {

    }

    //keep doing until all connections succesfull (main)
    public void startConnectionsLight(String ip, int port) throws IOException {
        for (int i = 1; i < 4; i++) {
            if (i != myId) {
                int otherPort = port + i;
                // clientSockets.add(new Socket("localhost", port));

            }
        }
    }

    public void notifyHeavyweight() throws IOException {
        System.out.println("notifying heavyweight");
        Msg message = new Msg(myId, 1, "token");
        outHeavy.writeObject(message);
    }

    public void sendMessage(Msg msg) {
        System.out.println("sending message");
        try {
            outHeavy.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() throws IOException {
        inHeavy.close();
        outHeavy.close();
        // clientSockets.get(0).close();
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        do {
            try {
                clientSocket = new Socket("localhost", port);
                outHeavy = new ObjectOutputStream(clientSocket.getOutputStream());
                inHeavy = new ObjectInputStream(clientSocket.getInputStream());
                connected = true;
                System.out.println("connected HA");

            } catch (IOException e) {
                System.out.println("Retrying");
                //e.printStackTrace();
            }
        } while (!connected);

        while (connected) {
            Message input;
            try {
                input = (Message) inHeavy.readObject();
                System.out.println("heavyweight mesagge");
                if (input.getTag().equals(Message.Tag.GO)) {
                    outHeavy.writeObject(new Message(Message.Tag.DONE));
                }
            } catch (IOException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }
        }

    /*    try {
            System.out.println("I am a client now");
            connectHeavyWeight(port);
            startConnectionsLight(host, port);

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


}