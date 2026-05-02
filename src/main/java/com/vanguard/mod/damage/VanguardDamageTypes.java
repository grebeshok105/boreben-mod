package com.vanguard.mod.damage;

import com.vanguard.mod.VanguardMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * Registry of {@link ResourceKey}s for all Vanguard custom damage types. The
 * actual JSON definitions live under
 * {@code src/main/resources/data/vanguard/damage_type/}.
 *
 * <p>Each ability that deals damage uses its OWN damage type so external mods
 * (notably Doomsday from the base superheroes mod) can adapt to specific
 * Reinhard abilities individually instead of flagging everything as
 * {@code minecraft:generic}.
 */
public final class VanguardDamageTypes {
	public static final ResourceKey<DamageType> REID_MELEE = key("reid_melee");
	public static final ResourceKey<DamageType> AIR_SLASH = key("air_slash");
	public static final ResourceKey<DamageType> HEAVENS_SWORD_STRIKE = key("heavens_sword_strike");
	public static final ResourceKey<DamageType> SWORD_WAVE = key("sword_wave");
	public static final ResourceKey<DamageType> COUNTER_RIPOSTE = key("counter_riposte");
	public static final ResourceKey<DamageType> SKY_VAULT_SHOCKWAVE = key("sky_vault_shockwave");

	private VanguardDamageTypes() {}

	private static ResourceKey<DamageType> key(String path) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, VanguardMod.id(path));
	}
}
