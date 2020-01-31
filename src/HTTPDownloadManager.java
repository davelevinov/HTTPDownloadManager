import java.io.IOException;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class HTTPDownloadManager {
    //private static List<URL> m_ListOfURLs;
    private String m_FileURL;
    private int m_NumOfConnections;
    private String m_FileName;
    private long m_FileLength;
    private Thread[] m_Threads;
    private Metadata m_Metadata;
    private int m_TotalNumOfChunks;
    private static final int CHUNK_SIZE = 40960;
    private static final String METADATA_NAME_EXTENSION = "_metadata";
    private double m_NumOfChunksPerThread;
    private long m_RangeSize;
    private LinkedBlockingQueue<Chunk> m_ChunksQueue;

    public HTTPDownloadManager(String fileURL, int numOfConnections) {
        m_FileURL = fileURL;
        m_NumOfConnections = numOfConnections;
        m_ChunksQueue = new LinkedBlockingQueue<Chunk>();
        m_Threads = new Thread[m_NumOfConnections];
        m_FileName = getFileName(m_FileURL);
        m_FileLength = getFileLength();
        m_TotalNumOfChunks = getTotalNumOfChunks();
        m_NumOfChunksPerThread = getNumOfChunksPerThread();
        m_RangeSize = getRangeSize();
    }

    public String getFileName(String fileURL) {
        return fileURL.substring(fileURL.lastIndexOf('/') + 1);
    }

    public int getFileLength() {
        //for (URL downloadUrl : m_ListOfURLs) {
        URL downloadUrl = null;
        HttpURLConnection conn = null;
        int fileLength;
        try {
            downloadUrl = new URL(m_FileURL);
            conn = (HttpURLConnection) downloadUrl.openConnection();
            fileLength = conn.getContentLength();
        }
        // TODO: decide what happens on error
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return fileLength;
    }

    private int getTotalNumOfChunks() {
        int numOfChunks = (int) (m_FileLength / CHUNK_SIZE);
        //we check if there is a remainder for the last chunk
        if (m_FileLength % CHUNK_SIZE != 0) {
            numOfChunks++;
        }
        return numOfChunks;
    }

    private void initRangeGetters() {
        int threadId = 0;
        int startPosition = 0;
        if (m_NumOfConnections == 1) {
            HTTPRangeGetter rangeGetter = new HTTPRangeGetter(startPosition, m_FileLength, threadId,
                    m_FileURL, m_FileName, m_NumOfChunksPerThread, m_ChunksQueue, m_Metadata);
            m_Threads[0] = new Thread(rangeGetter);
        } else {
            for (threadId = 0; threadId < m_Threads.length - 1; threadId++) {
                System.out.println(startPosition);
                HTTPRangeGetter rangeGetter = new HTTPRangeGetter(startPosition, startPosition + m_RangeSize, threadId,
                        m_FileURL, m_FileName, m_NumOfChunksPerThread, m_ChunksQueue, m_Metadata);
                m_Threads[threadId] = new Thread(rangeGetter);
                //the startPosition jumps by the number of bytes each thread needs to read
                startPosition += m_RangeSize;
            }
            // last rangeGetter gets the remainder of the file
            System.out.println(startPosition);
            HTTPRangeGetter rangeGetter = new HTTPRangeGetter(startPosition, m_FileLength, threadId,
                    m_FileURL, m_FileName, m_NumOfChunksPerThread, m_ChunksQueue, m_Metadata);
            m_Threads[threadId] = new Thread(rangeGetter);
        }
    }

    private void startRangeGetters() {
        for (int i = 0; i < m_Threads.length; i++) {
            m_Threads[i].start();
        }
    }

    private void startWriter() {
        Thread fileWriter = new Thread(new FileWriter(m_Metadata, m_ChunksQueue, m_FileName,
                m_FileLength, m_TotalNumOfChunks));
        fileWriter.start();
    }

    protected void startDownload() {
        //will set metadata file to be existent one or new one
        initMetadata();
        initRangeGetters();
        startRangeGetters();
        startWriter();
    }

    private double getNumOfChunksPerThread() {
        return Math.floor((m_TotalNumOfChunks / m_NumOfConnections));
    }

    private long getRangeSize() {
        return (long) (CHUNK_SIZE * m_NumOfChunksPerThread);
    }

    private void initMetadata() {
        m_Metadata = Metadata.getMetadata(m_TotalNumOfChunks, m_FileName + METADATA_NAME_EXTENSION);
    }
}
