package io.github.drakonkinst.worldsinger.command.argument;

import com.mojang.brigadier.context.CommandContext;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class CosmerePlanetArgumentType extends EnumArgumentType<CosmerePlanet> {

    private CosmerePlanetArgumentType() {
        super(CosmerePlanet.CODEC, CosmerePlanet::getOrderedPlanets);
    }

    public static CosmerePlanetArgumentType cosmerePlanet() {
        return new CosmerePlanetArgumentType();
    }

    public static CosmerePlanet getCosmerePlanet(CommandContext<ServerCommandSource> context,
            String id) {
        return context.getArgument(id, CosmerePlanet.class);
    }
}
