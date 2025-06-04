package io.github.drakonkinst.worldsinger.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlineSpawner;
import net.minecraft.block.BlockState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
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
public abstract class WorldLumarMixin implements WorldAccess, AutoCloseable, LumarManagerAccess {

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
    protected LumarManager lumarManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLumar(MutableWorldProperties properties, RegistryKey<World> registryRef,
            DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
            boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates,
            CallbackInfo ci) {
        lumarManager = LumarManager.NULL;
    }

    @ModifyExpressionValue(method = "getPrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/world/biome/Biome$Precipitation;"))
    private Precipitation considerRainlinesAsRaining(Precipitation original, BlockPos pos) {
        if (original != Precipitation.NONE) {
            return original;
        }
        if ((Object) this instanceof ServerWorld serverWorld) {
            if (RainlineSpawner.shouldRainlineAffectBlocks(serverWorld, pos.toCenterPos())) {
                return Precipitation.RAIN;
            }
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
    public LumarManager worldsinger$getLumarManager() {
        return lumarManager;
    }
}
