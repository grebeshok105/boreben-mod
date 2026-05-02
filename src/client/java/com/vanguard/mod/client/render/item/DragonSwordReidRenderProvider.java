package com.vanguard.mod.client.render.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

/**
 * Client-only bridge between the {@link com.vanguard.mod.item.sword.DragonSwordReidItem}
 * and its lazily-instantiated {@link DragonSwordReidRenderer}.
 *
 * <p>GeckoLib only invokes this on the physical client, so a stale reference
 * is safe on the dedicated server (the JVM never resolves the renderer class
 * there).
 */
@Environment(EnvType.CLIENT)
public final class DragonSwordReidRenderProvider implements GeoRenderProvider {
	private DragonSwordReidRenderer renderer;

	@Override
	public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
		if (renderer == null) {
			renderer = new DragonSwordReidRenderer();
		}
		return renderer;
	}
}
