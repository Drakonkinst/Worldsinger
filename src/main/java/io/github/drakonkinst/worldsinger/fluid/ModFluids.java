package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModFluids {

    public static final FlowableFluid DEAD_SPORES = register("dead_spores",
            new DeadSporeFluid.Still()
    );
    public static final FlowableFluid FLOWING_DEAD_SPORES = register("flowing_dead_spores",
            new DeadSporeFluid.Flowing()
    );
    public static final FlowableFluid VERDANT_SPORES = register("verdant_spores",
            new VerdantSporeFluid.Still()
    );
    public static final FlowableFluid FLOWING_VERDANT_SPORES = register("flowing_verdant_spores",
            new VerdantSporeFluid.Flowing()
    );

    public static <T extends Fluid> T register(String id, T fluid) {
        Identifier fluidId = new Identifier(ModConstants.MOD_ID, id);
        return Registry.register(Registries.FLUID, fluidId, fluid);
    }

    public static void initialize() {}

    private ModFluids() {}
}
