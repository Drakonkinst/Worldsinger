package io.github.drakonkinst.worldsinger.item;

import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.select.SelectProperties;

public class ModModelProperties {

    public static void initialize() {
        NumericProperties.ID_MAPPER.put(CannonballFuseProperty.ID, CannonballFuseProperty.CODEC);
        SelectProperties.ID_MAPPER.put(CannonballCoreProperty.ID, CannonballCoreProperty.TYPE);
        SelectProperties.ID_MAPPER.put(CannonballContentProperty.ID,
                CannonballContentProperty.TYPE);
    }
}
