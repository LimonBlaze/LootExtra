package limonblaze.lootextra.loot.injector.base;

import net.minecraft.loot.LootTable;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface LootManagerHook {

    Map<Identifier, LootTable> lootExtra$getLootTables();

    void lootExtra$setLootTables(Map<Identifier, LootTable> newLootTables);

}
