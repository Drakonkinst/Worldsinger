/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.util;

import net.minecraft.util.math.Direction;

public final class ModConstants {

    // Namespaces
    public static final String MOD_ID = "worldsinger";
    public static final String COMMON_ID = "c";

    // Time
    public static final float TICKS_TO_SECONDS = 1.0f / 20.0f;
    public static final int SECONDS_TO_TICKS = 20;
    public static final int MINUTES_TO_SECONDS = 60;
    public static final int GAME_DAYS_TO_MINUTES = 20;
    public static final long VANILLA_DAY_LENGTH = 24000L;
    public static final int MINUTES_TO_TICKS = MINUTES_TO_SECONDS * SECONDS_TO_TICKS;
    public static final int GAME_DAYS_TO_TICKS = GAME_DAYS_TO_MINUTES * MINUTES_TO_TICKS;

    // Direction
    public static final Direction[] CARDINAL_DIRECTIONS = Direction.values();
    public static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    // Other
    public static final int ITEM_DURABILITY_METER_MAX_STEPS = 13;
    public static final int MAX_LIGHT_LEVEL = 15; // TODO: Does this exist elsewhere?

    private ModConstants() {}
}