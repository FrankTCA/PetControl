package org.infotoast.petcontrol.customanimals.goal;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

public class TameRandomTargetGoal extends NonTameRandomTargetGoal {
    public TameRandomTargetGoal(TamableAnimal tamableMob, Class targetType, boolean mustSee, @Nullable TargetingConditions.Selector selector) {
        super(tamableMob, targetType, mustSee, selector);
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && super.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }
    }


}
