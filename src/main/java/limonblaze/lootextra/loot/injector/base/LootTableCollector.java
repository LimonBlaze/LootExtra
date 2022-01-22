package limonblaze.lootextra.loot.injector.base;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An event base loot table id collector used by {@link LootInjectorManager}.<br>
 * This handler is used to collect loot table ids<br>
 * from json objects specified in [loot_tables] field in loot_injector.json<br>*/
@FunctionalInterface
public interface LootTableCollector {

    Event<LootTableCollector> EVENT = EventFactory.createArrayBacked(LootTableCollector.class,
            collectors -> (type, json, allTableIds) -> {
                for(LootTableCollector collector : collectors) {
                    List<Identifier> result = collector.fromJson(type, json, allTableIds);
                    if(result != null) return result;
                }
                return new ArrayList<>();
            });

    /**
     * @param type a type identifer specified in json object used to match right handler
     * @param json the json object for handler to read
     * @param allTableIds a collection of all loaded loot table ids
     * @return a list of loot table ids (matched handler type)
     * or null (mismatched handler type, leaving other handlers to do their work)*/
    List<Identifier> fromJson(Identifier type, JsonObject json, Collection<Identifier> allTableIds);

    static void registerBuiltIn() {
        EVENT.register(LootInjectorManager::tablesFromBlockTag);
        EVENT.register(LootInjectorManager::tablesFromEntityTypeTag);
        EVENT.register(LootInjectorManager::tablesFilteredFromAll);
    }

}
