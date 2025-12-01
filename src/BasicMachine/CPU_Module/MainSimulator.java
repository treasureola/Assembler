package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;
import BasicMachine.simulator.ProgramLoader;

import java.io.File;

/* ==============================================================
 * MainSimulator.java
 * --------------------------------------------------------------
 * CLI entry point for running load.ld without the GUI.
 * Now also exposes runOnceForCli() which returns a status code
 * so the TestRunner can print PASS/FAIL summaries.
 * ==============================================================
 */
public class MainSimulator {

    /**
     * Run the simulator once from the command line.
     *
     * @return 0 if the program executed and halted normally (HLT),
     *         1 if there was an exception during simulation,
     *         2 if load.ld could not be found.
     */
    public static int runOnceForCli() {
        try {
            // ------------------------------------------------------
            // STEP 1: Locate load.ld (written by the Assembler)
            // ------------------------------------------------------
            File cwd = new File(System.getProperty("user.dir"));
            // System.out.println("[DEBUG] CWD (MainSimulator): " + cwd.getAbsolutePath());

            File projectRoot = cwd;
            // If running from out/, step up to project root.
            if ("out".equalsIgnoreCase(cwd.getName())) {
                projectRoot = cwd.getParentFile();
            }
            if (projectRoot == null) {
                projectRoot = cwd;
            }

            // Preferred: <projectRoot>/load.ld
            File loadFile = new File(projectRoot, "load.ld");

            // Optional fallback: <projectRoot>/BasicMachine/load.ld
            if (!loadFile.exists()) {
                loadFile = new File(projectRoot, "BasicMachine/load.ld");
            }

            // Last-resort fallback: ./load.ld relative to CWD
            if (!loadFile.exists()) {
                loadFile = new File("load.ld");
            }

            if (!loadFile.exists()) {
                System.err.println("[ERROR] load.ld not found.");
                System.err.println("Checked:");
                System.err.println("  " + new File(projectRoot, "load.ld").getAbsolutePath());
                System.err.println("  " + new File(projectRoot, "BasicMachine/load.ld").getAbsolutePath());
                System.err.println("  " + new File("load.ld").getAbsolutePath());
                System.err.println("Please assemble your source program first using Assembler.java.");
                return 2; // load.ld missing
            }

            // ------------------------------------------------------
            // STEP 2: Initialize memory and registers
            // ------------------------------------------------------
            Memory mem = new Memory();
            Registers regs = new Registers();

            // ------------------------------------------------------
            // STEP 3: Load program into memory
            // ------------------------------------------------------
            ProgramLoader loader = new ProgramLoader();
            loader.load(loadFile, mem);

            // ------------------------------------------------------
            // STEP 4: Set initial PC (program start)
            // ------------------------------------------------------
            int startAddr = loader.getProgramStartAddress();
            regs.setPC(startAddr);
            System.out.printf("[INFO] Program start address set to %06o%n", startAddr);

            // ------------------------------------------------------
            // STEP 5: Create CPU core and run until HLT
            // ------------------------------------------------------
            MachineCore core = new MachineCore(mem, regs);

            System.out.println("[RUN] Beginning program execution...\n");
            core.run();   // <-- run ONCE, not twice

            // ------------------------------------------------------
            // STEP 6: Normal halt
            // ------------------------------------------------------
            System.out.println("\n[HALT] Simulation finished successfully.");
            System.out.println("All instructions executed. CPU stopped normally.");

            return 0;  // success

        } catch (Exception e) {
            System.err.println("[EXCEPTION] Simulation terminated due to an error:");
            e.printStackTrace();
            return 1;  // runtime error
        }
    }

    /**
     * Classic main() – just delegate to runOnceForCli().
     */
    public static void main(String[] args) {
        runOnceForCli();
    }
}
