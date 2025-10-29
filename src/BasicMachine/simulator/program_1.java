package BasicMachine.simulator;

import BasicMachine.Memory_Module.*;
import BasicMachine.CPU_module.*;
import java.io.File;

/**
 * ==============================================================
 * Program1.java (Part 2)
 * --------------------------------------------------------------
 * Purpose:
 *   Demonstrates integration of the Simple Cache and Enhanced
 *   Memory system with the CPU and Program Loader.
 *
 * Description:
 *   - Loads a program file (.ld or .lst) using ProgramLoader
 *   - Initializes Memory + Cache
 *   - Connects CPU to memory
 *   - Executes the program step-by-step
 *   - Reports cache performance statistics
 * ==============================================================
 */
public class Program1 {

    public static void main(String[] args) {
        try {
            System.out.println("=== Basic Machine: Part 2 (Cache Integration) ===\n");

            // Step 1: Create enhanced memory and cache
            Memory memory = new Memory(2048);
            Cache cache = new Cache(8, memory);  // Example: 8-line cache
            memory.attachCache(cache);

            // Step 2: Load program into memory
            ProgramLoader loader = new ProgramLoader();
            File programFile = new File("src/BasicMachine/assembler/source.src"); // adjust path if needed
            loader.load(programFile, memory);

            // Step 3: Create CPU and connect memory/cache
            CPU cpu = new CPU(memory, cache);
            cpu.reset();

            System.out.println("[INFO] Starting program execution...\n");

            // Step 4: Run program (step-by-step or full run)
            cpu.run();  // assuming your CPU supports run()

            System.out.println("\n[INFO] Program halted.");
            System.out.println("[INFO] Final cache stats:");
            cache.displayStats();

        } catch (Exception e) {
            System.err.println("Error running Program 1: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
