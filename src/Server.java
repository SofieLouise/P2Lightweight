import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{
    private ServerSocket serverSocket;
    private ArrayList<Socket> clientSockets;
    private PrintWriter out;
    private BufferedReader in;
    private boolean setup = false;
    private static volatile boolean token;
    private final int id;

    public Server(int id, int port) {
        //A
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        this.id = id;
    }


    public void start() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            clientSockets.add((socket));
            new Thread(() -> {
                try {
                    handleRequest(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

       // setup = true;
    }

    public void handleRequest(Socket socket) throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String input;
        while ( (input = in.readLine()) != null){

             out.println(input.toUpperCase());
        }

        /* String line = in.readLine();
        String[] tokens = line.split(" ");
        int senderId = Integer.parseInt(tokens[0]);

        long timestamp = Long.parseLong(tokens[1]);
        if (tokens[2].equals("request")) {
            handleRequestMessage(senderId, timestamp);
        } else if (tokens[2].equals("release")) {
            handleReleaseMessage(senderId, timestamp);
        }*/

    }

    public int waitHeavyweight() throws IOException {
        System.out.println("Listening to heavyweight");
        int token = Integer.parseInt(in.readLine());
        System.out.println("Heavyweight token received: " + token);
        return token;
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        //clientSocket.close();
        serverSocket.close();
    }

    @Override
    public void run() {
        try {
            System.out.println("I am a server now");
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSetup() {
        return setup;
    }
}
