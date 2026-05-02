package com.vanguard.mod.hero;

public enum ReinhardPhase {
	P1(0f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f),
	P2(50f, 0.10f, 0.05f, 1.0f, 4.0f, 0.10f),
	P3(150f, 0.20f, 0.10f, 2.0f, 8.0f, 0.20f),
	P4(200f, 0.30f, 0.15f, 3.0f, 14.0f, 0.30f),
	P5(250f, 0.50f, 0.25f, 5.0f, 22.0f, 0.50f);

	public final float thresholdDamageTaken;
	public final float bonusDamagePercent;
	public final float bonusSpeedPercent;
	public final float bonusAttackSpeed;
	public final float bonusArmor;
	public final float incomingDamageReductionPercent;

	ReinhardPhase(float thresholdDamageTaken, float bonusDamagePercent, float bonusSpeedPercent,
				  float bonusAttackSpeed, float bonusArmor, float incomingDamageReductionPercent) {
		this.thresholdDamageTaken = thresholdDamageTaken;
		this.bonusDamagePercent = bonusDamagePercent;
		this.bonusSpeedPercent = bonusSpeedPercent;
		this.bonusAttackSpeed = bonusAttackSpeed;
		this.bonusArmor = bonusArmor;
		this.incomingDamageReductionPercent = incomingDamageReductionPercent;
	}

	public int index() {
		return ordinal() + 1;
	}

	public static ReinhardPhase forDamageTaken(float damageTaken) {
		ReinhardPhase result = P1;
		for (ReinhardPhase p : values()) {
			if (damageTaken >= p.thresholdDamageTaken) {
				result = p;
			} else {
				break;
			}
		}
		return result;
	}
}
