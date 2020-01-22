public class Chunk {
    public byte[] m_ChunkData;
    public long m_Offset;
    public int m_ChunkIndex;

    Chunk(byte[] data, long offset, int chunkIndex) {
        m_ChunkIndex = chunkIndex;
        m_ChunkData = data;
        m_Offset = offset;
    }

    public long getOffset(){
        return m_Offset;
    }

    public byte[] getChunkData(){
        return m_ChunkData;
    }

    public int getChunkIndex(){
        return m_ChunkIndex;
    }
}
