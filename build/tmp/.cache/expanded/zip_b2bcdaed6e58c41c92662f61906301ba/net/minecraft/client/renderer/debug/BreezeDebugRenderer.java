package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BreezeDebugRenderer {
   private static final int JUMP_TARGET_LINE_COLOR = FastColor.ARGB32.color(255, 255, 100, 255);
   private static final int TARGET_LINE_COLOR = FastColor.ARGB32.color(255, 100, 255, 255);
   private static final int INNER_CIRCLE_COLOR = FastColor.ARGB32.color(255, 0, 255, 0);
   private static final int MIDDLE_CIRCLE_COLOR = FastColor.ARGB32.color(255, 255, 165, 0);
   private static final int OUTER_CIRCLE_COLOR = FastColor.ARGB32.color(255, 255, 0, 0);
   private static final int CIRCLE_VERTICES = 20;
   private static final float SEGMENT_SIZE_RADIANS = ((float)Math.PI / 10F);
   private final Minecraft minecraft;
   private final Map<Integer, BreezeDebugPayload.BreezeInfo> perEntity = new HashMap<>();

   public BreezeDebugRenderer(Minecraft p_312673_) {
      this.minecraft = p_312673_;
   }

   public void render(PoseStack p_311387_, MultiBufferSource p_310722_, double p_312623_, double p_310151_, double p_312438_) {
      LocalPlayer localplayer = this.minecraft.player;
      localplayer.level().getEntities(EntityType.BREEZE, localplayer.getBoundingBox().inflate(100.0D), (p_312249_) -> {
         return true;
      }).forEach((p_313067_) -> {
         Optional<BreezeDebugPayload.BreezeInfo> optional = Optional.ofNullable(this.perEntity.get(p_313067_.getId()));
         optional.map(BreezeDebugPayload.BreezeInfo::attackTarget).map((p_312408_) -> {
            return localplayer.level().getEntity(p_312408_);
         }).map((p_311009_) -> {
            return p_311009_.getPosition(this.minecraft.getFrameTime());
         }).ifPresent((p_310972_) -> {
            drawLine(p_311387_, p_310722_, p_312623_, p_310151_, p_312438_, p_313067_.position(), p_310972_, TARGET_LINE_COLOR);
            Vec3 vec3 = p_310972_.add(0.0D, (double)0.01F, 0.0D);
            drawCircle(p_311387_.last().pose(), p_312623_, p_310151_, p_312438_, p_310722_.getBuffer(RenderType.debugLineStrip(2.0D)), vec3, 4.0F, INNER_CIRCLE_COLOR);
            drawCircle(p_311387_.last().pose(), p_312623_, p_310151_, p_312438_, p_310722_.getBuffer(RenderType.debugLineStrip(2.0D)), vec3, 8.0F, MIDDLE_CIRCLE_COLOR);
            drawCircle(p_311387_.last().pose(), p_312623_, p_310151_, p_312438_, p_310722_.getBuffer(RenderType.debugLineStrip(2.0D)), vec3, 20.0F, OUTER_CIRCLE_COLOR);
         });
         optional.map(BreezeDebugPayload.BreezeInfo::jumpTarget).ifPresent((p_310100_) -> {
            drawLine(p_311387_, p_310722_, p_312623_, p_310151_, p_312438_, p_313067_.position(), p_310100_.getCenter(), JUMP_TARGET_LINE_COLOR);
            DebugRenderer.renderFilledBox(p_311387_, p_310722_, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(p_310100_)).move(-p_312623_, -p_310151_, -p_312438_), 1.0F, 0.0F, 0.0F, 1.0F);
         });
      });
   }

   private static void drawLine(PoseStack p_310860_, MultiBufferSource p_311050_, double p_312740_, double p_310856_, double p_311669_, Vec3 p_309935_, Vec3 p_311298_, int p_312664_) {
      VertexConsumer vertexconsumer = p_311050_.getBuffer(RenderType.debugLineStrip(2.0D));
      vertexconsumer.vertex(p_310860_.last().pose(), (float)(p_309935_.x - p_312740_), (float)(p_309935_.y - p_310856_), (float)(p_309935_.z - p_311669_)).color(p_312664_).endVertex();
      vertexconsumer.vertex(p_310860_.last().pose(), (float)(p_311298_.x - p_312740_), (float)(p_311298_.y - p_310856_), (float)(p_311298_.z - p_311669_)).color(p_312664_).endVertex();
   }

   private static void drawCircle(Matrix4f p_309536_, double p_312264_, double p_310099_, double p_311317_, VertexConsumer p_310217_, Vec3 p_311990_, float p_311488_, int p_309735_) {
      for(int i = 0; i < 20; ++i) {
         drawCircleVertex(i, p_309536_, p_312264_, p_310099_, p_311317_, p_310217_, p_311990_, p_311488_, p_309735_);
      }

      drawCircleVertex(0, p_309536_, p_312264_, p_310099_, p_311317_, p_310217_, p_311990_, p_311488_, p_309735_);
   }

   private static void drawCircleVertex(int p_313136_, Matrix4f p_311552_, double p_312433_, double p_309912_, double p_312340_, VertexConsumer p_311728_, Vec3 p_312252_, float p_311583_, int p_312406_) {
      float f = (float)p_313136_ * ((float)Math.PI / 10F);
      Vec3 vec3 = p_312252_.add((double)p_311583_ * Math.cos((double)f), 0.0D, (double)p_311583_ * Math.sin((double)f));
      p_311728_.vertex(p_311552_, (float)(vec3.x - p_312433_), (float)(vec3.y - p_309912_), (float)(vec3.z - p_312340_)).color(p_312406_).endVertex();
   }

   public void clear() {
      this.perEntity.clear();
   }

   public void add(BreezeDebugPayload.BreezeInfo p_313013_) {
      this.perEntity.put(p_313013_.id(), p_313013_);
   }
}