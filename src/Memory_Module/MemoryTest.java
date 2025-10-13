package MemoryModule;   

public class MemoryTest {
    public static void main(String[] args) {
        Memory memory = new Memory();
        MAR mar = new MAR();
        MBR mbr = new MBR();

        // Reset memory
        memory.reset();
        System.out.println("Memory[0] after reset: " + memory.readWord(0));

        // Write a value
        mar.setAddress(100);
        mbr.setData(0x1234);
        memory.writeWord(mar.getAddress(), mbr.getData());
        System.out.println("Wrote 0x1234 to address 100.");

        // Read it back
        int result = memory.readWord(mar.getAddress());
        System.out.printf("Read back: 0x%04X%n", result);

        // Try invalid address
        memory.readWord(3000); // should print an error
    }
}
