package org.infotoast.petcontrol.customanimals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.infotoast.petcontrol.PetControl;
import org.infotoast.petcontrol.customanimals.goal.RemainWithinRadiusGoal;
import org.infotoast.petcontrol.customanimals.goal.TameRandomTargetGoal;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class RoamingCat extends Cat {
    @Nullable
    private TemptGoal temptGoal;
    private RemainWithinRadiusGoal remainWithinRadiusGoal;
    private final int centerX;
    private final int centerZ;
    private final int radius;
    private final boolean guarded;
    public RoamingCat(Level level, int centerX, int centerZ, int radius, boolean guarded) {
        super(EntityType.CAT, level);
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.guarded = guarded;
        this.remainWithinRadiusGoal.setCenterAndRadius(centerX, centerZ, radius);
        level.addFreshEntity(this);
        PetControl.cacheManager.addRoamingCat(this);
    }

    public static RoamingCat convertFromCat(Cat cat, int centerX, int centerZ, int radius, boolean guarded) {
        RoamingCat rcat = new RoamingCat(cat.level(),  centerX, centerZ, radius, guarded);
        rcat.setPos(cat.getX(), cat.getY(), cat.getZ());
        ((org.bukkit.entity.Tameable)Bukkit.getEntity(rcat.getUUID())).setOwner(Bukkit.getOfflinePlayer(cat.getOwnerReference().getUUID()));
        rcat.setAge(cat.getAge());
        rcat.setAirSupply(cat.getAirSupply());
        rcat.setCustomName(cat.getCustomName());
        rcat.setCustomNameVisible(cat.isCustomNameVisible());
        rcat.setGlowingTag(cat.hasGlowingTag());
        rcat.setInvulnerable(cat.isInvulnerable());
        rcat.setInvisible(cat.isInvisible());
        rcat.setNoGravity(cat.isNoGravity());
        rcat.setSilent(cat.isSilent());
        rcat.setAbsorptionAmount(cat.getAbsorptionAmount());
        rcat.setHealth(cat.getHealth());
        rcat.setLeashData(cat.getLeashData());
        rcat.setNoAi(cat.isNoAi());
        rcat.setPersistenceRequired(true);
        if (cat.isSleeping()) {
            rcat.setSleepingPos(cat.getSleepingPos().get());
        }
        rcat.setCollarColor(cat.getCollarColor());
        rcat.setVariant(cat.getVariant());
        Entity bEnt = Bukkit.getEntity(rcat.getUUID());
        assert bEnt != null;
        PetControl.roamingTeam.addEntity(bEnt);
        bEnt.addScoreboardTag("roaming");
        PetControl.cacheManager.removeByUUID(cat.getUUID());
        cat.remove(RemovalReason.DISCARDED);
        BukkitScheduler scheduler = PetControl.plugin.getServer().getScheduler();
        return rcat;
    }

    public Cat convertToCat() {
        Cat cat = new Cat(EntityType.CAT, this.level());
        level().addFreshEntity(cat);
        cat.setPos(this.getX(), this.getY(), this.getZ());
        ((org.bukkit.entity.Tameable)Bukkit.getEntity(cat.getUUID())).setOwner(Bukkit.getOfflinePlayer(this.getOwnerReference().getUUID()));
        cat.setAge(this.getAge());
        cat.setAirSupply(this.getAirSupply());
        cat.setCustomName(this.getCustomName());
        cat.setCustomNameVisible(this.isCustomNameVisible());
        cat.setGlowingTag(this.hasGlowingTag());
        cat.setInvulnerable(this.isInvulnerable());
        cat.setInvisible(this.isInvisible());
        cat.setNoGravity(this.isNoGravity());
        cat.setSilent(this.isSilent());
        cat.setAbsorptionAmount(this.getAbsorptionAmount());
        cat.setHealth(this.getHealth());
        cat.setLeashData(this.getLeashData());
        cat.setNoAi(this.isNoAi());
        cat.setPersistenceRequired(true);
        if (this.isSleeping()) {
            cat.setSleepingPos(this.getSleepingPos().get());
        }
        cat.setCollarColor(this.getCollarColor());
        cat.setVariant(this.getVariant());
        PetControl.cacheManager.removeByUUID(this.getUUID());
        this.remove(RemovalReason.DISCARDED);

        return cat;
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new CatTemptGoal(this, 0.6, (stack) -> stack.is(ItemTags.CAT_FOOD), true);
        this.remainWithinRadiusGoal = new RemainWithinRadiusGoal(this, 1.0D);
        super.goalSelector.addGoal(1, new FloatGoal(this));
        super.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5D));
        super.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        super.goalSelector.addGoal(3, new CatRelaxOnOwnerGoal(this));
        super.goalSelector.addGoal(3, this.remainWithinRadiusGoal);
        super.goalSelector.addGoal(4, this.temptGoal);
        super.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1, 8));
        super.goalSelector.addGoal(6, new FollowOwnerGoal(this, (double)1.0F, 10.0F, 5.0F));
        super.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8));
        super.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
        super.goalSelector.addGoal(9, new OcelotAttackGoal(this));
        super.goalSelector.addGoal(10, new BreedGoal(this, 0.8));
        super.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
        super.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0F));
        super.targetSelector.addGoal(1, new TameRandomTargetGoal(this, Rabbit.class, false, (TargetingConditions.Selector)null));
        super.targetSelector.addGoal(1, new TameRandomTargetGoal(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
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
    public void setOrderedToSit(boolean orderedToSit) {
        PetControl.cacheManager.checkIfRoamingAnimalFromUUID(this.uuid).setSitting(orderedToSit);
        super.setOrderedToSit(orderedToSit);
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
        return this.centerX;
    }

    public int getRoamingCenterZ() {
        return this.centerZ;
    }

    public int getRoamingRadius() {
        return this.radius;
    }

    public boolean isGuarded() {
        return this.guarded;
    }

    static class CatRelaxOnOwnerGoal extends Goal {
        private final Cat cat;
        @Nullable
        private Player ownerPlayer;
        @Nullable
        private BlockPos goalPos;
        private int onBedTicks;

        public CatRelaxOnOwnerGoal(Cat cat) {
            this.cat = cat;
        }

        public boolean canUse() {
            if (!this.cat.isTame()) {
                return false;
            } else if (this.cat.isOrderedToSit()) {
                return false;
            } else {
                LivingEntity owner = this.cat.getOwner();
                if (owner instanceof Player) {
                    Player player = (Player)owner;
                    this.ownerPlayer = player;
                    if (!owner.isSleeping()) {
                        return false;
                    }

                    if (this.cat.distanceToSqr(this.ownerPlayer) > (double)100.0F) {
                        return false;
                    }

                    BlockPos blockPos = this.ownerPlayer.blockPosition();
                    BlockState blockState = this.cat.level().getBlockState(blockPos);
                    if (blockState.is(BlockTags.BEDS)) {
                        this.goalPos = (BlockPos)blockState.getOptionalValue(HorizontalDirectionalBlock.FACING).map((pos) -> blockPos.relative(pos.getOpposite())).orElseGet(() -> new BlockPos(blockPos));
                        return !this.spaceIsOccupied();
                    }
                }

                return false;
            }
        }

        private boolean spaceIsOccupied() {
            for(Cat cat : this.cat.level().getEntitiesOfClass(Cat.class, (new AABB(this.goalPos)).inflate((double)2.0F))) {
                if (cat != this.cat && (cat.isLying() || cat.isRelaxStateOne())) {
                    return true;
                }
            }

            return false;
        }

        public boolean canContinueToUse() {
            return this.cat.isTame() && !this.cat.isOrderedToSit() && this.ownerPlayer != null && this.ownerPlayer.isSleeping() && this.goalPos != null && !this.spaceIsOccupied();
        }

        public void start() {
            if (this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), (double)1.1F);
            }

        }

        public void stop() {
            this.cat.setLying(false);
            if (this.ownerPlayer.getSleepTimer() >= 100 && this.cat.level().getRandom().nextFloat() < (Float)this.cat.level().environmentAttributes().getValue(EnvironmentAttributes.CAT_WAKING_UP_GIFT_CHANCE, this.cat.position())) {
                this.giveMorningGift();
            }

            this.onBedTicks = 0;
            this.cat.setRelaxStateOne(false);
            this.cat.getNavigation().stop();
        }

        private void giveMorningGift() {
            RandomSource random = this.cat.getRandom();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            mutableBlockPos.set(this.cat.isLeashed() ? this.cat.getLeashHolder().blockPosition() : this.cat.blockPosition());
            this.cat.randomTeleport((double)(mutableBlockPos.getX() + random.nextInt(11) - 5), (double)(mutableBlockPos.getY() + random.nextInt(5) - 2), (double)(mutableBlockPos.getZ() + random.nextInt(11) - 5), false);
            mutableBlockPos.set(this.cat.blockPosition());
            this.cat.dropFromGiftLootTable(getServerLevel(this.cat), BuiltInLootTables.CAT_MORNING_GIFT, (level, stack) -> {
                ItemEntity item = new ItemEntity(level, (double)mutableBlockPos.getX() - (double) Mth.sin(this.cat.yBodyRot * ((float)Math.PI / 180F)), (double)mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + (double)Mth.cos(this.cat.yBodyRot * ((float)Math.PI / 180F)), stack);
                EntityDropItemEvent event = new EntityDropItemEvent(this.cat.getBukkitEntity(), (org.bukkit.entity.Item)item.getBukkitEntity());
                if (event.callEvent()) {
                    level.addFreshEntity(item);
                }
            });
        }

        public void tick() {
            if (this.ownerPlayer != null && this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), (double)1.1F);
                if (this.cat.distanceToSqr(this.ownerPlayer) < (double)2.5F) {
                    ++this.onBedTicks;
                    if (this.onBedTicks > this.adjustedTickDelay(16)) {
                        this.cat.setLying(true);
                        this.cat.setRelaxStateOne(false);
                    } else {
                        this.cat.lookAt(this.ownerPlayer, 45.0F, 45.0F);
                        this.cat.setRelaxStateOne(true);
                    }
                } else {
                    this.cat.setLying(false);
                }
            }

        }
    }

    static class CatTemptGoal extends TemptGoal {
        @Nullable
        private LivingEntity selectedPlayer;
        private final Cat cat;

        public CatTemptGoal(Cat cat, double speedModifier, Predicate<ItemStack> items, boolean canScare) {
            super(cat, speedModifier, items, canScare);
            this.cat = cat;
        }

        public void tick() {
            super.tick();
            if (this.selectedPlayer == null && super.mob.getRandom().nextInt(this.adjustedTickDelay(600)) == 0) {
                this.selectedPlayer = super.player;
            } else if (super.mob.getRandom().nextInt(this.adjustedTickDelay(500)) == 0) {
                this.selectedPlayer = null;
            }

        }

        protected boolean canScare() {
            return (this.selectedPlayer == null || !this.selectedPlayer.equals(super.player)) && super.canScare();
        }

        public boolean canUse() {
            return super.canUse() && !this.cat.isTame();
        }
    }
}
