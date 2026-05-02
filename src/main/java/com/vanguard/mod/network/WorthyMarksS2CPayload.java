package com.vanguard.mod.network;

import com.vanguard.mod.VanguardMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

public record WorthyMarksS2CPayload(List<UUID> worthyIds) implements CustomPacketPayload {
	public static final Type<WorthyMarksS2CPayload> TYPE =
			new Type<>(VanguardMod.id("worthy_marks"));

	public static final StreamCodec<ByteBuf, WorthyMarksS2CPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.<ByteBuf, UUID>list().apply(UUIDUtil.STREAM_CODEC),
					WorthyMarksS2CPayload::worthyIds,
					WorthyMarksS2CPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
