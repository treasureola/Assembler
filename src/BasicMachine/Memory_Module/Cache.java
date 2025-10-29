package BasicMachine.Memory_Module;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple Cache Implementation
 * - Fully associative, unified cache (for both instructions and data)
 * - 16 cache lines
 * - FIFO (First In, First Out) replacement policy
 * - Write-through policy (writes go to both cache and memory)
 */
public class Cache {
    private static final int CACHE_SIZE = 16;
    
    // Cache line structure
    private class CacheLine {
        boolean valid;      // Valid bit
        int tag;           // Memory address (tag)
        int data;          // Data stored (16-bit word)
        int fifoIndex;     // FIFO ordering index
        
        public CacheLine() {
            this.valid = false;
            this.tag = -1;
            this.data = 0;
            this.fifoIndex = 0;
        }
        
        @Override
        public String toString() {
            return String.format("Valid=%b, Tag=%04o, Data=%06o, FIFO=%d", 
                               valid, tag, data & 0xFFFF, fifoIndex);
        }
    }
    
    // Cache storage
    private CacheLine[] cacheLines;
    
    // FIFO counter for replacement policy
    private int fifoCounter;
    
    // Cache statistics
    private int hits;
    private int misses;
    private int reads;
    private int writes;
    
    // Trace file for debugging
    private PrintWriter traceWriter;
    private boolean tracingEnabled;
    
    /**
     * Constructor - Initialize cache
     */
    public Cache() {
        cacheLines = new CacheLine[CACHE_SIZE];
        for (int i = 0; i < CACHE_SIZE; i++) {
            cacheLines[i] = new CacheLine();
        }
        fifoCounter = 0;
        hits = 0;
        misses = 0;
        reads = 0;
        writes = 0;
        tracingEnabled = false;
    }
    
    /**
     * Enable trace logging to file
     */
    public void enableTracing(String filename) {
        try {
            traceWriter = new PrintWriter(new FileWriter(filename, true));
            tracingEnabled = true;
            writeTrace("=== Cache Trace Started at " + getCurrentTimestamp() + " ===");
            writeTrace("Cache Configuration: Size=" + CACHE_SIZE + " lines, Policy=FIFO, Type=Unified");
        } catch (IOException e) {
            System.err.println("Failed to enable cache tracing: " + e.getMessage());
            tracingEnabled = false;
        }
    }
    
    /**
     * Disable trace logging
     */
    public void disableTracing() {
        if (tracingEnabled && traceWriter != null) {
            writeTrace("=== Cache Trace Ended at " + getCurrentTimestamp() + " ===");
            writeTrace("");
            traceWriter.close();
            tracingEnabled = false;
        }
    }
    
    /**
     * Write to trace file
     */
    private void writeTrace(String message) {
        if (tracingEnabled && traceWriter != null) {
            traceWriter.println(message);
            traceWriter.flush();
        }
    }
    
    /**
     * Get current timestamp for trace
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Read from cache (checks cache first, then fetches from memory if needed)
     * @param address Memory address
     * @param memory Reference to main memory
     * @return Data value
     */
    public int read(int address, Memory memory) {
        reads++;
        
        // Search for address in cache
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cacheLines[i].valid && cacheLines[i].tag == address) {
                // Cache hit
                hits++;
                writeTrace(String.format("READ  HIT  | Addr=%04o | Data=%06o | Line=%d", 
                                       address, cacheLines[i].data & 0xFFFF, i));
                return cacheLines[i].data;
            }
        }
        
        // Cache miss - fetch from memory
        misses++;
        int data = memory.readWordDirect(address);
        
        // Insert into cache using FIFO
        int victimLine = findFIFOVictim();
        
        if (cacheLines[victimLine].valid) {
            writeTrace(String.format("READ  MISS | Addr=%04o | Data=%06o | Evict Line=%d (Tag=%04o)", 
                                   address, data & 0xFFFF, victimLine, cacheLines[victimLine].tag));
        } else {
            writeTrace(String.format("READ  MISS | Addr=%04o | Data=%06o | Insert Line=%d", 
                                   address, data & 0xFFFF, victimLine));
        }
        
        // Update cache line
        cacheLines[victimLine].valid = true;
        cacheLines[victimLine].tag = address;
        cacheLines[victimLine].data = data;
        cacheLines[victimLine].fifoIndex = fifoCounter++;
        
        return data;
    }
    
    /**
     * Write to cache (write-through: update both cache and memory)
     * @param address Memory address
     * @param data Data to write
     * @param memory Reference to main memory
     */
    public void write(int address, int data, Memory memory) {
        writes++;
        
        // Write to memory (write-through policy)
        memory.writeWordDirect(address, data);
        
        // Check if address is in cache
        boolean found = false;
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cacheLines[i].valid && cacheLines[i].tag == address) {
                // Update cache line
                cacheLines[i].data = data;
                found = true;
                writeTrace(String.format("WRITE HIT  | Addr=%04o | Data=%06o | Line=%d", 
                                       address, data & 0xFFFF, i));
                break;
            }
        }
        
        if (!found) {
            // Cache miss on write - allocate cache line (write-allocate)
            int victimLine = findFIFOVictim();
            
            if (cacheLines[victimLine].valid) {
                writeTrace(String.format("WRITE MISS | Addr=%04o | Data=%06o | Evict Line=%d (Tag=%04o)", 
                                       address, data & 0xFFFF, victimLine, cacheLines[victimLine].tag));
            } else {
                writeTrace(String.format("WRITE MISS | Addr=%04o | Data=%06o | Insert Line=%d", 
                                       address, data & 0xFFFF, victimLine));
            }
            
            cacheLines[victimLine].valid = true;
            cacheLines[victimLine].tag = address;
            cacheLines[victimLine].data = data;
            cacheLines[victimLine].fifoIndex = fifoCounter++;
        }
    }
    
    /**
     * Find victim cache line using FIFO policy
     * @return Index of cache line to replace
     */
    private int findFIFOVictim() {
        // First check for invalid (empty) lines
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (!cacheLines[i].valid) {
                return i;
            }
        }
        
        // All lines valid - find oldest (smallest fifoIndex)
        int oldestLine = 0;
        int oldestIndex = cacheLines[0].fifoIndex;
        
        for (int i = 1; i < CACHE_SIZE; i++) {
            if (cacheLines[i].fifoIndex < oldestIndex) {
                oldestIndex = cacheLines[i].fifoIndex;
                oldestLine = i;
            }
        }
        
        return oldestLine;
    }
    
    /**
     * Reset cache (clear all lines and statistics)
     */
    public void reset() {
        for (int i = 0; i < CACHE_SIZE; i++) {
            cacheLines[i].valid = false;
            cacheLines[i].tag = -1;
            cacheLines[i].data = 0;
            cacheLines[i].fifoIndex = 0;
        }
        fifoCounter = 0;
        
        writeTrace("CACHE RESET");
    }
    
    /**
     * Reset statistics only (keep cache contents)
     */
    public void resetStatistics() {
        hits = 0;
        misses = 0;
        reads = 0;
        writes = 0;
        writeTrace("STATISTICS RESET");
    }
    
    /**
     * Get cache hit rate
     * @return Hit rate as percentage (0.0 to 100.0)
     */
    public double getHitRate() {
        int totalAccesses = hits + misses;
        if (totalAccesses == 0) {
            return 0.0;
        }
        return (hits * 100.0) / totalAccesses;
    }
    
    /**
     * Get cache statistics as formatted string
     */
    public String getStatistics() {
        int totalAccesses = hits + misses;
        return String.format("Cache Statistics:\n" +
                           "  Total Accesses: %d\n" +
                           "  Hits: %d\n" +
                           "  Misses: %d\n" +
                           "  Hit Rate: %.2f%%\n" +
                           "  Reads: %d\n" +
                           "  Writes: %d",
                           totalAccesses, hits, misses, getHitRate(), reads, writes);
    }
    
    /**
     * Get cache contents as formatted string
     */
    public String getCacheContents() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cache Contents (FIFO Order):\n");
        sb.append(String.format("%-5s %-6s %-8s %-10s %-8s\n", 
                               "Line", "Valid", "Tag(Oct)", "Data(Oct)", "FIFO"));
        sb.append("---------------------------------------------------\n");
        
        for (int i = 0; i < CACHE_SIZE; i++) {
            sb.append(String.format("%-5d %-6s %-8s %-10s %-8d\n",
                                  i,
                                  cacheLines[i].valid ? "Y" : "N",
                                  cacheLines[i].valid ? String.format("%04o", cacheLines[i].tag) : "----",
                                  cacheLines[i].valid ? String.format("%06o", cacheLines[i].data & 0xFFFF) : "------",
                                  cacheLines[i].fifoIndex));
        }
        
        return sb.toString();
    }
    
    // Getters for statistics
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public int getReads() { return reads; }
    public int getWrites() { return writes; }
    public int getCacheSize() { return CACHE_SIZE; }
    
    /**
     * Check if address is in cache
     */
    public boolean isInCache(int address) {
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cacheLines[i].valid && cacheLines[i].tag == address) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get cache line information for debugging
     */
    public String getCacheLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= CACHE_SIZE) {
            return "Invalid line index";
        }
        return cacheLines[lineIndex].toString();
    }
}

