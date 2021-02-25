package info.sciman.minefuck;

import info.sciman.minefuck.block.InterpreterBlock;
import info.sciman.minefuck.block.InterpreterBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MinefuckMod implements ModInitializer {

	public static final String MODID = "minefuck";

	// Blocks
	private static final Block INTERPRETER_BLOCK = new InterpreterBlock(FabricBlockSettings.of(Material.STONE));
	public static BlockEntityType<InterpreterBlockEntity> INTERPRETER_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		// Setup interpreter block
		Identifier interpreterId = id("interpreter");
		INTERPRETER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,interpreterId,BlockEntityType.Builder.create(InterpreterBlockEntity::new,INTERPRETER_BLOCK).build(null));
		registerBlockAndItem(interpreterId,INTERPRETER_BLOCK,ItemGroup.REDSTONE);
	}

	private void registerBlockAndItem(Identifier id, Block block, ItemGroup group) {
		Registry.register(Registry.BLOCK, id, block);
		Registry.register(Registry.ITEM, id, new BlockItem(block,new Item.Settings().group(group)));
	}

	// Helper to create an identifier
	public static Identifier id(String path) {
		return new Identifier(MODID,path);
	}


}
