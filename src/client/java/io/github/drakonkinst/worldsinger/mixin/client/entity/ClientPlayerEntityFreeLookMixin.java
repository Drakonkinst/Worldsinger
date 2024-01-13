package io.github.drakonkinst.worldsinger.mixin.client.entity;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.freelook.FreeLook;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityFreeLookMixin extends AbstractClientPlayerEntity implements
        FreeLook {

    @Unique
    private float freeLookYaw = 0.0f;
    @Unique
    private float freeLookPitch = 0.0f;

    public ClientPlayerEntityFreeLookMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void worldsinger$setFreeLookYaw(float yaw) {
        this.freeLookYaw = yaw;
    }

    @Override
    public void worldsinger$setFreeLookPitch(float pitch) {
        this.freeLookPitch = pitch;
    }

    @Override
    public boolean worldsinger$isFreeLookEnabled() {
        // Can add more conditions here, including a manual on/off, though this currently isn't
        // needed
        CameraPossessable possessionTarget = ModComponents.POSSESSION.get(this)
                .getPossessionTarget();
        return possessionTarget != null && possessionTarget.canFreeLook();
    }

    @Override
    public float worldsinger$getFreeLookYaw() {
        return freeLookYaw;
    }

    @Override
    public float worldsinger$getFreeLookPitch() {
        return freeLookPitch;
    }
}
