package net.v_black_cat.goetydelight.buff;

import javax.annotation.Nullable;


public interface IBuffHolder {
    @Nullable
    ActiveBuffs goetydelight$getActiveBuffs();
    void goetydelight$setActiveBuffs(ActiveBuffs buffs);
}
