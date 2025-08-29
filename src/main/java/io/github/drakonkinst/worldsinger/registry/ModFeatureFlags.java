package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureManager;

public final class ModFeatureFlags {

    private ModFeatureFlags() {}

    public static FeatureFlag ITEM_CONTAINERS;

    public static void initialize(FeatureManager.Builder builder) {
        ITEM_CONTAINERS = builder.addFlag(Worldsinger.id("item_containers"));
    }

    public static FeatureFlag[] getEnabledFeatures() {
        return new FeatureFlag[] {
                // ITEM_CONTAINERS,
        };
    }
}
