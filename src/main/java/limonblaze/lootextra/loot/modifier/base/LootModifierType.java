package limonblaze.lootextra.loot.modifier.base;

import limonblaze.lootextra.loot.injector.base.LootInjector;


/**
 * Represents a type of loot modifiers.<br>
 * Accepts an {@link LootInjector.Serializer} for deserializing from json,
 * and an {@link Integer} priority for sequencing modfiers on apply.<br>
 * Smaller integer means higher priority.*/
public class LootModifierType<M extends LootModifier> {
    public static final LootModifierType<LootModifier> EMPTY = new LootModifierType<>((id, json, conditions) -> LootModifier.EMPTY, 0);

    private final LootModifier.Serializer<M> serialzer;
    private final int priority;

    public LootModifierType(LootModifier.Serializer<M> serializer, int priority) {
        this.serialzer = serializer;
        this.priority = priority;
    }

    public LootModifier.Serializer<M> getSerialzer() {
        return this.serialzer;
    }

    public int getPriority() {
        return this.priority;
    }

}
