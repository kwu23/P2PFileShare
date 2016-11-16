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
    static ArrayList<ObjectOutputStream> threads = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        commonCfg = new CommonCfg("Common.cfg");
        peerProcess.unchokeInterval = CommonCfg.getUnchokingInterval() * (long) 1000000000;
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
        private Neighbor neighbor;

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
                sendMessage(out, handshakeMessage);
                System.out.println("Message \"" + handshakeMessage.getMessage() + "\" sent");
                Boolean connect = true;
                handshakeMessage = (HandshakeMessage) in.readObject();
                
                //show the message to the user
                System.out.println("Receive message: \"" + handshakeMessage.getMessage() + "\" from client ");
                System.out.print("TEST1");
                if(!Utilities.isValidHandshake(handshakeMessage.getMessage(), peers)){
                    connect = false;
                    sendMessage(out, "Disconnecting due to invalid handshake");
                    System.out.println("Disconnect with Client due to invalid handshake");
                }
                if(connect){
                    sendMessage(out, "Connection successful!");
                    int connectedPeerId = handshakeMessage.getPeerID();
                    neighbor = new Neighbor(connectedPeerId);
                    amountReceived.add(neighbor);
                }
                System.out.print("TEST2");
                String handshakeVerification = (String) in.readObject();
                System.out.println(handshakeVerification);
                if(!handshakeVerification.equals("Connection successful!")){
                    connect = false;
                }
                threads.add(out);
                if(connect){
                    sendMessage(out, new BitfieldMessage(peerProcess.ourBitfield));
                    BitfieldMessage bitfieldMessage = (BitfieldMessage) in.readObject();
                    theirBitfield = bitfieldMessage.getPayload();
                    interested = interestedCheck(and(not(ourBitfield), theirBitfield));
                }
                try {
                    long startTime = System.nanoTime();
                    while (connect) {
                        if(System.nanoTime() - startTime >= unchokeInterval){
                            int k = CommonCfg.getNumberOfPreferredNeighbors();
                            ArrayList<Neighbor> tempNeighbors = new ArrayList<>();
                            tempNeighbors.addAll(amountReceived);
                            int[] arrMax = new int[k];

                            for (int i = 0; i < k; i++) {
                                int max = -1;
                                int index = -1;
                                for (Neighbor n : tempNeighbors) {
                                    if (n.getNumOfPieces() > max) {
                                        max = n.getNumOfPieces();
                                        index = tempNeighbors.indexOf(n);
                                    }
                                }
                                tempNeighbors.remove(index);
                                arrMax[k] = max;
                            }

                            startTime = System.nanoTime();
                        }
                        message = (Message) in.readObject();
                        
                        switch(message.getValue()){
                            case 0: handleChokeMessage(); break;
                            case 1: sendMessage(out, handleUnchokeMessage()); break;
                            case 2: handleInterestedMessage(); break;
                            case 3: handleNotInterestedMessage(); break;
                            case 4: theirBitfield = handleHaveMessage(theirBitfield, ((HaveMessage) message).getPayload()); break;
                            case 6: if(!isChoked) sendMessage(out, handleRequestMessage(((RequestMessage) message).getPayload())); break;
                            case 7: sendMessageToAll(threads, handlePieceMessage((PieceMessage) message)); break;
                            default: break;
                        }

                        //show the message to the user
                        //System.out.println("Receive message: \"" + message.getValue() + "\" from client ");
                    }
                } catch (Exception classnot) {
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

            int num = ((int)((Math.random())*ones.size())-1);
            if(num < 0 || num >= ones.size()){
                return 0;
            }
            return ones.get(num);
        }

        public boolean interestedCheck(boolean[] bitfield){
            List<Integer> ones = new ArrayList<>();
            for(int x=0; x<bitfield.length; x++){
                if(bitfield[x]){
                    ones.add(x);
                }
            }

            if(ones.size() == 0){
                return false;
            }
            else{
                return true;
            }
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
            interested = interestedCheck(and(not(ourBitfield), theirBitfield));
            return bitfield;
        }
        public PieceMessage handleRequestMessage(int index){
            return new PieceMessage(fileData[index], index);
        }

        public HaveMessage handlePieceMessage(PieceMessage pieceMessage){
            fileData[pieceMessage.getIndex()] = pieceMessage.getPayload();
            ourBitfield[pieceMessage.getIndex()] = true;
            interested = interestedCheck(and(not(ourBitfield), theirBitfield));
            neighbor.receivedPiece();
            return new HaveMessage(pieceMessage.getIndex());
        }

        void sendMessage(ObjectOutputStream out, Object msg)
        {
            try{
                out.writeObject(msg);
                out.flush();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
        void sendMessageToAll(ArrayList<ObjectOutputStream> connections, Object msg){
            for(ObjectOutputStream connection : connections){
                sendMessage(connection, msg);
            }
        }
    }

}

