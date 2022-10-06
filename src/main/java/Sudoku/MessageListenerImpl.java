package Sudoku;
import javax.print.DocFlavor.STRING;

import Class.Sudoku;
import Interface.MessageListener;

public class MessageListenerImpl implements MessageListener {

    private int peerID;

    public MessageListenerImpl(int peerID){
        this.peerID = peerID;
    }

    public Object parseMessage(Object obj){
        String msg = (String) obj;
        if (msg.equals(END_GAME) && !Sudoku.EXECUTION_TYPE.equals("TEST"))
            System.exit(0);
        else
            System.out.printf("\n\n["+peerID+"] (Direct Message Received) "+ msg +"\n\n");
        return "success";
    }
}