package horiuchi.improvedsigns.mixin;

import horiuchi.improvedsigns.PacketImprovedSignUpdate;
import net.minecraft.core.net.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Packet.class)
public abstract class PacketMixin {
	@Shadow
	public static void addMapping(int id, boolean clientBound, boolean serverBound, Class<? extends Packet> packetClass) {}

	@Inject(
		method = "<clinit>",
		at = @At("TAIL")
	)
	private static void addImprovedSignPacket(CallbackInfo ci) {
		addMapping(150, false, true, PacketImprovedSignUpdate.class);
	}
}
