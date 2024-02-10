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
package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModItemTags {

    public static final TagKey<Item> FLINT_AND_STEEL_VARIANTS = ModItemTags.of(
            "flint_and_steel_variants");
    public static final TagKey<Item> HAS_STEEL = ModItemTags.of("has_steel");
    public static final TagKey<Item> HAS_IRON = ModItemTags.of("has_iron");
    public static final TagKey<Item> BREWING_STAND_FUELS = ModItemTags.of("brewing_stand_fuels");
    public static final TagKey<Item> KILLS_SPORE_GROWTHS = ModItemTags.of("kills_spore_growths");
    public static final TagKey<Item> TEMPTS_MIDNIGHT_CREATURES = ModItemTags.of(
            "kills_spore_growths");

    public static final TagKey<Item> CAN_BE_SALTED = ModItemTags.ofCommon("can_be_salted");
    public static final TagKey<Item> EXCLUDE_SILVER_LINED = ModItemTags.ofCommon(
            "exclude_silver_lined");
    public static final TagKey<Item> SALT = ModItemTags.ofCommon("salt");
    public static final TagKey<Item> SILVER_INGOTS = ModItemTags.ofCommon("silver_ingots");
    public static final TagKey<Item> SHIELDS = ModItemTags.ofCommon("shields");
    public static final TagKey<Item> ALWAYS_GIVE_THIRST = ModItemTags.ofCommon(
            "always_give_thirst");
    public static final TagKey<Item> CHANCE_TO_GIVE_THIRST = ModItemTags.ofCommon(
            "chance_to_give_thirst");

    private static TagKey<Item> of(String id) {
        return TagKey.of(RegistryKeys.ITEM, Worldsinger.id(id));
    }

    private static TagKey<Item> ofCommon(String id) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(ModConstants.COMMON_ID, id));
    }

    private ModItemTags() {}
}
