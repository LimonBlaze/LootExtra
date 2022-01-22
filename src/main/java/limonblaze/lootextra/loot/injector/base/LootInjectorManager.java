package limonblaze.lootextra.loot.injector.base;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.registry.LootExtraRegistry;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.LootJsonParser;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LootInjectorManager implements SimpleSynchronousResourceReloadListener {
    private static final String ID = "loot_injectors";
    private final ListMultimap<Identifier, LootInjector> injectors = ArrayListMultimap.create();
    private LootManager lootManager;

    public Identifier getFabricId() {
        return LootExtra.identifier(ID);
    }

    public Collection<Identifier> getFabricDependencies() {
        return Arrays.asList(ResourceReloadListenerKeys.LOOT_TABLES, ResourceReloadListenerKeys.TAGS);
    }

    public void updateLootManager(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    public void reload(ResourceManager manager) {
        //Inspect the needed loot manager's presence
        if(this.lootManager == null) return;
        //Clear caches
        this.injectors.clear();
        //Collect data dirven injectors
        int injectorsLoaded = 0;
        for(Identifier id : manager.findResources(ID, path -> path.endsWith(".json"))) {
            try {
                InputStream stream = manager.getResource(id).getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                JsonObject injectorObject = LootJsonParser.read(reader, JsonObject.class);
                //Get injector type
                if(injectorObject != null) {
                    Identifier typeId = new Identifier(JsonHelper.getString(injectorObject, "type", LootExtra.EMPTY_IDENTIER.toString()));
                    LootInjectorType<?> type = LootExtraRegistry.LOOT_INJECTOR_TYPE.get(typeId);
                    if(type != LootInjectorType.EMPTY) {
                        JsonArray rawLootTableTargets = JsonHelper.getArray(injectorObject, "loot_tables", null);
                        if(rawLootTableTargets == null) {
                            LootExtra.LOGGER.error("Invalid loot injector {}, missing array property \"loot_tables\"!", id);
                        } else if(rawLootTableTargets.size() == 0) {
                            LootExtra.LOGGER.error("Invalid loot injector {}, no loot table target specified!", id);
                        } else {
                            //Get loot table targets from array, get injector from context object
                            List<Identifier> lootTableTargets = this.getLootTablesFromJsonArray(rawLootTableTargets);
                            LootInjector injector = type.getSerialzer().fromJson(id, injectorObject);
                            //Check and collect injectors to map
                            if(injector.isValid()) {
                                lootTableTargets.forEach(lootTable -> this.injectors.put(lootTable, injector));
                                injectorsLoaded++;
                            } else LootExtra.LOGGER.error("Invalid loot injector {} will not be applied!", id);
                        }
                    } else LootExtra.LOGGER.error("Invalid loot injector {} of unknown or empty type!", id);
                }
                //Close reader and stream, end work
                reader.close();
                stream.close();
            } catch(Exception e) {
                LootExtra.LOGGER.error("Error occurred while loading loot injector " + id.toString(), e);
            }
        }
        LootExtra.LOGGER.info("Loaded {} loot injector(s) from data!", injectorsLoaded);
        //Collect runtime injectors
        LootInjectorReloadCallback.EVENT.invoker().addRuntimeLootInjectors().forEach(entry -> injectors.put(entry.getKey(), entry.getValue()));
        //Collect loot tables
        Map<Identifier, LootTable> newLootTables = new HashMap<>(((LootManagerHook) this.lootManager).lootExtra$getLootTables());
        int injectionApplied = 0;
        int injectedLootTables = 0;
        //Process injections
        for(Map.Entry<Identifier, Collection<LootInjector>> entry : this.injectors.asMap().entrySet()) {
            Identifier identifier = entry.getKey();
            LootExtra.LOGGER.info("Target: {}!", identifier);
            LootTable lootTable = newLootTables.get(identifier);
            if(lootTable != null) {
                FabricLootSupplierBuilder builder = FabricLootSupplierBuilder.of(lootTable);
                List<LootInjector> sortedInjectors = entry.getValue().stream()
                        .sorted(Comparator.comparing(lootInjector -> lootInjector.getType().getPriority()))
                        .collect(Collectors.toList());
                for(LootInjector lootInjector : sortedInjectors) {
                    lootInjector.inject(identifier, builder);
                    injectionApplied++;
                }
                newLootTables.put(identifier, builder.build());
                injectedLootTables++;
            } else {
                LootExtra.LOGGER.error("Unknown loot table {}!", identifier);
            }
        }
        //Give back loot tables and report work
        ((LootManagerHook) this.lootManager).lootExtra$setLootTables(ImmutableMap.copyOf(newLootTables));
        LootExtra.LOGGER.info("Applied {} injections to {} loot tables!", injectionApplied, injectedLootTables);
    }

    public List<Identifier> getLootTablesFromJsonArray(JsonArray array) {
        List<Identifier> result = new ArrayList<>();
        for(JsonElement entry : array) {
            if(entry.isJsonPrimitive()) {
                Identifier tableId = Identifier.tryParse(entry.getAsString());
                result.add(tableId);
            } else if(entry.isJsonObject()) {
                JsonObject jsonObject = entry.getAsJsonObject();
                Identifier type = Identifier.tryParse(JsonHelper.getString(jsonObject, "type"));
                result.addAll(LootTableCollector.EVENT.invoker().fromJson(type, jsonObject, this.lootManager.getTableIds()));
            }
        }
        return result;
    }

    public static List<Identifier> tablesFromBlockTag(Identifier type, JsonObject json, Collection<Identifier> allTableIds) {
        if(type.equals(LootExtra.identifier("blocks"))) {
            Identifier name = Identifier.tryParse(JsonHelper.getString(json, "name"));
            Tag<Block> tag = ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.BLOCK_KEY).getTag(name);
            if(tag == null) {
                LootExtra.LOGGER.error("Invalid block tag {} found on loading loot tables for loot injector", name);
                return new ArrayList<>();
            } else {
                return tag.values().stream().map(AbstractBlock::getLootTableId).collect(Collectors.toList());
            }
        }
        return null;
    }

    public static List<Identifier> tablesFromEntityTypeTag(Identifier type, JsonObject json, Collection<Identifier> allTableIds) {
        if(type.equals(LootExtra.identifier("entity_types"))) {
            Identifier name = Identifier.tryParse(JsonHelper.getString(json, "name"));
            Tag<EntityType<?>> tag = ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.ENTITY_TYPE_KEY).getTag(name);
            if(tag == null) {
                LootExtra.LOGGER.error("Invalid entity type tag {} found on loading loot tables for loot injector", name);
                return new ArrayList<>();
            } else {
                return tag.values().stream().map(EntityType::getLootTableId).collect(Collectors.toList());
            }
        }
        return null;
    }

    public static List<Identifier> tablesFilteredFromAll(Identifier type, JsonObject json, Collection<Identifier> allTableIds) {
        if(type.equals(LootExtra.identifier("predicate"))) {
            String name = JsonHelper.getString(json, "name");
            return allTableIds.stream().filter(id -> id.getPath().contains(name)).collect(Collectors.toList());
        }
        return null;
    }
}
