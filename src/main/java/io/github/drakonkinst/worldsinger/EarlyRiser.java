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
package io.github.drakonkinst.worldsinger;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class EarlyRiser implements Runnable {

    private static final String INTERMEDIARY = "intermediary";

    private static void loadClient(MappingResolver remapper) {
        String skyTypeEnum = remapper.mapClassName(INTERMEDIARY,
                "net.minecraft.class_5294$class_5401");
        ClassTinkerers.enumBuilder(skyTypeEnum).addEnum("LUMAR").build();

        String cameraSubmersionTypeEnum = remapper.mapClassName(INTERMEDIARY,
                "net.minecraft.class_5636");
        ClassTinkerers.enumBuilder(cameraSubmersionTypeEnum)
                .addEnum("SPORE_SEA")
                .addEnum("SUNLIGHT")
                .build();
    }

    private static void loadServer(MappingResolver remapper) {
        // Nothing yet
    }

    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String pathNodeTypeEnum = remapper.mapClassName(INTERMEDIARY, "net.minecraft.class_7");
        ClassTinkerers.enumBuilder(pathNodeTypeEnum, float.class)
                .addEnum("AETHER_SPORE_SEA", -1.0f)
                .addEnum("BLOCKING_SILVER", -1.0f)
                .addEnum("DANGER_SILVER", 0.0f) // Should be 8.0f for mobs scared of silver
                .addEnum("DAMAGE_SILVER", 0.0f) // Should be -1.0f for mobs scared of silver
                .build();

        switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> EarlyRiser.loadClient(remapper);
            case SERVER -> EarlyRiser.loadServer(remapper);
        }
    }
}
