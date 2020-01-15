import java.util.concurrent.BlockingQueue;

public class FileWriter implements Runnable{
    private BlockingQueue<Chunk> m_Chunks;
    private Metadata m_Metadata;

    public FileWriter(Metadata metadata, BlockingQueue<Chunk> chunkQueue) {
        m_Chunks = chunkQueue;
        m_Metadata = metadata;
    }

    public void writeToFile(){

    }

    @Override
    public void run() {

    }
}
