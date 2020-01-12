import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;

public class HTTPDownloadManager {
    private static String m_fileURL;
    private static int m_maximumConcurrentConnections;
    private String m_FileName;
    private int m_currentPoint;


    public HTTPDownloadManager(String fileURL){
        m_fileURL = fileURL;
    }

    public HTTPDownloadManager(String fileURL, int maximumConcurrentConnections){
        m_fileURL = fileURL;
        m_maximumConcurrentConnections = maximumConcurrentConnections;
    }

    private void setFileNameFromURL() {
        m_FileName = m_fileURL.substring(m_fileURL.lastIndexOf('/') + 1);
    }

    public void downloadFile() {
        setFileNameFromURL();
        //System.out.println("Downloading using " + m_maximumConcurrentConnections + " connections...");
        try{
            URL downloadUrl = new URL(m_fileURL);
            HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            //FileOutputStream fileOutputStream = new FileOutputStream(m_FileName);

            RandomAccessFile fileToWrite = new RandomAccessFile(m_FileName, "rw");
            int startPoint = 0;
            fileToWrite.seek(startPoint);

            m_currentPoint = startPoint;

            int bytesRead;
            double totalBytesRead = 0;

            int currentPercentage;
            int counterPercentage = 0;

            long fileSize = conn.getContentLengthLong();
            int chunckSize = (int)fileSize/1024;
            int endingPoint = (int)fileSize;
            byte dataBuffer[] = new byte[chunckSize];


            System.out.println("File size is: " + chunckSize + "KB");

            while (totalBytesRead < endingPoint){

                //reads up to chunckSize bytes from this input stream into dataBuffer array of bytes
                //the start offset in the bytes array is 0
                //returns number of bytes read into dataBuffer or -1 if no more data to read (end of input stream)
                bytesRead = in.read(dataBuffer, 0, chunckSize);
                if(bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;

                //writes bytesRead bytes from the dataBuffer into this file
                //the start offset in the file is 0
                fileToWrite.write(dataBuffer, 0, bytesRead);

                currentPercentage = (int) ((totalBytesRead / fileSize) * 100.0);

                // if still downloading
                if(currentPercentage > counterPercentage) {
                    System.out.println("Downloaded " + currentPercentage + "%");
                    counterPercentage++;
                }

            }
            System.out.println("Finished downloading");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
