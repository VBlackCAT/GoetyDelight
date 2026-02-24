package net.v_black_cat.goetydelight.mixin.customer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.entities.ModEntityDataSerializers;
import net.v_black_cat.goetydelight.entities.ai.customer.ICustomerEntity;
import net.v_black_cat.goetydelight.mixin.EntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.v_black_cat.goetydelight.GoetyDelight.MODID;

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
