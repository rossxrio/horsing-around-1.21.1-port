package dev.horsingaround.net;

import dev.horsingaround.HorsingAround;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server to client, from a server with Icy's Better Horses installed too, on join (before the rules) and when the host
 * changes it: whose riding horses use there (see IcyCompat). A payload of its own so the rules keep their format.
 */
public record StylePayload(boolean icyRides) implements CustomPacketPayload {
	public static final Type<StylePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HorsingAround.MOD_ID, "riding_style"));
	public static final StreamCodec<FriendlyByteBuf, StylePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, StylePayload::icyRides,
		StylePayload::new
	);

	@Override
	public Type<StylePayload> type() {
		return TYPE;
	}
}
