// Simulator/core/Utils.java
package BasicMachine.simulator.core;

public final class Utils {
    private Utils(){}

    public static String oct(short v){
        int u = v & 0xFFFF;
        return String.format("%06o", u);
    }
    public static int parseOctal(String s){
        if (s==null || s.trim().isEmpty()) throw new IllegalArgumentException("empty octal");
        return Integer.parseInt(s.trim(), 8);
    }
}
