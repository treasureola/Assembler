package BasicMachine.simulator.ui;

import BasicMachine.simulator.core.CPU;
import BasicMachine.simulator.ProgramLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;

public class SimulatorController {

    @FXML private TextArea printerArea;
    @FXML private TextField r0Field, r1Field, r2Field, r3Field;
    @FXML private TextField x1Field, x2Field, x3Field;
    @FXML private TextField pcField, marField, mbrField, irField, ccField, mfrField;
    @FXML private TextField octalInput, programFileField;
    @FXML private TextField consoleInputField;

    private final CPU cpu = new CPU();
    private ProgramLoader loader;

    // ===================== IPL =====================
    @FXML
    private void onIPL(ActionEvent e) {
        System.out.println("IPL pressed");

        try {
            // Clear CPU and memory before loading
            cpu.reset();

            String path = programFileField.getText();
            if (path == null || path.isEmpty()) {
                printerArea.appendText("[ERROR] No program file selected.\n");
                return;
            }

            printerArea.appendText("[IPL] Loading program: " + path + "\n");

            // ✅ FIX 1: Use no-arg constructor
            ProgramLoader loader = new ProgramLoader();

            // ✅ FIX 2: Use File object, not String
            File programFile = new File(path);
            loader.load(programFile, cpu.getMemory());

            // Set PC and MAR to start address
            int start = cpu.getMemory().getProgramStartAddress();
            cpu.getRegisters().setPC(start);
            cpu.getRegisters().setMAR(start);

            printerArea.appendText(String.format("[IPL] Program loaded successfully. Start address = %06o\n", start));

            refreshAll();
        } catch (Exception ex) {
            ex.printStackTrace();
            printerArea.appendText("[IPL ERROR] " + ex.getMessage() + "\n");
        }
    }


    // ===================== RUN =====================
    @FXML private void onRun(ActionEvent e) {
        printerArea.appendText("[RUN] Running program...\n");
        cpu.run();
        printerArea.appendText("[HALT] Program stopped.\n");
        refreshAll();
    }

    // ===================== STEP =====================
    @FXML private void onStep(ActionEvent e) {
        printerArea.appendText("[STEP] One instruction executed.\n");
        cpu.step();
        refreshAll();
    }

    // ===================== HALT =====================
    @FXML
    private void onHalt(ActionEvent e) {
        printerArea.appendText("[HALT] CPU halted.\n");
        refreshAll();
    }

    // ===================== LOAD =====================
    @FXML
    private void onLoad(ActionEvent e) {
        try {
            String text = octalInput.getText().trim();
            if (text.isEmpty()) {
                printerArea.appendText("[LOAD ERROR] No octal value entered.\n");
                return;
            }

            int word = Integer.parseInt(text, 8) & 0xFFFF;
            int mar = cpu.getRegisters().getMAR() & 0x0FFF;
            cpu.getRegisters().setMBR(word);
            cpu.getMemory().writeWord(mar, word);

            printerArea.appendText(String.format("[LOAD] Wrote %06o to MEM[%06o]\n", word, mar));
            refreshAll();
        } catch (Exception ex) {
            printerArea.appendText("[LOAD ERROR] " + ex.getMessage() + "\n");
        }
    }

    // ===================== LOAD+ =====================
    @FXML
    private void onLoadPlus(ActionEvent e) {
        onLoad(e);
        cpu.getRegisters().setMAR((cpu.getRegisters().getMAR() + 1) & 0x0FFF);
        refreshAll();
    }

    // ===================== STORE =====================
    @FXML
    private void onStore(ActionEvent e) {
        try {
            int mar = cpu.getRegisters().getMAR() & 0x0FFF;
            int val = cpu.getMemory().readWord(mar) & 0xFFFF;
            cpu.getRegisters().setMBR(val);

            printerArea.appendText(String.format("[STORE] Read %06o from MEM[%06o] into MBR\n", val, mar));
            refreshAll();
        } catch (Exception ex) {
            printerArea.appendText("[STORE ERROR] " + ex.getMessage() + "\n");
        }
    }

    // ===================== STORE+ =====================
    @FXML
    private void onStorePlus(ActionEvent e) {
        onStore(e);
        cpu.getRegisters().setMAR((cpu.getRegisters().getMAR() + 1) & 0x0FFF);
        refreshAll();
    }

    // ===================== BROWSE =====================
    @FXML
    private void onBrowse(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Program File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Load Files (*.ld)", "*.ld"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            programFileField.setText(f.getAbsolutePath());
            printerArea.appendText("[BROWSE] " + f.getAbsolutePath() + "\n");
        }
    }

    // ===================== CONSOLE SEND =====================
    @FXML
    private void onConsoleSend(ActionEvent e) {
        if (consoleInputField == null) return;
        String msg = consoleInputField.getText().trim();
        if (!msg.isEmpty()) printerArea.appendText("[CONSOLE] " + msg + "\n");
        consoleInputField.clear();
    }

    // ===================== REFRESH ALL =====================
    private void refreshAll() {
        r0Field.setText(oct(cpu.getR0()));
        r1Field.setText(oct(cpu.getR1()));
        r2Field.setText(oct(cpu.getR2()));
        r3Field.setText(oct(cpu.getR3()));
        x1Field.setText(oct(cpu.getX1()));
        x2Field.setText(oct(cpu.getX2()));
        x3Field.setText(oct(cpu.getX3()));
        pcField.setText(oct(cpu.getPC()));
        marField.setText(oct(cpu.getMAR()));
        mbrField.setText(oct(cpu.getMBR()));
        irField.setText(oct(cpu.getIR()));
        ccField.setText(String.format("%04d", cpu.getCC()));
        mfrField.setText(oct(cpu.getMFR()));
    }

    private String oct(int val) {
        return String.format("%06o", val & 0xFFFF);
    }


}
