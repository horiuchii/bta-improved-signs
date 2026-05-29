package horiuchi.improvedsigns.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import horiuchi.improvedsigns.ImprovedSignsBlocks;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.enums.TextOutlineQuality;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.font.FontRendererDefault;
import net.minecraft.client.render.font.SF;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelBlock;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.render.tileentity.TileEntityRenderer;
import net.minecraft.client.render.tileentity.TileEntityRendererSign;
import net.minecraft.client.util.helper.Colors;
import net.minecraft.core.block.*;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.enums.EnumSignPicture;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.util.helper.Color;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Mixin(TileEntityRendererSign.class)
public abstract class TileEntityRendererSignMixin extends TileEntityRenderer<TileEntitySign> {

	@Shadow
	private FontRendererDefault fontRenderer = null;
	@Final
	@Shadow
	private @NotNull StringBuilder builder;
	@Shadow
	@Final
	public static TileEntityRendererSign.BufferedTextMeshRenderer textMeshRenderer;

	@Shadow
	private static void drawTexturedModalRect(double width, double height, boolean blended, @NotNull IconCoordinate coordinate) {}

	@Inject(
		method = "doRender(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/block/entity/TileEntitySign;DDDF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/renderer/GLRenderer;pushFrame()V",
			shift = At.Shift.BEFORE
		)
	)
	private void InjectNewSignModel(@NotNull TessellatorGeneral t, @NotNull TileEntitySign tileEntity, double x, double y, double z, float partialTick, CallbackInfo ci, @Local LocalRef<StaticEntityModel> localRef) {
		if(tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK.id() ||
			tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED.id())
			localRef.set(getModel("hanging"));
		if(tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK.id() ||
			tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED.id()) {
			GLRenderer.modelM4f().rotateY(Math.toRadians(180.F));
			GLRenderer.modelM4f().translate(0.0F, 0.0F, -0.45833334F);
			localRef.set(getModel("wall_hanging"));
		}
	}

	@Inject(
		method = "doRender(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/block/entity/TileEntitySign;DDDF)V",
		at = @At(
			value = "TAIL"
		)
	)
	private void renderBackSign(TessellatorGeneral t, TileEntitySign tileEntity, double x, double y, double z, float partialTick, CallbackInfo ci) {
		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) tileEntity;

		String[] backText = i.improvedsigns$getBackText();

		Block<?> block = tileEntity.getBlock();
		BlockLogicSign sign = (BlockLogicSign)block.getLogic();

		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
		int meta = tileEntity.getBlockMeta() & DyeColor.MASK_COLOR;
		float height;
		float angle;
		if (sign.isFreeStanding) {
			angle = (meta * 360.0F) / 16.0F;
			GLRenderer.modelM4f().rotateY(Math.toRadians(-angle));
			height = 20.0F;
		} else {
			switch (meta) {
				case 2 -> angle = 180.0F;
				case 3 -> angle = 0.0F;
				case 4 -> angle = 90.0F;
				default -> angle = -90.0F;
			}

			GLRenderer.modelM4f().rotateY(Math.toRadians(-angle));
			GLRenderer.modelM4f().translate(0.0F, 0.0F, -0.45833334F);
			height = 13.0F;
		}
		if(tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK.id() ||
			tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED.id()) {
			height = 4.0F;
		}
		if(tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK.id() ||
			tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED.id()) {
			GLRenderer.modelM4f().rotateY(Math.toRadians(180.F));
			GLRenderer.modelM4f().translate(0.0F, 0.0F, -0.45833334F);
		}
		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate(0.0F, height * 0.041666668F, -0.04375F);
		GLRenderer.modelM4f().rotateY(Math.toRadians(180.0F));
		GLRenderer.setDepthMask(false);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		EnumSignPicture picture = i.improvedsigns$getPictureBack();
		if (picture != null && picture.isBlended()) {
			GLRenderer.enableState(State.BLEND);
			GLRenderer.setBlendFunc(BlendFactor.ZERO, BlendFactor.SRC_COLOR);
		} else {
			GLRenderer.disableState(State.BLEND);
		}

		if (picture != null && picture != EnumSignPicture.NONE) {
			drawTexturedModalRect(1.0F, 0.5F, picture.isBlended(), TextureRegistry.getTexture(picture.getTextureKey()));
		}

		GLRenderer.disableState(State.BLEND);
		GLRenderer.popFrame();
		float lightOffset = 0.0F;
		if (i.improvedsigns$isGlowingBack()) {
			lightOffset = 96.0F;
			GLRenderer.setLightmapCoord2i(15, 15);
		}

		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate(0.0F, height * 0.041666668F, -0.045833334F);
		GLRenderer.modelM4f().rotateY(Math.toRadians(180.0F));
		GLRenderer.modelM4f().scale(0.011111113F, -0.011111113F, 0.011111113F);
		GLRenderer.setDepthMask(false);
		int color = Colors.allSignColors[i.improvedsigns$getColorBack().id].getARGB();
		int r = (int) MathHelper.clamp((float) Color.redFromInt(color) + lightOffset, 0.0F, 255.0F);
		int g = (int) MathHelper.clamp((float) Color.greenFromInt(color) + lightOffset, 0.0F, 255.0F);
		int b = (int) MathHelper.clamp((float) Color.blueFromInt(color) + lightOffset, 0.0F, 255.0F);
		color = Color.intToIntARGB(0, r, g, b);
		CharSequence line1 = backText[0];
		CharSequence line2 = backText[1];
		CharSequence line3 = backText[2];
		CharSequence line4 = backText[3];
		switch (tileEntity.lineBeingEdited) {
			case 0:
				this.builder.setLength(0);
				line1 = this.builder.append("§+§f> §-").append(line1).append("§+§f <§-");
				break;
			case 1:
				this.builder.setLength(0);
				line2 = this.builder.append("§+§f> §-").append(line2).append("§+§f <§-");
				break;
			case 2:
				this.builder.setLength(0);
				line3 = this.builder.append("§+§f> §-").append(line3).append("§+§f <§-");
				break;
			case 3:
				this.builder.setLength(0);
				line4 = this.builder.append("§+§f> §-").append(line4).append("§+§f <§-");
		}

		int _y = -backText.length * 5;
		if (i.improvedsigns$isGlowingBack() && GameSettings.TEXT_OUTLINE_QUALITY.value == TextOutlineQuality.FANCY) {
			GLRenderer.setLightmapCoord2i(15, 15);
			textMeshRenderer.render(this.fontRenderer, line1, line2, line3, line4, 0, _y, SF.setOutlined(SF.setColor(0L, color)));
		} else {
			textMeshRenderer.render(this.fontRenderer, line1, line2, line3, line4, 0, _y, SF.setColor(0L, color));
		}

		GLRenderer.popFrame();
		GLRenderer.setDepthMask(true);
		GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLRenderer.popFrame();

		// Render sign items
		renderSignItem(tileEntity, false, t, x, y, z, angle, partialTick);
		renderSignItem(tileEntity, true, t, x, y, z, angle, partialTick);
	}

	@Unique
	private void renderSignItem(TileEntitySign tileEntity, boolean back, TessellatorGeneral t, double x, double y, double z, float angle, float partialTick) {
		TileEntitySignBackVariablesInterface i = (TileEntitySignBackVariablesInterface) tileEntity;
		ItemStack item = i.improvedsigns$getItem(back);

		if (item == null)
			return;

		Block<?> block = tileEntity.getBlock();
		ItemModel itemModelDispatch = ItemModelDispatcher.getInstance().getDispatch(item);
		boolean isBlock = false;
		// Some blocks render as items in the inventory, and must be accounted for
		if (itemModelDispatch instanceof ItemModelBlock) {
			Block<?> itemBlock = (((ItemBlock<?>)item.getItem()).getBlock());
			// Hack for buttons rendering weird
			isBlock = !(itemBlock.getLogic() instanceof BlockLogicButton) && BlockModelDispatcher.getInstance().getDispatch(itemBlock).shouldItemRender3d();
		}
		byte light = Minecraft.getMinecraft().currentWorld.getLightIndex(new TilePos(tileEntity.tilePos.x, tileEntity.tilePos.y, tileEntity.tilePos.z), 0);
		float offsetBaseY = isBlock ? -0.540F : -0.4585F;
		Vector3f offset = new Vector3f();
		Vector3f offsetPostRot = new Vector3f();

		if (block == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK || block == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED) {
			offset = new Vector3f(0.0F, offsetBaseY, 0.0F);
		}
		else if (block == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK || block == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED) {
			offset = new Vector3f(0.0F, 0.375F + offsetBaseY, 0.0F);
			angle += 180.0F;
		}
		else if (block == Blocks.SIGN_WALL_PLANKS_OAK || block == Blocks.SIGN_WALL_PLANKS_OAK_PAINTED) {
			offsetPostRot = new Vector3f(0.0F, 0.375F + offsetBaseY, -0.45833334F * (back ? -1.0F : 1.0F));
		}
		else if (block == Blocks.SIGN_POST_PLANKS_OAK || block == Blocks.SIGN_POST_PLANKS_OAK_PAINTED) {
			offset = new Vector3f(0.0F, 0.666F + offsetBaseY, 0.0F);
		}

		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate((float)x + 0.5F + offset.x, (float)y + 0.5F + offset.y, (float)z + 0.5F + offset.z);
		GLRenderer.modelM4f().rotateY(Math.toRadians(-angle + (back ? 180.0F : 0.0F)));
		GLRenderer.modelM4f().translate(0.0F + offsetPostRot.x, 0.0F + offsetPostRot.y, 0.0565F + (isBlock ? 0.11F : 0.0F) + offsetPostRot.z);
		//TODO: option for glowing signs to have their item fullbright
		itemModelDispatch.renderItemEntity(t, item, true, 1, 0, isBlock ? 180.0F : 0.0F, light, partialTick);
		GLRenderer.popFrame();
	}

	@Inject(
		method = "doRender(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/block/entity/TileEntitySign;DDDF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/block/entity/TileEntitySign;getPicture()Lnet/minecraft/core/enums/EnumSignPicture;"
		)
	)
	private void InjectNewSignPictureRotation(@NotNull TessellatorGeneral t, @NotNull TileEntitySign tileEntity, double x, double y, double z, float partialTick, CallbackInfo ci, @Local LocalRef<StaticEntityModel> localRef) {
		if(tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK.id() ||
			tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED.id()) {
			GLRenderer.modelM4f().rotateY(Math.toRadians(180.F));
			GLRenderer.modelM4f().translate(0.0F, 0.0F, -0.45833334F + (0.04375F * 2.0F));
		}
	}

	@ModifyExpressionValue(
		method = "doRender(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/block/entity/TileEntitySign;DDDF)V",
		at = @At(value = "CONSTANT", args = "floatValue=20.0F")
	)
	private float InjectNewSignTextHeight(float original, @NotNull TessellatorGeneral t, @NotNull TileEntitySign tileEntity, double x, double y, double z, float partialTick) {
		if(tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK.id() ||
			tileEntity.getBlockId() == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED.id())
			return 4.0F;

		return original;
	}
}
