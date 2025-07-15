package io.github.drakonkinst.worldsinger.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.advancement.ModCriteria;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.component.type.MapDecorationsComponent.Decoration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EmptyMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmptyMapItem.class)
public class EmptyMapItemMixin {

    // Currently this only handles map icons that appear on creation
    // We only need it for the rainline advancement, so that'll do for now and it can be extended later
    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FilledMapItem;createMap(Lnet/minecraft/server/world/ServerWorld;IIBZZ)Lnet/minecraft/item/ItemStack;"))
    private ItemStack addFindMapIconTrigger(ItemStack original, World world, PlayerEntity user,
            Hand hand) {
        if (!(user instanceof ServerPlayerEntity player)) {
            return original;
        }

        MapDecorationsComponent decorations = original.get(DataComponentTypes.MAP_DECORATIONS);
        if (decorations == null) {
            return original;
        }

        Set<RegistryKey<MapDecorationType>> seenMapDecorations = new HashSet<>();
        for (Decoration decoration : decorations.decorations().values()) {
            decoration.type().getKey().ifPresent(seenMapDecorations::add);
        }
        ModCriteria.FIND_ICON_ON_MAP.trigger(player, seenMapDecorations);
        return original;
    }
}
