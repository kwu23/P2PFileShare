package Messages;

import java.io.Serializable;

/**
 * Created by kevinwu on 10/19/16.
 */
public class PieceMessage extends Message  implements Serializable {
    private int value = 7;
    private int length;
	private int index;
    private byte[] payload;
    
    public PieceMessage(byte[] payload, int num){
    	this.length = payload.length + 5;
    	this.payload = payload;
    	this.index = num;
    }

    public int getValue(){
    	return value;
	}

	public int getLength() {
		return length;
	}

	public int getIndex(){
		return this.index;
	}

	public byte[] getPayload() {
		return payload;
	}
}
