// file for all the definitions of the HTTPDownloader
import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;

public class HTTPDownloader {
    private static String FILE_URL = "https://archive.org/download/Mario1_500/Mario1_500.avi";
    private static String FILE_NAME = "Mario1_500.avi";

    public static void main (String[]args){
        try {
            URL downloadUrl = new URL(FILE_URL);
            try (BufferedInputStream in = new BufferedInputStream(downloadUrl.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME)) {
                URLConnection conn = downloadUrl.openConnection();
                int bytesRead;
                double totalBytesRead = 0;
                int currentPercentage = 0;
                int downloadedSoFar = 0;
                int fileSize = conn.getContentLength();
                byte dataBuffer[] = new byte[fileSize / 1024];
                //conn.getInputStream();
                System.out.println("file size is: " + conn.getContentLength() / 1024 + "KB");
                while ((bytesRead = in.read(dataBuffer, 0, fileSize / 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    currentPercentage = (int) ((totalBytesRead / fileSize) * 100.0);
                    if (currentPercentage > downloadedSoFar){
                        System.out.println("downloading.. " + currentPercentage + "%");
                        downloadedSoFar++;
                    }
                }
                System.out.println("Done downloading");
            } catch (IOException e) {
                // handle exception
            }
        } catch (Exception e) {
        }
    }
}
