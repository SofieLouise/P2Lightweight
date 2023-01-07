import java.io.Serializable;

public class Message implements Serializable {
    private Tag tag;

    public Message(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    enum Tag {
        GO,
        DONE,
        TOKEN
    }

}
