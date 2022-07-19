package zerokun265.fabric.totem_of_dying.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import zerokun265.fabric.totem_of_dying.TotemOfDying;

public class ModItems {

    public static final Item TOTEM_OF_DYING = registerItem("totem_of_dying", new Item(new FabricItemSettings().maxCount(1).group(ItemGroup.COMBAT)));
    public static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(TotemOfDying.MODID, name), item);
    }

    public static void registerModItems() {
        TotemOfDying.LOGGER.info("Initializing ModItems for mod: " + TotemOfDying.MODID);
    }
}
