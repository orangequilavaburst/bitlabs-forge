package xyz.j8bit.bitlabs.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import xyz.j8bit.bitlabs.BitLabsMod;
import xyz.j8bit.bitlabs.util.ModTags;

import java.util.List;

public class ModToolTiers {

    public static final Tier DRAGONITE = TierSortingRegistry.registerTier(
            new ForgeTier(5, 4000, 8f, 5f, 10, ModTags.Blocks.NEEDS_DRAGONITE_TOOL, () -> Ingredient.of(ModItems.DRAGONITE_INGOT.get())),
            new ResourceLocation(BitLabsMod.MOD_ID, "dragonite"), List.of(Tiers.NETHERITE), List.of());

}
