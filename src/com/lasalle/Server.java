package com.lasalle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean setup = false;

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        setup = true;
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
        clientSocket.close();
        serverSocket.close();
    }

    @Override
    public void run() {
        try {
            System.out.println("I am a server now");
            start(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSetup() {
        return setup;
    }
}
