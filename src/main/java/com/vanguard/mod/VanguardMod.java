package com.vanguard.mod;

import com.example.superheroes.api.AbilityApi;
import com.example.superheroes.api.CreativeTabIds;
import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.ability.sword.AirSlashAbility;
import com.vanguard.mod.ability.sword.CounterAutoRiposteAbility;
import com.vanguard.mod.ability.sword.HeavensSwordStrikeAbility;
import com.vanguard.mod.ability.sword.ReidDrawAbility;
import com.vanguard.mod.ability.sword.ReidDrawState;
import com.vanguard.mod.ability.sword.SkyVaultJumpAbility;
import com.vanguard.mod.ability.sword.SwordCooldowns;
import com.vanguard.mod.ability.sword.SwordWaveAbility;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.effect.HeavensSwordStrikeManager;
import com.vanguard.mod.effect.ReidSwordWorthyGate;
import com.vanguard.mod.effect.ReinhardAbsoluteRegenController;
import com.vanguard.mod.effect.ReinhardPhaseController;
import com.vanguard.mod.effect.ReinhardPhoenixController;
import com.vanguard.mod.effect.ReinhardSuperReflexController;
import com.vanguard.mod.effect.ReinhardWishController;
import com.vanguard.mod.effect.ReinhardWorthyOpponentTracker;
import com.vanguard.mod.effect.SkyVaultLandingTracker;
import com.vanguard.mod.effect.SwordWaveProjectiles;
import com.vanguard.mod.effect.WishCapVulnerabilityHook;
import com.vanguard.mod.hero.ReinhardHero;
import com.vanguard.mod.item.VanguardItems;
import com.vanguard.mod.network.VanguardNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VanguardMod implements ModInitializer {
	public static final String MOD_ID = "vanguard";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Vanguard addon initializing — depends on superheroes-2.3.0+");

		VanguardAttachments.init();
		VanguardItems.init();

		HeroApi.register(new ReinhardHero());

		AbilityApi.register(new ReidDrawAbility());
		AbilityApi.register(new AirSlashAbility());
		AbilityApi.register(new SkyVaultJumpAbility());
		AbilityApi.register(new HeavensSwordStrikeAbility());
		AbilityApi.register(new SwordWaveAbility());
		AbilityApi.register(new CounterAutoRiposteAbility());

		// Order matters: dodge runs before damage counting so dodged hits don't
		// pump the phase tracker. Worthy and wish trackers also subscribe to
		// ALLOW_DAMAGE — wish runs first so adapted hits never feed the worthy
		// counter, and worthy runs before phase so a successful wish-block
		// doesn't bump the phase threshold. The wish-cap vulnerability runs
		// LAST in the Reinhard-as-victim chain so all earlier filters resolve
		// against the original amount before the +30% amplification kicks in.
		// The sword worthy-gate is unrelated (Reinhard-as-attacker filter).
		ReinhardSuperReflexController.init();
		ReinhardWishController.init();
		ReinhardWorthyOpponentTracker.init();
		ReinhardPhaseController.init();
		ReinhardAbsoluteRegenController.init();
		ReinhardPhoenixController.init();
		WishCapVulnerabilityHook.init();
		ReidSwordWorthyGate.init();
		ReidDrawState.init();
		SwordCooldowns.init();
		SkyVaultLandingTracker.init();
		HeavensSwordStrikeManager.init();
		SwordWaveProjectiles.init();
		VanguardNetworking.init();

		ItemGroupEvents.modifyEntriesEvent(CreativeTabIds.SUPERHEROES_TAB)
				.register(entries -> {
					entries.accept(VanguardItems.REINHARD_SUIT);
					entries.accept(VanguardItems.DRAGON_SWORD_REID);
				});

		LOGGER.info("Vanguard addon initialized — Reinhard hero registered");
	}
}
