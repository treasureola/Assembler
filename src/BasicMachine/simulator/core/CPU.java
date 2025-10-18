// Simulator/core/CPU.java
package BasicMachine.simulator.core;

public class CPU {

    private short r0, r1, r2, r3;
    private short x1, x2, x3;


    private short pc, mar, mbr, ir, mfr;
    private short cc;    // 4 bits: O/U/Z/E (示意)
    private final int[] memory = new int[2048];

    public int[] memory() { return memory; }

    // getters/setters
    public short getR0() { return r0; } public void setR0(short v){ r0=v; }
    public short getR1() { return r1; } public void setR1(short v){ r1=v; }
    public short getR2() { return r2; } public void setR2(short v){ r2=v; }
    public short getR3() { return r3; } public void setR3(short v){ r3=v; }
    public short getX1() { return x1; } public void setX1(short v){ x1=v; }
    public short getX2() { return x2; } public void setX2(short v){ x2=v; }
    public short getX3() { return x3; } public void setX3(short v){ x3=v; }

    public short getPC() { return pc; } public void setPC(short v){ pc=v; }
    public short getMAR(){ return mar;} public void setMAR(short v){mar=v;}
    public short getMBR(){ return mbr;} public void setMBR(short v){mbr=v;}
    public short getIR() { return ir; } public void setIR(short v){ ir=v; }
    public short getMFR(){ return mfr;} public void setMFR(short v){mfr=v;}
    public short getCC() { return cc; }  public void setCC(short v){ cc=(short)(v&0xF); }


}
