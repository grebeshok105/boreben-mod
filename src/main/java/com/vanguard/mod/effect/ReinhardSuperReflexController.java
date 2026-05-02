package com.vanguard.mod.effect;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.attachment.ReinhardData;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.hero.ReinhardHero;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Reinhard super-reflexes: 15% chance to dodge incoming hits (no teleport — just
 * cancel + counter-prime). Dodge cooldown 20t. On dodge, the next outgoing
 * attack from Reinhard within 5s deals +200% bonus damage (2x).
 */
public final class ReinhardSuperReflexController {
	private static final int DODGE_COOLDOWN_TICKS = 20;
	private static final int COUNTER_WINDOW_TICKS = 100;
	private static final float COUNTER_MULTIPLIER = 2.0f;
	private static final float BASE_DODGE_CHANCE = 0.15f;

	private ReinhardSuperReflexController() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) {
				return true;
			}
			if (!isReinhard(player)) {
				return true;
			}
			if (source.is(DamageTypes.GENERIC_KILL)
					|| source.is(DamageTypes.FELL_OUT_OF_WORLD)
					|| source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				return true;
			}
			ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
			if (data.weakened()) {
				return true; // dodge disabled when weakened (post-sword-pickup loss).
			}
			if (data.lastDodgeTick() >= 0
					&& player.tickCount < data.lastDodgeTick() + DODGE_COOLDOWN_TICKS) {
				return true;
			}
			float chance = dodgeChanceFor(source);
			if (chance <= 0f) {
				return true;
			}
			if (ThreadLocalRandom.current().nextFloat() >= chance) {
				return true;
			}
			onDodgeSuccess(player, source);
			player.setAttached(VanguardAttachments.REINHARD_DATA,
					data.withLastDodgeTick(player.tickCount)
							.withCounterPrimedUntil(player.tickCount + COUNTER_WINDOW_TICKS));
			return false;
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
				return InteractionResult.PASS;
			}
			if (!isReinhard(sp)) {
				return InteractionResult.PASS;
			}
			ReinhardData data = sp.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
			if (data.counterPrimedUntilTick() < 0
					|| sp.tickCount > data.counterPrimedUntilTick()) {
				return InteractionResult.PASS;
			}
			if (entity instanceof LivingEntity target) {
				float bonus = (float) sp.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
						* (COUNTER_MULTIPLIER - 1f);
				target.hurt(sp.damageSources().playerAttack(sp), bonus);
				ServerLevel level = sp.serverLevel();
				level.sendParticles(ParticleTypes.CRIT,
						target.getX(), target.getY() + target.getBbHeight() * 0.5,
						target.getZ(), 20, 0.4, 0.4, 0.4, 0.2);
				level.playSound(null, target.getX(), target.getY(), target.getZ(),
						SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.4f);
				sp.setAttached(VanguardAttachments.REINHARD_DATA,
						data.withCounterPrimedUntil(-1L));
			}
			return InteractionResult.PASS;
		});
	}

	private static void onDodgeSuccess(ServerPlayer player, DamageSource source) {
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.SWEEP_ATTACK,
				player.getX(), player.getY() + 1.0, player.getZ(),
				4, 0.3, 0.2, 0.3, 0.0);
		level.sendParticles(ParticleTypes.ENCHANT,
				player.getX(), player.getY() + 1.2, player.getZ(),
				16, 0.4, 0.6, 0.4, 0.3);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 0.6f, 1.6f);
		Entity attacker = source.getEntity();
		if (attacker != null) {
			level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
					SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 0.7f, 1.3f);
		}
	}

	private static float dodgeChanceFor(DamageSource source) {
		if (source.is(DamageTypeTags.IS_FALL)) return 0.0f;
		if (source.is(DamageTypeTags.IS_DROWNING)) return 0.0f;
		if (source.is(DamageTypeTags.IS_FREEZING)) return 0.0f;
		if (source.is(DamageTypeTags.IS_FIRE)) return 0.0f;
		if (source.is(DamageTypeTags.IS_EXPLOSION)) return BASE_DODGE_CHANCE * 0.5f;
		return BASE_DODGE_CHANCE;
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}
}
