package io.github.drakonkinst.worldsinger.component;

import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import org.jetbrains.annotations.Nullable;

public interface PossessionComponent extends CommonTickingComponent {

    @Nullable CameraPossessable getPossessionTarget();

    void setPossessionTarget(CameraPossessable entity);

    void resetPossessionTarget();

    default boolean isPossessing() {
        return getPossessionTarget() != null;
    }
}
