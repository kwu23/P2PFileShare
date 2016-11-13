import Messages.*;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kevinwu on 10/19/16.
 */
public class peerProcess {
    static CommonCfg commonCfg;
    List<Peer> peers;
    static int peerID;
    List<Connection> connections;
    Peer me;
    static byte[][] fileData;
    static boolean[] ourBitfield;
    static long unchokeInterval;
    static List<Neighbor> amountReceived = new ArrayList<>();
    static ArrayList<Socket> threads = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        commonCfg = new CommonCfg("Common.cfg");
        peerProcess.unchokeInterval = CommonCfg.getUnchokingInterval() * 1000000000;
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
                    System.out.println("DO I HAVE FILE? " + peer.hasFile());
                    peerProcess.fileData = Utilities.getBytesOfFile(CommonCfg.getFileName(), peer.hasFile());
                    peerProcess.ourBitfield = Utilities.createBitfield(me.hasFile());
                    break;
                }
                Socket tempSocket = new Socket(peer.getHostName(), peer.getListeningPort());
                System.out.println("Connected to " + peer.getHostName() + "(" + peer.getPeerID() + ")" + " in port " + peer.getListeningPort());
                new Handler(tempSocket, peers).start();
            }
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
        private Message message;    //message received from the client
        private Socket connection;
        private ObjectInputStream in;    //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        private List<Peer> peers;
        private boolean[] theirBitfield;
        private boolean isChoked = true;
        private boolean interested = false;

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
                HandshakeMessage handshakeMessage = new HandshakeMessage(peerID);
                sendMessage(connection, handshakeMessage);
                System.out.println("Message \"" + handshakeMessage.getMessage() + "\" sent");
                Boolean connect = true;
                handshakeMessage = (HandshakeMessage) in.readObject();
                
                //show the message to the user
                System.out.println("Receive message: \"" + handshakeMessage.getMessage() + "\" from client ");
                if(!Utilities.isValidHandshake(handshakeMessage.getMessage(), peers)){
                    connect = false;
                    sendMessage(connection, "Disconnecting due to invalid handshake");
                    System.out.println("Disconnect with Client due to invalid handshake");
                }
                if(connect){
                    sendMessage(connection, "Connection successful!");
                }
                String handshakeVerification = (String) in.readObject();
                if(!handshakeVerification.equals("Connection successful!")){
                    connect = false;
                }
                threads.add(connection);
                if(connect){
                    sendMessage(connection, new BitfieldMessage(peerProcess.ourBitfield));
                    BitfieldMessage bitfieldMessage = (BitfieldMessage) in.readObject();
                    theirBitfield = bitfieldMessage.getPayload();
                    System.out.println("THEIR BITFIELD: ");
                    for(int x=0; x<theirBitfield.length; x++){
                        System.out.print(theirBitfield[x]);
                    }
                }
                try {
                    long startTime = System.nanoTime();
                    while (connect) {
                        if(System.nanoTime() - startTime >= unchokeInterval){
                            sendMessage(connection, new ChokeMessage());
                            startTime = System.nanoTime();
                        }
                        message = (Message) in.readObject();
                        switch(message.getValue()){
                            case 0: handleChokeMessage();
                            case 1: sendMessage(connection, handleUnchokeMessage());
                            case 2: handleInterestedMessage();
                            case 3: handleNotInterestedMessage();
                            case 4: theirBitfield = handleHaveMessage(theirBitfield, ((HaveMessage) message).getPayload());
                            case 6: if(!isChoked) sendMessage(connection, handleRequestMessage(((RequestMessage) message).getPayload()));
                            case 7: sendMessageToAll(threads, handlePieceMessage((PieceMessage) message));
                            default: break;
                        }


                        //show the message to the user
                        System.out.println("Receive message: \"" + message.getValue() + "\" from client ");
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

        public boolean[] not(boolean[] array){
            boolean[] tempArray = new boolean[array.length];
            for(int x=0; x<array.length; x++){
                tempArray[x] = !array[x];
            }
            return tempArray;
        }
        public boolean[] and(boolean[] ourArray, boolean[] theirArray){
            boolean[] tempArray = new boolean[ourArray.length];
            for(int x=0; x<ourArray.length; x++){
                tempArray[x] = ourArray[x] && theirArray[x];
            }
            return tempArray;
        }

        public int findAOne(boolean[] bitfield){
            List<Integer> ones = new ArrayList<>();
            for(int x=0; x<bitfield.length; x++){
                if(bitfield[x]){
                    ones.add(x);
                }
            }
            return ones.get(new Random().nextInt(ones.size()));
        }

        public void handleChokeMessage(){
            System.out.println("We're now choked");
            isChoked = true;
        }

        public RequestMessage handleUnchokeMessage(){
            System.out.println("We're now unchoked");
            isChoked = false;
            return new RequestMessage(findAOne(and(not(ourBitfield), theirBitfield)));
        }

        public void handleInterestedMessage(){
            interested = true;
        }
        public void handleNotInterestedMessage(){
            interested = false;
        }
        public boolean[] handleHaveMessage(boolean[] bitfield, int index){
            bitfield[index] = true;
            return bitfield;
        }
        public PieceMessage handleRequestMessage(int index){
            return new PieceMessage(fileData[index], index);
        }
        public HaveMessage handlePieceMessage(PieceMessage pieceMessage){
            fileData[pieceMessage.getIndex()] = pieceMessage.getPayload();
            return new HaveMessage(pieceMessage.getIndex());
        }
        void sendMessage(Socket connection, Object msg)
        {
            try{
                ObjectOutputStream tempOut = new ObjectOutputStream(connection.getOutputStream());
                tempOut.writeObject(msg);
                tempOut.flush();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
        void sendMessageToAll(ArrayList<Socket> connections, Object msg){
            for(Socket connection : connections){
                sendMessage(connection, msg);
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



    class Neighbor{
        int peerID;
        int numOfPiecesReceived;
        public Neighbor(int peerID){
            this.peerID = peerID;
            numOfPiecesReceived = 0;
        }
        public void receivedPiece(){
            numOfPiecesReceived++;
        }
        public void resetPieces(){
            numOfPiecesReceived = 0;
        }
        public int getNumOfPieces(){
            return numOfPiecesReceived;
        }
        public int getPeerID(){
            return peerID;
        }
    }

}

