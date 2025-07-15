package io.github.drakonkinst.worldsinger.registry;

import java.util.function.Function;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ModRenderLayer {

    private static final int BUFFER_SIZE = 1536; // 16 * 16 * 6. I have no idea why this number is special
    private static final Function<Identifier, RenderLayer> LUMAR_MOONS = Util.memoize(
            texture -> RenderLayer.of("celestial", BUFFER_SIZE, false, false,
                    ModRenderPipelines.POSITION_TEX_COLOR_LUMAR_MOONS,
                    RenderLayer.MultiPhaseParameters.builder()
                            .texture(new RenderPhase.Texture(texture, false))
                            .build(false)));

    public static RenderLayer getLumarMoons(Identifier texture) {
        return LUMAR_MOONS.apply(texture);
    }
}
