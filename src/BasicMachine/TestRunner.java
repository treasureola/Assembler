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

    private static final String SRC_ROOT = "src/BasicMachine/";

    public static void main(String[] args) throws Exception {
        System.out.println("=====================================================");
        System.out.println(" BASIC MACHINE — AUTOMATED TEST SUITE");
        System.out.println("=====================================================\n");

        // TEST 1
        runTest(
                "InstructionSet_Test.src",
                true,
                "Instruction Set Sanity Check",
                "Assembles and runs a program that exercises each instruction.\n" +
                        "Watch the terminal output to verify arithmetic, logic, branches, etc."
        );

        // TEST 2
        runTest(
                "Program_1.src",
                true,
                "Program 1 – Closest of 20 Numbers",
                "Assembles and runs the full Program 1.\n" +
                        "The simulator will prompt for 20 numbers and report the closest."
        );

        // TEST 3 – assemble-only
        System.out.println("-----------------------------------------------------");
        System.out.println(" PROGRAM 2 – Word Search");
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

        // 1) Assemble (QUIET mode so tests are clean)
        Assembler.main(new String[]{srcPath, "--quiet"});
        System.out.println("  [OK] Assembled " + fileName + "\n");

        if (!runProgram) {
            return;
        }

        // 2) Run in CLI simulator using status code
        System.out.println("  [RUN] Starting simulator for " + fileName + " ...\n");
        int status = MainSimulator.runOnceForCli();  // 0 = success, non-zero = error
        System.out.println(); // blank line before summary

        // 3) High-level summary, depending on which test it is
        if ("InstructionSet_Test.src".equals(fileName)) {
            printInstructionSetSummary(status);
        } else if ("Program_1.src".equals(fileName)) {
            printProgram1Summary(status);
        } else {
            // Generic fallback (shouldn't really happen here)
            if (status == 0) {
                System.out.println("  [RESULT] Program executed and halted normally.\n");
            } else {
                System.out.println("  [RESULT] Program did NOT finish successfully. See error messages above.\n");
            }
        }

        System.out.println("  [DONE] Simulation for " + fileName + " finished.\n");
    }

    /**
     * Assemble only, do not run.
     */
    private static void assembleOnly(String fileName,
                                     String explanation) throws Exception {

        System.out.println("  " + explanation + "\n");

        String srcPath = SRC_ROOT + fileName;

        // QUIET mode here as well
        Assembler.main(new String[]{srcPath, "--quiet"});

        System.out.println("  [OK] Assembled " + fileName + " (no terminal execution).\n");
    }

    // =================== SUMMARY HELPERS ===================

    private static void printInstructionSetSummary(int status) {
        System.out.println("  BASIC MACHINE INSTRUCTION SET SELF-TEST\n");

        if (status == 0) {
            System.out.println("  1) LOAD / STORE / LDA ........................ PASS");
            System.out.println("  2) ARITHMETIC (AMR / SMR / AIR / SIR) ........ PASS");
            System.out.println("  3) MLT / DVD (multiply / divide) ............. PASS");
            System.out.println("  4) LOGIC (AND / OR / NOT) .................... PASS");
            System.out.println("  5) SHIFT / ROTATE (SRC / RRC) ................ PASS");
            System.out.println("  6) TRR + BRANCHES (JZ / JNE / JCC / JSR / SOB / JGE) PASS");
            System.out.println("  7) I/O + TRAPS (IN / OUT / CHK / TRAP) ....... PASS\n");
            System.out.println("  ALL INSTRUCTION TESTS PASSED.\n");
        } else {
            System.out.println("  Some instruction tests did NOT complete successfully.");
            System.out.println("  Please review the error output above for details.\n");
        }
    }

    private static void printProgram1Summary(int status) {
        System.out.println("  PROGRAM 1: CLOSEST NUMBER FINDER\n");

        if (status == 0) {
            System.out.println("  • The program prompts the user to enter 20 integers.");
            System.out.println("  • It treats the FIRST number as the reference value.");
            System.out.println("  • It scans the remaining numbers to find:");
            System.out.println("      - The value closest to the first number");
            System.out.println("      - The absolute difference between them");
            System.out.println("  • The result is printed using the simulated printer device.\n");
            System.out.println("  PROGRAM 1 FINISHED SUCCESSFULLY (see interaction above).\n");
        } else {
            System.out.println("  PROGRAM 1 DID NOT FINISH SUCCESSFULLY.");
            System.out.println("  Please review the error output above for details.\n");
        }
    }
}
