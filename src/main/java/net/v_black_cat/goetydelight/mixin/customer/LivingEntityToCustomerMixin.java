package net.v_black_cat.goetydelight.mixin.customer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.v_black_cat.goetydelight.entities.ICustomerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityToCustomerMixin {


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
