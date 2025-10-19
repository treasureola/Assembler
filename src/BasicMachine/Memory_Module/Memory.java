package BasicMachine.Memory_Module;

public class Memory {
    private final short[] memory = new short[2048];
    private int programStartAddress = 0;

    public void reset() {
        for (int i = 0; i < memory.length; i++) memory[i] = 0;
        programStartAddress = 0;
    }

    public void writeWord(int address, int value) {
        if (address < 0 || address >= memory.length)
            throw new IllegalArgumentException("Address out of range: " + address);
        memory[address] = (short) (value & 0xFFFF);
        if (programStartAddress == 0) programStartAddress = address;
    }

    public short readWord(int address) {
        if (address < 0 || address >= memory.length)
            throw new IllegalArgumentException("Address out of range: " + address);
        return memory[address];
    }

    public int size() { return memory.length; }

    public void setProgramStartAddress(int address) { this.programStartAddress = address; }

    public int getProgramStartAddress() { return programStartAddress; }

    public short[] dumpMemory() { return memory.clone(); }
}
