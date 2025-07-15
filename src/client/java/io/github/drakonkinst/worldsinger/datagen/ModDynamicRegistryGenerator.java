package io.github.drakonkinst.worldsinger.datagen;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class ModDynamicRegistryGenerator extends FabricDynamicRegistryProvider {

    private final String name;
    private final RegistryKey<? extends Registry<?>> registryKey;

    public ModDynamicRegistryGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registriesFuture, String name,
            RegistryKey<? extends Registry<?>> registryKey) {
        super(output, registriesFuture);
        this.name = name;
        this.registryKey = registryKey;
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup, Entries entries) {
        entries.addAll(wrapperLookup.getOrThrow(registryKey));
    }

    @Override
    public String getName() {
        return name;
    }
}
