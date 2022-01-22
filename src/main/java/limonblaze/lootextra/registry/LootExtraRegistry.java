package limonblaze.lootextra.registry;

import com.mojang.serialization.Lifecycle;
import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.loot.injector.base.LootInjectorType;
import limonblaze.lootextra.loot.modifier.base.LootModifierType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class LootExtraRegistry {
    public static final RegistryKey<Registry<LootInjectorType<?>>> LOOT_INJECTOR_TYPE_KEY = RegistryKey.ofRegistry(LootExtra.identifier("loot_injector_types"));
    public static final DefaultedRegistry<LootInjectorType<?>> LOOT_INJECTOR_TYPE = FabricRegistryBuilder.from(new DefaultedRegistry<>(
            LootExtra.EMPTY_IDENTIER.toString(),
            LOOT_INJECTOR_TYPE_KEY,
            Lifecycle.stable())
    ).buildAndRegister();

    public static final RegistryKey<Registry<LootModifierType<?>>> LOOT_MODIFIER_TYPE_KEY = RegistryKey.ofRegistry(LootExtra.identifier("loot_modifier_types"));
    public static final DefaultedRegistry<LootModifierType<?>> LOOT_MODIFIER_TYPE = FabricRegistryBuilder.from(new DefaultedRegistry<>(
            LootExtra.EMPTY_IDENTIER.toString(),
            LOOT_MODIFIER_TYPE_KEY,
            Lifecycle.stable())
    ).buildAndRegister();

    static {
        LOOT_INJECTOR_TYPE.add(RegistryKey.of(LOOT_INJECTOR_TYPE_KEY, LootExtra.EMPTY_IDENTIER), LootInjectorType.EMPTY, Lifecycle.stable());
        LOOT_MODIFIER_TYPE.add(RegistryKey.of(LOOT_MODIFIER_TYPE_KEY, LootExtra.EMPTY_IDENTIER), LootModifierType.EMPTY, Lifecycle.stable());
    }
}
