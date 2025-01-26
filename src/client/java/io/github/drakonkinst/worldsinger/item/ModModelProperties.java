package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.select.SelectProperties;

public class ModModelProperties {

    public static void initialize() {
        NumericProperties.ID_MAPPER.put(Worldsinger.id("cannonball_fuse"),
                CannonballFuseProperty.CODEC);
        SelectProperties.ID_MAPPER.put(Worldsinger.id("cannonball_core"),
                CannonballCoreProperty.TYPE);
        SelectProperties.ID_MAPPER.put(Worldsinger.id("cannonball_content"),
                CannonballContentProperty.TYPE);
    }
}
