import Messages.HandshakeMessage;
import Messages.Message;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinwu on 10/19/16.
 */
public class peerProcess {
    static CommonCfg commonCfg;
    List<Peer> peers;
    static int peerID;
    List<Connection> connections;
    Peer me;

    public static void main(String[] args) throws IOException {
        commonCfg = new CommonCfg("Common.cfg");
        peerID = Integer.parseInt(args[0]);
        peerProcess client = new peerProcess();
        client.run();
    }
    void run() throws IOException {
        try{
            peers = Utilities.readCfg("PeerInfo.cfg");
            connections = new ArrayList<>();
            for(Peer peer : peers){
                if(peer.getPeerID() == peerID){
                    me = peer;
                    break;
                }
                Socket tempSocket = new Socket(peer.getHostName(), peer.getListeningPort());
                System.out.println("Connected to " + peer.getHostName() + "(" + peer.getPeerID() + ")" + " in port " + peer.getListeningPort());
                new Handler(tempSocket, peers).start();
            }
            Boolean hasSent = false;
            ServerSocket listener = new ServerSocket(me.getListeningPort());
            while(true) {
                new Handler(listener.accept(), peers).start();
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            try{
                for(Connection connection : connections){
                    connection.getIn().close();
                    connection.getOut().close();
                    connection.getSocket().close();
                }
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
    private static class Handler extends Thread {
        private HandshakeMessage message;    //message received from the client
        private Socket connection;
        private ObjectInputStream in;    //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        private List<Peer> peers;

        public Handler(Socket connection, List<Peer> peers) {
            this.connection = connection;
            this.peers = peers;
        }

        public void run() {
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                HandshakeMessage messageToSendServer = new HandshakeMessage(peerID);
                sendMessage(messageToSendServer);
                System.out.println("Message \"" + messageToSendServer.getMessage() + "\" sent");
                Boolean connect = true;
                message = (HandshakeMessage) in.readObject();
                
                //show the message to the user
                System.out.println("Receive message: \"" + message.getMessage() + "\" from client ");
                if(!Utilities.isValidHandshake(message.getMessage(), peers)){
                    connect = false;
                    sendMessage("Disconnecting due to invalid handshake");
                    System.out.println("Disconnect with Client due to invalid handshake");
                }
                try {
                    while (connect) {
                        //receive the message sent from the client
                        message = (HandshakeMessage) in.readObject();
                        //show the message to the user
                        System.out.println("Receive message: \"" + message.getMessage() + "\" from client ");
                    }
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client ");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                //Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client ");
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

    void sendMessage(Connection connection, Object msg)
    {
        try{
            connection.getOut().writeObject(msg);
            connection.getOut().flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

}

