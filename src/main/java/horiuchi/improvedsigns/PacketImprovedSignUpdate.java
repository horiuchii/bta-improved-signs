package horiuchi.improvedsigns;

import net.minecraft.core.block.entity.TileEntitySign;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketImprovedSignUpdate extends Packet {
	public int xPosition;
	public int yPosition;
	public int zPosition;
	public String[] signLinesFront;
	public int pictureFront;
	public int colorFront;
	public String[] signLinesBack;
	public int pictureBack;
	public int colorBack;

	public PacketImprovedSignUpdate() {
		this.isChunkDataPacket = true;
	}

	public PacketImprovedSignUpdate(int x, int y, int z, String[] linesFront, int pictureFront, int colorFront, String[] linesBack, int pictureBack, int colorBack) {
		this.isChunkDataPacket = true;
		this.xPosition = x;
		this.yPosition = y;
		this.zPosition = z;
		this.signLinesFront = linesFront;
		this.pictureFront = pictureFront;
		this.colorFront = colorFront;
		this.signLinesBack = linesBack;
		this.pictureBack = pictureBack;
		this.colorBack = colorBack;
	}

	@Override
	public void read(DataInputStream dis) throws IOException {
		this.xPosition = dis.readInt();
		this.yPosition = dis.readShort();
		this.zPosition = dis.readInt();
		this.signLinesFront = new String[4];
		this.signLinesBack = new String[4];

		for(int i = 0; i < 4; ++i) {
			this.signLinesFront[i] = readStringUTF16BE(dis, TileEntitySign.MAX_LINE_SIZE);
			this.signLinesBack[i] = readStringUTF16BE(dis, TileEntitySign.MAX_LINE_SIZE);
		}

		this.pictureFront = dis.readInt();
		this.pictureBack = dis.readInt();
		this.colorFront = dis.readInt();
		this.colorBack = dis.readInt();
	}

	@Override
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(this.xPosition);
		dos.writeShort(this.yPosition);
		dos.writeInt(this.zPosition);

		for(int i = 0; i < 4; ++i) {
			writeStringUTF16BE(this.signLinesFront[i], dos);
			writeStringUTF16BE(this.signLinesBack[i], dos);
		}

		dos.writeInt(this.pictureFront);
		dos.writeInt(this.pictureBack);
		dos.writeInt(this.colorFront);
		dos.writeInt(this.colorBack);
	}

	@Override
	public void handlePacket(PacketHandler packetHandler) {
		PackerHandlerServerInterface i = (PackerHandlerServerInterface) packetHandler;
		i.improvedsigns$handleSImprovedSignUpdate(this);
	}

	@Override
	public int getEstimatedSize() {
		int i = 16;

		for(int j = 0; j < 4; ++j) {
			i += this.signLinesFront[j].length();
			i += this.signLinesBack[j].length();
		}

		return i;
	}
}
