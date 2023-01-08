import java.util.Map;

public class LamportMutex {
    private DirectClock v;
    int[] q; //request queue
    int N;
    private Map<Integer, LightweightHandler> lightweightProcesses;
    private int myId;

    public LamportMutex(Map<Integer, LightweightHandler> lightweightProcesses, int N, int myId) {
        this.lightweightProcesses = lightweightProcesses;
        this.N = N;
        v = new DirectClock(N, myId);
        q = new int[N];
        for (int i = 0; i < N; i++) {
            q[i] = Integer.MAX_VALUE;
        }
        this.myId = myId;
    }

    public synchronized void requestCS() {
        v.tick();
        q[myId] = v.getValue(myId);
        broadCastMsg("request", q[myId]);
        while (!okayCS()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void releaseCS() {
        q[myId] = Integer.MAX_VALUE;
        broadCastMsg("release", v.getValue(myId));
    }

    public boolean okayCS() {
        for (int j = 0; j < N; j++) {
            if (isGreater(q[myId], myId, q[j], j)) {
                return false;
            }
            if (isGreater(q[myId], myId, v.getValue(j), j)) {
                return false;
            }
        }
        return true;
    }

    public boolean isGreater(int entry1, int pid1, int entry2, int pid2) {
        if (entry2 == Integer.MAX_VALUE) {
            return false;
        }
        return ((entry1 > entry2) || ((entry1 == entry2) && (pid1 > pid2)));
    }

    public synchronized void handleMsg(Msg m, int src, String tag) {
        int timeStamp = m.getMessageInt();
        v.receiveAction(src, timeStamp);
        if (tag.equals("request")) {
            q[src] = timeStamp;
            sendMsg(src, "ack", v.getValue(myId));
        } else if (tag.equals("release")) {
            q[src] = Integer.MAX_VALUE;
        }
        notify();
    }

    private void sendMsg(int destination, String tag, int value) {
        System.out.println("Sending " + tag + " to " + destination);
        LightweightHandler receiver = lightweightProcesses.get(destination);
        receiver.sendMessage(new Msg(myId, tag, value));
    }

    private void broadCastMsg(String tag, int value) {
        for (LightweightHandler lightweight : lightweightProcesses.values()) {
            lightweight.sendMessage(new Msg(myId, tag, value));
        }
    }

    public void myWait() {
        System.out.println("Waiting for CS");
    }


}
