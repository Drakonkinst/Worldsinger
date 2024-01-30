package io.github.drakonkinst.worldsinger;

import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.block.ModCauldronBehaviors;
import io.github.drakonkinst.worldsinger.command.ModCommands;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.entity.ai.ModActivities;
import io.github.drakonkinst.worldsinger.entity.ai.ModMemoryModuleTypes;
import io.github.drakonkinst.worldsinger.entity.ai.sensor.ModSensors;
import io.github.drakonkinst.worldsinger.event.ModEventHandlers;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.network.CommonProxy;
import io.github.drakonkinst.worldsinger.network.ServerNetworkHandler;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.registry.ModDispenserBehaviors;
import io.github.drakonkinst.worldsinger.registry.ModPotions;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensionTypes;
import io.github.drakonkinst.worldsinger.worldgen.structure.ModStructurePieceTypes;
import io.github.drakonkinst.worldsinger.worldgen.structure.ModStructureTypes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worldsinger implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_ID);

    public static CommonProxy PROXY;

    public static Identifier id(String id) {
        return new Identifier(ModConstants.MOD_ID, id);
    }

    public static String idStr(String id) {
        return ModConstants.MOD_ID + ":" + id;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Worldsinger...");
        PROXY = new CommonProxy();

        // I'll figure out the proper order for these...one day
        Fluidlogged.initialize();

        ModProperties.initialize();
        ModParticleTypes.initialize();
        ModSoundEvents.initialize();
        ModFluids.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModEntityTypes.initialize();
        ModCommands.initialize();
        ModPotions.initialize();
        ModCauldronBehaviors.initialize();
        ModDispenserBehaviors.register();
        ModDimensionTypes.initialize();
        ModStructurePieceTypes.initialize();
        ModStructureTypes.initialize();
        ModEventHandlers.initialize();

        // AI
        ModMemoryModuleTypes.initialize();
        ModSensors.initialize();
        ModActivities.initialize();

        ServerNetworkHandler.registerPacketHandler();

        ModApi.initialize();
    }
}