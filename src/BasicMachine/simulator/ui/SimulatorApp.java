// Simulator/ui/SimulatorApp.java
package BasicMachine.simulator.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimulatorApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/BasicMachine/simulator/ui/simulator.fxml")));
        stage.setTitle("CSCI 6461 Machine Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
