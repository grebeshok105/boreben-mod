package com.vanguard.mod.network;

import com.vanguard.mod.VanguardMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UseWishC2SPayload(int index) implements CustomPacketPayload {
	public static final Type<UseWishC2SPayload> TYPE = new Type<>(VanguardMod.id("use_wish"));

	public static final StreamCodec<ByteBuf, UseWishC2SPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, UseWishC2SPayload::index,
					UseWishC2SPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
