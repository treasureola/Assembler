package BasicMachine.simulator;

import BasicMachine.Memory_Module.Memory;
import BasicMachine.CPU_Module.MachineCore;
import BasicMachine.CPU_Module.Registers;

import java.io.File;

/**
 * ==============================================================
 * Program1.java — Part 2
 * --------------------------------------------------------------
 * Demonstrates the integrated Cache + Memory + CPU system.
 * Loads a .ld/.lst program, runs it using MachineCore, and prints
 * cache statistics after execution.
 * ==============================================================
 */
public class Program1 {

    public static void main(String[] args) {
        try {
            System.out.println("=== Basic Machine Simulator — Part 2 (Cache Integration) ===\n");

            // ------------------------------------------------------
            // 1. Initialize system memory (with integrated cache)
            // ------------------------------------------------------
            Memory memory = new Memory();
            memory.enableCache();               // optional, enabled by default
            memory.resetCacheStatistics();      // clear old stats

            // ------------------------------------------------------
            // 2. Load program into memory
            // ------------------------------------------------------
            ProgramLoader loader = new ProgramLoader();
            File programFile = new File("src/BasicMachine/assembler/source.ld");
            loader.load(programFile, memory);

            // ------------------------------------------------------
            // 3. Initialize registers and CPU core
            // ------------------------------------------------------
            Registers registers = new Registers();
            registers.setPC(memory.getProgramStartAddress());

            MachineCore cpu = new MachineCore(memory, registers);

            // ------------------------------------------------------
            // 4. Run the program
            // ------------------------------------------------------
            System.out.printf("[INFO] Starting execution at PC=%06o%n%n",
                    registers.getPC());
            cpu.run();

            // ------------------------------------------------------
            // 5. Report cache performance
            // ------------------------------------------------------
            System.out.println("\n=== Execution Complete ===");
            System.out.println(memory.getCacheStatistics());
            System.out.println("\nCache contents snapshot:");
            System.out.println(memory.getCacheContents());

        } catch (Exception e) {
            System.err.println("[ERROR] Program execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
