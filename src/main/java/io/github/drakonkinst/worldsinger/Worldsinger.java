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
import io.github.drakonkinst.worldsinger.network.ModPayloadRegistry;
import io.github.drakonkinst.worldsinger.network.ServerNetworkHandler;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.recipe.ModRecipeSerializer;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModDispenserBehaviors;
import io.github.drakonkinst.worldsinger.registry.ModGameRules;
import io.github.drakonkinst.worldsinger.registry.ModMapDecorationTypes;
import io.github.drakonkinst.worldsinger.registry.ModPotions;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
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
        return Identifier.of(ModConstants.MOD_ID, id);
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
        ModGameRules.initialize();
        ModParticleTypes.initialize();
        ModSoundEvents.initialize();
        ModFluids.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModRecipeSerializer.initialize();
        ModEntityTypes.initialize();
        ModCommands.initialize();
        ModPotions.initialize();
        ModMapDecorationTypes.initialize();
        ModCauldronBehaviors.initialize();
        ModDispenserBehaviors.register();
        ModDimensions.initialize();
        ModStructurePieceTypes.initialize();
        ModStructureTypes.initialize();
        ModEventHandlers.initialize();
        ModDataComponentTypes.initialize();

        // AI
        ModMemoryModuleTypes.initialize();
        ModSensors.initialize();
        ModActivities.initialize();

        ModPayloadRegistry.initialize();
        ServerNetworkHandler.initialize();

        // Should always be last
        ModApi.initialize();
    }
}