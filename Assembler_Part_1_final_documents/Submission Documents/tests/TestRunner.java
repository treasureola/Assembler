package tests;

/* ==============================================================
 * TestRunner.java
 * --------------------------------------------------------------
 * Purpose:
 *   Centralized automated test runner for the Basic Machine project.
 *   This class executes all major subsystem tests (Memory, CPU, Integration,
 *   and Simulator) in sequence before launching the GUI.
 *
 * Description:
 *   - Each test is executed via Java reflection.
 *   - Tests print [PASS] or [FAIL] for visual clarity.
 *   - If all tests pass, the simulator GUI (SimulatorRunner) is launched.
 *   - If any test fails, the GUI is not launched to prevent running
 *     an unstable system configuration.
 *
 * Integration Overview:
 *   - This file acts as the bridge between automated grading
 *     and the interactive simulator.
 *   - It is the primary entry point when running `java -jar Basic_Machine.jar`.
 *
 * Components Tested:
 *   1. MemoryTest.java              — Memory read/write validation.
 *   2. CPUInstructionTest.java      — Single-instruction decoding and execution.
 *   3. IntegrationTest.java         — Full assembler + CPU + memory integration.
 *   4. SimulatorRunTest.java        — End-to-end system test with live execution.
 *
 * Expected Output:
 *   - Each test prints “[PASS] TestName” or “[FAIL] TestName”.
 *   - A final summary line displays total passes and failures.
 *   - If all tests succeed, the simulator GUI automatically launches.
 * ==============================================================
 */
public class TestRunner {

    /* ==============================================================
     * main()
     * --------------------------------------------------------------
     * Execution Steps:
     *
     * Step 1 — Initialize Counters:
     *   Two counters track the number of tests passed and failed.
     *
     * Step 2 — Define Test Sequence:
     *   The array "tests" lists each test class in execution order.
     *
     * Step 3 — Execute Tests:
     *   - For each test class, load it dynamically using reflection.
     *   - Invoke its static main() method (standard test entry).
     *   - Capture success or failure.
     *   - Print detailed results for grader visibility.
     *
     * Step 4 — Print Summary:
     *   Display total pass/fail counts in a clear summary format.
     *
     * Step 5 — Conditional GUI Launch:
     *   - If all tests pass, automatically start the simulator GUI
     *     (SimulatorRunner).
     *   - If any test fails, output a warning and skip GUI launch.
     *
     * ==============================================================
     */
    public static void main(String[] args) {
        System.out.println("=== Basic Machine Automated Tests ===");

        int passed = 0, failed = 0;

        // ───────────────────────────────────────────────
        // Step 2: Define test suite
        String[] tests = {
                "tests.MemoryTest",
                "tests.CPUInstructionTest",
                "tests.IntegrationTest",
                "tests.SimulatorRunTest"
        };

        // ───────────────────────────────────────────────
        // Step 3: Execute tests sequentially
        for (String t : tests) {
            try {
                System.out.println("\nRunning " + t + "...");
                // Dynamically load class and call its main(String[]) method
                Class<?> cls = Class.forName(t);
                cls.getDeclaredMethod("main", String[].class)
                        .invoke(null, (Object) new String[]{});
                System.out.println("[PASS] " + t);
                passed++;
            } catch (Throwable e) {
                System.out.println("[FAIL] " + t);
                e.printStackTrace(System.out);
                failed++;
            }
        }

        // ───────────────────────────────────────────────
        // Step 4: Print summary of test outcomes
        System.out.printf("\n=== Test Summary: %d passed, %d failed ===\n", passed, failed);

        // ───────────────────────────────────────────────
        // Step 5: Conditional GUI launch
        if (failed == 0) {
            System.out.println("\nAll tests passed! Launching simulator GUI...\n");
            try {
                BasicMachine.simulator.ui.SimulatorRunner.main(args);
            } catch (Exception e) {
                System.err.println("Simulator failed to launch: " + e.getMessage());
            }
        } else {
            System.out.println("\nSimulator will not run until all tests pass.");
        }
    }
}
