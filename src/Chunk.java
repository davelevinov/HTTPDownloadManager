public class Chunk {
    public byte[] m_ChunkData;
    public long m_StartPosition;
    public int m_ChunkIndex;

    Chunk(byte[] data, long startPosition, int chunkIndex) {
        m_ChunkIndex = chunkIndex;
        m_ChunkData = data;
        m_StartPosition = startPosition;
    }

    public long getStartPosition(){
        return m_StartPosition;
    }

    public byte[] getChunkData(){
        return m_ChunkData;
    }

    public int getChunkIndex(){
        return m_ChunkIndex;
    }
}
