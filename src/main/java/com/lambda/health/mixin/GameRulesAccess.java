package com.lambda.health.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.class)
public interface GameRulesAccess {
    @Invoker("register")
    static GameRules.Key<GameRules.IntegerValue> registerRule(
            String name, GameRules.Category category, GameRules.Type<GameRules.IntegerValue> type) {
        throw new AssertionError();
    }
}
