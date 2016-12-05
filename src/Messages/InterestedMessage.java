package Messages;

import java.io.Serializable;

/**
 * Created by kevinwu on 10/19/16.
 */
public class InterestedMessage extends Message  implements Serializable {
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

    public String getPayload() {
        return null;
    }
}
