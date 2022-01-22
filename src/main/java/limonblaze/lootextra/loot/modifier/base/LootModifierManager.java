package limonblaze.lootextra.loot.modifier.base;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import limonblaze.lootextra.LootExtra;
import limonblaze.lootextra.registry.LootExtraRegistry;
import net.fabricmc.fabric.api.loot.v1.LootJsonParser;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LootModifierManager implements SimpleSynchronousResourceReloadListener {
    private static final String ID = "loot_modifiers";
    private final List<LootModifier> modifiers = new ArrayList<>();

    public Identifier getFabricId() {
        return LootExtra.identifier(ID);
    }

    public Collection<Identifier> getFabricDependencies() {
        return Arrays.asList(ResourceReloadListenerKeys.LOOT_TABLES, ResourceReloadListenerKeys.TAGS);
    }

    public List<LootModifier> getModifiers() {
        return this.modifiers;
    }

    public void reload(ResourceManager manager) {
        //Clear caches
        this.modifiers.clear();
        //Collect data dirven modfidiers
        List<LootModifier> tmpModifiers = new ArrayList<>();
        Gson gson = LootGsons.getConditionGsonBuilder().create();
        for(Identifier id : manager.findResources(ID, path -> path.endsWith(".json"))) {
            try {
                InputStream stream = manager.getResource(id).getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                JsonObject modifierObject = LootJsonParser.read(reader, JsonObject.class);
                //Get injector type
                if(modifierObject != null) {
                    Identifier typeId = new Identifier(JsonHelper.getString(modifierObject, "type"));
                    LootModifierType<?> type = LootExtraRegistry.LOOT_MODIFIER_TYPE.get(typeId);
                    if(type != LootModifierType.EMPTY) {
                        JsonArray conditionArray = JsonHelper.getArray(modifierObject, "conditions");
                        List<LootCondition> conditions = new ArrayList<>();
                        for(JsonElement element : conditionArray) {
                            conditions.add(gson.fromJson(element, LootCondition.class));
                        }
                        LootModifier modifier = type.getSerialzer().fromJson(id, modifierObject, conditions);
                        //Check and collect modifiers to list
                        if(modifier.isValid()) {
                            tmpModifiers.add(modifier);
                        } else LootExtra.LOGGER.error("Invalid loot modifier {} will not be applied!", id);
                    } else LootExtra.LOGGER.error("Invalid loot modifier {} of unknown or empty type!", id);
                }
                //Close reader and stream, end work
                reader.close();
                stream.close();
            } catch(Exception e) {
                LootExtra.LOGGER.error("Error occurred while loading loot modifier " + id.toString(), e);
            }
        }
        //Add runtime loot modifiers
        tmpModifiers.addAll(LootModifierReloadCallBack.EVENT.invoker().addRuntimeLootModifiers());
        //Report work and validate modifiers
        LootExtra.LOGGER.info("Loaded {} loot modifier(s) from data!", tmpModifiers.size());
        tmpModifiers = tmpModifiers.stream()
                .sorted(Comparator.comparing(lootModifier -> lootModifier.getType().getPriority()))
                .collect(Collectors.toList());
        this.modifiers.addAll(tmpModifiers);
    }
}
