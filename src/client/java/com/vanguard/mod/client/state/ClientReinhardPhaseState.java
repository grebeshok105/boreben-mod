package com.vanguard.mod.client.state;

/**
 * Owning-client mirror of Reinhard's phase state. Driven by
 * {@link com.vanguard.mod.network.ReinhardPhaseSyncS2CPayload}.
 */
public final class ClientReinhardPhaseState {
	private static int phaseIndex = 0;
	private static float damageTaken = 0f;

	private ClientReinhardPhaseState() {}

	public static void update(int phase, float damage) {
		phaseIndex = phase;
		damageTaken = damage;
	}

	public static int phaseIndex() {
		return phaseIndex;
	}

	public static float damageTaken() {
		return damageTaken;
	}

	public static boolean isReinhard() {
		return phaseIndex >= 1;
	}
}
