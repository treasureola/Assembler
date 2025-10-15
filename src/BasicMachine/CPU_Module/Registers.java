package BasicMachine.CPU_Module;

/**
 * Registers.java
 * ----------------
 * Implements all CPU registers for the Basic Machine.
 *
 * Integration:
 *  - Used by MachineCore.java to fetch, store, and modify values.
 *  - Shared with UI_Module (for visual display of register values).
 *  - MAR and MBR connect directly with Memory_Module.
 */
class Register {  // internal helper, not public
    private int value;
    private final int size;

    public Register(int size) {
        this.size = size;
        this.value = 0;
    }

    /** Set register value (masked to register width). */
    public void set(int val) {
        int mask = (1 << size) - 1;
        this.value = val & mask;
    }

    /** Get current value. */
    public int get() { return value; }

    /** Reset to zero. */
    public void clear() { value = 0; }
}

public class Registers {
    // Special registers
    public Register PC  = new Register(12);  // Program Counter
    public Register MAR = new Register(12);  // Memory Address Register
    public Register MBR = new Register(16);  // Memory Buffer Register
    public Register IR  = new Register(16);  // Instruction Register

    // General Purpose Registers (R0–R3)
    public Register[] GPR = {
            new Register(16), new Register(16),
            new Register(16), new Register(16)
    };

    // Index Registers (X1–X3)
    public Register[] IXR = {
            new Register(16), new Register(16), new Register(16)
    };
}
