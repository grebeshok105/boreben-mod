package com.vanguard.mod.network;

import com.vanguard.mod.VanguardMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C: synchronises Reinhard's phase state to the owning client so the
 * status HUD can render the phase pip and damage progress bar.
 *
 * @param phaseIndex   1..5 (matches {@code ReinhardPhase.index()})
 * @param damageTaken  cumulative pre-mitigation damage (drives next-phase progress)
 */
public record ReinhardPhaseSyncS2CPayload(int phaseIndex, float damageTaken) implements CustomPacketPayload {
	public static final ResourceLocation ID = VanguardMod.id("reinhard_phase_sync");
	public static final Type<ReinhardPhaseSyncS2CPayload> TYPE = new Type<>(ID);

	public static final StreamCodec<ByteBuf, ReinhardPhaseSyncS2CPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, ReinhardPhaseSyncS2CPayload::phaseIndex,
					ByteBufCodecs.FLOAT, ReinhardPhaseSyncS2CPayload::damageTaken,
					ReinhardPhaseSyncS2CPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
