public class FloatingPointUnit {
    // Convert 16-bit custom FP -> Java double
    public static double fp16ToDouble(int word16) {
        word16 &= 0xFFFF;
        int sign = (word16 >>> 15) & 0x1;
        int exp7 = (word16 >>> 8) & 0x7F;
        int mant8 = word16 & 0xFF;
        int exp = (exp7 & 0x40) != 0 ? exp7 - 0x80 : exp7;
        double mantissa = (double) mant8 / 256.0;
        double val = mantissa * Math.pow(2.0, exp);
        return (sign == 1) ? -val : val;
    }

    // Convert Java double -> 16-bit custom FP
    public static int doubleToFp16(double val) {
        int sign = val < 0 ? 1 : 0;
        double abs = Math.abs(val);
        if (abs == 0.0) return (sign << 15);

        int exp = (int) Math.floor(Math.log(abs) / Math.log(2.0));
        double mant = abs / Math.pow(2.0, exp);
        while (mant >= 1.0) { mant /= 2.0; exp++; }

        int mant8 = (int) Math.round(mant * 256.0);
        if (mant8 > 255) mant8 = 255;

        if (exp < -63) exp = -63;
        if (exp > 63) exp = 63;
        int exp7 = (exp < 0) ? ((exp + 128) & 0x7F) : (exp & 0x7F);

        return (sign << 15) | (exp7 << 8) | (mant8 & 0xFF);
    }

    public static int addFp16(int frWord, int memWord) {
        return doubleToFp16(fp16ToDouble(frWord) + fp16ToDouble(memWord));
    }

    public static int subFp16(int frWord, int memWord) {
        return doubleToFp16(fp16ToDouble(frWord) - fp16ToDouble(memWord));
    }

    public static int intToFp16(int fixed) {
        if ((fixed & 0x8000) != 0) fixed = fixed - 0x10000;
        return doubleToFp16((double) fixed);
    }

    public static int fp16ToInt(int fpWord) {
        long rounded = Math.round(fp16ToDouble(fpWord));
        if (rounded < Short.MIN_VALUE) rounded = Short.MIN_VALUE;
        if (rounded > Short.MAX_VALUE) rounded = Short.MAX_VALUE;
        return (int) (rounded & 0xFFFF);
    }
}
