package com.vanguard.mod.ability.sword;

import com.example.superheroes.ability.Ability;
import com.example.superheroes.api.AbilityApi;
import com.vanguard.mod.damage.VanguardDamageSources;
import com.vanguard.mod.damage.VanguardDamageTypes;
import com.vanguard.mod.effect.SwordWaveProjectiles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Sword Wave — ranged horizontal slash projectile. Travels ~14 blocks straight
 * out of the player's look direction over ~0.7s, hitting all (worthy) enemies
 * along the path. Implemented as a software-driven moving AABB rather than
 * a real Entity to keep the assets footprint small.
 */
public final class SwordWaveAbility implements Ability {
	private static final int COOLDOWN_TICKS = 100; // 5s
	private static final float DAMAGE = 12f;

	@Override
	public ResourceLocation getId() {
		return SwordAbilityIds.SWORD_WAVE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 60f;
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

		ServerLevel level = player.serverLevel();
		Vec3 origin = player.getEyePosition().subtract(0, 0.4, 0);
		Vec3 dir = player.getLookAngle().normalize();

		SwordWaveProjectiles.spawn(player, origin, dir);

		level.playSound(null, player.blockPosition(),
				SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.2f, 0.85f);

		SwordCooldowns.start(player, getId(), COOLDOWN_TICKS);
		return true;
	}

	/** Called from {@link SwordWaveProjectiles} on hit. */
	public static void onHit(ServerPlayer caster, LivingEntity victim) {
		ServerLevel level = caster.serverLevel();
		victim.hurt(VanguardDamageSources.source(level, VanguardDamageTypes.SWORD_WAVE, caster), DAMAGE);
		Vec3 push = victim.position().subtract(caster.position()).normalize();
		victim.setDeltaMovement(victim.getDeltaMovement().add(push.x * 0.3, 0.15, push.z * 0.3));
		victim.hurtMarked = true;
	}
}
