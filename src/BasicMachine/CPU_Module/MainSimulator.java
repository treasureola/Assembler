package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;
// import Assembler.Assembler;

/**
Current problems
- SHould the program start on the first line of instruction in the load file
 - Or the first executable instruction
 - if first, how can we handle halting?
 */
public class MainSimulator {
    public static void main(String[] args) {
        try {
            String sourceFile = "src/Assembler/source.src";
            System.out.println("Running assembler for " + sourceFile + " ...");
            // Assembler.assemble(sourceFile); // Skip for now, use existing load.ld
            System.out.println("\nAssembler finished. Loading into BasicMachine.simulator...\n");

            Memory mem = new Memory();
            Registers regs = new Registers();
            MachineCore cpu = new MachineCore(mem, regs);

            // Load program and automatically detect start address
            mem.load("load.ld");
            regs.PC.set(mem.getProgramStartAddress());  // 👈 dynamic start

            System.out.printf("Program starting at address %04o%n%n",
                    mem.getProgramStartAddress());

            System.out.println("Starting simulation...");
            cpu.run();
            System.out.println("Execution finished.");
        } catch (Exception e) {
            System.err.println("Simulation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
