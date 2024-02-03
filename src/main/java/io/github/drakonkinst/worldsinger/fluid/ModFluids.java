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
package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModFluids {

    public static final FlowableFluid DEAD_SPORES = register("dead_spores",
            new DeadSporeFluid.Still());
    public static final FlowableFluid FLOWING_DEAD_SPORES = register("flowing_dead_spores",
            new DeadSporeFluid.Flowing());
    public static final FlowableFluid VERDANT_SPORES = register("verdant_spores",
            new VerdantSporeFluid.Still());
    public static final FlowableFluid FLOWING_VERDANT_SPORES = register("flowing_verdant_spores",
            new VerdantSporeFluid.Flowing());
    public static final FlowableFluid CRIMSON_SPORES = register("crimson_spores",
            new CrimsonSporeFluid.Still());
    public static final FlowableFluid FLOWING_CRIMSON_SPORES = register("flowing_crimson_spores",
            new CrimsonSporeFluid.Flowing());
    public static final FlowableFluid ZEPHYR_SPORES = register("zephyr_spores",
            new ZephyrSporeFluid.Still());
    public static final FlowableFluid FLOWING_ZEPHYR_SPORES = register("flowing_zephyr_spores",
            new ZephyrSporeFluid.Flowing());
    public static final FlowableFluid SUNLIGHT_SPORES = register("sunlight_spores",
            new SunlightSporeFluid.Still());
    public static final FlowableFluid FLOWING_SUNLIGHT_SPORES = register("flowing_sunlight_spores",
            new SunlightSporeFluid.Flowing());
    public static final FlowableFluid ROSEITE_SPORES = register("roseite_spores",
            new RoseiteSporeFluid.Still());
    public static final FlowableFluid FLOWING_ROSEITE_SPORES = register("flowing_roseite_spores",
            new RoseiteSporeFluid.Flowing());
    public static final FlowableFluid MIDNIGHT_SPORES = register("midnight_spores",
            new MidnightSporeFluid.Still());
    public static final FlowableFluid FLOWING_MIDNIGHT_SPORES = register("flowing_midnight_spores",
            new MidnightSporeFluid.Flowing());
    public static final StillFluid SUNLIGHT = register("sunlight", new SunlightFluid());

    public static <T extends Fluid> T register(String id, T fluid) {
        return Registry.register(Registries.FLUID, Worldsinger.id(id), fluid);
    }

    public static void initialize() {}

    private ModFluids() {}
}
