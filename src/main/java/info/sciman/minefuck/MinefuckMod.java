package info.sciman.minefuck;

import info.sciman.minefuck.block.InterpreterBlock;
import info.sciman.minefuck.block.InterpreterBlockEntity;
import info.sciman.minefuck.item.ProbeBehaviour;
import info.sciman.minefuck.item.RedstoneProbeItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
	public static final Block INTERPRETER_BLOCK = new InterpreterBlock(FabricBlockSettings.of(Material.STONE).hardness(0.2f));
	public static final BlockEntityType<InterpreterBlockEntity> INTERPRETER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(InterpreterBlockEntity::new, INTERPRETER_BLOCK).build(null);

	// Items
	public static final Item REDSTONE_PROBE = new RedstoneProbeItem(new FabricItemSettings().group(ItemGroup.REDSTONE).maxCount(1));

	@Override
	public void onInitialize() {
		// Setup interpreter block
		Identifier interpreterId = id("interpreter");
		Registry.register(Registry.BLOCK_ENTITY_TYPE, interpreterId, INTERPRETER_BLOCK_ENTITY);
		registerBlockAndItem(interpreterId,INTERPRETER_BLOCK,ItemGroup.REDSTONE);

		// Setup probe
		Registry.register(Registry.ITEM,id("redstone_probe"),REDSTONE_PROBE);
		ProbeBehaviour.registerDefaults();
	}

	// Helper to register a block and it's item simultaneously
	private void registerBlockAndItem(Identifier id, Block block, ItemGroup group) {
		Registry.register(Registry.BLOCK, id, block);
		Registry.register(Registry.ITEM, id, new BlockItem(block,new Item.Settings().group(group)));
	}

	// Helper to create an identifier
	public static Identifier id(String path) {
		return new Identifier(MODID,path);
	}


}
