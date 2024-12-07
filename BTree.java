// src/BTree.java
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BTree {
    private FileIO fileIO;
    private int t; // Minimal degree
    private int maxKeys;
    private Node root;
    private final int CACHE_SIZE = 3;
    private LinkedHashMap<Long, Node> cache;

    public BTree(FileIO fileIO, int minimalDegree) throws IOException {
        this.fileIO = fileIO;
        this.t = minimalDegree;
        this.maxKeys = 2 * t - 1; // For t=10, maxKeys=19
        this.cache = new LinkedHashMap<Long, Node>(CACHE_SIZE, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Long, Node> eldest) {
                if (size() > CACHE_SIZE) {
                    try {
                        writeNode(eldest.getValue());
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

    private void loadRoot() throws IOException {
        long rootBlock = fileIO.getRoot();
        if (rootBlock == 0L) {
            this.root = null;
        } else {
            this.root = readNode(rootBlock);
        }
    }

    public Node getRoot() {
        return this.root;
    }

    public Node readNode(long blockId) throws IOException {
        if (cache.containsKey(blockId)) {
            return cache.get(blockId);
        }
        Node node = Node.fromBytes(fileIO.readBlock(blockId));
        cache.put(blockId, node);
        return node;
    }

    public void writeNode(Node node) throws IOException {
        fileIO.writeBlock(node.getBlockId(), node.toBytes());
        // Optionally, update the cache if needed
    }

    public void insert(long key, long value) throws Exception {
        if (fileIO.getRoot() == 0L) {
            // Tree is empty, create root
            long rootId = fileIO.allocateBlock();
            Node root = new Node(rootId, 0L);
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
                Node newRoot = new Node(newRootId, 0L);
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

    private void insertNonFull(Node node, long key, long value) throws Exception {
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

    private void splitChild(Node parent, int index, Node child) throws IOException {
        long newChildId = fileIO.allocateBlock();
        Node newChild = new Node(newChildId, parent.getBlockId());

        // Move the last t-1 keys and values from child to newChild
        for (int j = t; j < child.getKeys().size(); j++) {
            newChild.getKeys().add(child.getKeys().get(j));
            newChild.getValues().add(child.getValues().get(j));
        }
        newChild.setNumKeys(child.getNumKeys() - t);
        for (int j = t; j < child.getKeys().size(); j++) {
            child.getKeys().remove(t);
            child.getValues().remove(t);
        }
        child.setNumKeys(t - 1);

        // If child is not leaf, move the last t children to newChild
        if (!child.isLeaf()) {
            for (int j = t; j < child.getChildren().size(); j++) {
                newChild.getChildren().set(j - t, child.getChildren().get(j));
                child.getChildren().set(j, 0L);
            }
        }

        // Insert the middle key into the parent
        parent.getKeys().add(index, child.getKeys().remove(t - 1));
        parent.getValues().add(index, child.getValues().remove(t - 1));
        parent.getChildren().add(index + 1, newChildId);
        parent.setNumKeys(parent.getNumKeys() + 1);

        // Write the updated nodes to disk
        writeNode(child);
        writeNode(newChild);
        writeNode(parent);

        cache.put(newChildId, newChild); // Add new child to cache
        System.out.println("Split child " + child.getBlockId() + " into " + child.getBlockId() + " and " + newChildId + ".");
    }

    public Long search(long key) throws IOException {
        return searchRecursive(root, key);
    }

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

    // Implement other methods like print, load, extract here
}
