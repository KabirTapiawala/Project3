// src/Commands.java
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

public class Commands {
    private static Scanner scanner = new Scanner(System.in);

    public static BTree createCommand(FileIO fileIO, int minimalDegree) {
        System.out.print("Enter new index file name: ");
        String filename = scanner.nextLine().trim();
        try {
            boolean success = fileIO.createFile(filename, false);
            if (success) {
                return new BTree(fileIO, minimalDegree);
            }
        } catch (IOException e) {
            System.out.println("Error creating file: " + e.getMessage());
        }
        return null;
    }

    public static BTree openCommand(FileIO fileIO, int minimalDegree) {
        System.out.print("Enter index file name to open: ");
        String filename = scanner.nextLine().trim();
        try {
            boolean success = fileIO.openFileFunc(filename);
            if (success) {
                return new BTree(fileIO, minimalDegree);
            }
        } catch (IOException e) {
            System.out.println("Error opening file: " + e.getMessage());
        }
        return null;
    }

    public static void insertCommand(BTree btree) {
        try {
            System.out.print("Enter key (unsigned integer): ");
            long key = Long.parseUnsignedLong(scanner.nextLine().trim());
            System.out.print("Enter value (unsigned integer): ");
            long value = Long.parseUnsignedLong(scanner.nextLine().trim());
            btree.insert(key, value);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input. Please enter valid unsigned integers.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void searchCommand(BTree btree) {
        try {
            System.out.print("Enter key to search: ");
            long key = Long.parseUnsignedLong(scanner.nextLine().trim());
            Long value = btree.search(key);
            if (value != null) {
                System.out.println("Found: Key = " + key + ", Value = " + value);
            } else {
                System.out.println("Key not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid key.");
        } catch (IOException e) {
            System.out.println("Error during search: " + e.getMessage());
        }
    }

    public static void printCommand(BTree btree) {
        try {
            Node root = btree.getRoot();
            if (root == null) {
                System.out.println("The index is empty.");
                return;
            }
            inorderPrint(btree, root);
        } catch (IOException e) {
            System.out.println("Error during printing: " + e.getMessage());
        }
    }

    private static void inorderPrint(BTree btree, Node node) throws IOException {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.getNumKeys(); i++) {
            if (node.getChildren().get(i) != 0L) {
                Node child = btree.readNode(node.getChildren().get(i));
                inorderPrint(btree, child);
            }
            System.out.println("Key: " + node.getKeys().get(i) + ", Value: " + node.getValues().get(i));
        }
        if (node.getChildren().get(node.getNumKeys()) != 0L) {
            Node child = btree.readNode(node.getChildren().get(node.getNumKeys()));
            inorderPrint(btree, child);
        }
    }

    public static void extractCommand(BTree btree) {
        System.out.print("Enter file name to extract to: ");
        String filename = scanner.nextLine().trim();
        File f = new File(filename);
        if (f.exists()) {
            System.out.print("File '" + filename + "' already exists. Overwrite? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (!response.equals("y")) {
                System.out.println("Extract command aborted.");
                return;
            }
        }
        try (FileWriter writer = new FileWriter(f)) {
            Node root = btree.getRoot();
            if (root == null) {
                System.out.println("The index is empty.");
                return;
            }
            inorderExtract(btree, root, writer);
            System.out.println("Extracted data to '" + filename + "'.");
        } catch (IOException e) {
            System.out.println("Error during extraction: " + e.getMessage());
        }
    }

    private static void inorderExtract(BTree btree, Node node, FileWriter writer) throws IOException {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.getNumKeys(); i++) {
            if (node.getChildren().get(i) != 0L) {
                Node child = btree.readNode(node.getChildren().get(i));
                inorderExtract(btree, child, writer);
            }
            writer.write(node.getKeys().get(i) + "," + node.getValues().get(i) + "\n");
        }
        if (node.getChildren().get(node.getNumKeys()) != 0L) {
            Node child = btree.readNode(node.getChildren().get(node.getNumKeys()));
            inorderExtract(btree, child, writer);
        }
    }

    public static void loadCommand(BTree btree) {
        System.out.print("Enter CSV file name to load: ");
        String filename = scanner.nextLine().trim();
        File f = new File(filename);
        if (!f.exists()) {
            System.out.println("Error: File '" + filename + "' does not exist.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length != 2) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }
                try {
                    long key = Long.parseUnsignedLong(parts[0].trim());
                    long value = Long.parseUnsignedLong(parts[1].trim());
                    btree.insert(key, value);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid number in line: " + line);
                } catch (Exception e) {
                    System.out.println("Duplicate key " + parts[0].trim() + " found. Skipping insertion.");
                }
            }
            System.out.println("Loaded data from '" + filename + "'.");
        } catch (IOException e) {
            System.out.println("Error during loading: " + e.getMessage());
        }
    }

    public static void quitCommand(FileIO fileIO) {
        try {
            fileIO.closeFile();
        } catch (IOException e) {
            System.out.println("Error closing file: " + e.getMessage());
        }
        System.out.println("Exiting the program.");
        System.exit(0);
    }
}
