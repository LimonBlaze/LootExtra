package limonblaze.lootextra.mixin;

import limonblaze.lootextra.LootExtra;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Redirect(method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"))
    private void lootExtra$applyLootModifiers(LootTable lootTable, LootContext context, Consumer<ItemStack> lootConsumer) {
        List<ItemStack> newLoot = new ArrayList<>();
        lootTable.generateUnprocessedLoot(context, newLoot::add);
        LootExtra.MODIFIER_MANAGER.applyModifiers(newLoot, context).forEach(lootConsumer);
    }

}
