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
    public void render(DevilsknifeEntity entity, float pEntityYaw, float pPartialTick, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight) {
        ItemStack itemStack = entity.getPickupItemStackOrigin();
        Level level = entity.level();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        //poseStack.mulPose(Axis.ZN.rotationDegrees(90));
        if (itemStack.getItem() != Items.AIR){
            BakedModel bakedmodel = this.itemRenderer.getModel(itemStack, entity.level(), null, entity.getId());
            this.itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, poseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
        }
        else {
            super.render(entity, pEntityYaw, pPartialTick, poseStack, pBuffer, pPackedLight);
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DevilsknifeEntity p_114482_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
