package io.github.drakonkinst.worldsinger.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

class BlockPosUtilTest {

    @Test
    void toBlockPos() {
        testToBlockPos(1.0, 2.0, 3.0, 1, 2, 3);
        testToBlockPos(0.1, 1.9, 4.5, 0, 1, 4);
    }

    private void testToBlockPos(double posX, double posY, double posZ, int expectedBlockX,
            int expectedBlockY, int expectedBlockZ) {
        Vec3d pos = new Vec3d(posX, posY, posZ);
        BlockPos blockPos = BlockPosUtil.toBlockPos(pos);
        assertEquals(expectedBlockX, blockPos.getX());
        assertEquals(expectedBlockY, blockPos.getY());
        assertEquals(expectedBlockZ, blockPos.getZ());
    }

    @Test
    void toRoundedBlockPos() {
        testToRoundedBlockPos(1.0, 2.0, 3.0, 1, 2, 3);
        testToRoundedBlockPos(0.1, 1.9, 4.5, 0, 2, 4);
        testToRoundedBlockPos(0.9, 3.2, 6.1, 0, 3, 6);
        testToRoundedBlockPos(0, 3.5, 0, 0, 4, 0);
    }

    private void testToRoundedBlockPos(double posX, double posY, double posZ, int expectedBlockX,
            int expectedBlockY, int expectedBlockZ) {
        Vec3d pos = new Vec3d(posX, posY, posZ);
        BlockPos blockPos = BlockPosUtil.toRoundedBlockPos(pos);
        assertEquals(expectedBlockX, blockPos.getX());
        assertEquals(expectedBlockY, blockPos.getY());
        assertEquals(expectedBlockZ, blockPos.getZ());
    }
}