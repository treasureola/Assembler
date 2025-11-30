package BasicMachine;

import BasicMachine.Assembler.Assembler;
import BasicMachine.CPU_Module.MainSimulator;
import BasicMachine.simulator.ui.SimulatorApp;

import java.io.File;

public class TestRunner {

    public static void main(String[] args) {
        try {
            // Figure out where we are:
            // When run via run_all.sh, user.dir is typically .../Assembler/out
            String userDir = System.getProperty("user.dir");
            System.out.println("[DEBUG] Current working dir: " + userDir);

            File cwd = new File(userDir);
            File projectRoot;

            // If we're in .../Assembler/out, go up one level to .../Assembler
            if (cwd.getName().equalsIgnoreCase("out")) {
                projectRoot = cwd.getParentFile();
            } else {
                projectRoot = cwd;
            }

            // Build the full path to Program2_test.src under src/BasicMachine
            File srcFile = new File(projectRoot, "src/BasicMachine/Program2_test.src");
            String srcPath = srcFile.getPath();

            System.out.println("=== Phase 1: Running Assembler on Program2_test.src ===");
            System.out.println("[DEBUG] Assembler source path: " + srcPath);

            // PHASE 1: Run the assembler on Program2_test.src
            Assembler.main(new String[]{ srcPath });

            System.out.println("=== Phase 1 done. load.ld and output.lst should be generated. ===");

            // PHASE 2: Text-mode simulator
            System.out.println("=== Phase 2: Running Text-Mode Simulator ===");
            MainSimulator.main(new String[0]);

            // PHASE 3: JavaFX GUI
            System.out.println("=== Phase 3: Launching GUI Simulator ===");
            SimulatorApp.main(new String[0]);

            System.out.println("=== All phases complete. ===");

        } catch (Exception e) {
            System.err.println("ERROR during TestRunner execution:");
            e.printStackTrace();
        }
    }
}
