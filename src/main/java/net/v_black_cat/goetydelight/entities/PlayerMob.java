package net.v_black_cat.goetydelight.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.mixin.MobAccessor;

public class PlayerMob extends Player {
    private final PathfinderMob mobController;

    public PlayerMob(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
        this.mobController = new Zombie(level);
        
        this.mobController.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void tick() {
        super.tick();

        
        if (!this.level().isClientSide) {
            
            this.mobController.setPos(this.getX(), this.getY(), this.getZ());
            this.mobController.setYRot(this.getYRot());
            this.mobController.setXRot(this.getXRot());

            
            this.mobController.aiStep();

            
            applyMobMovement();
        }
    }

    private void applyMobMovement() {
        this.setZza(this.mobController.zza);
        this.setYya(this.mobController.yya);
        this.setXxa(this.mobController.xxa);
    }

    private void setZza(float zza) {
        this.zza = zza;
    }
    private void setYya(float yya) {
        this.yya = yya;
    }
    private void setXxa(float xxa) {
        this.xxa = xxa;
    }


    @Override
    public void aiStep() {
        
        if (!this.level().isClientSide) {
            if (this.mobController instanceof MobAccessor mobAccessor){
                mobAccessor.callCustomServerAiStep();
            }
        }
        super.aiStep();
    }

    
    @Override
    public void attack(Entity target) {
        if (this.mobController instanceof Zombie zombie) {
            
            if (zombie.canAttack((LivingEntity) target)) {
                super.attack(target); 
            }
        } else {
            super.attack(target);
        }
    }

    
    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    public Mob getMobController() {
        return this.mobController;
    }
}