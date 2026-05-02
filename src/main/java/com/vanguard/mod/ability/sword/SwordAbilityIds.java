package com.vanguard.mod.ability.sword;

import com.vanguard.mod.VanguardMod;
import net.minecraft.resources.ResourceLocation;

/** Resource locations for sword-bound abilities. Matched by both the C2S
 * activation packet and {@link com.example.superheroes.api.AbilityApi}. */
public final class SwordAbilityIds {
	public static final ResourceLocation REID_DRAW = VanguardMod.id("reid_draw");
	public static final ResourceLocation AIR_SLASH = VanguardMod.id("air_slash");
	public static final ResourceLocation SKY_VAULT = VanguardMod.id("sky_vault_jump");
	public static final ResourceLocation HEAVENS_SWORD_STRIKE = VanguardMod.id("heavens_sword_strike");
	public static final ResourceLocation SWORD_WAVE = VanguardMod.id("sword_wave");
	public static final ResourceLocation COUNTER_RIPOSTE = VanguardMod.id("counter_riposte");

	private SwordAbilityIds() {}
}
