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
package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class ModDamageTypes {

    public static final RegistryKey<DamageType> VERDANT_SPORE = ModDamageTypes.of("verdant_spore");
    public static final RegistryKey<DamageType> CRIMSON_SPORE = ModDamageTypes.of("crimson_spore");
    public static final RegistryKey<DamageType> ZEPHYR_SPORE = ModDamageTypes.of("zephyr_spore");
    public static final RegistryKey<DamageType> ROSEITE_SPORE = ModDamageTypes.of("roseite_spore");
    public static final RegistryKey<DamageType> SUNLIGHT_SPORE = ModDamageTypes.of(
            "sunlight_spore");
    public static final RegistryKey<DamageType> MIDNIGHT_ESSENCE = ModDamageTypes.of(
            "midnight_essence");
    public static final RegistryKey<DamageType> SPIKE = ModDamageTypes.of("spike");
    public static final RegistryKey<DamageType> SPIKE_FALL = ModDamageTypes.of("spike_fall");
    public static final RegistryKey<DamageType> DROWN_SPORE = ModDamageTypes.of("drown_spore");
    public static final RegistryKey<DamageType> THIRST = ModDamageTypes.of("thirst");

    // TODO: Create custom damage type
    public static final RegistryKey<DamageType> SUNLIGHT = ModDamageTypes.of(
            new Identifier("lava"));

    private static RegistryKey<DamageType> of(String id) {
        return ModDamageTypes.of(Worldsinger.id(id));
    }

    private static RegistryKey<DamageType> of(Identifier id) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id);
    }

    public static DamageSource createSource(World world, RegistryKey<DamageType> key) {
        return new DamageSource(
                world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }

    private ModDamageTypes() {}
}
