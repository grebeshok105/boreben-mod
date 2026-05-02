package com.vanguard.mod.attachment;

import com.vanguard.mod.VanguardMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class VanguardAttachments {
	public static final AttachmentType<ReinhardData> REINHARD_DATA = AttachmentRegistry.<ReinhardData>builder()
			.initializer(() -> ReinhardData.EMPTY)
			.persistent(ReinhardData.CODEC)
			.copyOnDeath()
			.buildAndRegister(VanguardMod.id("reinhard_data"));

	private VanguardAttachments() {
	}

	public static void init() {
	}
}
