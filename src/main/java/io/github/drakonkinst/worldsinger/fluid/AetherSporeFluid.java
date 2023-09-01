package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.block.AetherSporeFluidBlock;
import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.mixin.accessor.FlowableFluidInvoker;
import io.github.drakonkinst.worldsinger.world.lumar.AetherSporeType;
import io.github.drakonkinst.worldsinger.world.lumar.LumarSeethe;
import io.github.drakonkinst.worldsinger.world.lumar.SporeParticleManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AetherSporeFluid extends FlowableFluid implements SporeEmitting {

    public static final float FOG_START = 0.25f;
    public static final float FOG_END = 2.0f;
    public static final float HORIZONTAL_DRAG_MULTIPLIER = 0.7f;
    public static final float VERTICAL_DRAG_MULTIPLIER = 0.8f;

    // How fast this fluid pushes entities.
    // Water uses the value 0.014, and lava uses 0.007 in the Nether and 0.0023 otherwise
    public static final double FLUID_SPEED = 0.012;

    private final AetherSporeType aetherSporeType;
    private final float fogRed;
    private final float fogGreen;
    private final float fogBlue;

    public AetherSporeFluid(AetherSporeType aetherSporeType) {
        super();
        this.aetherSporeType = aetherSporeType;

        int color = aetherSporeType.getColor();
        this.fogRed = AetherSporeType.getNormalizedRed(color);
        this.fogGreen = AetherSporeType.getNormalizedGreen(color);
        this.fogBlue = AetherSporeType.getNormalizedBlue(color);
    }

    @Override
    public int getLevel(FluidState state) {
        return state.getLevel();
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    protected boolean isInfinite(World world) {
        return true;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        // Drop stacks of blocks when breaking them with fluid
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        if (!LumarSeethe.areSporesFluidized(world)) {
            return;
        }

        // Play particles and sounds during seething only
        BlockPos posAbove = pos.up();
        if (world.getBlockState(posAbove).isAir() && !world.getBlockState(posAbove)
                .isOpaqueFullCube(world, posAbove)) {
            if (random.nextInt(100) == 0) {
                double spawnX = (double) pos.getX() + random.nextDouble();
                double spawnY = (double) pos.getY() + 1.0 + random.nextDouble();
                double spawnZ = (double) pos.getZ() + random.nextDouble();
                SporeParticleManager.spawnDisplayParticles(world, aetherSporeType, spawnX,
                        spawnY, spawnZ, 1f);
            }
            if (random.nextInt(200) == 0) {
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_LAVA_AMBIENT,
                        SoundCategory.BLOCKS, 0.2f + random.nextFloat() * 0.2f,
                        0.9f + random.nextFloat() * 0.15f, false);
            }
        }
    }

    @Override
    public void onScheduledTick(World world, BlockPos pos, FluidState state) {
        if (this.isStill(state) && !AetherSporeFluidBlock.shouldFluidize(
                world.getBlockState(pos.down()))) {
            AetherSporeFluidBlock.updateFluidizationForBlock(world, pos, state.getBlockState(),
                    false);
        }
        super.onScheduledTick(world, pos, state);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState fluidState, BlockView blockView,
            BlockPos blockPos, Fluid fluid, Direction direction) {
        return fluidState.isIn(ModFluidTags.AETHER_SPORES) && !fluidState.isStill();
    }

    @Override
    protected FluidState getUpdatedState(World world, BlockPos pos, BlockState state) {
        int maxNeighboringFluidLevel = 0;
        int neighboringSourceBlocks = 0;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos currPos = pos.offset(direction);
            BlockState currBlockState = world.getBlockState(currPos);
            FluidState currFluidState = currBlockState.getFluidState();
            if (!(currFluidState.getFluid() instanceof AetherSporeFluid)
                    || !((FlowableFluidInvoker) this).worldsinger$receivesFlow(direction, world,
                    pos, state, currPos, currBlockState)) {
                continue;
            }
            if (currFluidState.isStill()) {
                ++neighboringSourceBlocks;
            }
            maxNeighboringFluidLevel = Math.max(maxNeighboringFluidLevel,
                    currFluidState.getLevel());
        }

        if (this.isInfinite(world) && neighboringSourceBlocks >= 2) {
            BlockState belowBlockState = world.getBlockState(pos.down());
            FluidState belowFluidState = belowBlockState.getFluidState();
            if (belowBlockState.isSolid()
                    || ((FlowableFluidInvoker) this).worldsinger$isMatchingAndStill(
                    belowFluidState)) {
                return this.getStill(false);
            }
        }

        BlockPos abovePos = pos.up();
        BlockState aboveBlockState = world.getBlockState(abovePos);
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        if (!aboveFluidState.isEmpty() && aboveFluidState.getFluid() instanceof AetherSporeFluid
                && ((FlowableFluidInvoker) this).worldsinger$receivesFlow(Direction.UP, world, pos,
                state, abovePos, aboveBlockState)) {
            return this.getFlowing(8, true);
        }
        int updatedFluidLevel = maxNeighboringFluidLevel - this.getLevelDecreasePerBlock(world);
        if (updatedFluidLevel <= 0) {
            return Fluids.EMPTY.getDefaultState();
        }
        return this.getFlowing(updatedFluidLevel, false);
    }

    @Override
    // This is used when the fluid "pathfinds" to see which way it should flow
    // Since it only flows as far as lava in the Overworld, match the flow speed
    protected int getFlowSpeed(WorldView worldView) {
        return 2;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView worldView) {
        return 2;
    }

    @Override
    public int getTickRate(WorldView worldView) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }

    public float getFogRed() {
        return fogRed;
    }

    public float getFogGreen() {
        return fogGreen;
    }

    public float getFogBlue() {
        return fogBlue;
    }

    public AetherSporeType getSporeType() {
        return aetherSporeType;
    }
}
