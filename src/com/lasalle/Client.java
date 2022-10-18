package com.lasalle;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class Client implements Runnable{
    private Socket clientSocket;
    private BufferedReader in;
    ObjectOutputStream objectOutputStream;

    private final String host;
    private final int port;
    private boolean connected = false;
    private int myId;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startConnection(String ip, int port) throws IOException {
        while(!connected){
            try {
                clientSocket = new Socket(ip, port);
                connected = true;
                System.out.println("Connected");
            } catch (ConnectException ignored) {
                System.out.println("Retrying...");
            }
        }
        objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void notifyHeavyweight() throws IOException {
        System.out.println("notifying heavyweight");
        Msg message = new Msg(myId, 1, "token");
        objectOutputStream.writeObject(message);
    }

    public void sendMessage(Msg msg) {
        System.out.println("sending message");
        try {
            objectOutputStream.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() throws IOException {
        in.close();
        objectOutputStream.close();
        clientSocket.close();
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        try {
            System.out.println("I am a client now");
            startConnection(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}