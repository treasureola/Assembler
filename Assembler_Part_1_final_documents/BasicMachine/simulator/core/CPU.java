package BasicMachine.simulator.core;

/* ==============================================================
 * CPU.java
 * --------------------------------------------------------------
 * Purpose:
 *   Provides a minimal CPU container class for backward compatibility
 *   with early versions of the simulator UI or test scripts.
 *
 * Background:
 *   In the original Part 0 implementation, the CPU was modeled as a
 *   single class containing both registers and memory.
 *
 *   In the current modular design (Part 1 and beyond), CPU behavior
 *   has been refactored into:
 *      - MachineCore   → fetch–decode–execute logic
 *      - Registers     → all general and control registers
 *      - Memory        → 2048 × 16-bit single-port memory
 *
 *   However, some parts of the simulator UI (FXML or controllers)
 *   may still expect a "CPU" class with a public `memory` field.
 *   This class preserves compatibility for those cases.
 * ==============================================================
 */
public class CPU {

    /* --------------------------------------------------------------
     * Field: memory
     * --------------------------------------------------------------
     * A simple 2K-word memory array (2048 × 16 bits).
     * Exists only for backward compatibility with UI components
     * that reference `CPU.memory`.
     *
     * Note:
     *   This array is *not* the authoritative memory for the system.
     *   The active memory resides in BasicMachine.Memory_Module.Memory.
     */
    private final int[] memory = new int[2048];

    /* ==============================================================
     * memory()
     * --------------------------------------------------------------
     * Returns the reference to the internal memory array.
     *
     * Purpose:
     *   - Allows older UI code to access or display memory contents.
     *   - Provides a placeholder to prevent null reference errors
     *     if the legacy code still expects `CPU.memory()`.
     *
     * Example:
     *   int[] mem = cpu.memory();
     *   mem[10] = 012345;  // legacy compatibility only
     * ==============================================================
     */
    public int[] memory() {
        return memory;
    }

    /* ==============================================================
     * Notes:
     * --------------------------------------------------------------
     * - This class intentionally performs no real computation.
     * - The “authoritative” CPU behavior is implemented in:
     *       → MachineCore  (execution logic)
     *       → Registers    (data and control registers)
     *       → Memory       (main memory model)
     *
     * - Keeping this class ensures the simulator remains compatible
     *   with all previously linked UI controllers and testing scripts.
     * ==============================================================
     */
}
