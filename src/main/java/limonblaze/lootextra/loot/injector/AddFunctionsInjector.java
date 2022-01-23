package limonblaze.lootextra.loot.injector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.loot.injector.base.LootInjector;
import limonblaze.lootextra.loot.injector.base.LootInjectorType;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.LootJsonParser;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class AddFunctionsInjector extends LootInjector {
    private final List<LootFunction> functions;

    public AddFunctionsInjector(Identifier id, List<LootFunction> functions) {
        super(id);
        this.functions = functions;
    }

    public LootInjectorType<?> getType() {
        return LootInjectorType.ADD_FUNCTIONS;
    }
    public void inject(Identifier id, FabricLootSupplierBuilder lootTable) {
        lootTable.withFunctions(this.functions);
    }

    public boolean isValid() {
        return !this.functions.isEmpty();
    }

    public static AddFunctionsInjector fromJson(Identifier id, JsonObject context) {
        List<LootFunction> functions = new ArrayList<>();
        try {
            JsonArray jsonArray = JsonHelper.getArray(context, "functions");
            for(JsonElement element : jsonArray) {
                functions.add(LootJsonParser.read(element.toString(), LootFunction.class));
            }
        } catch (Exception e) {
            LootExtra.LOGGER.error("Failed in loading loot_extra: add_functions injector "+ id.toString(), e);
        }
        return new AddFunctionsInjector(id, functions);
    }

}
