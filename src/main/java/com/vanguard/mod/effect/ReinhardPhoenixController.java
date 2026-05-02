package com.vanguard.mod.effect;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.attachment.ReinhardData;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.hero.ReinhardHero;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.vanguard.mod.network.PhoenixResurrectS2CPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Phoenix resurrection. On lethal damage, if {@code !data.phoenixUsed}:
 * cancels death, restores HP to 50 %, sets {@code phoenixUsed = true}, runs a
 * downed→revive animation (broadcast S2C payload), grants 5 s of vulnerability
 * window for {@link com.vanguard.mod.effect.ReinhardWorthyOpponentTracker}-tagged
 * opponents to grab the dropped sword (sword-drop logic lands in stage 4d —
 * here we only set the flag and emit the broadcast).
 */
public final class ReinhardPhoenixController {
	private static final Set<UUID> CURRENTLY_RISING = new HashSet<>();

	private ReinhardPhoenixController() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			if (!isReinhard(player)) return true;
			ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
			if (data.phoenixUsed()) return true;
			triggerPhoenix(player, data);
			return false;
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				CURRENTLY_RISING.remove(handler.getPlayer().getUUID()));

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> CURRENTLY_RISING.clear());
	}

	private static void triggerPhoenix(ServerPlayer player, ReinhardData data) {
		player.setHealth(player.getMaxHealth() * 0.5f);
		player.removeAllEffects();
		// Re-establish absolute-regen base effects (controllers will refresh on next tick).
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 1, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, true, false, true));
		player.setRemainingFireTicks(0);

		player.setAttached(VanguardAttachments.REINHARD_DATA, data.withPhoenixUsed(true));
		CURRENTLY_RISING.add(player.getUUID());

		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.FLAME,
				player.getX(), player.getY() + 0.2, player.getZ(),
				200, 0.8, 1.4, 0.8, 0.3);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
				player.getX(), player.getY() + 1.0, player.getZ(),
				120, 0.6, 1.2, 0.6, 0.2);
		level.sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY() + 1.0, player.getZ(),
				60, 0.5, 1.0, 0.5, 0.10);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 0.7f);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.4f);

		PhoenixResurrectS2CPayload payload =
				new PhoenixResurrectS2CPayload(player.getUUID(), player.getX(), player.getY(), player.getZ());
		ServerPlayNetworking.send(player, payload);
		for (ServerPlayer observer : PlayerLookup.tracking(player)) {
			ServerPlayNetworking.send(observer, payload);
		}
	}

	public static boolean isCurrentlyRising(UUID playerId) {
		return CURRENTLY_RISING.contains(playerId);
	}

	public static void clearRising(UUID playerId) {
		CURRENTLY_RISING.remove(playerId);
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}
}
