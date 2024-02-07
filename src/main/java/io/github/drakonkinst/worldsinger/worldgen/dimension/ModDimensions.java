package io.github.drakonkinst.worldsinger.worldgen.dimension;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensions {

    public static final Identifier LUMAR = Worldsinger.id("lumar");
    public static final RegistryKey<DimensionType> DIMENSION_TYPE_LUMAR = ModDimensions.registerDimension(
            LUMAR);
    public static final RegistryKey<World> WORLD_LUMAR = ModDimensions.registerWorld(LUMAR);

    public static void initialize() {
        Registry.register(Registries.CHUNK_GENERATOR, Worldsinger.id("lumar"),
                LumarChunkGenerator.CODEC);
    }

    private static RegistryKey<DimensionType> registerDimension(Identifier id) {
        return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, id);
    }

    private static RegistryKey<World> registerWorld(Identifier id) {
        return RegistryKey.of(RegistryKeys.WORLD, id);
    }
}
