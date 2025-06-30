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
import io.github.drakonkinst.worldsinger.item.ItemOverlay;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.ModelSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public final class ModItemRendering {

    public static final Identifier CANNONBALL_CORE_ROSEITE = Worldsinger.id(
            "item/cannonball/core_roseite");
    public static final Identifier CANNONBALL_CORE_WATER = Worldsinger.id(
            "item/cannonball/core_water");
    public static final Identifier CANNONBALL_FUSE_1 = Worldsinger.id("item/cannonball/fuse_1");
    public static final Identifier CANNONBALL_FUSE_2 = Worldsinger.id("item/cannonball/fuse_2");
    public static final Identifier CANNONBALL_FUSE_3 = Worldsinger.id("item/cannonball/fuse_3");
    public static final Identifier BLANK = Worldsinger.id("item/blank");
    private static final float Z_FIGHTING_SCALE_MODIFIER = 0.001f;

    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> {
            for (ItemOverlay itemOverlay : ItemOverlay.VALUES) {
                registerItemOverlay(pluginContext, itemOverlay.getModelKey(), itemOverlay.getId());
            }
        });
    }

    private static void registerItemOverlay(ModelLoadingPlugin.Context pluginContext,
            ExtraModelKey<ItemModel> model, Identifier id) {
        pluginContext.addModel(model,
                new SimpleUnbakedExtraModel<>(id, ((bakedSimpleModel, baker) -> {
                    ModelTextures modelTextures = bakedSimpleModel.getTextures();
                    List<BakedQuad> list = bakedSimpleModel.bakeGeometry(modelTextures, baker,
                            ModelRotation.X0_Y0).getAllQuads();
                    // Modifies ModelSettings.resolveSettings()
                    // Sprite sprite = bakedSimpleModel.getParticleTexture(modelTextures, baker);
                    // ModelSettings modelSettings = new ModelSettings(
                    //         bakedSimpleModel.getGuiLight().isSide(), sprite,
                    //         fixZFighting(bakedSimpleModel.getTransformations()));
                    ModelSettings modelSettings = ModelSettings.resolveSettings(baker,
                            bakedSimpleModel, modelTextures);
                    return new BasicItemModel(Collections.emptyList(), list, modelSettings);
                })));
    }

    private static ModelTransformation fixZFighting(ModelTransformation model) {
        Transformation firstPersonLeftHand = fixZFighting(model.firstPersonLeftHand());
        Transformation firstPersonRightHand = fixZFighting(model.firstPersonRightHand());
        Transformation thirdPersonLeftHand = fixZFighting(model.thirdPersonLeftHand());
        Transformation thirdPersonRightHand = fixZFighting(model.thirdPersonRightHand());
        Transformation head = model.head();
        Transformation gui = model.gui();
        Transformation ground = model.ground();
        Transformation fixed = model.fixed();
        return new ModelTransformation(thirdPersonLeftHand, thirdPersonRightHand,
                firstPersonLeftHand, firstPersonRightHand, head, gui, ground, fixed);
    }

    private static Transformation fixZFighting(Transformation transformation) {
        Vector3f resultVector = new Vector3f();
        transformation.scale()
                .add(Z_FIGHTING_SCALE_MODIFIER, Z_FIGHTING_SCALE_MODIFIER,
                        Z_FIGHTING_SCALE_MODIFIER, resultVector);
        return new Transformation(transformation.rotation(), transformation.translation(),
                resultVector);
    }

    private ModItemRendering() {}
}
