public class MBR {
    private int data;
    public void setData(int value) { this.data = value & 0xFFFF; }
    public int getData() { return data; }
}
