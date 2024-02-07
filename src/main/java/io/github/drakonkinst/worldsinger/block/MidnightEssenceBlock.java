package io.github.drakonkinst.worldsinger.block;

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.ThirstManager;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MidnightEssenceBlock extends Block {

    private static final int WATER_COST = 4;

    public MidnightEssenceBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(Properties.PERSISTENT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.PERSISTENT);
        super.appendProperties(builder);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState != null) {
            placementState = placementState.with(Properties.PERSISTENT, true);
        }
        return placementState;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(Properties.PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        // Decay over time
        if (random.nextInt(5) == 0) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
            BlockHitResult hit) {
        ThirstManager thirstManager = player.getAttachedOrCreate(ModAttachmentTypes.THIRST);
        if (thirstManager.get() < WATER_COST) {
            // Not enough water to summon anything, but should still swing hand
            return ActionResult.success(true);
        }
        thirstManager.remove(WATER_COST);
        world.removeBlock(pos, false);
        MidnightCreatureEntity entity = new MidnightCreatureEntity(world);
        entity.setMidnightEssenceAmount(1);
        entity.setPosition(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
        entity.setController(player);
        entity.acceptWaterBribe(player, MidnightCreatureEntity.INITIAL_BRIBE);
        world.spawnEntity(entity);
        world.playSound(null, pos, ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_AMBIENT,
                SoundCategory.BLOCKS, 1.0f, 1.0f);
        return ActionResult.success(true);
    }
}
