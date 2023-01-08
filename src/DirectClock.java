public class DirectClock {
    public int[] clock;
    public int myId;

    public DirectClock(int numProc, int myId) {
        this.myId = myId;
        this.clock = new int[numProc];

        for (int i = 0; i < numProc; i++) {
            clock[i] = 0;
        }
        clock[myId] = 1;
    }

    public int getValue(int i) {
        return clock[i];
    }

    public void tick() {
        clock[myId]++;
    }

    // TODO: Not used?
    public void sendAction() {
        // sentValue = clock[myId];
        tick();
    }

    public void receiveAction(int sender, int sentValue) {
        clock[sender] = Math.max(clock[sender], sentValue);
        clock[myId] = Math.max(clock[myId], sentValue) + 1;
    }
}