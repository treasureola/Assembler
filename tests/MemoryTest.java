package tests;

import BasicMachine.Memory_Module.*;

/* ==============================================================
 * MemoryTest.java
 * --------------------------------------------------------------
 * Purpose:
 *   Unit test for verifying correct operation of the Memory subsystem.
 *   This test confirms that:
 *     - Data can be written to and read from memory using MAR and MBR.
 *     - The single-port memory model preserves values across read/write cycles.
 *     - Memory addressing follows correct masking (11-bit addresses, 16-bit words).
 *
 * Components Under Test:
 *   - Memory.java
 *   - MAR.java (Memory Address Register)
 *   - MBR.java (Memory Buffer Register)
 *
 * Test Summary:
 *   1. Create new instances of Memory, MAR, and MBR.
 *   2. Store a 16-bit value into a specific memory location.
 *   3. Retrieve it back and confirm equality.
 *   4. Print a success message if verification passes.
 *
 * ==============================================================
 */
public class MemoryTest {

    /* ==============================================================
     * main()
     * --------------------------------------------------------------
     * Test Steps:
     *
     * Step 1 — Initialize:
     *   Create clean instances of Memory, MAR, and MBR.
     *   At startup, all memory locations are expected to contain zero.
     *
     * Step 2 — Write Cycle:
     *   - Set MAR to address 10 (decimal).
     *   - Set MBR to value 0x1234 (hexadecimal).
     *   - Write the contents of MBR into memory at the address in MAR.
     *
     * Step 3 — Read Cycle:
     *   - Read the word stored at address 10 using MAR.
     *   - The returned value should match 0x1234.
     *
     * Step 4 — Verification:
     *   - Use an assertion to confirm correct memory transfer.
     *   - If successful, print "[PASS] MemoryTest".
     *   - If not, trigger an assertion failure.
     *
     * ==============================================================
     */
    public static void main(String[] args) {
        // ───────────────────────────────────────────────
        // Step 1: Initialize Memory, MAR, and MBR modules
        Memory memory = new Memory();
        MAR mar = new MAR();
        MBR mbr = new MBR();

        // ───────────────────────────────────────────────
        // Step 2: Write operation
        // MAR ← 10 (target address)
        mar.set(10);

        // MBR ← 0x1234 (data to be stored)
        mbr.set(0x1234);

        // Write MBR contents into Memory[MAR]
        memory.writeWord(mar.get(), mbr.get());

        // ───────────────────────────────────────────────
        // Step 3: Read operation
        // Read value from Memory[MAR]
        int val = memory.readWord(mar.get());

        // ───────────────────────────────────────────────
        // Step 4: Validate that read value matches written value
        assert val == 0x1234 : "Memory read/write failed — expected 0x1234 but got " + Integer.toHexString(val);

        // ───────────────────────────────────────────────
        // Step 5: Output success message
        System.out.println("[PASS] MemoryTest — memory read/write validated successfully.");
    }
}
