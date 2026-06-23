package horiuchi.improvedsigns.mixin;

import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import horiuchi.improvedsigns.ImprovedSignsBlocks;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.enums.TextOutlineQuality;
import net.minecraft.client.render.MapItemRenderer;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.font.FontRendererDefault;
import net.minecraft.client.render.font.SF;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelBlock;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
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
import net.minecraft.core.item.*;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.util.helper.Color;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.saveddata.maps.ItemMapSavedData;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
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
	private FontRendererDefault fontRenderer;
	@Final
	@Shadow
	private @NotNull StringBuilder builder;
	@Final
	@Shadow
	private String[] signColorTextures;
	@Shadow
	@Final
	public static TileEntityRendererSign.BufferedTextMeshRenderer textMeshRenderer;
	@Unique
	private final Minecraft mc = Minecraft.getMinecraft();
	@Unique
	private final MapItemRenderer renderMapInstance = new MapItemRenderer();

	@Shadow
	private static void drawTexturedModalRect(double width, double height, boolean blended, @NotNull IconCoordinate coordinate) {}


	@Unique
	private static final int FLAG_WIDTH = 24;
	@Unique
	private static final int FLAG_HEIGHT = 16;
	@Unique
	private byte[] unpackFlagColors(byte[] packed) {
		byte[] unpacked = new byte[384];

		for(int i = 0; i < 96; ++i) {
			unpacked[i * 4 + 0] = (byte)((packed[i] & 3) >> 0);
			unpacked[i * 4 + 1] = (byte)((packed[i] & 12) >> 2);
			unpacked[i * 4 + 2] = (byte)((packed[i] & 48) >> 4);
			unpacked[i * 4 + 3] = (byte)((packed[i] & 192) >> 6);
		}

		return unpacked;
	}

	@Inject(
		method = "doRender(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/block/entity/TileEntitySign;DDDF)V",
		at = @At(
			value = "HEAD"
		),
		cancellable = true
	)
	private void renderSignMixin(TessellatorGeneral t, TileEntitySign tileEntity, double x, double y, double z, float partialTick, CallbackInfo ci) {
		ci.cancel();

		Block<?> block = tileEntity.getBlock();
		BlockLogicSign signLogic = (BlockLogicSign) block.getLogic();
		TileEntitySignBackVariablesInterface signInterface = (TileEntitySignBackVariablesInterface) tileEntity;

		if (this.fontRenderer == null) {
			this.fontRenderer = new FontRendererDefault();
			this.fontRenderer.init();
		}

		// Set Sign model and offset
		GLRenderer.pushFrame();
		GLRenderer.modelM4f().translate((float)x + 0.5F, (float)y, (float)z + 0.5F);
		int meta = tileEntity.getBlockMeta();
		float height, angle = 0.0F;
		StaticEntityModel model;
		if (signLogic.isFreeStanding) {
			angle = ((meta & DyeColor.MASK_COLOR) * 360.0F) / 16.0F;
			GLRenderer.modelM4f().rotateY(Math.toRadians(-angle));
			model = this.getModel("post");
			height = 20.0F;
			if(block == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK || block == ImprovedSignsBlocks.SIGN_HANGING_PLANKS_OAK_PAINTED) {
				model = this.getModel("hanging");
				height = 4.0F;
			}
		} else {
			switch (Direction.fromIdLenient(meta & DyeColor.MASK_COLOR)) {
				case NORTH -> angle = 180.0F;
				case SOUTH -> angle = 0.0F;
				case WEST -> angle = 90.0F;
				case EAST -> angle = -90.0F;
			}

			GLRenderer.modelM4f().rotateY(Math.toRadians(-angle));
			GLRenderer.modelM4f().translate(0.0F, 0.0F, -0.45833334F);
			model = this.getModel("wall");
			height = 13.0F;
			if(block == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK || block == ImprovedSignsBlocks.SIGN_WALL_HANGING_PLANKS_OAK_PAINTED) {
				GLRenderer.modelM4f().rotateY(Math.toRadians(180.F));
				GLRenderer.modelM4f().translate(0.0F, 0.0F, -0.45833334F);
				model = this.getModel("wall_hanging");
			}
		}
		for (int back = 0; back < 2; back++) {
			ItemStack itemStack = signInterface.improvedsigns$getItem(back == 1);

			if (itemStack != null) {
				Item itemStackItem = itemStack.getItem();
				if (itemStackItem instanceof ItemMap) {
					if (ItemMap.hasInitialized(itemStack)) {
						model = this.getModel("map");
						height = 13.0F;
						break;
					}
				}
			}
		}

		// Set sign texture
		GLRenderer.pushFrame();
		float scale = 0.041666668F;
		GLRenderer.modelM4f().scale(scale, scale, -scale);
		if (Block.hasLogicClass(block, BlockLogicSignPainted.class)) {
			DyeColor c = ((IPainted)block.getLogic()).fromMetadata(meta);
			this.bindTexture(this.signColorTextures[c.blockMeta]);
		} else {
			this.bindTexture("/assets/minecraft/textures/entity/sign.png");
		}
		GLRenderer.disableState(State.BLEND);
		model.render();
		GLRenderer.enableState(State.BLEND);
		GLRenderer.popFrame();

		// Picture
		for (int back = 0; back < 2; back++) {
			GLRenderer.pushFrame();
			GLRenderer.modelM4f().translate(0.0F, height * 0.041666668F, back == 1 ? -0.04375F : 0.04375F);
			if (back == 1)
				GLRenderer.modelM4f().rotateY(Math.toRadians(180.0F));
			GLRenderer.setDepthMask(false);
			GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			EnumSignPicture picture = back == 1 ? signInterface.improvedsigns$getPictureBack() : tileEntity.getPicture();
			if (picture != null) {
				if (picture.isBlended()) {
					GLRenderer.enableState(State.BLEND);
					GLRenderer.setBlendFunc(BlendFactor.ZERO, BlendFactor.SRC_COLOR);
				} else {
					GLRenderer.disableState(State.BLEND);
				}
				if (picture != EnumSignPicture.NONE) {
					drawTexturedModalRect(1.0F, 0.5F, picture.isBlended(), TextureRegistry.getTexture(picture.getTextureKey()));
				}
				GLRenderer.disableState(State.BLEND);
			}
			GLRenderer.popFrame();
		}

		// Item
		for (int back = 0; back < 2; back++) {
			GLRenderer.pushFrame();
			ItemStack itemStack = signInterface.improvedsigns$getItem(back == 1);

			if (itemStack != null) {
				Item itemStackItem = itemStack.getItem();
				ItemModel itemModelDispatch = ItemModelDispatcher.getInstance().getDispatch(itemStack);
				GLRenderer.modelM4f().translate(0.0F, (height * 0.041666668F) - 0.125F, back == 1 ? -0.044791667F : 0.044791667F);
				if (back == 1)
					GLRenderer.modelM4f().rotateY(Math.toRadians(180.0F));
				byte light = this.mc.currentWorld.getLightIndex(new TilePos(tileEntity.tilePos.x, tileEntity.tilePos.y, tileEntity.tilePos.z), 0);
				// Render map
				if (itemStackItem instanceof ItemMap && ItemMap.hasInitialized(itemStack)) {
					ItemMapSavedData mapData = Items.MAP.getOrCreateSavedData(itemStack, this.mc.currentWorld);
					if (mapData != null) {
						float mapRenderScale = 1.0F/128.0F;
						GLRenderer.modelM4f().translate(-0.5F,  0.5875F, 0.0F);
						GLRenderer.modelM4f().scale(mapRenderScale, -mapRenderScale, mapRenderScale);
						this.renderMapInstance.renderMap(t, mapData);
					}
				}
				// Render flag
				else if (itemStackItem instanceof ItemFlag itemFlag && itemFlag.hasFlagBeenDrawnOn(itemStack)) {
					CompoundTag flagData = itemStack.getData().getCompoundOrDefault("FlagData", null);
					if (flagData != null) {
						byte[] colorIndexes = unpackFlagColors(flagData.getByteArray("Colors"));
						int[] colorData = {-1, -1, -1};

						ListTag list = flagData.getList("Items");
						for(int i = 0; i < list.tagCount(); ++i) {
							CompoundTag compound = (CompoundTag)list.tagAt(i);
							ItemStack stack = ItemStack.readItemStackFromNbt(compound);
							if (stack != null && stack.getItem().equals(Items.DYE)) {
								colorData[i] = Colors.allFlagColors[TextFormatting.get(DyeColor.MASK_COLOR - stack.getMetadata()).id].getARGB();
							}
						}

						float flagScale = scale * 0.75F;

						GLRenderer.pushFrame();
						GLRenderer.modelM4f().rotateY(Math.toRadians(180.0F));

						Minecraft.getMinecraft().textureManager.loadTexture("/assets/minecraft/textures/entity/flag_ui.png").bind();

						double flagBgMinX = ((FLAG_WIDTH / 2.0F) * flagScale);
						double flagBgMinY = ((FLAG_HEIGHT / 2.0F + 4F) * flagScale);
						double flagBgMaxX = ((FLAG_WIDTH / 2.0F) * flagScale) - FLAG_WIDTH * flagScale;
						double flagBgMaxY = ((FLAG_HEIGHT / 2.0F + 4F) * flagScale) - FLAG_HEIGHT * flagScale;

						t.startDrawingQuads();
						t.addVertexWithUV(flagBgMinX, flagBgMaxY, 0.001, 0, 1);
						t.addVertexWithUV(flagBgMaxX, flagBgMaxY, 0.001, 1, 1);
						t.addVertexWithUV(flagBgMaxX, flagBgMinY, 0.001, 1, 0);
						t.addVertexWithUV(flagBgMinX, flagBgMinY, 0.001, 0, 0);
						t.draw();

						GLRenderer.enableState(State.BLEND);
						GLRenderer.setBlendFunc(BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.SRC_COLOR);
						GLRenderer.setShader(Shaders.COLOR);
						GLRenderer.globalSetLightEnabled(false);

						for (int color = 0; color < 3; color++) {
							if (colorData[color] == -1) {
								continue;
							}

							GLRenderer.setColor1i(colorData[color]);

							t.startDrawingQuads();

							for (int dx = 0; dx < FLAG_WIDTH; dx++) {
								for (int dy = 0; dy < FLAG_HEIGHT; dy++) {
									if (colorIndexes[dx + FLAG_WIDTH * dy] - 1 != color) {
										continue;
									}

									double minX = (((FLAG_WIDTH - 2) / 2.0F) * flagScale) - dx * flagScale;
									double minY = ((FLAG_HEIGHT / 2.0F + 3F) * flagScale) - dy * flagScale;
									double maxX = minX + flagScale;
									double maxY = minY + flagScale;

									t.addVertex(minX, maxY, 0.0F);
									t.addVertex(maxX, maxY, 0.0F);
									t.addVertex(maxX, minY, 0.0F);
									t.addVertex(minX, minY, 0.0F);
								}
							}

							t.draw();
						}

						GLRenderer.disableState(State.BLEND);
						GLRenderer.globalSetLightEnabled(true);
						GLRenderer.popFrame();
					}
				}
				// Render item or block
				else {
					GLRenderer.enableState(State.BLEND);
					if (itemModelDispatch instanceof ItemModelBlock && itemStackItem instanceof ItemBlock<?> itemBlock &&
						BlockModelDispatcher.getInstance().getDispatch(itemBlock.getBlock()).shouldItemRender3d()) {
						GLRenderer.modelM4f().scale(0.5F, 0.5F, 0.001F);
						GLRenderer.modelM4f().translate(0.0F,  0.25F, 0.0F);
						itemModelDispatch.render(t, null, itemStack, "gui", false, 1, light, partialTick, false);
					}
					else {
						itemModelDispatch.render(t, null, itemStack, "ground", false, 1, light, partialTick, false);
					}
					GLRenderer.disableState(State.BLEND);
				}
			}

			GLRenderer.popFrame();
		}

		// Text
		for (int back = 0; back < 2; back++) {
			float lightOffset = 0.0F;
			boolean glowing = back == 1 ? signInterface.improvedsigns$isGlowingBack() : tileEntity.isGlowing();
			if (glowing) {
				lightOffset = 96.0F;
				GLRenderer.setLightmapCoord2i(15, 15);
			}

			GLRenderer.pushFrame();
			GLRenderer.modelM4f().translate(0.0F, height * 0.041666668F, back == 1 ? -0.045833334F : 0.045833334F);
			if (back == 1)
				GLRenderer.modelM4f().rotateY(Math.toRadians(180.0F));
			float scale2 = 0.011111113F;
			GLRenderer.modelM4f().scale(scale2, -scale2, scale2);
			GLRenderer.setDepthMask(false);
			int color = Colors.allSignColors[(back == 1 ? signInterface.improvedsigns$getColorBack() : tileEntity.getColor()).id].getARGB();
			int r = (int)MathHelper.clamp((float)Color.redFromInt(color) + lightOffset, 0.0F, 255.0F);
			int g = (int)MathHelper.clamp((float)Color.greenFromInt(color) + lightOffset, 0.0F, 255.0F);
			int b = (int)MathHelper.clamp((float)Color.blueFromInt(color) + lightOffset, 0.0F, 255.0F);
			color = Color.intToIntARGB(0, r, g, b);
			String[] signText = (back == 1 ? signInterface.improvedsigns$getBackText() : tileEntity.signText);
			CharSequence line1 = signText[0];
			CharSequence line2 = signText[1];
			CharSequence line3 = signText[2];
			CharSequence line4 = signText[3];
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

			int _x = 0;
			int _y = -signText.length * 5;
			if (glowing && GameSettings.TEXT_OUTLINE_QUALITY.value == TextOutlineQuality.FANCY) {
				GLRenderer.setLightmapCoord2i(15, 15);
				textMeshRenderer.render(this.fontRenderer, line1, line2, line3, line4, _x, _y, SF.setOutlined(SF.setColor(0L, color)));
			} else {
				textMeshRenderer.render(this.fontRenderer, line1, line2, line3, line4, _x, _y, SF.setColor(0L, color));
			}

			GLRenderer.popFrame();
			GLRenderer.setDepthMask(true);
			GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GLRenderer.popFrame();
	}
}
