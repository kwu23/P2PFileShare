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
    CommonCfg commonCfg;
    List<Peer> peers;
    static int peerID;
    List<Connection> connections;
    Peer me;

    public static void main(String[] args) throws IOException {
        peerID = Integer.parseInt(args[0]);
        peerProcess client = new peerProcess();
        client.run();
    }
    void run() throws IOException {
        try{
            //commonCfg = new CommonCfg("Common.cfg");
            peers = Utilities.readCfg("PeerInfo.cfg");
            connections = new ArrayList<>();
            for(Peer peer : peers){
                if(peer.getPeerID() == peerID){
                    me = peer;
                    break;
                }
                Socket tempSocket = new Socket(peer.getHostName(), peer.getListeningPort());
                System.out.println("Connected to " + peer.getHostName() + "(" + peer.getPeerID() + ")" + " in port " + peer.getListeningPort());
                new Handler(tempSocket).start();
            }
            Boolean hasSent = false;
            ServerSocket listener = new ServerSocket(me.getListeningPort());
            while(true) {
                new Handler(listener.accept()).start();
                /*
                Socket tempSocket = listener.accept();
                if(tempSocket != null){
                    connections.add(new Connection(tempSocket, new ObjectOutputStream(tempSocket.getOutputStream()), new ObjectInputStream(tempSocket.getInputStream())));
                }

                if(!hasSent && !connections.isEmpty()){
                    String messageToSendServer = "Hey from " + peerID;
                    for(Connection connection : connections){
                        sendMessage(connection, messageToSendServer);
                    }

                    System.out.println("Message \"" + messageToSendServer + "\" sent");
                    hasSent = true;
                }
                for(Connection connection : connections){
                    String messageFromServer = (String) connection.getIn().readObject();
                    System.out.println("Receive message: " + messageFromServer);
                }
                */
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        /*catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }*/
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
        private Message message;    //message received from the client
        private Socket connection;
        private ObjectInputStream in;    //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket

        public Handler(Socket connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                Boolean hasSent = false;
                try {
                    while (true) {
                        if(!hasSent){
                            HandshakeMessage messageToSendServer = new HandshakeMessage(peerID);
                            sendMessage(messageToSendServer);

                            System.out.println("Message \"" + messageToSendServer.getMessage() + "\" sent");
                            hasSent = true;
                        }
                        //receive the message sent from the client
                        message = (Message) in.readObject();
                        //show the message to the user
                        System.out.println("Receive message: " + message.getMessage() + " from client ");
                    }
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client ");
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

