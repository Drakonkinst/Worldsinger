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
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;

public final class LivingEntityOverlay {

    private static final Random random = Random.create();

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

        // Determine dimensions of new image: it must be square and support animation frames
        int newImageWidth = image.getWidth();
        int newImageHeight = image.getHeight();
        int maxDimension = Math.max(newImageWidth, newImageHeight);
        newImageWidth = maxDimension;
        newImageHeight = maxDimension;
        // TODO: Use below texture (which is a GeckoLibAnimatedTexture) to animate the model
        AbstractTexture midnightEssenceTexture = textureManager.getTexture(
                MidnightCreatureEntityRenderer.TEXTURE);

        NativeImage midnightOverlayImage = new NativeImage(newImageWidth, newImageHeight, true);
        midnightOverlayImage.copyFrom(image);
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                // Overlay all non-transparent pixels
                if (isNotTransparent(image, x, y)) {
                    int randomGrayColor = random.nextBetween(0, 50);
                    midnightOverlayImage.setColorArgb(x, y,
                            ColorHelper.getArgb(randomGrayColor, randomGrayColor, randomGrayColor));
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
        return (image.getColorArgb(x, y) | 0x00ffffff) != 0x00ffffff;
    }

    private static NativeImage copyImage(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage newImage = new NativeImage(width, height, true);
        newImage.copyFrom(image);
        return newImage;
    }

    // Make sure to close it once you're done!
    private static NativeImage getOriginalImage(TextureManager textureManager,
            Identifier original) {
        NativeImage image;
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
