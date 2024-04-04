package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
   public Codec<BrewedPotionTrigger.TriggerInstance> codec() {
      return BrewedPotionTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer p_19121_, Holder<Potion> p_311358_) {
      this.trigger(p_19121_, (p_308115_) -> {
         return p_308115_.matches(p_311358_);
      });
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Potion>> potion) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<BrewedPotionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((p_310242_) -> {
         return p_310242_.group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(BrewedPotionTrigger.TriggerInstance::player), ExtraCodecs.strictOptionalField(BuiltInRegistries.POTION.holderByNameCodec(), "potion").forGetter(BrewedPotionTrigger.TriggerInstance::potion)).apply(p_310242_, BrewedPotionTrigger.TriggerInstance::new);
      });

      public static Criterion<BrewedPotionTrigger.TriggerInstance> brewedPotion() {
         return CriteriaTriggers.BREWED_POTION.createCriterion(new BrewedPotionTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public boolean matches(Holder<Potion> p_311152_) {
         return !this.potion.isPresent() || this.potion.get().equals(p_311152_);
      }

      public Optional<ContextAwarePredicate> player() {
         return this.player;
      }
   }
}