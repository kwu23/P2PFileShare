package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class PieceMessage {
    private int value = 7;
    private int length;
    private byte[] payload;
    
    public PieceMessage(byte[] payload){
    	this.setLength(payload.length);
    	this.payload = payload;
    }

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getPayload() {
		return payload;
	}
}
