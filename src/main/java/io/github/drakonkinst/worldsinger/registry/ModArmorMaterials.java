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
package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.item.ModItems;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterial.Layer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModArmorMaterials {

    public static final int STEEL_DURABILITY_MULTIPLIER = 20;
    public static final RegistryEntry<ArmorMaterial> STEEL = ModArmorMaterials.register("steel",
            ModArmorMaterials.createMap(2, 5, 6, 2, 5), 11, ModSoundEvents.ITEM_ARMOR_EQUIP_STEEL,
            1.0f, 0.0f, () -> Ingredient.ofItems(ModItems.STEEL_INGOT));

    private static EnumMap<ArmorItem.Type, Integer> createMap(int boots, int leggings,
            int chestplate, int helmet, int body) {
        EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        map.put(ArmorItem.Type.BOOTS, boots);
        map.put(ArmorItem.Type.LEGGINGS, leggings);
        map.put(ArmorItem.Type.CHESTPLATE, chestplate);
        map.put(ArmorItem.Type.HELMET, helmet);
        map.put(ArmorItem.Type.BODY, body);
        return map;
    }

    private static RegistryEntry<ArmorMaterial> register(String id,
            EnumMap<ArmorItem.Type, Integer> defense, int enchantability,
            RegistryEntry<SoundEvent> equipSound, float toughness, float knockbackResistance,
            Supplier<Ingredient> repairIngredient, List<ArmorMaterial.Layer> layers) {
        EnumMap<ArmorItem.Type, Integer> enumMap = new EnumMap<>(ArmorItem.Type.class);

        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            enumMap.put(type, defense.get(type));
        }

        return Registry.registerReference(Registries.ARMOR_MATERIAL, new Identifier(id),
                new ArmorMaterial(enumMap, enchantability, equipSound, repairIngredient, layers,
                        toughness, knockbackResistance));
    }

    private static RegistryEntry<ArmorMaterial> register(String id,
            EnumMap<ArmorItem.Type, Integer> defense, int enchantability,
            RegistryEntry<SoundEvent> equipSound, float toughness, float knockbackResistance,
            Supplier<Ingredient> repairIngredient) {
        List<Layer> list = List.of(new ArmorMaterial.Layer(new Identifier(id)));
        return ModArmorMaterials.register(id, defense, enchantability, equipSound, toughness,
                knockbackResistance, repairIngredient, list);
    }

    private ModArmorMaterials() {}
}
