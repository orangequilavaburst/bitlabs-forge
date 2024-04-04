package xyz.j8bit.bitlabs.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.j8bit.bitlabs.BitLabsMod;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BitLabsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ALL_MOD_ITEMS =
            CREATIVE_MODE_TABS.register("bitlabs_main_tab",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(ModItems.DRAGONITE_INGOT.get()))
                            .title(Component.translatable("itemGroup.bitlabs.bitlabs_main_tab"))
                            .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
