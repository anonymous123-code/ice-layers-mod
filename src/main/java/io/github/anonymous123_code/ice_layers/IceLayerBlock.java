package io.github.anonymous123_code.ice_layers;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class IceLayerBlock extends TransparentBlock {
	public static final int MAX_LAYERS = 8;
	public static final IntProperty LAYERS = Properties.LAYERS;
	protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{
		VoxelShapes.empty(),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
	};
	public IceLayerBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(LAYERS, 1));
	}

	@Override
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return stateFrom.isOf(this) ? stateFrom.get(LAYERS) >= state.get(LAYERS) : super.isSideInvisible(state, stateFrom, direction);
	}

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        if (Objects.requireNonNull(type) == NavigationType.LAND) {
            return state.get(LAYERS) < 4;
        }
        return false;
    }

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return LAYERS_TO_SHAPE[state.get(LAYERS)];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return LAYERS_TO_SHAPE[state.get(LAYERS)];
	}

	@Override
	public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
		return LAYERS_TO_SHAPE[state.get(LAYERS)];
	}

	@Override
	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return LAYERS_TO_SHAPE[state.get(LAYERS)];
	}


	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}



	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos.down());
		if (blockState.isIn(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
			return false;
		} else if (blockState.isIn(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
			return true;
		} else {
			return Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP) || blockState.isOf(this) && blockState.get(LAYERS) == MAX_LAYERS;
		}
	}

	@Override
	public BlockState getStateForNeighborUpdate(
		BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
	) {
		return !state.canPlaceAt(world, pos)
			? Blocks.AIR.getDefaultState()
			: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		if (world.getLightLevel(LightType.BLOCK, pos) > 11 - state.getOpacity(world, pos)) {
			this.melt(state, world, pos);
		}
	}

	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext context) {
		int i = state.get(LAYERS);
		if (!context.getStack().isOf(this.asItem()) || i >= MAX_LAYERS) {
			return i == 1;
		} else if (context.canReplaceExisting()) {
			return context.getSide() == Direction.UP;
		} else {
			return true;
		}
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
		if (blockState.isOf(this)) {
			int i = blockState.get(LAYERS);
			return blockState.with(LAYERS, Math.min(MAX_LAYERS, i + 1));
		} else {
			return super.getPlacementState(ctx);
		}
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LAYERS);
	}

	public static BlockState getWaterState() {
		return Blocks.WATER.getDefaultState();
	}

	@Override
	public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
		super.afterBreak(world, player, pos, state, blockEntity, stack);
		if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0) {
			if (world.getDimension().ultraWarm()) {
				world.removeBlock(pos, false);
				return;
			}

			BlockState blockState = world.getBlockState(pos.down());
			if (blockState.blocksMovement() || blockState.isLiquid()) {
				world.setBlockState(pos, getWaterState());
			}
		}
	}


	protected void melt(BlockState state, World world, BlockPos pos) {
		if (world.getDimension().ultraWarm()) {
			world.removeBlock(pos, false);
		} else {
			world.setBlockState(pos, getWaterState());
			world.updateNeighbor(pos, getWaterState().getBlock(), pos);
		}
	}
}
