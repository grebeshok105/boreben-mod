package com.vanguard.mod.client.state;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ClientWorthyMarks {
	private static final Set<UUID> WORTHY = new HashSet<>();

	private ClientWorthyMarks() {
	}

	public static void update(Set<UUID> ids) {
		WORTHY.clear();
		WORTHY.addAll(ids);
	}

	public static boolean isWorthy(UUID id) {
		return WORTHY.contains(id);
	}

	public static int size() {
		return WORTHY.size();
	}
}
