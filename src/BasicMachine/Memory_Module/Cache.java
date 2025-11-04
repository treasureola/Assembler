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
    private int totalAccesses;

    // Cache line structure
    private class CacheLine {
        boolean valid;
        int tag;
        int data;
        int fifoIndex;

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
    private int fifoCounter;

    // Cache statistics
    private int hits;
    private int misses;
    private int reads;
    private int writes;

    // Trace file for debugging
    private PrintWriter traceWriter;
    private boolean tracingEnabled;

    /** Constructor - Initialize cache */
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
        totalAccesses = 0;
        tracingEnabled = false;
    }

    /** Enable trace logging to file */
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

    public void disableTracing() {
        if (tracingEnabled && traceWriter != null) {
            writeTrace("=== Cache Trace Ended at " + getCurrentTimestamp() + " ===");
            writeTrace("");
            traceWriter.close();
            tracingEnabled = false;
        }
    }

    private void writeTrace(String message) {
        if (tracingEnabled && traceWriter != null) {
            traceWriter.println(message);
            traceWriter.flush();
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** Read from cache */
    public int read(int address, Memory memory) {
        totalAccesses++;
        reads++;

        // Search for address in cache
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cacheLines[i].valid && cacheLines[i].tag == address) {
                hits++;
                writeTrace(String.format("READ  HIT  | Addr=%04o | Data=%06o | Line=%d",
                        address, cacheLines[i].data & 0xFFFF, i));
                return cacheLines[i].data;
            }
        }

        // Cache miss
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

        cacheLines[victimLine].valid = true;
        cacheLines[victimLine].tag = address;
        cacheLines[victimLine].data = data;
        cacheLines[victimLine].fifoIndex = fifoCounter++;

        return data;
    }

    /** Write to cache (write-through) */
    public void write(int address, int data, Memory memory) {
        totalAccesses++;
        writes++;

        // Always write through to memory
        memory.writeWordDirect(address, data);

        boolean found = false;
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cacheLines[i].valid && cacheLines[i].tag == address) {
                cacheLines[i].data = data;
                hits++;
                found = true;
                writeTrace(String.format("WRITE HIT  | Addr=%04o | Data=%06o | Line=%d",
                        address, data & 0xFFFF, i));
                break;
            }
        }

        if (!found) {
            misses++;
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

    /** FIFO victim */
    private int findFIFOVictim() {
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (!cacheLines[i].valid) return i;
        }
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

    /** Reset cache */
    public void reset() {
        for (int i = 0; i < CACHE_SIZE; i++) {
            cacheLines[i].valid = false;
            cacheLines[i].tag = -1;
            cacheLines[i].data = 0;
            cacheLines[i].fifoIndex = 0;
        }
        fifoCounter = 0;
        hits = 0;
        misses = 0;
        reads = 0;
        writes = 0;
        totalAccesses = 0;
        writeTrace("CACHE RESET");
    }

    /** Reset statistics only */
    public void resetStatistics() {
        hits = 0;
        misses = 0;
        reads = 0;
        writes = 0;
        totalAccesses = 0;
        writeTrace("STATISTICS RESET");
    }

    /** Hit rate as percentage */
    public double getHitRate() {
        if (totalAccesses == 0) return 0.0;
        return (hits * 100.0) / totalAccesses;
    }

    /** Cache statistics string */
    public String getStatistics() {
        return String.format("Cache Statistics:\n" +
                        "  Total Accesses: %d\n" +
                        "  Hits: %d\n" +
                        "  Misses: %d\n" +
                        "  Hit Rate: %.2f%%\n" +
                        "  Reads: %d\n" +
                        "  Writes: %d",
                totalAccesses, hits, misses, getHitRate(), reads, writes);
    }

    /** Cache contents string */
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

    // Getters
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public int getReads() { return reads; }
    public int getWrites() { return writes; }
    public int getCacheSize() { return CACHE_SIZE; }

    public boolean isInCache(int address) {
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cacheLines[i].valid && cacheLines[i].tag == address) return true;
        }
        return false;
    }

    public String getCacheLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= CACHE_SIZE) return "Invalid line index";
        return cacheLines[lineIndex].toString();
    }
}
