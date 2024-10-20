/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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

package io.github.drakonkinst.worldsinger.entity.rainline;

import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlinePath;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RainlineFollowPathBehavior implements RainlineBehavior {

    private static final int TICKS_PER_STEP = 20;
    private static final byte NUM_RAINLINES_PER_LUNAGREE = 8;
    private static final String KEY_RAINLINE_ID = "rainline_id";
    private static final String KEY_RAINLINE_INDEX = "rainline_index";

    public static @Nullable RainlineFollowPathBehavior readFromNbt(LumarManager manager,
            NbtCompound nbt) {
        if (!nbt.contains(KEY_RAINLINE_ID, NbtElement.LONG_TYPE) || !nbt.contains(
                KEY_RAINLINE_INDEX, NbtElement.BYTE_TYPE)) {
            return null;
        }
        long rainlineId = nbt.getLong(KEY_RAINLINE_ID);
        byte rainlineIndex = nbt.getByte(KEY_RAINLINE_INDEX);
        RainlinePath rainlinePath = manager.getRainlineManager().getRainlinePathById(rainlineId);
        if (rainlinePath == null) {
            return null;
        }
        return new RainlineFollowPathBehavior(rainlinePath, rainlineIndex);
    }

    private static final float STEPS_PER_TICK = 1.0f / TICKS_PER_STEP;
    private final RainlinePath rainlinePath;
    private final int index;
    private final int stepOffset;

    public RainlineFollowPathBehavior(RainlinePath rainlinePath, int index) {
        this.rainlinePath = rainlinePath;
        this.index = index;
        float percentageOffset = index * 1.0f / NUM_RAINLINES_PER_LUNAGREE;
        this.stepOffset = Math.round(percentageOffset * rainlinePath.getMaxSteps());
    }

    @Override
    public void serverTick(RainlineEntity entity) {
        World world = entity.getWorld();
        long gameTime = world.getTime();
        // Given game time and initial offset, what step should we be on?
        float progress = gameTime * STEPS_PER_TICK + stepOffset;
        int step = (int) progress;
        progress %= 1.0f;

        Vec2f currentStep = rainlinePath.getPositionAtStep(step);
        Vec2f nextStep = rainlinePath.getPositionAtStep(step + 1);

        double x = MathHelper.lerp(progress, currentStep.x, nextStep.x);
        double z = MathHelper.lerp(progress, currentStep.y, nextStep.y);
        entity.setPos(x, entity.getY(), z);
    }

    @Override
    public boolean isFollowingPath() {
        return true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {

    }

}
