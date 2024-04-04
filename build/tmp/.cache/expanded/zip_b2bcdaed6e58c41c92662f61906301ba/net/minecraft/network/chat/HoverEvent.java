package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HoverEvent {
   public static final Codec<HoverEvent> CODEC = Codec.either(HoverEvent.TypedHoverEvent.CODEC.codec(), HoverEvent.TypedHoverEvent.LEGACY_CODEC.codec()).xmap((p_310363_) -> {
      return new HoverEvent(p_310363_.map((p_311007_) -> {
         return p_311007_;
      }, (p_310641_) -> {
         return p_310641_;
      }));
   }, (p_311162_) -> {
      return Either.left(p_311162_.event);
   });
   private final HoverEvent.TypedHoverEvent<?> event;

   public <T> HoverEvent(HoverEvent.Action<T> p_130818_, T p_130819_) {
      this(new HoverEvent.TypedHoverEvent<>(p_130818_, p_130819_));
   }

   private HoverEvent(HoverEvent.TypedHoverEvent<?> p_313245_) {
      this.event = p_313245_;
   }

   public HoverEvent.Action<?> getAction() {
      return this.event.action;
   }

   @Nullable
   public <T> T getValue(HoverEvent.Action<T> p_130824_) {
      return (T)(this.event.action == p_130824_ ? p_130824_.cast(this.event.value) : null);
   }

   public boolean equals(Object p_130828_) {
      if (this == p_130828_) {
         return true;
      } else {
         return p_130828_ != null && this.getClass() == p_130828_.getClass() ? ((HoverEvent)p_130828_).event.equals(this.event) : false;
      }
   }

   public String toString() {
      return this.event.toString();
   }

   public int hashCode() {
      return this.event.hashCode();
   }

   public static class Action<T> implements StringRepresentable {
      public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>("show_text", true, ComponentSerialization.CODEC, DataResult::success);
      public static final HoverEvent.Action<HoverEvent.ItemStackInfo> SHOW_ITEM = new HoverEvent.Action<>("show_item", true, HoverEvent.ItemStackInfo.CODEC, HoverEvent.ItemStackInfo::legacyCreate);
      public static final HoverEvent.Action<HoverEvent.EntityTooltipInfo> SHOW_ENTITY = new HoverEvent.Action<>("show_entity", true, HoverEvent.EntityTooltipInfo.CODEC, HoverEvent.EntityTooltipInfo::legacyCreate);
      public static final Codec<HoverEvent.Action<?>> UNSAFE_CODEC = StringRepresentable.fromValues(() -> {
         return new HoverEvent.Action[]{SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY};
      });
      public static final Codec<HoverEvent.Action<?>> CODEC = ExtraCodecs.validate(UNSAFE_CODEC, HoverEvent.Action::filterForSerialization);
      private final String name;
      private final boolean allowFromServer;
      final Codec<HoverEvent.TypedHoverEvent<T>> codec;
      final Codec<HoverEvent.TypedHoverEvent<T>> legacyCodec;

      public Action(String p_130842_, boolean p_130843_, Codec<T> p_311195_, Function<Component, DataResult<T>> p_130844_) {
         this.name = p_130842_;
         this.allowFromServer = p_130843_;
         this.codec = p_311195_.xmap((p_308563_) -> {
            return new HoverEvent.TypedHoverEvent<>(this, p_308563_);
         }, (p_308564_) -> {
            return p_308564_.value;
         }).fieldOf("contents").codec();
         this.legacyCodec = Codec.of(Encoder.error("Can't encode in legacy format"), ComponentSerialization.CODEC.flatMap(p_130844_).map((p_308565_) -> {
            return new HoverEvent.TypedHoverEvent<>(this, p_308565_);
         }));
      }

      public boolean isAllowedFromServer() {
         return this.allowFromServer;
      }

      public String getSerializedName() {
         return this.name;
      }

      T cast(Object p_130865_) {
         return (T)p_130865_;
      }

      public String toString() {
         return "<action " + this.name + ">";
      }

      private static DataResult<HoverEvent.Action<?>> filterForSerialization(@Nullable HoverEvent.Action<?> p_311888_) {
         if (p_311888_ == null) {
            return DataResult.error(() -> {
               return "Unknown action";
            });
         } else {
            return !p_311888_.isAllowedFromServer() ? DataResult.error(() -> {
               return "Action not allowed: " + p_311888_;
            }) : DataResult.success(p_311888_, Lifecycle.stable());
         }
      }
   }

   public static class EntityTooltipInfo {
      public static final Codec<HoverEvent.EntityTooltipInfo> CODEC = RecordCodecBuilder.create((p_310594_) -> {
         return p_310594_.group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter((p_309982_) -> {
            return p_309982_.type;
         }), UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter((p_312795_) -> {
            return p_312795_.id;
         }), ExtraCodecs.strictOptionalField(ComponentSerialization.CODEC, "name").forGetter((p_310270_) -> {
            return p_310270_.name;
         })).apply(p_310594_, HoverEvent.EntityTooltipInfo::new);
      });
      public final EntityType<?> type;
      public final UUID id;
      public final Optional<Component> name;
      @Nullable
      private List<Component> linesCache;

      public EntityTooltipInfo(EntityType<?> p_130876_, UUID p_130877_, @Nullable Component p_130878_) {
         this(p_130876_, p_130877_, Optional.ofNullable(p_130878_));
      }

      public EntityTooltipInfo(EntityType<?> p_312321_, UUID p_312750_, Optional<Component> p_312078_) {
         this.type = p_312321_;
         this.id = p_312750_;
         this.name = p_312078_;
      }

      public static DataResult<HoverEvent.EntityTooltipInfo> legacyCreate(Component p_312203_) {
         try {
            CompoundTag compoundtag = TagParser.parseTag(p_312203_.getString());
            Component component = Component.Serializer.fromJson(compoundtag.getString("name"));
            EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(compoundtag.getString("type")));
            UUID uuid = UUID.fromString(compoundtag.getString("id"));
            return DataResult.success(new HoverEvent.EntityTooltipInfo(entitytype, uuid, component));
         } catch (Exception exception) {
            return DataResult.error(() -> {
               return "Failed to parse tooltip: " + exception.getMessage();
            });
         }
      }

      public List<Component> getTooltipLines() {
         if (this.linesCache == null) {
            this.linesCache = new ArrayList<>();
            this.name.ifPresent(this.linesCache::add);
            this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
            this.linesCache.add(Component.literal(this.id.toString()));
         }

         return this.linesCache;
      }

      public boolean equals(Object p_130886_) {
         if (this == p_130886_) {
            return true;
         } else if (p_130886_ != null && this.getClass() == p_130886_.getClass()) {
            HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = (HoverEvent.EntityTooltipInfo)p_130886_;
            return this.type.equals(hoverevent$entitytooltipinfo.type) && this.id.equals(hoverevent$entitytooltipinfo.id) && this.name.equals(hoverevent$entitytooltipinfo.name);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.type.hashCode();
         i = 31 * i + this.id.hashCode();
         return 31 * i + this.name.hashCode();
      }
   }

   public static class ItemStackInfo {
      public static final Codec<HoverEvent.ItemStackInfo> FULL_CODEC = RecordCodecBuilder.create((p_309880_) -> {
         return p_309880_.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter((p_309680_) -> {
            return p_309680_.item;
         }), ExtraCodecs.strictOptionalField(Codec.INT, "count", 1).forGetter((p_311217_) -> {
            return p_311217_.count;
         }), ExtraCodecs.strictOptionalField(TagParser.AS_CODEC, "tag").forGetter((p_311651_) -> {
            return p_311651_.tag;
         })).apply(p_309880_, HoverEvent.ItemStackInfo::new);
      });
      public static final Codec<HoverEvent.ItemStackInfo> CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), FULL_CODEC).xmap((p_311541_) -> {
         return p_311541_.map((p_311266_) -> {
            return new HoverEvent.ItemStackInfo(p_311266_, 1, Optional.empty());
         }, (p_311381_) -> {
            return p_311381_;
         });
      }, Either::right);
      private final Item item;
      private final int count;
      private final Optional<CompoundTag> tag;
      @Nullable
      private ItemStack itemStack;

      ItemStackInfo(Item p_130893_, int p_130894_, @Nullable CompoundTag p_130895_) {
         this(p_130893_, p_130894_, Optional.ofNullable(p_130895_));
      }

      ItemStackInfo(Item p_311378_, int p_311558_, Optional<CompoundTag> p_312925_) {
         this.item = p_311378_;
         this.count = p_311558_;
         this.tag = p_312925_;
      }

      public ItemStackInfo(ItemStack p_130897_) {
         this(p_130897_.getItem(), p_130897_.getCount(), p_130897_.getTag() != null ? Optional.of(p_130897_.getTag().copy()) : Optional.empty());
      }

      public boolean equals(Object p_130911_) {
         if (this == p_130911_) {
            return true;
         } else if (p_130911_ != null && this.getClass() == p_130911_.getClass()) {
            HoverEvent.ItemStackInfo hoverevent$itemstackinfo = (HoverEvent.ItemStackInfo)p_130911_;
            return this.count == hoverevent$itemstackinfo.count && this.item.equals(hoverevent$itemstackinfo.item) && this.tag.equals(hoverevent$itemstackinfo.tag);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.item.hashCode();
         i = 31 * i + this.count;
         return 31 * i + this.tag.hashCode();
      }

      public ItemStack getItemStack() {
         if (this.itemStack == null) {
            this.itemStack = new ItemStack(this.item, this.count);
            this.tag.ifPresent(this.itemStack::setTag);
         }

         return this.itemStack;
      }

      private static DataResult<HoverEvent.ItemStackInfo> legacyCreate(Component p_309792_) {
         try {
            CompoundTag compoundtag = TagParser.parseTag(p_309792_.getString());
            return DataResult.success(new HoverEvent.ItemStackInfo(ItemStack.of(compoundtag)));
         } catch (CommandSyntaxException commandsyntaxexception) {
            return DataResult.error(() -> {
               return "Failed to parse item tag: " + commandsyntaxexception.getMessage();
            });
         }
      }
   }

   static record TypedHoverEvent<T>(HoverEvent.Action<T> action, T value) {
      public static final MapCodec<HoverEvent.TypedHoverEvent<?>> CODEC = HoverEvent.Action.CODEC.dispatchMap("action", HoverEvent.TypedHoverEvent::action, (p_312897_) -> {
         return p_312897_.codec;
      });
      public static final MapCodec<HoverEvent.TypedHoverEvent<?>> LEGACY_CODEC = HoverEvent.Action.CODEC.dispatchMap("action", HoverEvent.TypedHoverEvent::action, (p_310775_) -> {
         return p_310775_.legacyCodec;
      });
   }
}