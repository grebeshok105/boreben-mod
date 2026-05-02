package com.vanguard.mod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vanguard.mod.client.state.ClientWorthyMarks;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Caster-only render of a small "WORTHY" tag floating above any entity that
 * has been flagged as a worthy opponent for the local Reinhard. Uses Fabric
 * world-render events to draw billboarded text in world space.
 */
public final class WorthyMarkerRenderer {
	private static final float OFFSET_Y = 0.6f;

	private WorthyMarkerRenderer() {
	}

	public static void render(WorldRenderContext context) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || ClientWorthyMarks.size() == 0) {
			return;
		}
		PoseStack poseStack = context.matrixStack();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		Vec3 cam = context.camera().getPosition();
		EntityRenderDispatcher disp = mc.getEntityRenderDispatcher();
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity le)) continue;
			if (le == mc.player) continue;
			UUID id = le.getUUID();
			if (!ClientWorthyMarks.isWorthy(id)) continue;
			if (le.distanceToSqr(mc.player) > 64.0 * 64.0) continue;
			drawTag(poseStack, buffers, disp, le, cam);
		}
		buffers.endBatch();
	}

	private static void drawTag(PoseStack stack, MultiBufferSource.BufferSource buffers,
								EntityRenderDispatcher disp, LivingEntity entity, Vec3 cam) {
		double x = entity.getX() - cam.x;
		double y = entity.getY() + entity.getBbHeight() + OFFSET_Y - cam.y;
		double z = entity.getZ() - cam.z;
		stack.pushPose();
		stack.translate(x, y, z);
		stack.mulPose(disp.cameraOrientation());
		stack.scale(-0.025f, -0.025f, 0.025f);
		Minecraft mc = Minecraft.getInstance();
		Component label = Component.translatable("vanguard.worthy.tag");
		float halfW = mc.font.width(label) / 2.0f;
		mc.font.drawInBatch(label, -halfW, 0f, 0xFFFFD54A, true,
				stack.last().pose(), buffers, net.minecraft.client.gui.Font.DisplayMode.NORMAL,
				0x66000000, 0xF000F0);
		stack.popPose();
	}
}
