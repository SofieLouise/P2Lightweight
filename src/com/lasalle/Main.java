package com.lasalle;

import java.io.IOException;

public class Main { // myId, serverPort,
    private static int myID;
    public static void main(String[] args) {
        myID = Integer.parseInt(args[0]);
        initializeConnections(myID);

        LamportMutex lamportMutex = new LamportMutex(servers, clients, N, myID);

        while (true) {
            try {
                server.waitHeavyweight();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            requestCS();
            for (int i = 0; i < 10; i++) {
                System.out.println("I am process lightweight" + myID);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            releaseCS();
            try {
                client.notifyHeavyweight();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }

        }

    }

    private static void initializeConnections(int myID) {

    }
}
