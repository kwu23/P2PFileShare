package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class PieceMessage extends Message{
    private int value = 7;
    private int length;
	private int index;
    private byte[] payload;
    
    public PieceMessage(byte[] payload, int index){
    	this.length = payload.length + 5;
    	this.payload = payload;
    }

    public int getValue(){
    	return value;
	}

	public int getLength() {
		return length;
	}

	public int getIndex(){
		return index;
	}

	public byte[] getPayload() {
		return payload;
	}
}
