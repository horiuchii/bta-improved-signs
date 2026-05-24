package horiuchi.improvedsigns.mixin;

import horiuchi.improvedsigns.ImprovedSignsUtil;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSign;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemPaintBrush;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPaintBrush.class)
abstract class ItemPaintBrushMixin {

	@Shadow
	public abstract void consumePaint(@NotNull ItemStack itemstack, @Nullable Player player);

	@Shadow
	public static @Nullable DyeColor getColor(@NotNull ItemStack stack) {throw new AssertionError();}

	@Inject(method = "onUseOnBlock", at = @At("HEAD"), cancellable = true)
	public void onUseOnBlock(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xPlaced, double yPlaced, CallbackInfoReturnable<Boolean> cir) {
		Block<?> block = world.getBlockType(blockPos);
		if(!Block.hasLogicClass(block, BlockLogicSign.class))
			return;

		if (player == null || !player.isSneaking())
			return;

		TileEntitySign signEntity = (TileEntitySign) world.getTileEntity(blockPos);

		if(signEntity == null)
			return;

		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) signEntity;
		boolean editingBack = ImprovedSignsUtil.shouldEditBack(signEntity, (PlayerLocal) player);

		if ((!editingBack && signEntity.isLocked()) || (editingBack && i.improvedsigns$isLockedBack())) {
			cir.cancel();
			cir.setReturnValue(false);
			return;
		}

		if (!editingBack)
			return;

		DyeColor color = getColor(selfStack);
		if (color != null && color.blockMeta != i.improvedsigns$getColorBack().id) {
			i.improvedsigns$setColorBack(TextFormatting.get(color.blockMeta));
			this.consumePaint(selfStack, player);
			cir.cancel();
			cir.setReturnValue(true);
			return;
		}

		cir.cancel();
		cir.setReturnValue(false);
		return;
	}
}
