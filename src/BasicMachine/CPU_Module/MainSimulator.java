package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;     // Memory simulation module (2048 × 16-bit words)
import BasicMachine.simulator.ProgramLoader;  // Handles reading and parsing of .ld files

import java.io.File;

/* ==============================================================
 * MainSimulator.java
 * --------------------------------------------------------------
 * Purpose:
 *   This class serves as the main driver or entry point for
 *   testing the Basic Machine CPU independently from the GUI.
 *
 * Description:
 *   - Initializes the Memory and Register modules.
 *   - Loads an assembled program (load.ld) into memory.
 *   - Starts the CPU core (MachineCore) to execute instructions
 *     until a HLT instruction halts execution.
 *
 * Usage:
 *   Run this class directly from IntelliJ or the command line:
 *       java BasicMachine.CPU_Module.MainSimulator
 *
 * Integration Summary:
 *   - Memory:     Handles read/write operations to memory.
 *   - Registers:  Stores CPU registers (GPR, IXR, PC, etc.).
 *   - ProgramLoader: Loads .ld (octal) machine code into memory.
 *   - MachineCore: Executes the instruction cycle.
 *
 * Equivalent to clicking "Run" in the Simulator UI (Part 1 GUI).
 * ==============================================================
 */
public class MainSimulator {

    /* --------------------------------------------------------------
     * main()
     * --------------------------------------------------------------
     * Entry point for running the CPU simulation manually.
     * Steps mirror the sequence that occurs when the user
     * runs a program via the JavaFX GUI.
     */
    public static void main(String[] args) {
        try {
            /* ----------------------------------------------------------
             * STEP 1: Select the program file to load.
             * ----------------------------------------------------------
             * The assembler (Part 0) generates a file named "load.ld"
             * containing machine code in octal format. Each line holds:
             *     [address] [octal instruction]
             *
             * This file must exist in the same directory from which
             * the simulator is launched.
             */
            File loadFile = new File("BasicMachine/load.ld");

            if (!loadFile.exists()) {
                System.err.println("[ERROR] load.ld not found in current directory.");
                System.err.println("Please assemble your source program first using Assembler.java.");
                return;
            }

            /* ----------------------------------------------------------
             * STEP 2: Initialize system components.
             * ----------------------------------------------------------
             * - Create the main memory (2K words, all initialized to 0).
             * - Create the CPU register set.
             */
            Memory mem = new Memory();        // Simulated main memory
            Registers regs = new Registers(); // All CPU registers (R0–R3, X1–X3, PC, etc.)

            /* ----------------------------------------------------------
             * STEP 3: Load the program into memory.
             * ----------------------------------------------------------
             * The ProgramLoader reads the .ld file line by line,
             * parsing each octal address/value pair and writing
             * it to memory using Memory.writeWord().
             */
            ProgramLoader loader = new ProgramLoader();
            loader.load(loadFile, mem);

            /* ----------------------------------------------------------
             * STEP 4: Set the initial Program Counter (PC).
             * ----------------------------------------------------------
             * The PC must point to the first valid instruction.
             * Memory.getProgramStartAddress() finds the first non-zero
             * memory word, which is assumed to be the program’s entry point.
             */
            int startAddr = loader.getProgramStartAddress();
            regs.setPC(startAddr);
            System.out.printf("[INFO] Program start address set to %06o%n", startAddr);

            /* ----------------------------------------------------------
             * STEP 5: Create the MachineCore (CPU).
             * ----------------------------------------------------------
             * MachineCore handles:
             *   - Fetching instructions (using PC, MAR, MBR)
             *   - Decoding opcode, register, and address fields
             *   - Executing supported instructions (LDR, STR, LDX, LDA, JZ, HLT)
             */
            MachineCore core = new MachineCore(mem, regs);
            System.out.printf("[VERIFY] PC before run = %06o%n", regs.getPC());
            core.run();

            /* ----------------------------------------------------------
             * STEP 6: Execute program.
             * ----------------------------------------------------------
             * The run() method continues executing instructions
             * until a HLT instruction or external stop signal occurs.
             */
            System.out.println("[RUN] Beginning program execution...\n");
            core.run();

            /* ----------------------------------------------------------
             * STEP 7: Simulation complete.
             * ----------------------------------------------------------
             * Once the CPU halts, print a completion message.
             * The register states can be examined for verification.
             */
            System.out.println("\n[HALT] Simulation finished successfully.");
            System.out.println("All instructions executed. CPU stopped normally.");

        } catch (Exception e) {
            /* ----------------------------------------------------------
             * ERROR HANDLING
             * ----------------------------------------------------------
             * If anything goes wrong (file not found, invalid octal,
             * null pointer, etc.), display a complete stack trace
             * for debugging and troubleshooting.
             */
            System.err.println("[EXCEPTION] Simulation terminated due to an error:");
            e.printStackTrace();
        }
    }
}
