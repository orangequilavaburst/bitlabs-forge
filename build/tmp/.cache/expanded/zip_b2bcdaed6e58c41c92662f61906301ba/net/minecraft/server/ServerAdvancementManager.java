package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).create();
   private Map<ResourceLocation, AdvancementHolder> advancements = Map.of();
   private AdvancementTree tree = new AdvancementTree();
   private final LootDataManager lootData;
   private final net.minecraftforge.common.crafting.conditions.ICondition.IContext context; //Forge: add context

   /** @deprecated Forge: use {@linkplain ServerAdvancementManager#ServerAdvancementManager(LootDataManager, net.minecraftforge.common.crafting.conditions.ICondition.IContext) constructor with context}. */
   @Deprecated
   public ServerAdvancementManager(LootDataManager p_279237_) {
      this(p_279237_, net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
   }

   public ServerAdvancementManager(LootDataManager p_279237_, net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
      super(GSON, "advancements");
      this.lootData = p_279237_;
      this.context = context;
   }

   protected void apply(Map<ResourceLocation, JsonElement> p_136034_, ResourceManager p_136035_, ProfilerFiller p_136036_) {
      ImmutableMap.Builder<ResourceLocation, AdvancementHolder> builder = ImmutableMap.builder();
      p_136034_.forEach((p_308595_, p_308596_) -> {
         try {
            var json = net.minecraftforge.common.ForgeHooks.readConditionalAdvancement(null, (com.google.gson.JsonObject)p_308596_);
            if (json == null) {
                LOGGER.debug("Skipping loading advancement {} as its conditions were not met", p_308595_);
                return;
            }
            Advancement advancement = Util.getOrThrow(Advancement.CODEC.parse(JsonOps.INSTANCE, json), JsonParseException::new);
            this.validate(p_308595_, advancement);
            builder.put(p_308595_, new AdvancementHolder(p_308595_, advancement));
         } catch (Exception exception) {
            LOGGER.error("Parsing error loading custom advancement {}: {}", p_308595_, exception.getMessage());
         }

      });
      this.advancements = builder.buildOrThrow();
      AdvancementTree advancementtree = new AdvancementTree();
      advancementtree.addAll(this.advancements.values());

      for(AdvancementNode advancementnode : advancementtree.roots()) {
         if (advancementnode.holder().value().display().isPresent()) {
            TreeNodePosition.run(advancementnode);
         }
      }

      this.tree = advancementtree;
   }

   private void validate(ResourceLocation p_309906_, Advancement p_310937_) {
      ProblemReporter.Collector problemreporter$collector = new ProblemReporter.Collector();
      p_310937_.validate(problemreporter$collector, this.lootData);
      Multimap<String, String> multimap = problemreporter$collector.get();
      if (!multimap.isEmpty()) {
         String s = multimap.asMap().entrySet().stream().map((p_308593_) -> {
            return "  at " + (String)p_308593_.getKey() + ": " + String.join("; ", p_308593_.getValue());
         }).collect(Collectors.joining("\n"));
         LOGGER.warn("Found validation problems in advancement {}: \n{}", p_309906_, s);
      }

   }

   @Nullable
   public AdvancementHolder get(ResourceLocation p_299615_) {
      return this.advancements.get(p_299615_);
   }

   public AdvancementTree tree() {
      return this.tree;
   }

   public Collection<AdvancementHolder> getAllAdvancements() {
      return this.advancements.values();
   }
}
