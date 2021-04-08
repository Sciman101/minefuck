package info.sciman.minefuck.mixin;

import info.sciman.minefuck.MinefuckMod;
import info.sciman.minefuck.block.AbstractInterpreterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.WritableBookItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WritableBookItem.class)
public class WritableBookItemMixin {
    @Inject(at = @At("HEAD"), method="useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> callback) {
        // Check for interpreter
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isOf(MinefuckMod.INTERPRETER_BLOCK)) {
            ActionResult result = AbstractInterpreterBlock.putBookIfAbsent(world, blockPos, blockState, context.getStack()) ? ActionResult.success(world.isClient) : ActionResult.PASS;
            callback.setReturnValue(result);
        }
    }
}
