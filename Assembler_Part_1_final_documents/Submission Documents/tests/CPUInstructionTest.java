package tests;

import BasicMachine.CPU_Module.*;
import BasicMachine.Memory_Module.*;

/* ==============================================================
 * CPUInstructionTest.java
 * --------------------------------------------------------------
 * Purpose:
 *   Verifies the correct execution of a single instruction
 *   (LDR — Load Register from Memory) in the CPU module.
 *
 * Context:
 *   This test isolates and validates the basic fetch–decode–execute
 *   pipeline for one instruction cycle in MachineCore.java.
 *
 *   Specifically, it ensures that:
 *     - The LDR instruction correctly fetches data from memory.
 *     - The result is properly stored in the specified general register.
 *     - The instruction format and bit encoding match the assembler spec.
 *
 * Instruction Format (16 bits total):
 *   [15..10] = Opcode
 *   [ 9.. 8] = Register (R)
 *   [ 7.. 6] = Index Register (IX)
 *   [    5 ] = Indirect bit (I)
 *   [ 4.. 0] = Address
 *
 * Example:
 *   102207 (octal)  → opcode=33 (LDX), R=0, IX=2, I=0, addr=7
 *   For this test:   → opcode=1 (LDR), R=0, IX=0, I=0, addr=5
 *
 * ==============================================================
 */
public class CPUInstructionTest {

    /* ==============================================================
     * main()
     * --------------------------------------------------------------
     * Test procedure:
     *
     * Step 1 — Setup:
     *   - Create Memory, Registers, and CPU core (MachineCore) instances.
     *
     * Step 2 — Load Instruction:
     *   - Manually encode an LDR instruction using bit shifts:
     *       opcode = 1 (LDR)
     *       R = 0 (R0)
     *       IX = 0 (no index)
     *       I = 0 (direct addressing)
     *       Address = 5
     *     Combined as:
     *       instr = (1 << 10) | (0 << 8) | (0 << 6) | (0 << 5) | 5
     *     → This produces the binary/decimal encoding for "LDR R0,0,5".
     *
     * Step 3 — Initialize Memory:
     *   - Write the instruction into memory at address 10.
     *   - Write a data word (0x1111) into memory location 5.
     *
     * Step 4 — Execute:
     *   - Set the Program Counter (PC) to 10 (the instruction address).
     *   - Execute one CPU cycle using runOneCycle().
     *
     * Step 5 — Validate:
     *   - Verify that register R0 now contains the value 0x1111
     *     (the value originally stored at memory address 5).
     *
     * Step 6 — Output:
     *   - Print a success message if all assertions pass.
     *
     * ==============================================================
     */
    public static void main(String[] args) {
        // ───────────────────────────────────────────────
        // Step 1: Initialize memory, registers, and CPU core
        Memory mem = new Memory();
        Registers regs = new Registers();
        MachineCore cpu = new MachineCore(mem, regs);

        // ───────────────────────────────────────────────
        // Step 2: Encode and store LDR R0,0,5
        // Opcode = 1 (LDR)
        // R = 0, IX = 0, I = 0, Address = 5
        int instr = (1 << 10) | (0 << 8) | (0 << 6) | (0 << 5) | 5;

        // Write the instruction into memory at address 10
        mem.writeWord(10, instr);

        // ───────────────────────────────────────────────
        // Step 3: Place data into memory location 5 (to be loaded into R0)
        mem.writeWord(5, 0x1111);

        // ───────────────────────────────────────────────
        // Step 4: Set PC to point to instruction and execute one cycle
        regs.setPC(10);
        cpu.runOneCycle();

        // ───────────────────────────────────────────────
        // Step 5: Verify result
        // After execution, R0 should contain the value from memory[5]
        assert regs.getGPRValue(0) == 0x1111 : "LDR failed — R0 does not contain expected value.";

        // ───────────────────────────────────────────────
        // Step 6: Output success
        System.out.println("[PASS] CPUInstructionTest — LDR instruction executed successfully.");
    }
}
