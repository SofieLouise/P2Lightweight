public class LamportClock {
    int c;

    public LamportClock() {
        this.c = 1;
    }

    public int getValue() {
        return c;
    }

    public void tick() {
        this.c++;
    }

    public void sendAction() {
        tick();
    }

    public void receiveAction(int src, int sentValue) {
        this.c = Math.max(this.c, sentValue) + 1;
    }
}
