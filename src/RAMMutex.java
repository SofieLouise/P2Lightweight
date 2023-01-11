import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RAMMutex implements Mutex {
    private final int myId;
    private int myTs;
    private final LamportClock c = new LamportClock();
    private final LinkedList<Integer> pendingQ = new LinkedList<>();
    private int numOkay = 0;
    private final ConcurrentHashMap<Integer, LightweightHandler> lightweights;
    private final int N;

    public RAMMutex(ConcurrentHashMap<Integer, LightweightHandler> lightweights, int N, int myId) {
        this.lightweights = lightweights;
        this.N = N;
        this.myId = myId;
        myTs = Integer.MAX_VALUE;
    }

    @Override
    public synchronized void requestCS() {
        c.tick();
        myTs = c.getValue();
        broadcastMsg(Msg.Tag.REQUEST, myTs);
        while (numOkay < N - 1) {
            try {
                System.out.println("Waiting for okay");
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void releaseCS() {
        myTs = Integer.MAX_VALUE;
        while (!pendingQ.isEmpty()) {
            int pid = pendingQ.removeFirst();
            System.out.println("Sending release to " + pid);
            LightweightHandler lightweight = lightweights.get(pid);
            lightweight.sendMessage(new Msg(myId, Msg.Tag.OKAY, c.getValue()));
        }
    }

    @Override
    public synchronized void handleMsg(Msg message, int src, Msg.Tag tag) {
        int timeStamp = message.getValue();
        c.receiveAction(src, timeStamp);
        System.out.println("Received message from " + src + " with tag " + tag + " and timestamp " + timeStamp);

        if (tag.equals(Msg.Tag.REQUEST)) {
            if (myTs == Integer.MAX_VALUE || (timeStamp < myTs) || (timeStamp == myTs && (src < myId))) {
                lightweights.get(src).sendMessage(new Msg(myId, Msg.Tag.OKAY, c.getValue()));
            } else {
                pendingQ.add(src);
                System.out.println("Added " + src + " to pendingQ");
            }
        } else if (tag.equals(Msg.Tag.OKAY)) {
            numOkay++;
            if (numOkay == N - 1) {
                System.out.println("Notifying okay");
                notifyAll();
            }
        } else if (tag.equals(Msg.Tag.RELEASE)) {
            if (pendingQ.contains(src)) {
                pendingQ.remove(src);
            }
        }
    }

    @Override
    public void broadcastMsg(Msg.Tag tag, int value) {
        for (LightweightHandler lightweight : lightweights.values()) {
            lightweight.sendMessage(new Msg(myId, tag, value));
        }
    }

    @Override
    public void sendMsg(int destination, Msg.Tag tag, int value) {
        System.out.println("Sending " + tag + " to " + destination);
        lightweights.get(destination).sendMessage(new Msg(myId, tag, value));
    }
}
