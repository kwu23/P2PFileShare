import Messages.HandshakeMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinwu on 10/19/16.
 */



public class Utilities {
    public static List<Peer> readCfg(String fileName){
        List<Peer> peers = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!lines.isEmpty()){
            for(String peerData : lines){
                peerData.trim();
                if(!peerData.isEmpty()){
                    String[] data = peerData.split(" ");
                    if(data[3].equals("1")){
                        peers.add(new Peer(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]), true));
                    }else{
                        peers.add(new Peer(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]), false));
                    }
                }
            }
        }
        return peers;
    }



    public static Boolean isValidHandshake(String message, List<Peer> peers){
        HandshakeMessage handshakeMessage = new HandshakeMessage(message);
        int peerID = handshakeMessage.getPeerID();
        if(handshakeMessage.getHEADER().equals("P2PFILESHARINGPROJ") && handshakeMessage.getZEROES().equals("0000000000")) {
            for(Peer peer : peers){
                if(peerID == peer.getPeerID()){
                    return true;
                }
            }
        }
        return false;
    }

    public static byte[][] getBytesOfFile(String path) throws IOException{
        byte[] fileData = Files.readAllBytes(Paths.get(path));
        byte[][] pieces = new byte[getBitfieldSize(CommonCfg.getFileSize(), CommonCfg.getPieceSize())][CommonCfg.getPieceSize()];
        int counter = 0;
        for(int x=0; x<fileData.length; x++){
            for(int y=0; y<pieces.length; y++){
                pieces[x][y] = fileData[counter];
                counter++;
            }
        }
        return pieces;
    }

    public static void turnBytesToFile(byte[] bytes) throws IOException{
        FileOutputStream stream = new FileOutputStream(CommonCfg.getFileName());
        try {
            stream.write(bytes);
        } finally {
            stream.close();
        }
    }

    public static boolean[] createBitfield(boolean hasFile){
        boolean[] bitfield = new boolean[getBitfieldSize(CommonCfg.getFileSize(), CommonCfg.getPieceSize())];
        for(int x=0; x<bitfield.length; x++){
            bitfield[x] = hasFile;
        }
        return bitfield;
    }

    public static int getBitfieldSize(int fileSize, int pieceSize){
        if(fileSize%pieceSize == 0) {
            return fileSize / pieceSize;
        }else {
            return fileSize / pieceSize +1;
        }
    }

    public static int getRandomNumberFrom(int start, int end){
        return (int) Math.random()*end + start;
    }
}
