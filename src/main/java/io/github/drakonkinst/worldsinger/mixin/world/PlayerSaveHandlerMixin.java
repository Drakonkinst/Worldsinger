package io.github.drakonkinst.worldsinger.mixin.world;

import io.github.drakonkinst.worldsinger.world.ExtendedPlayerSaveHandler;
import java.io.File;
import java.util.UUID;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerSaveHandler.class)
public abstract class PlayerSaveHandlerMixin implements ExtendedPlayerSaveHandler {

    @Shadow
    @Final
    private File playerDataDir;

    @Override
    public boolean worldsinger$existsInSaveData(UUID uuid) {
        String uuidStr = uuid.toString();
        File normalDataFile = new File(this.playerDataDir, uuidStr + ".dat");
        if (normalDataFile.exists() && normalDataFile.isFile()) {
            return true;
        }
        File oldDataFile = new File(this.playerDataDir, uuidStr + ".dat_old");
        return oldDataFile.exists() && oldDataFile.isFile();
    }
}
