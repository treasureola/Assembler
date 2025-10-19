package BasicMachine.simulator.core;

import BasicMachine.CPU_Module.MachineCore;
import BasicMachine.CPU_Module.Registers;
import BasicMachine.Memory_Module.Memory;

public class CPU {
    private final MachineCore core;

    public CPU() {
        Memory memory = new Memory();
        Registers regs = new Registers();
        this.core = new MachineCore(memory, regs);
    }

    // ================= BASIC CONTROL =================
    public void step() { core.step(); }
    public void run() { core.run(); }
    public void reset() { core.reset(); }
    public boolean isHalted() { return core.isHalted(); }

    // ================= ACCESSORS =================
    public Memory getMemory() { return core.getMemory(); }
    public Registers getRegisters() { return core.getRegisters(); }

    // ================= REGISTER SHORTCUTS =================
    public short getR0() { return (short) core.getRegisters().getGPR(0); }
    public short getR1() { return (short) core.getRegisters().getGPR(1); }
    public short getR2() { return (short) core.getRegisters().getGPR(2); }
    public short getR3() { return (short) core.getRegisters().getGPR(3); }

    public short getX1() { return (short) core.getRegisters().getIX(1); }
    public short getX2() { return (short) core.getRegisters().getIX(2); }
    public short getX3() { return (short) core.getRegisters().getIX(3); }

    public short getPC() { return (short) core.getRegisters().getPC(); }
    public short getMAR() { return (short) core.getRegisters().getMAR(); }
    public short getMBR() { return (short) core.getRegisters().getMBR(); }
    public short getIR() { return (short) core.getRegisters().getIR(); }
    public short getCC() { return (short) core.getRegisters().getCC(); }
    public short getMFR() { return (short) core.getRegisters().getMFR(); }
}
