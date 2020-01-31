import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

public class FileWriter implements Runnable{
    private BlockingQueue<Chunk> m_ChunksQueue;
    private Metadata m_Metadata;
    private String m_FileName;
    private int m_FileLength;
    private int m_CurrentPercentage;
    private int m_TotalNumOfChunks;
    private boolean m_IsFirstPercentagePrint;

    public FileWriter(Metadata metadata, BlockingQueue<Chunk> chunksQueue, String fileName, int fileLength, int totalNumOfChunks) {
        m_ChunksQueue = chunksQueue;
        m_Metadata = metadata;
        m_FileName = fileName;
        m_FileLength = fileLength;
        m_TotalNumOfChunks = totalNumOfChunks;
        m_CurrentPercentage = m_Metadata.getNumOfDownloadedChunks() / m_TotalNumOfChunks;
        m_IsFirstPercentagePrint = true;
    }

    public void writeToFile(){

    }

    @Override
    public void run() {
        System.out.println("Writer running...");

        while (m_Metadata.getNumOfDownloadedChunks() < m_TotalNumOfChunks){
            if(!m_ChunksQueue.isEmpty()) {
                Chunk chunk = m_ChunksQueue.poll();
                writeChunkToFile(chunk);
                updateMetadata(chunk);
                printDownloadPercentage();
            }
        }
        m_Metadata.deleteMetaData();
    }

    private void printDownloadPercentage() {
        int numOfDownloadedChunks = m_Metadata.getNumOfDownloadedChunks();
        int newCurrentPercentage = (numOfDownloadedChunks * 100 / m_TotalNumOfChunks);

        // if downloading percentage changed, or it's the first time we print a percentage, change the current percentage and print it
        if(m_IsFirstPercentagePrint || (newCurrentPercentage != m_CurrentPercentage)) {
            m_CurrentPercentage = newCurrentPercentage;
            System.out.println("Downloaded " + m_CurrentPercentage + "%" + " chunk: " + numOfDownloadedChunks);
            m_IsFirstPercentagePrint = false;
        }
    }


    private void updateMetadata(Chunk chunk) {
        m_Metadata.updateChunksBitMap(chunk.getChunkIndex());
    }


    private void writeChunkToFile(Chunk chunk) {
        RandomAccessFile downloadedFile = null;
        try {
            downloadedFile = new RandomAccessFile(m_FileName, "rw");
            downloadedFile.seek(chunk.getOffset());
            downloadedFile.write(chunk.getChunkData());
        } catch (IOException e) {
            System.err.println("Failed to write packet to file");
        }
    }


}
