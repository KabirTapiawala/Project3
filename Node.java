// src/Node.java
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private long blockId;
    private long parentId;
    private int numKeys;
    private List<Long> keys;
    private List<Long> values;
    private List<Long> children; // Child pointers
    private int t; // Minimal degree

    /**
     * Constructs a new Node with the given block ID, parent ID, and minimal degree.
     *
     * @param blockId  The unique block ID of the node.
     * @param parentId The block ID of the parent node.
     * @param t        The minimal degree of the B-Tree.
     */
    public Node(long blockId, long parentId, int t) {
        this.blockId = blockId;
        this.parentId = parentId;
        this.t = t;
        this.numKeys = 0;
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
        this.children = new ArrayList<>();
        // Initialize children with zeros based on t
        for (int i = 0; i < 2 * t; i++) {
            children.add(0L);
        }
    }

    /**
     * Deserializes a node from a byte array.
     *
     * @param data The byte array representing the node.
     * @param t    The minimal degree of the B-Tree.
     * @return The Node object.
     */
    public static Node fromBytes(byte[] data, int t) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        long blockId = buffer.getLong();
        long parentId = buffer.getLong();
        int numKeys = (int) buffer.getLong(); // Assuming numKeys is stored as a long

        Node node = new Node(blockId, parentId, t);
        node.numKeys = numKeys;

        // Read keys
        for (int i = 0; i < 2 * t - 1; i++) {
            long key = buffer.getLong();
            if (i < numKeys && key != 0L) { // Assuming 0L is unused
                node.keys.add(key);
            }
        }

        // Read values
        for (int i = 0; i < 2 * t - 1; i++) {
            long value = buffer.getLong();
            if (i < numKeys && value != 0L) { // Assuming 0L is unused
                node.values.add(value);
            }
        }

        // Read child pointers
        for (int i = 0; i < 2 * t; i++) {
            long child = buffer.getLong();
            if (i < node.children.size()) {
                node.children.set(i, child);
            } else {
                node.children.add(child);
            }
        }

        return node;
    }

    /**
     * Serializes the node to a byte array.
     *
     * @return The byte array representing the node.
     */
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(FileIO.BLOCK_SIZE);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(blockId);
        buffer.putLong(parentId);
        buffer.putLong((long) numKeys);

        // Write keys
        for (int i = 0; i < 2 * t - 1; i++) {
            if (i < keys.size()) {
                buffer.putLong(keys.get(i));
            } else {
                buffer.putLong(0L);
            }
        }

        // Write values
        for (int i = 0; i < 2 * t - 1; i++) {
            if (i < values.size()) {
                buffer.putLong(values.get(i));
            } else {
                buffer.putLong(0L);
            }
        }

        // Write child pointers
        for (int i = 0; i < 2 * t; i++) {
            buffer.putLong(children.get(i));
        }

        // Fill the rest with zeros if necessary
        while (buffer.position() < FileIO.BLOCK_SIZE) {
            buffer.put((byte) 0);
        }

        return buffer.array();
    }

    /**
     * Determines if the node is a leaf node.
     *
     * @return True if the node is a leaf, false otherwise.
     */
    public boolean isLeaf() {
        for (long child : children) {
            if (child != 0L) {
                return false;
            }
        }
        return true;
    }

    /**
     * Inserts a key-value pair into the node in sorted order.
     *
     * @param key   The key to insert.
     * @param value The value associated with the key.
     * @throws Exception If the key already exists or the node exceeds its capacity.
     */
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
        if (numKeys > 2 * t - 1) {
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
