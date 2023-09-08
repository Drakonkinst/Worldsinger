package io.github.drakonkinst.worldsinger;

import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.command.ModCommands;
import io.github.drakonkinst.worldsinger.datatable.DataTables;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.registry.ModRegistries;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class Worldsinger implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstants.LOGGER.info("Initializing Worldsinger...");

        ModProperties.initialize();
        ModParticleTypes.initialize();
        ModFluids.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModEntityTypes.initialize();
        ModCommands.initialize();

        Fluidlogged.initialize();
        ModRegistries.register();
        ModApi.initialize();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new DataTables());

        CommonLifecycleEvents.TAGS_LOADED.register(((registries, client) -> {
            if (!client) {
                if (DataTables.INSTANCE != null) {
                    DataTables.INSTANCE.resolveTags();
                } else {
                    ModConstants.LOGGER.error(
                            "Failed to resolve tags for data tables: Data tables not initialized");
                }
            }
        }));
    }
}