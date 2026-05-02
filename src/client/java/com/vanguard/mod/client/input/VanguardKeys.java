package com.vanguard.mod.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class VanguardKeys {
	public static final String CATEGORY = "key.categories.vanguard";

	public static KeyMapping WISH_RADIAL;
	public static KeyMapping REID_DRAW;
	public static KeyMapping AIR_SLASH;
	public static KeyMapping SKY_VAULT;
	public static KeyMapping HEAVENS_SWORD;
	public static KeyMapping SWORD_WAVE;

	private VanguardKeys() {
	}

	public static void init() {
		WISH_RADIAL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.vanguard.wish_radial",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				CATEGORY));
		REID_DRAW = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.vanguard.reid_draw",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_C,
				CATEGORY));
		AIR_SLASH = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.vanguard.air_slash",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_Z,
				CATEGORY));
		SKY_VAULT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.vanguard.sky_vault",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				CATEGORY));
		HEAVENS_SWORD = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.vanguard.heavens_sword",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_X,
				CATEGORY));
		SWORD_WAVE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.vanguard.sword_wave",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				CATEGORY));
	}
}
