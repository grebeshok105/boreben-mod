package com.vanguard.mod.network;

import com.vanguard.mod.VanguardMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record PhoenixResurrectS2CPayload(UUID playerId, double x, double y, double z)
		implements CustomPacketPayload {
	public static final Type<PhoenixResurrectS2CPayload> TYPE =
			new Type<>(VanguardMod.id("phoenix_resurrect"));

	public static final StreamCodec<ByteBuf, PhoenixResurrectS2CPayload> STREAM_CODEC =
			StreamCodec.composite(
					UUIDUtil.STREAM_CODEC, PhoenixResurrectS2CPayload::playerId,
					ByteBufCodecs.DOUBLE, PhoenixResurrectS2CPayload::x,
					ByteBufCodecs.DOUBLE, PhoenixResurrectS2CPayload::y,
					ByteBufCodecs.DOUBLE, PhoenixResurrectS2CPayload::z,
					PhoenixResurrectS2CPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
