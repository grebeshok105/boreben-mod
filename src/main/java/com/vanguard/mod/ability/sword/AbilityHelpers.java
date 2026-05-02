package com.vanguard.mod.ability.sword;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.hero.ReinhardHero;
import com.vanguard.mod.item.sword.DragonSwordReidItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Common predicates used by sword abilities. */
public final class AbilityHelpers {
	private AbilityHelpers() {}

	/** True when the player is currently transformed as Reinhard. */
	public static boolean isReinhard(ServerPlayer player) {
		return HeroApi.getCurrentHeroId(player)
				.map(id -> id.equals(ReinhardHero.ID))
				.orElse(false);
	}

	/** True when the player holds a {@link DragonSwordReidItem} in either hand. */
	public static boolean holdsReid(ServerPlayer player) {
		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (main.getItem() instanceof DragonSwordReidItem) return true;
		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
		return off.getItem() instanceof DragonSwordReidItem;
	}

	/** Activation pre-check used by all sword abilities. */
	public static boolean canActivateSwordAbility(ServerPlayer player) {
		return isReinhard(player) && holdsReid(player);
	}
}
