package BasicMachine.Memory_Module;

/* ==============================================================
 * MAR.java
 * --------------------------------------------------------------
 * Purpose:
 *   Represents the Memory Address Register (MAR) in the Basic Machine.
 *   The MAR is part of the CPU–Memory interface and temporarily holds
 *   the address of the memory location that will be accessed next.
 *
 * Description:
 *   - The CPU writes an address into the MAR whenever it intends to
 *     read from or write to a memory word.
 *   - The Memory module then reads the address from MAR to perform
 *     the requested operation (READ or WRITE).
 *
 * Width:
 *   - 11 bits (can address up to 2048 memory locations)
 *
 * Examples:
 *   FETCH cycle:
 *       MAR ← PC        // The next instruction’s address
 *       MBR ← M[MAR]    // Fetch instruction into MBR
 *
 *   EXECUTE (LDR):
 *       MAR ← EA        // Effective address of operand
 *       Rr  ← M[MAR]    // Load into general-purpose register
 * ==============================================================
 */
public class MAR {

    /* --------------------------------------------------------------
     * Field: address
     * --------------------------------------------------------------
     * Holds the current memory address (11-bit value).
     * All memory access operations reference this register.
     */
    private int address;

    /* ==============================================================
     * set()
     * --------------------------------------------------------------
     * Sets the MAR to a new memory address.
     *
     * Called by:
     *   - CPU during FETCH (MAR ← PC)
     *   - CPU during memory-reference instructions (LDR, STR, etc.)
     *
     * Parameters:
     *   addr — the desired memory address (integer)
     *
     * Implementation Detail:
     *   - Only the lowest 11 bits of the value are stored.
     *   - This enforces the Basic Machine’s 2 K word memory limit.
     * ==============================================================
     */
    public void set(int addr) {
        this.address = addr & 0x7FF;  // Mask to 11 bits (0–2047)
    }

    /* ==============================================================
     * get()
     * --------------------------------------------------------------
     * Returns the address currently stored in MAR.
     *
     * Used by:
     *   - Memory.readWord() and Memory.writeWord() to determine
     *     which memory cell to operate on.
     *
     * Example:
     *   int addr = MAR.get();      // CPU or Memory retrieves address
     *   int data = M[addr];        // Access that location
     * ==============================================================
     */
    public int get() {
        return address;
    }

    /* ==============================================================
     * Notes:
     * --------------------------------------------------------------
     * - MAR does not store data; only addresses.
     * - MBR (Memory Buffer Register) handles the actual word data.
     * - Together, MAR and MBR enable isolated address and data paths
     *   between the CPU and main memory for cleaner simulation logic.
     * ==============================================================
     */
}
