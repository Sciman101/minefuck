package info.sciman.minefuck.item;

import info.sciman.minefuck.BFSession;
import info.sciman.minefuck.MinefuckMod;
import info.sciman.minefuck.block.InterpreterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ProbeBehaviour {
    Text getProbeInfo(World world, BlockPos pos, BlockState state);

    static void registerDefaults() {
        // Redstone wire
        RedstoneProbeItem.registerBehavior(Blocks.REDSTONE_WIRE,(world,pos,state) -> {
            int pwr = state.get(RedstoneWireBlock.POWER);
            return new TranslatableText("probe.signal_power",pwr);
        });

        // Define shared behaviour for regular and accelerated interpreters
        ProbeBehaviour behaviour = (world, pos, state) -> {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof InterpreterBlockEntity) {
                InterpreterBlockEntity interpreter = (InterpreterBlockEntity) be;
                BFSession bf = interpreter.getBf();
                if (bf.checkError()) {
                    return new TranslatableText("probe.interpreter_bracket_error", bf.getError());
                }else{
                    return new TranslatableText("probe.interpreter_debug", bf.getPC(), bf.getPointer(), bf.getTapeValue(bf.getPointer()));
                }
            }else{
                return new LiteralText("???");
            }
        };

        // Brainfuck interpreter
        RedstoneProbeItem.registerBehavior(MinefuckMod.INTERPRETER_BLOCK,behaviour);
        RedstoneProbeItem.registerBehavior(MinefuckMod.ACCELERATED_INTERPRETER_BLOCK,behaviour);
    }
}
