package xyz.j8bit.bitlabs;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import xyz.j8bit.bitlabs.block.ModBlocks;
import xyz.j8bit.bitlabs.entity.ModEntities;
import xyz.j8bit.bitlabs.item.ModCreativeModeTabs;
import xyz.j8bit.bitlabs.item.ModItems;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BitLabsMod.MOD_ID)
public class BitLabsMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "bitlabs";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public BitLabsMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == ModCreativeModeTabs.ALL_MOD_ITEMS.getKey()){
            event.accept(ModItems.BAGUETTE);
            event.accept(ModItems.SLICED_BREAD);

            event.accept(ModBlocks.DRAGONITE_BLOCK);
            event.accept(ModBlocks.RAW_DRAGONITE_BLOCK);
            event.accept(ModBlocks.DRAGONITE_ORE);
            event.accept(ModBlocks.DEEPSLATE_DRAGONITE_ORE);
            event.accept(ModItems.RAW_DRAGONITE);
            event.accept(ModItems.DRAGONITE_INGOT);

            event.accept(ModItems.DRAGONITE_SWORD);
            event.accept(ModItems.DRAGONITE_PICKAXE);
            event.accept(ModItems.DRAGONITE_AXE);
            event.accept(ModItems.DRAGONITE_SHOVEL);
            event.accept(ModItems.DRAGONITE_HOE);

            event.accept(ModItems.DEVILSKNIFE);
        }
        else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(ModItems.RAW_DRAGONITE);
            event.accept(ModItems.DRAGONITE_INGOT);
        }
        else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS){
            event.accept(ModItems.BAGUETTE);
            event.accept(ModItems.SLICED_BREAD);
        }
        else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){
            event.accept(ModBlocks.DRAGONITE_BLOCK);
            event.accept(ModBlocks.RAW_DRAGONITE_BLOCK);
            event.accept(ModBlocks.DRAGONITE_ORE);
            event.accept(ModBlocks.DEEPSLATE_DRAGONITE_ORE);
        }
        else if (event.getTabKey() == CreativeModeTabs.COMBAT){
            event.accept(ModItems.DRAGONITE_SWORD);
            event.accept(ModItems.DEVILSKNIFE);
        }
        else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
            event.accept(ModItems.DRAGONITE_PICKAXE);
            event.accept(ModItems.DRAGONITE_AXE);
            event.accept(ModItems.DRAGONITE_SHOVEL);
            event.accept(ModItems.DRAGONITE_HOE);
        }

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void entityRenderers(final EntityRenderersEvent.RegisterRenderers event){
            ModEntities.entityRenderers(event);
        }
        @SubscribeEvent
        public static void attributeRegister(EntityAttributeCreationEvent event) {
            ModEntities.onAttributeCreate(event);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){
            //event.registerLayerDefinition(CuteAlienModel.LAYER_LOCATION, CuteAlienModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerGUIOverlays(final RegisterGuiOverlaysEvent event){

            //event.registerBelowAll("freddy_mask", FreddyMaskOverlay.FREDDY_MASK_OVERLAY);

        }

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            //LOGGER.info("HELLO FROM CLIENT SETUP");
            //LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
