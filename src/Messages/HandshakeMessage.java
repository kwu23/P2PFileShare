package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class HandshakeMessage extends Message{
    private static final String HEADER = "P2PFILESHARINGPROJ";
    private static final String ZEROES = "0000000000";
    private String message;
    public HandshakeMessage(int peerID){
        message = HEADER + ZEROES + peerID;
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
