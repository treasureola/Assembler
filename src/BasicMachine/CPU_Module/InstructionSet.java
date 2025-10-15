package BasicMachine.CPU_Module;

import java.util.HashMap;

/**
 * InstructionSet.java
 * --------------------
 * Provides opcode–mnemonic mapping for debugging and UI display.
 */
public class InstructionSet {
    private static final HashMap<Integer, String> opMap = new HashMap<>();

    static {
        opMap.put(1, "LDR");
        opMap.put(2, "STR");
        opMap.put(33, "HLT");
    }

    /** Return the mnemonic string for a given opcode. */
    public static String getMnemonic(int opcode) {
        return opMap.getOrDefault(opcode, "UNKNOWN");
    }
}
