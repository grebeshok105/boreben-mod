package com.vanguard.mod.transform;

import com.example.superheroes.api.HeroApi;
import com.vanguard.mod.attachment.ReinhardData;
import com.vanguard.mod.attachment.VanguardAttachments;
import com.vanguard.mod.hero.ReinhardHero;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Reinhard transformation suit. Right-click to transform; shift-right-click to
 * untransform. Resets the cumulative damage counter on each transformation.
 */
public class ReinhardSuitItem extends Item {
	public ReinhardSuitItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			boolean changed;
			if (player.isShiftKeyDown()) {
				changed = HeroApi.untransform(serverPlayer);
			} else {
				changed = HeroApi.transform(serverPlayer, ReinhardHero.ID);
				if (changed) {
					ReinhardData reset = ReinhardData.EMPTY.withTransformedAt(serverPlayer.tickCount);
					serverPlayer.setAttached(VanguardAttachments.REINHARD_DATA, reset);
				}
			}
			return changed ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.vanguard.reinhard_suit.lore.line1")
				.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.translatable("item.vanguard.reinhard_suit.lore.line2")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.empty());
		tooltip.add(Component.translatable("item.vanguard.reinhard_suit.lore.usage")
				.withStyle(ChatFormatting.YELLOW));
		tooltip.add(Component.translatable("item.vanguard.reinhard_suit.lore.untransform")
				.withStyle(ChatFormatting.YELLOW));
	}
}
