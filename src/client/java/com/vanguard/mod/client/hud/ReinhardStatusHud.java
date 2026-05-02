package com.vanguard.mod.client.hud;

import com.vanguard.mod.client.state.ClientReinhardPhaseState;
import com.vanguard.mod.client.state.ClientWishesState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Top-left status panel — distinct from the base mod's bottom-left resource
 * bar. Shows current Astrea phase (P1..P5), damage progress to next phase,
 * and remaining wishes. Gold/ivory palette to set Reinhard apart from the
 * default red/blue HUD theme of the base mod.
 *
 * <p>Only renders when the local player is transformed into Reinhard and not
 * inside a screen. Vanishes if {@code phaseIndex == 0}.
 */
public final class ReinhardStatusHud {
	private static final int PANEL_X = 6;
	private static final int PANEL_Y = 6;
	private static final int PANEL_W = 132;
	private static final int PANEL_H = 38;

	private static final int COLOR_PANEL_TOP = 0xCC1A1408;
	private static final int COLOR_PANEL_BOTTOM = 0xCC0E0A04;
	private static final int COLOR_BORDER = 0xFF705430;
	private static final int COLOR_BORDER_ACCENT = 0xFFFFD54A;
	private static final int COLOR_TEXT = 0xFFF6E5B5;
	private static final int COLOR_TEXT_ACCENT = 0xFFFFE08A;
	private static final int COLOR_GOLD_FILL = 0xFFFFD54A;
	private static final int COLOR_GOLD_DIM = 0x88FFD54A;
	private static final int COLOR_BAR_TRACK = 0xC0040301;

	private static final float[] THRESHOLDS = {0f, 50f, 150f, 200f, 250f};

	private ReinhardStatusHud() {}

	public static void render(GuiGraphics g, DeltaTracker tracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui) return;
		if (mc.screen != null) return;
		int phase = ClientReinhardPhaseState.phaseIndex();
		if (phase < 1) return;

		drawPanel(g);
		drawTitle(g, mc, phase);
		drawProgressBar(g, phase);
		drawWishPips(g, mc);
	}

	private static void drawPanel(GuiGraphics g) {
		g.fillGradient(PANEL_X, PANEL_Y, PANEL_X + PANEL_W, PANEL_Y + PANEL_H,
				COLOR_PANEL_TOP, COLOR_PANEL_BOTTOM);
		// Gold accent on top edge, plain border elsewhere.
		g.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_W, PANEL_Y + 1, COLOR_BORDER_ACCENT);
		g.fill(PANEL_X, PANEL_Y + PANEL_H - 1, PANEL_X + PANEL_W, PANEL_Y + PANEL_H, COLOR_BORDER);
		g.fill(PANEL_X, PANEL_Y, PANEL_X + 1, PANEL_Y + PANEL_H, COLOR_BORDER);
		g.fill(PANEL_X + PANEL_W - 1, PANEL_Y, PANEL_X + PANEL_W, PANEL_Y + PANEL_H, COLOR_BORDER);
	}

	private static void drawTitle(GuiGraphics g, Minecraft mc, int phase) {
		Component label = Component.literal("ASTREA · P" + phase);
		g.drawString(mc.font, label, PANEL_X + 6, PANEL_Y + 4,
				phase >= 4 ? COLOR_TEXT_ACCENT : COLOR_TEXT, false);
	}

	private static void drawProgressBar(GuiGraphics g, int phase) {
		int barX = PANEL_X + 6;
		int barY = PANEL_Y + 16;
		int barW = PANEL_W - 12;
		int barH = 6;
		g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, COLOR_BORDER);
		g.fill(barX, barY, barX + barW, barY + barH, COLOR_BAR_TRACK);

		float dmg = ClientReinhardPhaseState.damageTaken();
		float lower = THRESHOLDS[Math.max(0, phase - 1)];
		float upper = phase < THRESHOLDS.length ? THRESHOLDS[phase] : THRESHOLDS[THRESHOLDS.length - 1] + 1f;
		float frac = phase >= 5 ? 1.0f : Math.max(0f, Math.min(1f, (dmg - lower) / (upper - lower)));
		int filled = Math.round(frac * barW);
		if (filled > 0) {
			g.fillGradient(barX, barY, barX + filled, barY + barH, COLOR_GOLD_FILL, COLOR_GOLD_DIM);
		}
	}

	private static void drawWishPips(GuiGraphics g, Minecraft mc) {
		int remaining = ClientWishesState.wishesRemaining();
		int pipsY = PANEL_Y + PANEL_H - 10;
		int labelX = PANEL_X + 6;
		Component lbl = Component.translatable("vanguard.wish.hub");
		g.drawString(mc.font, lbl, labelX, pipsY, COLOR_TEXT_ACCENT, false);
		int pipX = labelX + mc.font.width(lbl) + 6;
		for (int i = 0; i < 3; i++) {
			int x = pipX + i * 8;
			int color = i < remaining ? COLOR_GOLD_FILL : 0x55554020;
			g.fill(x, pipsY + 1, x + 6, pipsY + 7, COLOR_BORDER);
			g.fill(x + 1, pipsY + 2, x + 5, pipsY + 6, color);
		}
	}
}
