import java.io.*;
import java.nio.file.*;
import java.sql.SQLOutput;

public class Metadata implements Serializable {
    public boolean[] m_ChunksBitMap;
    public String m_MetadataFileName;
    private int m_NumOfDownloadedChunks;

    public Metadata(int numOfChunks, String metadataFileName) {
        m_ChunksBitMap = new boolean[numOfChunks]; //initially all values are false
        m_MetadataFileName = metadataFileName;
        m_NumOfDownloadedChunks = 0;
    }

    // TODO: fix
    public static boolean metadataExists(String metadataFileName) {
        File metadataFile = new File(metadataFileName);
        return metadataFile.exists();
    }

    public static Metadata getMetadata(int numOfChunks, String metadataFileName) {
        Metadata metadata;
        //we are resuming an interrupted download
        if (metadataExists(metadataFileName)) {
            metadata = getMetadataFromDisk(metadataFileName);
            System.out.println("METADATA EXISTS - TOOK IT FROM DISK");
        } else {
            metadata = new Metadata(numOfChunks, metadataFileName);
        }
        return metadata;
    }

    public void deleteMetaData(){
        File metadataFile = new File(m_MetadataFileName);
        metadataFile.delete();
    }

    public int getNumOfDownloadedChunks() {
        return m_NumOfDownloadedChunks;
    }

    //deserialization
    // TODO: fix
    private static Metadata getMetadataFromDisk(String metadataFileName) {
        Metadata metadata = null;
        try {
            // Reading the metadata instance from the file
            FileInputStream file = new FileInputStream(metadataFileName);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            metadata = (Metadata) in.readObject();

            in.close();
            file.close();

            System.out.println("Metadata instance has been deserialized ");
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return metadata;
    }

    public void updateChunksBitMap(int chunkIndex) {
        m_ChunksBitMap[chunkIndex] = true;
        updateMetadataOnDisk();
    }

    //serialization to save metadata instance into file
    // TODO: fix
    private void updateMetadataOnDisk() {
        try {
            //create temporary metadata file to store the updated instance, inside the folder from where we run the program
            File tempMetadataFile = File.createTempFile(m_MetadataFileName, ".tmp", new File(System.getProperty("user.dir")));
            FileOutputStream tempFileOutputStream = new FileOutputStream(tempMetadataFile);
            ObjectOutputStream out = new ObjectOutputStream(tempFileOutputStream);

            // Method for serialization of object
            out.writeObject(this);

            out.close();
            tempFileOutputStream.close();

            Path tempPath = Paths.get(tempMetadataFile.getAbsolutePath());

            File metadataFile = new File(m_MetadataFileName).getAbsoluteFile();
            Path metadataPath = Paths.get(metadataFile.getAbsolutePath());

            //atomic move of contents of temp to metadata
            Files.move(tempPath, metadataPath, StandardCopyOption.ATOMIC_MOVE);
            m_NumOfDownloadedChunks++;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}