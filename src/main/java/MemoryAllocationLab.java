import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory;
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    /**
     * TODO 1, 2: Process memory requests from file
     * <p>
     * This method reads the input file and processes each REQUEST and RELEASE.
     * <p>
     * TODO 1: Read and parse the file
     *   - Open the file using BufferedReader
     *   - Read the first line to get total memory size
     *   - Initialize the memory list with one large free block
     *   - Read each subsequent line and parse it
     *   - Call appropriate method based on REQUEST or RELEASE
     * <p>
     * TODO 2: Implement allocation and deallocation
     *   - For REQUEST: implement First-Fit algorithm
     *     * Search memory list for first free block >= requested size
     *     * If found: split the block if necessary and mark as allocated
     *     * If not found: increment failedAllocations
     *   - For RELEASE: find the process's block and mark it as free
     *   - Optionally: merge adjacent free blocks (bonus)
     */
   public void processRequests(String filename) {
    System.out.println("Reading from: " + filename);

    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

        // ---- Read total memory size ----
        int totalMemory = Integer.parseInt(br.readLine().trim());
        System.out.println("Total Memory: " + totalMemory + " KB");
        System.out.println("----------------------------------------");
        System.out.println("\nProcessing requests...\n");

        // Initialize memory with one big free block
        memory.clear();
        memory.add(new MemoryBlock(0, totalMemory, null));

        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(" ");
            String command = parts[0];

            if (command.equalsIgnoreCase("REQUEST")) {
                String processName = parts[1];
                int size = Integer.parseInt(parts[2]);
                allocate(processName, size);
            } else if (command.equalsIgnoreCase("RELEASE")) {
                String processName = parts[1];
                deallocate(processName);
            }
        }

    } catch (IOException e) {
        System.out.println("Error reading file: " + e.getMessage());
    }
}
    /**
     * TODO 2A: Allocate memory using First-Fit
     */
    public void allocate(String processName, int size) {
    for (int i = 0; i < memory.size(); i++) {
        MemoryBlock block = memory.get(i);

        // Check: block is free AND large enough
        if (block.isFree() && block.size >= size) {

            // --- Found our First-Fit block ---
            int remaining = block.size - size;

            // Allocate the block
            block.processName = processName;
            block.size = size;

            // If unused space exists → split block
            if (remaining > 0) {
                MemoryBlock freeBlock = new MemoryBlock(
                        block.start + size,
                        remaining,
                        null
                );
                memory.add(i + 1, freeBlock);
            }

            successfulAllocations++;
            System.out.println("REQUEST " + processName + " " + size + " KB → SUCCESS");
            return;
        }
    }

    // No block found
    failedAllocations++;
    System.out.println("REQUEST " + processName + " " + size + " KB → FAILED (insufficient memory)");
} 
    public void deallocate(String processName) {
    for (int i = 0; i < memory.size(); i++) {
        MemoryBlock block = memory.get(i);

        if (!block.isFree() && block.processName.equals(processName)) {

            // Mark the block as free
            block.processName = null;
            System.out.println("RELEASE " + processName + " → SUCCESS");

            // Optional but recommended: merge adjacent free blocks
            mergeFreeBlocks();

            return;
        }
    }

    System.out.println("RELEASE " + processName + " → FAILED (process not found)");
}

    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    /**
     * Main method (FULLY PROVIDED)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}
