package io.github.drakonkinst.worldsinger.mixin.worldgen;

import io.github.drakonkinst.worldsinger.registry.ModLootTables;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensionTypes;
import java.util.Map;
import net.minecraft.class_8934;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.ShipwreckGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShipwreckGenerator.Piece.class)
public abstract class ShipwreckGeneratorPieceMixin {

    @Unique
    private static final Map<String, Identifier> LUMAR_LOOT_TABLES = Map.of(
            "map_chest", ModLootTables.LUMAR_SHIPWRECK_SPROUTER_CHEST,
            "treasure_chest", ModLootTables.LUMAR_SHIPWRECK_CAPTAIN_CHEST,
            "supply_chest", ModLootTables.LUMAR_SHIPWRECK_SUPPLY_CHEST
    );

    @Inject(method = "handleMetadata", at = @At("HEAD"), cancellable = true)
    private void injectLumarLootTables(String metadata, BlockPos pos, ServerWorldAccess world,
            Random random, BlockBox boundingBox, CallbackInfo ci) {
        if (world.getDimension().equals(world.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE)
                .get(ModDimensionTypes.LUMAR))) {
            Identifier identifier = LUMAR_LOOT_TABLES.get(metadata);
            if (identifier != null) {
                // LootableContainerBlockEntity.setLootTable(world, random, pos.down(), identifier);
                class_8934.method_54868(world, random, pos.down(), identifier);
            }
            ci.cancel();
        }
    }
}
