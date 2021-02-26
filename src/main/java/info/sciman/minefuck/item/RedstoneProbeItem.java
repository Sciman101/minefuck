package info.sciman.minefuck.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class RedstoneProbeItem extends Item {

    // Uses a similar system to dispensers to determine what to output on blocks
    private static final Map<Block, ProbeBehaviour> BEHAVIORS;

    public static void registerBehavior(Block block, ProbeBehaviour behavior) {
        BEHAVIORS.put(block, behavior);
    }

    public RedstoneProbeItem(Settings settings) {
        super(settings);
    }

    /*
    When we right-click a block, if the probe can gleam any info from it,
    show that info to the player
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        PlayerEntity playerEntity = context.getPlayer();
        World world = context.getWorld();
        Text promptText = null;
        // Verify we're in a position to use this
        if (!world.isClient && playerEntity != null) {
            // Get block info and behaviour
            BlockPos pos = context.getBlockPos();
            BlockState state = context.getWorld().getBlockState(pos);
            ProbeBehaviour behaviour = BEHAVIORS.get(state.getBlock());

            boolean hasBehaviour = behaviour != null;
            boolean hasComparator = state.hasComparatorOutput();
            boolean sneaking = playerEntity.isSneaking();
            // Show probe behaviour if it exists, and we aren't sneaking and picking a block with comparator output
            // Show comparator output if it exists and we're sneaking, or there's no existing behaviour

            if (hasBehaviour && ((hasComparator ^ sneaking) || !hasComparator)) {
                // Get probe output
                promptText = behaviour.getProbeInfo(world,pos,state);
            }else if (hasComparator) {
                // Is this a comparator output block?
                int compStrength = state.getComparatorOutput(world,pos);
                promptText = new TranslatableText("probe.comparator_output",compStrength);
            }

            // Show message
            if (promptText != null) {
                sendMessage(playerEntity,promptText);
                return ActionResult.SUCCESS;
            }else{
                return super.useOnBlock(context);
            }
        }

        return ActionResult.success(world.isClient);
    }

    private static void sendMessage(PlayerEntity player, Text message) {
        ((ServerPlayerEntity)player).sendMessage(message, MessageType.GAME_INFO, Util.NIL_UUID);
    }

    static {
        BEHAVIORS = (Map) Util.make(new Object2ObjectOpenHashMap(), (object2ObjectOpenHashMap) -> {
            object2ObjectOpenHashMap.defaultReturnValue(null);
        });
    }
}
