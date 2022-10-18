package com.lasalle;

import java.util.Map;

public class LamportMutex  {
    private DirectClock v;
    int[] q;
    int N;
    private Map<Integer, Server> serverInstances;
    private Map<Integer, Client> clientInstances;


    public LamportMutex (Map<Integer, Server> serverInstances, Map<Integer, Client> clientInstances, int N, int myId) {
        this.serverInstances = serverInstances;
        this.clientInstances = clientInstances;
        this.N = N;
        v = new DirectClock(N, myId);
        q = new int[N];
        for (int i = 0; i < N; i++) {
            q[i] = Integer.MAX_VALUE;
        }
    }

    public synchronized void requestCS() {
        v.tick();
        q[v.myId] = v.getValue(v.myId);
        broadCastMsg("request" , q[v.myId]);
        while(!okayCS()){
            myWait();
        }
    }


    public synchronized void releaseCS(){
        q[v.myId] =  Integer.MAX_VALUE;
        broadCastMsg("release", v.getValue(v.myId));
    }

    public boolean okayCS (){
        for (int j = 0; j < N; j++) {
            if (isGreater(q[v.myId], v.myId, q[j], j)){
                return false;
            }
            if (isGreater(q[v.myId], v.myId, v.getValue(j), j)) {
                return false;
            }
        }
        return true;
    }

    public boolean isGreater (int entry1, int pid1, int entry2, int pid2){
        if (entry2 == Integer.MAX_VALUE){
            return false;
        }
        return ((entry1 > entry2) || ((entry1 == entry2) && (pid1 >pid2)));
    }

    public synchronized void handleMsg (Msg m, int src, String tag){
        int timeStamp = m.getMessageInt();
        v.receiveAction(src, timeStamp);
        if (tag.equals("request")) {
            q[src] = timeStamp;
            sendMsg(src, "ack", v.getValue(v.myId));
        }else if (tag.equals("release")){
            q[src] = Integer.MAX_VALUE;
        }
        // TODO is this notify correct
        notify();
    }

    private void sendMsg(int src, String tag, int value) {
        clientInstances.get(src).sendMessage(new Msg(v.myId, tag, value));
    }

    private void broadCastMsg(String tag, int value) {
        for(Client clientInstance : clientInstances.values()){
            clientInstance.sendMessage(new Msg(v.myId, tag, value));
        }
    }

    public void myWait(){

    }



}
