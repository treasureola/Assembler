package BasicMachine.simulator.core;

/* ==============================================================
 * Utils.java
 * --------------------------------------------------------------
 * Purpose:
 *   Provides general-purpose utility methods used throughout
 *   the Basic Machine simulator’s backend and user interface.
 *
 * Overview:
 *   - Handles conversion between binary integers and octal strings.
 *   - Used extensively by the UI for display, parsing, and debugging.
 *
 * Context:
 *   In this project, all addresses, instructions, and memory values
 *   are represented in *octal* notation (base 8) — matching the
 *   original Basic Machine specification.
 *
 * Examples:
 *   Decimal  → Octal:   10 → 000012
 *   Octal    → Decimal: 000012 → 10
 *
 * ==============================================================
 */
public final class Utils {

    /* ==============================================================
     * Constructor
     * --------------------------------------------------------------
     * Private constructor to prevent instantiation.
     *
     * Reason:
     *   This is a static utility class — no instance should ever be
     *   created (i.e., you should never call `new Utils()`).
     * ==============================================================
     */
    private Utils() {}

    /* ==============================================================
     * oct()
     * --------------------------------------------------------------
     * Converts a 16-bit signed value into a 6-digit octal string.
     *
     * Input:
     *   v — 16-bit short value (can represent register or memory word)
     *
     * Output:
     *   Zero-padded 6-character string in base 8.
     *
     * Example:
     *   oct((short)10)   → "000012"
     *   oct((short)4096) → "010000"
     *
     * Used by:
     *   - SimulatorController.java  → to display register contents
     *   - Memory window             → to show memory cells in octal
     *   - Debug logs (MachineCore)  → instruction trace output
     *
     * Implementation:
     *   - Masks to 16 bits (v & 0xFFFF) to handle signed/unsigned cases.
     *   - Uses Java’s String.format() to format the number in base 8.
     * ==============================================================
     */
    public static String oct(short v) {
        int u = v & 0xFFFF;               // Normalize to 16-bit unsigned
        return String.format("%06o", u);  // Format as zero-padded 6-digit octal
    }

    /* ==============================================================
     * parseOctal()
     * --------------------------------------------------------------
     * Converts a string containing an octal value (e.g., "020011")
     * into its decimal integer equivalent.
     *
     * Input:
     *   s — octal string (as entered by user or read from file)
     *
     * Output:
     *   Integer equivalent of the octal value.
     *
     * Example:
     *   parseOctal("020011") → 8209
     *
     * Used by:
     *   - SimulatorController → user-entered memory operations (Load/Store)
     *   - ProgramLoader       → reading `.ld` files
     *
     * Implementation:
     *   - Validates that input is not null or empty.
     *   - Uses built-in Integer.parseInt(string, 8) for base-8 parsing.
     *
     * Throws:
     *   IllegalArgumentException if input is blank or invalid.
     * ==============================================================
     */
    public static int parseOctal(String s) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException("empty octal");
        return Integer.parseInt(s.trim(), 8);  // Convert from base-8 string to int
    }

    /* ==============================================================
     * Notes:
     * --------------------------------------------------------------
     * - All simulator I/O uses octal for consistency with instruction
     *   formats and memory dump listings.
     * - This class centralizes conversions so that display, parsing,
     *   and internal computation remain synchronized.
     * ==============================================================
     */
}
