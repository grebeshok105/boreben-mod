package com.vanguard.mod.effect;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.attachment.ReinhardData;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.hero.ReinhardAttributes;
import com.vanguard.mod.hero.ReinhardHero;
import com.vanguard.mod.hero.ReinhardPhase;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks cumulative damage incoming to Reinhard (pre-mitigation, since
 * fabric-entity-events-v1 1.6.x has no AFTER_DAMAGE event in MC 1.21) and
 * progresses him through phases P1..P5. Phase modifiers apply on transition;
 * reset on death / untransform.
 *
 * <p>Must be initialised AFTER {@link ReinhardSuperReflexController} so that
 * dodge cancellations short-circuit ALLOW_DAMAGE before this counter runs.
 */
public final class ReinhardPhaseController {
	private static final Map<UUID, ReinhardPhase> CURRENT_PHASE = new HashMap<>();

	private ReinhardPhaseController() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player && isReinhard(player) && amount > 0f) {
				ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
				float newTotal = data.damageTaken() + amount;
				player.setAttached(VanguardAttachments.REINHARD_DATA,
						data.withDamageTaken(newTotal));
				tickPhase(player, newTotal);
			}
			return true;
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer player) {
				CURRENT_PHASE.remove(player.getUUID());
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				CURRENT_PHASE.remove(handler.getPlayer().getUUID()));

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> CURRENT_PHASE.clear());

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
				if (!isReinhard(player)) {
					if (CURRENT_PHASE.remove(player.getUUID()) != null) {
						ReinhardAttributes.applyPhaseBonuses(player, ReinhardPhase.P1);
					}
					continue;
				}
				if (data.transformedAtTick() == 0L) {
					player.setAttached(VanguardAttachments.REINHARD_DATA,
							data.withTransformedAt(player.tickCount));
				}
				tickPhase(player, data.damageTaken());
			}
		});
	}

	private static void tickPhase(ServerPlayer player, float damageTaken) {
		ReinhardPhase next = ReinhardPhase.forDamageTaken(damageTaken);
		ReinhardPhase prev = CURRENT_PHASE.get(player.getUUID());
		if (next == prev) {
			return;
		}
		CURRENT_PHASE.put(player.getUUID(), next);
		ReinhardAttributes.applyPhaseBonuses(player, next);
		if (prev != null && next.ordinal() > prev.ordinal()) {
			announcePhaseUp(player, next);
		}
	}

	private static void announcePhaseUp(ServerPlayer player, ReinhardPhase phase) {
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY() + 1.0, player.getZ(),
				40, 0.6, 1.0, 0.6, 0.06);
		level.sendParticles(ParticleTypes.GLOW,
				player.getX(), player.getY() + 1.0, player.getZ(),
				24, 0.5, 1.0, 0.5, 0.04);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS,
				0.9f, 0.7f + 0.1f * phase.index());
	}

	public static ReinhardPhase getCurrentPhase(ServerPlayer player) {
		return CURRENT_PHASE.getOrDefault(player.getUUID(), ReinhardPhase.P1);
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}
}
