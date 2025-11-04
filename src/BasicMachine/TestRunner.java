package BasicMachine;

import BasicMachine.Assembler.Assembler;
import BasicMachine.CPU_Module.MainSimulator;
import BasicMachine.simulator.ui.SimulatorApp;
import javafx.application.Application;

/**
 * ==============================================================
 *  TestRunner.java
 *  --------------------------------------------------------------
 *  Unified launcher for Basic Machine Project
 *  (Assembler + Text Simulator + GUI)
 *
 *  This automatically:
 *   1. Assembles test programs
 *   2. Runs MainSimulator in console mode
 *   3. Launches SimulatorApp (JavaFX UI)
 * ==============================================================
 */
public class TestRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("==============================================================");
        System.out.println("   Basic Machine: Assembler + Simulator (Unified Launcher)");
        System.out.println("==============================================================");

        // ==========================================================
        // PHASE 1 — Assemble .src programs
        // ==========================================================
        System.out.println("[PHASE 1] Running Assembler...");
        String[] programs = {"Part0_Test.src", "Part2_test.src", "Program_1.src"};
        for (String src : programs) {
            java.io.File f = new java.io.File(src);
            if (f.exists()) {
                System.out.println("→ Assembling " + src + " ...");
                Assembler.main(new String[]{src});
            } else {
                System.out.println("⚠️  Skipped missing file: " + src);
            }
        }

        // Check if output files exist
        java.io.File lst = new java.io.File("BasicMachine/output.lst");
        java.io.File ld  = new java.io.File("BasicMachine/load.ld");
        if (!lst.exists() || !ld.exists()) {
            System.err.println("[ERROR] Assembly output not found. Exiting.");
            return;
        }

        System.out.println("[OK] Assembly complete!");
        System.out.println("   • BasicMachine/output.lst");
        System.out.println("   • BasicMachine/load.ld");

        // ==========================================================
        // PHASE 2 — Run Text Simulator (MainSimulator)
        // ==========================================================
        System.out.println("\n[PHASE 2] Running Text-Based Simulator...");
        MainSimulator.main(args);

        // ==========================================================
        // PHASE 3 — Launch JavaFX Simulator UI
        // ==========================================================
        System.out.println("\n[PHASE 3] Launching GUI (SimulatorApp)...");
        Application.launch(SimulatorApp.class, args);

        System.out.println("==============================================================");
        System.out.println("✅ Finished — Both text and GUI simulations completed.");
        System.out.println("==============================================================");
    }
}
