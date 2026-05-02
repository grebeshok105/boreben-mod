package com.vanguard.mod.ability.sword;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which Reinhard-form players currently have Reid Draw active. Used by
 * sword abilities and damage handlers to apply the bonus damage / VFX.
 */
public final class ReidDrawState {
	private static final Set<UUID> DRAWN = ConcurrentHashMap.newKeySet();

	private ReidDrawState() {}

	public static void init() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				DRAWN.remove(handler.player.getUUID()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
				DRAWN.remove(oldPlayer.getUUID()));
	}

	public static boolean isDrawn(ServerPlayer player) {
		return DRAWN.contains(player.getUUID());
	}

	public static void setDrawn(ServerPlayer player, boolean drawn) {
		if (drawn) DRAWN.add(player.getUUID());
		else DRAWN.remove(player.getUUID());
	}

	public static void clear(UUID id) {
		DRAWN.remove(id);
	}
}
