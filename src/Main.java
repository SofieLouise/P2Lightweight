public class Main { // myId, heavyPort, general port, numberOfLightweights algorithm(LAM/RAM)

    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: java Main <myId> <heavyPort> <generalPort> <numberOfLightweights> <algorithm>");
            System.exit(1);
        }

        int myId = Integer.parseInt(args[0]);
        int heavyPort = Integer.parseInt(args[1]);
        int generalPort = Integer.parseInt(args[2]);
        int numberOfLightweights = Integer.parseInt(args[3]);
        Mutex.Algorithm algorithm = Mutex.Algorithm.valueOf(args[4]);

        int myPort = generalPort + myId;

        Connection connection = new Connection(myId, myPort, heavyPort, generalPort);

        System.out.println("I am " + myId + " and my port is " + myPort);

        connection.connectToLightWeights(numberOfLightweights);

        while (!connection.areLightweightsSetup(numberOfLightweights - 1)) {
            try {
                System.out.println("Waiting for lightweights to setup");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        connection.connectToHeavyweight();

        Mutex mutex = getMutex(myId, numberOfLightweights, algorithm, connection);

        while (true) {
            while (!connection.waitHeavyweight()) {
                try {
                    System.out.println("Waiting for heavyweight...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (LightweightHandler lightweight : connection.getLightweightMap().values()) {
                new Thread(() -> {
                    lightweight.listenForMessages(mutex);
                }).start();
            }

            mutex.requestCS();
            for (int i = 0; i < 10; i++) {
                System.out.println(("I am the process lightweight " + myId + " and I am in the critical section"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mutex.releaseCS();

            connection.notifyHeavyweight();
        }
    }

    private static Mutex getMutex(int myId, int numberOfLightweights, Mutex.Algorithm algorithm, Connection connection) {
        Mutex mutex;
        if (algorithm.equals(Mutex.Algorithm.LAM)) {
            mutex = new LamportMutex(connection.getLightweightMap(), numberOfLightweights, myId);
        } else if (algorithm.equals(Mutex.Algorithm.RAM)) {
            mutex = new RAMMutex(connection.getLightweightMap(), numberOfLightweights, myId);
        } else {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
        return mutex;
    }
}
