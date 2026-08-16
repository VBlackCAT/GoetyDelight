package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.block.ModBlocks;
import net.minecraft.util.RandomSource;
import net.v_black_cat.goetydelight.item.ModItems;

@Mod.EventBusSubscriber(modid = "goetydelight")
public class RoastLaowangItem extends Item{
    public RoastLaowangItem(Item.Properties properties) {
        super(properties);
    }
    @Override
    public boolean isFoil(ItemStack pStack) {return true;}

    private static final String BAKATAG = "bakatag";
    static Float bakatime= 0.0f;
    static Boolean bakatime_flag=false;

    public static void randomFlag(Float bakatime) {
        if (bakatime != null && bakatime > 0.0f) {
            bakatime_flag = RandomSource.create().nextBoolean();
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().getType() == EntityType.PIG && !event.getEntity().isBaby()) {
            if (event.getSource().typeHolder().unwrapKey().isPresent()) {
                String damageTypeName = event.getSource().typeHolder().unwrapKey().get().location().toString();
                if (damageTypeName.contains("lightning") || damageTypeName.contains("shock")) {
                    if (event.getEntity().getRandom().nextFloat() < 0.50f) {
                        ItemStack roastLaowang = new ItemStack(ModItems.ROAST_LAOWANG.get());
                        event.getEntity().spawnAtLocation(roastLaowang);
                    }
                }
            }
        }
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            if(player.getName().getString().equals("laowang237")){
                if (!event.getSource().is(DamageTypeTags.IS_FIRE)) {
                ItemStack roastLaowang = new ItemStack(ModItems.ROAST_LAOWANG.get());
                player.spawnAtLocation(roastLaowang);}
                else {
                    ItemStack roastLaowangblock = new ItemStack(ModBlocks.ROAST_LAOWANG_BLOCK.get());
                    player.spawnAtLocation(roastLaowangblock);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() instanceof RoastLaowangItem) {
            bakatime = 200.0f;
            LivingEntity entity = event.getEntity();
            CompoundTag tag = entity.getPersistentData();
            tag.putFloat(BAKATAG, bakatime);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Level level = player.level();
        if (!level.isClientSide) {
            CompoundTag persistentData = player.getPersistentData();
            if (persistentData.getBoolean(BAKATAG)) {
                long activationTime = persistentData.getLong(BAKATAG);
                long currentTime = level.getGameTime();
                if (currentTime - activationTime >=bakatime) {
                    persistentData.remove(BAKATAG);
                }
            }
        }
    }

    @SubscribeEvent
    public static void DamageEvent(LivingDamageEvent event) {
        // 检查受伤实体是否有BAKATAG标签
        if (event.getEntity().getPersistentData().contains(BAKATAG) && bakatime_flag == true) {
            event.setCanceled(true); // 取消伤害事件，使实体免疫伤害
            return;
        }
        // 或者检查攻击者是否有BAKATAG标签（防止造成伤害）
        if (event.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            if (attacker.getPersistentData().contains(BAKATAG) && bakatime_flag == true) {
                event.setCanceled(true); // 取消伤害事件，攻击者无法造成伤害
            }
        }
    }
}