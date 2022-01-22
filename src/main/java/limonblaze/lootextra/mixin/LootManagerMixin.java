package limonblaze.lootextra.mixin;

import com.google.gson.JsonElement;
import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.loot.injector.base.LootManagerHook;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LootManager.class)
public class LootManagerMixin implements LootManagerHook {

    @Shadow
    private Map<Identifier, LootTable> tables;

    public Map<Identifier, LootTable> lootExtra$getLootTables() {
        return tables;
    }

    public void lootExtra$setLootTables(Map<Identifier, LootTable> newTables) {
        this.tables = newTables;
    }

    @Inject(method = "apply", at = @At("TAIL"))
    private void accessLootManager(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        LootExtra.INJECTOR_MANAGER.updateLootManager((LootManager) (Object)this);
    }

}
