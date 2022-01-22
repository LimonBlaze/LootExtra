package limonblaze.lootextra;

import limonblaze.lootextra.loot.injector.base.LootInjectorManager;
import limonblaze.lootextra.loot.injector.base.LootInjectorType;
import limonblaze.lootextra.loot.injector.base.LootTableCollector;
import limonblaze.lootextra.loot.modifier.base.LootModifierManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootExtra implements ModInitializer {
    public static String MODID = "loot_extra";
    public static Logger LOGGER = LogManager.getLogger(MODID);
    public static Identifier EMPTY_IDENTIER = identifier("empty");
    public static LootInjectorManager INJECTOR_MANAGER = new LootInjectorManager();
    public static LootModifierManager MODIFIER_MANAGER = new LootModifierManager();

    public void onInitialize() {
        LootTableCollector.registerBuiltIn();
        LootInjectorType.registerBuiltin();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(INJECTOR_MANAGER);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(MODIFIER_MANAGER);
    }

    public static Identifier identifier(String path) {
        return new Identifier(MODID, path);
    }

}
