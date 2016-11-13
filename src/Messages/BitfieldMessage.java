package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class BitfieldMessage extends Message{
    private int value = 5;
    private int length;
    private boolean[] payload;

    public BitfieldMessage(boolean[] bitfield) {
    	this.payload = bitfield;
    	this.length = bitfield.length + 1;
    }
    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public int getValue() {
        return 0;
    }

    public String getPayload() {
        return null;
    }
}
