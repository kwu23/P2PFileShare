package Messages;

import java.io.Serializable;

/**
 * Created by kevinwu on 10/19/16.
 */
public abstract class Message implements Serializable{
    int value;
    int length;
    String message;
    String payload;


    public abstract int getLength();
    public abstract int getValue();
    public abstract String getPayload();
    public abstract String getMessage();

}
