package io.github.drakonkinst.worldsinger.datagen;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.datagen.provider.EquipmentAssetProvider;
import io.github.drakonkinst.worldsinger.registry.ModEquipmentAssetKeys;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;

public class ModEquipmentAssetGenerator extends EquipmentAssetProvider {

    public ModEquipmentAssetGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    protected void bootstrap(BiConsumer<RegistryKey<EquipmentAsset>, EquipmentModel> consumer) {
        consumer.accept(ModEquipmentAssetKeys.STEEL,
                createHumanoidOnlyModel(Worldsinger.id("steel")));
    }
}
