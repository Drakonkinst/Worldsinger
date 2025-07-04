package io.github.drakonkinst.worldsinger.command.argument;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public final class ModArgumentTypes {

    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(Worldsinger.id("planet"),
                CosmerePlanetArgumentType.class,
                ConstantArgumentSerializer.of(CosmerePlanetArgumentType::cosmerePlanet));
    }

    private ModArgumentTypes() {}
}
