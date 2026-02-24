package net.v_black_cat.goetydelight.entities.ai.customer;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.entities.ai.ModMemory;
import net.v_black_cat.goetydelight.item.ModItems;

import java.util.ArrayList;
import java.util.List;

/**
 * 顾客点餐行为
 * 当顾客到达取餐区时，会随机选择几种食物进行点餐
 */
public class CustomerPlaceOrderBehavior extends CustomerBehavior<PathfinderMob> {

    public CustomerPlaceOrderBehavior() {
        super(ImmutableMap.of(
                ModMemory.IS_IN_RESTAURANT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemory.PICKUP_RANGE.get(), MemoryStatus.VALUE_PRESENT
        ), 60, 120); // 运行60-120 tick
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob owner) {
        Brain<?> brain = ((ICustomerEntity) owner).goetyDelight$getCustomerBrain();
        if (brain == null) return false;

        // 检查是否在餐厅内
        boolean isInRestaurant = brain.getMemory(ModMemory.IS_IN_RESTAURANT.get())
                .map(Boolean.class::cast)
                .orElse(false);
        
        if (!isInRestaurant) {
            return false;
        }

        // 检查是否在取餐区域内
        return isInPickupArea(brain, owner.position());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob entity, long gameTime) {
        Brain<?> brain = ((ICustomerEntity) entity).goetyDelight$getCustomerBrain();
        if (brain == null) return false;

        // 检查是否仍在餐厅内且在取餐区域
        boolean isInRestaurant = brain.getMemory(ModMemory.IS_IN_RESTAURANT.get())
                .map(Boolean.class::cast)
                .orElse(false);
        
        return isInRestaurant && isInPickupArea(brain, entity.position());
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
        // 生成订单
        List<ItemStack> order = generateOrder();
        
        // 设置订单到顾客实体
        ((ICustomerEntity) entity).goetyDelight$setOrder(order);
        
        // 可以在这里添加一些视觉效果或音效
        System.out.println("顾客 " + entity.getName().getString() + " 已点餐: " + getOrderSummary(order));
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob entity, long gameTime) {
        // 点餐行为运行期间保持在取餐区
        // 可以添加一些等待动画或其他行为
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob entity, long gameTime) {
        // 行为结束时的清理工作
        System.out.println("顾客 " + entity.getName().getString() + " 点餐完成");
    }

    /**
     * 生成订单 - 固定点4种食物用于测试
     */
    private List<ItemStack> generateOrder() {
        List<ItemStack> order = new ArrayList<>();
        
        // 固定点餐4种食物用于测试
        order.add(new ItemStack(ModItems.ECTOPLASMIC_MELON.get(), 2));  // 活体西瓜片 x2
        order.add(new ItemStack(ModItems.SEVEN_LEAF_PUDDING.get(), 1)); // 七叶布丁 x1
        order.add(new ItemStack(ModItems.OMINOUS_ICE_CREAM.get(), 1));  // 不祥冰淇淋 x1
        order.add(new ItemStack(ModItems.TOXIC_MEAL.get(), 1));         // 有毒大餐 x1
        
        return order;
    }

    /**
     * 检查实体是否在取餐区域内
     */
    private boolean isInPickupArea(Brain<?> brain, Vec3 entityPos) {
        return brain.getMemory(ModMemory.PICKUP_RANGE.get())
                .map((AABB pickupArea) -> pickupArea.contains(entityPos))
                .orElse(false);
    }

    /**
     * 获取订单摘要用于日志输出
     */
    private String getOrderSummary(List<ItemStack> order) {
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < order.size(); i++) {
            ItemStack item = order.get(i);
            summary.append(item.getHoverName().getString())
                   .append(" x")
                   .append(item.getCount());
            if (i < order.size() - 1) {
                summary.append(", ");
            }
        }
        return summary.toString();
    }
}