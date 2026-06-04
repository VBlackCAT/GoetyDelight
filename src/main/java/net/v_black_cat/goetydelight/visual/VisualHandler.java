package net.v_black_cat.goetydelight.visual;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.GoetyDelight;

import static net.v_black_cat.goetydelight.visual.GDVisualEffects.RED_EYE_FLASH_KEY;

@Mod.EventBusSubscriber(modid = GoetyDelight.MODID)
public final class VisualHandler {


    @SubscribeEvent
    public static void onSkeletonChangeTarget(LivingChangeTargetEvent event) {
        
        
        if (event.getEntity() instanceof AbstractSkeleton skeleton) {

            
            if (event.getNewTarget() instanceof Player player) {

                
                if (player.getHealth() < 6.0F) {

                    
                    
                    EntityVisualEffectSystem.addEffect(skeleton, RED_EYE_FLASH_KEY, 50, new CompoundTag());
                }
            }
        }
    }
    private static final ResourceLocation DOLL_ENTITY_ID = new ResourceLocation("goetydelight", "doll_entity");
    private static final ResourceLocation FLAME_EFFECT_ID = new ResourceLocation("goetydelight", "volumetric_flame");
    
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        
        if (event.getLevel().isClientSide) {
            return;
        }

        
        if (event.getItemStack().is(Items.FLINT_AND_STEEL)) {
            Entity target = event.getTarget();
            ResourceLocation targetId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

            
            if (DOLL_ENTITY_ID.equals(targetId)) {

                
                
                CompoundTag entityNbt = new CompoundTag();
                target.saveWithoutId(entityNbt);

                if (entityNbt.contains("custom_doll_id") && "doll_p7".equals(entityNbt.getString("custom_doll_id"))) {

                    
                    

                    try {
                        
                        CompoundTag data = TagParser.parseTag(
                                "{Color:[0.12f,0.0f,0.0f],CoreColor:[0.55f,0.02f,0.0f],TipColor:[0.22f,0.0f,0.0f],SmokeColor:[0.0f,0.0f,0.0f],Intensity:0.65f}"
                        );

                        
                        if (EntityVisualEffectSystem.addEffect(target, FLAME_EFFECT_ID, 0, data)) {

                            
                            if (!event.getEntity().isCreative()) {
                                event.getItemStack().hurtAndBreak(1, event.getEntity(),
                                        (player) -> player.broadcastBreakEvent(event.getHand()));
                            }

                            event.setCancellationResult(InteractionResult.SUCCESS);
                            event.setCanceled(true);
                        }
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}