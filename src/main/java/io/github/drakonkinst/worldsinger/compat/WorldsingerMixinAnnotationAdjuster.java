package io.github.drakonkinst.worldsinger.compat;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import java.util.List;
import org.objectweb.asm.tree.MethodNode;

// https://github.com/Bawnorton/MixinSquared/wiki/Mixin-Annotation-Adjuster
public class WorldsingerMixinAnnotationAdjuster implements MixinAnnotationAdjuster {

    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName,
            MethodNode method, AdjustableAnnotationNode annotation) {
        // Nothing for now
        return annotation;
    }
}
