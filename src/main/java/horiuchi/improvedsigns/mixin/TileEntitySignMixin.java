package horiuchi.improvedsigns.mixin;

import com.mojang.nbt.tags.CompoundTag;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumSignPicture;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(TileEntitySign.class)
public abstract class TileEntitySignMixin extends TileEntity implements TileEntitySignBackVariablesInterface {
	@Unique
	public @NotNull String @NotNull [] signText_back = new String[]{"", "", "", ""};
	@Unique
	private boolean backBeingEdited = false;
	@Unique
	private int selectedPicture_back = 0;
	@Unique
	private int selectedColor_back = 15;
	@Unique
	private boolean glowing_back = false;
	@Unique
	private boolean locked_back = false;
	@Shadow
	private @Nullable UUID owner;
	@Shadow
	private int selectedPicture;

	@Unique
	private @Nullable ItemStack itemFront;
	@Unique
	private @Nullable ItemStack itemBack;

	@Inject(method = "writeAdditionalData", at = @At("HEAD"))
	private void writeBackData(@NotNull CompoundTag compoundTag, CallbackInfo ci) {
		compoundTag.putString("Text1Back", this.signText_back[0]);
		compoundTag.putString("Text2Back", this.signText_back[1]);
		compoundTag.putString("Text3Back", this.signText_back[2]);
		compoundTag.putString("Text4Back", this.signText_back[3]);
		compoundTag.putInt("PictureBack", this.selectedPicture_back);
		compoundTag.putInt("ColorBack", this.selectedColor_back);
		compoundTag.putBoolean("GlowingBack", this.glowing_back);
		compoundTag.putBoolean("LockedBack", this.locked_back);

		if (this.itemFront != null) {
			compoundTag.putCompound("itemFront", this.itemFront.writeToNBT(new CompoundTag()));
			compoundTag.putBoolean("hasItemFront", true);
		}
		if (this.itemBack != null) {
			compoundTag.putCompound("itemBack", this.itemBack.writeToNBT(new CompoundTag()));
			compoundTag.putBoolean("hasItemBack", true);
		}
	}

	@Inject(method = "readAdditionalData", at = @At("HEAD"))
	private void readBackData(@NotNull CompoundTag compoundTag, CallbackInfo ci) {
		for(int i = 0; i < 4; ++i) {
			this.signText_back[i] = compoundTag.getString("Text" + (i + 1) + "Back");
			if (this.signText_back[i].length() > 15) {
				this.signText_back[i] = this.signText_back[i].substring(0, 15);
			}
		}

		this.selectedPicture_back = compoundTag.getIntegerOrDefault("PictureBack", 0);
		this.selectedColor_back = compoundTag.getIntegerOrDefault("ColorBack", 15);
		this.glowing_back = compoundTag.getBooleanOrDefault("GlowingBack", false);
		this.locked_back = compoundTag.getBooleanOrDefault("LockedBack", false);

		this.itemFront = compoundTag.getBoolean("hasItemFront") ? ItemStack.readItemStackFromNbt(compoundTag.getCompound("itemFront")) : null;
		this.itemBack = compoundTag.getBoolean("hasItemBack") ? ItemStack.readItemStackFromNbt(compoundTag.getCompound("itemBack")) : null;
	}

	@Override
	public ItemStack improvedsigns$getItem(boolean back) {
		return back ? this.itemBack : this.itemFront;
	}

	@Override
	public boolean improvedsigns$setItem(Player player, ItemStack stack, boolean editingBack) {
		boolean success = false;
		boolean wasNull = editingBack ? this.itemBack == null : this.itemFront == null;
		if ((editingBack ? this.itemBack != null : this.itemFront != null) && player.getGamemode().hasBlockConsumption()) {
			player.inventory.insertItem(editingBack ? this.itemBack : this.itemFront, true);
			if ((editingBack ? this.itemBack : this.itemFront).stackSize > 0) {
				player.dropPlayerItem(editingBack ? this.itemBack : this.itemFront);
			}

			success = true;
		}

		if (stack != null) {
			stack.consumeItem(player);
			if (editingBack)
				this.itemBack = new ItemStack(stack.getItem(), 1, stack.getMetadata(), new CompoundTag(stack.getData()));
			else
				this.itemFront = new ItemStack(stack.getItem(), 1, stack.getMetadata(), new CompoundTag(stack.getData()));
			this.setChanged();
			return true;
		} else {
			if (editingBack)
				this.itemBack = null;
			else
				this.itemFront = null;
			this.setChanged();
			return success | !wasNull;
		}
	}

	@Override
	public String[] improvedsigns$getBackText() {
		return this.signText_back;
	}

	@Override
	public boolean improvedsigns$getBackBeingEdited() {
		return this.backBeingEdited;
	}

	@Override
	public void improvedsigns$setBackBeingEdited(boolean backBeingEdited) {
		this.backBeingEdited = backBeingEdited;
	}

	@Override
	public @Nullable EnumSignPicture improvedsigns$getPictureBack() {
		return EnumSignPicture.fromId(this.selectedPicture_back);
	}

	@Override
	public void improvedsigns$setPictureBack(@Nullable EnumSignPicture picture) {
		if (picture == null) {
			this.selectedPicture_back = 0;
		}
		else {
			this.selectedPicture_back = picture.getId();
		}
	}

	@Override
	public TextFormatting improvedsigns$getColorBack() {
		return this.selectedColor_back >= 0 && this.selectedColor_back <= 15
			? TextFormatting.FORMATTINGS[this.selectedColor_back]
			: TextFormatting.BLACK;
	}

	@Override
	public void improvedsigns$setColorBack(TextFormatting color) {
		this.selectedColor_back = color.id;
	}

	@Override
	public boolean improvedsigns$isGlowingBack() {
		return this.glowing_back;
	}

	@Override
	public void improvedsigns$setGlowingBack(boolean glowing) {
		this.glowing_back = glowing;
	}

	@Override
	public boolean improvedsigns$isLockedBack() {
		return this.locked_back;
	}

	@Override
	public void improvedsigns$setLockedBack(boolean locked) {
		this.locked_back = locked;
	}

	@Override
	public boolean improvedsigns$isBackEditableBy(Player player) {
		return player.uuid.equals(this.owner) && !this.locked_back;
	}

	@Inject(method = "finalizeEditor(Z)V", at = @At("HEAD"))
	private void finalizeEditorBackData(boolean allowColorCode, CallbackInfo ci) {
		if (!allowColorCode) {
			for(int i = 0; i < 4; ++i) {
				this.signText_back[i] = TextFormatting.removeAllColorFormatting(this.signText_back[i]);
			}
		}
	}

	@Inject(method = "setPicture", at = @At("HEAD"), cancellable = true)
	public void setPicture(EnumSignPicture picture, CallbackInfo ci) {
		int value = picture == null ? 0 : picture.getId();

		if (this.backBeingEdited) {
			this.selectedPicture_back = value;
		} else {
			this.selectedPicture = value;
		}
		ci.cancel();
	}

	@Override
	public void dropContents(World world, int x, int y, int z) {
		super.dropContents(world, x, y, z);
		if (this.itemFront != null) {
			EntityItem item = world.dropItem(new TilePos(x, y, z), this.itemFront);
			item.xd *= 0.5F;
			item.yd *= 0.5F;
			item.zd *= 0.5F;
			item.pickupDelay = 0;
		}
		if (this.itemBack != null) {
			EntityItem item = world.dropItem(new TilePos(x, y, z), this.itemBack);
			item.xd *= 0.5F;
			item.yd *= 0.5F;
			item.zd *= 0.5F;
			item.pickupDelay = 0;
		}
	}
}
