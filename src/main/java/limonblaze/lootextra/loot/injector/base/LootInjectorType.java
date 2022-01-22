package limonblaze.lootextra.loot.injector.base;

import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.loot.injector.*;
import limonblaze.lootextra.registry.LootExtraRegistry;
import net.minecraft.util.registry.Registry;

/**
 * Represents a type of loot injectors.<br>
 * Accepts an {@link LootInjector.Serializer} for deserializing from json,
 * and an {@link Integer} priority for sequencing injectors on apply
 * (e.g. Injection to pools should be done after pools are injected into loot table).
 * Smaller integer means higher priority.*/
public class LootInjectorType<I extends LootInjector> {
    public static final LootInjectorType<LootInjector> EMPTY = new LootInjectorType<>((id, json) -> LootInjector.EMPTY, 0);
    public static final LootInjectorType<AddFunctionsInjector> ADD_FUNCTIONS = new LootInjectorType<>(AddFunctionsInjector::fromJson, -50);
    public static final LootInjectorType<AddPoolsInjector> ADD_POOLS = new LootInjectorType<>(AddPoolsInjector::fromJson, -40);
    public static final LootInjectorType<AddPoolConditionsInjector> ADD_POOL_CONDITIONS = new LootInjectorType<>(AddPoolConditionsInjector::fromJson, -30);
    public static final LootInjectorType<AddPoolFunctionsInjector> ADD_POOL_FUNCTIONS = new LootInjectorType<>(AddPoolFunctionsInjector::fromJson, -20);
    public static final LootInjectorType<AddPoolEntriesInjector> ADD_POOL_ENTRIES = new LootInjectorType<>(AddPoolEntriesInjector::fromJson, -10);

    private final LootInjector.Serializer<I> serialzer;
    private final int priority;

    public LootInjectorType(LootInjector.Serializer<I> serializer, int priority) {
        this.serialzer = serializer;
        this.priority = priority;
    }

    public LootInjector.Serializer<I> getSerialzer() {
        return this.serialzer;
    }

    public int getPriority() {
        return this.priority;
    }

    public static void registerBuiltin() {
        Registry.register(LootExtraRegistry.LOOT_INJECTOR_TYPE, LootExtra.identifier("add_pools"), ADD_POOLS);
        Registry.register(LootExtraRegistry.LOOT_INJECTOR_TYPE, LootExtra.identifier("add_functions"), ADD_FUNCTIONS);
        Registry.register(LootExtraRegistry.LOOT_INJECTOR_TYPE, LootExtra.identifier("add_pool_conditions"), ADD_POOL_CONDITIONS);
        Registry.register(LootExtraRegistry.LOOT_INJECTOR_TYPE, LootExtra.identifier("add_pool_functions"), ADD_POOL_FUNCTIONS);
        Registry.register(LootExtraRegistry.LOOT_INJECTOR_TYPE, LootExtra.identifier("add_pool_entries"), ADD_POOL_ENTRIES);
    }

}
