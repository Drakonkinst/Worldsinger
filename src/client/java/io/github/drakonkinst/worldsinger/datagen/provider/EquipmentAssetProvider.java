package io.github.drakonkinst.worldsinger.datagen.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.data.DataOutput.OutputType;
import net.minecraft.data.DataOutput.PathResolver;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public abstract class EquipmentAssetProvider implements DataProvider {

    private final PathResolver pathResolver;

    public EquipmentAssetProvider(FabricDataOutput output) {
        this.pathResolver = output.getResolver(OutputType.RESOURCE_PACK, "equipment");
    }

    protected abstract void bootstrap(
            BiConsumer<RegistryKey<EquipmentAsset>, EquipmentModel> consumer);

    protected EquipmentModel createHumanoidOnlyModel(Identifier id) {
        return EquipmentModel.builder().addHumanoidLayers(id).build();
    }

    protected EquipmentModel createHumanoidAndHorseModel(Identifier id) {
        return EquipmentModel.builder()
                .addHumanoidLayers(id)
                .addLayers(EquipmentModel.LayerType.HORSE_BODY,
                        EquipmentModel.Layer.createWithLeatherColor(id, false))
                .build();
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Map<RegistryKey<EquipmentAsset>, EquipmentModel> map = new HashMap<>();
        bootstrap((key, model) -> {
            if (map.putIfAbsent(key, model) != null) {
                throw new IllegalStateException(
                        "Tried to register equipment asset twice for id: " + key);
            }
        });
        return DataProvider.writeAllToPath(writer, EquipmentModel.CODEC,
                this.pathResolver::resolveJson, map);
    }

    @Override
    public String getName() {
        return "Equipment Asset Definitions";
    }
}
