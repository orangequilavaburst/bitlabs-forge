package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
   public Codec<RecipeUnlockedTrigger.TriggerInstance> codec() {
      return RecipeUnlockedTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer p_63719_, RecipeHolder<?> p_300165_) {
      this.trigger(p_63719_, (p_296143_) -> {
         return p_296143_.matches(p_300165_);
      });
   }

   public static Criterion<RecipeUnlockedTrigger.TriggerInstance> unlocked(ResourceLocation p_63729_) {
      return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new RecipeUnlockedTrigger.TriggerInstance(Optional.empty(), p_63729_));
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation recipe) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<RecipeUnlockedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((p_312171_) -> {
         return p_312171_.group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(RecipeUnlockedTrigger.TriggerInstance::player), ResourceLocation.CODEC.fieldOf("recipe").forGetter(RecipeUnlockedTrigger.TriggerInstance::recipe)).apply(p_312171_, RecipeUnlockedTrigger.TriggerInstance::new);
      });

      public boolean matches(RecipeHolder<?> p_299959_) {
         return this.recipe.equals(p_299959_.id());
      }

      public Optional<ContextAwarePredicate> player() {
         return this.player;
      }
   }
}