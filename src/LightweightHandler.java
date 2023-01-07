import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LightweightHandler {
    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Connection connection;

    public LightweightHandler(Socket client, Connection c) {
        this.client = client;
        connection = c;
        c.addLightweight(this);
        /*try {
            in = new ObjectInputStream(client.getInputStream());
            out = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }*/
    }
}
