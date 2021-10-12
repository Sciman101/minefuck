package info.sciman.minefuck.block;

import info.sciman.minefuck.BFSession;
import info.sciman.minefuck.SingleSlotInventory;
import info.sciman.minefuck.MinefuckMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class InterpreterBlockEntity extends BlockEntity implements SingleSlotInventory, SidedInventory {

    // The book we're sampling code from
    private ItemStack book;

    // Our brainfuck session
    private BFSession bf;

    public boolean pulsed;

    public InterpreterBlockEntity(BlockPos pos, BlockState state) {
        super(MinefuckMod.INTERPRETER_BLOCK_ENTITY, pos, state);
        this.book = ItemStack.EMPTY;
        bf = new BFSession();
    }


    // Step BF session
    public boolean step() {
        if (!world.isClient) {
            if (bf.available()) {
                boolean didOutput = bf.step();
                this.markDirty();
                return didOutput;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public void setInputLevel(int str) {
        bf.setInputLevel(str);
    }

    public int getOutputLevel() {
        return bf.getOutputLevel();
    }

    public BFSession getBf() {
        return bf;
    }

    public ItemStack getBook() {
        return this.book;
    }

    public boolean hasBook() {
        Item item = this.book.getItem();
        return item == Items.WRITABLE_BOOK || item == Items.WRITTEN_BOOK;
    }

    public void setBook(ItemStack book) {
        this.setBook(book, (PlayerEntity)null);
    }

    private void onBookRemoved() {
        InterpreterBlock.setHasBook(this.getWorld(), this.getPos(), this.getCachedState(), false,false);
    }

    public void setBook(ItemStack book, @Nullable PlayerEntity player) {
        this.book = this.resolveBook(book, player);
        this.bf.load(book);
        this.markDirty();
    }

    private ItemStack resolveBook(ItemStack book, @Nullable PlayerEntity player) {
        if (this.world instanceof ServerWorld && book.getItem() == Items.WRITTEN_BOOK) {
            WrittenBookItem.resolve(book, this.getCommandSource(player), player);
        }

        return book;
    }

    private ServerCommandSource getCommandSource(@Nullable PlayerEntity player) {
        String string2;
        Object text2;
        if (player == null) {
            string2 = "Lectern";
            text2 = new LiteralText("Lectern");
        } else {
            string2 = player.getName().getString();
            text2 = player.getDisplayName();
        }

        Vec3d vec3d = Vec3d.ofCenter(this.pos);
        return new ServerCommandSource(CommandOutput.DUMMY, vec3d, Vec2f.ZERO, (ServerWorld)this.world, 2, string2, (Text)text2, this.world.getServer(), player);
    }

    public void clear() {
        this.setBook(ItemStack.EMPTY);
    }

    public void readNbt(BlockState state, NbtCompound tag) {
        super.readNbt(tag);
        bf.fromTag(tag);
        tag.putBoolean("pulsed",pulsed);
        if (tag.contains("Book", 10)) {
            this.book = this.resolveBook(ItemStack.fromNbt(tag.getCompound("Book")), (PlayerEntity)null);
        } else {
            this.book = ItemStack.EMPTY;
        }
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (!this.getBook().isEmpty()) {
            tag.put("Book", this.getBook().writeNbt(new NbtCompound()));
        }
        bf.toTag(tag);
        pulsed = tag.getBoolean("pulsed");
        return tag;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return SingleSlotInventory.of(book).getItems();
    }
    @Override
    public int size() {
        return 1;
    }
    @Override
    public boolean isEmpty() {
        return InterpreterBlockEntity.this.book.isEmpty();
    }
    @Override
    public ItemStack getStack(int slot) {
        return slot == 0 ? InterpreterBlockEntity.this.book : ItemStack.EMPTY;
    }
    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot == 0) {
            ItemStack itemStack = InterpreterBlockEntity.this.book.split(amount);
            if (InterpreterBlockEntity.this.book.isEmpty()) {
                InterpreterBlockEntity.this.onBookRemoved();
            }

            return itemStack;
        } else {
            return ItemStack.EMPTY;
        }
    }
    @Override
    public ItemStack removeStack(int slot) {
        if (slot == 0) {
            ItemStack itemStack = InterpreterBlockEntity.this.book;
            InterpreterBlockEntity.this.book = ItemStack.EMPTY;
            InterpreterBlockEntity.this.onBookRemoved();
            return itemStack;
        } else {
            return ItemStack.EMPTY;
        }
    }
    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            setBook(stack);
            InterpreterBlock.setHasBook(world, pos, getCachedState(), hasBook(),getBf().checkError());
        }
    }
    @Override
    public int getMaxCountPerStack() {
        return 1;
    }
    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (InterpreterBlockEntity.this.world.getBlockEntity(InterpreterBlockEntity.this.pos) != InterpreterBlockEntity.this) {
            return false;
        } else {
            return !(player.squaredDistanceTo((double) InterpreterBlockEntity.this.pos.getX() + 0.5D, (double) InterpreterBlockEntity.this.pos.getY() + 0.5D, (double) InterpreterBlockEntity.this.pos.getZ() + 0.5D) > 64.0D) && InterpreterBlockEntity.this.hasBook();
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[1];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.getItem() == Items.WRITTEN_BOOK || stack.getItem() == Items.WRITABLE_BOOK;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
