package com.vanguard.mod.item;

import com.vanguard.mod.VanguardMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public final class VanguardItems {
	public static final Item REINHARD_SUIT = register(
			"reinhard_suit",
			new Item(new Item.Properties().stacksTo(1))
	);

	private VanguardItems() {
	}

	public static void init() {
	}

	private static <T extends Item> T register(String path, T item) {
		return Registry.register(BuiltInRegistries.ITEM, VanguardMod.id(path), item);
	}
}
