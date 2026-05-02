package com.vanguard.mod.ability.sword;

import com.example.superheroes.ability.Ability;
import com.example.superheroes.api.AbilityApi;
import com.vanguard.mod.effect.SkyVaultLandingTracker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Sky Vault Jump — high vertical leap (~12 blocks). Reinhard receives an
 * upward velocity impulse and is registered with {@link SkyVaultLandingTracker}
 * so the landing impact (only this leap, not other falls) produces a knockback
 * shockwave. Fall damage is already cancelled by {@link
 * com.vanguard.mod.hero.ReinhardHero#cancelsFallDamage}.
 */
public final class SkyVaultJumpAbility implements Ability {
	private static final int COOLDOWN_TICKS = 80; // 4s
	/** Vertical impulse strength — empirically ~12 blocks of apex. */
	private static final double UPWARD_VELOCITY = 1.85;

	@Override
	public ResourceLocation getId() {
		return SwordAbilityIds.SKY_VAULT;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 40f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return AbilityHelpers.canActivateSwordAbility(player)
				&& SwordCooldowns.isReady(player, getId());
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		if (!canActivate(player)) return false;
		if (!AbilityApi.tryConsume(player, getId(), costOnActivate())) return false;

		Vec3 v = player.getDeltaMovement();
		player.setDeltaMovement(v.x * 1.1, UPWARD_VELOCITY, v.z * 1.1);
		player.hurtMarked = true;

		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.CLOUD,
				player.getX(), player.getY() + 0.05, player.getZ(),
				24, 0.6, 0.05, 0.6, 0.08);
		level.sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY(), player.getZ(),
				16, 0.4, 0.2, 0.4, 0.08);
		level.playSound(null, player.blockPosition(),
				SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.7f, 1.4f);

		SkyVaultLandingTracker.markAirborne(player);
		SwordCooldowns.start(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
