package BasicMachine.simulator.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/* ==============================================================
 * SimulatorApp.java
 * --------------------------------------------------------------
 * Purpose:
 *   Acts as the main graphical entry point for the Basic Machine Simulator.
 *   It launches the JavaFX-based user interface (UI) that allows users
 *   and graders to:
 *      - Load the assembled program (.ld file)
 *      - View and edit registers and memory
 *      - Execute instructions step-by-step or continuously
 *      - Observe the effects of CPU operations (LDR, STR, LDX, etc.)
 *
 * Integration:
 *   - Loads the FXML layout file: simulator.fxml
 *   - Connects the layout to its controller: SimulatorController.java
 *   - Displays the fully interactive window titled “CSCI 6461 Machine Simulator”
 *
 * Usage:
 *   This class is executed automatically when the JAR file is launched
 *   (if it is the defined main entry in MANIFEST.MF),
 *   or manually via:
 *       java --module-path "path/to/javafx/lib" \
 *            --add-modules javafx.controls,javafx.fxml \
 *            -cp out BasicMachine.simulator.ui.SimulatorApp
 *
 * ==============================================================
 */
public class SimulatorApp extends Application {

    /* ==============================================================
     * start()
     * --------------------------------------------------------------
     * Called automatically by the JavaFX runtime when the application
     * is launched. This method initializes and displays the simulator UI.
     *
     * Steps:
     *   1. Loads the layout from simulator.fxml (stored in UI resources)
     *   2. Creates a new Scene object using the layout
     *   3. Sets the scene on the application window (Stage)
     *   4. Displays the window to the user
     *
     * Parameters:
     *   stage — the top-level container (window) provided by JavaFX
     *
     * Throws:
     *   Exception if the FXML file cannot be found or loaded.
     * ==============================================================
     */
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(
                FXMLLoader.load(getClass().getResource("/BasicMachine/simulator/ui/simulator.fxml")));

        stage.setTitle("CSCI 6461 Machine Simulator");
        stage.setScene(scene);
        stage.show();
    }

    /* ==============================================================
     * main()
     * --------------------------------------------------------------
     * Launches the JavaFX application.
     *
     * This is the entry point when running the simulator directly
     * via the command line or when double-clicking the compiled JAR.
     *
     * Example usage:
     *   java --module-path "/path/to/javafx/lib" \
     *        --add-modules javafx.controls,javafx.fxml \
     *        -jar Basic_Machine.jar
     * ==============================================================
     */
    public static void main(String[] args) {
        launch(args);  // Delegate control to JavaFX runtime
    }
}
