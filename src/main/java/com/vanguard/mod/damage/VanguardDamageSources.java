package com.vanguard.mod.damage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

/**
 * Convenience builder for {@link DamageSource} instances backed by Vanguard
 * damage types. The level argument is required because damage type holders
 * are owned by the server's registry access.
 */
public final class VanguardDamageSources {
	private VanguardDamageSources() {}

	public static DamageSource source(ServerLevel level, ResourceKey<DamageType> typeKey, Entity attacker) {
		return new DamageSource(level.registryAccess()
				.registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
				.getHolderOrThrow(typeKey), attacker);
	}

	public static DamageSource source(ServerLevel level, ResourceKey<DamageType> typeKey,
									  Entity directEntity, Entity causingEntity) {
		return new DamageSource(level.registryAccess()
				.registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
				.getHolderOrThrow(typeKey), directEntity, causingEntity);
	}
}
