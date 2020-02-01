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
    public int CHUNK_SIZE = 243345;
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
    }


    public void run() {
        HttpURLConnection conn = null;
        try {
            int chunkIndex = (int)Math.ceil(m_CurrentPosition / CHUNK_SIZE);
            int delta, bytesToRead = 0;
            URL downloadUrl = new URL(m_FileURL);
            // iterates and moves the starting position if chunk has been read from previous download
            while (m_Metadata.m_ChunksArray[chunkIndex]){
                m_CurrentPosition += CHUNK_SIZE;
                chunkIndex++;
            }
            conn = (HttpURLConnection) downloadUrl.openConnection();
            conn.setRequestMethod("GET");
            m_byteRange = String.format("Bytes=%d-%d", m_CurrentPosition, m_EndPosition);
            conn.setRequestProperty("Range", m_byteRange);
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            System.err.println("[" + m_ThreadID + "]" + " Start downloading range (" + m_CurrentPosition +
                    " - " + m_EndPosition + ") from: " + m_FileURL);
            while (m_CurrentPosition < m_EndPosition) {
                delta = (int) m_EndPosition - m_CurrentPosition;
                bytesToRead = Math.min(delta, CHUNK_SIZE);
                if (!m_Metadata.m_ChunksArray[chunkIndex]) {
                    byte[] dataBuffer = new byte[bytesToRead];
                    in.readNBytes(dataBuffer, 0, bytesToRead);
                    Chunk chunk = new Chunk(dataBuffer, m_CurrentPosition, chunkIndex);
                    m_ChunksQueue.add(chunk);
                }
                chunkIndex++;
                m_CurrentPosition += bytesToRead;
            }
        } catch (IOException ex) {
            conn.disconnect();
        }
    }
}
