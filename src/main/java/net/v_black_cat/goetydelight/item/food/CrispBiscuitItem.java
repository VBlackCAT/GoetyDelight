package net.v_black_cat.goetydelight.item.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.Polarice3.Goety.common.effects.GoetyEffects;

public class CrispBiscuitItem extends Item {


    public CrispBiscuitItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof Player player) {
            
            handleCorpseEaterEffect(player);

            
            handleSaveEffects(player);

            MinecraftServer server = world.getServer();
            if (server != null) {
                Component message = Component.literal("食人术~").withStyle(ChatFormatting.RED);
                for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
                    onlinePlayer.sendSystemMessage(message);
                }
            }
        }

        return super.finishUsingItem(stack, world, entity);
    }

    private void handleCorpseEaterEffect(Player player) {
        MobEffectInstance currentEffect = player.getEffect(GoetyEffects.CORPSE_EATER.get());

        int newAmplifier = 0;
        int newDuration = 1200; 

        if (currentEffect != null) {
            
            newAmplifier = Math.min(currentEffect.getAmplifier() + 1, 3);
            
            newDuration = Math.min(currentEffect.getDuration() * 2, 12000); 
        }

        
        player.addEffect(new MobEffectInstance(
                GoetyEffects.CORPSE_EATER.get(),
                newDuration,
                newAmplifier,
                false, 
                true,  
                true   
        ));
    }

    private void handleSaveEffects(Player player) {
        MobEffectInstance currentEffect = player.getEffect(GoetyEffects.SAVE_EFFECTS.get());
        int durationToAdd = 36000; 

        int newDuration = durationToAdd;
        int amplifier = 0;

        if (currentEffect != null) {
            
            newDuration = currentEffect.getDuration() + durationToAdd;
            amplifier = currentEffect.getAmplifier();
        }

        
        player.addEffect(new MobEffectInstance(
                GoetyEffects.SAVE_EFFECTS.get(),
                newDuration,
                amplifier,
                false, 
                true,  
                true   
        ));
    }
}