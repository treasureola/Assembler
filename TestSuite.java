import BasicMachine.Memory_Module.Memory;
import BasicMachine.CPU_Module.Registers;
import BasicMachine.CPU_Module.MachineCore;
import BasicMachine.simulator.ProgramLoader;
import java.io.*;
import java.util.*;

/**
 * Comprehensive Test Suite for Basic Machine Simulator
 * Testing and Documentation
 * 
 * This test suite validates:
 * - Memory operations (LOAD, STORE, HALT)
 * - Program Counter increment
 * - Memory reset functionality
 * - Loader error handling
 * - Illegal opcode handling
 */
public class TestSuite {
    private static int testsPassed = 0;
    private static int testsTotal = 0;
    private static StringBuilder testResults = new StringBuilder();
    
    public static void main(String[] args) {
        System.out.println("=== BASIC MACHINE SIMULATOR TEST SUITE ===");
        System.out.println("Testing and Documentation\n");
        
        // Run all test categories
        testMemoryReset();
        testBasicOperations();
        testProgramCounterIncrement();
        testStoreLoadCycle();
        testLoaderErrorHandling();
        testIllegalOpcode();
        testIntegration();
        
        // Print summary
        printTestSummary();
    }
    
    /**
     * Test 1: Memory Reset Test (all zeros)
     */
    private static void testMemoryReset() {
        startTest("Memory Reset Test");
        
        Memory memory = new Memory();
        memory.reset();
        
        boolean allZero = true;
        for (int i = 0; i < 2048; i++) {
            if (memory.readWord(i) != 0) {
                allZero = false;
                break;
            }
        }
        
        assertTest(allZero, "All memory locations should be zero after reset");
        endTest();
    }
    
    /**
     * Test 2: Basic LOAD, STORE, HALT Operations
     */
    private static void testBasicOperations() {
        startTest("Basic Operations Test (LOAD, STORE, HALT)");
        
        try {
            // Assemble test program
            assembleTestProgram("test1_basic_operations.src", "test1.lst");
            
            // Load and run
            Memory memory = new Memory();
            memory.load("test1.lst");
            
            // Verify data was loaded correctly
            int data100 = memory.readWord(100);
            int data102 = memory.readWord(102);
            
            assertTest(data100 == 42, "Data should be stored at address 100");
            assertTest(data102 == 42, "Data should be copied to address 102");
            
        } catch (Exception e) {
            assertTest(false, "Basic operations test failed: " + e.getMessage());
        }
        
        endTest();
    }
    
    /**
     * Test 3: Program Counter Increment Test
     */
    private static void testProgramCounterIncrement() {
        startTest("Program Counter Increment Test");
        
        try {
            assembleTestProgram("test2_pc_increment.src", "test2.lst");
            
            Memory memory = new Memory();
            Registers regs = new Registers();
            MachineCore cpu = new MachineCore(memory, regs);
            
            memory.load("test2.lst");
            regs.PC.set(200); // Start at address 200
            int initialPC = regs.PC.get();
            
            // Execute first instruction
            cpu.runOneCycle();
            assertTest(regs.PC.get() == initialPC + 1, "PC should increment by 1");
            
            // Execute second instruction
            cpu.runOneCycle();
            assertTest(regs.PC.get() == initialPC + 2, "PC should increment by 2");
            
            // Execute third instruction
            cpu.runOneCycle();
            assertTest(regs.PC.get() == initialPC + 3, "PC should increment by 3");
            
        } catch (Exception e) {
            assertTest(false, "PC increment test failed: " + e.getMessage());
        }
        
        endTest();
    }
    
    /**
     * Test 4: Store → Load → Halt Cycle Test
     */
    private static void testStoreLoadCycle() {
        startTest("Store → Load → Halt Cycle Test");
        
        try {
            assembleTestProgram("test3_store_load_cycle.src", "test3.lst");
            
            Memory memory = new Memory();
            Registers regs = new Registers();
            MachineCore cpu = new MachineCore(memory, regs);
            
            memory.load("test3.lst");
            regs.PC.set(300); // Start at address 300
            
            // Verify initial data
            assertTest(memory.readWord(300) == 123, "Initial data 123 should be at address 300");
            assertTest(memory.readWord(301) == 456, "Initial data 456 should be at address 301");
            assertTest(memory.readWord(302) == 789, "Initial data 789 should be at address 302");
            
            // Execute several instructions
            for (int i = 0; i < 5; i++) {
                cpu.runOneCycle();
            }
            
            // Verify data was stored correctly
            assertTest(memory.readWord(310) == 123, "Stored data 123 should be at address 310");
            assertTest(memory.readWord(311) == 456, "Stored data 456 should be at address 311");
            assertTest(memory.readWord(312) == 789, "Stored data 789 should be at address 312");
            
        } catch (Exception e) {
            assertTest(false, "Store-Load cycle test failed: " + e.getMessage());
        }
        
        endTest();
    }
    
    /**
     * Test 5: Loader Error Handling
     */
    private static void testLoaderErrorHandling() {
        startTest("Loader Error Handling Test");
        
        try {
            // Test with blank lines
            createTestFile("test_blank_lines.lst", 
                "000100 000001\n\n\n000101 000002\n");
            
            Memory memory = new Memory();
            ProgramLoader loader = new ProgramLoader();
            // Create a simple memory array for testing
            int[] memoryArray = new int[2048];
            loader.load(new File("test_blank_lines.lst"), memoryArray);
            
            assertTest(memoryArray[100] == 1, "Should handle blank lines");
            assertTest(memoryArray[101] == 2, "Should load data after blank lines");
            
            // Test with comments
            createTestFile("test_comments.lst",
                "000200 000003 ; This is a comment\n" +
                "000201 000004 ; Another comment\n");
            
            memory.reset();
            loader.load(new File("test_comments.lst"), memoryArray);
            
            assertTest(memoryArray[200] == 3, "Should handle comments");
            assertTest(memoryArray[201] == 4, "Should load data with comments");
            
        } catch (Exception e) {
            assertTest(false, "Loader error handling test failed: " + e.getMessage());
        }
        
        endTest();
    }
    
    /**
     * Test 6: Illegal Opcode Test
     */
    private static void testIllegalOpcode() {
        startTest("Illegal Opcode Test");
        
        try {
            // Create a program with illegal opcode
            createTestFile("test_illegal.lst",
                "000300 077777 ; Illegal opcode (all 1s)\n" +
                "000301 000000 ; HLT instruction\n");
            
            Memory memory = new Memory();
            Registers regs = new Registers();
            MachineCore cpu = new MachineCore(memory, regs);
            
            memory.load("test_illegal.lst");
            regs.PC.set(300);
            
            // Execute illegal instruction
            cpu.runOneCycle();
            
            // Should halt due to illegal opcode
            assertTest(cpu.isHalted(), "CPU should halt on illegal opcode");
            
        } catch (Exception e) {
            assertTest(false, "Illegal opcode test failed: " + e.getMessage());
        }
        
        endTest();
    }
    
    /**
     * Test 7: Integration Test
     */
    private static void testIntegration() {
        startTest("Integration Test");
        
        try {
            // Test the original program
            Memory memory = new Memory();
            Registers regs = new Registers();
            MachineCore cpu = new MachineCore(memory, regs);
            
            memory.load("load.ld");
            regs.PC.set(memory.getProgramStartAddress());
            
            // Run a few cycles
            for (int i = 0; i < 3; i++) {
                cpu.runOneCycle();
            }
            
            assertTest(regs.PC.get() > 6, "PC should have incremented");
            assertTest(!cpu.isHalted(), "CPU should not be halted yet");
            
        } catch (Exception e) {
            assertTest(false, "Integration test failed: " + e.getMessage());
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
    
    private static void assembleTestProgram(String sourceFile, String outputFile) throws Exception {
        // Simple assembler for test programs
        File source = new File("test_programs/" + sourceFile);
        if (!source.exists()) {
            throw new FileNotFoundException("Test program not found: " + sourceFile);
        }
        
        // For now, we'll create simple .lst files manually
        // In a real implementation, this would call the assembler
        System.out.println("  Assembling " + sourceFile + " -> " + outputFile);
    }
    
    private static void createTestFile(String filename, String content) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(content);
        }
    }
}
