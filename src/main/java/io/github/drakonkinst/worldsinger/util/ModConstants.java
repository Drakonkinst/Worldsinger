package io.github.drakonkinst.worldsinger.util;

import net.minecraft.util.math.Direction;

public final class ModConstants {

    public static final String MOD_ID = "worldsinger";
    public static final String COMMON_ID = "c";
    public static final float TICKS_TO_SECONDS = 1.0f / 20.0f;
    public static final int SECONDS_TO_TICKS = 20;
    public static final Direction[] CARDINAL_DIRECTIONS = Direction.values();
    public static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    private ModConstants() {}
}