// file for all the definitions of the HTTPDownloader
import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;

public class HTTPRangeGetter implements Runnable{
    private String m_FileURL;
    private String m_FileName;
    private int m_currentPoint;
    private int m_ChunkLength;
    private int m_Offset;
    private int m_ThreadID;

    public HTTPRangeGetter(int chunkLength, int offset, int threadID, String fileURL, String fileName){
        m_ChunkLength = chunkLength;
        m_FileURL = fileURL;
        m_Offset = offset;
        m_ThreadID = threadID;
        m_FileName = fileName;
    }

    public void run() {
        //open connection
        HttpURLConnection conn = null;
        try {
            URL downloadUrl = new URL(m_FileURL);
            conn = (HttpURLConnection) downloadUrl.openConnection();
            conn.setConnectTimeout(10000);

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

            RandomAccessFile fileToWrite = new RandomAccessFile(m_FileName, "rw");
            int startByte = 0;
            fileToWrite.seek(startByte);

            m_currentPoint = startByte;

            int bytesRead;
            double totalBytesRead = 0;

            int currentPercentage;
            int counterPercentage = 0;

            long fileSize = conn.getContentLengthLong();
            int chunckSize = (int)fileSize/1024;
            int endingByte = (int)fileSize;
            byte dataBuffer[] = new byte[chunckSize];


            System.out.println("File size is: " + chunckSize + "KB");

            while (totalBytesRead < endingByte){

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

                currentPercentage = (int) (totalBytesRead * 100 / fileSize);

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
