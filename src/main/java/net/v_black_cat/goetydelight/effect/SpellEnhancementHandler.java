package net.v_black_cat.goetydelight.effect;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.events.spell.StartMagicEvent;
import com.Polarice3.Goety.common.events.spell.StopMagicEvent;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SpellEnhancementHandler {

    @SubscribeEvent
    public static void onStartMagic(StartMagicEvent event) {
        LivingEntity caster = event.getEntity();
        ItemStack itemStack = event.getUseItem();

        boolean hasPotencyBuff = false;
        boolean hasDurationBuff = false;
        int potencyBuffLevel = 0;
        int durationBuffLevel = 0;

        
        if (caster.hasEffect(ModEffects.SPELL_MASTERY.get())) {
            potencyBuffLevel = caster.getEffect(ModEffects.SPELL_MASTERY.get()).getAmplifier() + 1;
            hasPotencyBuff = true;
        }

        
        if (caster.hasEffect(ModEffects.SPELL_DURATION.get())) {
            durationBuffLevel = caster.getEffect(ModEffects.SPELL_DURATION.get()).getAmplifier() + 1;
            hasDurationBuff = true;
        }

        
        if (hasPotencyBuff || hasDurationBuff) {
            if (itemStack.getItem() instanceof DarkWand) {
                ItemStack magicFocus = IWand.getFocus(itemStack);

                if (magicFocus != null && !magicFocus.isEmpty()) {
                    
                    if (!hasBeenBoosted(magicFocus)) {
                        
                        applyEnhancement(magicFocus, potencyBuffLevel, durationBuffLevel);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onStopMagic(StopMagicEvent event) {
        LivingEntity caster = event.getEntity();
        ItemStack itemStack = event.getUseItem();

        if (itemStack.getItem() instanceof DarkWand) {
            ItemStack magicFocus = IWand.getFocus(itemStack);

            if (magicFocus != null && !magicFocus.isEmpty()) {
                
                if (hasBeenBoosted(magicFocus)) {
                    removeEnhancement(magicFocus);
                }
            }
        }
    }

    
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity) event.getEntity();
            ItemStack stack = itemEntity.getItem();

            
            if (stack.getItem() instanceof DarkWand) {
                ItemStack magicFocus = IWand.getFocus(stack);

                if (magicFocus != null && !magicFocus.isEmpty()) {
                    
                    if (hasBeenBoosted(magicFocus)) {
                        removeEnhancement(magicFocus);
                        
                        itemEntity.setItem(stack);
                    }
                }
            }
            
            else if (stack.getItem() instanceof IFocus) {
                if (hasBeenBoosted(stack)) {
                    removeEnhancement(stack);
                    itemEntity.setItem(stack);
                }
            }
        }
    }

    
    private static boolean hasBeenBoosted(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        return tag != null && tag.contains("Boosted");
    }

    
    private static void applyEnhancement(ItemStack itemStack, int potencyBuffLevel, int durationBuffLevel) {
        CompoundTag tag = itemStack.getOrCreateTag();
        CompoundTag boostTag = new CompoundTag();

        
        boostTag.putInt("PotencyBoost", potencyBuffLevel);
        boostTag.putInt("DurationBoost", durationBuffLevel);

        
        if (potencyBuffLevel > 0) {
            handleEnchantment(itemStack, "goety:potency", potencyBuffLevel, boostTag);
        }

        
        if (durationBuffLevel > 0) {
            handleEnchantment(itemStack, "goety:duration", durationBuffLevel, boostTag);
        }

        
        tag.put("Boosted", boostTag);
    }

    
    private static void handleEnchantment(ItemStack itemStack, String enchantId, int buffLevel, CompoundTag boostTag) {
        CompoundTag tag = itemStack.getOrCreateTag();
        ListTag enchantments = tag.getList("Enchantments", 10);
        int originalLevel = 0;
        int index = -1;

        
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantTag = enchantments.getCompound(i);
            if (enchantId.equals(enchantTag.getString("id"))) {
                originalLevel = enchantTag.getShort("lvl");
                index = i;
                break;
            }
        }

        
        int newLevel = originalLevel + buffLevel;
        boostTag.putInt(enchantId.replace(':', '_') + "_Original", originalLevel); 

        
        CompoundTag newEnchant = new CompoundTag();
        newEnchant.putString("id", enchantId);
        newEnchant.putShort("lvl", (short) newLevel);

        if (index >= 0) {
            enchantments.set(index, newEnchant);
        } else {
            enchantments.add(newEnchant);
        }

        tag.put("Enchantments", enchantments);
    }

    
    private static void removeEnhancement(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return;

        CompoundTag boostTag = tag.getCompound("Boosted");
        ListTag enchantments = tag.getList("Enchantments", 10);
        ListTag newEnchantments = new ListTag();

        
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantTag = enchantments.getCompound(i);
            String id = enchantTag.getString("id");
            String boostKey = id.replace(':', '_') + "_Original";

            if (boostTag.contains(boostKey)) {
                int originalLevel = boostTag.getInt(boostKey);
                
                if (originalLevel > 0) {
                    CompoundTag restoredEnchant = new CompoundTag();
                    restoredEnchant.putString("id", id);
                    restoredEnchant.putShort("lvl", (short) originalLevel);
                    newEnchantments.add(restoredEnchant);
                }
            } else {
                
                newEnchantments.add(enchantTag);
            }
        }

        
        if (newEnchantments.isEmpty()) {
            tag.remove("Enchantments");
        } else {
            tag.put("Enchantments", newEnchantments);
        }

        
        tag.remove("Boosted");
        if (tag.isEmpty()) {
            itemStack.setTag(null);
        }
    }
}