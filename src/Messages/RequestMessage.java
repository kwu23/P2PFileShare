package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class RequestMessage extends Message{
    private int value = 6;
    private int length = 5;
    private int payload;
    
    public RequestMessage(int pieceIndex){
    	this.payload = pieceIndex;
    }

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return length;
	}

	@Override
	public int getValue() {
		// TODO Auto-generated method stub
		return value;
	}
	
	public int getPayload() {
		return payload;
	}

}
