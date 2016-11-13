package Messages;

import java.io.Serializable;

/**
 * Created by kevinwu on 10/19/16.
 */
public abstract class Message implements Serializable{
    int value;
    int length;


    public abstract int getLength();
    public abstract int getValue();

}
