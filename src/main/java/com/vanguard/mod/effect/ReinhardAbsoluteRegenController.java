package com.vanguard.mod.effect;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.attachment.ReinhardData;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.hero.ReinhardHero;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Absolute regeneration: while transformed, perpetual Regen II is maintained
 * and every 30 seconds the player gets a +6 instant heal (visualised as a
 * golden flash). Halves cooldown when HP {@literal <}= 30%.
 *
 * <p>If {@code weakened == true} (sword pickup vulnerability from stage 4c),
 * the perpetual effect downgrades to Regen I and instant heal is suspended.
 */
public final class ReinhardAbsoluteRegenController {
	private static final int FULL_INSTANT_HEAL_COOLDOWN = 600;   // 30s
	private static final int EMERGENCY_INSTANT_HEAL_COOLDOWN = 60; // 3s when low HP
	private static final float INSTANT_HEAL_AMOUNT = 6.0f;
	private static final float EMERGENCY_HP_FRACTION = 0.30f;
	private static final int REGEN_REFRESH_INTERVAL = 100; // 5s

	private ReinhardAbsoluteRegenController() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!isReinhard(player)) {
					continue;
				}
				ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
				maintainPerpetualRegen(player, data);
				maybeInstantHeal(player, data);
			}
		});
	}

	private static void maintainPerpetualRegen(ServerPlayer player, ReinhardData data) {
		if (player.tickCount % REGEN_REFRESH_INTERVAL != 0) {
			return;
		}
		int amplifier = data.weakened() ? 0 : 1;
		MobEffectInstance current = player.getEffect(MobEffects.REGENERATION);
		if (current == null
				|| current.getDuration() < REGEN_REFRESH_INTERVAL * 2
				|| current.getAmplifier() != amplifier) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
					-1, amplifier, true, false, true));
		}
	}

	private static void maybeInstantHeal(ServerPlayer player, ReinhardData data) {
		if (data.weakened()) {
			return;
		}
		if (player.getHealth() >= player.getMaxHealth()) {
			return;
		}
		int cooldown = player.getHealth() / Math.max(1f, player.getMaxHealth()) <= EMERGENCY_HP_FRACTION
				? EMERGENCY_INSTANT_HEAL_COOLDOWN
				: FULL_INSTANT_HEAL_COOLDOWN;
		long earliest = data.lastInstantHealTick() < 0 ? data.transformedAtTick() : data.lastInstantHealTick();
		if (player.tickCount < earliest + cooldown) {
			return;
		}
		player.heal(INSTANT_HEAL_AMOUNT);
		player.setAttached(VanguardAttachments.REINHARD_DATA,
				data.withLastInstantHeal(player.tickCount));
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.HEART,
				player.getX(), player.getY() + 1.5, player.getZ(),
				6, 0.4, 0.4, 0.4, 0.05);
		level.sendParticles(ParticleTypes.GLOW,
				player.getX(), player.getY() + 1.0, player.getZ(),
				16, 0.5, 0.7, 0.5, 0.04);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7f, 1.4f);
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}
}
