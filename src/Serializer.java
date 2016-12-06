import java.io.*;

public class Serializer {

    public static String serialize(Object obj) throws IOException {
        String s = "";
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(obj);
            so.flush();
            s = bo.toString();
        } catch (Exception e) {
            System.out.println(e);
        }
        return s;
    }

    public static Object deserialize(String s) throws IOException, ClassNotFoundException {
        Object obj = new Object();
        try {
            byte b[] = s.getBytes();
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            obj = si.readObject();
        } catch (Exception e) {
            System.out.println(e);
        }
        return obj;
    }

}