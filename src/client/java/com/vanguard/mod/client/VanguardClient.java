package com.vanguard.mod.client;

import com.vanguard.mod.client.hud.ReinhardStatusHud;
import com.vanguard.mod.client.hud.WishRadialHud;
import com.vanguard.mod.client.input.SwordKeyDispatcher;
import com.vanguard.mod.client.input.VanguardKeys;
import com.vanguard.mod.client.render.PhoenixVfx;
import com.vanguard.mod.client.render.WorthyMarkerRenderer;
import com.vanguard.mod.client.render.item.DragonSwordReidRenderProvider;
import com.vanguard.mod.item.VanguardItems;
import com.vanguard.mod.client.state.ClientReinhardPhaseState;
import com.vanguard.mod.client.state.ClientWishesState;
import com.vanguard.mod.client.state.ClientWorthyMarks;
import com.vanguard.mod.network.PhoenixResurrectS2CPayload;
import com.vanguard.mod.network.ReinhardPhaseSyncS2CPayload;
import com.vanguard.mod.network.WishesStateS2CPayload;
import com.vanguard.mod.network.WorthyMarksS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.HashSet;

public final class VanguardClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VanguardKeys.init();

		VanguardItems.DRAGON_SWORD_REID.setRenderProviderFactory(DragonSwordReidRenderProvider::new);

		ClientPlayNetworking.registerGlobalReceiver(WorthyMarksS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ClientWorthyMarks.update(new HashSet<>(payload.worthyIds()))));

		ClientPlayNetworking.registerGlobalReceiver(WishesStateS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ClientWishesState.update(
						payload.recentSources(), payload.adaptations(), payload.wishesUsed())));

		ClientPlayNetworking.registerGlobalReceiver(PhoenixResurrectS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> PhoenixVfx.burst(payload.x(), payload.y(), payload.z())));

		ClientPlayNetworking.registerGlobalReceiver(ReinhardPhaseSyncS2CPayload.TYPE, (payload, context) ->
				context.client().execute(() -> ClientReinhardPhaseState.update(
						payload.phaseIndex(), payload.damageTaken())));

		ClientTickEvents.END_CLIENT_TICK.register(WishRadialHud::clientTick);
		ClientTickEvents.END_CLIENT_TICK.register(SwordKeyDispatcher::clientTick);

		HudRenderCallback.EVENT.register(ReinhardStatusHud::render);
		HudRenderCallback.EVENT.register(WishRadialHud::render);

		WorldRenderEvents.AFTER_ENTITIES.register(WorthyMarkerRenderer::render);
	}
}
