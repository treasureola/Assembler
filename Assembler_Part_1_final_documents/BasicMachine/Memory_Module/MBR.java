package BasicMachine.Memory_Module;

/* ==============================================================
 * MBR.java
 * --------------------------------------------------------------
 * Purpose:
 *   Represents the Memory Buffer Register (MBR) in the Basic Machine.
 *
 * Description:
 *   The MBR acts as a temporary data buffer between the CPU and
 *   main memory. All data transfers to and from memory pass through
 *   this register.
 *
 * Relationship with MAR:
 *   - MAR specifies *where* to access in memory.
 *   - MBR holds *what* is being read from or written to memory.
 *
 * Function:
 *   - On READ  → MBR ← M[MAR]
 *   - On WRITE → M[MAR] ← MBR
 *
 * Width:
 *   - 16 bits (each memory word in the Basic Machine is 16 bits wide)
 *
 * Behavior:
 *   The MBR models the “one-cycle delay” of real hardware:
 *   when the CPU requests data from memory, it becomes available
 *   in the MBR during the next cycle.
 * ==============================================================
 */
public class MBR {

    /* --------------------------------------------------------------
     * Field: data
     * --------------------------------------------------------------
     * Holds the 16-bit word currently being transferred.
     * This can represent either:
     *   - An instruction (fetched from memory)
     *   - A data value (loaded or stored)
     */
    private int data;

    /* ==============================================================
     * set()
     * --------------------------------------------------------------
     * Loads a new 16-bit value into the MBR.
     *
     * Called by:
     *   - CPU after reading from memory (MBR ← M[MAR])
     *   - CPU before writing to memory (MBR ← value from register)
     *
     * Parameters:
     *   value — integer to be stored (only lower 16 bits are kept)
     *
     * Implementation Detail:
     *   - Bitwise AND with 0xFFFF ensures 16-bit masking.
     *   - Prevents overflow or invalid word size.
     * ==============================================================
     */
    public void set(int value) {
        data = value & 0xFFFF;  // Enforce 16-bit word width
    }

    /* ==============================================================
     * get()
     * --------------------------------------------------------------
     * Returns the current 16-bit word stored in the MBR.
     *
     * Used by:
     *   - CPU to transfer data into registers (e.g., during LDR)
     *   - Memory to write data to a specific address (Mem[MAR] ← MBR)
     *
     * Example:
     *   // During fetch
     *   MAR ← PC
     *   MBR ← M[MAR]
     *   IR  ← MBR
     * ==============================================================
     */
    public int get() {
        return data;
    }

    /* ==============================================================
     * Notes:
     * --------------------------------------------------------------
     * - The MBR always holds the *last word transferred* between
     *   CPU and memory.
     * - Combined with MAR, it models the data/address pathway
     *   of a real single-port memory bus.
     * - Accessed directly by the MachineCore during each fetch cycle.
     * ==============================================================
     */
}
