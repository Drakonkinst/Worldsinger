package io.github.drakonkinst.worldsinger.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarSeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.NullSeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManagerAccess;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import java.util.function.Supplier;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldLumarMixin implements WorldAccess, AutoCloseable, SeetheManagerAccess {

    @Unique
    protected boolean isLumar = false;
    @Unique
    protected SeetheManager seetheManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    protected void worldsinger$initializeLumar(MutableWorldProperties properties,
            RegistryKey<World> registryRef, DynamicRegistryManager registryManager,
            RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler,
            boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates,
            CallbackInfo ci) {
        isLumar = registryRef.equals(ModDimensions.WORLD_LUMAR);
        if (isLumar) {
            seetheManager = new LumarSeetheManager();
        } else {
            seetheManager = new NullSeetheManager();
        }
    }

    @ModifyReturnValue(method = "getRainGradient", at = @At("RETURN"))
    private float removeCustomDimensionRainGradient(float originalValue) {
        if (isLumar) {
            return 0.0f;
        }
        return originalValue;
    }

    @ModifyReturnValue(method = "getThunderGradient", at = @At("RETURN"))
    private float removeCustomDimensionThunderGradient(float originalValue) {
        if (isLumar) {
            return 0.0f;
        }
        return originalValue;
    }

    @Override
    public SeetheManager worldsinger$getSeetheManager() {
        return seetheManager;
    }
}
