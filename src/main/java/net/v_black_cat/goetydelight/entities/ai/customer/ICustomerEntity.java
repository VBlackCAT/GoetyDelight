package net.v_black_cat.goetydelight.entities.ai.customer;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import net.v_black_cat.goetydelight.capability.CustomerOrderItemProvider;
import net.v_black_cat.goetydelight.capability.ICustomerOrderItemList;
import net.v_black_cat.goetydelight.entities.ModEntityDataSerializers;
import net.v_black_cat.goetydelight.network.CustomerItemListUpdatePacket;
import net.v_black_cat.goetydelight.network.NetworkHandler;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

import static net.v_black_cat.goetydelight.GoetyDelight.LOGGER;

public interface ICustomerEntity {

    String TAG_CUSTOMER_INVENTORY = "GoetyDelightCustomerInventory";

    SimpleContainer goetyDelight$getCustomerInventory();


    void goetyDelight$setCustomerMode(boolean enabled);
    boolean goetyDelight$isCustomerMode();
    Brain<PathfinderMob> goetyDelight$getCustomerBrain();
    void goetyDelight$setCustomerBrain(Brain<PathfinderMob> brain);
    default void goetyDelight$setOrder(List<ItemStack> order) {
        if (this instanceof PathfinderMob mob) {
            mob.getCapability(CustomerOrderItemProvider.CAPABILITY).ifPresent(cap -> {
                cap.setItems(order);
                if (!mob.level().isClientSide) {
                    CompoundTag tag = new CompoundTag();
                    ListTag listTag = new ListTag();
                    for (ItemStack stack : order) {
                        listTag.add(stack.save(new CompoundTag()));
                    }
                    tag.put("CustomerOrderItems", listTag);
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY.with(() -> mob),
                            new CustomerItemListUpdatePacket(mob.getId(), tag)
                    );
                }
            });
        }
    }

    default List<ItemStack> goetyDelight$getOrder() {
        if (this instanceof PathfinderMob mob) {
            return mob.getCapability(CustomerOrderItemProvider.CAPABILITY)
                    .map(ICustomerOrderItemList::getItems)
                    .orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }
    static void CustomerPickUpItem(Mob mob, ICustomerEntity carrier, ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        if (mob.wantsToPickUp(itemstack)) {
            SimpleContainer simplecontainer = carrier.goetyDelight$getCustomerInventory();
            boolean flag = simplecontainer.canAddItem(itemstack);
            if (!flag) {
                return;
            }

            mob.onItemPickup(itemEntity);
            int i = itemstack.getCount();
            ItemStack itemstack1 = simplecontainer.addItem(itemstack);
            mob.take(itemEntity, i - itemstack1.getCount());
            if (itemstack1.isEmpty()) {
                itemEntity.discard();
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }

    }

    default void readCustomerInventoryFromTag(CompoundTag tag) {
        if (tag.contains(TAG_CUSTOMER_INVENTORY, 9)) {
            this.goetyDelight$getCustomerInventory().fromTag(tag.getList(TAG_CUSTOMER_INVENTORY, 10));
        }

    }

    default void writeCustomerInventoryToTag(CompoundTag tag) {
        tag.put(TAG_CUSTOMER_INVENTORY, this.goetyDelight$getCustomerInventory().createTag());
    }

    default void goetyDelight$readCustomerData(CompoundTag nbt, PathfinderMob mob) {
        Dynamic<Tag> dyn = new Dynamic(NbtOps.INSTANCE, nbt.get("CustomerBrain"));
        this.goetyDelight$setCustomerBrain(CustomerAi.makeBrain(mob, dyn));
        this.goetyDelight$setCustomerMode(nbt.getBoolean("GoetyDelightCustomerMode"));
        this.readCustomerInventoryFromTag(nbt);
    }
    
    default void goetyDelight$addCustomerData(CompoundTag nbt) {
        DataResult<Tag> dataresult = this.goetyDelight$getCustomerBrain().serializeStart(NbtOps.INSTANCE);
        Logger var10001 = LOGGER;
        java.util.Objects.requireNonNull(var10001);
        dataresult.resultOrPartial(var10001::error).ifPresent((p_21102_) -> {
            nbt.put("CustomerBrain", p_21102_);
        });
        nbt.putBoolean("GoetyDelightCustomerMode", this.goetyDelight$isCustomerMode());
        this.writeCustomerInventoryToTag(nbt);

    }
}
