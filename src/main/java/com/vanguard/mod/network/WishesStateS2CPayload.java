package com.vanguard.mod.network;

import com.vanguard.mod.VanguardMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record WishesStateS2CPayload(
		List<ResourceLocation> recentSources,
		List<ResourceLocation> adaptations,
		int wishesUsed
) implements CustomPacketPayload {
	public static final Type<WishesStateS2CPayload> TYPE =
			new Type<>(VanguardMod.id("wishes_state"));

	public static final StreamCodec<ByteBuf, WishesStateS2CPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.<ByteBuf, ResourceLocation>list().apply(ResourceLocation.STREAM_CODEC),
					WishesStateS2CPayload::recentSources,
					ByteBufCodecs.<ByteBuf, ResourceLocation>list().apply(ResourceLocation.STREAM_CODEC),
					WishesStateS2CPayload::adaptations,
					ByteBufCodecs.VAR_INT,
					WishesStateS2CPayload::wishesUsed,
					WishesStateS2CPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
