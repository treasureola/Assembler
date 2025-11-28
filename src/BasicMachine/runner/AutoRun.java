package BasicMachine.runner;
import BasicMachine.Memory_Module.Memory;
import BasicMachine.CPU_Module.MachineCore;
import BasicMachine.CPU_Module.Registers;
import BasicMachine.simulator.ProgramLoader;
import java.io.File;

public class AutoRun {
    public static void main(String[] args) throws Exception {
        Memory memory = new Memory();
        Registers regs = new Registers();
        ProgramLoader loader = new ProgramLoader();
        File ldFile = new File("BasicMachine/load.ld");
        if (!ldFile.exists()) {
            System.err.println("[ERROR] load.ld not found — assemble first!");
            return;
        }
        
        loader.load(ldFile, memory);

        int start = loader.getProgramStartAddress();
        regs.setPC(start);
        
        MachineCore cpu = new MachineCore(memory, regs);
        cpu.run();
    }
}
