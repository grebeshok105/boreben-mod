package com.vanguard.mod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects when a Reinhard player who used Sky Vault Jump touches the ground
 * again, and produces a single shockwave at the landing site. Only triggers
 * for landings caused by Sky Vault — regular falls are ignored.
 */
public final class SkyVaultLandingTracker {
	private static final Map<UUID, AirborneState> AIRBORNE = new ConcurrentHashMap<>();

	private SkyVaultLandingTracker() {}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<Map.Entry<UUID, AirborneState>> it = AIRBORNE.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, AirborneState> e = it.next();
				ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
				if (player == null || !player.isAlive()) {
					it.remove();
					continue;
				}
				AirborneState state = e.getValue();
				// Wait at least 4 ticks before checking ground — gives the
				// initial upward velocity time to lift the player off.
				if (player.tickCount - state.markedAtTick < 4) continue;
				if (player.onGround()) {
					triggerShockwave(player);
					it.remove();
				} else if (player.tickCount - state.markedAtTick > 200) {
					// 10s safety timeout — never produce a delayed shockwave.
					it.remove();
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				AIRBORNE.remove(handler.getPlayer().getUUID()));
	}

	public static void markAirborne(ServerPlayer player) {
		AIRBORNE.put(player.getUUID(), new AirborneState(player.tickCount));
	}

	private static void triggerShockwave(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Vec3 origin = player.position();
		level.sendParticles(ParticleTypes.EXPLOSION,
				origin.x, origin.y, origin.z, 1, 0.0, 0.0, 0.0, 0.0);
		level.sendParticles(ParticleTypes.CLOUD,
				origin.x, origin.y + 0.1, origin.z, 60, 1.5, 0.1, 1.5, 0.25);
		level.sendParticles(ParticleTypes.SWEEP_ATTACK,
				origin.x, origin.y + 0.5, origin.z, 6, 1.0, 0.05, 1.0, 0.0);
		level.playSound(null, player.blockPosition(),
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4f, 0.7f);

		AABB box = new AABB(origin, origin).inflate(5.0, 2.0, 5.0);
		List<Entity> entities = level.getEntities(player, box,
				e -> e instanceof LivingEntity && e != player && e.isAlive());
		for (Entity e : entities) {
			LivingEntity living = (LivingEntity) e;
			Vec3 push = e.position().subtract(origin);
			double horizontal = Math.max(0.5, Math.sqrt(push.x * push.x + push.z * push.z));
			Vec3 dir = new Vec3(push.x / horizontal, 0.0, push.z / horizontal);
			living.knockback(2.4, -dir.x, -dir.z);
			living.setDeltaMovement(living.getDeltaMovement().add(0.0, 0.6, 0.0));
			living.hurt(level.damageSources().playerAttack(player), 6.0f);
			living.hurtMarked = true;
		}
	}

	public static void clear(UUID id) {
		AIRBORNE.remove(id);
	}

	private record AirborneState(int markedAtTick) {}
}
