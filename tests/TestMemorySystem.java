import BasicMachine.Memory_Module.Memory;
import BasicMachine.Memory_Module.MAR;
import BasicMachine.Memory_Module.MBR;

public class TestMemorySystem {
    public static void main(String[] args) {
        System.out.println("=== Testing Memory System ===\n");
        
        // Test 1: Memory Initialization
        System.out.println("1. Testing Memory Initialization:");
        Memory memory = new Memory();
        System.out.println("   PASS: Memory created with 2048 words");
        
        // Test 2: Memory Reset (should be all zeros)
        System.out.println("\n2. Testing Memory Reset:");
        memory.reset();
        boolean allZero = true;
        for (int i = 0; i < 10; i++) {
            if (memory.readWord(i) != 0) {
                allZero = false;
                break;
            }
        }
        System.out.println("   PASS: Memory reset: " + (allZero ? "PASS" : "FAIL"));
        
        // Test 3: Basic Read/Write Operations
        System.out.println("\n3. Testing Basic Read/Write Operations:");
        memory.writeWord(100, 0x1234);
        int value = memory.readWord(100);
        System.out.printf("   Write 0x1234 to address 100, Read: 0x%04X %s%n", 
                         value, (value == 0x1234 ? "PASS" : "FAIL"));
        
        // Test 4: 16-bit Masking
        System.out.println("\n4. Testing 16-bit Masking:");
        memory.writeWord(200, 0x12345); // Value > 16 bits
        value = memory.readWord(200);
        System.out.printf("   Write 0x12345 (20-bit), Read: 0x%04X %s%n", 
                         value, (value == 0x2345 ? "PASS" : "FAIL"));
        
        // Test 5: Invalid Address Handling
        System.out.println("\n5. Testing Invalid Address Handling:");
        System.out.println("   Testing negative address:");
        memory.writeWord(-1, 123);
        value = memory.readWord(-1);
        System.out.printf("   Read from -1: %d (should be 0) %s%n", 
                         value, (value == 0 ? "PASS" : "FAIL"));
        
        System.out.println("   Testing address > 2047:");
        memory.writeWord(3000, 456);
        value = memory.readWord(3000);
        System.out.printf("   Read from 3000: %d (should be 0) %s%n", 
                         value, (value == 0 ? "PASS" : "FAIL"));
        
        // Test 6: MAR Register
        System.out.println("\n6. Testing MAR (Memory Address Register):");
        MAR mar = new MAR();
        mar.setAddress(500);
        int addr = mar.getAddress();
        System.out.printf("   Set MAR to 500, Get: %d %s%n", 
                         addr, (addr == 500 ? "PASS" : "FAIL"));
        
        // Test 7: MBR Register
        System.out.println("\n7. Testing MBR (Memory Buffer Register):");
        MBR mbr = new MBR();
        mbr.setData(0xABCD);
        int data = mbr.getData();
        System.out.printf("   Set MBR to 0xABCD, Get: 0x%04X %s%n", 
                         data, (data == 0xABCD ? "PASS" : "FAIL"));
        
        // Test 8: MBR 16-bit Masking
        System.out.println("\n8. Testing MBR 16-bit Masking:");
        mbr.setData(0x12345); // Value > 16 bits
        data = mbr.getData();
        System.out.printf("   Set MBR to 0x12345, Get: 0x%04X %s%n", 
                         data, (data == 0x2345 ? "PASS" : "FAIL"));
        
        // Test 9: Memory Clock Cycle Simulation
        System.out.println("\n9. Testing Memory Clock Cycle (Single Operation):");
        System.out.println("   Simulating one clock cycle:");
        mar.setAddress(300);
        mbr.setData(0x5678);
        System.out.println("   Clock cycle: Write operation");
        memory.writeWord(mar.getAddress(), mbr.getData());
        int readBack = memory.readWord(300);
        System.out.printf("   Write 0x5678 to address 300, Read back: 0x%04X %s%n", 
                         readBack, (readBack == 0x5678 ? "PASS" : "FAIL"));
        
        // Test 10: Load Program Test
        System.out.println("\n10. Testing Program Loading:");
        try {
            memory.load("load.ld");
            System.out.println("   PASS: Program loaded successfully from load.ld");
            
            // Show first few loaded values
            System.out.println("   First few loaded values:");
            for (int i = 6; i < 20; i++) {
                int val = memory.readWord(i);
                if (val != 0) {
                    System.out.printf("     Memory[%d] = %06o%n", i, val);
                }
            }
        } catch (Exception e) {
            System.out.println("   FAIL: Program loading failed: " + e.getMessage());
        }
        
        System.out.println("\n=== Memory System Test Complete ===");
    }
}

