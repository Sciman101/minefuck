package info.sciman.minefuck.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class InterpreterBlock extends AbstractInterpreterBlock {

    public InterpreterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void onSidePowerChanged(BlockState state, World world, BlockPos pos, InterpreterBlockEntity interpreterBlockEntity, int power) {
        if (power > 0) {
            // Do we have a book?
            if (state.get(HAS_BOOK) && !interpreterBlockEntity.pulsed) {
                // Step the blockentity
                boolean didOutput = interpreterBlockEntity.step();

                // Update neighbor
                Direction direction = (Direction) state.get(FACING);
                BlockPos blockPos = pos.offset(direction.getOpposite());
                world.updateNeighbor(blockPos, this, pos);
                interpreterBlockEntity.pulsed = true;

            } else {
                // Disable the TRIGGERED state
                interpreterBlockEntity.pulsed = false;
            }
            interpreterBlockEntity.markDirty();
        }
    }


}
