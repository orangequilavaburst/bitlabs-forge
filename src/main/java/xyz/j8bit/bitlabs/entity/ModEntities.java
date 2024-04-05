package xyz.j8bit.bitlabs.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.j8bit.bitlabs.BitLabsMod;
import xyz.j8bit.bitlabs.entity.custom.DevilsknifeEntity;
import xyz.j8bit.bitlabs.entity.render.DevilsknifeEntityRenderer;

public class ModEntities {

    // make the register
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BitLabsMod.MOD_ID);

    /*
    public static final RegistryObject<EntityType<BlockProjectileEntity>> BLOCK_PROJECTILE_ENTITY = ENTITY_TYPES.register("block_projectile",
            () -> EntityType.Builder.of((EntityType.EntityFactory<BlockProjectileEntity>) BlockProjectileEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).build(new ResourceLocation(BaseMod.MODID, "block_projectile").toString()));
     */

    public static final RegistryObject<EntityType<DevilsknifeEntity>> DEVILSKNIFE_ENTITY = ENTITY_TYPES.register("devilsknife",
            () -> EntityType.Builder.of((EntityType.EntityFactory<DevilsknifeEntity>) DevilsknifeEntity::new, MobCategory.MISC).sized(1.5f, 1.5f).build(new ResourceLocation(BitLabsMod.MOD_ID, "devilsknife").toString()));

    public static void register(IEventBus bus){
        ENTITY_TYPES.register(bus);
    }

    @SubscribeEvent
    public static void entityRenderers(final EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(ModEntities.DEVILSKNIFE_ENTITY.get(), DevilsknifeEntityRenderer::new);
    }

    // this is different than in 1.16 but everything else is the same
    // I do think this makes more sense than the other way but alas change is usually hard.
    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        //event.put(FRIENDLY_CREEPER.get(), FriendlyCreeperEntity.createAttributes().build());
    }

}
