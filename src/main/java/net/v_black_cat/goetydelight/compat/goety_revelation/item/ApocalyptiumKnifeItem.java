package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vectorwing.farmersdelight.common.item.KnifeItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;

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
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof ApocalyptiumKnifeItem) {
                LivingEntity target = event.getEntity();
                ServerLevel level = (ServerLevel) target.level();
                int lootingLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MOB_LOOTING, heldItem);
                int enhancedLooting = lootingLevel * 2;
                ResourceLocation lootTableId = target.getLootTable();
                if (lootTableId != null) {
                    LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);
                    LootParams lootParams = new LootParams.Builder(level)
                            .withParameter(LootContextParams.THIS_ENTITY, target)
                            .withParameter(LootContextParams.ORIGIN, target.position())
                            .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                            .withParameter(LootContextParams.KILLER_ENTITY, player)
                            .withParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                            .create(LootContextParamSets.ENTITY);
                    LootContext context = new LootContext.Builder(lootParams).create(null);
                    ObjectArrayList<ItemStack> baseLoot = new ObjectArrayList<>();
                    lootTable.getRandomItemsRaw(context, baseLoot::add);
                    ObjectArrayList<ItemStack> modifiedLoot = ForgeHooks.modifyLoot(lootTableId, baseLoot, context);
                    Set<ResourceLocation> delightItemIds = new HashSet<>();
                    for (ItemStack stack : modifiedLoot) {
                        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
                        if (itemId != null && itemId.getNamespace().contains("delight")) {
                            delightItemIds.add(itemId);
                        }
                    }
                    for (int i = 0; i < modifiedLoot.size(); i++) {
                        ItemStack stack = modifiedLoot.get(i);
                        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
                        if (itemId != null && itemId.getNamespace().contains("delight")) {
                            if (enhancedLooting > 0 && stack.getCount() > 0) {
                                int bonus = level.random.nextInt(enhancedLooting + 1);
                                if (bonus > 0) {
                                    stack.grow(bonus);
                                }
                            }
                        }
                    }
                    for (int extraDrop = 0; extraDrop < 2; extraDrop++) {
                        for (ResourceLocation itemId : delightItemIds) {
                            ItemStack droppedItem = new ItemStack(
                                    net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemId),
                                    1
                            );

                            if (!droppedItem.isEmpty()) {
                                ItemEntity itemEntity = new ItemEntity(
                                        level,
                                        target.getX(),
                                        target.getY(),
                                        target.getZ(),
                                        droppedItem
                                );
                                itemEntity.setDefaultPickUpDelay();
                                level.addFreshEntity(itemEntity);
                            }
                        }
                    }
                }
            }
        }
    }
}