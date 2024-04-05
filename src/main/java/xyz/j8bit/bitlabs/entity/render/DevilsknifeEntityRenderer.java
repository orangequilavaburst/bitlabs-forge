package xyz.j8bit.bitlabs.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import xyz.j8bit.bitlabs.entity.custom.DevilsknifeEntity;

public class DevilsknifeEntityRenderer extends EntityRenderer<DevilsknifeEntity> {

    private final ItemRenderer itemRenderer;

    public DevilsknifeEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.shadowRadius = 1.0F;
        this.itemRenderer = pContext.getItemRenderer();
    }

    @Override
    public void render(DevilsknifeEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        ItemStack itemStack = pEntity.getPickupItemStackOrigin();
        Level level = pEntity.level();
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YN.rotationDegrees(Mth.lerp(pPartialTick, pEntity.yRotO, pEntity.getYRot())));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(pPartialTick, pEntity.xRotO, pEntity.getXRot())));
        if (itemStack.getItem() != Items.AIR){

            BakedModel bakedmodel = this.itemRenderer.getModel(itemStack, pEntity.level(), (LivingEntity)null, pEntity.getId());
            pPoseStack.popPose();
            this.itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY, bakedmodel);

        }
        else {
            pPoseStack.popPose();
            super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(DevilsknifeEntity p_114482_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
