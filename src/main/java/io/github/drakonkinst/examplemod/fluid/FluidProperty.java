package io.github.drakonkinst.examplemod.fluid;

import net.minecraft.state.property.IntProperty;

public class FluidProperty extends IntProperty {

    protected FluidProperty(String name) {
        super(name, 0, Fluidlogged.WATERLOGGABLE_FLUIDS.size());
    }

    public static FluidProperty of(String name) {
        return new FluidProperty(name);
    }
}