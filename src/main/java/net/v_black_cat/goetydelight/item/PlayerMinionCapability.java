package net.v_black_cat.goetydelight.item;

public class PlayerMinionCapability {
    public int getPermanentBuffStacks() {
        return permanentBuffStacks;
    }

    public void setPermanentBuffStacks(int permanentBuffStacks) {
        this.permanentBuffStacks = permanentBuffStacks;
    }

    private int permanentBuffStacks; // 永久增益层数（最多5次）

    public boolean isHasDoubledCap() {
        return hasDoubledCap;
    }

    public void setHasDoubledCap(boolean hasDoubledCap) {
        this.hasDoubledCap = hasDoubledCap;
    }

    private boolean hasDoubledCap;   // 是否已触发召唤上限翻倍
}
