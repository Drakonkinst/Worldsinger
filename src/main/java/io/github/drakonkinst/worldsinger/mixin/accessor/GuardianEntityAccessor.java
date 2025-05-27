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
package io.github.drakonkinst.worldsinger.mixin.accessor;

import net.minecraft.entity.mob.GuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuardianEntity.class)
public interface GuardianEntityAccessor {

    // The method provided in the GuardianEntity class interpolates this value
    @Accessor("spikesExtension")
    float worldsinger$getSpikesExtension();

    @Accessor("spikesExtension")
    void worldsinger$setSpikesExtension(float value);

    @Accessor("lastSpikesExtension")
    void worldsinger$setLastSpikesExtension(float value);

    // The method provided in the GuardianEntity class interpolates this value
    @Accessor("tailAngle")
    float worldsinger$getTailAngle();

    @Accessor("tailAngle")
    void worldsinger$setTailAngle(float value);

    @Accessor("lastTailAngle")
    void worldsinger$setLastTailAngle(float value);
}
