package io.github.drakonkinst.worldsinger.registry;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;

// RenderPipelines are scary and I have no idea how they work
// But we shall do our best to power through
public class ModRenderPipelines {

    // Modeled after POSITION_TEX_COLOR_CELESTIAL
    public static final RenderPipeline POSITION_TEX_COLOR_LUMAR_MOONS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
                    .withLocation(Worldsinger.id("pipeline/lumar_moons"))
                    .withVertexShader("core/position_tex_color")
                    .withFragmentShader("core/position_tex_color")
                    .withSampler("Sampler0")
                    // .withBlend(BlendFunction.OVERLAY)
                    .withDepthWrite(false)
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR,
                            VertexFormat.DrawMode.QUADS)
                    .build());

}
