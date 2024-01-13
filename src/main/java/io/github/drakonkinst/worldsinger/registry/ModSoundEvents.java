package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public final class ModSoundEvents {

    // TODO Replace with original sound effects
    public static final SoundEvent ITEM_BUCKET_FILL_AETHER_SPORE = SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW;
    public static final SoundEvent ITEM_BUCKET_EMPTY_AETHER_SPORE = SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW;
    public static final SoundEvent ITEM_BOTTLE_FILL_AETHER_SPORE = SoundEvents.ITEM_BOTTLE_FILL;
    public static final SoundEvent ITEM_ARMOR_EQUIP_STEEL = SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    public static final SoundEvent BLOCK_SPORE_SEA_AMBIENT = SoundEvents.BLOCK_LAVA_AMBIENT;
    public static final SoundEvent BLOCK_SPORE_BLOCK_PLACE = SoundEvents.BLOCK_POWDER_SNOW_PLACE;
    public static final SoundEvent BLOCK_SUNLIGHT_EVAPORATE = SoundEvents.BLOCK_FIRE_EXTINGUISH;
    public static final SoundEvent BLOCK_SUNLIGHT_SPORE_BLOCK_CATALYZE = SoundEvents.ITEM_FIRECHARGE_USE;
    public static final SoundEvent BLOCK_ZEPHYR_SEA_CATALYZE = SoundEvents.ENTITY_GENERIC_EXPLODE;
    public static final SoundEvent ENTITY_BOAT_PADDLE_SPORE_SEA = SoundEvents.BLOCK_SAND_BREAK;
    public static final SoundEvent ENTITY_BOAT_LINE_SILVER = SoundEvents.ENTITY_IRON_GOLEM_REPAIR;
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_AMBIENT = SoundEvents.ENTITY_BREEZE_IDLE_GROUND;
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_HURT = SoundEvents.ENTITY_BREEZE_HURT;
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_DEATH = SoundEvents.ENTITY_BREEZE_DEATH;
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_DRINK = SoundEvents.ENTITY_GENERIC_DRINK;
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_BOND = SoundEvents.ENTITY_BREEZE_LAND;
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_TRANSFORM = SoundEvents.ENTITY_BREEZE_SLIDE;
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_BOND_BREAK = SoundEvents.ENTITY_BREEZE_JUMP;    // Play at ~0.5 pitch
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_POSSESS = SoundEvents.ENTITY_WITHER_SHOOT;    // Play at ~0.5 pitch
    public static final SoundEvent ENTITY_MIDNIGHT_CREATURE_STEP = SoundEvents.ENTITY_ZOMBIE_STEP;    // Play at ~0.5 pitch

    public static void initialize() {}

    // There are different SoundEvent.of() methods so make sure to add new methods to support the right one
    private static SoundEvent register(String path) {
        Identifier id = Worldsinger.id(path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    private ModSoundEvents() {}

}
