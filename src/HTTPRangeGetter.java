import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HTTPRangeGetter implements Runnable {
    private String m_FileURL;
    private String m_FileName;
    private int m_currentPoint;
    private double m_RangeSize;
    private int m_Offset;
    private int m_ThreadID;
    public int CHUNK_SIZE = 4096;
    public double m_NumOfChunksToRead;
    private LinkedBlockingQueue<Chunk> m_ChunksQueue;
    private Metadata m_Metadata;

    public HTTPRangeGetter(double rangeSize, int offset, int threadID, String fileURL, String fileName,
                           double numOfChunksToRead, LinkedBlockingQueue<Chunk> chunksQueue, Metadata metadata) {
        m_RangeSize = rangeSize;
        m_FileURL = fileURL;
        m_Offset = offset;
        m_ThreadID = threadID;
        m_FileName = fileName;
        m_NumOfChunksToRead = numOfChunksToRead;
        m_ChunksQueue = chunksQueue;
        m_Metadata = metadata;
    }

    public void run() {
        //open connection
        HttpURLConnection conn = null;
        try {
            URL downloadUrl = new URL(m_FileURL);
            conn = (HttpURLConnection) downloadUrl.openConnection();
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            int fileSize = conn.getContentLength();
            int bytesRead = 0;
            int delta = fileSize - bytesRead;
            int bytesToRead = Math.min(delta, CHUNK_SIZE);
            int i = 0;
            while(bytesRead < bytesToRead){
                /*
                if ((m_NumOfChunksToRead - (int) m_NumOfChunksToRead) != 0) {
                    //last thread got remainder, so it might read a piece of chunk at the end
                    if (i == m_NumOfChunksToRead) {
                        //last entire chunk to read for the last thread
                        CHUNK_SIZE *= (m_NumOfChunksToRead - (int) m_NumOfChunksToRead);
                    }
                }

                 */
                /*
                if(!m_Metadata.m_ChunksBitMap[i]) {
                    byte[] dataBuffer = new byte[CHUNK_SIZE];
                    in.readNBytes(dataBuffer, 0, CHUNK_SIZE);
                    Chunk chunk = new Chunk(dataBuffer, m_Offset, i);
                    m_ChunksQueue.put(chunk);
                }
                */
                delta = fileSize - bytesRead;
                bytesToRead = Math.min(delta, CHUNK_SIZE);
                if(!m_Metadata.m_ChunksBitMap[i]) {
                    byte[] dataBuffer = new byte[bytesToRead];
                    in.readNBytes(dataBuffer, 0, bytesToRead);
                    Chunk chunk = new Chunk(dataBuffer, m_Offset, i);
                    m_ChunksQueue.put(chunk);
                    i++;
                }
                m_Offset += CHUNK_SIZE;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


//    public void run2() {
//        //open connection
//        HttpURLConnection conn = null;
//        try {
//            URL downloadUrl = new URL(m_FileURL);
//            conn = (HttpURLConnection) downloadUrl.openConnection();
//            conn.setConnectTimeout(10000);
//
//            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
//
//            RandomAccessFile fileToWrite = new RandomAccessFile(m_FileName, "rw");
//            int startByte = 0;
//            fileToWrite.seek(startByte);
//
//            m_currentPoint = startByte;
//
//            int bytesRead;
//            double totalBytesRead = 0;
//
//            int currentPercentage;
//            int counterPercentage = 0;
//
//            long fileSize = conn.getContentLengthLong();
//            int chunckSize = (int)fileSize/1024;
//            int endingByte = (int)fileSize;
//            byte dataBuffer[] = new byte[chunckSize];
//
//
//            System.out.println("File size is: " + chunckSize + "KB");
//
//            while (totalBytesRead < endingByte){
//
//                //reads up to chunckSize bytes from this input stream into dataBuffer array of bytes
//                //the start offset in the bytes array is 0
//                //returns number of bytes read into dataBuffer or -1 if no more data to read (end of input stream)
//                bytesRead = in.read(dataBuffer, 0, chunckSize);
//                if(bytesRead == -1) {
//                    break;
//                }
//                totalBytesRead += bytesRead;
//
//                //writes bytesRead bytes from the dataBuffer into this file
//                //the start offset in the file is 0
//                fileToWrite.write(dataBuffer, 0, bytesRead);
//
//                currentPercentage = (int) (totalBytesRead * 100 / fileSize);
//
//                // if still downloading
//                if(currentPercentage > counterPercentage) {
//                    System.out.println("Downloaded " + currentPercentage + "%");
//                    counterPercentage++;
//                }
//
//            }
//            System.out.println("Finished downloading");
//        } catch (Exception e) {
//            System.err.println(e.getMessage());
//        }
//    }
}
