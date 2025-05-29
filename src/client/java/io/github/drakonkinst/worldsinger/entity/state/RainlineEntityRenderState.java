package io.github.drakonkinst.worldsinger.entity.state;

import java.util.Map;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class RainlineEntityRenderState extends EntityRenderState implements GeoRenderState {

    @Override
    public <D> void addGeckolibData(DataTicket<D> dataTicket, @Nullable D d) {

    }

    @Override
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return false;
    }

    @Override
    public <D> @Nullable D getGeckolibData(DataTicket<D> dataTicket) {
        return null;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return Map.of();
    }
}
