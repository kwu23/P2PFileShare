/**
 * Created by cfaik on 11/16/2016.
 */
public class Neighbor {
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
