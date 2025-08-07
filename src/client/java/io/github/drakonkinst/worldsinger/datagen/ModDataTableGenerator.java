package io.github.drakonkinst.worldsinger.datagen;

import io.github.drakonkinst.datatables.DataTableEntryProvider;
import io.github.drakonkinst.datatables.DataTableRegistry.DataTableEntry;
import io.github.drakonkinst.datatables.DataTableType;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.ModDataTables;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalBlockTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

public class ModDataTableGenerator extends DataTableEntryProvider {

    protected ModDataTableGenerator(FabricDataOutput dataOutput,
            CompletableFuture<WrapperLookup> registriesFuture) {
        super(dataOutput, registriesFuture);
    }

    @Override
    protected void accept(BiConsumer<Identifier, DataTableEntry> consumer) {
        consumer.accept(ModDataTables.CONSUMABLE_HYDRATION, DataTableEntry.builder()
                .type(DataTableType.ITEM)
                .tag(ConventionalItemTags.MILK_BUCKETS, 20)
                .entry(Items.POTION, 20)
                .entry(Items.ENCHANTED_GOLDEN_APPLE, 16)
                .entry(Items.MUSHROOM_STEW, 12)
                .entry(Items.SUSPICIOUS_STEW, 12)
                .entry(Items.RABBIT_STEW, 12)
                .entry(Items.GOLDEN_APPLE, 8)
                .entry(Items.HONEY_BOTTLE, 6)
                .entry(Items.GOLDEN_CARROT, 6)
                .entry(Items.APPLE, 4)
                .entry(Items.MELON_SLICE, 2)
                .entry(Items.CARROT, 3)
                .entry(Items.BEETROOT, 1)
                .entry(Items.BAKED_POTATO, 2)
                .entry(Items.CHORUS_FRUIT, 2)
                .entry(Items.SWEET_BERRIES, 1)
                .entry(Items.GLOW_BERRIES, 1)
                .entry(Items.POTATO, 1)
                .tag(ModItemTags.ALL_RAW_MEAT, 1)
                .tag(ModItemTags.ALL_COOKED_MEAT, 1)
                .entry(Items.DRIED_KELP, -1)
                .entry(Items.ROTTEN_FLESH, -2)
                .entry(Items.SPIDER_EYE, -2)
                .entry(Items.POISONOUS_POTATO, -2)
                .entry(ModItems.SALT, -4)
                .build());
        // TODO: Make this based on conventional item tags for metal armor pieces instead?
        consumer.accept(ModDataTables.ARMOR_METAL_CONTENT, DataTableEntry.builder()
                .type(DataTableType.ITEM)
                .entry(Items.IRON_HELMET, 5)
                .entry(Items.GOLDEN_HELMET, 5)
                .entry(Items.NETHERITE_HELMET, 5)
                .entry(Items.CHAINMAIL_HELMET, 3)
                .entry(ModItems.STEEL_HELMET, 5)
                .entry(Items.IRON_CHESTPLATE, 8)
                .entry(Items.GOLDEN_CHESTPLATE, 8)
                .entry(Items.NETHERITE_CHESTPLATE, 8)
                .entry(Items.CHAINMAIL_CHESTPLATE, 4)
                .entry(ModItems.STEEL_CHESTPLATE, 8)
                .entry(Items.IRON_LEGGINGS, 7)
                .entry(Items.GOLDEN_LEGGINGS, 7)
                .entry(Items.NETHERITE_LEGGINGS, 7)
                .entry(Items.CHAINMAIL_LEGGINGS, 4)
                .entry(ModItems.STEEL_LEGGINGS, 7)
                .entry(Items.IRON_BOOTS, 4)
                .entry(Items.GOLDEN_BOOTS, 4)
                .entry(Items.NETHERITE_BOOTS, 4)
                .entry(Items.CHAINMAIL_BOOTS, 2)
                .entry(ModItems.STEEL_BOOTS, 4)
                .build());
        // TODO: Add other metals too, only care about steel/iron/silver for now
        consumer.accept(ModDataTables.BLOCK_METAL_CONTENT, DataTableEntry.builder()
                .type(DataTableType.BLOCK)
                .tag(ConventionalBlockTags.STORAGE_BLOCKS_IRON, 9)
                .tag(ConventionalBlockTags.STORAGE_BLOCKS_COPPER, 9)
                .tag(ConventionalBlockTags.STORAGE_BLOCKS_GOLD, 9)
                .tag(ConventionalBlockTags.STORAGE_BLOCKS_NETHERITE, 9)
                .tag(ModConventionalBlockTags.STORAGE_BLOCKS_STEEL, 9)
                .tag(ModBlockTags.STEEL_ANVIL, 12)
                .tag(BlockTags.ANVIL, 12)
                .tag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON, 8)
                .tag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_GOLD, 8)
                .tag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER, 8)
                .tag(ModConventionalBlockTags.STORAGE_BLOCKS_RAW_SILVER, 8)
                .tag(ModBlockTags.ALL_CAULDRONS, 7)
                .entry(Blocks.BLAST_FURNACE, 5)
                .entry(Blocks.IRON_TRAPDOOR, 4)
                .tag(ConventionalBlockTags.COPPER_ORES, 2)
                .tag(ConventionalBlockTags.IRON_ORES, 2)
                .tag(ConventionalBlockTags.GOLD_ORES, 2)
                .tag(ConventionalBlockTags.NETHERITE_SCRAP_ORES, 2) // May want to change this
                .tag(ModConventionalBlockTags.SILVER_ORES, 2)
                .entry(Blocks.HOPPER, 2)
                .entry(Blocks.IRON_BARS, 2)
                .entry(Blocks.SMITHING_TABLE, 2)
                .entry(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 2)
                .entry(Blocks.IRON_DOOR, 1)
                .entry(Blocks.STONECUTTER, 1)
                .entry(Blocks.LANTERN, 1)
                .entry(Blocks.SOUL_LANTERN, 1)
                .tag(BlockTags.RAILS, 1)
                .entry(Blocks.PISTON, 1)
                .entry(Blocks.STICKY_PISTON, 1)
                .entry(Blocks.TRIPWIRE_HOOK, 1)
                .build());
        consumer.accept(ModDataTables.ENTITY_METAL_CONTENT, DataTableEntry.builder()
                .type(DataTableType.ENTITY)
                .tag(ConventionalEntityTypeTags.MINECARTS, 5)
                .entry(EntityType.IRON_GOLEM, 36)
                .build());
        consumer.accept(ModDataTables.SPORE_KILLING_RADIUS, DataTableEntry.builder()
                .type(DataTableType.BLOCK)
                .tag(ModBlockTags.SALTSTONE, 2)
                .tag(ModConventionalBlockTags.SALT_ORES, 2)
                .tag(ModConventionalBlockTags.SILVER_ORES, 3)
                .tag(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER, 5)
                .tag(ModConventionalBlockTags.STORAGE_BLOCKS_RAW_SILVER, 4)
                .tag(ModConventionalBlockTags.STORAGE_BLOCKS_SALT, 4)
                .build());
    }
}
