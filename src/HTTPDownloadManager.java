import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

public class HTTPDownloadManager {
    //private static List<URL> m_ListOfURLs;
    private String m_FileURL;
    private static int m_NumOfConnections;
    private String m_FileName;
    private int m_FileLength;
    private Thread[] m_Threads;

    public HTTPDownloadManager(String fileURL, int numOfConnections){
        m_FileURL = fileURL;
        m_NumOfConnections = numOfConnections;
    }

    public void setFileName(String fileURL) {
        m_FileName = fileURL.substring(fileURL.lastIndexOf('/') + 1);
    }

    public void setFileLength() {
        //for (URL downloadUrl : m_ListOfURLs) {
            URL downloadUrl = null;
            HttpURLConnection conn = null;
            try {
                downloadUrl = new URL(m_FileURL);
                conn = (HttpURLConnection) downloadUrl.openConnection();
                m_FileLength = conn.getContentLength();
                System.out.println("File size is: " + m_FileLength / 1024 + "KB");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    //}

    public void setNumOfConnections(int numOfConnections){
        m_NumOfConnections = numOfConnections;
    }

    public void startDownload() {
        setFileName(m_FileURL);
        setFileLength();
        setNumOfConnections(m_NumOfConnections);

        Metadata metadata = null;
        LinkedBlockingQueue<Chunk> chunkQueue = new LinkedBlockingQueue<Chunk>();
        Thread fileWriter = new Thread(new FileWriter(metadata, chunkQueue));
        fileWriter.start();
        m_Threads = new Thread[m_NumOfConnections];

        if (Metadata.exists(m_FileURL))
        {
            metadata = metadata.existingMetadata(m_FileURL);
        } else {
            metadata = new Metadata(m_FileURL);
        }

        int i;
        int offset = 0;
        int chunkLength = m_FileLength / m_NumOfConnections ;

        for (i = 0; i < m_NumOfConnections - 1; i++){
            HTTPRangeGetter rangeGetter = new HTTPRangeGetter(chunkLength, offset, i, m_FileURL, m_FileName);
            m_Threads[i] = new Thread(rangeGetter);
            offset += chunkLength;
        }

        HTTPRangeGetter rangeGetter = new HTTPRangeGetter(((m_FileLength % m_NumOfConnections) + chunkLength), offset, i, m_FileURL, m_FileName); //last one gets remainder of the file
        m_Threads[i] = new Thread(rangeGetter);

    }

}
