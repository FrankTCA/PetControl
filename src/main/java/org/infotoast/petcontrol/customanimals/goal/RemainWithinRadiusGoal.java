package org.infotoast.petcontrol.customanimals.goal;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;

public class RemainWithinRadiusGoal extends Goal {
    private final TamableAnimal tamable;
    private int centerX;
    private int centerZ;
    private int radius;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private int pathfindingToX;
    private int pathfindingToY;
    private int pathfindingToZ;
    private float oldWaterCost;

    public RemainWithinRadiusGoal(TamableAnimal tamable, double speedModifier) {
        this.tamable = tamable;
        this.navigation = tamable.getNavigation();
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(tamable.getNavigation() instanceof GroundPathNavigation) && !(tamable.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    private double distanceToSqrt(int x, int z) {
        double d = tamable.getX() - x;
        double d1 = tamable.getZ() - z;
        return Math.sqrt(d * d + d1 * d1);
    }

    @Override
    public boolean canUse() {
        return distanceToSqrt(centerX, centerZ) >= radius;
    }

    public void setCenterAndRadius(int centerX, int centerZ, int radius) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.navigation.isDone() && !this.navigation.isStuck();
    }

    @Override
    public void start() {
        System.out.println("Radius 5: " + this.radius);
        this.timeToRecalcPath = 0;
        int absoluteValue = this.radius / 2;
        int xR = tamable.random.nextInt(absoluteValue*2);
        int zR = tamable.random.nextInt(absoluteValue*2);
        this.pathfindingToX = this.centerX-absoluteValue+xR;
        this.pathfindingToZ = this.centerZ-absoluteValue+zR;
        this.pathfindingToY = tamable.getBukkitEntity().getWorld().getHighestBlockYAt(this.pathfindingToX, this.pathfindingToZ)+1;
        this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
        this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.navigation.moveTo(this.pathfindingToX, this.pathfindingToY, this.pathfindingToZ, this.speedModifier);
    }
}
