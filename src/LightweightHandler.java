import java.io.*;
import java.net.Socket;

public class LightweightHandler {
    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Connection connection;
    private int id;
    private int port;

    public LightweightHandler(Socket client, int id) {
        this.client = client;
        this.id = id;
    }

    public LightweightHandler(Socket client) {
        this.client = client;
    }

    public int fetchId() {
        try {
            if (in == null) {
                in = new ObjectInputStream(client.getInputStream());
            }
            Msg msg = (Msg) in.readObject();
            while (!msg.getTag().equals(Msg.Tag.ID)) {
                msg = (Msg) in.readObject();
            }
            this.id = msg.getSrc();
            System.out.println("Received id from " + id);
            return id;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void sendId(int myId) {
        sendMessage(new Msg(myId, Msg.Tag.ID));
    }

    public void sendMessage(Msg msg) {
        try {
            if (out == null) {
                out = new ObjectOutputStream(client.getOutputStream());
            }
            try {
                out.writeObject(msg);
                out.flush();
            } catch (NotSerializableException e) { // if stream is closed
                out = new ObjectOutputStream(client.getOutputStream());
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForMessage(Mutex mutex) {
        try {
            if (in == null) {
                in = new ObjectInputStream(client.getInputStream());
            }
            Msg msg = (Msg) in.readObject();
            mutex.handleMsg(msg, msg.getSrc(), msg.getTag());
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    public Integer getId() {
        return id;
    }

    public void listenForMessages(Mutex mutex) {
        while (true) {
            if (client.isConnected() && !client.isClosed()) {
                listenForMessage(mutex);
            }
        }
    }
}
