package com.vanguard.mod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Client VFX burst when a phoenix-resurrect S2C arrives. Emits flame + soul
 * particles around the resurrect position and plays a wither-spawn cue.
 *
 * <p>This is just the "rising flame" visual — the actual death cancellation
 * happens server-side in {@link com.vanguard.mod.effect.ReinhardPhoenixController}.
 */
public final class PhoenixVfx {
	private PhoenixVfx() {
	}

	public static void burst(double x, double y, double z) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		for (int i = 0; i < 200; i++) {
			double ox = (mc.level.random.nextDouble() - 0.5) * 1.6;
			double oy = mc.level.random.nextDouble() * 2.4;
			double oz = (mc.level.random.nextDouble() - 0.5) * 1.6;
			mc.level.addParticle(ParticleTypes.FLAME,
					x + ox, y + oy, z + oz,
					(mc.level.random.nextDouble() - 0.5) * 0.2,
					mc.level.random.nextDouble() * 0.3,
					(mc.level.random.nextDouble() - 0.5) * 0.2);
		}
		for (int i = 0; i < 80; i++) {
			double ox = (mc.level.random.nextDouble() - 0.5) * 1.0;
			double oy = mc.level.random.nextDouble() * 2.0;
			double oz = (mc.level.random.nextDouble() - 0.5) * 1.0;
			mc.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
					x + ox, y + oy, z + oz, 0.0, 0.1, 0.0);
		}
		for (int i = 0; i < 40; i++) {
			double ox = (mc.level.random.nextDouble() - 0.5) * 0.8;
			double oy = mc.level.random.nextDouble() * 1.8;
			double oz = (mc.level.random.nextDouble() - 0.5) * 0.8;
			mc.level.addParticle(ParticleTypes.END_ROD,
					x + ox, y + oy, z + oz, 0.0, 0.05, 0.0);
		}
		mc.level.playLocalSound(x, y, z, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 0.7f, false);
		mc.level.playLocalSound(x, y, z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.4f, false);
	}
}
