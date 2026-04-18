package net.v_black_cat.goetydelight.entities;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.v_black_cat.goetydelight.capability.CustomerOrderItemProvider;
import net.v_black_cat.goetydelight.capability.ICustomerOrderItemList;
import net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi;
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
    float goetyDelight$getCustomerSatietyValue();
    void goetyDelight$setCustomerSatietyValue(float value);
    long goetyDelight$getEnterCustomerModeCooldown();
    void goetyDelight$setEnterCustomerModeCooldown(long cooldown);
    default void goetyDelight$reduceEnterCustomerModeCooldown(long reduction) {
        long currentCooldown = goetyDelight$getEnterCustomerModeCooldown();
        if (currentCooldown > 0) {
            if (currentCooldown > reduction) {
                goetyDelight$setEnterCustomerModeCooldown(currentCooldown - reduction);
            } else {
                goetyDelight$setEnterCustomerModeCooldown(0);
            }
        }
    }



    default boolean goetyDelight$canEnterCustomerMode() {
        return goetyDelight$getEnterCustomerModeCooldown()<=1;
    }

    default void goetyDelight$enterCustomerModeAndCheckCoolDown() {
        if (goetyDelight$canEnterCustomerMode()) {
            goetyDelight$setCustomerMode(true);
        }
    }



    default boolean goetyDelight$isHungry() {
        float v = 0.3f;
        return goetyDelight$getCustomerSatietyValue() < (goetyDelight$getCustomerMaxSatietyValue() * v);
    }
    default boolean goetyDelight$isFull() {
        float v = 0.95f;
        return goetyDelight$getCustomerSatietyValue() >= goetyDelight$getCustomerMaxSatietyValue()* v;
    }


    default void goetyDelight$addCustomerSatietyValue(float value){
        if(value>0){
            float v = goetyDelight$getCustomerSatietyValue() + value;
            float v1 = goetyDelight$getCustomerMaxSatietyValue();
            if (v > v1){
                goetyDelight$setCustomerSatietyValue(v1);
            }else {
                goetyDelight$setCustomerSatietyValue(v);
            }
        }
    };
    default void goetyDelight$SubtractionCustomerSatietyValue(float value){
        if(value>0){
            float v = goetyDelight$getCustomerSatietyValue() - value;
            if (v > 0){
                goetyDelight$setCustomerSatietyValue(v);
            }else {
                goetyDelight$setCustomerSatietyValue(0);
            }
        }
    };
    default float goetyDelight$getCustomerMaxSatietyValue() {
        if (this instanceof PathfinderMob mob) {
            float maxHealth = (float) mob.getAttributeBaseValue(Attributes.MAX_HEALTH);
            float width = mob.getBbWidth();
            float height = mob.getBbHeight();
            float volume = width * width * height;
            float humanoidVolume = 0.6f * 0.6f * 1.8f;
            float healthSatiety = maxHealth / 10.0f;
            float volumeSatiety = volume * 5.0f;
            float maxSatiety = healthSatiety + volumeSatiety;
            if (volume > (humanoidVolume + 0.01f) || maxHealth > 20.0f) {
                maxSatiety = Math.max(maxSatiety, 20.0f);
            }

            return maxSatiety;
        }

        return 20f;
    }

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
    if (nbt.contains("CustomerBrain", 10)) {
        Dynamic<Tag> dyn = new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("CustomerBrain"));
        this.goetyDelight$setCustomerBrain(CustomerAi.makeBrain(mob, dyn));
    } else {
        this.goetyDelight$setCustomerBrain(CustomerAi.makeBrain(mob));
    }

    this.goetyDelight$setCustomerMode(nbt.getBoolean("GoetyDelightCustomerMode"));
    this.goetyDelight$setCustomerSatietyValue(nbt.getFloat("GoetyDelightCustomerSatietyValue"));
    this.goetyDelight$setEnterCustomerModeCooldown(nbt.getLong("GoetyDelightCustomerEnterCustomerModeCooldown"));
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
        nbt.putFloat("GoetyDelightCustomerSatietyValue", this.goetyDelight$getCustomerSatietyValue());
        nbt.putLong("GoetyDelightCustomerEnterCustomerModeCooldown", this.goetyDelight$getEnterCustomerModeCooldown());
        this.writeCustomerInventoryToTag(nbt);
    }
}
