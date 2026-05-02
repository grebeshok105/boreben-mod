package com.vanguard.mod.effect;

import com.vanguard.mod.ability.sword.AbilityHelpers;
import com.vanguard.mod.ability.sword.ReidDrawState;
import com.vanguard.mod.item.sword.DragonSwordReidItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Filters melee-attack damage events: the Reid sword only inflicts damage
 * against {@code worthy} opponents and against non-player creatures (mobs,
 * bosses). Hits against unworthy player targets are nullified and a soft VFX
 * cue plays so the attacker knows the strike was rejected.
 *
 * <p>Also applies the Reid Draw bonus damage envelope when the wielder has the
 * draw active.
 */
public final class ReidSwordWorthyGate {
	/** Multiplier applied to outgoing damage when Reid Draw is active. */
	private static final float DRAW_MULTIPLIER = 1.35f;

	private ReidSwordWorthyGate() {}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(source.getEntity() instanceof ServerPlayer attacker)) return true;
			if (!AbilityHelpers.isReinhard(attacker)) return true;
			ItemStack mainHand = attacker.getItemInHand(InteractionHand.MAIN_HAND);
			if (!(mainHand.getItem() instanceof DragonSwordReidItem)) return true;

			// Always allow damage to non-player living entities (mobs/bosses)
			// — only PvP needs the worthy gate. The user-facing rule is "only
			// against worthy *players*", which covers dueling other heroes.
			if (entity instanceof ServerPlayer victimPlayer) {
				if (!ReinhardWorthyOpponentTracker.isWorthy(attacker, victimPlayer.getUUID())) {
					rejectHit(attacker, victimPlayer);
					return false;
				}
			}

			return true;
		});
	}

	private static void rejectHit(ServerPlayer attacker, LivingEntity victim) {
		ServerLevel level = attacker.serverLevel();
		level.sendParticles(ParticleTypes.SMOKE,
				victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
				6, 0.2, 0.2, 0.2, 0.0);
		level.playSound(null, victim.blockPosition(),
				SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.6f, 1.6f);
	}

	public static float scaleForDraw(ServerPlayer attacker, float amount) {
		return ReidDrawState.isDrawn(attacker) ? amount * DRAW_MULTIPLIER : amount;
	}
}
