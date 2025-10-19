package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;
import BasicMachine.CPU_Module.MachineCore;
import BasicMachine.CPU_Module.Registers;
import BasicMachine.simulator.ProgramLoader;

import java.io.File;

/**
 * MainSimulator - simple console-level test harness for the Basic Machine.
 * Loads a .ld file into memory and runs the CPU core directly.
 */
public class MainSimulator {

    public static void main(String[] args) {
        try {
            System.out.println("=== CSCI 6461 Machine Simulator ===");

            // Step 1: Create memory and register modules
            Memory mem = new Memory();
            Registers regs = new Registers();

            // Step 2: Create machine core (CPU engine)
            MachineCore cpu = new MachineCore(mem, regs);

            // Step 3: Load program into memory
            System.out.println("Loading program file: load.ld");

            ProgramLoader loader = new ProgramLoader();  // ✅ no arguments

            File loadFile = new File("load.ld");         // ✅ use File object
            loader.load(loadFile, mem);                  // ✅ pass both file and memory
            // ✅ only pass file path now

            // Step 4: Dynamically set PC to program start address
            int startAddr = mem.getProgramStartAddress();
            regs.setPC(startAddr);

            System.out.printf("Program loaded successfully.\nStart address = %06o\n", startAddr);
            System.out.println("----------------------------------");

            // Step 5: Run the CPU simulation
            System.out.println("Starting simulation...\n");
            cpu.run();
            System.out.println("\nExecution finished.");

            // Step 6: Print register values in octal after execution
            dumpRegisters(regs);

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Utility: print all register values in octal for debugging */
    private static void dumpRegisters(Registers regs) {
        System.out.println("\n=== Final Register States ===");
        System.out.printf("R0: %06o\n", regs.getGPR(0));
        System.out.printf("R1: %06o\n", regs.getGPR(1));
        System.out.printf("R2: %06o\n", regs.getGPR(2));
        System.out.printf("R3: %06o\n", regs.getGPR(3));
        System.out.printf("X1: %06o\n", regs.getIX(1));
        System.out.printf("X2: %06o\n", regs.getIX(2));
        System.out.printf("X3: %06o\n", regs.getIX(3));
        System.out.printf("PC: %06o\n", regs.getPC());
        System.out.printf("MAR: %06o\n", regs.getMAR());
        System.out.printf("MBR: %06o\n", regs.getMBR());
        System.out.printf("IR: %06o\n", regs.getIR());
        System.out.printf("CC: %06o\n", regs.getCC());
        System.out.printf("MFR: %06o\n", regs.getMFR());
        System.out.println("==============================\n");
    }

}
