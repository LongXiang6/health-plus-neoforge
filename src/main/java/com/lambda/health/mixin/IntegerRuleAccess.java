package com.lambda.health.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.IntegerValue.class)
public interface IntegerRuleAccess {
    @Invoker("create")
    static GameRules.Type<GameRules.IntegerValue> createRule(int value) {
        throw new AssertionError();
    }
}
