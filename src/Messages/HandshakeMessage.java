package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class HandshakeMessage extends Message{
    private int peerID;
    private String HEADER;
    private String ZEROES;
    private String message;
    public HandshakeMessage(int peerID){
        this.peerID = peerID;
        HEADER = "P2PFILESHARINGPROJ";
        ZEROES = "0000000000";
        message = HEADER + ZEROES + peerID;
    }

    public HandshakeMessage(String receivedMessage){
        HEADER = receivedMessage.substring(0, 18);
        ZEROES = receivedMessage.substring(18, 28);
        peerID = Integer.parseInt(receivedMessage.substring(28));
        message = receivedMessage;
    }

    public int getPeerID() {
        return peerID;
    }

    public String getHEADER() {
        return HEADER;
    }

    public String getZEROES() {
        return ZEROES;
    }

    public int getLength() {
        return -1;
    }
    public int getValue(){
        return -1;
    }
    public String getPayload(){
        return null;
    }
    public String getMessage(){
        return message;
    }
}
