package net.minecraft.world.level.block.entity.trialspawner;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public enum TrialSpawnerState implements StringRepresentable {
   INACTIVE("inactive", 0, TrialSpawnerState.ParticleEmission.NONE, -1.0D, false),
   WAITING_FOR_PLAYERS("waiting_for_players", 4, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, 200.0D, true),
   ACTIVE("active", 8, TrialSpawnerState.ParticleEmission.FLAMES_AND_SMOKE, 1000.0D, true),
   WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0D, false),
   EJECTING_REWARD("ejecting_reward", 8, TrialSpawnerState.ParticleEmission.SMALL_FLAMES, -1.0D, false),
   COOLDOWN("cooldown", 0, TrialSpawnerState.ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0D, false);

   private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
   private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
   private final String name;
   private final int lightLevel;
   private final double spinningMobSpeed;
   private final TrialSpawnerState.ParticleEmission particleEmission;
   private final boolean isCapableOfSpawning;

   private TrialSpawnerState(String p_309652_, int p_311553_, TrialSpawnerState.ParticleEmission p_309474_, double p_312481_, boolean p_310488_) {
      this.name = p_309652_;
      this.lightLevel = p_311553_;
      this.particleEmission = p_309474_;
      this.spinningMobSpeed = p_312481_;
      this.isCapableOfSpawning = p_310488_;
   }

   TrialSpawnerState tickAndGetNext(BlockPos p_313024_, TrialSpawner p_310869_, ServerLevel p_313233_) {
      TrialSpawnerData trialspawnerdata = p_310869_.getData();
      TrialSpawnerConfig trialspawnerconfig = p_310869_.getConfig();
      PlayerDetector playerdetector = p_310869_.getPlayerDetector();
      TrialSpawnerState trialspawnerstate;
      switch (this) {
         case INACTIVE:
            trialspawnerstate = trialspawnerdata.getOrCreateDisplayEntity(p_310869_, p_313233_, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
            break;
         case WAITING_FOR_PLAYERS:
            if (!trialspawnerdata.hasMobToSpawn()) {
               trialspawnerstate = INACTIVE;
            } else {
               trialspawnerdata.tryDetectPlayers(p_313233_, p_313024_, playerdetector, trialspawnerconfig.requiredPlayerRange());
               trialspawnerstate = trialspawnerdata.detectedPlayers.isEmpty() ? this : ACTIVE;
            }
            break;
         case ACTIVE:
            if (!trialspawnerdata.hasMobToSpawn()) {
               trialspawnerstate = INACTIVE;
            } else {
               int i = trialspawnerdata.countAdditionalPlayers(p_313024_);
               trialspawnerdata.tryDetectPlayers(p_313233_, p_313024_, playerdetector, trialspawnerconfig.requiredPlayerRange());
               if (trialspawnerdata.hasFinishedSpawningAllMobs(trialspawnerconfig, i)) {
                  if (trialspawnerdata.haveAllCurrentMobsDied()) {
                     trialspawnerdata.cooldownEndsAt = p_313233_.getGameTime() + (long)trialspawnerconfig.targetCooldownLength();
                     trialspawnerdata.totalMobsSpawned = 0;
                     trialspawnerdata.nextMobSpawnsAt = 0L;
                     trialspawnerstate = WAITING_FOR_REWARD_EJECTION;
                     break;
                  }
               } else if (trialspawnerdata.isReadyToSpawnNextMob(p_313233_, trialspawnerconfig, i)) {
                  p_310869_.spawnMob(p_313233_, p_313024_).ifPresent((p_310382_) -> {
                     trialspawnerdata.currentMobs.add(p_310382_);
                     ++trialspawnerdata.totalMobsSpawned;
                     trialspawnerdata.nextMobSpawnsAt = p_313233_.getGameTime() + (long)trialspawnerconfig.ticksBetweenSpawn();
                     trialspawnerdata.spawnPotentials.getRandom(p_313233_.getRandom()).ifPresent((p_310481_) -> {
                        trialspawnerdata.nextSpawnData = Optional.of(p_310481_.getData());
                        p_310869_.markUpdated();
                     });
                  });
               }

               trialspawnerstate = this;
            }
            break;
         case WAITING_FOR_REWARD_EJECTION:
            if (trialspawnerdata.isReadyToOpenShutter(p_313233_, trialspawnerconfig, 40.0F)) {
               p_313233_.playSound((Player)null, p_313024_, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
               trialspawnerstate = EJECTING_REWARD;
            } else {
               trialspawnerstate = this;
            }
            break;
         case EJECTING_REWARD:
            if (!trialspawnerdata.isReadyToEjectItems(p_313233_, trialspawnerconfig, (float)TIME_BETWEEN_EACH_EJECTION)) {
               trialspawnerstate = this;
            } else if (trialspawnerdata.detectedPlayers.isEmpty()) {
               p_313233_.playSound((Player)null, p_313024_, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
               trialspawnerdata.ejectingLootTable = Optional.empty();
               trialspawnerstate = COOLDOWN;
            } else {
               if (trialspawnerdata.ejectingLootTable.isEmpty()) {
                  trialspawnerdata.ejectingLootTable = trialspawnerconfig.lootTablesToEject().getRandomValue(p_313233_.getRandom());
               }

               trialspawnerdata.ejectingLootTable.ifPresent((p_310597_) -> {
                  p_310869_.ejectReward(p_313233_, p_313024_, p_310597_);
               });
               trialspawnerdata.detectedPlayers.remove(trialspawnerdata.detectedPlayers.iterator().next());
               trialspawnerstate = this;
            }
            break;
         case COOLDOWN:
            if (trialspawnerdata.isCooldownFinished(p_313233_)) {
               trialspawnerdata.cooldownEndsAt = 0L;
               trialspawnerstate = WAITING_FOR_PLAYERS;
            } else {
               trialspawnerstate = this;
            }
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return trialspawnerstate;
   }

   public int lightLevel() {
      return this.lightLevel;
   }

   public double spinningMobSpeed() {
      return this.spinningMobSpeed;
   }

   public boolean hasSpinningMob() {
      return this.spinningMobSpeed >= 0.0D;
   }

   public boolean isCapableOfSpawning() {
      return this.isCapableOfSpawning;
   }

   public void emitParticles(Level p_310333_, BlockPos p_312414_) {
      this.particleEmission.emit(p_310333_, p_310333_.getRandom(), p_312414_);
   }

   public String getSerializedName() {
      return this.name;
   }

   static class LightLevel {
      private static final int UNLIT = 0;
      private static final int HALF_LIT = 4;
      private static final int LIT = 8;

      private LightLevel() {
      }
   }

   interface ParticleEmission {
      TrialSpawnerState.ParticleEmission NONE = (p_311158_, p_313095_, p_309870_) -> {
      };
      TrialSpawnerState.ParticleEmission SMALL_FLAMES = (p_309392_, p_312550_, p_312505_) -> {
         if (p_312550_.nextInt(2) == 0) {
            Vec3 vec3 = p_312505_.getCenter().offsetRandom(p_312550_, 0.9F);
            addParticle(ParticleTypes.SMALL_FLAME, vec3, p_309392_);
         }

      };
      TrialSpawnerState.ParticleEmission FLAMES_AND_SMOKE = (p_310259_, p_311563_, p_310144_) -> {
         Vec3 vec3 = p_310144_.getCenter().offsetRandom(p_311563_, 1.0F);
         addParticle(ParticleTypes.SMOKE, vec3, p_310259_);
         addParticle(ParticleTypes.FLAME, vec3, p_310259_);
      };
      TrialSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (p_312500_, p_312202_, p_311828_) -> {
         Vec3 vec3 = p_311828_.getCenter().offsetRandom(p_312202_, 0.9F);
         if (p_312202_.nextInt(3) == 0) {
            addParticle(ParticleTypes.SMOKE, vec3, p_312500_);
         }

         if (p_312500_.getGameTime() % 20L == 0L) {
            Vec3 vec31 = p_311828_.getCenter().add(0.0D, 0.5D, 0.0D);
            int i = p_312500_.getRandom().nextInt(4) + 20;

            for(int j = 0; j < i; ++j) {
               addParticle(ParticleTypes.SMOKE, vec31, p_312500_);
            }
         }

      };

      private static void addParticle(SimpleParticleType p_311275_, Vec3 p_310309_, Level p_310163_) {
         p_310163_.addParticle(p_311275_, p_310309_.x(), p_310309_.y(), p_310309_.z(), 0.0D, 0.0D, 0.0D);
      }

      void emit(Level p_310445_, RandomSource p_311021_, BlockPos p_310003_);
   }

   static class SpinningMob {
      private static final double NONE = -1.0D;
      private static final double SLOW = 200.0D;
      private static final double FAST = 1000.0D;

      private SpinningMob() {
      }
   }
}