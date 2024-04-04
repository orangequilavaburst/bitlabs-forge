package net.minecraft.world.level.block.state.properties;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum DoubleBlockHalf implements StringRepresentable {
   UPPER(Direction.DOWN),
   LOWER(Direction.UP);

   private final Direction directionToOther;

   private DoubleBlockHalf(Direction p_312507_) {
      this.directionToOther = p_312507_;
   }

   public Direction getDirectionToOther() {
      return this.directionToOther;
   }

   public String toString() {
      return this.getSerializedName();
   }

   public String getSerializedName() {
      return this == UPPER ? "upper" : "lower";
   }

   public DoubleBlockHalf getOtherHalf() {
      return this == UPPER ? LOWER : UPPER;
   }
}