package io.github.drakonkinst.worldsinger.mixin.accessor;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.class)
public interface AbstractBlockAccessor {

    @Accessor("settings")
    AbstractBlock.Settings worldsinger$getSettings();
}