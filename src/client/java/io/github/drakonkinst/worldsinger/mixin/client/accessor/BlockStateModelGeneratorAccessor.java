package io.github.drakonkinst.worldsinger.mixin.client.accessor;

import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStateModelGenerator.class)
public interface BlockStateModelGeneratorAccessor {

    @Accessor("UP_DEFAULT_ROTATION_OPERATIONS")
    static BlockStateVariantMap<ModelVariantOperator> worldsinger$getUpDefaultFacingVariantMap() {
        throw new NotImplementedException();
    }
}

