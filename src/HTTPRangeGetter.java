import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HTTPRangeGetter implements Runnable {
    private String m_FileURL;
    private String m_FileName;
    private int m_CurrentPosition;
    private long m_EndPosition;
    private int m_ThreadID;
    private String m_byteRange;
    public int CHUNK_SIZE = 40960;
    private LinkedBlockingQueue<Chunk> m_ChunksQueue;
    private Metadata m_Metadata;

    public HTTPRangeGetter(int startPosition, long endPosition, int threadID, String fileURL, String fileName,
                           LinkedBlockingQueue<Chunk> chunksQueue, Metadata metadata) {
        m_FileURL = fileURL;
        m_CurrentPosition = startPosition;
        m_EndPosition = endPosition;
        m_ThreadID = threadID;
        m_FileName = fileName;
        m_ChunksQueue = chunksQueue;
        m_Metadata = metadata;
        m_byteRange = String.format("Bytes=%d-%d", startPosition, endPosition);
    }

    public void run() {
        HttpURLConnection conn = null;
        try {
            URL downloadUrl = new URL(m_FileURL);
            conn = (HttpURLConnection) downloadUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Range", m_byteRange);
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            int delta;
            int bytesToRead;
            int chunkIndex = m_CurrentPosition / CHUNK_SIZE;
            System.out.println("Start downloading range (" + m_CurrentPosition + " - " +
                    +m_EndPosition + ") from: " + m_FileURL);
            while (m_CurrentPosition < m_EndPosition) {
                delta = (int) m_EndPosition - m_CurrentPosition;
                bytesToRead = Math.min(delta, CHUNK_SIZE);
                if (!m_Metadata.m_ChunksArray[chunkIndex]) {
                    byte[] dataBuffer = new byte[bytesToRead];
                    in.readNBytes(dataBuffer, 0, bytesToRead);
                    Chunk chunk = new Chunk(dataBuffer, m_CurrentPosition, chunkIndex);
                    m_ChunksQueue.put(chunk);
                }
                chunkIndex++;
                m_CurrentPosition += CHUNK_SIZE;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
