import java.io.Serializable;

public class Msg implements Serializable {
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

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setMessageInt(int messageInt) {
        this.messageInt = messageInt;
    }
}
