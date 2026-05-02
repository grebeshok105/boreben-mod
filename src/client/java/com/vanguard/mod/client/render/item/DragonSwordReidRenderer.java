package com.vanguard.mod.client.render.item;

import com.vanguard.mod.VanguardMod;
import com.vanguard.mod.item.sword.DragonSwordReidItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Renders the Reid sword as a 3D GeckoLib model.
 *
 * <p>Asset layout under {@code assets/vanguard/}:
 * <ul>
 *   <li>{@code geo/item/dragon_sword_reid.geo.json}</li>
 *   <li>{@code animations/item/dragon_sword_reid.animation.json}</li>
 *   <li>{@code textures/item/dragon_sword_reid.png}</li>
 * </ul>
 *
 * <p>{@link DefaultedItemGeoModel} prepends the {@code item/} subfolder
 * automatically, so the {@code modelLocation} below points at
 * {@code dragon_sword_reid} (without the {@code item/} prefix).
 */
@Environment(EnvType.CLIENT)
public class DragonSwordReidRenderer extends GeoItemRenderer<DragonSwordReidItem> {
	public DragonSwordReidRenderer() {
		super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(VanguardMod.MOD_ID, "dragon_sword_reid")));
	}
}
