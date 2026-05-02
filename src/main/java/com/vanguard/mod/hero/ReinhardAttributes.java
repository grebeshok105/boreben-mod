package com.vanguard.mod.hero;

import com.vanguard.mod.VanguardMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Reinhard attribute modifier helper. Self-contained — does not depend on the
 * base mod's internal {@code AttributeModifierSet} class so the addon stays
 * source-stable across base-mod refactors.
 */
public final class ReinhardAttributes {
	public static final ResourceLocation BASE_ARMOR = VanguardMod.id("modifiers/reinhard/armor");
	public static final ResourceLocation BASE_TOUGHNESS = VanguardMod.id("modifiers/reinhard/toughness");
	public static final ResourceLocation BASE_DAMAGE = VanguardMod.id("modifiers/reinhard/damage");
	public static final ResourceLocation BASE_SPEED = VanguardMod.id("modifiers/reinhard/speed");
	public static final ResourceLocation BASE_HP = VanguardMod.id("modifiers/reinhard/max_health");
	public static final ResourceLocation BASE_KNOCKBACK = VanguardMod.id("modifiers/reinhard/knockback_resistance");
	public static final ResourceLocation BASE_ATTACK_SPEED = VanguardMod.id("modifiers/reinhard/attack_speed");
	public static final ResourceLocation BASE_JUMP = VanguardMod.id("modifiers/reinhard/jump_strength");
	public static final ResourceLocation BASE_STEP = VanguardMod.id("modifiers/reinhard/step_height");

	public static final ResourceLocation PHASE_BONUS_DAMAGE = VanguardMod.id("modifiers/reinhard/phase_damage");
	public static final ResourceLocation PHASE_BONUS_SPEED = VanguardMod.id("modifiers/reinhard/phase_speed");
	public static final ResourceLocation PHASE_BONUS_ATTACK_SPEED = VanguardMod.id("modifiers/reinhard/phase_attack_speed");
	public static final ResourceLocation PHASE_BONUS_ARMOR = VanguardMod.id("modifiers/reinhard/phase_armor");

	public static final ResourceLocation REFLEX_SPEED = VanguardMod.id("modifiers/reinhard/reflex_speed");

	private static final List<Entry> BASE = new ArrayList<>();

	static {
		BASE.add(new Entry(Attributes.ARMOR, BASE_ARMOR, 18.0, AttributeModifier.Operation.ADD_VALUE));
		BASE.add(new Entry(Attributes.ARMOR_TOUGHNESS, BASE_TOUGHNESS, 6.0, AttributeModifier.Operation.ADD_VALUE));
		BASE.add(new Entry(Attributes.ATTACK_DAMAGE, BASE_DAMAGE, 6.0, AttributeModifier.Operation.ADD_VALUE));
		BASE.add(new Entry(Attributes.MOVEMENT_SPEED, BASE_SPEED, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		BASE.add(new Entry(Attributes.MAX_HEALTH, BASE_HP, 30.0, AttributeModifier.Operation.ADD_VALUE));
		BASE.add(new Entry(Attributes.KNOCKBACK_RESISTANCE, BASE_KNOCKBACK, 0.6, AttributeModifier.Operation.ADD_VALUE));
		BASE.add(new Entry(Attributes.ATTACK_SPEED, BASE_ATTACK_SPEED, 1.0, AttributeModifier.Operation.ADD_VALUE));
		BASE.add(new Entry(Attributes.JUMP_STRENGTH, BASE_JUMP, 0.20, AttributeModifier.Operation.ADD_VALUE));
		BASE.add(new Entry(Attributes.STEP_HEIGHT, BASE_STEP, 0.5, AttributeModifier.Operation.ADD_VALUE));
	}

	private ReinhardAttributes() {
	}

	public static void applyBase(LivingEntity entity) {
		for (Entry e : BASE) {
			apply(entity, e.attribute, e.id, e.amount, e.operation);
		}
	}

	public static void removeBase(LivingEntity entity) {
		for (Entry e : BASE) {
			remove(entity, e.attribute, e.id);
		}
		// Clean phase + reflex modifiers too (safety on detransform).
		remove(entity, Attributes.ATTACK_DAMAGE, PHASE_BONUS_DAMAGE);
		remove(entity, Attributes.MOVEMENT_SPEED, PHASE_BONUS_SPEED);
		remove(entity, Attributes.ATTACK_SPEED, PHASE_BONUS_ATTACK_SPEED);
		remove(entity, Attributes.ARMOR, PHASE_BONUS_ARMOR);
		remove(entity, Attributes.MOVEMENT_SPEED, REFLEX_SPEED);
	}

	public static void applyPhaseBonuses(LivingEntity entity, ReinhardPhase phase) {
		applyOrReplace(entity, Attributes.ATTACK_DAMAGE, PHASE_BONUS_DAMAGE,
				phase.bonusDamagePercent, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		applyOrReplace(entity, Attributes.MOVEMENT_SPEED, PHASE_BONUS_SPEED,
				phase.bonusSpeedPercent, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		applyOrReplace(entity, Attributes.ATTACK_SPEED, PHASE_BONUS_ATTACK_SPEED,
				phase.bonusAttackSpeed, AttributeModifier.Operation.ADD_VALUE);
		applyOrReplace(entity, Attributes.ARMOR, PHASE_BONUS_ARMOR,
				phase.bonusArmor, AttributeModifier.Operation.ADD_VALUE);
	}

	public static void applyReflexSpeed(LivingEntity entity, double percent) {
		applyOrReplace(entity, Attributes.MOVEMENT_SPEED, REFLEX_SPEED, percent,
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}

	private static void apply(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id, double amount,
							  AttributeModifier.Operation op) {
		AttributeInstance instance = entity.getAttribute(attr);
		if (instance != null) {
			instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, op));
		}
	}

	private static void applyOrReplace(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id,
									   double amount, AttributeModifier.Operation op) {
		AttributeInstance instance = entity.getAttribute(attr);
		if (instance != null) {
			instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, op));
		}
	}

	private static void remove(LivingEntity entity, Holder<Attribute> attr, ResourceLocation id) {
		AttributeInstance instance = entity.getAttribute(attr);
		if (instance != null) {
			instance.removeModifier(id);
		}
	}

	private record Entry(Holder<Attribute> attribute, ResourceLocation id, double amount,
						 AttributeModifier.Operation operation) {
	}
}
