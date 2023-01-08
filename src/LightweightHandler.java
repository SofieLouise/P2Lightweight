import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LightweightHandler {
    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Connection connection;
    private int id;
    private int port;

    public LightweightHandler(Socket client, Connection c, int id) {
        this.client = client;
        connection = c;
        c.addLightweight(this);
        this.id = id;
        port=client.getPort();
    }

    public void sendMessage(Msg msg) {
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getId() {
        return id;
    }

    public void listenForMessages(LamportMutex lamportMutex) {
        try {
            in = new ObjectInputStream(client.getInputStream());
            Msg msg = (Msg) in.readObject();
            System.out.println("New Message: " + msg.getTag() + " from " + msg.getSrc());
            lamportMutex.handleMsg(msg, msg.getSrc(), msg.getTag());
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }
}
