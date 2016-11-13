package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class PieceMessage {
    private int value = 7;
    private int length;
    private byte[] payload;
    
    public PieceMessage(byte[] payload){
    	this.length = payload.length + 1;
    	this.payload = payload;
    }

	public int getLength() {
		return length;
	}

	public byte[] getPayload() {
		return payload;
	}
}
