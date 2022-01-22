package limonblaze.lootextra.loot.injector.base;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@FunctionalInterface
public interface LootInjectorReloadCallback {

    /**
     * Called after collecting data-driven {@link LootInjector}s.<br>
     * Use this event to add runtime loot injectors to {@link LootInjectorManager}.<br>*/
    Collection<Map.Entry<Identifier, LootInjector>> addRuntimeLootInjectors();

    Event<LootInjectorReloadCallback> EVENT = EventFactory.createArrayBacked(LootInjectorReloadCallback.class,
            (listeners -> () -> {
                Collection<Map.Entry<Identifier, LootInjector>> entries = new HashSet<>();
                for(LootInjectorReloadCallback listener : listeners) {
                    entries.addAll(listener.addRuntimeLootInjectors());
                }
                return entries;
            }));
}
