# Lab 8: Memory Allocation Simulator

## Learning Objectives

* Understand how memory allocation strategies manage available memory blocks
* Implement the First-Fit memory allocation algorithm
* Analyze memory fragmentation and utilization
* Calculate and interpret memory management performance metrics

## Prerequisites

* Basic Java programming knowledge
* Understanding of loops and ArrayLists
* Familiarity with file reading in Java

## Introduction

Every time you open an application, create a file, or load a game level, the operating system must find space in memory to store that data. Memory allocation is one of the most fundamental tasks an OS performs, happening thousands of times per second. But memory doesn't come in neat, pre-packaged sizes. Programs need different amounts of memory, and as they start and stop, memory becomes fragmented—filled with gaps of various sizes.

Memory allocation strategies determine how the OS decides where to place each new process. Should it grab the first available space? Search for the perfect fit? Use the largest block available? Each strategy creates different patterns of fragmentation and has different performance characteristics.

First-Fit allocation is the most straightforward: scan through memory from the beginning and use the first block that's large enough. It's fast because it doesn't need to search the entire memory space. But does it use memory efficiently? That's what you'll discover in this lab.

You'll build a simulator that reads allocation requests from a file and watches memory fill up, fragment, and reorganize as processes come and go. By the end, you'll understand why memory management is such a critical challenge in operating systems.

## What You'll Implement

Complete **2 TODO tasks** in one file:

* **TODO 1:** Read memory requests from a file and parse them
* **TODO 2:** Implement First-Fit allocation and deallocation logic

The MemoryBlock class, displayStatistics method, and main method are fully provided.

## Lab File Structure

**MemoryAllocationLab.java** - The only file you need to complete

* `MemoryBlock` class - Fully provided (represents a memory block)
* `processRequests()` method - **TODO 1, 2** (you implement core logic)
* `allocate()` method - **TODO 2A** (you implement allocation)
* `displayStatistics()` method - Fully provided (displays results)
* `main()` method - Fully provided (runs simulation)

## Project Setup

1. Download `MemoryAllocationLab.java`
2. Download `memory_requests.txt` (sample input file)
3. Complete the 2 TODOs
4. Compile: `javac MemoryAllocationLab.java`
5. Run: `java MemoryAllocationLab memory_requests.txt`

## Understanding Memory Allocation

Memory allocation manages a list of memory blocks. Each block is either:

* **Free** - Available for allocation
* **Allocated** - Currently in use by a process

When a REQUEST arrives, the allocator searches for a free block large enough. With First-Fit, it uses the first suitable block found. When a RELEASE arrives, that block becomes free again.

**Key Concepts:**

* **Free Block:** Unallocated memory available for processes
* **Allocated Block:** Memory currently used by a process
* **Memory Fragmentation:** Unusable gaps between allocated blocks
* **First-Fit:** Allocate the first free block that's large enough

## The Algorithm

Here's how First-Fit allocation works:

1. Maintain a list of all memory blocks (free and allocated)
2. For each REQUEST:
   - Search from the beginning for the first free block ≥ requested size
   - If found: mark it as allocated to that process
   - If not found: allocation fails (insufficient memory)
3. For each RELEASE:
   - Find the block allocated to that process
   - Mark it as free
4. After all requests, calculate statistics

**Example:**

Initial memory: 1024 KB (all free)

```
REQUEST P1 100  →  [P1: 0-99][Free: 100-1023]
REQUEST P2 250  →  [P1: 0-99][P2: 100-349][Free: 350-1023]
REQUEST P3 150  →  [P1: 0-99][P2: 100-349][P3: 350-499][Free: 500-1023]
RELEASE P1      →  [Free: 0-99][P2: 100-349][P3: 350-499][Free: 500-1023]
REQUEST P4 50   →  [P4: 0-49][Free: 50-99][P2: 100-349][P3: 350-499][Free: 500-1023]
```

Notice how P4 used the first free block (left by P1), even though it's larger than needed. The unused portion (50-99) remains free but creates fragmentation.

## Input File Format

**memory_requests.txt:**

```
1024
REQUEST P1 100
REQUEST P2 250
REQUEST P3 150
RELEASE P1
REQUEST P4 120
RELEASE P2
REQUEST P5 200
```

**Format:**

* **Line 1:** Total memory size in KB
* **Subsequent lines:** Either `REQUEST ProcessName Size` or `RELEASE ProcessName`

## Expected Output

```
========================================
Memory Allocation Simulator (First-Fit)
========================================

Reading from: memory_requests.txt
Total Memory: 1024 KB
----------------------------------------

Processing requests...

REQUEST P1 100 KB → SUCCESS
REQUEST P2 250 KB → SUCCESS
REQUEST P3 150 KB → SUCCESS
RELEASE P1 → SUCCESS
REQUEST P4 120 KB → SUCCESS
RELEASE P2 → SUCCESS
REQUEST P5 200 KB → SUCCESS

========================================
Final Memory State
========================================
Block 1: [0-119]     P4 (120 KB) - ALLOCATED
Block 2: [120-369]   FREE (250 KB)
Block 3: [370-519]   P3 (150 KB) - ALLOCATED
Block 4: [520-719]   P5 (200 KB) - ALLOCATED
Block 5: [720-1023]  FREE (304 KB)

========================================
Memory Statistics
========================================
Total Memory:           1024 KB
Allocated Memory:       470 KB (45.90%)
Free Memory:            554 KB (54.10%)
Number of Processes:    3
Number of Free Blocks:  2
Largest Free Block:     304 KB
External Fragmentation: 24.41%

Successful Allocations: 5
Failed Allocations:     0
========================================
```

## Implementation Guide

### TODO 1: Read File and Initialize Memory

Use a try-with-resources block to open the file with BufferedReader. Read the first line and parse it as an integer to get the total memory size. Print this value to match the expected output format. Initialize the memory ArrayList with a single MemoryBlock starting at position 0, spanning the entire memory space, with null as the process name (indicating it's free). Print the header for the processing section. Then loop through the remaining lines of the file. For each line, split it by spaces to get the command parts. If the first part is "REQUEST", extract the process name and size, then call the allocate method with these parameters. If the first part is "RELEASE", extract the process name and call a deallocate method (which you'll need to create). Make sure to handle IOException appropriately with a catch block that prints an error message.

### TODO 2A: Implement First-Fit Allocation

Create the allocate method that takes a process name and size as parameters. Loop through the memory ArrayList using an indexed for loop (you'll need the index later). For each block, check if it's free and if its size is greater than or equal to the requested size. If you find such a block, this is your allocation target. Set the block's processName to the given process name. Check if the block's size is larger than the requested size—if so, you need to split it. Calculate the remaining size by subtracting the requested size from the block's current size. Update the block's size to be exactly the requested size. Create a new MemoryBlock for the remaining free space, with a start position of the current block's start plus the requested size, the remaining size you calculated, and null for the process name. Insert this new block into the memory list at position i+1 (right after the current block). Increment the successfulAllocations counter and print a success message showing the process name and size. Then return from the method since allocation is complete. If the loop completes without finding a suitable block, increment failedAllocations and print a failure message indicating insufficient memory.

You'll also need to create a deallocate method. Loop through all blocks in the memory ArrayList. For each block, check if it's not free and if its processName matches the given process name. When you find the matching block, set its processName to null to mark it as free, print a success message, and return. If no matching process is found after checking all blocks, print an error message indicating the process wasn't found.

## Common Mistakes to Avoid

When allocating from a larger block, you must create a new free block for the leftover space. Simply setting the process name without splitting wastes memory because the entire large block gets marked as allocated even though the process only needs part of it. The correct approach is to check if the block size exceeds the requested size, and if so, shrink the allocated block to exactly the requested size and create a new free block for the remaining space.

The new free block must start after the allocated block ends. A common error is setting the new block's start position to be the same as the current block's start, which would cause memory overlap. The correct start position is calculated by adding the requested size to the current block's start position. This ensures the new free block begins immediately after the allocated space.

Always use try-catch when reading files to handle potential IOExceptions. Files might not exist, might be inaccessible, or might contain invalid data. Wrapping file operations in a try-catch block prevents your program from crashing and allows you to provide helpful error messages to the user. The try-with-resources syntax automatically closes the file even if an exception occurs, preventing resource leaks.

## Sample Input Files

### memory_requests.txt (Basic)

```
1024
REQUEST P1 100
REQUEST P2 250
REQUEST P3 150
RELEASE P1
REQUEST P4 120
RELEASE P2
REQUEST P5 200
```

### workload1.txt (Small Frequent Allocations)

```
512
REQUEST A 50
REQUEST B 30
REQUEST C 40
RELEASE A
REQUEST D 45
REQUEST E 25
RELEASE B
REQUEST F 35
RELEASE C
REQUEST G 50
```

### workload2.txt (Large Allocations)

```
2048
REQUEST X 500
REQUEST Y 800
REQUEST Z 300
RELEASE X
REQUEST W 400
RELEASE Y
REQUEST V 600
```

## Analysis Questions

After completing the lab, answer these questions:

1. **Fragmentation Analysis:** In the basic example, what is the external fragmentation percentage? Explain why free memory becomes fragmented.

2. **First-Fit Efficiency:** Why does P4 use the block left by P1 even though it's larger than needed? What happens to the unused portion?

3. **Allocation Patterns:** Compare workload1.txt (many small processes) with workload2.txt (few large processes). Which creates more fragmentation? Why?

4. **Failed Allocations:** Create a scenario where an allocation fails even though the total free memory is larger than the request. Why does this happen?

5. **Algorithm Comparison:** How might Best-Fit allocation handle the basic example differently than First-Fit? Would it reduce fragmentation?

## What You're Really Learning

Memory allocation is about managing a finite resource with competing demands. Every computer system—from embedded devices to supercomputers—faces this challenge. The allocation strategy determines how efficiently that resource is used.

First-Fit is fast because it stops searching as soon as it finds a suitable block. But this speed comes at a cost: it tends to leave small fragments at the beginning of memory that are too small to be useful. Over time, memory becomes "swiss cheese"—full of little holes that can't hold anything.

The fragmentation percentage tells the real story. You might have plenty of total free memory, but if it's scattered in tiny pieces, large allocations will fail. This is why operating systems must balance allocation speed against memory efficiency.

Understanding these trade-offs prepares you for real-world system programming challenges: cache management, database buffer pools, memory-mapped files, and custom allocators for high-performance applications.

## Bonus Challenge (Optional)

### Merge Adjacent Free Blocks

After deallocation, check if adjacent blocks are also free and merge them:

```java
private static void mergeAdjacentBlocks() {
    for (int i = 0; i < memory.size() - 1; i++) {
        MemoryBlock current = memory.get(i);
        MemoryBlock next = memory.get(i + 1);
        
        if (current.isFree() && next.isFree()) {
            current.size += next.size;
            memory.remove(i + 1);
            i--;  // Check this position again
        }
    }
}
```

Call this method after each deallocation to reduce fragmentation.

## Compilation and Execution

```bash
javac MemoryAllocationLab.java
java MemoryAllocationLab memory_requests.txt
```

## Submission Requirements

After completing your work:

```bash
git add .
git commit -m "completed lab 8 - memory allocation simulator"
git push origin main
```

**Include:**

* Completed `processRequests()` method with TODO 1 and 2
* Completed `allocate()` helper method
* Implemented `deallocate()` helper method (called from processRequests)
* Screenshots showing successful execution with all three workload files
* Answers to the 5 analysis questions
* Verify statistics are calculated correctly (percentages, fragmentation)