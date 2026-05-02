package com.vanguard.mod.ability.sword;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player cooldown registry for sword abilities. Cooldowns are stored as
 * absolute server-tick deadlines. The server is the only authority.
 */
public final class SwordCooldowns {
	private static final Map<UUID, Map<ResourceLocation, Long>> COOLDOWNS = new ConcurrentHashMap<>();

	private SwordCooldowns() {}

	public static void init() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				COOLDOWNS.remove(handler.player.getUUID()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
				COOLDOWNS.remove(oldPlayer.getUUID()));
	}

	/** Returns true if the ability is off cooldown right now. */
	public static boolean isReady(ServerPlayer player, ResourceLocation abilityId) {
		Map<ResourceLocation, Long> map = COOLDOWNS.get(player.getUUID());
		if (map == null) return true;
		Long until = map.get(abilityId);
		if (until == null) return true;
		return player.tickCount >= until;
	}

	/** Stamps the cooldown to {@code current_tick + cooldownTicks}. */
	public static void start(ServerPlayer player, ResourceLocation abilityId, int cooldownTicks) {
		COOLDOWNS.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
				.put(abilityId, (long) (player.tickCount + cooldownTicks));
	}

	/** Remaining cooldown ticks, or 0 if ready. */
	public static int remaining(ServerPlayer player, ResourceLocation abilityId) {
		Map<ResourceLocation, Long> map = COOLDOWNS.get(player.getUUID());
		if (map == null) return 0;
		Long until = map.get(abilityId);
		if (until == null) return 0;
		long delta = until - player.tickCount;
		return delta <= 0 ? 0 : (int) delta;
	}

	public static void clear(UUID id) {
		COOLDOWNS.remove(id);
	}
}
