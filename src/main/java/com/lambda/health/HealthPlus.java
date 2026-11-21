package com.lambda.health;

import com.lambda.health.mixin.GameRulesAccess;
import com.lambda.health.mixin.IntegerRuleAccess;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(HealthPlus.ID)
public final class HealthPlus {
    static final String ID = "health_plus";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredItem<HeartContainer> CONTAINER = ITEMS.registerItem("heart_container", HeartContainer::new,
            new Item.Properties().stacksTo(5).rarity(Rarity.RARE));
    private static final DeferredItem<Item> CHALLENGE_ICON = ITEMS.registerSimpleItem("cant_be_killed_advancement_icon");
    private static final ResourceKey<CreativeModeTab> FOOD_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, ResourceLocation.withDefaultNamespace("food_and_drinks"));
    private static final ResourceLocation BONUS = id("heart_container_bonus");
    private static final GameRules.Key<GameRules.IntegerValue> LIMIT = GameRulesAccess.registerRule(
            "limitExtraHearts", GameRules.Category.PLAYER, IntegerRuleAccess.createRule(40));

    public HealthPlus(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(HealthPlus::creativeTab);
        NeoForge.EVENT_BUS.addListener(HealthPlus::copyBonus);
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(FOOD_TAB)) {
            event.accept(CONTAINER.get());
        }
    }

    private static void copyBonus(PlayerEvent.Clone event) {
        AttributeInstance original = event.getOriginal().getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance replacement = event.getEntity().getAttribute(Attributes.MAX_HEALTH);
        if (original != null && replacement != null) {
            AttributeModifier modifier = original.getModifier(BONUS);
            if (modifier != null) {
                replacement.addOrReplacePermanentModifier(modifier);
                if (event.isWasDeath()) {
                    event.getEntity().setHealth(event.getEntity().getMaxHealth());
                }
            }
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    private static void award(ServerPlayer player, String path) {
        var advancement = player.server.getAdvancements().get(id(path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, path);
        }
    }

    private static final class HeartContainer extends Item {
        HeartContainer(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
            var attribute = player.getAttribute(Attributes.MAX_HEALTH);
            int limit = level.getGameRules().getInt(LIMIT);
            var previous = attribute == null ? null : attribute.getModifier(BONUS);
            double bonus = previous == null ? 0 : previous.amount();
            // Temporary modifiers must not let a player permanently exceed the cap.
            double permanent = attribute == null ? limit : attribute.getBaseValue() + bonus;
            if (permanent + 2 > limit) {
                player.displayClientMessage(Component.translatable(ID + ".at_limit", limit), true);
                return InteractionResultHolder.fail(stack);
            }
            attribute.addOrReplacePermanentModifier(new AttributeModifier(BONUS,
                    bonus + 2, AttributeModifier.Operation.ADD_VALUE));
            player.heal(50);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1, 1);
            award(serverPlayer, "use_heart_container");
            if (permanent + 4 > limit) {
                award(serverPlayer, "cant_be_killed");
            }
            return InteractionResultHolder.consume(stack);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context,
                                    List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable(ID + ".heart_container.hint").withStyle(ChatFormatting.GRAY));
        }
    }
}
