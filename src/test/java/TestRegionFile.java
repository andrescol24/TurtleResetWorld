import org.bukkit.World;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

public class TestRegionFile {

    @Test
    public void test_inequality_floor_to_list_of_chunks_in_region() {
        int x = 1;
        int z = 0;
        Set<Integer> set = new TreeSet<>();
        System.out.println(String.format("X Inferior: %d, Superior: %d", 32 * x, 32 * x + 32));
        System.out.println(String.format("Z Inferior: %d, Superior: %d", 32 * z, 32 * z + 32));
        for (int i = 32 * x; i < 32 * x + 32; i++) {
            for (int j = 32 * z; j < 32 * z + 32; j++) {
                int pos = ((i & 31) + (j & 31) * 32);
                int xCalculate = i >> 5;
                int zCalculate = j >> 5;
                System.out.println(String.format("x: %d z: %d, pos: %d, xCalc: %d, zCalc: %d",
                        i, j, pos, xCalculate, zCalculate));

                Assert.assertFalse(set.contains(pos));
                Assert.assertTrue(0 <= pos && pos < 1024);
                Assert.assertTrue(xCalculate == x && zCalculate == z);
                set.add(pos);
            }
        }
    }

    @Test
    public void test_clear_regions_file() throws IOException {
        // After - Copying test world
        URL sourceFolder = this.getClass().getClassLoader().getResource("world");
        File sourceFile = new File(sourceFolder.getFile());
        System.out.println("Source " + sourceFile.getAbsolutePath());

        File destinationFile = new File(sourceFile.getParent(), "temp_world");
        System.out.println("Dest " + destinationFile.getAbsolutePath());

        if(destinationFile.exists()) {
            System.out.println("Deleting destination folder");
            this.deleteDirectory(destinationFile);
        }
        this.copyFiles(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath());

        // Action

        // Assert
    }

    private void copyFiles(String sourceDirectory, String destinationDirectory) throws IOException {
        Files.walk(Paths.get(sourceDirectory))
                .forEach(source -> {
                    System.out.println("Copying " + source.getFileName());
                    Path destination = Paths.get(destinationDirectory, source.toString()
                            .substring(sourceDirectory.length()));
                    try {
                        Files.copy(source, destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        boolean deleted = directoryToBeDeleted.delete();
        System.out.println("deleted: " + directoryToBeDeleted.getAbsolutePath() + "=" + deleted);
        return deleted;
    }
}
