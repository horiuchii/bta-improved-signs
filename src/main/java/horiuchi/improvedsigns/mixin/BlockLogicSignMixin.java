package horiuchi.improvedsigns.mixin;

import horiuchi.improvedsigns.ImprovedSignsBlocks;
import horiuchi.improvedsigns.ImprovedSignsUtil;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.achievement.Achievements;
import net.minecraft.core.block.*;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLogicSign.class)
public abstract class BlockLogicSignMixin extends BlockLogic implements IPaintable {
	@Unique
	public boolean isHanging = false;
	@Unique
	public boolean isWallHanging = false;

	public BlockLogicSignMixin(@NotNull Block<?> block, @NotNull Material material) {
		super(block, material);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(Block<?> block, boolean isFreeStanding, CallbackInfo ci) {
		this.isHanging = block.id() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK.id() || block.id() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED.id();
		this.isWallHanging = block.id() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK.id() || block.id() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED.id();
	}

	@Inject(method = "getBoundsFromState", at = @At("HEAD"), cancellable = true)
	public void getBoundsFromStateNewSigns(WorldSource source, TilePosc tilePos, CallbackInfoReturnable<AABBdc> cir) {
		if (!this.isWallHanging)
			return;

		float bottom = 0.28125F;
		float top = 0.78125F;
		float width = 1.0F;
		float thickness = 0.125F;
		float offset = 0.45833334F;
		AABBd bounds;
		switch (source.getBlockData(tilePos) & DyeColor.MASK_COLOR) {
			case 2 -> bounds = new AABBd(0.0F, bottom, offset, width, top, offset + thickness);
			case 3 -> bounds = new AABBd(0.0F, bottom, 1.0F - (offset + thickness), width, top, 1.0F - offset);
			case 4 -> bounds = new AABBd(offset, bottom, 0.0F, offset + thickness, top, width);
			case 5 -> bounds = new AABBd(1.0F - (offset + thickness), bottom, 0.0F, 1.0F - offset, top, width);

			default -> bounds = new AABBd(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
		cir.setReturnValue(bounds);
		cir.cancel();
	}

	@Inject(method = "onNeighborChanged(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/block/Block;)V", at = @At(value = "HEAD"), cancellable = true)
	private void onNeighborChangedNewSigns(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Block<?> block, CallbackInfo ci) {
		if (!this.isHanging && !this.isWallHanging)
			return;

		TilePos queryPos = new TilePos();
		boolean isUnstable = false;
		if (this.isHanging) {
			if (!world.getBlockMaterial(tilePos.up(queryPos)).isSolid()) {
				isUnstable = true;
			}
		}
		else {
			int direction = world.getBlockData(tilePos) & DyeColor.MASK_COLOR;
			if (direction == 2 || direction == 3) {
				if (!world.getBlockMaterial(tilePos.west(queryPos)).isSolid() && !world.getBlockMaterial(tilePos.east(queryPos)).isSolid()) {
					isUnstable = true;
				}
			}
			else if (direction == 4 || direction == 5) {
				if (!world.getBlockMaterial(tilePos.north(queryPos)).isSolid() && !world.getBlockMaterial(tilePos.south(queryPos)).isSolid()) {
					isUnstable = true;
				}
			}
		}

		if (isUnstable) {
			this.dropWithCause(world, EnumDropCause.WORLD, tilePos, world.getBlockData(tilePos), null, null);
			world.setBlockTypeNotify(tilePos, Blocks.AIR);
		}

		super.onNeighborChanged(world, tilePos, block);
		ci.cancel();
	}

	@Inject(method = "setColor(Lnet/minecraft/core/world/World;Lnet/minecraft/core/world/pos/TilePosc;Lnet/minecraft/core/util/helper/DyeColor;)V", at = @At(value = "HEAD"), cancellable = true)
	public void setColorNewSigns(@NotNull World world, @NotNull TilePosc tilePos, @NotNull DyeColor color, CallbackInfo ci) {
		if(!this.isHanging && !this.isWallHanging)
			return;

		world.setBlockTypeRaw(tilePos, this.isHanging ? ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED : ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED);
		world.setBlockDataNotify(tilePos, color.blockMeta << 4 | world.getBlockData(tilePos) & DyeColor.MASK_COLOR);
		ci.cancel();
	}

	@Inject(method = "onInteracted", at = @At("HEAD"), cancellable = true)
	public void onInteractedBackInteractions(@NotNull World world, @NotNull TilePosc tilePos, @NotNull Player player, @Nullable Side side, double xHit, double yHit, CallbackInfoReturnable<Boolean> cir) {
		TileEntitySign signEntity = (TileEntitySign) world.getTileEntity(tilePos);

		if (signEntity == null) {
			cir.cancel();
			cir.setReturnValue(false);
			return;
		}

		ItemStack heldItem = player.getHeldItem();

		boolean editingBack = ImprovedSignsUtil.shouldEditBack(signEntity, (PlayerLocal) player);
		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) signEntity;

		// If the side we're editing is locked, attempt to place an item in the sign
		if ((!editingBack && signEntity.isLocked()) || (editingBack && i.improvedsigns$isLockedBack())) {
			boolean flag = i.improvedsigns$setItem(player, heldItem, editingBack);
			world.notifyBlocksOfNeighborChange(tilePos, this.block);
			if (heldItem != null && heldItem.stackSize <= 0) {
				player.inventory.setItem(player.inventory.getCurrentSlot(), (ItemStack)null);
			}

			cir.cancel();
			cir.setReturnValue(flag);
			return;
		}

		// We're editing the back from here on out
		if (!editingBack) {
			return;
		}

		if (i.improvedsigns$isLockedBack()) {
			cir.cancel();
			cir.setReturnValue(false);
			return;
		}

		if (heldItem != null && heldItem.itemID == Items.DUST_GLOWSTONE.id && !i.improvedsigns$isGlowingBack() && heldItem.consumeItem(player)) {
			i.improvedsigns$setGlowingBack(true);
			if (player.getGamemode().hasBlockConsumption()) {
				--heldItem.stackSize;
			}

			player.addStat(Achievements.LIGHT_SIGN, 1);
			cir.cancel();
			cir.setReturnValue(true);
			return;
		}

		if (heldItem != null && heldItem.itemID == Items.SLIMEBALL.id && heldItem.consumeItem(player)) {
			i.improvedsigns$setLockedBack(true);
			cir.cancel();
			cir.setReturnValue(true);
			return;
		}

		if (heldItem != null && (heldItem.itemID == Items.DYE.id || heldItem.itemID == Items.PAINTBRUSH.id)) {
			cir.cancel();
			cir.setReturnValue(false);
			return;
		}

		if (i.improvedsigns$isBackEditableBy(player)) {
			player.displaySignEditorScreen(signEntity);
			cir.cancel();
			cir.setReturnValue(true);
			return;
		}

		cir.cancel();
		cir.setReturnValue(false);
		return;
	}
}
