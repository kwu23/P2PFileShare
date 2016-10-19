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
                    peers.add(new Peer(Integer.parseInt(data[0]), data[1], Integer.parseInt(data[2]), Boolean.parseBoolean(data[3])));
                }
            }
        }
        return peers;
    }

    public static int getRandomNumberFrom(int start, int end){
        return (int) Math.random()*end + start;
    }
}
