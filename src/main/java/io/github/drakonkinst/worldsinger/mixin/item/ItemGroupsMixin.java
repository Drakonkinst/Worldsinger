package io.github.drakonkinst.worldsinger.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.registry.ModPotions;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.item.ItemGroups;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {

    @WrapOperation(method = "addPotions", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private static Stream<RegistryEntry<Potion>> removeModdedPotionItems(
            Stream<RegistryEntry<Potion>> instance,
            Predicate<? super RegistryEntry<Potion>> predicate,
            Operation<Stream<RegistryEntry<Potion>>> original) {
        return original.call(instance, predicate)
                .filter(potion -> !ModPotions.EXCLUDE_ITEMS.contains(potion));
    }
}
