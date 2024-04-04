package xyz.j8bit.bitlabs.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.j8bit.bitlabs.BitLabsMod;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BitLabsMod.MOD_ID);

    public static final RegistryObject<Item> RAW_DRAGONITE = ITEMS.register("raw_dragonite", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DRAGONITE_INGOT = ITEMS.register("dragonite_ingot", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BAGUETTE = ITEMS.register("baguette", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0f).build())));
    public static final RegistryObject<Item> SLICED_BREAD = ITEMS.register("sliced_bread", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(0.1f).build())));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

}
