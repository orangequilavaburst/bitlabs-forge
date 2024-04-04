package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;

public record ShapedRecipePattern(int width, int height, NonNullList<Ingredient> ingredients, Optional<ShapedRecipePattern.Data> data) {
   private static final int MAX_SIZE = 3;
   public static final MapCodec<ShapedRecipePattern> MAP_CODEC = ShapedRecipePattern.Data.MAP_CODEC.flatXmap(ShapedRecipePattern::unpack, (p_310854_) -> {
      return p_310854_.data().map(DataResult::success).orElseGet(() -> {
         return DataResult.error(() -> {
            return "Cannot encode unpacked recipe";
         });
      });
   });

   public static ShapedRecipePattern of(Map<Character, Ingredient> p_310983_, String... p_310430_) {
      return of(p_310983_, List.of(p_310430_));
   }

   public static ShapedRecipePattern of(Map<Character, Ingredient> p_313226_, List<String> p_310089_) {
      ShapedRecipePattern.Data shapedrecipepattern$data = new ShapedRecipePattern.Data(p_313226_, p_310089_);
      return Util.getOrThrow(unpack(shapedrecipepattern$data), IllegalArgumentException::new);
   }

   private static DataResult<ShapedRecipePattern> unpack(ShapedRecipePattern.Data p_312333_) {
      String[] astring = shrink(p_312333_.pattern);
      int i = astring[0].length();
      int j = astring.length;
      NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);
      CharSet charset = new CharArraySet(p_312333_.key.keySet());

      for(int k = 0; k < astring.length; ++k) {
         String s = astring[k];

         for(int l = 0; l < s.length(); ++l) {
            char c0 = s.charAt(l);
            Ingredient ingredient = c0 == ' ' ? Ingredient.EMPTY : p_312333_.key.get(c0);
            if (ingredient == null) {
               return DataResult.error(() -> {
                  return "Pattern references symbol '" + c0 + "' but it's not defined in the key";
               });
            }

            charset.remove(c0);
            nonnulllist.set(l + i * k, ingredient);
         }
      }

      return !charset.isEmpty() ? DataResult.error(() -> {
         return "Key defines symbols that aren't used in pattern: " + charset;
      }) : DataResult.success(new ShapedRecipePattern(i, j, nonnulllist, Optional.of(p_312333_)));
   }

   @VisibleForTesting
   static String[] shrink(List<String> p_311492_) {
      int i = Integer.MAX_VALUE;
      int j = 0;
      int k = 0;
      int l = 0;

      for(int i1 = 0; i1 < p_311492_.size(); ++i1) {
         String s = p_311492_.get(i1);
         i = Math.min(i, firstNonSpace(s));
         int j1 = lastNonSpace(s);
         j = Math.max(j, j1);
         if (j1 < 0) {
            if (k == i1) {
               ++k;
            }

            ++l;
         } else {
            l = 0;
         }
      }

      if (p_311492_.size() == l) {
         return new String[0];
      } else {
         String[] astring = new String[p_311492_.size() - l - k];

         for(int k1 = 0; k1 < astring.length; ++k1) {
            astring[k1] = p_311492_.get(k1 + k).substring(i, j + 1);
         }

         return astring;
      }
   }

   private static int firstNonSpace(String p_309836_) {
      int i;
      for(i = 0; i < p_309836_.length() && p_309836_.charAt(i) == ' '; ++i) {
      }

      return i;
   }

   private static int lastNonSpace(String p_312853_) {
      int i;
      for(i = p_312853_.length() - 1; i >= 0 && p_312853_.charAt(i) == ' '; --i) {
      }

      return i;
   }

   public boolean matches(CraftingContainer p_310690_) {
      for(int i = 0; i <= p_310690_.getWidth() - this.width; ++i) {
         for(int j = 0; j <= p_310690_.getHeight() - this.height; ++j) {
            if (this.matches(p_310690_, i, j, true)) {
               return true;
            }

            if (this.matches(p_310690_, i, j, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean matches(CraftingContainer p_313091_, int p_311269_, int p_310676_, boolean p_313153_) {
      for(int i = 0; i < p_313091_.getWidth(); ++i) {
         for(int j = 0; j < p_313091_.getHeight(); ++j) {
            int k = i - p_311269_;
            int l = j - p_310676_;
            Ingredient ingredient = Ingredient.EMPTY;
            if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
               if (p_313153_) {
                  ingredient = this.ingredients.get(this.width - k - 1 + l * this.width);
               } else {
                  ingredient = this.ingredients.get(k + l * this.width);
               }
            }

            if (!ingredient.test(p_313091_.getItem(i + j * p_313091_.getWidth()))) {
               return false;
            }
         }
      }

      return true;
   }

   public void toNetwork(FriendlyByteBuf p_312386_) {
      p_312386_.writeVarInt(this.width);
      p_312386_.writeVarInt(this.height);

      for(Ingredient ingredient : this.ingredients) {
         ingredient.toNetwork(p_312386_);
      }

   }

   public static ShapedRecipePattern fromNetwork(FriendlyByteBuf p_310753_) {
      int i = p_310753_.readVarInt();
      int j = p_310753_.readVarInt();
      NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);
      nonnulllist.replaceAll((p_309588_) -> {
         return Ingredient.fromNetwork(p_310753_);
      });
      return new ShapedRecipePattern(i, j, nonnulllist, Optional.empty());
   }

   public static record Data(Map<Character, Ingredient> key, List<String> pattern) {
      private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap((p_311191_) -> {
         if (p_311191_.size() > ShapedRecipe.MAX_HEIGHT) {
            return DataResult.error(() -> {
               return "Invalid pattern: too many rows, " + ShapedRecipe.MAX_HEIGHT + " is maximum";
            });
         } else if (p_311191_.isEmpty()) {
            return DataResult.error(() -> {
               return "Invalid pattern: empty pattern not allowed";
            });
         } else {
            int i = p_311191_.get(0).length();

            for(String s : p_311191_) {
               if (s.length() > ShapedRecipe.MAX_WIDTH) {
                  return DataResult.error(() -> {
                     return "Invalid pattern: too many columns, " + ShapedRecipe.MAX_WIDTH + " is maximum";
                  });
               }

               if (i != s.length()) {
                  return DataResult.error(() -> {
                     return "Invalid pattern: each row must be the same width";
                  });
               }
            }

            return DataResult.success(p_311191_);
         }
      }, Function.identity());
      private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap((p_313217_) -> {
         if (p_313217_.length() != 1) {
            return DataResult.error(() -> {
               return "Invalid key entry: '" + p_313217_ + "' is an invalid symbol (must be 1 character only).";
            });
         } else {
            return " ".equals(p_313217_) ? DataResult.error(() -> {
               return "Invalid key entry: ' ' is a reserved symbol.";
            }) : DataResult.success(p_313217_.charAt(0));
         }
      }, String::valueOf);
      public static final MapCodec<ShapedRecipePattern.Data> MAP_CODEC = RecordCodecBuilder.mapCodec((p_310577_) -> {
         return p_310577_.group(ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC_NONEMPTY).fieldOf("key").forGetter((p_311797_) -> {
            return p_311797_.key;
         }), PATTERN_CODEC.fieldOf("pattern").forGetter((p_309770_) -> {
            return p_309770_.pattern;
         })).apply(p_310577_, ShapedRecipePattern.Data::new);
      });
   }
}
