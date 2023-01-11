import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LamportMutex implements Mutex {
    private final DirectClock v;
    int[] queue;
    int N;
    private final ConcurrentHashMap<Integer, LightweightHandler> lightweights;
    private final int myId;

    public LamportMutex(ConcurrentHashMap<Integer, LightweightHandler> lightweights, int N, int myId) {
        this.lightweights = lightweights;
        this.N = N;
        v = new DirectClock(N, myId);
        queue = new int[N];
        for (int i = 0; i < N; i++) {
            queue[i] = Integer.MAX_VALUE;
        }
        this.myId = myId;
    }

    public synchronized void requestCS() {
        v.tick();
        queue[myId] = v.getValue(myId);
        broadcastMsg(Msg.Tag.REQUEST, queue[myId]);
        while (!okayCS()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void releaseCS() {
        queue[myId] = Integer.MAX_VALUE;
        broadcastMsg(Msg.Tag.RELEASE, v.getValue(myId));
    }

    public boolean okayCS() {
        for (int j = 0; j < N; j++) {
            if (isGreater(queue[myId], myId, queue[j], j)) {
                return false;
            }
            if (isGreater(queue[myId], myId, v.getValue(j), j)) {
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

    public synchronized void handleMsg(Msg m, int src, Msg.Tag tag) {
        int timeStamp = m.getValue();
        v.receiveAction(src, timeStamp);
        if (tag.equals(Msg.Tag.REQUEST)) {
            queue[src] = timeStamp;
            sendMsg(src, Msg.Tag.ACK, v.getValue(myId));
        } else if (tag.equals(Msg.Tag.RELEASE)) {
            queue[src] = Integer.MAX_VALUE;
        }
        notify();
    }

    public void sendMsg(int destination, Msg.Tag tag, int value) {
        System.out.println("Sending " + tag + " to " + destination);
        LightweightHandler receiver = lightweights.get(destination);
        receiver.sendMessage(new Msg(myId, tag, value));
    }

    public void broadcastMsg(Msg.Tag tag, int value) {
        for (LightweightHandler lightweight : lightweights.values()) {
            lightweight.sendMessage(new Msg(myId, tag, value));
        }
    }


}
