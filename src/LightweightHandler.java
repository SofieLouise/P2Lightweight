import java.io.*;
import java.net.Socket;

public class LightweightHandler {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Connection connection;
    private int id;
    private int port;

    public LightweightHandler(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
    }

    public LightweightHandler(Socket socket) {
        this.socket = socket;
    }

    public int fetchId() {
        try {
            if (in == null) {
                in = new ObjectInputStream(socket.getInputStream());
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
                out = new ObjectOutputStream(socket.getOutputStream());
            }
            try {
                System.out.println("Sending message " + msg.getTag() + " to " + id);
                out.writeObject(msg);
                out.flush();
            } catch (NotSerializableException e) {
                System.out.println("Stream is closed. Opening a new one");
                out = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("Sending message " + msg.getTag() + " to " + id);
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForMessage(Mutex mutex) {
        System.out.println("Listening for incoming messages...");
        try {
            if(!socket.isConnected() || socket.isClosed()) {
                System.out.println("Socket is closed");
                return;
            }
            if (in == null) {
                in = new ObjectInputStream(socket.getInputStream());
            }
            Msg msg = (Msg) in.readObject();
            mutex.handleMsg(msg, msg.getSrc(), msg.getTag());
        } catch (EOFException e) {
            System.out.println("Connection closed");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Integer getId() {
        return id;
    }

    public void listenForMessages(Mutex mutex) {
        while (true) {
            if (socket.isConnected() && !socket.isClosed()) {
                listenForMessage(mutex);
            }
        }
    }
}
