package com.vanguard.mod.item.sword;

import com.vanguard.mod.VanguardMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Reinhard's Dragon Sword "Reid". A 3D-rendered melee weapon. Accepts attacks
 * only against worthy opponents (gated server-side via {@link
 * com.vanguard.mod.effect.ReidSwordWorthyGate}). The sword itself just provides
 * the 3D render, attribute modifiers, and is the carrier item for the various
 * sword-bound abilities (Z/X/C/G/V/Shift+RClick).
 */
public class DragonSwordReidItem extends Item implements GeoItem {
	/** Bonus attack damage on top of the player base 1.0 damage. */
	public static final float ATTACK_DAMAGE = 9.0f;
	/** Attack-speed delta. -2.4 == standard sword cooldown. */
	public static final float ATTACK_SPEED = -2.4f;

	public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.dragon_sword_reid.idle");
	public static final RawAnimation DRAW = RawAnimation.begin().thenLoop("animation.dragon_sword_reid.draw");

	private static final ResourceLocation ATTACK_DAMAGE_ID = VanguardMod.id("dragon_sword_reid.attack_damage");
	private static final ResourceLocation ATTACK_SPEED_ID = VanguardMod.id("dragon_sword_reid.attack_speed");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	/** Lazy factory for the client-side render provider. Set from the client
	 * entrypoint to avoid loading client-only classes on the server. */
	private Supplier<GeoRenderProvider> renderProviderFactory;

	public DragonSwordReidItem(Properties properties) {
		super(properties
				.stacksTo(1)
				.fireResistant()
				.durability(2200)
				.component(DataComponents.ATTRIBUTE_MODIFIERS, defaultAttributes()));
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	private static ItemAttributeModifiers defaultAttributes() {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(ATTACK_DAMAGE_ID, ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build();
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "controller", 0,
				state -> state.setAndContinue(IDLE)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public boolean isPerspectiveAware() {
		return true;
	}

	@Override
	public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
		// GeckoLib only invokes this lambda on the physical client. The factory
		// is wired up in {@code VanguardClient.onInitializeClient}; on a
		// dedicated server it stays null and we simply do nothing.
		Supplier<GeoRenderProvider> factory = renderProviderFactory;
		if (factory != null) {
			consumer.accept(factory.get());
		}
	}

	/** Called from the client init to install the GeckoLib render provider. */
	public void setRenderProviderFactory(Supplier<GeoRenderProvider> factory) {
		this.renderProviderFactory = factory;
	}
}
