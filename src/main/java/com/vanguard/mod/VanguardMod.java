package com.vanguard.mod;

import com.example.superheroes.api.CreativeTabIds;
import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.effect.ReinhardAbsoluteRegenController;
import com.vanguard.mod.effect.ReinhardPhaseController;
import com.vanguard.mod.effect.ReinhardSuperReflexController;
import com.vanguard.mod.hero.ReinhardHero;
import com.vanguard.mod.item.VanguardItems;
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

		// Order matters: dodge runs before damage counting so dodged hits don't
		// pump the phase tracker.
		ReinhardSuperReflexController.init();
		ReinhardPhaseController.init();
		ReinhardAbsoluteRegenController.init();

		ItemGroupEvents.modifyEntriesEvent(CreativeTabIds.SUPERHEROES_TAB)
				.register(entries -> entries.accept(VanguardItems.REINHARD_SUIT));

		LOGGER.info("Vanguard addon initialized — Reinhard hero registered");
	}
}
