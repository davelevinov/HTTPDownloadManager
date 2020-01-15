public class Chunk {
    private byte[] m_Data;
    private long m_Offset;
    private int m_LengthInBytes;

    Chunk(byte[] data, long offset, int lengthInBytes) {
        m_Data = data != null ? data.clone() : null;
        m_Offset = offset;
        m_LengthInBytes = lengthInBytes;
    }
}
