package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
   public Codec<LightningStrikeTrigger.TriggerInstance> codec() {
      return LightningStrikeTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer p_153392_, LightningBolt p_153393_, List<Entity> p_153394_) {
      List<LootContext> list = p_153394_.stream().map((p_153390_) -> {
         return EntityPredicate.createContext(p_153392_, p_153390_);
      }).collect(Collectors.toList());
      LootContext lootcontext = EntityPredicate.createContext(p_153392_, p_153393_);
      this.trigger(p_153392_, (p_153402_) -> {
         return p_153402_.matches(lootcontext, list);
      });
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> lightning, Optional<ContextAwarePredicate> bystander) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<LightningStrikeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((p_308142_) -> {
         return p_308142_.group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(LightningStrikeTrigger.TriggerInstance::player), ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "lightning").forGetter(LightningStrikeTrigger.TriggerInstance::lightning), ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "bystander").forGetter(LightningStrikeTrigger.TriggerInstance::bystander)).apply(p_308142_, LightningStrikeTrigger.TriggerInstance::new);
      });

      public static Criterion<LightningStrikeTrigger.TriggerInstance> lightningStrike(Optional<EntityPredicate> p_301310_, Optional<EntityPredicate> p_299336_) {
         return CriteriaTriggers.LIGHTNING_STRIKE.createCriterion(new LightningStrikeTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_301310_), EntityPredicate.wrap(p_299336_)));
      }

      public boolean matches(LootContext p_153419_, List<LootContext> p_153420_) {
         if (this.lightning.isPresent() && !this.lightning.get().matches(p_153419_)) {
            return false;
         } else {
            return !this.bystander.isPresent() || !p_153420_.stream().noneMatch(this.bystander.get()::matches);
         }
      }

      public void validate(CriterionValidator p_312134_) {
         SimpleCriterionTrigger.SimpleInstance.super.validate(p_312134_);
         p_312134_.validateEntity(this.lightning, ".lightning");
         p_312134_.validateEntity(this.bystander, ".bystander");
      }

      public Optional<ContextAwarePredicate> player() {
         return this.player;
      }
   }
}