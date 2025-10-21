package tests;

import Assembler.Assembler;
import BasicMachine.Memory_Module.*;
import BasicMachine.CPU_Module.*;
import BasicMachine.simulator.ProgramLoader;

import java.io.File;

/* ==============================================================
 * IntegrationTest.java
 * --------------------------------------------------------------
 * Purpose:
 *   Validates the full integration of all major subsystems:
 *   - Assembler (Part 0)
 *   - Memory and Loader (Part 1)
 *   - CPU (MachineCore execution)
 *
 * Description:
 *   This test runs a complete end-to-end cycle of the Basic Machine.
 *   It assembles a source program, loads it into memory,
 *   executes it through the CPU, and verifies that the machine halts correctly.
 *
 * Modules Involved:
 *   1. Assembler.Assembler      — Converts source.src into load.ld
 *   2. BasicMachine.Memory      — Provides 2048-word memory model
 *   3. BasicMachine.ProgramLoader — Loads .ld file into memory
 *   4. BasicMachine.MachineCore — Executes the instructions
 *
 * Expected Behavior:
 *   - The assembler generates a valid load.ld file.
 *   - ProgramLoader successfully loads all instructions into memory.
 *   - MachineCore executes until a HLT (opcode 0) is encountered.
 *   - The simulation halts normally, confirming correct integration.
 * ==============================================================
 */
public class IntegrationTest {

    /* ==============================================================
     * main()
     * --------------------------------------------------------------
     * Steps:
     *
     * Step 1 — Assemble the source:
     *   Invokes the assembler to translate the input program file
     *   ("source.src") into machine code ("load.ld" and "output.lst").
     *
     * Step 2 — Initialize memory and loader:
     *   Creates a Memory instance (cleared to all zeros)
     *   and a ProgramLoader to load the assembled .ld file.
     *
     * Step 3 — Load program:
     *   Reads the contents of "load.ld" line-by-line,
     *   storing octal instructions into memory.
     *
     * Step 4 — Initialize CPU registers:
     *   Sets up the Register file and the CPU core (MachineCore),
     *   linking both to the memory instance.
     *
     * Step 5 — Set Program Counter:
     *   Attempts to read the starting address from Memory
     *   (as determined by ProgramLoader). If not found,
     *   uses a manual fallback (octal 0016).
     *
     * Step 6 — Execute the program:
     *   Calls cpu.run() to execute until a HLT instruction is reached.
     *
     * Step 7 — Validate:
     *   Confirms that the CPU halted successfully and prints
     *   “[PASS] Integration test completed successfully.”
     *
     * ==============================================================
     */
    public static void main(String[] args) throws Exception {

        // ───────────────────────────────────────────────
        // Step 1: Assemble the source file into machine code
        Assembler.assemble("source.src");

        // ───────────────────────────────────────────────
        // Step 2: Initialize memory and loader
        Memory mem = new Memory();
        ProgramLoader loader = new ProgramLoader();

        // ───────────────────────────────────────────────
        // Step 3: Load program from load.ld
        File loadFile = new File("load.ld");
        loader.load(loadFile, mem);

        // ───────────────────────────────────────────────
        // Step 4: Initialize registers and CPU core
        Registers regs = new Registers();
        MachineCore cpu = new MachineCore(mem, regs);

        // ───────────────────────────────────────────────
        // Step 5: Set the Program Counter to start address
        int startAddr = mem.getProgramStartAddress(); // retrieved from loader
        if (startAddr == 0) startAddr = 0016;         // fallback default
        regs.setPC(startAddr);

        // ───────────────────────────────────────────────
        // Step 6: Run the program until it halts
        cpu.run();

        // ───────────────────────────────────────────────
        // Step 7: Validate test outcome
        if (cpu.isHalted())
            System.out.println("[PASS] Integration test completed successfully.");
        else
            System.out.println("[FAIL] CPU did not halt as expected.");
    }
}
