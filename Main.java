// src/Main.java
import java.io.*;
import java.util.*;

public class Main {
    private static final int MINIMAL_DEGREE = 10; // Set t=10 as per requirements

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileIO fileIO = new FileIO();
        BTree btree = null;

        while (true) {
            printMenu();
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "create":
                case "1":
                    if (btree != null) {
                        System.out.println("An index file is already open. Please close it before creating a new one.");
                        break;
                    }
                    System.out.print("Enter new index file name: ");
                    String createFilename = scanner.nextLine().trim();
                    try {
                        boolean created = fileIO.createFile(createFilename, true);
                        if (created) {
                            btree = new BTree(fileIO, MINIMAL_DEGREE);
                            System.out.println("Index file '" + createFilename + "' created and B-Tree initialized.");
                        } else {
                            System.out.println("Failed to create index file.");
                        }
                    } catch (IOException e) {
                        System.out.println("Error creating index file: " + e.getMessage());
                    }
                    break;

                case "open":
                case "2":
                    if (btree != null) {
                        System.out.println("An index file is already open. Please close it before opening another one.");
                        break;
                    }
                    System.out.print("Enter index file name to open: ");
                    String openFilename = scanner.nextLine().trim();
                    try {
                        boolean opened = fileIO.openFileFunc(openFilename);
                        if (opened) {
                            btree = new BTree(fileIO, MINIMAL_DEGREE);
                            System.out.println("Index file '" + openFilename + "' opened and B-Tree initialized.");
                        } else {
                            System.out.println("Failed to open index file.");
                        }
                    } catch (IOException e) {
                        System.out.println("Error opening index file: " + e.getMessage());
                    }
                    break;

                case "insert":
                case "3":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                        break;
                    }
                    System.out.print("Enter key (unsigned integer): ");
                    String keyStr = scanner.nextLine().trim();
                    System.out.print("Enter value (unsigned integer): ");
                    String valueStr = scanner.nextLine().trim();
                    try {
                        long key = Long.parseUnsignedLong(keyStr);
                        long value = Long.parseUnsignedLong(valueStr);
                        btree.insert(key, value);
                        System.out.println("Inserted key " + key + " with value " + value + ".");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter valid unsigned integers for key and value.");
                    } catch (Exception e) {
                        System.out.println("Error inserting key-value pair: " + e.getMessage());
                    }
                    break;

                case "search":
                case "4":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                        break;
                    }
                    System.out.print("Enter key to search: ");
                    String searchKeyStr = scanner.nextLine().trim();
                    try {
                        long searchKey = Long.parseUnsignedLong(searchKeyStr);
                        Long searchValue = btree.search(searchKey);
                        if (searchValue != null) {
                            System.out.println("Key " + searchKey + " found with value " + searchValue + ".");
                        } else {
                            System.out.println("Key " + searchKey + " not found.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid unsigned integer for key.");
                    } catch (IOException e) {
                        System.out.println("Error searching for key: " + e.getMessage());
                    }
                    break;

                case "print":
                case "6":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                        break;
                    }
                    try {
                        List<KeyValuePair> allEntries = traverseTree(btree);
                        if (allEntries.isEmpty()) {
                            System.out.println("The B-Tree is empty.");
                        } else {
                            for (KeyValuePair kv : allEntries) {
                                System.out.println("Key: " + kv.key + ", Value: " + kv.value);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error printing B-Tree: " + e.getMessage());
                    }
                    break;

                case "load":
                case "5":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                        break;
                    }
                    System.out.print("Enter CSV file name to load: ");
                    String loadFilename = scanner.nextLine().trim();
                    try {
                        loadFromCSV(btree, loadFilename);
                        System.out.println("Loaded key-value pairs from '" + loadFilename + "'.");
                    } catch (FileNotFoundException e) {
                        System.out.println("CSV file not found: " + e.getMessage());
                    } catch (IOException e) {
                        System.out.println("Error reading CSV file: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Error inserting key-value pairs: " + e.getMessage());
                    }
                    break;

                case "extract":
                case "7":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                        break;
                    }
                    System.out.print("Enter CSV file name to extract to: ");
                    String extractFilename = scanner.nextLine().trim();
                    try {
                        List<KeyValuePair> allEntries = traverseTree(btree);
                        extractToCSV(allEntries, extractFilename);
                        System.out.println("Extracted key-value pairs to '" + extractFilename + "'.");
                    } catch (IOException e) {
                        System.out.println("Error writing to CSV file: " + e.getMessage());
                    }
                    break;

                case "quit":
                case "8":
                    if (btree != null) {
                        try {
                            btree.close();
                            System.out.println("Closed index file.");
                        } catch (IOException e) {
                            System.out.println("Error closing index file: " + e.getMessage());
                        }
                    }
                    System.out.println("Exiting program.");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid command. Please try again.");
                    break;
            }
        }
    }

    /**
     * Prints the command menu to the console.
     */
    private static void printMenu() {
        String menu = "\n==== B-Tree Index Manager ====\n" +
                "Commands:\n" +
                "1. CREATE  - Create a new index file\n" +
                "2. OPEN    - Open an existing index file\n" +
                "3. INSERT  - Insert a key/value pair\n" +
                "4. SEARCH  - Search for a key\n" +
                "5. LOAD    - Load key/value pairs from a CSV file\n" +
                "6. PRINT   - Print all key/value pairs\n" +
                "7. EXTRACT - Extract key/value pairs to a CSV file\n" +
                "8. QUIT    - Exit the program\n" +
                "===============================";
        System.out.println(menu);
    }

    /**
     * Traverses the B-Tree and collects all key-value pairs.
     *
     * @param btree The B-Tree to traverse.
     * @return A list of key-value pairs.
     * @throws IOException If an I/O error occurs during traversal.
     */
    private static List<KeyValuePair> traverseTree(BTree btree) throws IOException {
        List<KeyValuePair> result = new ArrayList<>();
        traverseNode(btree, btree.getRoot(), result);
        return result;
    }

    /**
     * Recursively traverses a node and its children to collect key-value pairs.
     *
     * @param btree  The B-Tree.
     * @param node   The current node.
     * @param result The list to collect key-value pairs.
     * @throws IOException If an I/O error occurs during traversal.
     */
    private static void traverseNode(BTree btree, Node node, List<KeyValuePair> result) throws IOException {
        if (node == null) {
            return;
        }
        int i;
        for (i = 0; i < node.getKeys().size(); i++) {
            if (!node.isLeaf()) {
                long childId = node.getChildren().get(i);
                if (childId != 0L) {
                    Node child = btree.readNode(childId);
                    traverseNode(btree, child, result);
                }
            }
            result.add(new KeyValuePair(node.getKeys().get(i), node.getValues().get(i)));
        }
        if (!node.isLeaf()) {
            long childId = node.getChildren().get(i);
            if (childId != 0L) {
                Node child = btree.readNode(childId);
                traverseNode(btree, child, result);
            }
        }
    }

    /**
     * Loads key-value pairs from a CSV file and inserts them into the B-Tree.
     *
     * @param btree    The B-Tree to insert into.
     * @param filename The CSV file to load from.
     * @throws IOException If an I/O error occurs during reading the file.
     * @throws Exception   If insertion errors occur.
     */
    private static void loadFromCSV(BTree btree, String filename) throws IOException, Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String[] parts = line.split(",");
            if (parts.length != 2) {
                System.out.println("Skipping invalid line " + lineNumber + ": " + line);
                continue; // Skip invalid lines
            }
            try {
                long key = Long.parseUnsignedLong(parts[0].trim());
                long value = Long.parseUnsignedLong(parts[1].trim());
                btree.insert(key, value);
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid number at line " + lineNumber + ": " + line);
            }
        }
        reader.close();
    }

    /**
     * Extracts key-value pairs to a CSV file.
     *
     * @param entries  The list of key-value pairs to write.
     * @param filename The CSV file to write to.
     * @throws IOException If an I/O error occurs during writing the file.
     */
    private static void extractToCSV(List<KeyValuePair> entries, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (KeyValuePair kv : entries) {
            writer.write(kv.key + "," + kv.value);
            writer.newLine();
        }
        writer.close();
    }

    /**
     * A simple class to hold key-value pairs.
     */
    private static class KeyValuePair {
        long key;
        long value;

        KeyValuePair(long key, long value) {
            this.key = key;
            this.value = value;
        }
    }
}
