package limonblaze.lootextra.loot.injector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.loot.injector.base.LootInjector;
import limonblaze.lootextra.loot.injector.base.LootInjectorType;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.LootJsonParser;
import net.fabricmc.fabric.mixin.loot.table.LootSupplierBuilderHooks;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class AddPoolEntriesInjector extends LootInjector {
    private final int poolIndex;
    private final List<LootPoolEntry> entries;

    public AddPoolEntriesInjector(Identifier id, int poolIndex, List<LootPoolEntry> entries) {
        super(id);
        this.poolIndex = poolIndex;
        this.entries = entries;
    }

    public LootInjectorType<?> getType() {
        return LootInjectorType.ADD_POOL_ENTRIES;
    }

    public boolean isValid() {
        return !this.entries.isEmpty();
    }

    public void inject(Identifier id, FabricLootSupplierBuilder builder) {
        List<LootPool> pools = ((LootSupplierBuilderHooks)builder).getPools();
        if(this.poolIndex < 0) {
            for(int i = 0; i < pools.size(); i++) {
                FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.of(pools.get(i));
                this.entries.forEach(poolBuilder::withEntry);
                pools.set(i, poolBuilder.build());
            }
        } else if(this.poolIndex < pools.size()) {
            FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.of(pools.get(this.poolIndex));
            this.entries.forEach(poolBuilder::withEntry);
            pools.set(this.poolIndex, poolBuilder.build());
        } else {
            LootExtra.LOGGER.error("Invalid entry injection {} to loot pool[{}], pool index out of bounds!", id, this.poolIndex);
        }
    }

    public static AddPoolEntriesInjector fromJson(Identifier id, JsonObject context) {
        List<LootPoolEntry> entries = new ArrayList<>();
        int index = 0;
        try {
            index = JsonHelper.getInt(context, "pool_index", 0);
            JsonArray jsonArray = JsonHelper.getArray(context, "entries");
            for(JsonElement element : jsonArray) {
                entries.add(LootJsonParser.read(element.toString(), LootPoolEntry.class));
            }
        } catch (Exception e) {
            LootExtra.LOGGER.error("Failed in loading loot_extra:add_pool_entries injector "+ id.toString(), e);
        }
        return new AddPoolEntriesInjector(id, index, entries);
    }

}
