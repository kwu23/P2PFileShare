package Messages;

/**
 * Created by kevinwu on 10/19/16.
 */
public abstract class Message {
    private int value;
    private int length;

    public int getLength(){
        return length;
    }
    public int getValue(){
        return value;
    }

}
