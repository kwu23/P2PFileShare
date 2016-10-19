import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by kevinwu on 10/19/16.
 */
public class peerProcess {
    CommonCfg commonCfg;
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String messageToSendServer;
    String messageFromServer;

    public static void main() throws IOException {
        peerProcess client = new peerProcess();
        client.run();
    }
    void run() throws IOException {
        try{
            commonCfg = new CommonCfg("common.cfg");
            requestSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while(true)
            {
                messageToSendServer = commonCfg.getFileName();
                sendMessage(commonCfg);
                PeerInfoCfg messageFromServer = (PeerInfoCfg) in.readObject();
                System.out.println("Receive message: " + messageFromServer.toString());
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
    void sendMessage(Object msg)
    {
        try{
            out.writeObject(msg);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}

