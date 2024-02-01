package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.entity.ThrownSporeBottleEntity;
import io.github.drakonkinst.worldsinger.item.ModItems;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public final class ModDispenserBehaviors {

    private static final ItemDispenserBehavior DEFAULT_ITEM_BEHAVIOR = new ItemDispenserBehavior();
    private static final ItemDispenserBehavior FLUID_BUCKET_BEHAVIOR = new ItemDispenserBehavior() {

        @Override
        public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            FluidModificationItem fluidModificationItem = (FluidModificationItem) stack.getItem();
            BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
            ServerWorld world = pointer.world();
            if (fluidModificationItem.placeFluid(null, world, blockPos, null)) {
                fluidModificationItem.onEmptied(null, world, stack, blockPos);
                return new ItemStack(Items.BUCKET);
            }
            return DEFAULT_ITEM_BEHAVIOR.dispense(pointer, stack);
        }
    };
    private static final ProjectileDispenserBehavior SPLASH_POTION_BEHAVIOR = new ProjectileDispenserBehavior() {
        @Override
        protected ProjectileEntity createProjectile(World world, Position position,
                ItemStack stack) {
            return Util.make(new ThrownSporeBottleEntity(world, position.getX(), position.getY(),
                    position.getZ()), entity -> entity.setItem(stack));
        }

        @Override
        protected float getVariation() {
            return super.getVariation() * 0.5f;
        }

        @Override
        protected float getForce() {
            return super.getForce() * 1.25f;
        }
    };

    public static void register() {
        DispenserBlock.registerBehavior(ModItems.DEAD_SPORES_BUCKET, FLUID_BUCKET_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.VERDANT_SPORES_BUCKET, FLUID_BUCKET_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.CRIMSON_SPORES_BUCKET, FLUID_BUCKET_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.ZEPHYR_SPORES_BUCKET, FLUID_BUCKET_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.SUNLIGHT_SPORES_BUCKET, FLUID_BUCKET_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.ROSEITE_SPORES_BUCKET, FLUID_BUCKET_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.MIDNIGHT_SPORES_BUCKET, FLUID_BUCKET_BEHAVIOR);

        DispenserBlock.registerBehavior(ModItems.DEAD_SPORES_SPLASH_BOTTLE, SPLASH_POTION_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.VERDANT_SPORES_SPLASH_BOTTLE,
                SPLASH_POTION_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.CRIMSON_SPORES_SPLASH_BOTTLE,
                SPLASH_POTION_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE,
                SPLASH_POTION_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE,
                SPLASH_POTION_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.ROSEITE_SPORES_SPLASH_BOTTLE,
                SPLASH_POTION_BEHAVIOR);
        DispenserBlock.registerBehavior(ModItems.MIDNIGHT_SPORES_BOTTLE, SPLASH_POTION_BEHAVIOR);
    }

    private ModDispenserBehaviors() {}
}
