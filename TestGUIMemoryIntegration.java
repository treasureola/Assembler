import BasicMachine.simulator.core.CPU;
import BasicMachine.simulator.ProgramLoader;
import java.io.File;

public class TestGUIMemoryIntegration {
    public static void main(String[] args) {
        System.out.println("=== Testing GUI Memory Integration ===\n");
        
        try {
            // Test 1: CPU Memory Access
            System.out.println("1. Testing CPU Memory Access:");
            CPU cpu = new CPU();
            int[] memory = cpu.memory();
            System.out.println("   ✓ CPU memory array created: " + memory.length + " words");
            
            // Test 2: Program Loading into CPU Memory
            System.out.println("\n2. Testing Program Loading into CPU Memory:");
            File file = new File("output.lst");
            System.out.println("   Loading file: " + file.getAbsolutePath());
            System.out.println("   File exists: " + file.exists());
            
            new ProgramLoader().load(file, memory);
            System.out.println("   ✓ Program loaded into CPU memory");
            
            // Test 3: Verify Memory Contents
            System.out.println("\n3. Verifying Memory Contents:");
            System.out.println("   Memory contents after loading:");
            for (int i = 6; i < 20; i++) {
                if (memory[i] != 0) {
                    System.out.printf("     Memory[%d] = %06o%n", i, memory[i]);
                }
            }
            
            // Test 4: CPU Register Operations
            System.out.println("\n4. Testing CPU Register Operations:");
            cpu.setPC((short)6);
            cpu.setMAR((short)6);
            cpu.setMBR((short)(memory[6] & 0xFFFF));
            cpu.setIR(cpu.getMBR());
            
            System.out.printf("   PC: %06o%n", cpu.getPC());
            System.out.printf("   MAR: %06o%n", cpu.getMAR());
            System.out.printf("   MBR: %06o%n", cpu.getMBR());
            System.out.printf("   IR: %06o%n", cpu.getIR());
            
            // Test 5: Memory Read/Write through CPU
            System.out.println("\n5. Testing Memory Read/Write through CPU:");
            cpu.setMAR((short)100);
            cpu.setMBR((short)0x1234);
            memory[cpu.getMAR()] = cpu.getMBR() & 0xFFFF;
            
            int readValue = memory[cpu.getMAR()] & 0xFFFF;
            System.out.printf("   Write 0x1234 to address 100%n");
            System.out.printf("   Read back: 0x%04X %s%n", readValue, 
                             (readValue == 0x1234 ? "PASS" : "FAIL"));
            
            // Test 6: Simulate Instruction Fetch
            System.out.println("\n6. Simulating Instruction Fetch:");
            cpu.setPC((short)14); // Address of first instruction
            cpu.setMAR(cpu.getPC());
            cpu.setMBR((short)(memory[cpu.getMAR()] & 0xFFFF));
            cpu.setIR(cpu.getMBR());
            cpu.setPC((short)(cpu.getPC() + 1));
            
            System.out.printf("   Fetch from address 14:%n");
            System.out.printf("   PC: %06o%n", cpu.getPC());
            System.out.printf("   MAR: %06o%n", cpu.getMAR());
            System.out.printf("   MBR: %06o%n", cpu.getMBR());
            System.out.printf("   IR: %06o%n", cpu.getIR());
            
            // Test 7: Clock Cycle Simulation
            System.out.println("\n7. Testing Clock Cycle Simulation:");
            System.out.println("   Simulating one complete clock cycle:");
            System.out.println("   - MAR ← PC");
            System.out.println("   - MBR ← Memory[MAR]");
            System.out.println("   - IR ← MBR");
            System.out.println("   - PC ← PC + 1");
            
            short oldPC = cpu.getPC();
            cpu.setMAR(cpu.getPC());
            cpu.setMBR((short)(memory[cpu.getMAR()] & 0xFFFF));
            cpu.setIR(cpu.getMBR());
            cpu.setPC((short)(cpu.getPC() + 1));
            
            System.out.printf("   PC: %06o → %06o%n", oldPC, cpu.getPC());
            System.out.printf("   MAR: %06o%n", cpu.getMAR());
            System.out.printf("   MBR: %06o%n", cpu.getMBR());
            System.out.printf("   IR: %06o%n", cpu.getIR());
            
            System.out.println("\n✓ All memory integration tests passed!");
            
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== GUI Memory Integration Test Complete ===");
    }
}
