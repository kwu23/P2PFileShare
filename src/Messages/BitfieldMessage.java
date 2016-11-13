package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public class BitfieldMessage extends Message{
    private int value = 5;

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public int getValue() {
        return 0;
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
