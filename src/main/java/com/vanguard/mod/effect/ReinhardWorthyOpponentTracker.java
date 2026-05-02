package com.vanguard.mod.effect;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.hero.ReinhardHero;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Worthy-opponent tracker. Triggered DYNAMICALLY by attackers who manage to
 * deal meaningful damage to Reinhard (≥ 4hp single hit OR ≥ 12hp cumulative
 * within 6s). Once flagged, the attacker stays "worthy" for 30s of inactivity.
 *
 * <p>State is keyed by {@code reinhardId → attackerId → AttackerStats} and is
 * non-persistent (cleared on logout / server stop). The tracker exposes a sync
 * helper so the network layer can push updates to the caster client.
 */
public final class ReinhardWorthyOpponentTracker {
	private static final float SINGLE_HIT_THRESHOLD = 4.0f;
	private static final float CUMULATIVE_THRESHOLD = 12.0f;
	private static final int CUMULATIVE_WINDOW_TICKS = 120;
	private static final int WORTHY_DECAY_TICKS = 600;

	private static final Map<UUID, Map<UUID, AttackerStats>> ATTACKERS = new HashMap<>();
	private static final Map<UUID, Set<UUID>> LAST_BROADCAST = new HashMap<>();

	private ReinhardWorthyOpponentTracker() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player && isReinhard(player) && amount > 0f) {
				LivingEntity attacker = resolveAttacker(source.getEntity(), source.getDirectEntity(), player);
				if (attacker != null) {
					recordHit(player, attacker, amount);
				}
			}
			return true;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<Map.Entry<UUID, Map<UUID, AttackerStats>>> outer = ATTACKERS.entrySet().iterator();
			while (outer.hasNext()) {
				Map.Entry<UUID, Map<UUID, AttackerStats>> e = outer.next();
				ServerPlayer reinhard = server.getPlayerList().getPlayer(e.getKey());
				if (reinhard == null || !isReinhard(reinhard)) {
					outer.remove();
					LAST_BROADCAST.remove(e.getKey());
					continue;
				}
				int now = reinhard.tickCount;
				Iterator<Map.Entry<UUID, AttackerStats>> inner = e.getValue().entrySet().iterator();
				while (inner.hasNext()) {
					Map.Entry<UUID, AttackerStats> a = inner.next();
					AttackerStats stats = a.getValue();
					if (now - stats.lastHitTick > WORTHY_DECAY_TICKS) {
						inner.remove();
					}
				}
				if (e.getValue().isEmpty()) {
					outer.remove();
					LAST_BROADCAST.remove(e.getKey());
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ATTACKERS.remove(handler.getPlayer().getUUID());
			LAST_BROADCAST.remove(handler.getPlayer().getUUID());
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ATTACKERS.clear();
			LAST_BROADCAST.clear();
		});
	}

	private static void recordHit(ServerPlayer reinhard, LivingEntity attacker, float amount) {
		Map<UUID, AttackerStats> map = ATTACKERS.computeIfAbsent(reinhard.getUUID(), k -> new HashMap<>());
		AttackerStats stats = map.computeIfAbsent(attacker.getUUID(), k -> new AttackerStats());
		int now = reinhard.tickCount;
		if (now - stats.windowStartTick > CUMULATIVE_WINDOW_TICKS) {
			stats.windowStartTick = now;
			stats.windowDamage = 0f;
		}
		stats.windowDamage += amount;
		stats.lastHitTick = now;
		if (!stats.worthy && (amount >= SINGLE_HIT_THRESHOLD || stats.windowDamage >= CUMULATIVE_THRESHOLD)) {
			stats.worthy = true;
		}
	}

	public static boolean isWorthy(ServerPlayer reinhard, UUID attackerId) {
		Map<UUID, AttackerStats> map = ATTACKERS.get(reinhard.getUUID());
		if (map == null) return false;
		AttackerStats stats = map.get(attackerId);
		return stats != null && stats.worthy;
	}

	public static Set<UUID> getWorthyIds(ServerPlayer reinhard) {
		Map<UUID, AttackerStats> map = ATTACKERS.get(reinhard.getUUID());
		if (map == null || map.isEmpty()) return Collections.emptySet();
		Set<UUID> out = new HashSet<>();
		for (Map.Entry<UUID, AttackerStats> e : map.entrySet()) {
			if (e.getValue().worthy) {
				out.add(e.getKey());
			}
		}
		return out;
	}

	public static boolean tookOver(ServerPlayer reinhard, Set<UUID> currentSet) {
		Set<UUID> last = LAST_BROADCAST.get(reinhard.getUUID());
		if (last == null) {
			return !currentSet.isEmpty();
		}
		return !last.equals(currentSet);
	}

	public static void recordBroadcast(ServerPlayer reinhard, Set<UUID> set) {
		LAST_BROADCAST.put(reinhard.getUUID(), new HashSet<>(set));
	}

	public static void reset(UUID reinhardId) {
		ATTACKERS.remove(reinhardId);
		LAST_BROADCAST.remove(reinhardId);
	}

	private static LivingEntity resolveAttacker(Entity cause, Entity direct, ServerPlayer self) {
		if (cause instanceof LivingEntity le && le != self) return le;
		if (direct instanceof LivingEntity le && le != self) return le;
		return null;
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}

	private static final class AttackerStats {
		int lastHitTick;
		int windowStartTick;
		float windowDamage;
		boolean worthy;
	}
}
