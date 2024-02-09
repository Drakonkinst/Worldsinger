/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.block;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.registry.ModSoundGroups;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Offsetter;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.LavaCauldronBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.biome.Biome;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModBlocks {

    // Dead Spores
    public static final Block DEAD_SPORE_SEA = register("dead_spore_sea",
            new AetherSporeFluidBlock(DeadSpores.getInstance(),
                    createSporeFluidSettings(false, MapColor.GRAY)), false);
    public static final Block DEAD_SPORE_BLOCK = register("dead_spore_block",
            new AetherSporeBlock(DeadSpores.getInstance(), AbstractBlock.Settings.create()
                    // Same as sand
                    .strength(0.5f).mapColor(MapColor.GRAY).sounds(ModSoundGroups.SPORES)), false);
    public static final Block DEAD_VERDANT_VINE_BLOCK = register("dead_verdant_vine_block",
            new VerdantVineBlock(AbstractBlock.Settings.create()
                    // 0.5x strength of living version
                    .strength(1.0f)
                    .requiresTool()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.VERDANT_VINE_BRANCH)), true);
    public static final Block DEAD_VERDANT_VINE_BRANCH = register("dead_verdant_vine_branch",
            new VerdantVineBranchBlock(AbstractBlock.Settings.create()
                    // 0.5x strength of living version
                    .strength(0.4f)
                    .solid()
                    .requiresTool()
                    .nonOpaque()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.VERDANT_VINE_BRANCH)), true);
    public static final Block DEAD_VERDANT_VINE_SNARE = register("dead_verdant_vine_snare",
            new VerdantVineSnareBlock(
                    AbstractBlock.Settings.create()
                            .solid()
                            .noCollision()
                            .requiresTool()
                            // 0.5x strength of living version
                            .strength(0.4f)
                            .ticksRandomly()
                            .mapColor(MapColor.LIGHT_GRAY)
                            .sounds(ModSoundGroups.VERDANT_VINE_SNARE)
                            .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_TWISTING_VERDANT_VINES = register("dead_twisting_verdant_vines",
            new TwistingVerdantVineBlock(AbstractBlock.Settings.create()
                    // 0.5x strength of living version
                    .strength(0.2f)
                    .noCollision()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.TWISTING_VERDANT_VINES)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_TWISTING_VERDANT_VINES_PLANT = register(
            "dead_twisting_verdant_vines_plant",
            new TwistingVerdantVineStemBlock(AbstractBlock.Settings.create()
                    // 0.5x strength of living version
                    .strength(0.2f)
                    .noCollision()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.TWISTING_VERDANT_VINES)
                    .pistonBehavior(PistonBehavior.DESTROY)), false);
    public static final Block DEAD_CRIMSON_GROWTH = register("dead_crimson_growth",
            new CrimsonGrowthBlock(AbstractBlock.Settings.create()
                    // Same strength as Coral Block, same blast resistance as Pointed Dripstone
                    .strength(1.5f, 3.0f)
                    .requiresTool()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_GROWTH)), true);
    public static final Block DEAD_CRIMSON_SPIKE = register("dead_crimson_spike",
            new CrimsonSpikeBlock(
                    ModBlocks.createSettingsWithCustomOffsetter(CrimsonSpikeBlock.getOffsetter())
                            // Same strength as Pointed Dripstone
                            .strength(1.5f, 3.0f)
                            .solid()
                            .ticksRandomly()
                            .dynamicBounds()
                            .requiresTool()
                            .solidBlock(Blocks::never)
                            .mapColor(MapColor.LIGHT_GRAY)
                            .sounds(ModSoundGroups.CRIMSON_SPINE)
                            .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_CRIMSON_SNARE = register("dead_crimson_snare",
            new CrimsonSnareBlock(AbstractBlock.Settings.create()
                    // Same as Amethyst Bud
                    .strength(1.5f)
                    .solid()
                    .requiresTool()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_SPINE)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_TALL_CRIMSON_SPINES = register("dead_tall_crimson_spines",
            new TallCrimsonSpinesBlock(AbstractBlock.Settings.create()
                    // Same as Amethyst Bud
                    .strength(1.5f)
                    .solid()
                    .ticksRandomly()
                    .dynamicBounds()
                    .offset(AbstractBlock.OffsetType.XZ)
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_SPINE)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_CRIMSON_SPINES = register("dead_crimson_spines",
            new CrimsonSpinesBlock(AbstractBlock.Settings.create()
                    // Same as Amethyst Bud
                    .strength(1.5f)
                    .solid()
                    .noCollision()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_SPINE)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);

    // Verdant Spores
    public static final Block VERDANT_SPORE_SEA = register("verdant_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.VERDANT_SPORES, VerdantSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.DARK_GREEN)), false);
    public static final Block VERDANT_SPORE_BLOCK = register("verdant_spore_block",
            new LivingAetherSporeBlock(VerdantSpores.getInstance(), ModBlocks.VERDANT_SPORE_SEA,
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.DARK_GREEN)), false);
    public static final Block VERDANT_VINE_BLOCK = register("verdant_vine_block",
            new LivingVerdantVineBlock(
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_VERDANT_VINE_BLOCK)
                            // Same strength as log
                            .strength(2.0f).mapColor(MapColor.GREEN)), true);
    public static final Block VERDANT_VINE_BRANCH = register("verdant_vine_branch",
            new LivingVerdantVineBranchBlock(AbstractBlock.Settings.copy(DEAD_VERDANT_VINE_BRANCH)
                    // 2x the strength of Chorus Plant
                    .strength(0.8f).mapColor(MapColor.GREEN)), true);
    public static final Block VERDANT_VINE_SNARE = register("verdant_vine_snare",
            new LivingVerdantVineSnareBlock(AbstractBlock.Settings.copy(DEAD_VERDANT_VINE_SNARE)
                    // Same strength as branch
                    .strength(0.8f).mapColor(MapColor.GREEN)), true);
    public static final Block TWISTING_VERDANT_VINES = register("twisting_verdant_vines",
            new LivingTwistingVerdantVineBlock(
                    AbstractBlock.Settings.copy(DEAD_TWISTING_VERDANT_VINES)
                            // Same strength as Ladder
                            .strength(0.4f).mapColor(MapColor.GREEN)), true);
    public static final Block TWISTING_VERDANT_VINES_PLANT = register(
            "twisting_verdant_vines_plant", new LivingTwistingVerdantVineStemBlock(
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT)
                            // Same strength as Twisting Verdant Vines
                            .strength(0.4f).mapColor(MapColor.GREEN)), false);

    // Crimson Spores
    public static final Block CRIMSON_SPORE_SEA = register("crimson_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.CRIMSON_SPORES, CrimsonSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.DARK_RED)), false);
    public static final Block CRIMSON_SPORE_BLOCK = register("crimson_spore_block",
            new LivingAetherSporeBlock(CrimsonSpores.getInstance(), ModBlocks.CRIMSON_SPORE_SEA,
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.DARK_RED)), false);
    public static final Block CRIMSON_GROWTH = register("crimson_growth",
            new LivingCrimsonGrowthBlock(AbstractBlock.Settings.copy(ModBlocks.DEAD_CRIMSON_GROWTH)
                    // Double hardness, same resistance as dead version
                    .strength(3.0f, 3.0f).mapColor(MapColor.RED)), true);
    public static final Block CRIMSON_SPIKE = register("crimson_spike",
            new LivingCrimsonSpikeBlock(AbstractBlock.Settings.copy(ModBlocks.DEAD_CRIMSON_SPIKE)
                    // Double hardness, same resistance as dead version
                    .strength(3.0f, 3.0f).mapColor(MapColor.RED)), true);
    public static final Block CRIMSON_SNARE = register("crimson_snare",
            new LivingCrimsonSnareBlock(AbstractBlock.Settings.copy(ModBlocks.DEAD_CRIMSON_SNARE)
                    // Slightly stronger than dead version
                    .strength(2.0f).mapColor(MapColor.RED)), true);
    public static final Block TALL_CRIMSON_SPINES = register("tall_crimson_spines",
            new LivingTallCrimsonSpinesBlock(
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_TALL_CRIMSON_SPINES)
                            // Slightly stronger than dead version
                            .strength(2.0f).mapColor(MapColor.RED)), true);
    public static final Block CRIMSON_SPINES = register("crimson_spines",
            new LivingCrimsonSpinesBlock(AbstractBlock.Settings.copy(ModBlocks.DEAD_CRIMSON_SPINES)
                    // Slightly stronger than dead version
                    .strength(2.0f).mapColor(MapColor.RED)), true);

    // Zephyr Spores
    public static final Block ZEPHYR_SPORE_SEA = register("zephyr_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.ZEPHYR_SPORES, ZephyrSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.LIGHT_BLUE)), false);
    public static final Block ZEPHYR_SPORE_BLOCK = register("zephyr_spore_block",
            new LivingAetherSporeBlock(ZephyrSpores.getInstance(), ModBlocks.ZEPHYR_SPORE_SEA,
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.LIGHT_BLUE)), false);

    // Sunlight Spores
    public static final Block SUNLIGHT_SPORE_SEA = register("sunlight_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.SUNLIGHT_SPORES, SunlightSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.TERRACOTTA_YELLOW)), false);
    public static final Block SUNLIGHT_SPORE_BLOCK = register("sunlight_spore_block",
            new LivingAetherSporeBlock(SunlightSpores.getInstance(), ModBlocks.SUNLIGHT_SPORE_SEA,
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.TERRACOTTA_YELLOW)), false);
    public static final Block SUNLIGHT = register("sunlight", new SunlightBlock(
            AbstractBlock.Settings.create()
                    .strength(100.0f)
                    .noCollision()
                    .dropsNothing()
                    .liquid()
                    .replaceable()
                    .ticksRandomly()
                    .luminance(SunlightBlock.STATE_TO_LUMINANCE)
                    .mapColor(MapColor.YELLOW)
                    .pistonBehavior(PistonBehavior.DESTROY)), false);

    // Roseite Spores
    public static final Block ROSEITE_SPORE_SEA = register("roseite_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.ROSEITE_SPORES, RoseiteSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.PINK)), false);
    public static final Block ROSEITE_SPORE_BLOCK = register("roseite_spore_block",
            new LivingAetherSporeBlock(RoseiteSpores.getInstance(), ModBlocks.ROSEITE_SPORE_SEA,
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.PINK)), false);
    public static final Block ROSEITE_BLOCK = register("roseite_block",
            new RoseiteBlock(AbstractBlock.Settings.create()
                    .nonOpaque()
                    // Double strength, same blast resistance as Stone
                    .strength(3.0F, 6.0F)
                    .ticksRandomly()
                    .mapColor(MapColor.TERRACOTTA_PINK)
                    .sounds(ModSoundGroups.ROSEITE)), true);
    public static final Block ROSEITE_STAIRS = register("roseite_stairs",
            new RoseiteStairsBlock(ROSEITE_BLOCK.getDefaultState(),
                    AbstractBlock.Settings.copy(ROSEITE_BLOCK)), true);
    public static final Block ROSEITE_SLAB = register("roseite_slab",
            new RoseiteSlabBlock(AbstractBlock.Settings.copy(ROSEITE_BLOCK)), true);
    public static final Block ROSEITE_CLUSTER = register("roseite_cluster",
            new RoseiteClusterBlock(7.0F, 3.0F, AbstractBlock.Settings.create()
                    .mapColor(MapColor.PURPLE)
                    .solid()
                    .nonOpaque()
                    .sounds(ModSoundGroups.ROSEITE)
                    .ticksRandomly()
                    .strength(1.5F)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block LARGE_ROSEITE_BUD = register("large_roseite_bud",
            new RoseiteClusterBlock(5.0F, 3.0F,
                    AbstractBlock.Settings.copy(ROSEITE_CLUSTER).sounds(ModSoundGroups.ROSEITE)),
            true);
    public static final Block MEDIUM_ROSEITE_BUD = register("medium_roseite_bud",
            new RoseiteClusterBlock(4.0F, 3.0F,
                    AbstractBlock.Settings.copy(ROSEITE_CLUSTER).sounds(ModSoundGroups.ROSEITE)),
            true);
    public static final Block SMALL_ROSEITE_BUD = register("small_roseite_bud",
            new RoseiteClusterBlock(3.0F, 4.0F,
                    AbstractBlock.Settings.copy(ROSEITE_CLUSTER).sounds(ModSoundGroups.ROSEITE)),
            true);

    // Midnight Spores
    public static final Block MIDNIGHT_SPORE_SEA = register("midnight_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.MIDNIGHT_SPORES, MidnightSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.BLACK)), false);
    public static final Block MIDNIGHT_SPORE_BLOCK = register("midnight_spore_block",
            new LivingAetherSporeBlock(MidnightSpores.getInstance(), ModBlocks.MIDNIGHT_SPORE_SEA,
                    AbstractBlock.Settings.copy(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.BLACK)), false);
    public static final Block MIDNIGHT_ESSENCE = register("midnight_essence",
            new MidnightEssenceBlock(AbstractBlock.Settings.create()
                    // Impossible to break, but can be destroyed
                    .strength(-1.0f, 0.5f)
                    .dropsNothing()
                    .noBlockBreakParticles()
                    .ticksRandomly()
                    .solidBlock(Blocks::never)
                    .sounds(ModSoundGroups.MIDNIGHT_ESSENCE)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);

    // Spore Cauldrons
    // Order matters! Be careful.
    public static final Block DEAD_SPORE_CAULDRON = register("dead_spore_cauldron",
            new SporeCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON),
                    ModCauldronBehaviors.DEAD_SPORE_CAULDRON_BEHAVIOR, DeadSpores.getInstance()),
            false);
    public static final Block VERDANT_SPORE_CAULDRON = register("verdant_spore_cauldron",
            new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.VERDANT_SPORE_CAULDRON_BEHAVIOR,
                    VerdantSpores.getInstance()), false);
    public static final Block CRIMSON_SPORE_CAULDRON = register("crimson_spore_cauldron",
            new SporeCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.CRIMSON_SPORE_CAULDRON_BEHAVIOR,
                    CrimsonSpores.getInstance()), false);
    public static final Block ZEPHYR_SPORE_CAULDRON = register("zephyr_spore_cauldron",
            new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.ZEPHYR_SPORE_CAULDRON_BEHAVIOR,
                    ZephyrSpores.getInstance()), false);
    public static final Block SUNLIGHT_SPORE_CAULDRON = register("sunlight_spore_cauldron",
            new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.SUNLIGHT_SPORE_CAULDRON_BEHAVIOR,
                    SunlightSpores.getInstance()), false);
    public static final Block ROSEITE_SPORE_CAULDRON = register("roseite_spore_cauldron",
            new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.ROSEITE_SPORE_CAULDRON_BEHAVIOR,
                    RoseiteSpores.getInstance()), false);
    public static final Block MIDNIGHT_SPORE_CAULDRON = register("midnight_spore_cauldron",
            new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.MIDNIGHT_SPORE_CAULDRON_BEHAVIOR,
                    MidnightSpores.getInstance()), false);
    public static final Block ALUMINUM_CAULDRON = register("aluminum_cauldron",
            new CauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON)), true);
    public static final Block ALUMINUM_WATER_CAULDRON = register("aluminum_water_cauldron",
            new LeveledCauldronBlock(Biome.Precipitation.RAIN,
                    CauldronBehavior.WATER_CAULDRON_BEHAVIOR,
                    AbstractBlock.Settings.copy(Blocks.CAULDRON)), false);
    public static final Block ALUMINUM_LAVA_CAULDRON = register("aluminum_lava_cauldron",
            new LavaCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).luminance(state -> 15)), false);
    public static final Block ALUMINUM_POWDER_SNOW_CAULDRON = register(
            "aluminum_powder_snow_cauldron", new LeveledCauldronBlock(Biome.Precipitation.SNOW,
                    CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR,
                    AbstractBlock.Settings.copy(Blocks.CAULDRON)), false);
    public static final Block ALUMINUM_DEAD_SPORE_CAULDRON = register(
            "aluminum_dead_spore_cauldron",
            new SporeCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON),
                    ModCauldronBehaviors.DEAD_SPORE_CAULDRON_BEHAVIOR, DeadSpores.getInstance()),
            false);
    public static final Block ALUMINUM_VERDANT_SPORE_CAULDRON = register(
            "aluminum_verdant_spore_cauldron", new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.VERDANT_SPORE_CAULDRON_BEHAVIOR,
                    VerdantSpores.getInstance()), false);
    public static final Block ALUMINUM_CRIMSON_SPORE_CAULDRON = register(
            "aluminum_crimson_spore_cauldron",
            new SporeCauldronBlock(AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.CRIMSON_SPORE_CAULDRON_BEHAVIOR,
                    CrimsonSpores.getInstance()), false);
    public static final Block ALUMINUM_ZEPHYR_SPORE_CAULDRON = register(
            "aluminum_zephyr_spore_cauldron", new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.ZEPHYR_SPORE_CAULDRON_BEHAVIOR,
                    ZephyrSpores.getInstance()), false);
    public static final Block ALUMINUM_SUNLIGHT_SPORE_CAULDRON = register(
            "aluminum_sunlight_spore_cauldron", new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.SUNLIGHT_SPORE_CAULDRON_BEHAVIOR,
                    SunlightSpores.getInstance()), false);
    public static final Block ALUMINUM_ROSEITE_SPORE_CAULDRON = register(
            "aluminum_roseite_spore_cauldron", new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.ROSEITE_SPORE_CAULDRON_BEHAVIOR,
                    RoseiteSpores.getInstance()), false);
    public static final Block ALUMINUM_MIDNIGHT_SPORE_CAULDRON = register(
            "aluminum_midnight_spore_cauldron", new LivingSporeCauldronBlock(
                    AbstractBlock.Settings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.MIDNIGHT_SPORE_CAULDRON_BEHAVIOR,
                    MidnightSpores.getInstance()), false);

    // Other
    public static final Block MAGMA_VENT = register("magma_vent",
            new VentBlock(AbstractBlock.Settings.create()
                    // Same strength as Obsidian (should not be broken easily)
                    .strength(50.0F, 1200.0F).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY)),
            true);
    public static final Block SALTSTONE = register("saltstone",
            new Block(AbstractBlock.Settings.create().requiresTool()
                    // Slightly harder to break than Netherrack
                    .strength(0.5f, 6.0f).sounds(ModSoundGroups.SALTSTONE)), true);
    public static final Block SALTSTONE_SALT_ORE = register("saltstone_salt_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(0, 2),
                    AbstractBlock.Settings.create().requiresTool()
                            // Slightly easier to break than Nether Gold Ore
                            .strength(2.5f, 3.0f).sounds(ModSoundGroups.SALTSTONE)), true);
    public static final Block SALT_BLOCK = register("salt_block",
            new Block(AbstractBlock.Settings.create().requiresTool()
                    // Same strength as Calcite
                    .strength(0.75f).sounds(ModSoundGroups.SALT)), true);
    public static final Block SILVER_ORE = register("silver_ore",
            new ExperienceDroppingBlock(ConstantIntProvider.create(0),
                    AbstractBlock.Settings.create().requiresTool()
                            // Equal to Gold Ore
                            .strength(3.0f, 3.0f)), true);
    public static final Block DEEPSLATE_SILVER_ORE = register("deepslate_silver_ore",
            new ExperienceDroppingBlock(ConstantIntProvider.create(0),
                    AbstractBlock.Settings.create().requiresTool()
                            // Equal to Deepslate Gold Ore
                            .strength(4.5f, 3.0f).sounds(BlockSoundGroup.DEEPSLATE)), true);
    public static final Block SILVER_BLOCK = register("silver_block",
            new Block(AbstractBlock.Settings.create().requiresTool()
                    // Same strength as Gold Block
                    .strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL)), true);
    public static final Block RAW_SILVER_BLOCK = register("raw_silver_block",
            new Block(AbstractBlock.Settings.create().requiresTool()
                    // Same strength as Raw Gold Block
                    .strength(5.0f, 6.0f)), true);

    // Steel
    public static final Block STEEL_BLOCK = register("steel_block",
            new Block(AbstractBlock.Settings.create().requiresTool()
                    // +1 strength of Iron Block, same blast resistance as End Stone
                    .strength(6.0f, 9.0f).sounds(BlockSoundGroup.METAL)), true);
    public static final Block STEEL_ANVIL = register("steel_anvil",
            new SteelAnvilBlock(AbstractBlock.Settings.create()
                    .requiresTool()
                    // +1 strength of Iron Anvil, same blast resistance as Anvil
                    .strength(6.0f, 1200.0f)
                    .sounds(BlockSoundGroup.ANVIL)
                    .pistonBehavior(PistonBehavior.BLOCK)), true);
    public static final Block CHIPPED_STEEL_ANVIL = register("chipped_steel_anvil",
            new SteelAnvilBlock(AbstractBlock.Settings.create()
                    .requiresTool()
                    // Same settings as Steel Anvil
                    .strength(6.0f, 1200.0f)
                    .sounds(BlockSoundGroup.ANVIL)
                    .pistonBehavior(PistonBehavior.BLOCK)), true);
    public static final Block DAMAGED_STEEL_ANVIL = register("damaged_steel_anvil",
            new SteelAnvilBlock(AbstractBlock.Settings.create()
                    .requiresTool()
                    // Same settings as Steel Anvil
                    .strength(6.0f, 1200.0f)
                    .sounds(BlockSoundGroup.ANVIL)
                    .pistonBehavior(PistonBehavior.BLOCK)), true);

    // Aluminum
    public static final Block ALUMINUM_BLOCK = register("aluminum_block",
            new Block(AbstractBlock.Settings.create().requiresTool()
                    // Same strength as Gold Block
                    .strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL)), true);
    public static final Block ALUMINUM_SHEET = register("aluminum_sheet",
            new AluminumSheetBlock(AbstractBlock.Settings.create()
                    .requiresTool()
                    // Same strength as Aluminum Block
                    .strength(3.0f, 6.0f)
                    .solid()
                    .noCollision()
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .sounds(BlockSoundGroup.METAL)), true);

    public static <T extends Block> T register(String id, T block, boolean shouldRegisterItem) {
        Identifier blockId = Worldsinger.id(id);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, blockId, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockId, block);
    }

    private static AbstractBlock.Settings createSettingsWithCustomOffsetter(Offsetter offsetter) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create();
        ((CustomBlockOffsetterAccess) settings).worldsinger$setCustomOffsetter(offsetter);
        return settings;
    }

    private static AbstractBlock.Settings createSporeFluidSettings(boolean ticksRandomly,
            MapColor color) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .strength(100.0f)
                .replaceable()
                .noCollision()
                .dropsNothing()
                .liquid()
                .mapColor(color)
                .pistonBehavior(PistonBehavior.DESTROY)
                .sounds(BlockSoundGroup.SAND);
        if (ticksRandomly) {
            settings = settings.ticksRandomly();
        }
        return settings;
    }

    public static void initialize() {
    }

    private ModBlocks() {}
}
