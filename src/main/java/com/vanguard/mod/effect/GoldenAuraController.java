package com.vanguard.mod.effect;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.hero.ReinhardHero;
import com.vanguard.mod.hero.ReinhardPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Vector3f;

/**
 * Golden aura VFX from phase 4+. Spawns a torus of gold dust + sparkling
 * end-rod particles around Reinhard each server tick. Intensity scales
 * between P4 and P5 (P5 is denser and adds an upward column).
 *
 * <p>Particle counts are deliberately conservative — this fires every server
 * tick on potentially many players, so we lean on a small number of high-
 * visibility particles instead of bulk emission.
 */
public final class GoldenAuraController {
	private static final Vector3f GOLD = new Vector3f(1.0f, 0.84f, 0.20f);
	private static final Vector3f IVORY = new Vector3f(1.0f, 0.96f, 0.78f);
	private static final DustParticleOptions DUST_GOLD = new DustParticleOptions(GOLD, 1.4f);
	private static final DustParticleOptions DUST_IVORY = new DustParticleOptions(IVORY, 1.0f);

	private GoldenAuraController() {}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!isReinhard(player)) continue;
				ReinhardPhase phase = ReinhardPhaseController.getCurrentPhase(player);
				if (phase.ordinal() < ReinhardPhase.P4.ordinal()) continue;
				emitAura(player, phase);
			}
		});
	}

	private static void emitAura(ServerPlayer player, ReinhardPhase phase) {
		ServerLevel level = player.serverLevel();
		double cx = player.getX();
		double cy = player.getY() + 0.05;
		double cz = player.getZ();
		long t = level.getGameTime();

		// Slow rotating ring of dust at boots — 4 spokes drift around the player
		double angleStep = Math.PI * 2.0 / 4.0;
		double phase01 = (t % 80L) / 80.0;
		double radius = 0.65;
		for (int i = 0; i < 4; i++) {
			double a = angleStep * i + phase01 * Math.PI * 2.0;
			double dx = Math.cos(a) * radius;
			double dz = Math.sin(a) * radius;
			level.sendParticles(DUST_GOLD,
					cx + dx, cy + 0.05, cz + dz,
					1, 0.0, 0.0, 0.0, 0.0);
		}

		// Periodic sparkles at chest height
		if (t % 4L == 0L) {
			level.sendParticles(ParticleTypes.END_ROD,
					cx, cy + 1.0, cz,
					phase == ReinhardPhase.P5 ? 3 : 1,
					0.4, 0.6, 0.4, 0.005);
		}

		// P5 only — upward column of ivory dust + glow flecks
		if (phase == ReinhardPhase.P5 && t % 2L == 0L) {
			level.sendParticles(DUST_IVORY,
					cx, cy + 1.4, cz,
					2, 0.30, 0.6, 0.30, 0.01);
			level.sendParticles(ParticleTypes.GLOW,
					cx, cy + 1.6, cz,
					1, 0.25, 0.5, 0.25, 0.002);
		}
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}

}
