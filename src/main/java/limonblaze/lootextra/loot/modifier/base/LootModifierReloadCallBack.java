package limonblaze.lootextra.loot.modifier.base;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Collection;
import java.util.HashSet;

@FunctionalInterface
public interface LootModifierReloadCallBack {

    Event<LootModifierReloadCallBack> EVENT = EventFactory.createArrayBacked(LootModifierReloadCallBack.class,
            (listeners -> () -> {
                Collection<LootModifier> modifiers = new HashSet<>();
                for(LootModifierReloadCallBack listener : listeners) {
                    modifiers.addAll(listener.addRuntimeLootModifiers());
                }
                return modifiers;
            }));

    Collection<LootModifier> addRuntimeLootModifiers();

}
