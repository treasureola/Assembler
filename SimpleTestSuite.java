import BasicMachine.Memory_Module.Memory;
import BasicMachine.CPU_Module.Registers;
import BasicMachine.CPU_Module.MachineCore;
import BasicMachine.simulator.ProgramLoader;
import java.io.*;

/**
 * Simplified Test Suite for Basic Machine Simulator
 * Testing and Documentation
 */
public class SimpleTestSuite {
    private static int testsPassed = 0;
    private static int testsTotal = 0;
    
    public static void main(String[] args) {
        System.out.println("=== SIMPLIFIED TEST SUITE ===");
        System.out.println("Testing and Documentation\n");
        
        // Run basic tests
        testMemoryReset();
        testBasicMemoryOperations();
        testRegisterOperations();
        testProgramLoading();
        testCPUExecution();
        
        // Print summary
        printTestSummary();
    }
    
    /**
     * Test 1: Memory Reset Test
     */
    private static void testMemoryReset() {
        startTest("Memory Reset Test");
        
        Memory memory = new Memory();
        memory.reset();
        
        boolean allZero = true;
        for (int i = 0; i < 100; i++) { // Test first 100 locations
            if (memory.readWord(i) != 0) {
                allZero = false;
                break;
            }
        }
        
        assertTest(allZero, "All memory locations should be zero after reset");
        endTest();
    }
    
    /**
     * Test 2: Basic Memory Operations
     */
    private static void testBasicMemoryOperations() {
        startTest("Basic Memory Operations Test");
        
        Memory memory = new Memory();
        
        // Test write and read
        memory.writeWord(100, 0x1234);
        int value = memory.readWord(100);
        assertTest(value == 0x1234, "Write and read should work correctly");
        
        // Test 16-bit masking
        memory.writeWord(200, 0x12345); // Value > 16 bits
        value = memory.readWord(200);
        assertTest(value == 0x2345, "16-bit masking should work");
        
        // Test invalid address
        memory.writeWord(-1, 123);
        value = memory.readWord(-1);
        assertTest(value == 0, "Invalid address should return 0");
        
        endTest();
    }
    
    /**
     * Test 3: Register Operations
     */
    private static void testRegisterOperations() {
        startTest("Register Operations Test");
        
        Registers regs = new Registers();
        
        // Test PC register
        regs.PC.set(100);
        assertTest(regs.PC.get() == 100, "PC register should work");
        
        // Test GPR registers
        regs.GPR[0].set(0x1234);
        assertTest(regs.GPR[0].get() == 0x1234, "GPR[0] should work");
        
        // Test IXR registers
        regs.IXR[0].set(0x5678);
        assertTest(regs.IXR[0].get() == 0x5678, "IXR[0] should work");
        
        endTest();
    }
    
    /**
     * Test 4: Program Loading
     */
    private static void testProgramLoading() {
        startTest("Program Loading Test");
        
        try {
            Memory memory = new Memory();
            memory.load("load.ld");
            
            // Check if program was loaded
            boolean loaded = false;
            for (int i = 0; i < 2048; i++) {
                if (memory.readWord(i) != 0) {
                    loaded = true;
                    break;
                }
            }
            
            assertTest(loaded, "Program should be loaded into memory");
            
            // Check specific loaded values
            int value6 = memory.readWord(6);
            assertTest(value6 == 0x12, "Value at address 6 should be 0x12");
            
        } catch (Exception e) {
            assertTest(false, "Program loading failed: " + e.getMessage());
        }
        
        endTest();
    }
    
    /**
     * Test 5: CPU Execution
     */
    private static void testCPUExecution() {
        startTest("CPU Execution Test");
        
        try {
            Memory memory = new Memory();
            Registers regs = new Registers();
            MachineCore cpu = new MachineCore(memory, regs);
            
            // Load program
            memory.load("load.ld");
            regs.PC.set(memory.getProgramStartAddress());
            
            // Execute one cycle
            int initialPC = regs.PC.get();
            cpu.runOneCycle();
            int newPC = regs.PC.get();
            
            assertTest(newPC == initialPC + 1, "PC should increment after instruction");
            
            // Check if CPU is not halted
            assertTest(!cpu.isHalted(), "CPU should not be halted after one cycle");
            
        } catch (Exception e) {
            assertTest(false, "CPU execution failed: " + e.getMessage());
        }
        
        endTest();
    }
    
    // Helper methods
    private static void startTest(String testName) {
        System.out.println("\n--- " + testName + " ---");
    }
    
    private static void assertTest(boolean condition, String message) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.println("  ✓ " + message);
        } else {
            System.out.println("  ✗ " + message);
        }
    }
    
    private static void endTest() {
        System.out.println("  Test completed.");
    }
    
    private static void printTestSummary() {
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.printf("Tests Passed: %d/%d (%.1f%%)\n", 
                         testsPassed, testsTotal, 
                         (double)testsPassed/testsTotal * 100);
        
        if (testsPassed == testsTotal) {
            System.out.println("🎉 ALL TESTS PASSED! System is working correctly.");
        } else {
            System.out.println("⚠️  Some tests failed. Please review the results above.");
        }
    }
}
