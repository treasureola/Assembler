package BasicMachine.simulator.ui;

import BasicMachine.Memory_Module.Memory;
import BasicMachine.CPU_Module.MachineCore;
import BasicMachine.CPU_Module.Registers;
import BasicMachine.simulator.ProgramLoader;
import BasicMachine.simulator.core.Utils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import java.io.File;

/* ==============================================================
 * SimulatorController.java
 * --------------------------------------------------------------
 * Purpose:
 *   Acts as the controller for the Basic Machine Simulator UI.
 *   Handles all interaction between the user interface (FXML layout)
 *   and the backend modules: Memory, Registers, and MachineCore.
 *
 * Responsibilities:
 *   - Load assembled programs (.ld / .lst) into memory
 *   - Control execution (IPL, Step, Run, Halt)
 *   - Perform memory read/write operations (Load, Store)
 *   - Display register and memory state dynamically
 *   - Manage user console input/output
 *
 * Integration:
 *   Connects UI (simulator.fxml) ↔ CPU backend:
 *     Memory.java         — main memory array (2048 words)
 *     Registers.java      — holds GPR, IXR, PC, MAR, etc.
 *     MachineCore.java    — fetch–decode–execute engine
 *     ProgramLoader.java  — loads .ld files into memory
 *     Utils.java          — handles octal conversions
 *
 * ==============================================================
 */
public class SimulatorController {

    /* ==============================================================
     * UI COMPONENTS (wired via FXML)
     * --------------------------------------------------------------
     * Each field is automatically linked to a control element
     * in simulator.fxml using the @FXML annotation.
     *
     * TextFields → represent CPU registers and user input boxes
     * TextAreas  → display console and memory cache
     * ==============================================================
     */
    @FXML private TextField r0Field, r1Field, r2Field, r3Field;
    @FXML private TextField x1Field, x2Field, x3Field;
    @FXML private TextField pcField, marField, mbrField, irField, ccField, mfrField;
    @FXML private HBox binaryLeds;
    @FXML private TextField octalInput, programFileField;
    @FXML private TextArea cacheArea, printerArea;
    @FXML private TextField consoleInput;

    /* ==============================================================
     * CORE MODULES
     * --------------------------------------------------------------
     * These represent the simulated CPU’s components and are shared
     * throughout the simulator session.
     * ==============================================================
     */
    private Memory memory;
    private Registers registers;
    private MachineCore core;
    private final ProgramLoader loader = new ProgramLoader();

    private volatile boolean programLoaded = false;
    private volatile boolean printedHalt = false;

    /* ==============================================================
     * initialize()
     * --------------------------------------------------------------
     * Called automatically after FXML layout is loaded.
     * Sets up memory, registers, and CPU core for the simulation.
     * ==============================================================
     */
    @FXML
    private void initialize() {
        memory    = new Memory();
        registers = new Registers();
        core      = new MachineCore(memory, registers);
        core.setOnCycleListener(this::refreshAll);  // refresh GUI after each cycle

        registers.reset();
        memory.reset();
        refreshAll();
        printerArea.appendText("[READY] Basic Machine Simulator initialized.\n");
    }

    /* ==============================================================
     * FILE / IPL (Initial Program Load)
     * --------------------------------------------------------------
     * Handles user file browsing and program loading into memory.
     * ==============================================================
     */

    /** Opens file chooser dialog to select .ld or .lst file. */
    @FXML
    private void onBrowse() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Program Listing (.ld or .lst)");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("List or Load Files", "*.ld", "*.lst", "*.txt"));
        File f = fc.showOpenDialog(binaryLeds.getScene().getWindow());
        if (f != null) programFileField.setText(f.getAbsolutePath());
    }

    /** Loads selected program file into memory and sets PC to start address. */
    @FXML
    private void onIPL() {
        try {
            String filePath = programFileField.getText().trim();
            if (filePath.isEmpty()) { showError("Please select a program file."); return; }

            loader.load(new File(filePath), memory);
            programLoaded = true;
            printedHalt = false;

            int startAddr = memory.getProgramStartAddress();
            registers.setPC(startAddr);

            registers.setMAR(0);
            registers.setMBR(0);
            registers.setIR(0);

            printerArea.appendText("[IPL] Program loaded successfully from: "
                    + new File(filePath).getName() + "\n");
            printerArea.appendText(String.format("[START] Program begins at address %04o\n", startAddr));
            refreshAll();
        } catch (Exception e) {
            showError("IPL failed: " + e.getMessage());
        }
    }

    /* ==============================================================
     * EXECUTION CONTROL (STEP / RUN / HALT)
     * --------------------------------------------------------------
     * Controls CPU execution modes.
     * ==============================================================
     */

    /** Executes one fetch–decode–execute cycle. */
    @FXML
    private void onStep() {
        if (!programLoaded) { showError("No program loaded. Perform IPL first."); return; }
        try {
            core.runOneCycle();
            Platform.runLater(this::refreshAll);
            printerArea.appendText("[STEP] One instruction executed.\n");
        } catch (Exception e) {
            showError("Execution step failed: " + e.getMessage());
        }
    }

    /** Runs the loaded program continuously until a HLT instruction. */
    @FXML
    private void onRun() {
        if (!programLoaded) { showError("No program loaded. Perform IPL first."); return; }

        printerArea.appendText("[RUN] Program running...\n");

        new Thread(() -> {
            try {
                while (!core.isHalted()) {
                    Platform.runLater(() -> {
                        core.runOneCycle();
                        refreshAll();
                    });
                    Thread.sleep(300); // delay for visible update
                }
                Platform.runLater(() ->
                        printerArea.appendText("[HALT] Program finished.\n"));
            } catch (InterruptedException ignored) {}
        }).start();
    }

    /** Stops program execution manually. */
    @FXML
    private void onHalt() {
        core.stop();
        printerArea.appendText("[HALT] Simulation stopped by user.\n");
    }

    /* ==============================================================
     * MEMORY OPERATIONS (LOAD / STORE)
     * --------------------------------------------------------------
     * Handles direct memory editing via the MAR and MBR text fields.
     * ==============================================================
     */

    /** LOAD: Writes value from octalInput → memory[MAR]. */
    @FXML
    private void onLoad() {
        try {
            int addr = Utils.parseOctal(marField.getText());
            registers.setMAR(addr);
            int value = Utils.parseOctal(octalInput.getText());
            registers.setMBR(value);
            memory.writeWord(addr, value);

            printerArea.appendText(String.format("[LOAD] Wrote %06o to address %04o\n", value, addr));
            refreshAll();
        } catch (Exception e) {
            showError("Load error: " + e.getMessage());
        }
    }

    /** LOAD+: Same as LOAD, but increments MAR afterward. */
    @FXML
    private void onLoadPlus() {
        try {
            int addr = Utils.parseOctal(marField.getText());
            registers.setMAR(addr);
            int value = Utils.parseOctal(octalInput.getText());
            registers.setMBR(value);
            memory.writeWord(addr, value);

            printerArea.appendText(String.format("[LOAD+] Wrote %06o to address %04o, MAR++\n", value, addr));
            registers.setMAR(addr + 1);
            marField.setText(Utils.oct((short) (addr + 1)));
            refreshAll();
        } catch (Exception e) {
            showError("Load+ error: " + e.getMessage());
        }
    }

    /** STORE: Reads memory[MAR] → MBR → octalInput. */
    @FXML
    private void onStore() {
        try {
            int addr = Utils.parseOctal(marField.getText());
            registers.setMAR(addr);
            int val = memory.readWord(addr);
            registers.setMBR(val);
            octalInput.setText(Utils.oct((short) val));

            printerArea.appendText(String.format("[STORE] Read %06o from address %04o\n", val, addr));
            refreshAll();
        } catch (Exception e) {
            showError("Store error: " + e.getMessage());
        }
    }

    /** STORE+: Same as STORE, but increments MAR afterward. */
    @FXML
    private void onStorePlus() {
        try {
            int addr = Utils.parseOctal(marField.getText());
            registers.setMAR(addr);
            int val = memory.readWord(addr);
            registers.setMBR(val);
            octalInput.setText(Utils.oct((short) val));

            printerArea.appendText(String.format("[STORE+] Read %06o from address %04o, MAR++\n", val, addr));
            registers.setMAR(addr + 1);
            marField.setText(Utils.oct((short) (addr + 1)));
            refreshAll();
        } catch (Exception e) {
            showError("Store+ error: " + e.getMessage());
        }
    }

    /* ==============================================================
     * CONSOLE / USER INPUT
     * --------------------------------------------------------------
     * Provides simple echo-based console I/O for user commands.
     * ==============================================================
     */
    @FXML
    private void onConsoleSend() {
        String s = consoleInput.getText();
        if (s != null && !s.isEmpty()) {
            printerArea.appendText("[CONSOLE] " + s + "\n");
            consoleInput.clear();
        }
    }

    /* ==============================================================
     * REFRESH AND ERROR HANDLING
     * --------------------------------------------------------------
     * Updates all register and memory displays after every instruction.
     * ==============================================================
     */

    /** Updates all on-screen register and memory values. */
    private void refreshAll() {
        pcField.setText (Utils.oct((short) registers.getPC()));
        marField.setText(Utils.oct((short) registers.getMAR()));
        mbrField.setText(Utils.oct((short) registers.getMBR()));
        irField.setText (Utils.oct((short) registers.getIR()));
        mfrField.setText(Utils.oct((short) registers.getMFR()));

        ccField.setText(String.format("%4s",
                Integer.toBinaryString(registers.getCC() & 0xF)).replace(' ', '0'));

        int[] g = registers.getGPR();
        r0Field.setText(Utils.oct((short) g[0]));
        r1Field.setText(Utils.oct((short) g[1]));
        r2Field.setText(Utils.oct((short) g[2]));
        r3Field.setText(Utils.oct((short) g[3]));

        int[] x = registers.getIXR();
        x1Field.setText(Utils.oct((short) x[0]));
        x2Field.setText(Utils.oct((short) x[1]));
        x3Field.setText(Utils.oct((short) x[2]));

        // Update cache/memory display
        StringBuilder sb = new StringBuilder();
        
        // Cache statistics section
        sb.append("=== CACHE STATISTICS ===\n");
        if (memory.isCacheEnabled()) {
            sb.append(memory.getCacheStatistics()).append("\n\n");
            sb.append("=== CACHE CONTENTS ===\n");
            sb.append(memory.getCacheContents()).append("\n");
        } else {
            sb.append("Cache is DISABLED\n\n");
        }
        
        // Memory display section
        sb.append("\n=== MEMORY (First 40 Words) ===\n");
        for (int i = 0; i < 40; i++)
            sb.append(String.format("%04o: %06o\n", i, memory.readWord(i)));
        
        cacheArea.setText(sb.toString());
    }

    /** Displays a simple popup alert for user-visible errors. */
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Simulator");
        a.showAndWait();
    }
    
    /* ==============================================================
     * CACHE CONTROL METHODS (Field Engineer Console)
     * --------------------------------------------------------------
     * Methods for controlling and monitoring the cache system.
     * These can be called from additional UI buttons or menu items.
     * ==============================================================
     */
    
    /**
     * Enable cache tracing to file
     */
    public void enableCacheTracing() {
        memory.enableCacheTracing("cache_trace.log");
        printerArea.appendText("[CACHE] Tracing enabled to cache_trace.log\n");
    }
    
    /**
     * Disable cache tracing
     */
    public void disableCacheTracing() {
        memory.disableCacheTracing();
        printerArea.appendText("[CACHE] Tracing disabled\n");
    }
    
    /**
     * Reset cache statistics
     */
    public void resetCacheStats() {
        memory.resetCacheStatistics();
        printerArea.appendText("[CACHE] Statistics reset\n");
        refreshAll();
    }
    
    /**
     * Toggle cache enable/disable
     */
    public void toggleCache() {
        if (memory.isCacheEnabled()) {
            memory.disableCache();
            printerArea.appendText("[CACHE] Cache DISABLED (direct memory access)\n");
        } else {
            memory.enableCache();
            printerArea.appendText("[CACHE] Cache ENABLED\n");
        }
        refreshAll();
    }
    
    /**
     * Print cache statistics to console
     */
    public void printCacheStats() {
        printerArea.appendText("\n" + memory.getCacheStatistics() + "\n");
    }
}
