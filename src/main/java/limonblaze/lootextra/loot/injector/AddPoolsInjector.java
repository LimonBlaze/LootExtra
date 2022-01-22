package limonblaze.lootextra.loot.injector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.loot.injector.base.LootInjector;
import limonblaze.lootextra.loot.injector.base.LootInjectorType;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.LootJsonParser;
import net.minecraft.loot.LootPool;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class AddPoolsInjector extends LootInjector {
    private final List<LootPool> pools;

    public AddPoolsInjector(Identifier id, List<LootPool> pools) {
        super(id);
        this.pools = pools;
    }

    public LootInjectorType<?> getType() {
        return LootInjectorType.ADD_POOLS;
    }

    public void inject(Identifier id, FabricLootSupplierBuilder lootTable) {
        lootTable.withPools(this.pools);
    }

    public boolean isValid() {
        return !this.pools.isEmpty();
    }

    public static AddPoolsInjector fromJson(Identifier id, JsonObject context) {
        List<LootPool> pools = new ArrayList<>();
        try {
            JsonArray jsonArray = JsonHelper.getArray(context, "pools");
            for(JsonElement element : jsonArray) {
                pools.add(LootJsonParser.read(element.toString(), LootPool.class));
            }
        } catch (Exception e) {
            LootExtra.LOGGER.error("Failed in loading loot_extra: add_pools injector "+ id.toString(), e);
        }
        return new AddPoolsInjector(id, pools);
    }
}
