package com.vanguard.mod.effect;

import com.vanguard.mod.ability.sword.SwordWaveAbility;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Server-tick driver for sword-wave "projectiles". Each wave is a small AABB
 * that advances a fixed distance per tick along a stored direction; on each
 * step we collide it with living entities and forward hits to
 * {@link SwordWaveAbility#onHit}.
 */
public final class SwordWaveProjectiles {
	private static final int LIFE_TICKS = 14;
	private static final double SPEED = 1.0;
	private static final double HALF_WIDTH = 1.4;
	private static final double HALF_HEIGHT = 1.0;

	private static final List<Wave> ACTIVE = new ArrayList<>();

	private SwordWaveProjectiles() {}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<Wave> it = ACTIVE.iterator();
			while (it.hasNext()) {
				Wave w = it.next();
				ServerPlayer caster = server.getPlayerList().getPlayer(w.casterId);
				if (caster == null || ++w.elapsed >= LIFE_TICKS) {
					it.remove();
					continue;
				}
				ServerLevel level = caster.serverLevel();
				w.position = w.position.add(w.direction.scale(SPEED));
				AABB box = new AABB(
						w.position.x - HALF_WIDTH, w.position.y - HALF_HEIGHT, w.position.z - HALF_WIDTH,
						w.position.x + HALF_WIDTH, w.position.y + HALF_HEIGHT, w.position.z + HALF_WIDTH);
				List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, box,
						e -> e != caster && e.isAlive() && !w.alreadyHit.contains(e.getUUID()));
				for (LivingEntity living : hits) {
					w.alreadyHit.add(living.getUUID());
					SwordWaveAbility.onHit(caster, living);
				}
				// Visual streak.
				level.sendParticles(ParticleTypes.SWEEP_ATTACK,
						w.position.x, w.position.y, w.position.z, 1, 0.0, 0.0, 0.0, 0.0);
				level.sendParticles(ParticleTypes.GLOW,
						w.position.x, w.position.y, w.position.z, 4, 0.4, 0.2, 0.4, 0.01);
				level.sendParticles(ParticleTypes.CLOUD,
						w.position.x, w.position.y, w.position.z, 6, 0.6, 0.2, 0.6, 0.02);
			}
		});
	}

	public static void spawn(ServerPlayer caster, Vec3 origin, Vec3 direction) {
		ACTIVE.add(new Wave(caster.getUUID(), origin, direction));
	}

	public static void clear(UUID id) {
		ACTIVE.removeIf(w -> w.casterId.equals(id));
	}

	private static final class Wave {
		final UUID casterId;
		final Vec3 direction;
		Vec3 position;
		int elapsed;
		final Set<UUID> alreadyHit = new HashSet<>();

		Wave(UUID casterId, Vec3 origin, Vec3 direction) {
			this.casterId = casterId;
			this.direction = direction;
			this.position = origin;
		}
	}
}
