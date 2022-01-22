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
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class AddPoolFunctionsInjector extends LootInjector {
    private final int poolIndex;
    private final List<LootFunction> functions;

    public AddPoolFunctionsInjector(Identifier id, int poolIndex, List<LootFunction> functions) {
        super(id);
        this.poolIndex = poolIndex;
        this.functions = functions;
    }

    public LootInjectorType<?> getType() {
        return LootInjectorType.ADD_POOL_FUNCTIONS;
    }

    public boolean isValid() {
        return !this.functions.isEmpty();
    }

    public void inject(Identifier id, FabricLootSupplierBuilder builder) {
        List<LootPool> pools = ((LootSupplierBuilderHooks)builder).getPools();
        if(this.poolIndex < 0) {
            for(int i = 0; i < pools.size(); i++) {
                FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.of(pools.get(i));
                this.functions.forEach(poolBuilder::withFunction);
                pools.set(i, poolBuilder.build());
            }
        } else if(this.poolIndex < pools.size()) {
            FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.of(pools.get(this.poolIndex));
            this.functions.forEach(poolBuilder::withFunction);
            pools.set(this.poolIndex, poolBuilder.build());
        } else {
            LootExtra.LOGGER.error("Invalid function injection {} to loot pool[{}], pool index out of bounds!", id, this.poolIndex);
        }
    }

    public static AddPoolFunctionsInjector fromJson(Identifier id, JsonObject context) {
        List<LootFunction> functions = new ArrayList<>();
        int index = 0;
        try {
            index = JsonHelper.getInt(context, "pool_index", 0);
            JsonArray jsonArray = JsonHelper.getArray(context, "functions");
            for(JsonElement element : jsonArray) {
                functions.add(LootJsonParser.read(element.toString(), LootFunction.class));
            }
        } catch (Exception e) {
            LootExtra.LOGGER.error("Failed in loading loot_extra:add_pool_functions injector "+ id.toString(), e);
        }
        return new AddPoolFunctionsInjector(id, index, functions);
    }
}
