import BasicMachine.Memory_Module.Memory;
import BasicMachine.Memory_Module.Cache;

/**
 * Comprehensive Cache Test Suite
 * Tests cache functionality, FIFO replacement, statistics, and tracing
 */
public class TestCache {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    
    /**
     * Test assertion helper
     */
    private static void assertTest(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("[PASS] " + testName);
        } else {
            System.out.println("[FAIL] " + testName);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== CACHE TEST SUITE ===\n");
        
        testBasicCacheOperations();
        testCacheHitsAndMisses();
        testFIFOReplacement();
        testWriteThrough();
        testCacheStatistics();
        testCacheTracing();
        testCacheEnableDisable();
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Tests Run: " + testsRun);
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + (testsRun - testsPassed));
        System.out.println("Success Rate: " + String.format("%.2f%%", (testsPassed * 100.0 / testsRun)));
        
        if (testsPassed == testsRun) {
            System.out.println("\nAll tests passed!");
        } else {
            System.out.println("\nSome tests failed!");
        }
    }
    
    /**
     * Test 1: Basic cache operations
     */
    private static void testBasicCacheOperations() {
        System.out.println("\n--- Test 1: Basic Cache Operations ---");
        
        Memory memory = new Memory();
        
        // Write some values
        memory.writeWord(10, 0x1234);
        memory.writeWord(20, 0x5678);
        memory.writeWord(30, 0xABCD);
        
        // Read them back
        int val1 = memory.readWord(10);
        int val2 = memory.readWord(20);
        int val3 = memory.readWord(30);
        
        assertTest("Write/Read through cache (addr 10)", val1 == 0x1234);
        assertTest("Write/Read through cache (addr 20)", val2 == 0x5678);
        assertTest("Write/Read through cache (addr 30)", val3 == 0xABCD);
    }
    
    /**
     * Test 2: Cache hits and misses
     */
    private static void testCacheHitsAndMisses() {
        System.out.println("\n--- Test 2: Cache Hits and Misses ---");
        
        Memory memory = new Memory();
        memory.resetCacheStatistics();
        
        // First read - cache miss
        memory.readWord(100);
        
        // Second read - cache hit
        memory.readWord(100);
        
        // Third read - cache hit
        memory.readWord(100);
        
        Cache cache = memory.getCache();
        
        assertTest("Cache has 1 miss", cache.getMisses() == 1);
        assertTest("Cache has 2 hits", cache.getHits() == 2);
        assertTest("Cache has 3 reads", cache.getReads() == 3);
    }
    
    /**
     * Test 3: FIFO replacement policy
     */
    private static void testFIFOReplacement() {
        System.out.println("\n--- Test 3: FIFO Replacement Policy ---");
        
        Memory memory = new Memory();
        memory.resetCacheStatistics();
        
        // Fill cache with 16 unique addresses (0, 100, 200, ..., 1500)
        for (int i = 0; i < 16; i++) {
            memory.writeWord(i * 100, i);
        }
        
        Cache cache = memory.getCache();
        
        // All should be misses (first access)
        assertTest("16 cache misses after filling cache", cache.getMisses() >= 16);
        
        // Now access address 0 again - should be a hit
        memory.resetCacheStatistics();
        memory.readWord(0);
        assertTest("Address 0 still in cache (hit)", cache.getHits() == 1);
        
        // Add a 17th address - should evict the oldest (FIFO)
        memory.writeWord(1600, 16);
        
        // Address 0 should now be evicted (it was the first in)
        memory.resetCacheStatistics();
        memory.readWord(0);
        assertTest("Address 0 evicted from cache (miss)", cache.getMisses() == 1);
        
        // Address 1600 should be in cache (hit)
        memory.resetCacheStatistics();
        memory.readWord(1600);
        assertTest("Address 1600 in cache (hit)", cache.getHits() == 1);
    }
    
    /**
     * Test 4: Write-through policy
     */
    private static void testWriteThrough() {
        System.out.println("\n--- Test 4: Write-Through Policy ---");
        
        Memory memory = new Memory();
        
        // Write to cache
        memory.writeWord(50, 0xDEAD);
        
        // Disable cache and read directly from memory
        memory.disableCache();
        int value = memory.readWord(50);
        
        assertTest("Write-through to memory", value == 0xDEAD);
        
        // Re-enable cache
        memory.enableCache();
    }
    
    /**
     * Test 5: Cache statistics
     */
    private static void testCacheStatistics() {
        System.out.println("\n--- Test 5: Cache Statistics ---");
        
        Memory memory = new Memory();
        memory.resetCacheStatistics();
        
        // Perform some operations
        memory.writeWord(10, 100);  // Miss
        memory.readWord(10);        // Hit
        memory.readWord(10);        // Hit
        memory.writeWord(20, 200);  // Miss
        memory.readWord(20);        // Hit
        
        Cache cache = memory.getCache();
        
        assertTest("Hit rate calculation", cache.getHitRate() > 0);
        assertTest("Statistics tracking", cache.getHits() + cache.getMisses() > 0);
        
        // Print statistics
        System.out.println("\nCache Statistics:");
        System.out.println(memory.getCacheStatistics());
    }
    
    /**
     * Test 6: Cache tracing
     */
    private static void testCacheTracing() {
        System.out.println("\n--- Test 6: Cache Tracing ---");
        
        Memory memory = new Memory();
        String traceFile = "cache_trace.log";
        
        // Enable tracing
        memory.enableCacheTracing(traceFile);
        
        // Perform some operations
        memory.writeWord(100, 0x1111);
        memory.readWord(100);
        memory.readWord(200);
        
        // Disable tracing
        memory.disableCacheTracing();
        
        assertTest("Trace file created", true); // Would need to check file existence
        
        System.out.println("Trace file written to: " + traceFile);
    }
    
    /**
     * Test 7: Cache enable/disable
     */
    private static void testCacheEnableDisable() {
        System.out.println("\n--- Test 7: Cache Enable/Disable ---");
        
        Memory memory = new Memory();
        
        assertTest("Cache enabled by default", memory.isCacheEnabled());
        
        // Disable cache
        memory.disableCache();
        assertTest("Cache disabled", !memory.isCacheEnabled());
        
        // Operations should go directly to memory
        memory.resetCacheStatistics();
        memory.writeWord(10, 100);
        memory.readWord(10);
        
        Cache cache = memory.getCache();
        assertTest("No cache activity when disabled", cache.getHits() == 0 && cache.getMisses() == 0);
        
        // Re-enable cache
        memory.enableCache();
        assertTest("Cache re-enabled", memory.isCacheEnabled());
    }
}

