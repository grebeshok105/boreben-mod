package com.vanguard.mod.ability.sword;

import com.example.superheroes.ability.Ability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Counter-Auto Riposte — registered as a passive {@link Ability} so external
 * mods (Doomsday) can adapt to its damage type. The actual mechanics live in
 * {@link com.vanguard.mod.effect.ReinhardSuperReflexController}: a successful
 * dodge primes Reinhard's next melee, and that primed strike is dealt under
 * {@link com.vanguard.mod.damage.VanguardDamageTypes#COUNTER_RIPOSTE}.
 *
 * <p>Players cannot manually fire this ability, so {@code canActivate} and
 * {@code tryActivate} both refuse. {@code costPerTick} is 0 — it's a passive,
 * not a drain.
 */
public final class CounterAutoRiposteAbility implements Ability {
	@Override
	public ResourceLocation getId() {
		return SwordAbilityIds.COUNTER_RIPOSTE;
	}

	@Override
	public boolean isToggle() {
		return false;
	}

	@Override
	public float costOnActivate() {
		return 0f;
	}

	@Override
	public float costPerTick() {
		return 0f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return false;
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		return false;
	}
}
