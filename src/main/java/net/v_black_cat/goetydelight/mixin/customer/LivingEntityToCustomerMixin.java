package net.v_black_cat.goetydelight.mixin.customer;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;
import net.v_black_cat.goetydelight.mixin.EntityAccessor;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.v_black_cat.goetydelight.GoetyDelight.LOGGER;

@Mixin(LivingEntity.class)
public class LivingEntityToCustomerMixin {



    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void goetyDelight$defineSynchedCustomerData(CallbackInfo ci) {
        if((LivingEntity) (Object) this instanceof PathfinderMob mob){
            if (mob instanceof ICustomerEntity customer){
                customer.goetyDelight$defineSynchedCustomerData(((EntityAccessor)customer).getEntityData());
            }
        }

    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void goetyDelight$readCustomerData(CompoundTag nbt, CallbackInfo ci) {
        if((LivingEntity) (Object) this instanceof PathfinderMob mob){
            if (mob instanceof ICustomerEntity customer){
                customer.goetyDelight$readCustomerData(nbt, mob);
            }
        }

    }



    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void goetyDelight$addCustomerData(CompoundTag nbt, CallbackInfo ci) {
        if((LivingEntity) (Object) this instanceof PathfinderMob mob){
            if (this instanceof ICustomerEntity customer) {
                customer.goetyDelight$addCustomerData(nbt);
            }
        }

    }


}
