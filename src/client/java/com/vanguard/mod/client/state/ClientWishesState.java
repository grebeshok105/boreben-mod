package com.vanguard.mod.client.state;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientWishesState {
	private static List<ResourceLocation> recent = Collections.emptyList();
	private static Set<ResourceLocation> adapted = Collections.emptySet();
	private static int wishesUsed = 0;

	private ClientWishesState() {
	}

	public static void update(List<ResourceLocation> recentSources, List<ResourceLocation> adaptations, int used) {
		recent = new ArrayList<>(recentSources);
		adapted = new HashSet<>(adaptations);
		wishesUsed = used;
	}

	public static List<ResourceLocation> getRecent() {
		return Collections.unmodifiableList(recent);
	}

	public static Set<ResourceLocation> getAdapted() {
		return Collections.unmodifiableSet(adapted);
	}

	public static int wishesUsed() {
		return wishesUsed;
	}

	public static int wishesRemaining() {
		return Math.max(0, 3 - wishesUsed);
	}

	public static boolean canActivateWishes() {
		return wishesRemaining() > 0 && !recent.isEmpty();
	}
}
