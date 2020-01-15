import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Metadata {
    public Metadata(String url) {
    }

    public static boolean exists(String fileURL){
        return true;
    }

    public Metadata getMetadata(String fileURL) {
        if (Metadata.exists(fileURL)) {
            return existingMetadata(fileURL);
        } else {
            return new Metadata(fileURL);
        }
    }

    public Metadata existingMetadata(String fileURL) {
        return null;
    }
}
