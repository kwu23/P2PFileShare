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
		return 0;
	}

	@Override
	public int getValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getPayload() {
		// TODO Auto-generated method stub
		return null;
	}

}
