package com.vanguard.mod.effect;

import com.vanguard.mod.ability.sword.HeavensSwordStrikeAbility;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Holds pending Heaven's Sword Strike impacts and ticks them to detonation.
 * During the telegraph window we paint a vertical column of particles above
 * the target so victims know what's coming and can disengage.
 */
public final class HeavensSwordStrikeManager {
	private static final List<PendingStrike> PENDING = new ArrayList<>();

	private HeavensSwordStrikeManager() {}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<PendingStrike> it = PENDING.iterator();
			while (it.hasNext()) {
				PendingStrike p = it.next();
				ServerPlayer caster = server.getPlayerList().getPlayer(p.casterId);
				if (caster == null) {
					it.remove();
					continue;
				}
				ServerLevel level = caster.serverLevel();
				int elapsed = caster.tickCount - p.startTick;
				if (elapsed >= p.fallTicks) {
					HeavensSwordStrikeAbility.detonate(caster, p.target, p.radius, p.damage);
					it.remove();
					continue;
				}
				// Telegraph: paint a falling column above the target. Column
				// height shrinks from 14 to 0 as time approaches detonation.
				double progress = elapsed / (double) p.fallTicks;
				double colHeight = 14.0 * (1.0 - progress);
				for (int i = 0; i < 6; i++) {
					double y = p.target.y + colHeight * (i / 5.0);
					level.sendParticles(ParticleTypes.FLAME,
							p.target.x, y, p.target.z, 1, 0.05, 0.05, 0.05, 0.0);
				}
				if (elapsed % 4 == 0) {
					level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
							p.target.x, p.target.y + 0.1, p.target.z, 8,
							p.radius * 0.5, 0.05, p.radius * 0.5, 0.0);
				}
			}
		});
	}

	public static void schedule(ServerPlayer caster, Vec3 target, int fallTicks,
								double radius, float damage) {
		PENDING.add(new PendingStrike(
				caster.getUUID(), caster.tickCount, target, fallTicks, radius, damage));
	}

	public static void clear(UUID id) {
		PENDING.removeIf(p -> p.casterId.equals(id));
	}

	private record PendingStrike(UUID casterId, int startTick, Vec3 target,
								  int fallTicks, double radius, float damage) {}
}
