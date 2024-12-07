// src/Node.java
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private static final int HEADER_SIZE = 24; // Block ID (8) + Parent ID (8) + Number of Keys (8)
    public static final int MAX_KEYS = 19;
    public static final int MAX_CHILDREN = 20;
    private static final int KEY_SIZE = 8; // 8 bytes per key
    private static final int VALUE_SIZE = 8; // 8 bytes per value
    private static final int POINTER_SIZE = 8; // 8 bytes per child pointer

    private long blockId;
    private long parentId;
    private int numKeys;
    private List<Long> keys;
    private List<Long> values;
    private List<Long> children; // Child pointers

    public Node(long blockId, long parentId) {
        this.blockId = blockId;
        this.parentId = parentId;
        this.numKeys = 0;
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
        this.children = new ArrayList<>();
        // Initialize children with zeros
        for (int i = 0; i < MAX_CHILDREN; i++) {
            children.add(0L);
        }
    }

    // Deserialize a node from a byte array
    public static Node fromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        long blockId = buffer.getLong();
        long parentId = buffer.getLong();
        int numKeys = (int) buffer.getLong();

        Node node = new Node(blockId, parentId);
        node.numKeys = numKeys;

        // Read keys
        for (int i = 0; i < MAX_KEYS; i++) {
            long key = buffer.getLong();
            if (i < numKeys) {
                node.keys.add(key);
            }
        }

        // Read values
        for (int i = 0; i < MAX_KEYS; i++) {
            long value = buffer.getLong();
            if (i < numKeys) {
                node.values.add(value);
            }
        }

        // Read child pointers
        for (int i = 0; i < MAX_CHILDREN; i++) {
            long child = buffer.getLong();
            node.children.set(i, child);
        }

        return node;
    }

    // Serialize the node to a byte array
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(FileIO.BLOCK_SIZE);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(blockId);
        buffer.putLong(parentId);
        buffer.putLong((long) numKeys);

        // Write keys
        for (int i = 0; i < MAX_KEYS; i++) {
            if (i < keys.size()) {
                buffer.putLong(keys.get(i));
            } else {
                buffer.putLong(0L);
            }
        }

        // Write values
        for (int i = 0; i < MAX_KEYS; i++) {
            if (i < values.size()) {
                buffer.putLong(values.get(i));
            } else {
                buffer.putLong(0L);
            }
        }

        // Write child pointers
        for (int i = 0; i < MAX_CHILDREN; i++) {
            buffer.putLong(children.get(i));
        }

        // Fill the rest with zeros if necessary
        while (buffer.position() < FileIO.BLOCK_SIZE) {
            buffer.put((byte) 0);
        }

        return buffer.array();
    }

    public boolean isLeaf() {
        for (long child : children) {
            if (child != 0L) {
                return false;
            }
        }
        return true;
    }

    public void insertKey(long key, long value) throws Exception {
        if (keys.contains(key)) {
            throw new Exception("Duplicate key insertion is not allowed.");
        }
        // Find the correct position to insert
        int index = 0;
        while (index < keys.size() && key > keys.get(index)) {
            index++;
        }
        keys.add(index, key);
        values.add(index, value);
        numKeys++;
        if (numKeys > MAX_KEYS) {
            throw new Exception("Node has exceeded maximum number of keys.");
        }
    }

    // Getters and Setters
    public long getBlockId() {
        return blockId;
    }

    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public int getNumKeys() {
        return numKeys;
    }

    public void setNumKeys(int numKeys) {
        this.numKeys = numKeys;
    }

    public List<Long> getKeys() {
        return keys;
    }

    public void setKeys(List<Long> keys) {
        this.keys = keys;
    }

    public List<Long> getValues() {
        return values;
    }

    public void setValues(List<Long> values) {
        this.values = values;
    }

    public List<Long> getChildren() {
        return children;
    }

    public void setChildren(List<Long> children) {
        this.children = children;
    }
}
