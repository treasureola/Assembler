package BasicMachine;

import BasicMachine.Assembler.Assembler;
import BasicMachine.CPU_Module.MainSimulator;

/**
 * TestRunner
 * --------------------------------------------
 * Runs three stages:
 *   1) InstructionSet_Test.src  – assembled + executed in terminal
 *   2) Program_1.src            – assembled + executed in terminal
 *   3) Program2_test.src        – assembled ONLY (for GUI IPL)
 */
public class TestRunner {

    // Base folder where your .src files live
    private static final String SRC_ROOT = "src/BasicMachine/";

    public static void main(String[] args) throws Exception {

        System.out.println("=====================================================");
        System.out.println(" BASIC MACHINE — AUTOMATED TEST SUITE");
        System.out.println("=====================================================\n");

        // -------------------------------------------------
        // TEST 1 – Instruction Set sanity check
        // -------------------------------------------------
        runTest(
                "InstructionSet_Test.src",
                true,
                "Instruction Set Sanity Check",
                "Assembles and runs a program that exercises each instruction.\n" +
                        "Watch the terminal output to verify arithmetic, logic, branches, etc."
        );

        // -------------------------------------------------
        // TEST 2 – Program 1 (Closest of 20 Numbers)
        // -------------------------------------------------
        runTest(
                "Program_1.src",
                true,
                "Program 1 – Closest of 20 Numbers",
                "Assembles and runs the full Program 1.\n" +
                        "The simulator will prompt for 20 numbers and report the closest."
        );

        // -------------------------------------------------
        // TEST 3 – Program 2 (Word Search) – assemble only
        // -------------------------------------------------
        System.out.println("-----------------------------------------------------");
        System.out.println(" PROGRAM 2 – Word Search (GUI ONLY)");
        System.out.println("-----------------------------------------------------");
        assembleOnly(
                "Program2_test.src",
                "Program 2 is designed to run with the GUI (Console Input + Printer).\n" +
                        "Here we ONLY assemble it so that load.ld is ready.\n" +
                        "Then, in the GUI, use IPL to load Program2_test and click RUN there."
        );

        System.out.println("=====================================================");
        System.out.println(" TEST SUITE COMPLETE");
        System.out.println("  - InstructionSet_Test.src : assembled + executed");
        System.out.println("  - Program_1.src           : assembled + executed");
        System.out.println("  - Program2_test.src       : assembled only (run in GUI)");
        System.out.println("=====================================================\n");
    }

    /**
     * Assemble a program and (optionally) run it in the terminal.
     */
    private static void runTest(String fileName,
                                boolean runProgram,
                                String title,
                                String description) throws Exception {

        System.out.println("-----------------------------------------------------");
        System.out.println(" TEST – " + title);
        System.out.println("  Source file : " + fileName);
        System.out.println("-----------------------------------------------------");
        System.out.println("  " + description + "\n");

        String srcPath = SRC_ROOT + fileName;

        // Assemble (quiet mode to keep output clean)
        Assembler.main(new String[]{srcPath, "--quiet"});
        System.out.println("  [OK] Assembled " + fileName + "\n");

        if (!runProgram) {
            return;
        }

        // Run in CLI simulator
        System.out.println("  [RUN] Starting simulator for " + fileName + " ...\n");
        MainSimulator.main(null);
        System.out.println("\n  [DONE] Simulation for " + fileName + " finished.\n");
    }

    /**
     * Assemble only, do not run.
     */
    private static void assembleOnly(String fileName,
                                     String explanation) throws Exception {

        System.out.println("  " + explanation + "\n");

        String srcPath = SRC_ROOT + fileName;

        // Assemble (quiet mode)
        Assembler.main(new String[]{srcPath, "--quiet"});

        System.out.println("  [OK] Assembled " + fileName + " (no terminal execution).\n");
    }
}
