package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class UnchokeMessage extends Message {
    private int value = 1;
    private int length = 1;

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getValue() {
        return value;
    }

    public String getPayload() {
        return null;
    }
}
