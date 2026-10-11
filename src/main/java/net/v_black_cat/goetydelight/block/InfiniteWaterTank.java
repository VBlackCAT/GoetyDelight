package net.v_black_cat.goetydelight.block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * 无限量的水。
 *
 * <p>水量与容量都视为 {@link Integer#MAX_VALUE}，从中取水永不扣减存量——即无限量、
 * 且不限制输出（要多少给多少）。实现思路与 goety 本体的缠魂水槽（{@code haunted_jug}）一致。
 */
public class InfiniteWaterTank extends FluidTank {

    /** 用 Integer.MAX_VALUE 表示“无限” */
    public static final int INFINITE = Integer.MAX_VALUE;

    public InfiniteWaterTank() {
        super(INFINITE);
    }

    @Override
    public FluidStack getFluid() {
        return new FluidStack(Fluids.WATER, INFINITE);
    }

    @Override
    public int getFluidAmount() {
        return INFINITE;
    }

    @Override
    public int getCapacity() {
        return INFINITE;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        // 本就满水：来者不拒，但不改变任何状态
        return resource == null ? 0 : resource.getAmount();
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.isEmpty() || !resource.getFluid().isSame(Fluids.WATER)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        // 关键：不扣减存量，要多少给多少
        return maxDrain <= 0 ? FluidStack.EMPTY : new FluidStack(Fluids.WATER, maxDrain);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        // 恒定满水，无需存档
        return nbt;
    }

    @Override
    public FluidTank readFromNBT(CompoundTag nbt) {
        return this;
    }
}
