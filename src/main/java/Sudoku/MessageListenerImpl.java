package Sudoku;
import Interface.MessageListener;

public class MessageListenerImpl implements MessageListener {

    private int peerID;

    public MessageListenerImpl(int peerID){
        this.peerID = peerID;
    }

    public Object parseMessage(Object obj){
        System.out.printf("\n\n["+peerID+"] (Direct Message Received) "+obj+"\n\n");
        return "success";
    }
}