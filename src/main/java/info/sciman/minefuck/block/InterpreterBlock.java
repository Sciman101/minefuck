package info.sciman.minefuck.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class InterpreterBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    public static final BooleanProperty HAS_BOOK = BooleanProperty.of("book");

    public InterpreterBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(HAS_BOOK,false));
    }

    // Insert or remove book
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if ((Boolean)state.get(HAS_BOOK)) {
            if (!world.isClient) {
                this.dropBook(state,world,pos);
            }
            return ActionResult.success(world.isClient);
        } else {
            ItemStack itemStack = player.getStackInHand(hand);
            if (!itemStack.isEmpty() && itemStack.getItem().isIn((Tag) ItemTags.LECTERN_BOOKS) && !world.isClient) {
                putBook(world,pos,state,itemStack);
                return ActionResult.CONSUME;
            }else {
                return ActionResult.PASS;
            }
        }
    }

    // Copied from lectern
    private static void putBook(World world, BlockPos pos, BlockState state, ItemStack book) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof InterpreterBlockEntity) {
            InterpreterBlockEntity blockEntity1 = (InterpreterBlockEntity)blockEntity;
            blockEntity1.setBook(book.split(1));
            setHasBook(world, pos, state, true);
            world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }
    public static void setHasBook(World world, BlockPos pos, BlockState state, boolean hasBook) {
        world.setBlockState(pos, (BlockState)((BlockState)state.with(HAS_BOOK, hasBook)), 3);
        updateNeighborAlways(world, pos, state);
    }
    private void dropBook(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof InterpreterBlockEntity) {
            InterpreterBlockEntity interpreterBlockEntity = (InterpreterBlockEntity)blockEntity;
            Direction direction = (Direction)state.get(FACING);
            ItemStack itemStack = interpreterBlockEntity.getBook().copy();
            float f = 0.25F * (float)direction.getOffsetX();
            float g = 0.25F * (float)direction.getOffsetZ();
            ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5D + (double)f, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5D + (double)g, itemStack);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
            interpreterBlockEntity.clear();
            setHasBook(world,pos,state,false);
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if ((Boolean)state.get(HAS_BOOK)) {
                this.dropBook(state, world, pos);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    private static void updateNeighborAlways(World world, BlockPos pos, BlockState state) {
        world.updateNeighborsAlways(pos.down(), state.getBlock());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(HAS_BOOK);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new InterpreterBlockEntity();
    }
}
