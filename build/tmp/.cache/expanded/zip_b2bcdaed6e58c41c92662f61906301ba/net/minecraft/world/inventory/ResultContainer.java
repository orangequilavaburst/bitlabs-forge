package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ResultContainer implements Container, RecipeCraftingHolder {
   private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
   @Nullable
   private RecipeHolder<?> recipeUsed;

   public int getContainerSize() {
      return 1;
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.itemStacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int p_40147_) {
      return this.itemStacks.get(0);
   }

   public ItemStack removeItem(int p_40149_, int p_40150_) {
      return ContainerHelper.takeItem(this.itemStacks, 0);
   }

   public ItemStack removeItemNoUpdate(int p_40160_) {
      return ContainerHelper.takeItem(this.itemStacks, 0);
   }

   public void setItem(int p_40152_, ItemStack p_40153_) {
      this.itemStacks.set(0, p_40153_);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player p_40155_) {
      return true;
   }

   public void clearContent() {
      this.itemStacks.clear();
   }

   public void setRecipeUsed(@Nullable RecipeHolder<?> p_297508_) {
      this.recipeUsed = p_297508_;
   }

   @Nullable
   public RecipeHolder<?> getRecipeUsed() {
      return this.recipeUsed;
   }
}