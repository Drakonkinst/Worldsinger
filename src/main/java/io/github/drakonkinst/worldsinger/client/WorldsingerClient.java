package io.github.drakonkinst.worldsinger.client;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.client.dimension.ModDimensionEffects;
import io.github.drakonkinst.worldsinger.client.fluid.ModFluidRenderers;
import io.github.drakonkinst.worldsinger.client.registry.ModModelPredicates;
import io.github.drakonkinst.worldsinger.datatable.DataTables;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

@Environment(EnvType.CLIENT)
public class WorldsingerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        DataTables.initializeClient();

        // Register fluids
        ModFluidRenderers.register();

        // Register block render layer

        final Block[] cutoutBlocks = {
                ModBlocks.VERDANT_VINE_SNARE,
                ModBlocks.DEAD_VERDANT_VINE_SNARE,
                ModBlocks.TWISTING_VERDANT_VINES,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES,
                ModBlocks.TWISTING_VERDANT_VINES_PLANT,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT,
                ModBlocks.CRIMSON_SPIKE,
                ModBlocks.DEAD_CRIMSON_SPIKE,
                ModBlocks.CRIMSON_SNARE,
                ModBlocks.DEAD_CRIMSON_SNARE,
                ModBlocks.CRIMSON_SPINES,
                ModBlocks.DEAD_CRIMSON_SPINES,
                ModBlocks.TALL_CRIMSON_SPINES,
                ModBlocks.DEAD_TALL_CRIMSON_SPINES,
                ModBlocks.ROSEITE_CLUSTER,
                ModBlocks.LARGE_ROSEITE_BUD,
                ModBlocks.MEDIUM_ROSEITE_BUD,
                ModBlocks.SMALL_ROSEITE_BUD
        };
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), cutoutBlocks);

        final Block[] translucentBlocks = {
                ModBlocks.ROSEITE_BLOCK, ModBlocks.ROSEITE_STAIRS, ModBlocks.ROSEITE_SLAB
        };
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), translucentBlocks);

        // Register particles
        ParticleFactoryRegistry.getInstance()
                .register(ModParticleTypes.SPORE_DUST, SporeDustParticle.Factory::new);

        // Register entity renderers
        EntityRendererRegistry.register(ModEntityTypes.THROWN_SPORE_BOTTLE,
                FlyingItemEntityRenderer::new);

        ModModelPredicates.register();
        ModDimensionEffects.initialize();
    }

}