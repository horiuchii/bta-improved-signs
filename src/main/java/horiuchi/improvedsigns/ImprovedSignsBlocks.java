package horiuchi.improvedsigns;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSign;
import net.minecraft.core.block.BlockLogicSignPainted;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.item.Items;
import net.minecraft.core.sound.BlockSounds;
import turniplabs.halplibe.helper.BlockBuilder;
import turniplabs.halplibe.util.BlockInitEntrypoint;

import static horiuchi.improvedsigns.ImprovedSignsMod.MOD_ID;

public class ImprovedSignsBlocks implements BlockInitEntrypoint {

	private static int BLOCK_IDS_START = 2150;

	public static Block<BlockLogicSign> SIGN_HANGING_PLANKS_OAK;
	public static Block<BlockLogicSignPainted> SIGN_HANGING_PLANKS_OAK_PAINTED;

	public static Block<BlockLogicSign> SIGN_WALL_HANGING_PLANKS_OAK;
	public static Block<BlockLogicSignPainted> SIGN_WALL_HANGING_PLANKS_OAK_PAINTED;

	private static boolean hasInit = false;

	@Override
	public void afterBlockInit() {
		init();
	}

	public static void init() {
		if (hasInit)
			return;

		hasInit = true;
		initializeBlocks();
	}

	public static void initializeBlocks() {
		SIGN_HANGING_PLANKS_OAK = new BlockBuilder(MOD_ID)
			.setBlockSound(BlockSounds.WOOD)
			.setHardness(1.0f)
			.setStatParent(() -> Items.SIGN)
			.setTags(BlockTags.NOT_IN_CREATIVE_MENU, BlockTags.MINEABLE_BY_AXE)
			.build("sign.hanging.planks.oak", "sign_hanging_planks_oak", BLOCK_IDS_START++, b -> new BlockLogicSign(b, true));
		SIGN_HANGING_PLANKS_OAK_PAINTED = new BlockBuilder(MOD_ID)
			.setBlockSound(BlockSounds.WOOD)
			.setHardness(1.0f)
			.setStatParent(() -> Items.SIGN)
			.setTags(BlockTags.NOT_IN_CREATIVE_MENU, BlockTags.MINEABLE_BY_AXE)
			.build("sign.hanging.planks.oak.painted", "sign_hanging_planks_oak_painted", BLOCK_IDS_START++, b -> new BlockLogicSignPainted(b, true));

		SIGN_WALL_HANGING_PLANKS_OAK = new BlockBuilder(MOD_ID)
			.setBlockSound(BlockSounds.WOOD)
			.setHardness(1.0f)
			.setStatParent(() -> Items.SIGN)
			.setTags(BlockTags.NOT_IN_CREATIVE_MENU, BlockTags.MINEABLE_BY_AXE)
			.build("sign.wall.hanging.planks.oak", "sign_wall_hanging_planks_oak", BLOCK_IDS_START++, b -> new BlockLogicSign(b, false));
		SIGN_WALL_HANGING_PLANKS_OAK_PAINTED = new BlockBuilder(MOD_ID)
			.setBlockSound(BlockSounds.WOOD)
			.setHardness(1.0f)
			.setStatParent(() -> Items.SIGN)
			.setTags(BlockTags.NOT_IN_CREATIVE_MENU, BlockTags.MINEABLE_BY_AXE)
			.build("sign.wall.hanging.planks.oak.painted", "sign_wall_hanging_planks_oak_painted", BLOCK_IDS_START++, b -> new BlockLogicSignPainted(b, false));
	}
}
