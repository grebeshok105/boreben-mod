package com.vanguard.mod.client.input;

import com.vanguard.mod.ability.sword.SwordAbilityIds;
import com.vanguard.mod.network.SwordAbilityActivateC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Drains buffered key-presses for the sword keybinds and forwards each as a
 * {@link SwordAbilityActivateC2SPayload}. Toggle keys (Reid Draw) and active
 * keys are uniformly handled — server-side {@code Ability.tryActivate} is
 * idempotent: it'll deactivate a toggle on second press.
 */
public final class SwordKeyDispatcher {
	private SwordKeyDispatcher() {}

	public static void clientTick(Minecraft client) {
		if (client.player == null) return;
		drain(VanguardKeys.AIR_SLASH, SwordAbilityIds.AIR_SLASH);
		drain(VanguardKeys.REID_DRAW, SwordAbilityIds.REID_DRAW);
		drain(VanguardKeys.SKY_VAULT, SwordAbilityIds.SKY_VAULT);
	}

	private static void drain(KeyMapping key, ResourceLocation abilityId) {
		while (key.consumeClick()) {
			ClientPlayNetworking.send(new SwordAbilityActivateC2SPayload(abilityId));
		}
	}
}
