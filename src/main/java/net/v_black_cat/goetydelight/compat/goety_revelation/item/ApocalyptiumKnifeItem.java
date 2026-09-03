package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vectorwing.farmersdelight.common.item.KnifeItem;
import net.v_black_cat.goetydelight.util.DelightLootTableCache;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber
public class ApocalyptiumKnifeItem extends KnifeItem {
    public ApocalyptiumKnifeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof ApocalyptiumKnifeItem) {
                DamageSource source = event.getSource();
                Holder<DamageType> damageTypeHolder = source.typeHolder();
                if (damageTypeHolder instanceof Holder.Reference<DamageType> reference) {
                    reference.bindTags(Set.of(
                            DamageTypeTags.BYPASSES_ARMOR,
                            DamageTypeTags.BYPASSES_ENCHANTMENTS,
                            DamageTypeTags.BYPASSES_RESISTANCE
                    ));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDropsEvent(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof ApocalyptiumKnifeItem) {
                LivingEntity target = event.getEntity();
                ServerLevel level = (ServerLevel) target.level();

                // 获取目标的实体类型
                EntityType<?> entityType = target.getType();

                // 从缓存获取delight掉落物（直接获取 List<ItemStack>）
                List<ItemStack> delightDrops = DelightLootTableCache.getDelightDropsForMob(entityType);
                // 获取抢夺等级
                int lootingLevel = EnchantmentHelper.getTagEnchantmentLevel(
                        Enchantments.MOB_LOOTING,
                        heldItem
                );
                if (!delightDrops.isEmpty()) {
                    // 强制额外掉落 2 次，不受抢夺影响
                    for (int extraDrop = 0; extraDrop < 2; extraDrop++) {
                        for (ItemStack dropStack : delightDrops) {
                            ItemStack droppedItem = dropStack.copy();
                            if (!droppedItem.isEmpty()) {
                                spawnDropItem(level, target, droppedItem);
                            }
                        }
                    }
                }
                // 使用抢夺修饰的额外掉落
                if (lootingLevel > 0) {
                    int enhancedLooting = lootingLevel * 2;
                    for (ItemStack dropStack : delightDrops) {
                        // 抢夺增加额外掉落机会
                        int bonus = level.random.nextInt(enhancedLooting + 1);
                        if (bonus > 0) {
                            ItemStack bonusStack = dropStack.copy();
                            bonusStack.setCount(Math.min(bonus, bonusStack.getMaxStackSize()));

                            if (!bonusStack.isEmpty()) {
                                spawnDropItem(level, target, bonusStack);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 生成掉落物实体的辅助方法
     */
    private static void spawnDropItem(ServerLevel level, LivingEntity target, ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(
                level,
                target.getX(),
                target.getY(),
                target.getZ(),
                itemStack
        );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
}