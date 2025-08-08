package io.github.drakonkinst.worldsinger.entity.render;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.render.state.ExtendedLivingEntityRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

public final class LivingEntityOverlay {

    // We can use this class to overwrite the texture of living entities and dynamically create new ones.
    public static Identifier applyLivingEntityOverlays(Identifier original,
            LivingEntityRenderState state) {
        boolean hasMidnightOverlay = ((ExtendedLivingEntityRenderState) state).worldsinger$hasMidnightOverlay();
        if (hasMidnightOverlay) {
            return attemptCreateMidnightEntityOverlay(original);
        }
        return original;
    }

    private static Identifier attemptCreateMidnightEntityOverlay(Identifier original) {
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
        Identifier midnightOverlay = Identifier.of(original.getNamespace(),
                original.getPath() + "_midnight_overlay");

        if (textureManager.getTexture(midnightOverlay) instanceof NativeImageBackedTexture) {
            return midnightOverlay;
        }

        NativeImage image = getOriginalImage(textureManager, original);
        if (image == null) {
            return original;
        }

        NativeImage midnightOverlayImage = copyImage(image);
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                // Overlay all non-transparent pixels
                if (isNotTransparent(image, x, y)) {
                    midnightOverlayImage.setColorArgb(x, y, 0xff000000);
                }
            }
        }
        image.close();

        NativeImageBackedTexture newTexture = new NativeImageBackedTexture(
                midnightOverlay::toString, midnightOverlayImage);
        textureManager.registerTexture(midnightOverlay, newTexture);
        return midnightOverlay;
    }

    private static boolean isNotTransparent(NativeImage image, int x, int y) {
        return image.getColorArgb(x, y) != 0x00000000;
    }

    private static NativeImage copyImage(NativeImage image) {
        NativeImage newImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        newImage.copyFrom(image);
        return newImage;
    }

    // Make sure to close it once you're done!
    private static NativeImage getOriginalImage(TextureManager textureManager,
            Identifier original) {
        NativeImage image = null;
        AbstractTexture originalTexture;
        try {
            originalTexture = textureManager.getTexture(original);
            if (originalTexture instanceof ResourceTexture resourceTexture) {
                image = resourceTexture.loadContents(
                        MinecraftClient.getInstance().getResourceManager()).image();
            } else if (originalTexture instanceof NativeImageBackedTexture nativeImageBackedTexture) {
                image = nativeImageBackedTexture.getImage();
            } else {
                Worldsinger.LOGGER.debug("Unknown originalTexture type for {}", original);
                return null;
            }
        } catch (Exception e) {
            Worldsinger.LOGGER.info("Encountered error while retrieving original texture");
            return null;
        }
        return image;
    }
}
