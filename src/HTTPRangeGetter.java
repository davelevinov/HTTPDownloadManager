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
            int chunkIndex = (int)Math.ceil(m_CurrentPosition / CHUNK_SIZE);
            int howManyWereRead = 0;
            int howManyZeros = 0;
            System.err.println("[" + m_ThreadID + "]" + " Start downloading range (" + m_CurrentPosition +
                                " - " + m_EndPosition + ") from: " + m_FileURL);
            System.out.println("range for this thread: " + m_ThreadID + " " + m_byteRange);
            while (m_CurrentPosition < m_EndPosition) {
                delta = (int) m_EndPosition - m_CurrentPosition;
                bytesToRead = Math.min(delta, CHUNK_SIZE);
                if (!m_Metadata.m_ChunksArray[chunkIndex]) {
                    byte[] dataBuffer = new byte[bytesToRead];
                    howManyWereRead = in.readNBytes(dataBuffer, 0, bytesToRead);
                    System.out.println("read this amount of bytes: " + howManyWereRead);
                    System.out.println("buffer array size " + dataBuffer.length + " for chunk: " + chunkIndex);
                    for (int i = 0; i < dataBuffer.length; i++) {
                        if (dataBuffer[i] == 0) howManyZeros++;
                    }
                    if (howManyZeros > 1000) System.out.println("number of zeros is: " + howManyZeros +
                            " for chunk number:" + chunkIndex);
                    Chunk chunk = new Chunk(dataBuffer, m_CurrentPosition, chunkIndex);
                    System.out.println("chunk's current position: " + m_CurrentPosition);
                    m_ChunksQueue.add(chunk);
                } else {
                    in.skip(bytesToRead);
                    System.out.println("I skipped in the input stream: " + bytesToRead + " bytes");
                }
                chunkIndex++;
                m_CurrentPosition += bytesToRead;
            }
        } catch (IOException ex) {
            conn.disconnect();
            ex.printStackTrace();
        }
    }
}
