public interface Mutex {
    void requestCS();

    void releaseCS();

    void handleMsg(Msg m, int src, Msg.Tag tag);

    void broadcastMsg(Msg.Tag tag, int messageInt);

    void sendMsg(int destination, Msg.Tag tag, int messageInt);

    enum Algorithm {
        LAM, RAM
    }
}
