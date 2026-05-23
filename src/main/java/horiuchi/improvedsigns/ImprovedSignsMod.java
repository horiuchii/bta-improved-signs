package horiuchi.improvedsigns;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.render.TileEntityRenderDispatcher;
import net.minecraft.client.render.tileentity.TileEntityRendererSign;
import net.minecraft.core.block.entity.TileEntityDispatcher;
import net.minecraft.core.block.entity.TileEntityFurnace;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.util.collection.NamespaceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.util.GameStartEntrypoint;
import turniplabs.halplibe.util.RecipeEntrypoint;

public class ImprovedSignsMod implements ModInitializer, GameStartEntrypoint, RecipeEntrypoint {
	public static final String MOD_ID = HalpLibe.registerMod("improvedsigns");
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Improved Signs initialized.");
	}

	@Override
	public void beforeGameStart() {
		ImprovedSignsBlocks.init();
		TileEntityDispatcher.addMapping(TileEntitySign.class, NamespaceID.fromPool("improvedsigns", "sign"));
	}

	@Override
	public void afterGameStart() {

	}

	@Override
	public void onRecipesReady() {

	}

	@Override
	public void initNamespaces() {

	}
}
