public class Msg {
    private int token;
    private int src;
    private String tag;
    private int messageInt;

    public Msg(int src, String tag, int messageInt) {
        this.src = src;
        this.tag = tag;
        this.messageInt = messageInt;
    }

    public Msg(int src, int token, String tag) {
        this.token = token;
        this.src = src;
        this.tag = tag;
    }

    public int getMessageInt() {
        return messageInt;
    }
}
