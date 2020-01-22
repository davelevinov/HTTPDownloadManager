import java.io.IOException;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class HTTPDownloadManager {
    //private static List<URL> m_ListOfURLs;
    private String m_FileURL;
    private int m_NumOfConnections;
    private String m_FileName;
    private int m_FileLength;
    private Thread[] m_Threads;
    public Metadata m_Metadata;
    public int m_TotalNumOfChunks;
    public static final int CHUNK_SIZE = 4096;
    public static final String METADATA_NAME_EXTENSION = "_metadata";
    private double m_NumOfChunksPerThread;
    private double m_RangeSize;
    private LinkedBlockingQueue<Chunk> m_ChunksQueue;

    public HTTPDownloadManager(String fileURL, int numOfConnections){
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
                System.out.println("File size is: " + fileLength / 1024 + "KB");
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return fileLength;
        }
    //}

    public int getTotalNumOfChunks(){
        int numOfChunks = (int) (m_FileLength / CHUNK_SIZE);
        //we check if there is a remainder for the last chunk
        if (m_FileLength % CHUNK_SIZE != 0) {
            numOfChunks++;
        }
        return numOfChunks;
    }

    
    public void startDownload() {
        //will set metadata file to be existent one or new one
        initMetadata();
        initRangeGetters();
        startWriter();
        startRangeGetters();
    }

    private void startRangeGetters() {
        for(int i = 0; i < m_Threads.length; i++){
            m_Threads[i].start();
        }
    }

    private void startWriter() {
        Thread fileWriter = new Thread(new FileWriter(m_Metadata, m_ChunksQueue, m_FileName, m_FileLength, m_TotalNumOfChunks));
        fileWriter.start();
    }

    private void initRangeGetters() {
        int i;
        int offset = 0;
        //We fill up the array of threads using rangeGetters, which will each do rangeGetter.run() and download a range of bytes
        for (i = 0; i < m_Threads.length - 1; i++){
            HTTPRangeGetter rangeGetter = new HTTPRangeGetter(m_RangeSize, offset, i, m_FileURL, m_FileName, m_NumOfChunksPerThread, m_ChunksQueue);
            m_Threads[i] = new Thread(rangeGetter);
            offset += m_RangeSize; //the offset jumps by the number of bytes each thread needs to read
        }

        //last rangeGetter gets the remainder of the file
        double remainderOfChunksToRead = m_TotalNumOfChunks % m_NumOfConnections;
        HTTPRangeGetter rangeGetter = new HTTPRangeGetter(remainderOfChunksToRead*CHUNK_SIZE, offset, i, m_FileURL, m_FileName, remainderOfChunksToRead, m_ChunksQueue);
        m_Threads[i] = new Thread(rangeGetter);
    }

    private double getNumOfChunksPerThread() {
        return Math.floor((m_TotalNumOfChunks/m_NumOfConnections));
    }

    public double getRangeSize(){
        return CHUNK_SIZE*m_NumOfChunksPerThread;
    }

    public void initMetadata() {
        m_Metadata = Metadata.getMetadata(m_TotalNumOfChunks, m_FileName + METADATA_NAME_EXTENSION);
    }
}
