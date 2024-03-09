package io.github.drakonkinst.worldsinger.mixin.world;

import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldAccess;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldData;
import java.util.function.Supplier;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldCosmereMixin implements WorldAccess, AutoCloseable, CosmereWorldAccess {

    @Shadow
    @Final
    protected MutableWorldProperties properties;
    @Unique
    protected CosmerePlanet planet;

    @Unique
    protected CosmereWorldData cosmereWorldData;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLumar(MutableWorldProperties properties, RegistryKey<World> registryRef,
            DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
            Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess,
            int maxChainedNeighborUpdates, CallbackInfo ci) {
        planet = CosmerePlanet.getPlanetFromKey(registryRef);
        cosmereWorldData = new CosmereWorldData();
    }

    @Override
    public CosmerePlanet worldsinger$getPlanet() {
        return planet;
    }

    @Override
    public CosmereWorldData worldsinger$getCosmereWorldData() {
        return cosmereWorldData;
    }

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void getCosmereTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            cir.setReturnValue(cosmereWorldData.getTimeOfDay());
        }
    }
}
