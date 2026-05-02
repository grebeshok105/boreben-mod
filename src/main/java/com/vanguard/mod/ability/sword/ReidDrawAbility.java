package com.vanguard.mod.ability.sword;

import com.example.superheroes.ability.Ability;
import com.example.superheroes.api.AbilityApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Toggle ability — while active, the Reid sword is "drawn": Reinhard gains
 * a passive bonus damage envelope (consumed by AttackEntityCallback hook) and
 * a slight movement boost. Costs trickle energy per tick to keep it active.
 */
public final class ReidDrawAbility implements Ability {
	@Override
	public ResourceLocation getId() {
		return SwordAbilityIds.REID_DRAW;
	}

	@Override
	public boolean isToggle() {
		return true;
	}

	@Override
	public float costOnActivate() {
		return 0f;
	}

	@Override
	public float costPerTick() {
		// 1 energy/tick == 20/s. Energy max is 200 with regen 1.0/tick (20/s)
		// so a steady-state draw is sustainable; bursts drain the bar.
		return 1f;
	}

	@Override
	public boolean canActivate(ServerPlayer player) {
		return AbilityHelpers.canActivateSwordAbility(player);
	}

	@Override
	public boolean tryActivate(ServerPlayer player) {
		if (!canActivate(player)) return false;
		if (!AbilityApi.tryConsume(player, getId(), 0f)) return false;
		ReidDrawState.setDrawn(player, true);
		player.level().playSound(null, player.blockPosition(),
				SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7f, 1.4f);
		return true;
	}

	@Override
	public void onTickActive(ServerPlayer player) {
		if (!AbilityHelpers.canActivateSwordAbility(player)) {
			onDeactivate(player);
			return;
		}
		// Brief speed boost while drawn — refreshed every tick. Amp 0 == 20%
		// move-speed bonus (vanilla Speed I).
		MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
		if (speed == null || speed.getDuration() < 30) {
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false, true));
		}
	}

	@Override
	public void onDeactivate(ServerPlayer player) {
		ReidDrawState.setDrawn(player, false);
		player.level().playSound(null, player.blockPosition(),
				SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.5f, 0.9f);
	}
}
