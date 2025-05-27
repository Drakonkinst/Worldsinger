package io.github.drakonkinst.worldsinger.util;

import net.minecraft.util.math.MathHelper;

public final class HexCoordUtil {

    // Associative array of direction vector offsets for axial hex coordinates
    private static final int[] DIRECTION_Q = { +1, +1, +0, -1, -1, +0 };
    private static final int[] DIRECTION_R = { +0, -1, -1, +0, +1, +1 };
    private static final float RAD_3 = MathHelper.sqrt(3);
    private static final float RAD_3_OVER_3 = RAD_3 / 3.0f;

    // Key is hex coordinates packed into a long
    // https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
    private static long packHexCoords(int q, int r) {
        return (((long) q) << 32) | (r & 0xffffffffL);
    }

    public static int getQ(long packedCoords) {
        return (int) (packedCoords >> 32);
    }

    public static int getR(long packedCoords) {
        return (int) packedCoords;
    }

    public static long[] getNeighborKeys(long centerKey) {
        long[] neighborKeys = new long[DIRECTION_Q.length];
        int q = getQ(centerKey);
        int r = getR(centerKey);

        for (int i = 0; i < DIRECTION_Q.length; ++i) {
            int neighborQ = q + DIRECTION_Q[i];
            int neighborR = r + DIRECTION_R[i];
            neighborKeys[i] = packHexCoords(neighborQ, neighborR);
        }
        return neighborKeys;
    }

    public static String cellToString(int q, int r) {
        return "(" + q + ", " + r + ")";
    }

    public static int getCenterXForHexCell(int q, int r, float cellSize) {
        return Math.round(1.5f * q * cellSize);
    }

    public static int getCenterZForHexCell(int q, int r, float cellSize) {
        return Math.round((RAD_3 * 0.5f * q + RAD_3 * r) * cellSize);
    }

    // Convert block pos to flat-top pixel coordinates, rounded
    public static long getHexCellForBlockPos(int blockX, int blockZ, float cellSize, int centerX,
            int centerZ) {
        float fracQ = (2.0f / 3.0f * (blockX - centerX)) / cellSize;
        float fracR = ((-1.0f / 3.0f) * (blockX - centerX) + RAD_3_OVER_3 * (blockZ - centerZ))
                / cellSize;
        return roundAxial(fracQ, fracR);
    }

    private static long roundAxial(float fracQ, float fracR) {
        float fracS = -fracQ - fracR;
        int q = Math.round(fracQ);
        int r = Math.round(fracR);
        int s = Math.round(fracS);
        float deltaQ = Math.abs(q - fracQ);
        float deltaR = Math.abs(r - fracR);
        float deltaS = Math.abs(s - fracS);

        if (deltaQ > deltaR && deltaQ > deltaS) {
            q = -r - s;
        } else if (deltaR > deltaS) {
            r = -q - s;
        }
        return packHexCoords(q, r);
    }

    private HexCoordUtil() {}
}
