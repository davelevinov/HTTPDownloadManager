import java.io.*;
import java.net.*;
import java.util.*;

public class IdcDm {
    /**
     * Receive arguments from command line and start the download
     * *
     */

    public static void main(String[] args) throws MalformedURLException {
       // List<String> url_list = new ArrayList<>();


        if (args.length < 1 || args.length > 2){
            System.err.println("java IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]");
            System.exit(1);
        }


        int numOfConnections = 1;
        if (args.length > 1) {
            numOfConnections = Integer.parseInt(args[1]);
        }

        String URLs = args[0];
        System.err.printf("Downloading using %d connections...", numOfConnections);

        HTTPDownloadManager manager = new HTTPDownloadManager(URLs, numOfConnections);
        manager.startDownload();
    }

    public static List<URL> getListOfURLs(String URLs){
        List<URL> listOfURLs = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(URLs));
            String url;
            while((url = br.readLine()) != null){
                listOfURLs.add(new URL(url));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listOfURLs;
    }
}
