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

package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent.Decoration;
import java.util.Map;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class NullRainlineSpawner implements RainlineSpawner {

    @Override
    public void serverTick(ServerWorld world) {
        // Do nothing
    }

    @Override
    public int applyMapDecorations(ServerWorld world, Map<String, Decoration> decorations,
            MapState mapState) {
        return 0;
    }

    @Override
    public @Nullable RainlinePath getRainlinePathById(long id) {
        return null;
    }
}
