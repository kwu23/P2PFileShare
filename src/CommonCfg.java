import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by kevinwu on 10/19/16.
 */
public class CommonCfg {
    private static int numberOfPreferredNeighbors;
    private static int unchokingInterval;
    private static int optimisticUnchokingInterval;
    private static String fileName;
    private static int fileSize;
    private static int pieceSize;

    public CommonCfg(String fileName) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(fileName));
            boolean temp = false;
            for (String s : lines) {

                if (s.contains("NumberOfPreferredNeighbors")) {
                    String[] tempS = s.split(" +");
                    CommonCfg.numberOfPreferredNeighbors = Integer.parseInt(tempS[1]);
                } else if (s.contains("UnchokingInterval")) {
                    temp = true;
                    String[] tempS = s.split(" +");
                    CommonCfg.unchokingInterval = Integer.parseInt(tempS[1]);
                } else if (s.contains("OptimisticUnchokingInterval")) {
                    String[] tempS = s.split(" +");
                    CommonCfg.optimisticUnchokingInterval = Integer.parseInt(tempS[1]);
                } else if (s.contains("FileName")) {
                    String[] splitStr = s.split(" ");
                    CommonCfg.fileName = splitStr[1];
                } else if (s.contains("FileSize")) {
                    String[] tempS = s.split(" +");
                    CommonCfg.fileSize = Integer.parseInt(tempS[1]);
                } else if (s.contains("PieceSize")) {
                    String[] tempS = s.split(" +");
                    CommonCfg.pieceSize = Integer.parseInt(tempS[1]);
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
