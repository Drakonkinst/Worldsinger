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

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.util.Identifier;

public final class ModLootTables {

    public static final Identifier LUMAR_SHIPWRECK_SPROUTER_CHEST = ModLootTables.of(
            "chests/lumar_shipwreck_sprouter");
    public static final Identifier LUMAR_SHIPWRECK_SUPPLY_CHEST = ModLootTables.of(
            "chests/lumar_shipwreck_supply");
    public static final Identifier LUMAR_SHIPWRECK_CAPTAIN_CHEST = ModLootTables.of(
            "chests/lumar_shipwreck_captain");
    public static final Identifier LUMAR_SALTSTONE_MINESHAFT_CHEST = ModLootTables.of(
            "chests/lumar_saltstone_mineshaft");

    private static Identifier of(String id) {
        return Worldsinger.id(id);
    }

    private ModLootTables() {}
}
