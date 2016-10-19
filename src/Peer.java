/**
 * Created by kevinwu on 10/19/16.
 */
public class Peer {
    private int peerID;
    private String hostName;
    private int listeningPort;
    private boolean hasFile;

    public Peer(int peerID, String hostName, int listeningPort, boolean hasFile){
        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
    }
    public int getPeerID(){
        return peerID;
    }
    public String getHostName(){
        return hostName;
    }
    public int getListeningPort(){
        return listeningPort;
    }
    public boolean isHasFile(){
        return hasFile;
    }
}
