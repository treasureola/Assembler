package BasicMachine.CPU_Module;

public class Registers {
    private int[] GPR = new int[4];   // R0–R3
    private int[] IXR = new int[4];   // X0 unused; X1–X3 used
    private int PC, MAR, MBR, IR, MFR, CC;

    public void reset() {
        for (int i = 0; i < 4; i++) {
            GPR[i] = 0;
            IXR[i] = 0;
        }
        PC = MAR = MBR = IR = MFR = CC = 0;
    }

    // ================= GPRs (R0–R3) =================
    public int getGPR(int r) { return GPR[r]; }
    public void setGPR(int r, int val) { GPR[r] = val & 0xFFFF; }

    // ================= IXRs (X1–X3) =================
    public int getIX(int x) { return IXR[x]; }
    public void setIX(int x, int val) { IXR[x] = val & 0xFFFF; }

    // ================= Control Registers =================
    public int getPC() { return PC; }
    public void setPC(int val) { PC = val & 0x7FF; }

    public int getMAR() { return MAR; }
    public void setMAR(int val) { MAR = val & 0x7FF; }

    public int getMBR() { return MBR; }
    public void setMBR(int val) { MBR = val & 0xFFFF; }

    public int getIR() { return IR; }
    public void setIR(int val) { IR = val & 0xFFFF; }

    public int getMFR() { return MFR; }
    public void setMFR(int val) { MFR = val & 0xFFFF; }

    public int getCC() { return CC; }
    public void setCC(int val) { CC = val & 0xFFFF; }
}
