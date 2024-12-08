# Kabir Tapiawala
# CS 4348.502
# Dr. Salazar
# B-Tree Index Manager

## Project Overview
The B-Tree Index Manager is an interactive Java program designed to manage an index file using a B-Tree structure. It provides a command-line interface that allows users to:

- Create and open index files.
- Insert key/value pairs.
- Search for keys.
- Load data from CSV files.
- Print all key/value pairs.
- Extract key/value pairs to CSV files.
- Handle user input errors and prevent file overwrites.

## File Structure
- **Main.java**: The entry point of the application. Handles user interactions, command parsing, and coordinates operations with other classes.
- **FileIO.java**: Manages all file-related operations, including creating, opening, reading, and writing index files.
- **BTree.java**: Implements the B-Tree data structure with a minimal degree of 10. Handles insertions, searches, and node splitting.
- **Node.java**: Represents individual nodes within the B-Tree. Manages keys, values, child pointers, and serialization/deserialization.
- **README.md**: This file, providing an overview, instructions, and additional information.

## Compilation Instructions
1. **Navigate to Project Directory**:
   Open your terminal and navigate to the directory containing the .java files.

   ```bash
   cd path/to/project/directory
   ```

2. **Compile the Source Files**: Use the `javac` compiler to compile all Java source files.

   ```bash
   javac *.java
   ```

   This will generate corresponding `.class` files in the same directory.

3. **Running the Program**: After successful compilation, run the program using the `java` command:

   ```bash
   java Main
   ```

   Upon running, you will see a menu with available commands:

   ```
   ==== B-Tree Index Manager ====
   Commands:
   1. CREATE  - Create a new index file
   2. OPEN    - Open an existing index file
   3. INSERT  - Insert a key/value pair
   4. SEARCH  - Search for a key
   5. LOAD    - Load key/value pairs from a CSV file
   6. PRINT   - Print all key/value pairs
   7. EXTRACT - Extract key/value pairs to a CSV file
   8. QUIT    - Exit the program
   ===============================
   ```

   Follow the on-screen prompts to execute desired operations.

## Additional Notes
- **CSV File Format**: Ensure that CSV files contain key/value pairs separated by commas, e.g., `3,30`.
- **Error Handling**: The program provides informative messages for invalid inputs or operations. Follow the prompts for guidance.
- **Dependencies**: The project uses standard Java libraries and does not require external dependencies.

## Notes to TA
- The project has been thoroughly tested to handle various scenarios, including duplicate key insertions and invalid inputs.
- Code is well-documented with comments explaining the functionality of classes and methods.
- The program ensures data integrity through consistent serialization/deserialization and robust error handling.
- Future enhancements could include a graphical user interface (GUI) and advanced querying capabilities.

---

Thank you for reviewing the B-Tree Index Manager. Please feel free to reach out if you have any questions or need further clarifications.
