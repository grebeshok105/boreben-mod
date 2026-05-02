package com.vanguard.mod.network;

import com.vanguard.mod.VanguardMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent from the client when the player presses one of the sword keybinds
 * (Reid Draw / Air Slash / Sky Vault). The server resolves the ability id
 * via {@link com.example.superheroes.api.AbilityApi#get} and dispatches to
 * its {@code tryActivate} method. The client should NEVER assume success —
 * the server is authoritative on resource cost, cooldown, and worthy gate.
 */
public record SwordAbilityActivateC2SPayload(ResourceLocation abilityId) implements CustomPacketPayload {
	public static final Type<SwordAbilityActivateC2SPayload> TYPE =
			new Type<>(VanguardMod.id("sword_ability_activate"));

	public static final StreamCodec<ByteBuf, SwordAbilityActivateC2SPayload> STREAM_CODEC =
			StreamCodec.composite(
					ResourceLocation.STREAM_CODEC, SwordAbilityActivateC2SPayload::abilityId,
					SwordAbilityActivateC2SPayload::new
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
