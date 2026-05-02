package com.vanguard.mod.hero;

import com.example.superheroes.hero.Hero;
import com.example.superheroes.hero.HeroTheme;
import com.example.superheroes.resource.ResourceKind;
import com.vanguard.mod.VanguardMod;
import com.vanguard.mod.ability.sword.SwordAbilityIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class ReinhardHero implements Hero {
	public static final ResourceLocation ID = VanguardMod.id("reinhard");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public float getEnergyMax() {
		return 200f;
	}

	@Override
	public float getEnergyRegenPerTick() {
		return 1.0f;
	}

	@Override
	public float getManaMax() {
		return 100f;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return switch (pose) {
			case CROUCHING -> EntityDimensions.scalable(0.6f, 1.5f).withEyeHeight(1.27f);
			case SWIMMING, FALL_FLYING, SPIN_ATTACK -> EntityDimensions.scalable(0.6f, 0.6f).withEyeHeight(0.4f);
			default -> EntityDimensions.scalable(0.6f, 1.8f).withEyeHeight(1.62f);
		};
	}

	@Override
	public List<ResourceLocation> getAbilities() {
		// Sword-bound abilities (stage 4d). Activated either through the base
		// mod's R-radial menu (gated by hand-holding the Reid sword) or via the
		// dedicated Z/C/G keybinds. Worthy-gate is enforced inside each ability.
		return List.of(
				SwordAbilityIds.REID_DRAW,
				SwordAbilityIds.AIR_SLASH,
				SwordAbilityIds.SKY_VAULT
		);
	}

	@Override
	public ResourceKind getDefaultBinding(ResourceLocation abilityId) {
		return ResourceKind.ENERGY;
	}

	@Override
	public void applyPassives(Player player) {
		ReinhardAttributes.applyBase(player);
		ReinhardAttributes.applyReflexSpeed(player, 1.00);
		// Permanent regen II + fire resistance (Astrea blessing).
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 1, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, true, false, true));
		player.setHealth(player.getMaxHealth());
	}

	@Override
	public void removePassives(Player player) {
		ReinhardAttributes.removeBase(player);
		player.removeEffect(MobEffects.REGENERATION);
		player.removeEffect(MobEffects.FIRE_RESISTANCE);
	}

	@Override
	public boolean cancelsFallDamage(Player player) {
		return true;
	}

	@Override
	public HeroTheme getTheme() {
		// Reuse a vaguely-light-coloured theme; addon HUD restyle in stage 4f anyway.
		return HeroTheme.DEFAULT;
	}
}
