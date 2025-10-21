package BasicMachine.simulator.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.File;
import java.net.URL;

/* ==============================================================
 * SimulatorRunner.java
 * --------------------------------------------------------------
 * Purpose:
 *   Serves as the standalone launcher for the Basic Machine Simulator.
 *   This is the entry point used by graders and testers when running
 *   the packaged JAR file for the project.
 *
 * Key Features:
 *   - Automatically locates and loads the GUI layout (simulator.fxml)
 *   - Opens the interactive simulator window (JavaFX interface)
 *   - Detects if a “load.ld” file is present in the working directory
 *     and reports it as ready for execution (via IPL)
 *
 * Integration:
 *   The SimulatorRunner sits at the top of the UI layer, connecting
 *   all simulator components:
 *       ├── SimulatorApp.java         (alternative UI launcher)
 *       ├── SimulatorController.java  (UI logic & CPU/memory bindings)
 *       ├── MachineCore.java          (fetch–decode–execute logic)
 *       ├── ProgramLoader.java        (loads assembled .ld files)
 *       └── Memory / Registers        (hardware simulation)
 *
 * Context:
 *   - Included as the main class in MANIFEST.MF.
 *   - Automatically executed when the grader runs:
 *       java -jar Basic_Machine.jar
 *
 * ==============================================================
 */
public class SimulatorRunner extends Application {

    /* ==============================================================
     * start()
     * --------------------------------------------------------------
     * Invoked automatically by the JavaFX runtime when this class is
     * launched as an application.
     *
     * Steps:
     *   1. Locate simulator.fxml (UI layout file)
     *   2. Create and configure a JavaFX Scene from that layout
     *   3. Set up and display the application window (Stage)
     *   4. Perform an automatic check for the “load.ld” file
     *
     * Parameters:
     *   stage — the top-level window (Stage) that hosts the UI
     *
     * Error Handling:
     *   If simulator.fxml cannot be found, a clear error message is
     *   printed to the console, helping graders fix missing resources.
     * ==============================================================
     */
    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("=== Launching Basic Machine Simulator ===");

        // Locate FXML layout for the simulator interface
        URL fxml = SimulatorRunner.class.getResource("simulator.fxml");
        if (fxml == null) {
            System.err.println("[ERROR] Could not find simulator.fxml in BasicMachine/simulator/ui/");
            System.err.println("Make sure the FXML file is inside: src/BasicMachine/simulator/ui/");
            return;
        }

        // Load the FXML and create a new JavaFX scene
        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load());

        // Configure and display the simulator window
        stage.setTitle("CSCI 6461: Basic Machine Simulator");
        stage.setScene(scene);
        stage.show();

        // Automatically check for existing load.ld (assembler output)
        File loadFile = new File("load.ld");
        if (loadFile.exists()) {
            System.out.println("[AUTO-LOAD] Found load.ld — program ready for IPL.");
        } else {
            System.out.println("[NOTE] No load.ld found. Assemble a program before IPL.");
        }
    }

    /* ==============================================================
     * main()
     * --------------------------------------------------------------
     * Standard Java entry point for launching the simulator.
     *
     * Usage:
     *   This method is automatically executed when the grader runs
     *   the packaged JAR file:
     *
     *       java -jar Basic_Machine.jar
     *
     * or manually with JavaFX libraries specified:
     *
     *       java --module-path "/path/to/javafx-sdk/lib" \
     *            --add-modules javafx.controls,javafx.fxml \
     *            -jar Basic_Machine.jar
     *
     * The call to Application.launch() delegates control to the
     * JavaFX lifecycle, which in turn calls start(Stage).
     * ==============================================================
     */
    public static void main(String[] args) {
        Application.launch(SimulatorRunner.class, args);
    }
}
