package horiuchi.improvedsigns.mixin;

import horiuchi.improvedsigns.ImprovedSignsUtil;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSign;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemDye;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemDye.class)
abstract class ItemDyeMixin {
	@Inject(method = "onUseOnBlock", at = @At("HEAD"), cancellable = true)
	public void onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xPlaced, double yPlaced, CallbackInfoReturnable<Boolean> cir) {
		Block<?> block = world.getBlockType(blockPos);
		if(!Block.hasLogicClass(block, BlockLogicSign.class))
			return;

		TileEntitySign signEntity = (TileEntitySign) world.getTileEntity(blockPos);

		if(signEntity == null || !ImprovedSignsUtil.shouldEditBack(signEntity, (PlayerLocal) player))
			return;

		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) signEntity;
		if (DyeColor.WHITE.itemMeta - selfStack.getMetadata() == i.improvedsigns$getColorBack().id) {
			cir.cancel();
			cir.setReturnValue(false);
			return;
		}

		i.improvedsigns$setColorBack(TextFormatting.get(DyeColor.WHITE.itemMeta - selfStack.getMetadata()));
		if (player == null || player.getGamemode().hasBlockConsumption()) {
			--selfStack.stackSize;
		}

		cir.cancel();
		cir.setReturnValue(true);
		return;
	}
}
