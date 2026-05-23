package horiuchi.improvedsigns.mixin;

import horiuchi.improvedsigns.ImprovedSignsBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFence;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLogicFence.class)
public class BlockLogicFenceMixin {
	@Inject(
		method = "canConnectTo(Lnet/minecraft/core/world/WorldSource;Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/util/helper/Side;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void AttachToWallHangingSign(WorldSource source, TilePosc tilePos, Side side, CallbackInfoReturnable<Boolean> cir) {
		TilePos queryPos = new TilePos();
		Block<?> block = source.getBlockType(tilePos.add(side.direction(), queryPos));
		int meta = source.getBlockData(tilePos.add(side.direction(), queryPos)) & DyeColor.MASK_COLOR;
		if (block == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK ||
			block == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED) {
			if ((side == Side.EAST || side == Side.WEST) && (meta == 2 || meta == 3)) {
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			else if ((side == Side.NORTH || side == Side.SOUTH) && (meta == 4 || meta == 5)) {
				cir.setReturnValue(true);
				cir.cancel();
				return;
			}

			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
