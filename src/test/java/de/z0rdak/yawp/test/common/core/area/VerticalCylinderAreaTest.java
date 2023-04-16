package de.z0rdak.yawp.test.common.core.area;

import de.z0rdak.yawp.core.area.VerticalCylinderArea;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerticalCylinderAreaTest {

    private static final List<VerticalCylinderArea> cylindersToTest = new ArrayList<>();
    private static final BlockPos center = new BlockPos(0, 10, 0);
    private static Map<Integer, Set<BlockPos>> validPositionsForRadius;
    private static Map<Integer, Set<BlockPos>> invalidPositionsForRadius;
    private static Map<Integer, Integer> expectedValidForRadius;
    private static Map<Integer, Integer> expectedInvalidForRadius;

    @BeforeAll
    public static void setup() {
        validPositionsForRadius = new HashMap<>();
        invalidPositionsForRadius = new HashMap<>();
        expectedValidForRadius = new HashMap<>();
        expectedInvalidForRadius = new HashMap<>();

        for (int i = 0; i < 6; i++) {
            cylindersToTest.add(new VerticalCylinderArea(center, i, 10));
            validPositionsForRadius.put(i, new HashSet<>());
            invalidPositionsForRadius.put(i, new HashSet<>());
        }

        expectedValidForRadius.put(0, 1);
        expectedValidForRadius.put(1, 11);
        expectedValidForRadius.put(2, 99);
        expectedValidForRadius.put(3, 231);
        expectedValidForRadius.put(4, 407);
        expectedValidForRadius.put(5, 759);

        expectedInvalidForRadius.put(0, 192);
        expectedInvalidForRadius.put(1, 421);
        expectedInvalidForRadius.put(2, 669);
        expectedInvalidForRadius.put(3, 969);
        expectedInvalidForRadius.put(4, 1321);
        expectedInvalidForRadius.put(5, 1593);

        for (int r = 0; r < 6; r++) {
            validPositionsForRadius.get(r).add(center);
            BlockPos pos;
            for (int y = center.getY()-1; y < center.getY()+10+1; y++) {
                for  (int x = -r-2; x < r+2; x++) {
                    for (int z = -r-2; z < r+2; z++) {
                        pos = new BlockPos(center.getX()+x, y, center.getZ()+z);
                        if (cylindersToTest.get(r).contains(pos)){
                            validPositionsForRadius.get(r).add(pos);
                        } else {
                            invalidPositionsForRadius.get(r).add(pos);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testValidPositions(){
        for (int i = 0; i < 6; i++) {
            assertEquals(invalidPositionsForRadius.get(i).size(), expectedInvalidForRadius.get(i));
            assertEquals(validPositionsForRadius.get(i).size(), expectedValidForRadius.get(i));
        }
    }

    @Test
    public void testSerialization() {
        cylindersToTest.forEach(verticalCylinderArea -> {
            CompoundTag area = verticalCylinderArea.serializeNBT();
            VerticalCylinderArea clone = new VerticalCylinderArea(area);

            // assertEquals(verticalCylinderArea.getCenter(), clone.getCenter(), "Center '" + verticalCylinderArea.getCenter() + "' not equal to '" + clone.getCenter().toString() + "'");
            // assertEquals(verticalCylinderArea.getRadius(), clone.getRadius(), "Radius '" + verticalCylinderArea.getRadius() + "' not equal to '" + clone.getRadius() + "'");
            // assertEquals(verticalCylinderArea.getDistance(), clone.getDistance(), "Distance '" + verticalCylinderArea.getDistance() + "' not equal to '" + clone.getDistance() + "'");
            // assertEquals(verticalCylinderArea.getAreaType(), clone.getAreaType(), "AreaType '" + verticalCylinderArea.getAreaType().toString() + "' not equal to '" + clone.getAreaType().toString() + "'");
            // assertEquals(verticalCylinderArea.toString(), clone.toString(), "CuboidArea '" + verticalCylinderArea + "' not equal to '" + clone + "'");
        });
    }
}
