package com.vanguard.mod.ability.sword;

import com.example.superheroes.ability.Ability;
import com.example.superheroes.api.AbilityApi;
import com.vanguard.mod.damage.VanguardDamageSources;
import com.vanguard.mod.damage.VanguardDamageTypes;
import com.vanguard.mod.effect.HeavensSwordStrikeManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;

import java.util.List;

/**
 * Heaven's Sword Strike — slow descending blade animation à la hammer slam.
 *
 * <p>The animation is "spawned" at the targeted point (where Reinhard is
 * looking, raycast onto blocks) and takes ~1.5s to fall. During that time a
 * vertical particle column climbs/descends to telegraph the impact area.
 * On impact, the AOE deals heavy damage and small knock-up. Cooldown 24s.
 *
 * <p>The actual delayed-impact bookkeeping lives in
 * {@link HeavensSwordStrikeManager}; this class just stamps the strike.
 */
public final class HeavensSwordStrikeAbility implements Ability {
	private static final int COOLDOWN_TICKS = 480; // 24s
	private static final int FALL_TICKS = 30; // 1.5s telegraph
	private static final double TARGET_RAYCAST_RANGE = 24.0;
	private static final double IMPACT_RADIUS = 4.5;
	private static final float IMPACT_DAMAGE = 18f;

	@Override
	public ResourceLocation getId() {
		return SwordAbilityIds.HEAVENS_SWORD_STRIKE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 90f;
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
		Vec3 origin = player.getEyePosition();
		Vec3 dir = player.getLookAngle();
		Vec3 end = origin.add(dir.scale(TARGET_RAYCAST_RANGE));
		HitResult hit = level.clip(new ClipContext(
				origin, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		Vec3 target;
		if (hit.getType() == HitResult.Type.BLOCK) {
			BlockHitResult br = (BlockHitResult) hit;
			target = br.getLocation();
		} else {
			target = end;
		}

		HeavensSwordStrikeManager.schedule(player, target, FALL_TICKS, IMPACT_RADIUS, IMPACT_DAMAGE);

		level.playSound(null, player.blockPosition(),
				SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5f, 0.6f);

		SwordCooldowns.start(player, getId(), COOLDOWN_TICKS);
		return true;
	}

	/** Runs the impact AoE — invoked from {@link HeavensSwordStrikeManager}. */
	public static void detonate(ServerPlayer caster, Vec3 target, double radius, float damage) {
		ServerLevel level = caster.serverLevel();
		level.sendParticles(ParticleTypes.EXPLOSION,
				target.x, target.y, target.z, 1, 0.0, 0.0, 0.0, 0.0);
		level.sendParticles(ParticleTypes.FLAME,
				target.x, target.y + 0.2, target.z, 80, radius * 0.6, 0.4, radius * 0.6, 0.05);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				target.x, target.y + 0.4, target.z, 40, radius * 0.5, 0.3, radius * 0.5, 0.06);
		level.playSound(null, target.x, target.y, target.z,
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6f, 0.7f);
		level.playSound(null, target.x, target.y, target.z,
				SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.4f);

		AABB box = new AABB(target, target).inflate(radius);
		List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e != caster && e.isAlive());
		for (LivingEntity living : hits) {
			double dist = living.position().distanceTo(target);
			float scaled = (float) Math.max(damage * 0.4, damage * (1.0 - dist / radius));
			living.hurt(VanguardDamageSources.source(level, VanguardDamageTypes.HEAVENS_SWORD_STRIKE, caster), scaled);
			Vec3 push = living.position().subtract(target).normalize();
			living.setDeltaMovement(living.getDeltaMovement().add(push.x * 0.4, 0.6, push.z * 0.4));
			living.hurtMarked = true;
		}
	}
}
