// src/Main.java
import java.io.IOException;

public class Main {
    private static final int MINIMAL_DEGREE = 3;

    public static void main(String[] args) {
        FileIO fileIO = new FileIO();
        BTree btree = null;
        while (true) {
            printMenu();
            System.out.print("Enter command: ");
            String command = System.console() != null ? System.console().readLine().trim().toLowerCase()
                    : new java.util.Scanner(System.in).nextLine().trim().toLowerCase();
            switch (command) {
                case "create":
                case "1":
                    try {
                        if (fileIO.getFilename() != null) {
                            fileIO.closeFile();
                        }
                        btree = Commands.createCommand(fileIO, MINIMAL_DEGREE);
                    } catch (Exception e) {
                        System.out.println("Error during create command: " + e.getMessage());
                    }
                    break;
                case "open":
                case "2":
                    try {
                        if (fileIO.getFilename() != null) {
                            fileIO.closeFile();
                        }
                        btree = Commands.openCommand(fileIO, MINIMAL_DEGREE);
                    } catch (Exception e) {
                        System.out.println("Error during open command: " + e.getMessage());
                    }
                    break;
                case "insert":
                case "3":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                    } else {
                        Commands.insertCommand(btree);
                    }
                    break;
                case "search":
                case "4":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                    } else {
                        Commands.searchCommand(btree);
                    }
                    break;
                case "load":
                case "5":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                    } else {
                        Commands.loadCommand(btree);
                    }
                    break;
                case "print":
                case "6":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                    } else {
                        Commands.printCommand(btree);
                    }
                    break;
                case "extract":
                case "7":
                    if (btree == null) {
                        System.out.println("Error: No index file is currently open.");
                    } else {
                        Commands.extractCommand(btree);
                    }
                    break;
                case "quit":
                case "8":
                    Commands.quitCommand(fileIO);
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
                    break;
            }
        }
    }

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
}
