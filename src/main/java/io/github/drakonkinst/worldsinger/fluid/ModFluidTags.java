package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.Constants;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModFluidTags {

    private ModFluidTags() {
    }

    public static final TagKey<Fluid> AETHER_SPORES = ModFluidTags.of("aether_spores");
    public static final TagKey<Fluid> STILL_AETHER_SPORES = ModFluidTags.of("still_aether_spores");
    public static final TagKey<Fluid> FLUIDLOGGABLE = ModFluidTags.of("fluidloggable");

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, new Identifier(Constants.MOD_ID, id));
    }
}