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
package io.github.drakonkinst.worldsinger.fluid;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.state.property.Property;

public class FluidProperty extends Property<Integer> {

    private final IntImmutableList values;
    private final int max;

    public static FluidProperty of(String name) {
        return new FluidProperty(name);
    }

    protected FluidProperty(String name) {
        super(name, Integer.class);
        this.max = Fluidlogged.WATERLOGGABLE_FLUIDS.size();
        if (max < 1) {
            throw new IllegalArgumentException("FluidProperty must support at least one fluid");
        }
        this.values = IntImmutableList.toList(IntStream.range(0, max + 1));
    }

    @Override
    public List<Integer> getValues() {
        return values;
    }

    @Override
    public String name(Integer value) {
        // TODO: Might need to offset this by one
        return Fluidlogged.WATERLOGGABLE_FLUIDS.get(value).toString();
    }

    @Override
    public Optional<Integer> parse(String name) {
        try {
            int i = Integer.parseInt(name);
            return i <= this.max ? Optional.of(i) : Optional.empty();
        } catch (NumberFormatException var3) {
            return Optional.empty();
        }
    }

    @Override
    public int ordinal(Integer value) {
        return value <= this.max ? value : -1;
    }

    @Override
    public int computeHashCode() {
        return 31 * super.computeHashCode() + this.values.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            if (object instanceof FluidProperty fluidProperty && super.equals(object)) {
                return this.values.equals(fluidProperty.values);
            }

            return false;
        }
    }
}
