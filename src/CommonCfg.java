import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by kevinwu on 10/19/16.
 */
public class CommonCfg {
    public static int numberOfPreferredNeighbors;
    public static int unchokingInterval;
    public static int optimisticUnchokingInterval;
    public static String fileName;
    public static int fileSize;
    public static int pieceSize;

    public CommonCfg(String fileName) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(fileName));
            boolean temp = false;
            for (String s : lines) {
                if (s.contains("NumberOfPreferredNeighbors")) {
                    String[] tempS = s.split(" +");
                    this.numberOfPreferredNeighbors = Integer.parseInt(tempS[1]);
                } else if (s.contains("UnchokingInterval")) {
                    temp = true;
                    String[] tempS = s.split(" +");
                    this.unchokingInterval = Integer.parseInt(tempS[1]);
                } else if (s.contains("OptimisticUnchokingInterval")) {
                    String[] tempS = s.split(" +");
                    this.optimisticUnchokingInterval = Integer.parseInt(tempS[1]);
                } else if (s.contains("FileName")) {
                    String[] splitStr = s.split(" ");
                    this.fileName = splitStr[1];
                } else if (s.contains("FileSize")) {
                    String[] tempS = s.split(" +");
                    this.fileSize = Integer.parseInt(tempS[1]);
                } else if (s.contains("PieceSize")) {
                    String[] tempS = s.split(" +");
                    this.pieceSize = Integer.parseInt(tempS[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNumberOfPreferredNeighbors() {
        return CommonCfg.numberOfPreferredNeighbors;
    }

    public static int getUnchokingInterval() {
        return CommonCfg.unchokingInterval;
    }

    public static int getOptimisticUnchokingInterval() {
        return CommonCfg.optimisticUnchokingInterval;
    }

    public static String getFileName() {
        return CommonCfg.fileName;
    }

    public static int getFileSize() {
        return CommonCfg.fileSize;
    }

    public static int getPieceSize() {
        return CommonCfg.pieceSize;
    }

}
