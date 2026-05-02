package com.vanguard.mod.effect;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.attachment.ReinhardData;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.hero.ReinhardHero;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wishes / divine-protection adaptation.
 *
 * <p>Tracks the last 5 distinct damage-type {@code ResourceLocation}s that hit
 * Reinhard (most recent first). Reinhard can spend up to 3 wishes total — each
 * wish converts one tracked damage type into a permanent immunity for the
 * remainder of this transformation.
 *
 * <p>After 3 wishes are spent, {@code ReinhardData.wishesUsed == 3}, the
 * absolute regeneration controller and other systems can read this and apply
 * the +30% incoming damage penalty (handled below in {@link #amplifyAfterWishCap}).
 */
public final class ReinhardWishController {
	public static final int MAX_TRACKED_SOURCES = 5;
	public static final int MAX_WISHES = 3;
	public static final float WISH_DAMAGE_AMP = 1.30f;

	private static final Map<UUID, Deque<ResourceLocation>> RECENT_SOURCES = new HashMap<>();
	private static final Map<UUID, Set<ResourceLocation>> ADAPTATIONS = new HashMap<>();

	private ReinhardWishController() {
	}

	public static void init() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player) || !isReinhard(player) || amount <= 0f) {
				return true;
			}
			ResourceKey<DamageType> typeKey = source.typeHolder().unwrapKey().orElse(null);
			if (typeKey == null) {
				return true;
			}
			ResourceLocation typeId = typeKey.location();
			Set<ResourceLocation> adapted = ADAPTATIONS.get(player.getUUID());
			if (adapted != null && adapted.contains(typeId)) {
				flashImmunity(player, typeId);
				return false;
			}
			pushRecentSource(player.getUUID(), typeId);
			return true;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<UUID> it = RECENT_SOURCES.keySet().iterator();
			while (it.hasNext()) {
				UUID id = it.next();
				ServerPlayer player = server.getPlayerList().getPlayer(id);
				if (player == null || !isReinhard(player)) {
					it.remove();
					ADAPTATIONS.remove(id);
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			RECENT_SOURCES.remove(handler.getPlayer().getUUID());
			ADAPTATIONS.remove(handler.getPlayer().getUUID());
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			RECENT_SOURCES.clear();
			ADAPTATIONS.clear();
		});
	}

	public static void resetForTransform(UUID playerId) {
		RECENT_SOURCES.remove(playerId);
		ADAPTATIONS.remove(playerId);
	}

	private static void pushRecentSource(UUID playerId, ResourceLocation typeId) {
		Deque<ResourceLocation> deque = RECENT_SOURCES.computeIfAbsent(playerId, k -> new ArrayDeque<>());
		deque.remove(typeId);
		deque.addFirst(typeId);
		while (deque.size() > MAX_TRACKED_SOURCES) {
			deque.removeLast();
		}
	}

	public static List<ResourceLocation> getRecentSources(UUID playerId) {
		Deque<ResourceLocation> deque = RECENT_SOURCES.get(playerId);
		if (deque == null) return List.of();
		return deque.stream().collect(Collectors.toUnmodifiableList());
	}

	public static Set<ResourceLocation> getAdaptations(UUID playerId) {
		return ADAPTATIONS.getOrDefault(playerId, Set.of());
	}

	/**
	 * Activate a wish, converting recent-source #{@code index} into a permanent
	 * immunity for this transformation. Returns true if successful.
	 */
	public static boolean activateWish(ServerPlayer player, int index) {
		ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
		if (data.wishesUsed() >= MAX_WISHES) return false;
		List<ResourceLocation> recent = getRecentSources(player.getUUID());
		if (index < 0 || index >= recent.size()) return false;
		ResourceLocation chosen = recent.get(index);
		Set<ResourceLocation> set = ADAPTATIONS.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
		if (!set.add(chosen)) {
			return false; // already adapted
		}
		player.setAttached(VanguardAttachments.REINHARD_DATA, data.withWishesUsed(data.wishesUsed() + 1));
		announceWish(player, chosen);
		return true;
	}

	/**
	 * Hook called by absolute-regen / phase / sword controllers to inflate
	 * incoming damage by +30 % once all 3 wishes are spent.
	 */
	public static float amplifyAfterWishCap(ServerPlayer player, float amount) {
		ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
		return data.wishesUsed() >= MAX_WISHES ? amount * WISH_DAMAGE_AMP : amount;
	}

	private static void flashImmunity(ServerPlayer player, ResourceLocation typeId) {
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.ENCHANT,
				player.getX(), player.getY() + 1.0, player.getZ(),
				12, 0.4, 0.6, 0.4, 0.5);
		level.sendParticles(ParticleTypes.GLOW,
				player.getX(), player.getY() + 1.0, player.getZ(),
				6, 0.3, 0.5, 0.3, 0.0);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6f, 1.8f);
	}

	private static void announceWish(ServerPlayer player, ResourceLocation chosen) {
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY() + 1.5, player.getZ(),
				50, 0.7, 1.2, 0.7, 0.05);
		level.sendParticles(ParticleTypes.GLOW,
				player.getX(), player.getY() + 1.0, player.getZ(),
				30, 0.5, 0.8, 0.5, 0.04);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0f, 1.5f);
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}

	@SuppressWarnings("unused")
	private static ResourceKey<DamageType> resolveTypeKey(ResourceLocation id) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, id);
	}
}
