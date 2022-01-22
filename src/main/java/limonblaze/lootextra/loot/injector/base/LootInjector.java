package limonblaze.lootextra.loot.injector.base;

import com.google.gson.JsonObject;
import limonblaze.lootextra.LootExtra;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.minecraft.util.Identifier;

/**
 * Represents an injection to specified loot table instances on reload.<br>
 * Applys after loot tables are fully load into loot manager,
 * as mod server data listeners must load after vanilla ones. See also {@link LootInjectorManager}.<br>
 * Loot injectors, as its name, does better on INJECT instead of MODIFY,
 * because it's no easy to read and handle existing loot table instances.<br>
 * Unlike {@link limonblaze.lootextra.loot.modifier.base.LootModifier} who work on runtime,
 * loot injectors doesn't support loot conditions to control whether to inject or not,
 * as vanilla loot functions, pools, entries are already conditional.*/
public abstract class LootInjector {
    public static final LootInjector EMPTY = new LootInjector(LootExtra.EMPTY_IDENTIER) {

        public LootInjectorType<?> getType() {
            return LootInjectorType.EMPTY;
        }

        public void inject(Identifier id, FabricLootSupplierBuilder lootTable) {}

        public boolean isValid() {
            return false;
        }

    };

    private final Identifier id;

    public LootInjector(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return this.id;
    }

    public abstract LootInjectorType<?> getType();

    public abstract boolean isValid();

    public abstract void inject(Identifier tableId, FabricLootSupplierBuilder table);


    @FunctionalInterface
    public interface Serializer<T extends LootInjector> {

        T fromJson(Identifier id, JsonObject json);

    }
}
