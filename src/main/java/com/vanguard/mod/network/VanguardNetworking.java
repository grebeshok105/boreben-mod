package com.vanguard.mod.network;

import com.example.superheroes.api.AbilityApi;
import com.example.superheroes.api.HeroApi;
import com.example.superheroes.ability.Ability;
import com.vanguard.mod.attachment.ReinhardData;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.effect.ReinhardWishController;
import com.vanguard.mod.effect.ReinhardWorthyOpponentTracker;
import com.vanguard.mod.hero.ReinhardHero;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Vanguard payload registry + tick-based sync from server tracker state to
 * caster client. Worthy-marker and wishes payloads are sent ONLY to the
 * Reinhard player themselves (caster-only visibility).
 */
public final class VanguardNetworking {
	private static final int SYNC_INTERVAL_TICKS = 5;
	private static final Map<UUID, WishSyncSnapshot> LAST_WISH_SNAPSHOT = new HashMap<>();

	private VanguardNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.playS2C().register(PhoenixResurrectS2CPayload.TYPE, PhoenixResurrectS2CPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(WorthyMarksS2CPayload.TYPE, WorthyMarksS2CPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(WishesStateS2CPayload.TYPE, WishesStateS2CPayload.STREAM_CODEC);

		PayloadTypeRegistry.playC2S().register(UseWishC2SPayload.TYPE, UseWishC2SPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SwordAbilityActivateC2SPayload.TYPE, SwordAbilityActivateC2SPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(UseWishC2SPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			int idx = payload.index();
			context.server().execute(() -> ReinhardWishController.activateWish(player, idx));
		});

		ServerPlayNetworking.registerGlobalReceiver(SwordAbilityActivateC2SPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ResourceLocation abilityId = payload.abilityId();
			context.server().execute(() -> {
				Ability ability = AbilityApi.get(abilityId);
				if (ability == null) return;
				ability.tryActivate(player);
			});
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % SYNC_INTERVAL_TICKS != 0) return;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!isReinhard(player)) {
					if (LAST_WISH_SNAPSHOT.remove(player.getUUID()) != null) {
						sendEmptyState(player);
					}
					continue;
				}
				syncWorthyMarks(player);
				syncWishesState(player);
			}
		});
	}

	private static void syncWorthyMarks(ServerPlayer player) {
		Set<UUID> set = ReinhardWorthyOpponentTracker.getWorthyIds(player);
		if (!ReinhardWorthyOpponentTracker.tookOver(player, set)) return;
		ReinhardWorthyOpponentTracker.recordBroadcast(player, set);
		ServerPlayNetworking.send(player, new WorthyMarksS2CPayload(new ArrayList<>(set)));
	}

	private static void syncWishesState(ServerPlayer player) {
		List<ResourceLocation> recent = ReinhardWishController.getRecentSources(player.getUUID());
		Set<ResourceLocation> adapted = ReinhardWishController.getAdaptations(player.getUUID());
		ReinhardData data = player.getAttachedOrCreate(VanguardAttachments.REINHARD_DATA);
		WishSyncSnapshot snap = new WishSyncSnapshot(new ArrayList<>(recent), new HashSet<>(adapted), data.wishesUsed());
		WishSyncSnapshot prev = LAST_WISH_SNAPSHOT.get(player.getUUID());
		if (snap.equals(prev)) return;
		LAST_WISH_SNAPSHOT.put(player.getUUID(), snap);
		ServerPlayNetworking.send(player, new WishesStateS2CPayload(
				snap.recent, new ArrayList<>(snap.adapted), snap.wishesUsed));
	}

	private static void sendEmptyState(ServerPlayer player) {
		ServerPlayNetworking.send(player, new WishesStateS2CPayload(List.of(), List.of(), 0));
		ServerPlayNetworking.send(player, new WorthyMarksS2CPayload(List.of()));
	}

	private static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(ReinhardHero.ID::equals)
				.orElse(false);
	}

	private record WishSyncSnapshot(List<ResourceLocation> recent, Set<ResourceLocation> adapted, int wishesUsed) {
	}
}
