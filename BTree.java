// src/BTree.java
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BTree {
    public static final int BLOCK_SIZE = 512;
    private int t; // Minimal degree
    private int maxKeys;
    private FileIO fileIO;
    private Node root;
    private final int CACHE_SIZE = 3;
    private LinkedHashMap<Long, Node> cache;

    /**
     * Constructs a new BTree with the given FileIO and minimal degree.
     *
     * @param fileIO        The FileIO instance for file operations.
     * @param minimalDegree The minimal degree (t) of the B-Tree.
     * @throws IOException If an I/O error occurs during initialization.
     */
    public BTree(FileIO fileIO, int minimalDegree) throws IOException {
        this.fileIO = fileIO;
        this.t = minimalDegree;
        this.maxKeys = 2 * t - 1; // For t=10, maxKeys=19
        this.cache = new LinkedHashMap<Long, Node>(CACHE_SIZE, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Long, Node> eldest) {
                if (size() > CACHE_SIZE) {
                    try {
                        writeNode(eldest.getValue());
                        System.out.println("Evicted node " + eldest.getKey() + " from cache.");
                    } catch (IOException e) {
                        System.out.println("Error writing node to disk: " + e.getMessage());
                    }
                    return true;
                }
                return false;
            }
        };
        loadRoot();
    }

    /**
     * Loads the root node from the file. If no root exists, sets root to null.
     *
     * @throws IOException If an I/O error occurs during reading.
     */
    private void loadRoot() throws IOException {
        long rootBlock = fileIO.getRoot();
        if (rootBlock == 0L) {
            this.root = null;
        } else {
            this.root = readNode(rootBlock);
        }
    }

    /**
     * Returns the root node of the B-Tree.
     *
     * @return The root Node.
     */
    public Node getRoot() {
        return this.root;
    }

    /**
     * Reads a node from disk or cache.
     *
     * @param blockId The block ID of the node to read.
     * @return The Node object.
     * @throws IOException If an I/O error occurs during reading.
     */
    public Node readNode(long blockId) throws IOException {
        if (cache.containsKey(blockId)) {
            return cache.get(blockId);
        }
        byte[] data = fileIO.readBlock(blockId);
        Node node = Node.fromBytes(data, this.t); // Pass 't' here
        cache.put(blockId, node);
        return node;
    }

    /**
     * Writes a node to disk and updates the cache.
     *
     * @param node The Node object to write.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void writeNode(Node node) throws IOException {
        fileIO.writeBlock(node.getBlockId(), node.toBytes());
        cache.put(node.getBlockId(), node); // Update cache with the latest node data
        System.out.println("Written node " + node.getBlockId() + " to disk and updated cache.");
    }

    /**
     * Inserts a key-value pair into the B-Tree.
     *
     * @param key   The key to insert.
     * @param value The value associated with the key.
     * @throws Exception If the key is a duplicate or other insertion errors occur.
     */
    public void insert(long key, long value) throws Exception {
        System.out.println("Attempting to insert key: " + key + " with value: " + value);
        // Perform a global search to check for duplicates
        if (search(key) != null) {
            System.out.println("Duplicate key " + key + " found. Insertion aborted.");
            throw new Exception("Duplicate key insertion is not allowed.");
        }

        if (fileIO.getRoot() == 0L) {
            // Tree is empty, create root
            long rootId = fileIO.allocateBlock();
            Node root = new Node(rootId, 0L, this.t);
            root.insertKey(key, value);
            writeNode(root);
            fileIO.setRoot(rootId);
            this.root = root;
            cache.put(rootId, root); // Add to cache
            System.out.println("Inserted key " + key + " as root.");
        } else {
            Node currentRoot = readNode(fileIO.getRoot());
            if (currentRoot.getNumKeys() == maxKeys) {
                // Root is full, need to split
                long newRootId = fileIO.allocateBlock();
                Node newRoot = new Node(newRootId, 0L, this.t);
                newRoot.getChildren().set(0, currentRoot.getBlockId());
                writeNode(newRoot);

                splitChild(newRoot, 0, currentRoot);

                fileIO.setRoot(newRootId);
                this.root = newRoot;
                cache.put(newRootId, newRoot); // Add to cache

                insertNonFull(newRoot, key, value);
                System.out.println("Inserted key " + key + " into new root.");
            } else {
                insertNonFull(currentRoot, key, value);
            }
        }
    }

    /**
     * Inserts a key-value pair into a node that is not full.
     *
     * @param node  The node to insert into.
     * @param key   The key to insert.
     * @param value The value associated with the key.
     * @throws Exception If insertion errors occur.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void insertNonFull(Node node, long key, long value) throws Exception, IOException {
        if (node.isLeaf()) {
            // Insert the key into the leaf node
            node.insertKey(key, value);
            writeNode(node);
            System.out.println("Inserted key " + key + " into leaf node " + node.getBlockId() + ".");
        } else {
            int i = node.getKeys().size() - 1;
            while (i >= 0 && key < node.getKeys().get(i)) {
                i--;
            }
            i++;
            long childId = node.getChildren().get(i);
            if (childId == 0L) {
                throw new Exception("Invalid child pointer.");
            }
            Node child = readNode(childId);
            if (child.getNumKeys() == maxKeys) {
                splitChild(node, i, child);
                if (key > node.getKeys().get(i)) {
                    i++;
                }
                child = readNode(node.getChildren().get(i));
            }
            insertNonFull(child, key, value);
        }
    }

    /**
     * Splits a child node into two nodes.
     *
     * @param parent The parent node.
     * @param index  The index of the child to split.
     * @param child  The child node to split.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void splitChild(Node parent, int index, Node child) throws IOException {
        System.out.println("Splitting child node " + child.getBlockId() + " at index " + index + " of parent node " + parent.getBlockId());

        long newChildId = fileIO.allocateBlock();
        Node newChild = new Node(newChildId, parent.getBlockId(), this.t);

        // Move the last t-1 keys and values from child to newChild
        for (int j = child.getKeys().size() - 1; j >= this.t; j--) {
            newChild.getKeys().add(0, child.getKeys().get(j)); // Prepend to maintain order
            newChild.getValues().add(0, child.getValues().get(j));
            child.getKeys().remove(j);
            child.getValues().remove(j);
        }
        newChild.setNumKeys(child.getNumKeys() - this.t);
        child.setNumKeys(this.t - 1);

        // If child is not leaf, move the last t children to newChild
        if (!child.isLeaf()) {
            for (int j = child.getChildren().size() - 1; j >= this.t; j--) {
                long movedChildId = child.getChildren().get(j);
                newChild.getChildren().set(j - this.t, movedChildId);
                child.getChildren().set(j, 0L);

                // Update the parentId of the moved child node to newChildId
                if (movedChildId != 0L) {
                    Node movedChild = readNode(movedChildId);
                    movedChild.setParentId(newChildId);
                    writeNode(movedChild);
                }
            }
        }

        // Insert the middle key into the parent
        long middleKey = child.getKeys().remove(this.t - 1);
        long middleValue = child.getValues().remove(this.t - 1);
        parent.getKeys().add(index, middleKey);
        parent.getValues().add(index, middleValue);
        parent.getChildren().add(index + 1, newChildId);
        parent.setNumKeys(parent.getNumKeys() + 1);

        // Update the parentId of the original child node to parent.getBlockId()
        child.setParentId(parent.getBlockId());
        writeNode(child);

        // Write the updated nodes to disk
        writeNode(newChild);
        writeNode(parent);

        System.out.println("Split child " + child.getBlockId() + " into " + child.getBlockId() + " and " + newChildId + ".");
        System.out.println("Parent node " + parent.getBlockId() + " now has keys: " + parent.getKeys());
    }

    /**
     * Searches for a key in the B-Tree.
     *
     * @param key The key to search for.
     * @return The value associated with the key, or null if not found.
     * @throws IOException If an I/O error occurs during searching.
     */
    public Long search(long key) throws IOException {
        return searchRecursive(root, key);
    }

    /**
     * Recursively searches for a key starting from a given node.
     *
     * @param node The node to start searching from.
     * @param key  The key to search for.
     * @return The value associated with the key, or null if not found.
     * @throws IOException If an I/O error occurs during searching.
     */
    private Long searchRecursive(Node node, long key) throws IOException {
        if (node == null) {
            return null;
        }
        int i = 0;
        while (i < node.getKeys().size() && key > node.getKeys().get(i)) {
            i++;
        }
        if (i < node.getKeys().size() && key == node.getKeys().get(i)) {
            return node.getValues().get(i);
        }
        if (node.isLeaf()) {
            return null;
        }
        long childId = node.getChildren().get(i);
        if (childId == 0L) {
            return null;
        }
        Node child = readNode(childId);
        return searchRecursive(child, key);
    }

    /**
     * Closes the B-Tree by closing its FileIO.
     *
     * @throws IOException If an I/O error occurs during closing.
     */
    public void close() throws IOException {
        fileIO.closeFile();
    }

}
