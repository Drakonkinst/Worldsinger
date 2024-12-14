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

import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalItemTags;
import net.minecraft.item.ToolMaterial;

public final class ModToolMaterials {

    // Steel is identical to Iron, but has more durability and slightly more enchantability
    public static final ToolMaterial STEEL = new ToolMaterial(
            ModBlockTags.INCORRECT_FOR_SILVER_TOOL, 484, 6.5f, 2.0f, 16,
            ModConventionalItemTags.STEEL_TOOL_MATERIALS);
    // Silver is similar to Gold, but does not mine as fast and has more durability
    // Not planned to support a full toolset for silver
    public static final ToolMaterial SILVER = new ToolMaterial(
            ModBlockTags.INCORRECT_FOR_SILVER_TOOL, 181, 6.0F, 0.0F, 22,
            ModConventionalItemTags.SILVER_TOOL_MATERIALS);

    private ModToolMaterials() {}
}
