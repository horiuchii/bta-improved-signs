package horiuchi.improvedsigns;

import net.minecraft.core.block.BlockLogicSign;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DyeColor;
import org.joml.Vector2d;

public class ImprovedSignsUtil {
	public static boolean shouldEditBack(TileEntitySign entitySign, Player player) {
		int meta = entitySign.getBlockMeta() & DyeColor.MASK_COLOR;
		float angle;
		BlockLogicSign sign = (BlockLogicSign)entitySign.getBlock().getLogic();
		if (sign.isFreeStanding) {
			angle = (meta * 360.0F) / 16.0F;
		}
		else {
			switch (meta) {
				case 2 -> angle = 180.0F;
				case 3 -> angle = 0.0F;
				case 4 -> angle = 90.0F;
				default -> angle = -90.0F;
			}
		}
		Vector2d entityPos = new Vector2d(entitySign.tilePos.x + 0.5F, entitySign.tilePos.z + 0.5F);
		Vector2d playerPos = new Vector2d(player.x, player.z);
		double angleRad = Math.toRadians(angle);

		boolean editingBack = ((playerPos.x - entityPos.x) * -Math.sin(angleRad) + (playerPos.y - entityPos.y) * Math.cos(angleRad)) < 0;
		if(entitySign.getBlock() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK || entitySign.getBlock() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED) {
			editingBack = !editingBack;
		}
		return editingBack;
	}
}
