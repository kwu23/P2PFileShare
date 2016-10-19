package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class InterestedMessage extends Message {
    private int value = 2;
    private int length = 1;


    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getPayload() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
