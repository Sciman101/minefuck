package info.sciman.minefuck.block;

import info.sciman.minefuck.MinefuckMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class InterpreterBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
    public static final BooleanProperty HAS_BOOK = BooleanProperty.of("book");
    public static final BooleanProperty ERROR = BooleanProperty.of("error");


    public InterpreterBlock(Settings settings) {

        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(HAS_BOOK,false).with(ERROR,false));
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        // If we have an error, smoke
        if (state.get(ERROR)) {
            double x, y, z;
            for (int i = 0; i < 3; ++i) {
                x = (double) pos.getX() + random.nextDouble();
                y = (double) pos.getY() + random.nextDouble() * 0.375D;
                z = (double) pos.getZ() + random.nextDouble();
                world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    // Comparator handling
    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }
    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (state.get(HAS_BOOK)){
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof InterpreterBlockEntity) {
                return ((InterpreterBlockEntity) be).getBf().available() ? 0 : 15;
            }
        }
        return 0;
    }

    // Redstone handling
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        // Get redstone output
        if (direction == state.get(FACING)) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof InterpreterBlockEntity) {
                return ((InterpreterBlockEntity) be).getOutputLevel();
            }
        }
        return 0;
    }

    // Apply redstone signal
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        // Check direction
        Direction dir = state.get(FACING);
        BlockEntity be = world.getBlockEntity(pos);
        // Make sure this is the right thing
        if (be instanceof InterpreterBlockEntity) {
            // Get interpreter entity
            InterpreterBlockEntity interpreterBlockEntity = (InterpreterBlockEntity) be;

            // Completely ignore any neighbor updates from in front of the interpreter. We don't accept inputs from here
            if (!pos.offset(dir.getOpposite()).equals(fromPos)) {
                // Are we receiving power from the side?
                if (world.isReceivingRedstonePower(pos.offset(dir.rotateYClockwise())) || world.isReceivingRedstonePower(pos.offset(dir.rotateYCounterclockwise()))) {
                    // Do we have a book?
                    if (state.get(HAS_BOOK) && !interpreterBlockEntity.pulsed) {
                        // If we aren't powered from the bottom, step the block entity

                        // Step the blockentity
                        boolean didOutput = interpreterBlockEntity.step();

                        // Update neighbor
                        Direction direction = (Direction) state.get(FACING);
                        BlockPos blockPos = pos.offset(direction.getOpposite());
                        world.updateNeighbor(blockPos, this, pos);
                        interpreterBlockEntity.pulsed = true;

                        // Try and output to sign
                        if (didOutput) {
                        }

                    } else {
                        // Disable the TRIGGERED state
                        interpreterBlockEntity.pulsed = false;
                    }
                    interpreterBlockEntity.markDirty();
                }

                // Update input value from the back
                int str = world.getReceivedStrongRedstonePower(pos.offset(dir));
                // Feed input to block entity
                interpreterBlockEntity.setInputLevel(str);
            }
        }
    }

    // Insert or remove book
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // Ignore probe
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() == MinefuckMod.REDSTONE_PROBE) {
            return ActionResult.PASS;
        }
        // Drop our book if we have one
        if ((Boolean)state.get(HAS_BOOK)) {
            if (!world.isClient) {
                this.dropBook(state,world,pos);
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    // Copied from lectern lol
    private static void putBook(World world, BlockPos pos, BlockState state, ItemStack book) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof InterpreterBlockEntity) {
            InterpreterBlockEntity blockEntity1 = (InterpreterBlockEntity)blockEntity;
            blockEntity1.setBook(book.split(1));
            setHasBook(world, pos, state, true,blockEntity1.getBf().checkError());
            world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }
    public static boolean putBookIfAbsent(World world, BlockPos pos, BlockState state, ItemStack book) {
        if (!(Boolean)state.get(HAS_BOOK)) {
            if (!world.isClient) {
                putBook(world, pos, state, book);
            }
            return true;
        } else {
            return false;
        }
    }
    public static void setHasBook(World world, BlockPos pos, BlockState state, boolean hasBook, boolean error) {
        if (world.getBlockState(pos).getBlock() == MinefuckMod.INTERPRETER_BLOCK) {
            world.setBlockState(pos, (BlockState) ((BlockState) state.with(HAS_BOOK, hasBook).with(ERROR,error)), 3);
            updateNeighborAlways(world, pos, state);
        }
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
            setHasBook(world,pos,state,false,false);
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
        stateManager.add(FACING);
        stateManager.add(HAS_BOOK);
        stateManager.add(ERROR);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new InterpreterBlockEntity();
    }
}
