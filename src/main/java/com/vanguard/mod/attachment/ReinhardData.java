package com.vanguard.mod.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ReinhardData(
		float damageTaken,
		long transformedAtTick,
		long lastDodgeTick,
		long lastInstantHealTick,
		long counterPrimedUntilTick,
		boolean phoenixUsed,
		boolean weakened,
		int wishesUsed
) {
	public static final ReinhardData EMPTY = new ReinhardData(0f, 0L, -1L, -1L, -1L, false, false, 0);

	public static final Codec<ReinhardData> CODEC = RecordCodecBuilder.create(in -> in.group(
			Codec.FLOAT.fieldOf("damage_taken").forGetter(ReinhardData::damageTaken),
			Codec.LONG.fieldOf("transformed_at_tick").forGetter(ReinhardData::transformedAtTick),
			Codec.LONG.fieldOf("last_dodge_tick").forGetter(ReinhardData::lastDodgeTick),
			Codec.LONG.fieldOf("last_instant_heal_tick").forGetter(ReinhardData::lastInstantHealTick),
			Codec.LONG.fieldOf("counter_primed_until_tick").forGetter(ReinhardData::counterPrimedUntilTick),
			Codec.BOOL.fieldOf("phoenix_used").forGetter(ReinhardData::phoenixUsed),
			Codec.BOOL.fieldOf("weakened").forGetter(ReinhardData::weakened),
			Codec.INT.fieldOf("wishes_used").forGetter(ReinhardData::wishesUsed)
	).apply(in, ReinhardData::new));

	public ReinhardData withDamageTaken(float v) {
		return new ReinhardData(v, transformedAtTick, lastDodgeTick, lastInstantHealTick,
				counterPrimedUntilTick, phoenixUsed, weakened, wishesUsed);
	}

	public ReinhardData withTransformedAt(long tick) {
		return new ReinhardData(0f, tick, -1L, -1L, -1L, false, false, 0);
	}

	public ReinhardData withLastDodgeTick(long t) {
		return new ReinhardData(damageTaken, transformedAtTick, t, lastInstantHealTick,
				counterPrimedUntilTick, phoenixUsed, weakened, wishesUsed);
	}

	public ReinhardData withLastInstantHeal(long t) {
		return new ReinhardData(damageTaken, transformedAtTick, lastDodgeTick, t,
				counterPrimedUntilTick, phoenixUsed, weakened, wishesUsed);
	}

	public ReinhardData withCounterPrimedUntil(long t) {
		return new ReinhardData(damageTaken, transformedAtTick, lastDodgeTick, lastInstantHealTick,
				t, phoenixUsed, weakened, wishesUsed);
	}

	public ReinhardData withPhoenixUsed(boolean used) {
		return new ReinhardData(damageTaken, transformedAtTick, lastDodgeTick, lastInstantHealTick,
				counterPrimedUntilTick, used, weakened, wishesUsed);
	}

	public ReinhardData withWeakened(boolean w) {
		return new ReinhardData(damageTaken, transformedAtTick, lastDodgeTick, lastInstantHealTick,
				counterPrimedUntilTick, phoenixUsed, w, wishesUsed);
	}

	public ReinhardData withWishesUsed(int n) {
		return new ReinhardData(damageTaken, transformedAtTick, lastDodgeTick, lastInstantHealTick,
				counterPrimedUntilTick, phoenixUsed, weakened, n);
	}
}
