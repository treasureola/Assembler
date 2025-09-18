public class VectorUnit {
    private final int[] memory;

    public VectorUnit(int[] memory) {
        this.memory = memory;
    }

    public void vadd(int frLength, int ea) {
        int len = frLength & 0xFFFF;
        if (len <= 0) return;
        int addr1 = memory[ea] & 0xFFFF;
        int addr2 = memory[ea + 1] & 0xFFFF;
        for (int i = 0; i < len; i++) {
            int v1 = (short) memory[addr1 + i];
            int v2 = (short) memory[addr2 + i];
            int res = v1 + v2;
            memory[addr1 + i] = res & 0xFFFF;
        }
    }

    public void vsub(int frLength, int ea) {
        int len = frLength & 0xFFFF;
        if (len <= 0) return;
        int addr1 = memory[ea] & 0xFFFF;
        int addr2 = memory[ea + 1] & 0xFFFF;
        for (int i = 0; i < len; i++) {
            int v1 = (short) memory[addr1 + i];
            int v2 = (short) memory[addr2 + i];
            int res = v1 - v2;
            memory[addr1 + i] = res & 0xFFFF;
        }
    }
}
