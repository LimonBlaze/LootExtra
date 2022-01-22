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
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class AddPoolConditionsInjector extends LootInjector {
    private final int poolIndex;
    private final List<LootCondition> conditions;

    public AddPoolConditionsInjector(Identifier id, int poolIndex, List<LootCondition> conditions) {
        super(id);
        this.poolIndex = poolIndex;
        this.conditions = conditions;
    }

    public LootInjectorType<?> getType() {
        return LootInjectorType.ADD_POOL_CONDITIONS;
    }

    public boolean isValid() {
        return !this.conditions.isEmpty();
    }

    public void inject(Identifier id, FabricLootSupplierBuilder builder) {
        List<LootPool> pools = ((LootSupplierBuilderHooks)builder).getPools();
        if(this.poolIndex < 0) {
            for(int i = 0; i < pools.size(); i++) {
                FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.of(pools.get(i));
                this.conditions.forEach(poolBuilder::withCondition);
                pools.set(i, poolBuilder.build());
            }
        } else if(this.poolIndex < pools.size()) {
            FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.of(pools.get(this.poolIndex));
            this.conditions.forEach(poolBuilder::withCondition);
            pools.set(this.poolIndex, poolBuilder.build());
        } else {
            LootExtra.LOGGER.error("Invalid condition injection {} to loot pool[{}], pool index out of bounds!", id, this.poolIndex);
        }
    }

    public static AddPoolConditionsInjector fromJson(Identifier id, JsonObject context) {
        List<LootCondition> conditions = new ArrayList<>();
        int index = 0;
        try {
            index = JsonHelper.getInt(context, "pool_index", 0);
            JsonArray jsonArray = JsonHelper.getArray(context, "conditions");
            for(JsonElement element : jsonArray) {
                conditions.add(LootJsonParser.read(element.toString(), LootCondition.class));
            }
        } catch (Exception e) {
            LootExtra.LOGGER.error("Failed in loading loot_extra:add_pool_conditions injector "+ id.toString(), e);
        }
        return new AddPoolConditionsInjector(id, index, conditions);
    }
}
