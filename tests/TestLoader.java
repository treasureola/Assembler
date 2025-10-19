import BasicMachine.simulator.ProgramLoader;
import java.io.File;

public class TestLoader {
    public static void main(String[] args) {
        try {
            int[] memory = new int[2048];
            File file = new File("output.lst");
            System.out.println("Loading file: " + file.getAbsolutePath());
            System.out.println("File exists: " + file.exists());
            
            new ProgramLoader().load(file, memory);
            
            System.out.println("Load successful!");
            for (int i = 0; i < 20; i++) {
                if (memory[i] != 0) {
                    System.out.printf("Memory[%d] = %06o%n", i, memory[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


