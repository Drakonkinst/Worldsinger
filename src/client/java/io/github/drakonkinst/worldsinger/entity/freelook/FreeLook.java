package io.github.drakonkinst.worldsinger.entity.freelook;

// Specialized free look implementation for when we want to allow clients to look around
// (though not moving the camera's position) on client-side, while the player has a different
// rotation server-side.
public interface FreeLook {

    void worldsinger$setFreeLookYaw(float yaw);

    void worldsinger$setFreeLookPitch(float pitch);

    boolean worldsinger$isFreeLookEnabled();

    float worldsinger$getFreeLookYaw();

    float worldsinger$getFreeLookPitch();
}
