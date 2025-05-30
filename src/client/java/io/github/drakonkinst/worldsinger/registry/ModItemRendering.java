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
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

public final class ModItemRendering {

    public static final Identifier SALT_OVERLAY = Worldsinger.id("item/salted_overlay");
    public static final Identifier SILVER_LINED_AXE_OVERLAY = Worldsinger.id(
            "item/silver_lined_axe_overlay");
    public static final Identifier SILVER_LINED_BOAT_OVERLAY = Worldsinger.id(
            "item/silver_lined_boat_overlay");
    public static final Identifier SILVER_LINED_CHEST_BOAT_OVERLAY = Worldsinger.id(
            "item/silver_lined_chest_boat_overlay");
    public static final Identifier SILVER_LINED_RAFT_OVERLAY = Worldsinger.id(
            "item/silver_lined_raft_overlay");
    public static final Identifier SILVER_LINED_CHEST_RAFT_OVERLAY = Worldsinger.id(
            "item/silver_lined_chest_raft_overlay");
    public static final Identifier CANNONBALL_CORE_ROSEITE = Worldsinger.id(
            "item/cannonball/core_roseite");
    public static final Identifier CANNONBALL_CORE_WATER = Worldsinger.id(
            "item/cannonball/core_water");
    public static final Identifier CANNONBALL_FUSE_1 = Worldsinger.id("item/cannonball/fuse_1");
    public static final Identifier CANNONBALL_FUSE_2 = Worldsinger.id("item/cannonball/fuse_2");
    public static final Identifier CANNONBALL_FUSE_3 = Worldsinger.id("item/cannonball/fuse_3");

    public static void register() {
        final Identifier[] overlayModels = new Identifier[] {
                ModItemRendering.SALT_OVERLAY,
                ModItemRendering.SILVER_LINED_AXE_OVERLAY,
                ModItemRendering.SILVER_LINED_BOAT_OVERLAY,
                ModItemRendering.SILVER_LINED_CHEST_BOAT_OVERLAY,
                ModItemRendering.SILVER_LINED_RAFT_OVERLAY,
                ModItemRendering.SILVER_LINED_CHEST_RAFT_OVERLAY,
                ModItemRendering.CANNONBALL_CORE_ROSEITE,
                ModItemRendering.CANNONBALL_CORE_WATER,
                ModItemRendering.CANNONBALL_FUSE_1,
                ModItemRendering.CANNONBALL_FUSE_2,
                ModItemRendering.CANNONBALL_FUSE_3,
        };
        ModelLoadingPlugin.register(pluginContext -> {
            // TODO: Restore
            // pluginContext.addModels(overlayModels);
            // pluginContext.modifyModelOnLoad()
            //         .register(ModelModifier.WRAP_PHASE, (model, context) -> {
            //             return model;
            //         });
        });
    }

    private ModItemRendering() {}
}
