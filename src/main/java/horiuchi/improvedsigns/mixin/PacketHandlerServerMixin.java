package horiuchi.improvedsigns.mixin;

import horiuchi.improvedsigns.PackerHandlerServerInterface;
import horiuchi.improvedsigns.PacketImprovedSignUpdate;
import horiuchi.improvedsigns.TileEntitySignBackVariablesInterface;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.enums.EnumSignPicture;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.util.helper.NetCharacters;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import net.minecraft.server.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PacketHandlerServer.class)
public class PacketHandlerServerMixin implements PackerHandlerServerInterface {
	@Final
	@Shadow
	private MinecraftServer mcServer;
	@Shadow
	private PlayerServer playerEntity;

	@Override
	public void improvedsigns$handleSImprovedSignUpdate(@NotNull PacketImprovedSignUpdate packet) {
		if (!this.playerEntity.getGamemode().canInteract() || !this.playerEntity.isAlive()) {
			return;
		}

		WorldServer worldserver = this.mcServer.getDimensionWorld(this.playerEntity.dimension);
		TilePos pos = new TilePos(packet.xPosition, packet.yPosition, packet.zPosition);
		if (!worldserver.isBlockLoaded(pos)) {
			return;
		}

		TileEntity tileEntity = worldserver.getTileEntity(pos);
		if (tileEntity instanceof TileEntitySign sign) {
			TileEntitySignBackVariablesInterface signExtra = (TileEntitySignBackVariablesInterface) sign;
			for(int i = 0; i < packet.signLinesFront.length; ++i) {
				String line = packet.signLinesFront[i];

				assert line.length() <= TileEntitySign.MAX_LINE_SIZE;

				if (!NetCharacters.isAllowed(line)) {
					packet.signLinesFront[i] = "!?";
				}
			}
			for(int i = 0; i < packet.signLinesBack.length; ++i) {
				String line = packet.signLinesBack[i];

				assert line.length() <= TileEntitySign.MAX_LINE_SIZE;

				if (!NetCharacters.isAllowed(line)) {
					packet.signLinesBack[i] = "!?";
				}
			}

			System.arraycopy(packet.signLinesFront, 0, sign.signText, 0, 4);
			System.arraycopy(packet.signLinesBack, 0, signExtra.improvedsigns$getBackText(), 0, 4);
			sign.finalizeEditor(this.playerEntity);
			sign.setColor(TextFormatting.FORMATTINGS[packet.colorFront]);
			sign.setPicture(EnumSignPicture.values()[packet.pictureFront]);
			signExtra.improvedsigns$setColorBack(TextFormatting.FORMATTINGS[packet.colorBack]);
			signExtra.improvedsigns$setPictureBack(EnumSignPicture.values()[packet.pictureBack]);
			sign.setChanged();
			worldserver.markBlockNeedsUpdate(pos);
		}
	}
}
