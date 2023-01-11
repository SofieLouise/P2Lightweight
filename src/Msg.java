import java.io.Serializable;

public class Msg implements Serializable {
    private int src;
    private Tag tag;
    private int value;

    public Msg(int src, Tag tag, int value) {
        this.src = src;
        this.tag = tag;
        this.value = value;
    }

    public Msg(int src, Tag tag) {
        this.src = src;
        this.tag = tag;
    }

    public int getValue() {
        return value;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public void setValue(int value) {
        this.value = value;
    }

    enum Tag {
        OKAY,
        REQUEST,
        RELEASE,
        ACK,
        ID
    }
}
