package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MidnightSporeGrowthEntity extends SporeGrowthEntity {

    private static final int COST_MIDNIGHT_ESSENCE = 30;
    private static final int GROWTH_DELAY = 4;

    public MidnightSporeGrowthEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected int getMaxStage() {
        return 1;
    }

    @Override
    protected BlockState getNextBlock() {
        return ModBlocks.MIDNIGHT_ESSENCE.getDefaultState();
    }

    @Override
    protected boolean canBreakHere(BlockState state) {
        return false;
    }

    @Override
    protected boolean canGrowHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_GROW);
    }

    @Override
    protected int getWeight(World world, BlockPos pos, Int3 direction, boolean allowPassthrough) {
        BlockState state = world.getBlockState(pos);
        int weight = -1;

        if (this.canGrowHere(state)) {
            weight = 100;
        } else if (allowPassthrough && this.isGrowthBlock(state)) {
            // If allowPassthrough is true, we assume that no actual block will be placed
            weight = 50;
        }

        if (weight < 0) {
            return 0;
        }

        // Strongly prefer to grow downwards
        weight -= 1000 * direction.y();

        // Bonus for moving towards origin, keeping it in a clump
        int bonusDistanceFromOrigin =
                this.getDistanceFromOrigin(this.getBlockPos()) - this.getDistanceFromOrigin(pos);
        weight += 50 * bonusDistanceFromOrigin;

        // Massive bonus for going along with external force
        double forceModifier = this.getExternalForceModifier(direction);
        weight += MathHelper.floor(FORCE_MODIFIER_MULTIPLIER * forceModifier);

        // Always have some weight, so it is an options if no other options are good
        weight = Math.max(1, weight);

        Worldsinger.LOGGER.info("DIR " + direction + " " + weight);

        return weight;
    }

    @Override
    protected boolean isGrowthBlock(BlockState state) {
        return state.isOf(ModBlocks.MIDNIGHT_ESSENCE);
    }

    @Override
    protected void onGrowBlock(BlockPos pos, BlockState state, BlockState originalState) {
        this.doGrowEffects(pos, state, COST_MIDNIGHT_ESSENCE, true, true, true);
        // TODO: Play particles?
        // TODO: Inflict Midnight Spores effect on things it passes through
        // Probably just explode into particles when you die like this
    }

    @Override
    protected void placeDecorator(BlockPos pos, Direction direction) {

    }

    @Override
    protected int getGrowthDelay() {
        return GROWTH_DELAY;
    }
}
