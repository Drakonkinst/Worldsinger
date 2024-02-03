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
package io.github.drakonkinst.worldsinger.worldgen.structure;

import java.util.Locale;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;

public final class ModStructurePieceTypes {

    public static void initialize() {}

    public static StructurePieceType register(StructurePieceType type, String id) {
        return Registry.register(Registries.STRUCTURE_PIECE, id.toLowerCase(Locale.ROOT), type);
    }

    private ModStructurePieceTypes() {}

    public static final StructurePieceType CUSTOM_MINESHAFT_CORRIDOR = ModStructurePieceTypes.register(
            CustomMineshaftGenerator.MineshaftCorridor::new, "CMSCorridor");

    public static final StructurePieceType CUSTOM_MINESHAFT_CROSSING = ModStructurePieceTypes.register(
            CustomMineshaftGenerator.MineshaftCrossing::new, "CMSCrossing");

    public static final StructurePieceType CUSTOM_MINESHAFT_ROOM = ModStructurePieceTypes.register(
            CustomMineshaftGenerator.MineshaftRoom::new, "CMSRoom");
    public static final StructurePieceType CUSTOM_MINESHAFT_STAIRS = ModStructurePieceTypes.register(
            CustomMineshaftGenerator.MineshaftStairs::new, "CMSStairs");

}
