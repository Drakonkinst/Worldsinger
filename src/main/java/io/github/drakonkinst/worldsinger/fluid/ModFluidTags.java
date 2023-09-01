package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModFluidTags {

    public static final TagKey<Fluid> AETHER_SPORES = ModFluidTags.of("aether_spores");
    public static final TagKey<Fluid> STILL_AETHER_SPORES = ModFluidTags.of("still_aether_spores");
    public static final TagKey<Fluid> VERDANT_SPORES = ModFluidTags.of("verdant_spores");
    public static final TagKey<Fluid> DEAD_SPORES = ModFluidTags.of("dead_spores");

    private ModFluidTags() {}

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, new Identifier(ModConstants.MOD_ID, id));
    }
}
