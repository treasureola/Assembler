package BasicMachine.simulator.core;

public final class Utils {
    private Utils(){}

    public static String oct6(int v) {
        return String.format("%06o", v & 0xFFFF);
    }
}
