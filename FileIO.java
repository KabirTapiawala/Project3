// src/FileIO.java
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileIO {
    public static final int BLOCK_SIZE = 512;
    public static final byte[] MAGIC_NUMBER = "4337PRJ3".getBytes();
    private static final int HEADER_SIZE = 24; // 8 bytes magic, 8 bytes root ID, 8 bytes next block ID

    private RandomAccessFile file;
    private String filename;
    private long rootBlock;
    private long nextBlock;

    public FileIO() {
        this.file = null;
        this.filename = null;
        this.rootBlock = 0;
        this.nextBlock = 1; // Start allocating from block 1 since block 0 is header
    }

    public boolean createFile(String filename, boolean overwrite) throws IOException {
        File f = new File(filename);
        if (f.exists()) {
            if (!overwrite) {
                System.out.print("File '" + filename + "' already exists. Overwrite? (y/n): ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String response = reader.readLine().trim().toLowerCase();
                if (!response.equals("y")) {
                    System.out.println("Create command aborted.");
                    return false;
                }
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.setLength(0); // Truncate the file
            ByteBuffer header = ByteBuffer.allocate(BLOCK_SIZE);
            header.order(ByteOrder.BIG_ENDIAN);
            header.put(MAGIC_NUMBER);
            header.putLong(0L); // Root block ID
            header.putLong(1L); // Next block ID
            // Fill the rest of the header with zeros
            while (header.position() < BLOCK_SIZE) {
                header.put((byte) 0);
            }
            raf.write(header.array());
        }

        openFile(filename);
        System.out.println("Created new index file '" + filename + "'.");
        return true;
    }

    public boolean openFileFunc(String filename) throws IOException {
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("Error: File '" + filename + "' does not exist.");
            return false;
        }

        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            byte[] magic = new byte[8];
            raf.readFully(magic);
            for (int i = 0; i < 8; i++) {
                if (magic[i] != MAGIC_NUMBER[i]) {
                    System.out.println("Error: Invalid magic number. Not a valid index file.");
                    return false;
                }
            }
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.order(ByteOrder.BIG_ENDIAN);
            raf.readFully(buffer.array());
            rootBlock = buffer.getLong();
            nextBlock = buffer.getLong();
        }

        openFile(filename);
        System.out.println("Opened index file '" + filename + "'.");
        return true;
    }

    private void openFile(String filename) throws IOException {
        closeFile(); // Close any previously open file
        this.file = new RandomAccessFile(filename, "rw");
        this.filename = filename;
    }

    public void closeFile() throws IOException {
        if (this.file != null) {
            this.file.close();
            this.file = null;
            this.filename = null;
            System.out.println("Closed the current index file.");
        }
    }

    public byte[] readBlock(long blockId) throws IOException {
        if (file == null) {
            throw new IOException("No file is currently open.");
        }
        byte[] data = new byte[BLOCK_SIZE];
        file.seek(blockId * BLOCK_SIZE);
        int bytesRead = file.read(data);
        if (bytesRead < BLOCK_SIZE) {
            throw new IOException("Error: Could not read block " + blockId + ".");
        }
        return data;
    }

    public void writeBlock(long blockId, byte[] data) throws IOException {
        if (file == null) {
            throw new IOException("No file is currently open.");
        }
        if (data.length > BLOCK_SIZE) {
            throw new IOException("Error: Data exceeds block size.");
        }
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(data);
        while (buffer.position() < BLOCK_SIZE) {
            buffer.put((byte) 0);
        }
        file.seek(blockId * BLOCK_SIZE);
        file.write(buffer.array());
    }

    public long allocateBlock() throws IOException {
        long blockId = nextBlock;
        nextBlock++;
        updateHeader();
        return blockId;
    }

    public void updateHeader() throws IOException {
        if (file == null) {
            throw new IOException("No file is currently open.");
        }
        ByteBuffer header = ByteBuffer.allocate(BLOCK_SIZE);
        header.order(ByteOrder.BIG_ENDIAN);
        header.put(MAGIC_NUMBER);
        header.putLong(rootBlock);
        header.putLong(nextBlock);
        // Fill the rest of the header with zeros
        while (header.position() < BLOCK_SIZE) {
            header.put((byte) 0);
        }
        file.seek(0);
        file.write(header.array());
    }

    public void setRoot(long blockId) throws IOException {
        this.rootBlock = blockId;
        updateHeader();
    }

    public long getRoot() {
        return this.rootBlock;
    }

    public long getNextBlock() {
        return this.nextBlock;
    }

    public String getFilename() {
        return this.filename;
    }

    @Override
    protected void finalize() throws Throwable {
        closeFile();
        super.finalize();
    }
}
