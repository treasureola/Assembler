package Simulator.ui;

import Simulator.ProgramLoader;
import Simulator.core.CPU;
import Simulator.core.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;

public class SimulatorController {

    @FXML private TextField r0Field, r1Field, r2Field, r3Field;
    @FXML private TextField x1Field, x2Field, x3Field;
    @FXML private TextField pcField, marField, mbrField, irField, ccField, mfrField;

    @FXML private ToggleButton r0Btn, r1Btn, r2Btn, r3Btn;
    @FXML private ToggleButton x1Btn, x2Btn, x3Btn;

    @FXML private HBox binaryLeds;
    @FXML private TextField octalInput;
    @FXML private TextField programFileField;
    @FXML private TextArea cacheArea, printerArea;
    @FXML private TextField consoleInput;

    private final CPU cpu = new CPU();
    private int[] memoryRef = cpu.memory();

    private short selectedRegisterBits() { return cpu.getPC(); }

    @FXML
    private void initialize() {

        for (int i = 15; i >= 0; i--) {
            Label lamp = new Label("0");
            lamp.setMinWidth(18);
            lamp.setStyle("-fx-background-color:#cdd7e1; -fx-alignment:center; -fx-padding:2 4 2 4; -fx-font-family:monospace;");
            binaryLeds.getChildren().add(lamp);
        }


        wireBlueButtons();

        refreshAll();
    }


    @FXML private void onBrowse() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Program Listing (.lst)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("List Files", "*.lst", "*.txt"));
        File f = fc.showOpenDialog(binaryLeds.getScene().getWindow());
        if (f != null) programFileField.setText(f.getAbsolutePath());
    }

    @FXML private void onIPL() {
        try {
            File f = programFileField.getText().isEmpty()
                    ? new File("Assembler_Part_0_final_documents/Submission_Documents/output.lst")
                    : new File(programFileField.getText());
            new ProgramLoader().load(f, memoryRef);

            cpu.setPC((short)6);
            printerArea.appendText("[IPL] Program loaded: " + f.getName() + "\n");
            refreshAll();
        } catch (Exception e) {
            showError("IPL failed: " + e.getMessage());
        }
    }

    @FXML private void onStep() {
        try {
            cpu.setMAR(cpu.getPC());
            cpu.setMBR((short)(memoryRef[cpu.getMAR()] & 0xFFFF));
            cpu.setIR(cpu.getMBR());
            cpu.setPC((short)(cpu.getPC() + 1));
            refreshAll();
        } catch (Exception e) {
            showError("Step error: " + e.getMessage());
        }
    }

    @FXML private void onRun() {

        new Thread(() -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    Platform.runLater(this::onStep);
                    Thread.sleep(10);
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    @FXML private void onHalt() {
        printerArea.appendText("[HALT]\n");
    }

    @FXML private void onLoad()     { writeSelected(false); }
    @FXML private void onLoadPlus() { writeSelected(true);  }
    @FXML private void onStore()    { storeSelected(false); }
    @FXML private void onStorePlus(){ storeSelected(true);  }

    private void writeSelected(boolean postInc) {
        try {
            int value = Utils.parseOctal(octalInput.getText());
            cpu.setMBR((short)(value & 0xFFFF));
            memoryRef[cpu.getMAR()] = value & 0xFFFF;
            if (postInc) cpu.setMAR((short)(cpu.getMAR() + 1));
            refreshAll();
        } catch (Exception e) {
            showError("Load/Load+ error: " + e.getMessage());
        }
    }

    private void storeSelected(boolean postInc) {
        cpu.setMBR((short)(memoryRef[cpu.getMAR()] & 0xFFFF));
        if (postInc) cpu.setMAR((short)(cpu.getMAR() + 1));
        refreshAll();
    }

    @FXML private void onConsoleSend() {
        String s = consoleInput.getText();
        if (s == null || s.isEmpty()) return;
        printerArea.appendText("[CONSOLE] " + s + "\n");
        consoleInput.clear();
    }

    private short parseBin4(TextField tf) {
        String s = tf.getText() == null ? "" : tf.getText().trim();
        if (s.isEmpty()) return 0;
        s = s.replaceAll("[^01]", "");
        if (s.isEmpty()) return 0;
        int v = Integer.parseInt(s, 2) & 0xF;
        return (short) v;
    }

    private short parseOct6(TextField tf) {
        String s = tf.getText() == null ? "" : tf.getText().trim();
        if (s.isEmpty()) return 0;
        try {
            return (short) (Utils.parseOctal(s) & 0xFFFF);
        } catch (Exception e) {
            return 0;
        }
    }


    private void refreshAll() {
        // GPR 4
        r0Field.setText(bin4(cpu.getR0()));
        r1Field.setText(bin4(cpu.getR1()));
        r2Field.setText(bin4(cpu.getR2()));
        r3Field.setText(bin4(cpu.getR3()));

        // IXR
        x1Field.setText(Utils.oct(cpu.getX1()));
        x2Field.setText(Utils.oct(cpu.getX2()));
        x3Field.setText(Utils.oct(cpu.getX3()));

        pcField.setText(Utils.oct(cpu.getPC()));
        marField.setText(Utils.oct(cpu.getMAR()));
        mbrField.setText(Utils.oct(cpu.getMBR()));
        irField.setText(Utils.oct(cpu.getIR()));

        ccField.setText(
                String.format("%4s", Integer.toBinaryString(cpu.getCC() & 0xF))
                        .replace(' ', '0')
        );

        mfrField.setText(Utils.oct(cpu.getMFR()));

        short bits = selectedRegisterBits();
        for (int i = 0; i < 16; i++) {
            int b = (bits >> i) & 1;
            Label lamp = (Label) binaryLeds.getChildren().get(15 - i);
            lamp.setText(String.valueOf(b));
            lamp.setStyle(b == 1
                    ? "-fx-background-color:#6fdc8c; -fx-alignment:center; -fx-padding:2 4 2 4; -fx-font-family:monospace;"
                    : "-fx-background-color:#cdd7e1; -fx-alignment:center; -fx-padding:2 4 2 4; -fx-font-family:monospace;");
        }
    }

    private static String bin4(int v) {
        String s = Integer.toBinaryString(v & 0xF);
        return ("0000" + s).substring(s.length());
    }


    private void wireBlueButtons() {
        EventHandler<ActionEvent> pickHandler = e -> {
            if (octalInput == null) return;

            short val = 0;
            Object src = e.getSource();

            // GPR
            if (src == r0Btn)      val = parseBin4(r0Field);
            else if (src == r1Btn) val = parseBin4(r1Field);
            else if (src == r2Btn) val = parseBin4(r2Field);
            else if (src == r3Btn) val = parseBin4(r3Field);

                // IXR
            else if (src == x1Btn) val = parseOct6(x1Field);
            else if (src == x2Btn) val = parseOct6(x2Field);
            else if (src == x3Btn) val = parseOct6(x3Field);

            octalInput.setText(Utils.oct(val));

            if (src instanceof ToggleButton) {
                ((ToggleButton) src).setSelected(false);
            }
        };

        if (r0Btn != null) r0Btn.setOnAction(pickHandler);
        if (r1Btn != null) r1Btn.setOnAction(pickHandler);
        if (r2Btn != null) r2Btn.setOnAction(pickHandler);
        if (r3Btn != null) r3Btn.setOnAction(pickHandler);
        if (x1Btn != null) x1Btn.setOnAction(pickHandler);
        if (x2Btn != null) x2Btn.setOnAction(pickHandler);
        if (x3Btn != null) x3Btn.setOnAction(pickHandler);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Simulator");
        a.showAndWait();
    }
}
