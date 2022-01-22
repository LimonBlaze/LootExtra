package limonblaze.lootextra.mixin;

import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.loot.modifier.base.LootModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), cancellable = true)
    private void addLootModifierHook(LootContext context, Consumer<ItemStack> consumer, CallbackInfo ci) {
        List<ItemStack> newLoot = new ArrayList<>();
        ((LootTable)(Object)this).generateUnprocessedLoot(context, newLoot::add);
        List<LootModifier> activeModifiers = LootExtra.MODIFIER_MANAGER.getModifiers().stream()
                .filter(modifier -> modifier.shouldApply(context))
                .collect(Collectors.toList());
        for(LootModifier modifier : activeModifiers) {
            newLoot = modifier.modify(newLoot, context);
        }
        newLoot.forEach(consumer);
        ci.cancel();
    }

}
