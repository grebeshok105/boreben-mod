package com.vanguard.mod.ability.sword;

import com.example.superheroes.ability.Ability;
import com.example.superheroes.api.AbilityApi;
import com.vanguard.mod.damage.VanguardDamageSources;
import com.vanguard.mod.damage.VanguardDamageTypes;
import com.vanguard.mod.effect.ReinhardWorthyOpponentTracker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Air Slash — short-range spirit cut. The sword carves the air in front of
 * Reinhard for ~9 blocks, hits the first {@code worthy} target on the line,
 * spawns a streak of glow / soul-fire particles. Damage scales with Reid Draw
 * being active.
 */
public final class AirSlashAbility implements Ability {
	private static final int COOLDOWN_TICKS = 24; // ~1.2s
	private static final float REACH = 9f;
	private static final float BASE_DAMAGE = 6f;
	private static final float DRAW_BONUS = 4f;

	@Override
	public ResourceLocation getId() {
		return SwordAbilityIds.AIR_SLASH;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 30f;
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
		Vec3 dir = player.getLookAngle().normalize();
		Vec3 end = origin.add(dir.scale(REACH));

		// Particle streak along the slash path (server-broadcasted so all players see it).
		int steps = 24;
		for (int i = 1; i <= steps; i++) {
			double t = (double) i / steps;
			Vec3 p = origin.add(end.subtract(origin).scale(t));
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.06, 0.06, 0.06, 0.0);
			if (i % 3 == 0) {
				level.sendParticles(ParticleTypes.GLOW, p.x, p.y, p.z, 1, 0.04, 0.04, 0.04, 0.005);
			}
		}

		level.playSound(null, player.blockPosition(),
				SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.1f, 0.9f);

		// Damage check: scan a thin AABB along the slash and hit the closest
		// worthy hostile encountered.
		AABB box = new AABB(origin, end).inflate(0.6);
		List<Entity> hits = level.getEntities(player, box,
				e -> e instanceof LivingEntity && e != player && e.isAlive());
		Entity victim = null;
		double bestDistSq = Double.POSITIVE_INFINITY;
		for (Entity e : hits) {
			if (!ReinhardWorthyOpponentTracker.isWorthy(player, e.getUUID())) continue;
			double d = e.distanceToSqr(player);
			if (d < bestDistSq) {
				bestDistSq = d;
				victim = e;
			}
		}

		if (victim instanceof LivingEntity living) {
			float damage = BASE_DAMAGE + (ReidDrawState.isDrawn(player) ? DRAW_BONUS : 0f);
			living.hurt(VanguardDamageSources.source(level, VanguardDamageTypes.AIR_SLASH, player), damage);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					living.getX(), living.getY() + living.getBbHeight() * 0.5,
					living.getZ(), 24, 0.3, 0.3, 0.3, 0.04);
		}

		SwordCooldowns.start(player, getId(), COOLDOWN_TICKS);
		return true;
	}
}
