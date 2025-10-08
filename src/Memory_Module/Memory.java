public class Memory {
    // 2048 words of 16 bits (used int for simplicity)
    private int[] data = new int[2048];
    
    public Memory() {
        reset();  // this clears up  memory on creation
    }

    /**
     * Clear all memory contents to zero (called on power-up/reset).
     */
    public void reset() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    /**
     * Read a word from the given address. The result will be placed into the MBR.
     * @param address memory address (0–2047)
     * @return the 16-bit value at that address (0 if invalid)
     */
    public int readWord(int address) {
        if (address < 0 || address >= data.length) {
            // Handle invalid address gracefully
            System.err.println("Memory read error: invalid address " + address);
            return 0;
        }
        return data[address];
    }

    /**
     * Write a word to the given address. Uses the value from the MBR.
     * @param address memory address (0–2047)
     * @param value 16-bit value to write
     */
    public void writeWord(int address, int value) {
        if (address < 0 || address >= data.length) {
            // Handle invalid address gracefully
            System.err.println("Memory write error: invalid address " + address);
            return;
        }
        data[address] = value & 0xFFFF;  // mask to 16 bits
    }
}

