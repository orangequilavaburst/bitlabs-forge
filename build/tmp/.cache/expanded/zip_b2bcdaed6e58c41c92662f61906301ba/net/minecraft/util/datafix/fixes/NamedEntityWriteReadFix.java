package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.Util;

public abstract class NamedEntityWriteReadFix extends DataFix {
   private final String name;
   private final String entityName;
   private final DSL.TypeReference type;

   public NamedEntityWriteReadFix(Schema p_310297_, boolean p_312818_, String p_313129_, DSL.TypeReference p_311108_, String p_313092_) {
      super(p_310297_, p_312818_);
      this.name = p_313129_;
      this.type = p_311108_;
      this.entityName = p_313092_;
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(this.type);
      Type<?> type1 = this.getInputSchema().getChoiceType(this.type, this.entityName);
      Type<?> type2 = this.getOutputSchema().getType(this.type);
      Type<?> type3 = this.getOutputSchema().getChoiceType(this.type, this.entityName);
      OpticFinder<?> opticfinder = DSL.namedChoice(this.entityName, type1);
      return this.fixTypeEverywhereTyped(this.name, type, type2, (p_312299_) -> {
         return p_312299_.updateTyped(opticfinder, type3, (p_312582_) -> {
            return Util.writeAndReadTypedOrThrow(p_312582_, type3, this::fix);
         });
      });
   }

   protected abstract <T> Dynamic<T> fix(Dynamic<T> p_310304_);
}