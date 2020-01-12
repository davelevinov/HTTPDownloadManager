import java.net.MalformedURLException;

public class IdcDm {
    public static void main(String[] args) throws MalformedURLException {
        HTTPDownloadManager manager;
        if (args.length == 1) {
            manager = new HTTPDownloadManager(args[0]);
        } else {
            manager = new HTTPDownloadManager(args[0], Integer.parseInt(args[1]));
        }
        manager.downloadFile();
    }
}
