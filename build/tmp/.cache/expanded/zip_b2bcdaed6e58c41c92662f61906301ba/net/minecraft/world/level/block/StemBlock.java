package net.minecraft.world.level.block;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StemBlock extends BushBlock implements BonemealableBlock {
   public static final MapCodec<StemBlock> CODEC = RecordCodecBuilder.mapCodec((p_311216_) -> {
      return p_311216_.group(ResourceKey.codec(Registries.BLOCK).fieldOf("fruit").forGetter((p_312514_) -> {
         return p_312514_.fruit;
      }), ResourceKey.codec(Registries.BLOCK).fieldOf("attached_stem").forGetter((p_309847_) -> {
         return p_309847_.attachedStem;
      }), ResourceKey.codec(Registries.ITEM).fieldOf("seed").forGetter((p_311480_) -> {
         return p_311480_.seed;
      }), propertiesCodec()).apply(p_311216_, StemBlock::new);
   });
   public static final int MAX_AGE = 7;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
   protected static final float AABB_OFFSET = 1.0F;
   protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
   private final ResourceKey<Block> fruit;
   private final ResourceKey<Block> attachedStem;
   private final ResourceKey<Item> seed;

   public MapCodec<StemBlock> codec() {
      return CODEC;
   }

   public StemBlock(ResourceKey<Block> p_310213_, ResourceKey<Block> p_312966_, ResourceKey<Item> p_312034_, BlockBehaviour.Properties p_154730_) {
      super(p_154730_);
      this.fruit = p_310213_;
      this.attachedStem = p_312966_;
      this.seed = p_312034_;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState p_57047_, BlockGetter p_57048_, BlockPos p_57049_, CollisionContext p_57050_) {
      return SHAPE_BY_AGE[p_57047_.getValue(AGE)];
   }

   protected boolean mayPlaceOn(BlockState p_57053_, BlockGetter p_57054_, BlockPos p_57055_) {
      return p_57053_.is(Blocks.FARMLAND);
   }

   public void randomTick(BlockState p_222538_, ServerLevel p_222539_, BlockPos p_222540_, RandomSource p_222541_) {
      if (!p_222539_.isAreaLoaded(p_222540_, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
      if (p_222539_.getRawBrightness(p_222540_, 0) >= 9) {
         float f = CropBlock.getGrowthSpeed(this, p_222539_, p_222540_);
         if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(p_222539_, p_222540_, p_222538_, p_222541_.nextInt((int)(25.0F / f) + 1) == 0)) {
            int i = p_222538_.getValue(AGE);
            if (i < 7) {
               p_222539_.setBlock(p_222540_, p_222538_.setValue(AGE, Integer.valueOf(i + 1)), 2);
            } else {
               Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(p_222541_);
               BlockPos blockpos = p_222540_.relative(direction);
               BlockState blockstate = p_222539_.getBlockState(blockpos.below());
               // Forge: TODO: Soil blockstate.canSustainPlant(p_222539_, blockpos.below(), Direction.UP, this.fruit)
               if (p_222539_.getBlockState(blockpos).isAir() && (blockstate.is(Blocks.FARMLAND) || blockstate.is(BlockTags.DIRT))) {
                  Registry<Block> registry = p_222539_.registryAccess().registryOrThrow(Registries.BLOCK);
                  Optional<Block> optional = registry.getOptional(this.fruit);
                  Optional<Block> optional1 = registry.getOptional(this.attachedStem);
                  if (optional.isPresent() && optional1.isPresent()) {
                     p_222539_.setBlockAndUpdate(blockpos, optional.get().defaultBlockState());
                     p_222539_.setBlockAndUpdate(p_222540_, optional1.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction));
                  }
               }
            }
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(p_222539_, p_222540_, p_222538_);
         }

      }
   }

   public ItemStack getCloneItemStack(LevelReader p_312829_, BlockPos p_57027_, BlockState p_57028_) {
      return new ItemStack(DataFixUtils.orElse(p_312829_.registryAccess().registryOrThrow(Registries.ITEM).getOptional(this.seed), this));
   }

   public boolean isValidBonemealTarget(LevelReader p_255699_, BlockPos p_57031_, BlockState p_57032_) {
      return p_57032_.getValue(AGE) != 7;
   }

   public boolean isBonemealSuccess(Level p_222533_, RandomSource p_222534_, BlockPos p_222535_, BlockState p_222536_) {
      return true;
   }

   public void performBonemeal(ServerLevel p_222528_, RandomSource p_222529_, BlockPos p_222530_, BlockState p_222531_) {
      int i = Math.min(7, p_222531_.getValue(AGE) + Mth.nextInt(p_222528_.random, 2, 5));
      BlockState blockstate = p_222531_.setValue(AGE, Integer.valueOf(i));
      p_222528_.setBlock(p_222530_, blockstate, 2);
      if (i == 7) {
         blockstate.randomTick(p_222528_, p_222530_, p_222528_.random);
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_57040_) {
      p_57040_.add(AGE);
   }

   //FORGE START
   @Override
   public net.minecraftforge.common.PlantType getPlantType(BlockGetter world, BlockPos pos) {
      return net.minecraftforge.common.PlantType.CROP;
   }
}
