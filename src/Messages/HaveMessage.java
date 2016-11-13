package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class HaveMessage extends Message {
    private int value = 4;
    private int length = 5;
    private int payload;
    
    public HaveMessage(int pieceIndex){
    	this.payload = pieceIndex;
    }
    
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
    
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}

	public int getPayload() {
		return payload;
	}

}
