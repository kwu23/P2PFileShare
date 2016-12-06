import Messages.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kevinwu on 10/19/16.
 */
public class peerProcess {
    static CommonCfg commonCfg;
    List<Peer> peers;
    static int peerID;
    List<Connection> connections;
    static Peer me;
    static byte[][] fileData;
    static boolean[] ourBitfield;
    static long unchokeInterval;
    static long optimisticallyUnchokeInterval;
    static ArrayList<Handler> preferredNeighbors = new ArrayList<>();
    static ArrayList<Neighbor> amountReceived = new ArrayList<>();
    static ArrayList<Handler> handlers = new ArrayList<>();
    static int optimisticallyUnchokedIndex = 0;
    static boolean isFirst = true;
    static long startTimeOptimistic = System.nanoTime();
    static long startTimeUnchoking = System.nanoTime();

    public static void main(String[] args) throws IOException {
        commonCfg = new CommonCfg("Common.cfg");
        peerProcess.unchokeInterval = CommonCfg.getUnchokingInterval() * (long) 1000000000;
        peerProcess.optimisticallyUnchokeInterval = CommonCfg.getOptimisticUnchokingInterval() * (long) 1000000000;
        peerID = Integer.parseInt(args[0]);
        peerProcess client = new peerProcess();
        client.run();
    }
    public static void sendMessageToAll(int msg){
        for(int x=0; x<handlers.size(); x++){
            handlers.get(x).sendHaveMessage(msg);
        }
    }

    public static void optimisticallyUnchokeSomeone(){
        if(System.nanoTime() - startTimeOptimistic >= optimisticallyUnchokeInterval){
            for(int a=0; a<5; a++){
                for(int x=0; x<handlers.size(); x++){
                    chokePreviousOptimisticallyunchokedPeer();
                    isFirst = false;
                    optimisticallyUnchokedIndex = (int) (Math.random() * handlers.size());
                    if(handlers.get(optimisticallyUnchokedIndex).unchoke()){
                        startTimeOptimistic = System.nanoTime();
                        return;
                    }
                }
            }
            startTimeOptimistic = System.nanoTime();
        }
    }

    public static void checkPreferredNeighbors(){
        if(System.nanoTime() - startTimeUnchoking >= unchokeInterval) {
            int k = CommonCfg.getNumberOfPreferredNeighbors();
            int currentNeighbors = 0;
            ArrayList<Handler> neighborsToUnchoke = new ArrayList<>();
            ArrayList<Integer> numPieces = new ArrayList<>();
            for (int x = 0; x < handlers.size(); x++) {
                if(handlers.get(x) != null && handlers.get(x).getNeighbor() != null){
                    numPieces.add(handlers.get(x).getNeighbor().getNumOfPieces());
                }
            }
            for (int x = 0; x < numPieces.size(); x++) {
                int max = numPieces.indexOf(Collections.max(numPieces));
                numPieces.remove(max);
                if (handlers.get(max).isInterestedIn()) {
                    neighborsToUnchoke.add(handlers.get(max));
                    currentNeighbors++;
                }
                if (currentNeighbors >= k) {
                    break;
                }
            }
            ArrayList<Integer> indexesToRemove = new ArrayList<>();
            for (int x = 0; x < preferredNeighbors.size(); x++) {
                if (!neighborsToUnchoke.contains(preferredNeighbors.get(x))) {
                    indexesToRemove.add(x);
                }
            }
            for(int x=0; x<indexesToRemove.size(); x++){
                preferredNeighbors.remove((int)indexesToRemove.get(x)).chokePeer();
            }
            for(int x=0; x<neighborsToUnchoke.size(); x++){
                neighborsToUnchoke.get(x).unchoke();
                if(!preferredNeighbors.contains(neighborsToUnchoke.get(x))){
                    preferredNeighbors.add(neighborsToUnchoke.get(x));
                }
            }
            for(int x = 0; x < handlers.size(); x++){
                if(handlers.get(x) != null && handlers.get(x).getNeighbor() != null){
                    handlers.get(x).getNeighbor().resetPieces();
                }
            }
            startTimeUnchoking = System.nanoTime();
        }
    }

    public static void randomCheckPreferredNeighbors(){
        if(System.nanoTime() - startTimeUnchoking >= unchokeInterval) {
            int k = CommonCfg.getNumberOfPreferredNeighbors();
            ArrayList<Handler> neighborsToUnchoke = new ArrayList<>();
            if (handlers.size() <= k) {
                for (int x = 0; x < handlers.size(); x++) {
                    neighborsToUnchoke.add(handlers.get(x));
                }
            }else{
                for (int x = 0; x < k; ) {
                    int randomPeer = (int) (Math.random() * handlers.size());
                    if (handlers.get(randomPeer).isInterestedIn()) {
                        neighborsToUnchoke.add(handlers.get(x));
                        x++;
                    }
                }
            }
            ArrayList<Integer> indexesToRemove = new ArrayList<>();
            for (int x = 0; x < preferredNeighbors.size(); x++) {
                if (!neighborsToUnchoke.contains(preferredNeighbors.get(x))) {
                    indexesToRemove.add(x);
                }
            }
            for(int x=0; x<indexesToRemove.size(); x++){
                preferredNeighbors.remove((int)indexesToRemove.get(x)).chokePeer();
            }
            for(int x=0; x<neighborsToUnchoke.size(); x++){
                neighborsToUnchoke.get(x).unchoke();
                if(!preferredNeighbors.contains(neighborsToUnchoke.get(x))){
                    preferredNeighbors.add(neighborsToUnchoke.get(x));
                }
            }
            startTimeUnchoking = System.nanoTime();
        }
    }

    public static void chokePreviousOptimisticallyunchokedPeer(){
        if(isFirst){
            return;
        }
        handlers.get(optimisticallyUnchokedIndex).chokePeer();
    }
    void run() throws IOException {
        try{
            peers = Utilities.readCfg("PeerInfo.cfg");
            connections = new ArrayList<>();
            for(Peer peer : peers){
                if(peer.getPeerID() == peerID){
                    me = peer;
                    System.out.println("DO I HAVE FILE? " + peer.hasFile());
                    fileData = Utilities.getBytesOfFile(CommonCfg.getFileName(), peer.hasFile());
                    ourBitfield = Utilities.createBitfield(me.hasFile());
                    break;
                }
                Socket tempSocket = new Socket(peer.getHostName(), peer.getListeningPort());
                System.out.println("Connected to " + peer.getHostName() + "(" + peer.getPeerID() + ")" + " in port " + peer.getListeningPort());
                Handler handler = new Handler(tempSocket, peers);
                handlers.add(handler);
                handler.start();
            }
            ServerSocket listener = new ServerSocket(me.getListeningPort());
            while(true) {
                Handler handler = new Handler(listener.accept(), peers);
                handlers.add(handler);
                handler.start();
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
        private boolean isChoked = true;        //if we are choked
        public boolean peerChoked = true;
        private boolean interested = false;
        private boolean areWeInterested = false;
        private Neighbor neighbor;

        public Handler(Socket connection, List<Peer> peers) {
            this.connection = connection;
            this.peers = peers;
        }

        public Neighbor getNeighbor(){
            return neighbor;
        }
        public boolean isInterestedIn(){
            return interested;
        }

        public void run() {
            try {
                //initialize Input and Output streams
                if(out == null){
                    out = new ObjectOutputStream(connection.getOutputStream());
                    out.flush();
                }
                if(in == null){
                    in = new ObjectInputStream(connection.getInputStream());
                }
                connection.setSoTimeout(5000);
                HandshakeMessage handshakeMessage = new HandshakeMessage(peerID);
                sendMessage(handshakeMessage);
                System.out.println("Message \"" + handshakeMessage.getMessage() + "\" sent");
                Boolean connect = true;
                handshakeMessage = (HandshakeMessage) in.readObject();

                //show the message to the user
                System.out.println("Receive message: \"" + handshakeMessage.getMessage() + "\" from client ");
                if(!Utilities.isValidHandshake(handshakeMessage.getMessage(), peers)){
                    connect = false;
                    sendMessage("Disconnecting due to invalid handshake");
                    System.out.println("Disconnect with Client due to invalid handshake");
                }
                if(connect){
                    sendMessage("Connection successful!");
                    int connectedPeerId = handshakeMessage.getPeerID();
                    neighbor = new Neighbor(connectedPeerId);
                    amountReceived.add(neighbor);
                }

                String handshakeVerification = (String) in.readObject();
                System.out.println(handshakeVerification);
                if(!handshakeVerification.equals("Connection successful!")){
                    connect = false;
                }
                if(connect){
                    sendMessage(new BitfieldMessage(peerProcess.ourBitfield));
                    BitfieldMessage bitfieldMessage = (BitfieldMessage) in.readObject();
                    theirBitfield = bitfieldMessage.getPayload();
                    if(interestedCheck(and(not(ourBitfield), theirBitfield))) {
                        sendMessage(new InterestedMessage());
                        System.out.println("Sending out interested msg to " + neighbor.getPeerID());
                        areWeInterested = true;
                    }
                }
                try {
                    while (connect) {
                        // if (hasFile) random unchoke else if...
                        //optimisticallyUnchokeSomeone();
                         if (me.hasFile()) {
                             //System.out.println("I hab a file");
                             randomCheckPreferredNeighbors();
                         } else {
                             //System.out.println("I don't hab a file");
                             checkPreferredNeighbors();
                         }
                        try{
                            message = null;
                            synchronized (in) {
                                message = (Message) in.readObject();
                            }

                        }catch (SocketTimeoutException e){
                            System.out.print("=============" + connection.isConnected() + "=============");
                            message = null;
                        }

                        if(message != null){
                            switch(message.getValue()){
                                case 0: handleChokeMessage(); break;                                                                                    //choke
                                case 1: sendMessage(handleUnchokeMessage()); break;                                                                //unchoke
                                case 2: handleInterestedMessage(); break;                                                                               //interested
                                case 3: handleNotInterestedMessage(); break;                                                                            //not interested
                                case 4: theirBitfield = handleHaveMessage(theirBitfield, ((HaveMessage) message).getPayload()); break;                  //have
                                case 6: sendMessage(handleRequestMessage(((RequestMessage) message).getPayload())); break;                         //request
                                case 7: PieceMessage tempPiece = (PieceMessage) message; sendMessageToAll(tempPiece.getIndex()); handlePieceMessage(tempPiece); break;                                            //piece
                                default: break;
                            }
                            System.out.println("Receive message: \"" + message.getClass().getName() + "\" from " + neighbor.getPeerID());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                //System.out.println("==============" + message.getClass().getName() + "==============");
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

        // Check if we are interested in others
        public boolean interestedCheck(boolean[] bitfield){
            List<Integer> ones = new ArrayList<>();
            for(int x=0; x<bitfield.length; x++){
                if(bitfield[x]){
                    return true;
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
            System.out.println("We're now choked by " + neighbor.getPeerID());
            isChoked = true;
        }

        public RequestMessage handleUnchokeMessage(){
            System.out.println("We're now unchoked from " + neighbor.getPeerID());
            isChoked = false;
            return new RequestMessage(findAOne(and(not(ourBitfield), theirBitfield)));
        }

        public void handleInterestedMessage(){
            interested = true;
            System.out.println(neighbor.getPeerID() + " is interested");
        }

        public void handleNotInterestedMessage(){
            interested = false;
        }

        public boolean[] handleHaveMessage(boolean[] bitfield, int index){
            bitfield[index] = true;
            if(interestedCheck(and(not(ourBitfield), theirBitfield)) && !areWeInterested) {
                sendMessage(new InterestedMessage());
                areWeInterested = true;
            }
            else if(!interestedCheck(and(not(ourBitfield), theirBitfield)) && areWeInterested) {
                sendMessage(new NotInterestedMessage());
                areWeInterested = false;
            }
            return bitfield;
        }
        public PieceMessage handleRequestMessage(int index){
            return new PieceMessage(fileData[index], index);
        }

        public int handlePieceMessage(PieceMessage pieceMessage){
            fileData[pieceMessage.getIndex()] = pieceMessage.getPayload();
            System.out.println("================= " + pieceMessage.getIndex() + " =================");
            ourBitfield[pieceMessage.getIndex()] = true;
            if(interestedCheck(and(not(ourBitfield), theirBitfield)) && !areWeInterested) {
                sendMessage(new InterestedMessage());
                areWeInterested = true;
            }
            else if(!interestedCheck(and(not(ourBitfield), theirBitfield)) && areWeInterested) {
                sendMessage(new NotInterestedMessage());
                areWeInterested = false;
            }
            System.out.println(currentBits() / (double) ourBitfield.length * (double) 100 + "% done");

            int missing = ourBitfield.length;
            for (int i = 0; i < ourBitfield.length; i++) {
                if (ourBitfield[i] == true) {
                    missing--;
                }

                if (missing < 3) {
                    if (ourBitfield[i] == false) {
                        System.out.println("Missing at index: " + i);
                    }
                }

            }

            neighbor.receivedPiece();
            if(areWeInterested && !isChoked){
                sendMessage(new RequestMessage(findAOne(and(not(ourBitfield), theirBitfield))));
            }
            return pieceMessage.getIndex();
        }

        public double currentBits(){
            double total = 0;
            for(int x=0; x<ourBitfield.length; x++){
                if(ourBitfield[x]){
                    total++;
                }
            }
            return total;
        }

        void sendMessage(Object msg)
        {
            if(msg == null){
                return;
            }
            try{
                out.writeObject(msg);
                out.flush();
            }
            catch(IOException ioException){
                return;
                //ioException.printStackTrace();
            }
        }
        void sendHaveMessage(int index){
            sendMessage(new HaveMessage(index));
        }


        boolean unchoke(){
            if(peerChoked && interested){
                sendMessage(new UnchokeMessage());
                System.out.println("New optimistically unchoke msg sent to " + neighbor.getPeerID());
                return true;
            }
            return false;
        }
        void chokePeer(){
            sendMessage(new ChokeMessage());
            System.out.println("Choke msg sent to " + neighbor.getPeerID());
        }

        /*
        void randomCheckPrefferedNeighbors() {
            int k = CommonCfg.getNumberOfPreferredNeighbors();
            //System.out.println("k: " + k);
            int numOfInterestedPeers = 0;
            for (Peer p : peers) {
                if (!p.hasFile()) {
                    numOfInterestedPeers++;
                }
            }
            boolean wasUnchoked = false;
            if(preferredNeighbors.contains(neighbor.getPeerID())){
                wasUnchoked = true;
                preferredNeighbors.remove(preferredNeighbors.indexOf(neighbor.getPeerID()));
            }
            if(Math.random() < (double) k/ (double) numOfInterestedPeers && interested && preferredNeighbors.size() < k) {
                preferredNeighbors.add(neighbor.getPeerID());
                peerChoked = false;
                if(!wasUnchoked){
                    sendMessage(out, new UnchokeMessage());
                    System.out.println("New rand unchoke msg sent to " + neighbor.getPeerID());
                }
            }else{
                if(preferredNeighbors.contains(neighbor.getPeerID())){
                    peerChoked = true;
                    System.out.println("New rand choke msg sent to " + neighbor.getPeerID());
                    preferredNeighbors.remove(preferredNeighbors.indexOf(neighbor.getPeerID()));
                    sendMessage(out, new ChokeMessage());
                }
            }
        }
        void checkPreferredNeighbors() {
            int k = CommonCfg.getNumberOfPreferredNeighbors();
            ArrayList<Neighbor> tempNeighbors = new ArrayList<>();
            tempNeighbors.addAll(amountReceived);
            ArrayList<Integer> arrMax = new ArrayList<>();

            int peerListSize = peers.size();

            for (int i = 0; i < k; i++) {
                if(i < peerListSize && interested){
                    int max = -1;
                    int index = -1;

                    for (Neighbor n : tempNeighbors) {
                        //System.out.println(tempNeighbors.size());
                        if (n.getNumOfPieces() > max) {
                            max = n.getNumOfPieces();
                            index = tempNeighbors.indexOf(n);
                        }
                    }

                    arrMax.add(tempNeighbors.get(index).getPeerID());
                    tempNeighbors.remove(index);
                }

            }

            for (Integer i : arrMax) {
                if(i == neighbor.getPeerID() && !preferredNeighbors.contains(i)) {
                    sendMessage(out, new UnchokeMessage());
                }
            }

            ArrayList<Integer> prefTempNeighbor = new ArrayList<>();
            prefTempNeighbor.addAll(preferredNeighbors);
            for(int i = 0; i < prefTempNeighbor.size(); i++){
                if(!arrMax.contains(prefTempNeighbor.get(i))) {
                    preferredNeighbors.remove(prefTempNeighbor.get(i));
                    sendMessage(out, new ChokeMessage());
                    System.out.println("New choke msg sent");
                }
            }

            for (Integer i : arrMax) {
                if(!preferredNeighbors.contains(i)) {
                    preferredNeighbors.add(i);
                }
            }

            // LOGGING if preferred neighbor exceeds k
            if (preferredNeighbors.size() > k) {
                System.out.println("Preferred Neighbors exceeded k: " + k);
            }
        }
        */
    }

}

