package org.infotoast.petcontrol.customanimals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitScheduler;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.PetListener;
import org.infotoast.petcontrol.customanimals.goal.RemainWithinRadiusGoal;

public class RoamingDog extends Wolf {
    private final int centerX;
    private final int centerZ;
    private final int radius;
    private final boolean guarded;
    private RemainWithinRadiusGoal remainWithinRadiusGoal;
    public RoamingDog(Level level, int centerX, int centerZ, int radius, boolean guarded) {
        super(EntityType.WOLF, level);
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.guarded = guarded;
        this.remainWithinRadiusGoal.setCenterAndRadius(centerX, centerZ, radius);
        level.addFreshEntity(this);
        PetControl.cacheManager.addRoamingDog(this);
    }

    public static RoamingDog convertFromWolf(Wolf wolf, int centerX, int centerZ, int radius, boolean guarded) {
        RoamingDog rdog = new RoamingDog(wolf.level(), centerX, centerZ, radius, guarded);
        rdog.setPos(wolf.getX(), wolf.getY(), wolf.getZ());
        ((org.bukkit.entity.Tameable)Bukkit.getEntity(rdog.getUUID())).setOwner(Bukkit.getOfflinePlayer(wolf.getOwnerReference().getUUID()));
        rdog.setAge(wolf.getAge());
        rdog.setAirSupply(wolf.getAirSupply());
        rdog.setCustomName(wolf.getCustomName());
        rdog.setCustomNameVisible(wolf.isCustomNameVisible());
        rdog.setGlowingTag(wolf.hasGlowingTag());
        rdog.setInvulnerable(wolf.isInvulnerable());
        rdog.setInvisible(wolf.isInvisible());
        rdog.setNoGravity(wolf.isNoGravity());
        rdog.setSilent(wolf.isSilent());
        rdog.setAbsorptionAmount(wolf.getAbsorptionAmount());
        rdog.setHealth(wolf.getHealth());
        rdog.setLeashData(wolf.getLeashData());
        rdog.setNoAi(wolf.isNoAi());
        rdog.setPersistenceRequired(true);
        if (wolf.isSleeping()) {
            rdog.setSleepingPos(wolf.getSleepingPos().get());
        }
        rdog.setCollarColor(wolf.getCollarColor());
        rdog.setVariant(wolf.getVariant());
        Entity bEnt = Bukkit.getEntity(rdog.getUUID());
        assert bEnt != null;
        PetControl.roamingTeam.addEntity(bEnt);
        bEnt.addScoreboardTag("roaming");
        PetControl.cacheManager.removeByUUID(wolf.getUUID());
        wolf.remove(RemovalReason.DISCARDED);
        return rdog;
    }

    public Wolf convertToWolf() {
        Wolf wolf = new Wolf(EntityType.WOLF, this.level());
        level().addFreshEntity(wolf);
        wolf.setPos(this.getX(), this.getY(), this.getZ());
        ((org.bukkit.entity.Tameable)Bukkit.getEntity(wolf.getUUID())).setOwner(Bukkit.getOfflinePlayer(this.getOwnerReference().getUUID()));
        wolf.setAge(this.getAge());
        wolf.setAirSupply(this.getAirSupply());
        wolf.setCustomName(this.getCustomName());
        wolf.setCustomNameVisible(this.isCustomNameVisible());
        wolf.setGlowingTag(this.hasGlowingTag());
        wolf.setInvulnerable(this.isInvulnerable());
        wolf.setInvisible(this.isInvisible());
        wolf.setNoGravity(this.isNoGravity());
        wolf.setSilent(this.isSilent());
        wolf.setAbsorptionAmount(this.getAbsorptionAmount());
        wolf.setHealth(this.getHealth());
        wolf.setLeashData(this.getLeashData());
        wolf.setNoAi(this.isNoAi());
        wolf.setPersistenceRequired(true);
        if (wolf.isSleeping()) {
            wolf.setSleepingPos(this.getSleepingPos().get());
        }
        wolf.setCollarColor(this.getCollarColor());
        wolf.setVariant(this.getVariant());
        PetControl.cacheManager.removeByUUID(this.getUUID());
        this.remove(RemovalReason.DISCARDED);
        return wolf;
    }

    @Override
    protected void registerGoals() {
        this.remainWithinRadiusGoal = new RemainWithinRadiusGoal(this, 1.0D);
        super.goalSelector.addGoal(1, new FloatGoal(this));
        super.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal((double)1.5F, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        super.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        super.goalSelector.addGoal(3, new RoamingDog.WolfAvoidEntityGoal(this, Llama.class, 24.0F, (double)1.5F, (double)1.5F));
        super.goalSelector.addGoal(3, this.remainWithinRadiusGoal);
        super.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        super.goalSelector.addGoal(5, new MeleeAttackGoal(this, (double)1.0F, true));
        super.goalSelector.addGoal(6, new FollowOwnerGoal(this, (double)1.0F, 10.0F, 2.0F));
        super.goalSelector.addGoal(7, new BreedGoal(this, (double)1.0F));
        super.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, (double)1.0F));
        super.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
        super.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        super.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        super.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        super.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        super.targetSelector.addGoal(3, (new HurtByTargetGoal(this, new Class[0])).setAlertOthers(new Class[0]));
        super.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, this::isAngryAt));
        super.targetSelector.addGoal(5, new NonTameRandomTargetGoal(this, Animal.class, false, PREY_SELECTOR));
        super.targetSelector.addGoal(6, new NonTameRandomTargetGoal(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        super.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, AbstractSkeleton.class, false));
        super.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal(this, true));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean shouldTryTeleportToOwner() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource damageSource) {
        if (this.guarded) {
            if (damageSource.is(DamageTypeTags.IS_FALL)) {
                return true;
            }

            if (damageSource.is(DamageTypeTags.IS_FIRE)) {
                return true;
            }

            if (damageSource.is(DamageTypeTags.IS_DROWNING)) {
                return true;
            }

            if (damageSource.is(DamageTypeTags.IS_FREEZING)) {
                return true;
            }
        }

        return super.isInvulnerableTo(level, damageSource);
    }

    public int getRoamingCenterX() {
        return centerX;
    }

    public int getRoamingCenterZ() {
        return centerZ;
    }

    public int getRoamingRadius() {
        return radius;
    }

    public boolean isGuarded() {
        return this.guarded;
    }

    class WolfAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Wolf wolf;

        public WolfAvoidEntityGoal(final Wolf wolf, final Class<T> avoidClass, final float maxDistance, final double walkSpeedModifier, final double sprintSpeedModifier) {
            super(wolf, avoidClass, maxDistance, walkSpeedModifier, sprintSpeedModifier);
            this.wolf = wolf;
        }

        public boolean canUse() {
            return super.canUse() && super.toAvoid instanceof Llama && !this.wolf.isTame() && this.avoidLlama((Llama)super.toAvoid);
        }

        private boolean avoidLlama(Llama llama) {
            return llama.getStrength() >= RoamingDog.super.random.nextInt(5);
        }

        public void start() {
            RoamingDog.this.setTarget((LivingEntity)null);
            super.start();
        }

        public void tick() {
            RoamingDog.this.setTarget((LivingEntity)null);
            super.tick();
        }
    }
}
