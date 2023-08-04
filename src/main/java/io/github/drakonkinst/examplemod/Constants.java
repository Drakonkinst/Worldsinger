package io.github.drakonkinst.examplemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {
    private Constants() {
    }

    public static final String MOD_ID = "examplemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int SECONDS_TO_TICKS = 20;
}
