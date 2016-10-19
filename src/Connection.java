import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by kevinwu on 10/19/16.
 */
public class Connection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Connection(Socket socket, ObjectOutputStream out, ObjectInputStream in){
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    public Socket getSocket() {
        return socket;
    }
    public ObjectOutputStream getOut() {
        return out;
    }
    public ObjectInputStream getIn() {
        return in;
    }
}
