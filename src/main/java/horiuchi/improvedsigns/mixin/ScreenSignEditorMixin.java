package horiuchi.improvedsigns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import horiuchi.improvedsigns.ImprovedSignsBlocks;
import horiuchi.improvedsigns.ImprovedSignsUtil;
import horiuchi.improvedsigns.PacketImprovedSignUpdate;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenSignEditor;
import net.minecraft.client.net.handler.PacketHandlerClient;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.enums.EnumSignPicture;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.PacketSignUpdate;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ScreenSignEditor.class)
public abstract class ScreenSignEditorMixin extends Screen {
	@Unique
	private boolean editingBack = false;

	@Final
	@Shadow
	private @NotNull TileEntitySign entitySign;

	@Inject(
		method = "init",
		at = @At("HEAD")
	)
	private void checkIfEditingBack(CallbackInfo ci) {
		this.editingBack = ImprovedSignsUtil.shouldEditBack(entitySign, this.mc.thePlayer);
		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) entitySign;
		i.improvedsigns$setBackBeingEdited(this.editingBack);
	}

	@Redirect(method = "removed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/net/handler/PacketHandlerClient;addToSendQueue(Lnet/minecraft/core/net/packet/Packet;)V"))
	private void replaceSignPacket(PacketHandlerClient instance, Packet packet) {
		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) entitySign;
		Objects.requireNonNull(this.mc.getSendQueue()).addToSendQueue(new PacketImprovedSignUpdate(
			this.entitySign.tilePos.x,
			this.entitySign.tilePos.y,
			this.entitySign.tilePos.z,
			this.entitySign.signText,
			this.entitySign.getPicture().getId(),
			this.entitySign.getColor().id,
			i.improvedsigns$getBackText(),
			i.improvedsigns$getPictureBack().getId(),
			i.improvedsigns$getColorBack().id));
	}

	@ModifyExpressionValue(
		method = "keyPressed(CIII)V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/core/block/entity/TileEntitySign;signText:[Ljava/lang/String;",
			opcode = Opcodes.GETFIELD
		)
	)
	private String[] replaceSignText(String[] original) {
		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) entitySign;

		if (this.editingBack) {
			return i.improvedsigns$getBackText();
		}

		return original;
	}

	@Inject(
		method = "render(IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/TileEntityRenderDispatcher;renderTileEntity(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/block/entity/TileEntity;DDDF)V",
			shift = At.Shift.BEFORE
		)
	)
	private void flipSignIfEditingBack(int mx, int my, float partialTick, CallbackInfo ci) {
		if (this.editingBack)
			GLRenderer.modelM4f().rotateY(org.joml.Math.toRadians(180.0F));
	}

	@Inject(
		method = "render(IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/ScreenSignEditor;drawStringCenteredShadow(Lnet/minecraft/client/render/font/FontRenderer;Ljava/lang/CharSequence;III)V",
			shift = At.Shift.BEFORE
		)
	)
	private void SetzLevelTo100(int mx, int my, float partialTick, CallbackInfo ci) {
		this.zLevel = 100;
	}

	@Inject(
		method = "render(IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/ScreenSignEditor;drawStringCenteredShadow(Lnet/minecraft/client/render/font/FontRenderer;Ljava/lang/CharSequence;III)V",
			shift = At.Shift.AFTER
		)
	)
	private void SetzLevelTo0(int mx, int my, float partialTick, CallbackInfo ci) {
		this.zLevel = 0;
	}

	@Redirect(
		method = { "mouseClicked(III)V", "render(IIF)V"},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/block/entity/TileEntitySign;getPicture()Lnet/minecraft/core/enums/EnumSignPicture;"
		)
	)
	private EnumSignPicture injectBackPicture(TileEntitySign tileEntitySign) {
		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) entitySign;
		return this.editingBack ? i.improvedsigns$getPictureBack() : tileEntitySign.getPicture();
	}

	@ModifyExpressionValue(
		method = "render(IIF)V",
		at = @At(value = "CONSTANT", args = "floatValue=-1.0625F")
	)
	private float InjectNewSignRenderOffset(float original, int mx, int my, float partialTick) {
		Block<?> block = this.entitySign.getBlock();
		if(block.id() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK.id() ||
			block.id() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED.id()) {
			GLRenderer.modelM4f().rotateY(org.joml.Math.toRadians(180.0F));
		}

		if(block.id() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK.id() ||
			block.id() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED.id())
			return -0.44F;

		return original;
	}
}
