package com.vanguard.mod.client.hud;

import com.vanguard.mod.client.input.VanguardKeys;
import com.vanguard.mod.client.state.ClientWishesState;
import com.vanguard.mod.network.UseWishC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Wishes radial menu — adapted from base mod's RadialMenuHud.
 *
 * <p>Press-and-hold N: opens radial showing up to 5 recent damage-source
 * wedges. Aim with mouse, release to activate the highlighted wedge as a wish.
 * Distinct gold/white styling so it does not visually clash with the base mod's
 * ability radial.
 */
public final class WishRadialHud {
	private static final float DEAD_ZONE = 5f;
	private static final int ITEM_RADIUS = 110;
	private static final int BACKPLATE_RADIUS = 104;
	private static final int SLOT_MIN_WIDTH = 124;
	private static final int SLOT_HEIGHT = 28;
	private static final int SLOT_PADDING_X = 14;
	private static final int CURSOR_RADIUS = 56;

	private static final int COLOR_TEXT_IDLE = 0xFFF6E5B5;
	private static final int COLOR_TEXT_ACTIVE = 0xFFFFFFFF;
	private static final int COLOR_KEY_IDLE = 0xFFB7864A;
	private static final int COLOR_KEY_ACTIVE = 0xFFFFE08A;
	private static final int COLOR_BORDER_IDLE = 0xFF705430;
	private static final int COLOR_BORDER_ACTIVE = 0xFFFFD54A;
	private static final int COLOR_BORDER_ADAPTED = 0xFF2E80FF;
	private static final int COLOR_GLOW = 0x66FFD54A;
	private static final int COLOR_SHADOW = 0x88000000;

	private static boolean open;
	private static float startYaw;
	private static float startPitch;
	private static int selected = -1;

	private WishRadialHud() {
	}

	public static void clientTick(Minecraft mc) {
		if (mc.player == null || mc.level == null || VanguardKeys.WISH_RADIAL == null) {
			closeWithoutActivate();
			return;
		}
		boolean down = VanguardKeys.WISH_RADIAL.isDown();
		List<ResourceLocation> recent = ClientWishesState.getRecent();
		if (down && !open) {
			if (!ClientWishesState.canActivateWishes() || recent.isEmpty()) {
				return;
			}
			open = true;
			startYaw = mc.player.getYRot();
			startPitch = mc.player.getXRot();
			selected = -1;
			return;
		}
		if (!down && open) {
			closeAndActivate(recent);
			return;
		}
		if (open) {
			updateSelection(mc, recent.size());
		}
	}

	private static void updateSelection(Minecraft mc, int n) {
		if (n <= 0 || mc.player == null) {
			selected = -1;
			return;
		}
		float dyaw = mc.player.getYRot() - startYaw;
		float dpitch = mc.player.getXRot() - startPitch;
		float magSq = dyaw * dyaw + dpitch * dpitch;
		if (magSq < DEAD_ZONE * DEAD_ZONE) {
			selected = -1;
			return;
		}
		float per = 360f / n;
		double angle = Math.toDegrees(Math.atan2(dpitch, dyaw)) + 90.0 + per / 2.0;
		angle = ((angle % 360.0) + 360.0) % 360.0;
		selected = ((int) Math.floor(angle / per)) % n;
	}

	private static void closeAndActivate(List<ResourceLocation> recent) {
		int idx = selected;
		open = false;
		selected = -1;
		if (idx >= 0 && idx < recent.size()) {
			ClientPlayNetworking.send(new UseWishC2SPayload(idx));
		}
	}

	private static void closeWithoutActivate() {
		open = false;
		selected = -1;
	}

	public static boolean isOpen() {
		return open;
	}

	public static void render(GuiGraphics graphics, DeltaTracker tracker) {
		if (!open) return;
		List<ResourceLocation> recent = ClientWishesState.getRecent();
		if (recent.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		int cx = mc.getWindow().getGuiScaledWidth() / 2;
		int cy = mc.getWindow().getGuiScaledHeight() / 2;
		int n = recent.size();
		drawBackplate(graphics, cx, cy);
		drawCursor(graphics, mc, cx, cy);
		drawHub(graphics, mc, cx, cy);
		for (int i = 0; i < n; i++) {
			double angle = (i * 2 * Math.PI / n) - Math.PI / 2;
			int x = cx + (int) (Math.cos(angle) * ITEM_RADIUS);
			int y = cy + (int) (Math.sin(angle) * ITEM_RADIUS);
			ResourceLocation typeId = recent.get(i);
			boolean adapted = ClientWishesState.getAdapted().contains(typeId);
			boolean active = i == selected;
			Component name = Component.literal(prettifyDamageType(typeId));
			Component sub = adapted
					? Component.translatable("vanguard.wish.adapted")
					: Component.literal("§e[" + typeId.getNamespace() + "]");
			int textWidth = mc.font.width(name);
			int slotWidth = Math.max(SLOT_MIN_WIDTH, textWidth + SLOT_PADDING_X * 2);
			int slotX = x - slotWidth / 2;
			int slotY = y - SLOT_HEIGHT / 2;
			drawSlot(graphics, slotX, slotY, slotWidth, SLOT_HEIGHT, active, adapted);
			graphics.drawCenteredString(mc.font, name, x, y - 9, active ? COLOR_TEXT_ACTIVE : COLOR_TEXT_IDLE);
			graphics.drawCenteredString(mc.font, sub, x, y + 3, active ? COLOR_KEY_ACTIVE : COLOR_KEY_IDLE);
		}
	}

	private static void drawHub(GuiGraphics graphics, Minecraft mc, int cx, int cy) {
		graphics.fill(cx - 26, cy - 26, cx + 26, cy + 26, 0xCC0E0A04);
		graphics.fill(cx - 26, cy - 26, cx + 26, cy - 25, COLOR_BORDER_ACTIVE);
		graphics.fill(cx - 26, cy + 25, cx + 26, cy + 26, COLOR_BORDER_ACTIVE);
		graphics.fill(cx - 26, cy - 26, cx - 25, cy + 26, COLOR_BORDER_ACTIVE);
		graphics.fill(cx + 25, cy - 26, cx + 26, cy + 26, COLOR_BORDER_ACTIVE);
		String text = ClientWishesState.wishesRemaining() + "/3";
		graphics.drawCenteredString(mc.font, Component.literal(text), cx, cy - 8, COLOR_TEXT_ACTIVE);
		graphics.drawCenteredString(mc.font, Component.translatable("vanguard.wish.hub"), cx, cy + 4, COLOR_KEY_IDLE);
	}

	private static void drawBackplate(GuiGraphics graphics, int cx, int cy) {
		int outer = BACKPLATE_RADIUS + 36;
		graphics.fillGradient(cx - outer, cy - outer, cx + outer, cy + outer, 0x44060814, 0x00040614);
		graphics.fillGradient(cx - BACKPLATE_RADIUS, cy - BACKPLATE_RADIUS,
				cx + BACKPLATE_RADIUS, cy + BACKPLATE_RADIUS, 0x88141008, 0x44070504);
	}

	private static void drawCursor(GuiGraphics graphics, Minecraft mc, int cx, int cy) {
		if (mc.player == null) return;
		float dyaw = mc.player.getYRot() - startYaw;
		float dpitch = mc.player.getXRot() - startPitch;
		float magSq = dyaw * dyaw + dpitch * dpitch;
		if (magSq < DEAD_ZONE * DEAD_ZONE) return;
		double a = Math.atan2(dpitch, dyaw);
		double r = CURSOR_RADIUS;
		int px = cx + (int) Math.round(r * Math.cos(a));
		int py = cy + (int) Math.round(r * Math.sin(a));
		drawSmoothLine(graphics, cx, cy, px, py, COLOR_GLOW);
		graphics.fill(px - 4, py - 4, px + 5, py + 5, COLOR_SHADOW);
		graphics.fill(px - 3, py - 3, px + 4, py + 4, COLOR_BORDER_ACTIVE);
		graphics.fill(px - 2, py - 2, px + 3, py + 3, COLOR_TEXT_ACTIVE);
	}

	private static void drawSmoothLine(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
		int dx = x1 - x0;
		int dy = y1 - y0;
		int steps = Math.max(Math.abs(dx), Math.abs(dy));
		if (steps == 0) return;
		float fx = (float) dx / steps;
		float fy = (float) dy / steps;
		for (int i = 24; i < steps - 4; i += 1) {
			int px = x0 + Math.round(fx * i);
			int py = y0 + Math.round(fy * i);
			g.fill(px, py, px + 1, py + 1, color);
		}
	}

	private static void drawSlot(GuiGraphics g, int x, int y, int width, int height, boolean active, boolean adapted) {
		g.fill(x + 2, y + 2, x + width + 2, y + height + 2, COLOR_SHADOW);
		if (active) {
			g.fill(x - 3, y - 3, x + width + 3, y + height + 3, COLOR_GLOW);
		}
		int top = active ? 0xF02A2014 : (adapted ? 0xCC0A1830 : 0xCC1A1408);
		int bottom = active ? 0xE01F1A0A : (adapted ? 0xCC050A14 : 0xCC0E0A04);
		int border = adapted ? COLOR_BORDER_ADAPTED : (active ? COLOR_BORDER_ACTIVE : COLOR_BORDER_IDLE);
		g.fillGradient(x, y, x + width, y + height, top, bottom);
		g.fill(x, y, x + width, y + 1, border);
		g.fill(x, y + height - 1, x + width, y + height, border);
		g.fill(x, y, x + 1, y + height, border);
		g.fill(x + width - 1, y, x + width, y + height, border);
		g.fill(x + 3, y + 1, x + width - 3, y + 2, 0x33FFFFFF);
	}

	private static String prettifyDamageType(ResourceLocation id) {
		String path = id.getPath();
		int us = path.indexOf('_');
		StringBuilder sb = new StringBuilder();
		boolean cap = true;
		for (char c : path.toCharArray()) {
			if (c == '_') {
				sb.append(' ');
				cap = true;
			} else if (cap) {
				sb.append(Character.toUpperCase(c));
				cap = false;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
