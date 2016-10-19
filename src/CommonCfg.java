import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by kevinwu on 10/19/16.
 */
public class CommonCfg {
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;

    public CommonCfg(String fileName) {
        readCommonConfig(fileName);
    }

    public void readCommonConfig(String fileName) {
        List<String> lines;
        try {
           lines = Files.readAllLines(Paths.get(fileName));
            for (String s : lines) {
                if (s.contains("NumberOfPreferredNeighbors")) {
                    numberOfPreferredNeighbors = Integer.parseInt(s);
                } else if (s.contains("UnchokingInterval")) {
                    unchokingInterval = Integer.parseInt(s);
                } else if (s.contains("OptimisticUnchokingInterval")) {
                    optimisticUnchokingInterval = Integer.parseInt(s);
                } else if (s.contains("FileName")) {
                    String[] splitStr = s.split(" ", 2);
                    fileName = splitStr[1];
                } else if (s.contains("FileName")) {
                    fileSize = Integer.parseInt(s);
                } else if (s.contains("PieceSize")) {
                    pieceSize = Integer.parseInt(s);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfPreferredNeighbors() {
        return numberOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

}
