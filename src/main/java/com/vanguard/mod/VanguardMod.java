package com.vanguard.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.superheroes.api.CreativeTabIds;
import com.vanguard.mod.item.VanguardItems;

public final class VanguardMod implements ModInitializer {
	public static final String MOD_ID = "vanguard";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Vanguard addon initializing — depends on superheroes-2.3.0+");

		VanguardItems.init();

		ItemGroupEvents.modifyEntriesEvent(CreativeTabIds.SUPERHEROES_TAB)
				.register(entries -> {
					entries.accept(VanguardItems.REINHARD_SUIT);
				});

		LOGGER.info("Vanguard addon initialized");
	}
}
