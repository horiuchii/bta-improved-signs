package horiuchi.improvedsigns;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.TileEntityRenderDispatcher;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.BlockModelEmpty;
import net.minecraft.client.render.block.model.BlockModelSignPainted;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import turniplabs.halplibe.util.ModelEntrypoint;

@Environment(EnvType.CLIENT)
public class ImprovedSignsModels implements ModelEntrypoint {
	@Override
	public void initBlockModels(BlockModelDispatcher dispatcher) {
		dispatcher.addDispatch(new BlockModelEmpty<>(ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK).setAllTextures("minecraft:block/planks/oak"));
		dispatcher.addDispatch((new BlockModelSignPainted<>(ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED)).setAllTextures("minecraft:block/planks/oak"));

		dispatcher.addDispatch(new BlockModelEmpty<>(ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK).setAllTextures("minecraft:block/planks/oak"));
		dispatcher.addDispatch((new BlockModelSignPainted<>(ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED)).setAllTextures("minecraft:block/planks/oak"));
	}

	@Override
	public void initItemModels(ItemModelDispatcher dispatcher) {

	}

	@Override
	public void initEntityModels(EntityRendererDispatcher dispatcher) {

	}

	@Override
	public void initTileEntityModels(TileEntityRenderDispatcher dispatcher) {

	}

	@Override
	public void initBlockColors(BlockColorDispatcher dispatcher) {

	}
}
