import java.util.List;

/**
 * Created by kevinwu on 10/19/16.
 */
public class PeerInfoCfg {
    private List<Peer> listOfPeers;

    public PeerInfoCfg(String fileName){
        listOfPeers = PeerUtilities.readCfg(fileName);
    }

    public List<Peer> getPeers(){
        return listOfPeers;
    }
}
