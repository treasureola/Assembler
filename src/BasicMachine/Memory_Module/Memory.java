package BasicMachine.Memory_Module;

public class Memory {
    // 2048 words of 16 bits 
    private int[] data = new int[2048];
    
    public Memory() {
        reset();  // this clears up  memory on creation
    }

    /**
     * Clear all memory contents to zero on reset.
     */
    public void reset() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    /**
     * Read a word from the given address, then places the result into the MBR.
     * address memory address (0–2047)
     * this then returns the 16-bit value at that address, 0 if invalid
     */
    public int readWord(int address) {
        if (address < 0 || address >= data.length) {
            // Handles invalid addresses
            System.err.println("Memory read error: invalid address " + address);
            return 0;
        }
        return data[address];
    }

    /**
     * Write a word to the given address. Uses the value from the MBR.
     * address memory address (0–2047)
     * value 16-bit value to write
     */
    public void writeWord(int address, int value) {
        if (address < 0 || address >= data.length) {
            // Handle invalid addresses 
            System.err.println("Memory write error: invalid address " + address);
            return;
        }
        data[address] = value & 0xFFFF;  // mask to 16 bits
    }
    
    /**
     * Load program from load.ld file
     */
    public void load(String filename) throws Exception {
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                int addr = Integer.parseInt(parts[0], 8); // octal
                int value = Integer.parseInt(parts[1], 8); // octal
                writeWord(addr, value);
            }
        }
        br.close();
    }
    
    /**
     * Get program start address (first non-zero instruction)
     */
    public int getProgramStartAddress() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0) return i;
        }
        return 0;
    }
}
