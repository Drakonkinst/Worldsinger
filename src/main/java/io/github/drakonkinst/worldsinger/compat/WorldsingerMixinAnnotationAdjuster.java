package io.github.drakonkinst.worldsinger.compat;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableModifyArgNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import java.util.List;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// https://github.com/Bawnorton/MixinSquared/wiki/Mixin-Annotation-Adjuster
public class WorldsingerMixinAnnotationAdjuster implements MixinAnnotationAdjuster {

    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName,
            MethodNode method, AdjustableAnnotationNode annotation) {
        // Fix ModMenu MixinTitleScreen to work on 1.21.6 before it has properly updated
        if (mixinClassName.equals("com.terraformersmc.modmenu.mixin.TitleScreen") && annotation.is(
                ModifyArg.class)) {
            AdjustableModifyArgNode modifyArgNode = annotation.as(AdjustableModifyArgNode.class);
            if (modifyArgNode.getAt()
                    .getTarget()
                    .equals("Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I")) {
                return null; // Just disable the mixin for now
            }
        }
        return annotation;
    }
}
