package BasicMachine.Memory_Module;

/* ==============================================================
 * Memory.java
 * --------------------------------------------------------------
 * Purpose:
 *   Implements the Basic Machine's single-port main memory with cache.
 *   Provides read and write access for both the CPU (via MAR/MBR)
 *   and the ProgramLoader (for loading .ld files).
 *
 * Architecture:
 *   - 2048 words (each word = 16 bits)
 *   - Address range: 0–2047 (11 bits)
 *   - Single-port model → one operation per cycle (read OR write)
 *   - Integrated with 16-line fully-associative cache (FIFO replacement)
 *
 * Role in System:
 *   - The CPU interacts with memory using MAR (address) and MBR (data).
 *   - Cache sits between CPU and main memory for performance.
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
     * cache: integrated cache for faster memory access
     * cacheEnabled: flag to enable/disable cache (for testing)
     */
    private static final int SIZE = 2048;
    private final int[] mem = new int[SIZE];
    private int programStartAddr = 0;
    private Cache cache;
    private boolean cacheEnabled;

    /* ==============================================================
     * Constructor
     * --------------------------------------------------------------
     * Initializes memory with cache enabled by default.
     * ==============================================================
     */
    public Memory() {
        this.cache = new Cache();
        this.cacheEnabled = true;
    }

    /* ==============================================================
     * reset()
     * --------------------------------------------------------------
     * Resets all memory cells to zero.
     *
     * Called by:
     *   - The simulator "Reset" button (before loading a new program)
     *   - System initialization (to simulate power-on clear)
     *
     * Behavior:
     *   - Loops through all 2048 words and sets them to 0.
     *   - Clears the recorded program start address.
     *   - Resets the cache if enabled.
     * ==============================================================
     */
    public void reset() {
        for (int i = 0; i < SIZE; i++) mem[i] = 0;
        programStartAddr = 0;
        if (cacheEnabled && cache != null) {
            cache.reset();
        }
    }

    /* ==============================================================
     * writeWord()
     * --------------------------------------------------------------
     * Writes a single 16-bit word into memory through cache.
     *
     * Parameters:
     *   address — memory address (0–2047)
     *   value   — data word to store (only lower 16 bits used)
     *
     * Called by:
     *   - CPU (during STR or store instructions)
     *   - ProgramLoader (when loading .ld program files)
     *
     * Behavior:
     *   - If cache is enabled, writes through cache (write-through policy)
     *   - Otherwise, writes directly to memory
     *
     * Throws:
     *   IllegalArgumentException if the address is outside 0–2047.
     * ==============================================================
     */
    public void writeWord(int address, int value) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);

        if (cacheEnabled && cache != null) {
            cache.write(address, value, this);
        } else {
            mem[address] = value & 0xFFFF;  // Mask to 16 bits
        }
//        System.out.printf("[DEBUG] writeWord: addr=%04o val=%06o%n", address, value & 0xFFFF);
    }

    /* ==============================================================
     * readWord()
     * --------------------------------------------------------------
     * Reads a single 16-bit word from memory through cache.
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
     * Behavior:
     *   - If cache is enabled, reads through cache (checks cache first)
     *   - Otherwise, reads directly from memory
     *
     * Throws:
     *   IllegalArgumentException if address is out of range.
     * ==============================================================
     */
    public int readWord(int address) {
//        System.out.printf("[DEBUG] readWord: addr=%04o val=%06o%n", address, mem[address]);
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);

        if (cacheEnabled && cache != null) {
            return cache.read(address, this);
        } else {
            return mem[address] & 0xFFFF;  // Return 16-bit masked value
        }
    }

    /* ==============================================================
     * readWordDirect()
     * --------------------------------------------------------------
     * Direct memory read bypassing cache (used by cache miss handler).
     *
     * Parameters:
     *   address — memory address (0–2047)
     *
     * Returns:
     *   The 16-bit word stored at that address.
     * ==============================================================
     */
    public int readWordDirect(int address) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);
        return mem[address] & 0xFFFF;
    }
    
    /* ==============================================================
     * writeWordDirect()
     * --------------------------------------------------------------
     * Direct memory write bypassing cache (used by cache write-through).
     *
     * Parameters:
     *   address — memory address (0–2047)
     *   value   — data word to store
     * ==============================================================
     */
    public void writeWordDirect(int address, int value) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);
        mem[address] = value & 0xFFFF;
    }

    // ===============================================================
// Cache-aware CPU access wrappers
// ===============================================================

    /**
     * Read a word for CPU execution (counts toward cache stats).
     */
    public int readForCPU(int address) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);

        // Go through cache if enabled
        if (cacheEnabled && cache != null) {
            return cache.read(address, this) & 0xFFFF;
        }
        return mem[address] & 0xFFFF;
    }

    /**
     * Write a word for CPU execution (counts toward cache stats).
     */
    public void writeFromCPU(int address, int value) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);

        // Go through cache if enabled
        if (cacheEnabled && cache != null) {
            cache.write(address, value, this);
        } else {
            mem[address] = value & 0xFFFF;
        }
    }


    /* ==============================================================
     * peekWord()
     * --------------------------------------------------------------
     * Display-only direct read that bypasses the cache and
     * does not affect cache statistics or timing.
     *
     * Used by:
     *   - SimulatorController.refreshAll() to safely display memory
     *     contents without triggering cache activity.
     * ==============================================================
     */
    public int peekWord(int address) {
        if (address < 0 || address >= SIZE)
            throw new IllegalArgumentException("Invalid memory address: " + address);
        return mem[address] & 0xFFFF;
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
     * Cache Control and Statistics Methods
     * --------------------------------------------------------------
     */
    
    /**
     * Enable cache
     */
    public void enableCache() {
        this.cacheEnabled = true;
    }
    
    /**
     * Disable cache (direct memory access only)
     */
    public void disableCache() {
        this.cacheEnabled = false;
    }
    
    /**
     * Check if cache is enabled
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
    
    /**
     * Get cache reference for statistics and control
     */
    public Cache getCache() {
        return cache;
    }
    
    /**
     * Enable cache tracing to file
     */
    public void enableCacheTracing(String filename) {
        if (cache != null) {
            cache.enableTracing(filename);
        }
    }
    
    /**
     * Disable cache tracing
     */
    public void disableCacheTracing() {
        if (cache != null) {
            cache.disableTracing();
        }
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStatistics() {
        if (cache != null) {
            return cache.getStatistics();
        }
        return "Cache not available";
    }
    
    /**
     * Get cache contents
     */
    public String getCacheContents() {
        if (cache != null) {
            return cache.getCacheContents();
        }
        return "Cache not available";
    }
    
    /**
     * Reset cache statistics only
     */
    public void resetCacheStatistics() {
        if (cache != null) {
            cache.resetStatistics();
        }
    }

    /* ==============================================================
     * Notes:
     * --------------------------------------------------------------
     * - Memory is modeled as a single integer array for simplicity.
     * - This module abstracts physical memory timing but preserves
     *   word-level accuracy and bus width.
     * - Together with MAR and MBR, this forms the full
     *   CPU–Memory interface.
     * - Cache integration provides performance improvement while
     *   maintaining transparency to the CPU.
     * ==============================================================
     */
}
