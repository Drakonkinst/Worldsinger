package io.github.drakonkinst.worldsinger.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManagerAccess;
import io.github.drakonkinst.worldsinger.entity.RainlineEntity;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome.Precipitation;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldLumarMixin implements WorldAccess, AutoCloseable, SeetheManagerAccess {

    @Shadow
    public abstract RegistryKey<World> getRegistryKey();

    @Shadow
    public abstract boolean setBlockState(BlockPos pos, BlockState state);

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    @Shadow
    @Final
    public Random random;
    @Unique
    protected SeetheManager seetheManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLumar(MutableWorldProperties properties, RegistryKey<World> registryRef,
            DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
            Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess,
            int maxChainedNeighborUpdates, CallbackInfo ci) {
        seetheManager = SeetheManager.NULL;
    }

    @ModifyExpressionValue(method = "hasRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    private boolean considerRainlinesAsRaining(boolean original, BlockPos pos) {
        return original || RainlineEntity.isRainlineOver((World) (Object) this, pos.toCenterPos());
    }

    // This approach isn't perfect, but is minimally invasive
    // Rainlines won't work in biomes without precipitation outside Lumar
    // Another approach would be to inject at HEAD, but this would require re-doing checks
    // and be less compatible
    // Another approach would be some kind of captured local, but trying to avoid this
    @ModifyExpressionValue(method = "hasRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;"))
    private Precipitation makeLumarAlwaysRaining(Precipitation original) {
        if (CosmerePlanet.isLumar((World) (Object) this)) {
            return Precipitation.RAIN;
        }
        return original;
    }

    @ModifyReturnValue(method = "getRainGradient", at = @At("RETURN"))
    private float removeCustomDimensionRainGradient(float originalValue) {
        if (CosmerePlanet.isLumar((World) (Object) this)) {
            return 0.0f;
        }
        return originalValue;
    }

    @ModifyReturnValue(method = "getThunderGradient", at = @At("RETURN"))
    private float removeCustomDimensionThunderGradient(float originalValue) {
        if (CosmerePlanet.isLumar((World) (Object) this)) {
            return 0.0f;
        }
        return originalValue;
    }

    @Override
    public SeetheManager worldsinger$getSeetheManager() {
        return seetheManager;
    }
}
