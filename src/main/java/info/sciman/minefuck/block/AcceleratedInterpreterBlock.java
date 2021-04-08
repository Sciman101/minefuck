package info.sciman.minefuck.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AcceleratedInterpreterBlock extends AbstractInterpreterBlock {

    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public AcceleratedInterpreterBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getStateManager().getDefaultState().with(POWERED,false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        super.appendProperties(stateManager);
        stateManager.add(POWERED);
    }

    @Override
    protected void onSidePowerChanged(BlockState state, World world, BlockPos pos, InterpreterBlockEntity be, int power) {
        world.setBlockState(pos, (BlockState) state.with(POWERED, power > 0), 3);
    }
}
