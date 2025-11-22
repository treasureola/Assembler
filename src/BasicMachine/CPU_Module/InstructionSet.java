// ✅ InstructionSet.java — Updated
package BasicMachine.CPU_Module;

public final class InstructionSet {

    private InstructionSet() {}

    // Core (Part 1)
    public static final int HLT = 00;
    public static final int LDR = 01;
    public static final int STR = 02;
    public static final int LDA = 03;
    public static final int LDX = 041;
    public static final int STX = 042;

    // Branch / Control
    public static final int JZ  = 010;
    public static final int JNE = 011;
    public static final int JCC = 012;
    public static final int JMA = 013;
    public static final int JSR = 014;
    public static final int RFS = 015;
    public static final int SOB = 016;
    public static final int JGE = 017;

    // Arithmetic / Immediate
    public static final int AMR = 04;
    public static final int SMR = 05;
    public static final int AIR = 06;
    public static final int SIR = 07;

    // Multiply / Divide
    public static final int MLT = 070;
    public static final int DVD = 071;

    // Logical / Test / Reg-Reg
    public static final int TRR = 072;
    public static final int AND = 073;
    public static final int ORR = 074;
    public static final int NOT = 075;

    // Shifts / Rotates
    public static final int SRC = 031;
    public static final int RRC = 032;

    // I/O
    public static final int IN  = 061;
    public static final int OUT = 062;
    public static final int CHK = 063;  // R <- device_ready(dev)

    // Trap
    public static final int TRAP = 024; // trap to vector
}


// ✅ MachineCore.java additions
// Place in decodeAndExecute() inside the switch block:
/*
case InstructionSet.CHK -> {
    int dev = ir & 0xFF;
    execCHK(r, dev);
    printExec("CHK");
}
case InstructionSet.TRAP -> {
    int trapCode = ir & 0xFF;
    execTRAP(trapCode);
    printExec("TRAP");
}
*/

// Add methods inside MachineCore.java class:
/*
private void execCHK(int r, int devId) {
    int ready = 1; // For simulation, assume device is always ready
    regs.setGPRValue(r, ready);
    printToPrinter(String.format("[CHK] R%d ← dev=%d ready=%d", r, devId, ready));
}

private void execTRAP(int trapCode) {
    switch (trapCode) {
        case 0x00 -> printToPrinter("[TRAP 0] Hello from the OS!");
        case 0x01 -> {
            halted = true;
            printToPrinter("[TRAP 1] Halting via trap.");
        }
        default -> printToPrinter(String.format("[TRAP %02o] Unknown trap code.", trapCode));
    }
}
*/

// ✅ Program2_test.src
/*
* Program 2 – TRAP and CHK demo
IN    R0, 0
OUT   R0, 0
CHK   R1, 0      ; Should return 1 if device is ready
TRAP  0          ; Should print "Hello from the OS!"
TRAP  1          ; Halts the program
HLT
*/
