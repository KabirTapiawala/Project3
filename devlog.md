# Dev Log - November 27, 2024

## Project Overview
The project is an interactive program designed to manage an index file using a B-Tree structure. The program will present a menu to the user, allowing them to execute various commands to manipulate the index file. Key features include:

- Creating and opening index files.
- Inserting key/value pairs.
- Searching for keys.
- Loading data from a file.
- Printing and extracting key/value pairs.
- Handling user input errors and file overwrites.

## Initial Thoughts
The program will need to handle file operations robustly, ensuring data integrity and user-friendly error handling. The B-Tree implementation will be crucial, especially managing memory efficiently by keeping only three nodes in memory at a time.

## Plan
1. **Set Up Project Structure**: I Create the necessary files and directories.
2. **Implement File Operations**: I Start with basic file creation and opening functionalities.
3. **Develop B-Tree Structure**: I will Implement the B-Tree with minimal degree 10.
4. **Command Handling**: I Implement the command menu and corresponding functionalities.
5. **Error Handling**: I Ensure robust error handling for user inputs and file operations.
6. **Testing**: I Thoroughly test each component to ensure reliability.


# Dev Log - November 28, 2024

## Implemented File Operations
Today, I spent most of the day on the `FileIO.java` class, which handles all the reading, writing, and creating of files. Honestly, I thought this would be a simple task, but I kept running into these annoying little issues that slowed me down.

### Key Functionalities Added
- **Creating New Files**: I set up the logic to create new index files with a unique magic number for validation. This part actually worked on the first try, which was a nice confidence boost.
- **Opening Existing Files**: Built functionality to open files and verify them using the magic number. I had to debug this more than I’d like to admit because I kept misreading my own checks.
- **Block Allocation**: This turned into a whole ordeal. I was trying to ensure unique block IDs, but my initial approach kept breaking. I took a break, came back, and reworked the logic until it finally made sense.
- **Reading and Writing Blocks**: Implemented 512-byte block handling, which, thankfully, worked smoother than I expected.

### Challenges Faced
- **Magic Number Validation**: This part really messed with me. I overcomplicated it at first, but once I simplified things, it felt obvious.
- **Block Management**: Designing a system that wasn’t overly complicated but still efficient drove me a little crazy. It’s one of those tasks where the “simple” solution isn’t obvious until you’ve tried five things that don’t work.

### Solutions
- I leaned heavily on `RandomAccessFile` for precise file operations. Once I got the hang of it, everything started falling into place.
- Byte buffers saved the day. They handled all the cross-platform data integrity issues I was worried about, and I wish I’d used them earlier.

---

# Dev Log - November 29, 2024

## Developed Initial Node Structure
The `Node.java` class took up all my time today. It’s basically the heart of the B-Tree, and getting it right felt like assembling IKEA furniture without instructions—one wrong move and everything fell apart.

### Features Implemented
- **Node Attributes**: Defined all the key properties like `blockId`, `parentId`, `numKeys`, `keys`, `values`, and `children`. Writing the code was easy; making it work together was not.
- **Serialization and Deserialization**: Figured out how to store nodes as byte arrays and then reconstruct them later. This part took a lot of trial and error.
- **Insertion Logic**: Added the ability to insert key-value pairs into nodes while keeping them in order. This was frustrating because I kept messing up the index calculations.
- **Leaf Detection**: This was the one straightforward part of the day. Just checking if a node has children—no surprises there.

### Challenges Faced
- **Data Alignment**: Misaligned data caused all sorts of bizarre bugs. Fixing it was like trying to find a needle in a haystack.
- **Serialization Complexity**: I didn’t realize how tricky it would be to ensure the serialized nodes could always be reconstructed properly.

### Solutions
- I switched `numKeys` to an `int` instead of a `long`, which simplified things way more than I expected.
- Padding saved my life here. Adding a few extra bytes to align everything fixed so many weird issues that I felt silly for not doing it sooner.

---

# Dev Log - December 1, 2024

## Revised Node Serialization
I thought I was done with serialization, but nope—testing proved otherwise. The way I was serializing nodes was causing issues with duplicate detection, so I had to go back and fix it.

### Key Changes
- **Switched `numKeys` to `int`**: Using a `long` was overkill, and switching to an `int` made everything cleaner and simpler.
- **Added Padding**: I added 4 bytes of padding to ensure proper alignment. At first, I thought this was unnecessary, but it ended up solving a lot of problems.
- **Updated Deserialization**: Adjusted the code to account for the new padding. This was tedious, but it worked.

### Impact
- **Duplicate Detection Fixed**: Finally, duplicates are being detected reliably during testing. This was a huge relief.
- **Stability Improved**: The nodes are now much more consistent, which makes me feel better about moving forward.

---

# Dev Log - December 4, 2024

## Enhanced loadFromCSV Functionality
I spent the day cleaning up the `loadFromCSV` function. It wasn’t handling duplicate keys well, and the way it stopped processing after hitting a duplicate was driving me nuts.

### Enhancements Made
- **Graceful Skipping of Duplicates**: Now the function just skips duplicates without throwing a fit. This feels so much better.
- **Detailed Logging**: I added logs that show when a duplicate is skipped, including the key and line number. It’s oddly satisfying to see this level of detail in action.
- **Summary Output**: At the end of the process, the program tells you how many entries were inserted and how many were skipped. It’s a small touch, but it makes the feature feel complete.

### Challenges Faced
- **Maintaining Continuity**: Getting the function to keep going after hitting a duplicate wasn’t as simple as I thought it’d be.
- **Balancing Feedback**: The logs were overwhelming at first, so I had to scale them back to focus on the important details.

### Solutions
- Used `try-catch` blocks to handle duplicate keys without crashing the process.
- Streamlined the log messages to make them informative but not overbearing.

---

# Dev Log - December 5, 2024

## Tested Load and Insert Functionalities
Today was all about testing, and it felt surprisingly rewarding to see everything come together. The `loadFromCSV` and `insert` features are finally working exactly how I wanted.

### Testing Scenarios
1. **Initial Load with Unique Keys**:
   - Created a CSV file with only unique keys and loaded it into a fresh index file.
   - No errors, no warnings—just smooth sailing.

2. **Reload with Duplicates**:
   - Loaded the same file again into the same index file.
   - The program skipped all duplicates and logged them clearly. This was such a satisfying moment.

3. **Mixed Data**:
   - Tested with a file that had a mix of unique and duplicate keys.
   - Unique entries were added successfully, and duplicates were handled gracefully.

### Observations
- **Insertion Efficiency**: The program handled large datasets without slowing down.
- **Duplicate Handling**: The logs are clear and helpful, showing exactly what’s happening behind the scenes.
- **Overall Stability**: Even with edge cases, everything worked as expected.

---

# Dev Log - December 6, 2024

## Implemented Search Functionality
The search functionality is finally done! I thought this would be easy, but debugging the recursion almost broke me.

### Features Implemented
- **Search Command**: Added an option to search for specific keys via the command menu.
- **Validation**: Made sure the input is valid before attempting to search.
- **Recursive Traversal**: Integrated the B-Tree’s search method to find keys efficiently.

### Challenges Faced
- **Debugging Recursion**: I kept breaking the logic for edge cases. It felt like every time I fixed one thing, something else went wrong.
- **User Messaging**: Balancing between too much information and not enough was trickier than I expected.

### Solutions
- Simplified the recursion step by step until it finally worked for all cases.
- Tweaked the output messages to be concise but clear.

---

# Dev Log - December 7, 2024

## Implemented Print and Extract Functionalities
Today I added two big features—printing all key-value pairs and exporting them to a CSV file. These felt like the finishing touches, and I’m really happy with how they turned out.

### Features Implemented
1. **Print Functionality**:
   - Added a command to print all key-value pairs in sorted order.
   - Used in-order traversal to ensure the output was clean and logical.

2. **Extract Functionality**:
   - Created a command to export all key-value pairs to a CSV file.
   - Built-in error handling to ensure smooth operation even if something goes wrong.

### Observations
- **Printing**: Seeing everything laid out neatly was so satisfying. It feels like all the hard work is paying off.
- **CSV Export**: The export worked perfectly on the first try, which was a huge relief.

---

# Dev Log - December 8, 2024

## Final Testing and Documentation
With all features in place, today was about wrapping things up. I ran a final round of tests and documented the project so it’s ready for future use or updates.

### Testing Conducted
1. **Load Testing**:
   - Tested with large datasets and mixed data. Everything ran smoothly, even with duplicates.

2. **Search Functionality**:
   - Verified that searches returned the correct results for both existing and missing keys.

3. **Print and Extract**:
   - Ensured all key-value pairs displayed correctly and exported without any errors.

4. **Error Handling**:
   - Simulated invalid inputs and file errors to confirm the program could handle them gracefully.

### Documentation
- Added comments to the code to explain every method and class.
- Wrote a user guide with clear instructions and examples for each feature.
- Compiled a final report summarizing the entire development process.

---
