package BasicMachine.Memory_Module;

/* ==============================================================
 * Memory.java
 * --------------------------------------------------------------
 * Purpose:
 *   Implements the Basic Machine’s single-port main memory.
 *   Provides read and write access for both the CPU (via MAR/MBR)
 *   and the ProgramLoader (for loading .ld files).
 *
 * Architecture:
 *   - 2048 words (each word = 16 bits)
 *   - Address range: 0–2047 (11 bits)
 *   - Single-port model → one operation per cycle (read OR write)
 *
 * Role in System:
 *   - The CPU interacts with memory using MAR (address) and MBR (data).
 *   - The ProgramLoader writes binary words into memory from the .ld file.
 *   - The Simulator UI can display memory contents in octal format.
 *
 * Example Access:
 *   MAR ← 0010
 *   MBR ← 102110
 *   M[MAR] ← MBR
 *
 * ==============================================================
 */
public class Memory {

    /* --------------------------------------------------------------
     * CONSTANTS AND FIELDS
     * --------------------------------------------------------------
     * SIZE: total memory capacity (2K words = 2048 × 16-bit)
     * mem : integer array representing each 16-bit word
     * programStartAddr: keeps track of the first instruction address
     *                   after a program is loaded (used for IPL/startup)
     */
    private static final int SIZE = 2048;
    private final int[] mem = new int[SIZE];
    private int programStartAddr = 0;

    /* ==============================================================
     * reset()
     * --------------------------------------------------------------
     * Resets all memory cells to zero.
     *
     * Called by:
     *   - The simulator “Reset” button (before loading a new program)
     *   - System initialization (to simulate power-on clear)
     *
     * Behavior:
     *   - Loops through all 2048 words and sets them to 0.
     *   - Clears the recorded program start address.
     * ==============================================================
     */
    public void reset() {
        for (int i = 0; i < SIZE; i++) mem[i] = 0;
        programStartAddr = 0;
    }

    /* ==============================================================
     * writeWord()
     * --------------------------------------------------------------
     * Writes a single 16-bit word into memory.
     *
     * Parameters:
     *   address — memory address (0–2047)
     *   value   — data word to store (only lower 16 bits used)
     *
     * Called by:
     *   - CPU (during STR or store instructions)
     *   - ProgramLoader (when loading .ld program files)
     *
     * Throws:
     *   IllegalArgumentException if the address is outside 0–2047.
     * ==============================================================
     */
    public void writeWord(int address, int value) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);
        mem[address] = value & 0xFFFF;  // Mask to 16 bits
    }

    /* ==============================================================
     * readWord()
     * --------------------------------------------------------------
     * Reads a single 16-bit word from memory.
     *
     * Parameters:
     *   address — memory address (0–2047)
     *
     * Returns:
     *   The 16-bit word stored at that address.
     *
     * Called by:
     *   - CPU (during LDR, LDA, or instruction fetch)
     *   - ProgramLoader (to verify memory contents if needed)
     *
     * Throws:
     *   IllegalArgumentException if address is out of range.
     * ==============================================================
     */
    public int readWord(int address) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);
        return mem[address] & 0xFFFF;  // Return 16-bit masked value
    }

    /* ==============================================================
     * size()
     * --------------------------------------------------------------
     * Returns total number of addressable memory words.
     *
     * Useful for:
     *   - ProgramLoader bounds checking
     *   - Simulator UI display and validation
     * ==============================================================
     */
    public int size() {
        return SIZE;
    }

    /* ==============================================================
     * setProgramStartAddress()
     * --------------------------------------------------------------
     * Records the starting address of the loaded program.
     * This is typically the first executable instruction in memory.
     *
     * Called by:
     *   - ProgramLoader after loading a .ld file
     *   - Allows CPU to set PC ← programStartAddr during IPL
     * ==============================================================
     */
    public void setProgramStartAddress(int addr) {
        this.programStartAddr = addr & 0x7FF;  // Mask to 11 bits
    }

    /* ==============================================================
     * getProgramStartAddress()
     * --------------------------------------------------------------
     * Returns the stored program start address.
     * Used by the CPU or simulator during initial program load (IPL).
     * ==============================================================
     */
    public int getProgramStartAddress() {
        return programStartAddr;
    }

    /* ==============================================================
     * Notes:
     * --------------------------------------------------------------
     * - Memory is modeled as a single integer array for simplicity.
     * - This module abstracts physical memory timing but preserves
     *   word-level accuracy and bus width.
     * - Together with MAR and MBR, this forms the full
     *   CPU–Memory interface.
     * ==============================================================
     */
}
