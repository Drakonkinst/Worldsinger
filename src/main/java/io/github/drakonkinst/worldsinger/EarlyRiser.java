package io.github.drakonkinst.worldsinger;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class EarlyRiser implements Runnable {

    private static final String INTERMEDIARY = "intermediary";

    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String pathNodeTypeEnum = remapper.mapClassName(INTERMEDIARY, "net.minecraft.class_7");
        ClassTinkerers.enumBuilder(pathNodeTypeEnum, float.class).addEnum("AETHER_SPORE_SEA", -1.0f)
                .build();
    }
}