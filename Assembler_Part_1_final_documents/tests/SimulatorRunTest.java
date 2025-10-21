package tests;

import BasicMachine.Memory_Module.*;
import BasicMachine.CPU_Module.*;
import BasicMachine.simulator.ProgramLoader;
import java.io.File;

/* ==============================================================
 * SimulatorRunTest.java
 * --------------------------------------------------------------
 * Purpose:
 *   Performs a complete end-to-end validation of the Basic Machine
 *   simulator. This test emulates the same sequence of actions that
 *   occur when the GUI “Run” button is pressed.
 *
 * Description:
 *   The test ensures that the full simulator pipeline operates correctly:
 *     1. A previously assembled program (load.ld) is loaded into memory.
 *     2. The CPU and registers are initialized.
 *     3. The MachineCore executes continuously until a HLT instruction.
 *     4. Register and memory states are printed for observation.
 *     5. The simulation halts successfully under expected conditions.
 *
 * Modules Involved:
 *   - Memory.java                → Provides the 2048-word memory system.
 *   - ProgramLoader.java         → Loads .ld files into memory.
 *   - Registers.java             → Holds CPU register values.
 *   - MachineCore.java           → Executes the fetch–decode–execute cycle.
 *
 * Expected Result:
 *   The simulator halts normally (HLT executed),
 *   indicating that the CPU, memory, and loader modules
 *   are functioning correctly together.
 * ==============================================================
 */
public class SimulatorRunTest {

    /* ==============================================================
     * main()
     * --------------------------------------------------------------
     * Test Steps:
     *
     * Step 1 — Initialize Memory and Loader:
     *   - Create Memory and ProgramLoader objects.
     *   - Ensure that "load.ld" exists in the current directory.
     *   - Abort test if missing (requires assembler output).
     *
     * Step 2 — Load Program:
     *   - Use ProgramLoader.load() to populate memory from the file.
     *   - Print a confirmation once the file is successfully loaded.
     *
     * Step 3 — Initialize CPU and Registers:
     *   - Create a Registers object and reset all values to zero.
     *   - Instantiate the MachineCore with the memory and registers.
     *
     * Step 4 — Set Program Counter (PC):
     *   - Retrieve the starting address from memory (ProgramLoader sets it).
     *   - Assign PC to that address to prepare for execution.
     *
     * Step 5 — Execute Program:
     *   - Continuously call runOneCycle() to simulate instruction cycles.
     *   - Output register and memory states every few cycles for monitoring.
     *   - Limit execution to 1000 cycles to prevent infinite loops.
     *
     * Step 6 — Validation:
     *   - If the CPU halts normally, print “[PASS]”.
     *   - Otherwise, print “[FAIL]” if it exceeds 1000 cycles.
     *
     * Step 7 — Memory Snapshot:
     *   - Print the first 20 memory addresses in octal format
     *     to allow manual inspection of results.
     *
     * ==============================================================
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Running Full Simulator Test ===");

        // ───────────────────────────────────────────────
        // Step 1: Initialize Memory and Loader
        Memory memory = new Memory();
        ProgramLoader loader = new ProgramLoader();

        File loadFile = new File("load.ld");
        if (!loadFile.exists()) {
            System.err.println("[FAIL] load.ld file not found. Please assemble source.src first.");
            return;
        }

        // ───────────────────────────────────────────────
        // Step 2: Load program into memory
        loader.load(loadFile, memory);
        System.out.println("[OK] Program loaded from load.ld");

        // ───────────────────────────────────────────────
        // Step 3: Initialize Registers and CPU Core
        Registers regs = new Registers();
        MachineCore cpu = new MachineCore(memory, regs);
        regs.reset();

        // ───────────────────────────────────────────────
        // Step 4: Set Program Counter (PC) to start address
        int startAddr = memory.getProgramStartAddress();
        regs.setPC(startAddr);
        System.out.printf("[OK] Program start address set to %04o%n", startAddr);

        // ───────────────────────────────────────────────
        // Step 5: Execute program until HLT
        int stepCount = 0;
        while (!cpu.isHalted() && stepCount < 1000) {
            cpu.runOneCycle();
            stepCount++;

            // Display summary every 3 cycles for readability
            if (stepCount % 3 == 0) {
                System.out.printf(
                        "Step %03d | PC=%04o IR=%06o R0=%06o R1=%06o R2=%06o R3=%06o X1=%06o X2=%06o X3=%06o%n",
                        stepCount,
                        regs.getPC(), regs.getIR(),
                        regs.getGPR()[0], regs.getGPR()[1],
                        regs.getGPR()[2], regs.getGPR()[3],
                        regs.getIXR()[0], regs.getIXR()[1], regs.getIXR()[2]);
            }
        }

        // ───────────────────────────────────────────────
        // Step 6: Test outcome
        if (cpu.isHalted()) {
            System.out.println("[PASS] Simulator halted successfully after " + stepCount + " cycles.");
        } else {
            System.out.println("[FAIL] Simulator did not halt within 1000 cycles.");
        }

        // ───────────────────────────────────────────────
        // Step 7: Memory dump (for manual inspection)
        System.out.println("\nMemory Snapshot (first 20 addresses):");
        for (int i = 0; i < 20; i++) {
            System.out.printf("%04o: %06o%n", i, memory.readWord(i));
        }
    }
}
