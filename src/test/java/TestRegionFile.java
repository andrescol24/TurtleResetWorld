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
}
