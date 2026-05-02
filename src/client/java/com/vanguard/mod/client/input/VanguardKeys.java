package com.vanguard.mod.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class VanguardKeys {
	public static final String CATEGORY = "key.categories.vanguard";

	public static KeyMapping WISH_RADIAL;

	private VanguardKeys() {
	}

	public static void init() {
		WISH_RADIAL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.vanguard.wish_radial",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				CATEGORY));
	}
}
