package horiuchi.improvedsigns.mixin;

import horiuchi.improvedsigns.ImprovedSignsBlocks;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumBlockSoundEffectType;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemSignPainted;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemSignPainted.class)
public abstract class ItemSignPaintedMixin extends Item {
	public ItemSignPaintedMixin(@NotNull String name, @NotNull String namespaceId, int id) {
		super(name, namespaceId, id);
	}

	@Inject(method = "onUseOnBlock(Lnet/minecraft/core/item/ItemStack;Lnet/minecraft/core/world/World;Lnet/minecraft/core/entity/player/Player;Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/util/helper/Side;DD)Z", at = @At("HEAD"), cancellable = true)
	private void AttemptPlaceNewSigns(@NotNull ItemStack selfStack, @NotNull World world, @Nullable Player player, @NotNull TilePosc blockPos, @NotNull Side side, double xHit, double yHit, CallbackInfoReturnable<Boolean> cir) {
		if (player == null || !world.getBlockMaterial(blockPos).isSolid()) {
			cir.setReturnValue(false);
			return;
		}

		if (!world.canPlaceInsideBlock(blockPos)) {
			blockPos = blockPos.add(side.direction(), new TilePos());
		}

		if ((blockPos.y() < 0 && blockPos.y() >= world.getHeightBlocks()) || !Blocks.SIGN_POST_PLANKS_OAK_PAINTED.canPlaceAt(world, blockPos)) {
			cir.setReturnValue(false);
			return;
		}

		Block<?> blockPlaced = null;
		if (side == Side.BOTTOM) {
			int direction = MathHelper.floor((double)((player.yRot + 180.0F) * 16.0F / 360.0F) + (double)0.5F) & DyeColor.MASK_COLOR;
			blockPlaced = ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED;
			world.setBlockTypeData(blockPos, blockPlaced, ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED.getLogic().toMetadata(DyeColor.colorFromItemMeta(selfStack.getMetadata())) | direction);
		}
		else if (side.isHorizontal() && Direction.fromYaw(player.yRot).opposite() != side.direction) {
			blockPlaced = ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED;
			world.setBlockTypeData(blockPos, blockPlaced, ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED.getLogic().toMetadata(DyeColor.colorFromItemMeta(selfStack.getMetadata())) | Direction.fromYaw(player.yRot).ordinal());
		}

		// We didn't place a new sign, get outta here and run the vanilla method.
		if (blockPlaced == null) {
			return;
		}

		selfStack.consumeItem(player);
		world.playBlockSoundEffect(player, (float)blockPos.x() + 0.5F, (float)blockPos.y() + 0.5F, (float)blockPos.z() + 0.5F, blockPlaced, EnumBlockSoundEffectType.PLACE);
		TileEntity var12 = world.getTileEntity(blockPos);
		if (var12 instanceof TileEntitySign sign) {
			if (selfStack.getData().containsKey("tileEntityData")) {
				sign.readAdditionalData(selfStack.getData().getCompound("tileEntityData"));
			} else {
				sign.setOwner(player);
				player.displaySignEditorScreen(sign);
			}
		}

		world.notifyBlockChange(blockPos, blockPlaced);
		cir.setReturnValue(true);
	}
}
