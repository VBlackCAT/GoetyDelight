package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SevenLeafPuddingItem extends BowlFoodItem {

    private static final ResourceLocation ATTACK_DAMAGE_BONUS_ID = ResourceLocation.fromNamespaceAndPath("goetydelight", "seven_leaf_pudding_attack_bonus");
    private static final ResourceLocation MOVEMENT_SPEED_BONUS_ID = ResourceLocation.fromNamespaceAndPath("goetydelight", "seven_leaf_pudding_speed_bonus");

    private static final String BONUS_ACTIVE_TAG = "SevenLeafPuddingActive";
    private static final String ACTIVATION_TIME_TAG = "SevenLeafPuddingActivationTime";

    // 持续时间（5分钟）
    private static final int DURATION_TICKS = 20 * 60 * 5;

    public SevenLeafPuddingItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag persistentData = player.getPersistentData();

            // 如果已有活跃加成，先移除
            if (persistentData.getBoolean(BONUS_ACTIVE_TAG)) {
                removeBonusAttributes(player);
            }

            // 添加新加成
            addBonusAttributes(player);

            // 记录激活数据
            persistentData.putBoolean(BONUS_ACTIVE_TAG, true);
            persistentData.putLong(ACTIVATION_TIME_TAG, level.getGameTime());
        }

        return resultStack;
    }

    private void addBonusAttributes(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && !attackDamage.hasModifier(ATTACK_DAMAGE_BONUS_ID)) {
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_BONUS_ID,
                    3.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null && !movementSpeed.hasModifier(MOVEMENT_SPEED_BONUS_ID)) {
            movementSpeed.addTransientModifier(new AttributeModifier(
                    MOVEMENT_SPEED_BONUS_ID,
                    0.05,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    public void removeBonusAttributes(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_BONUS_ID);
        }

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_BONUS_ID);
        }

        player.getPersistentData().remove(BONUS_ACTIVE_TAG);
        player.getPersistentData().remove(ACTIVATION_TIME_TAG);
    }

    /**
     * 检查并移除过期的加成效果。
     * 由 PlayerTick 事件处理器调用。
     */
    public static boolean checkAndRemoveExpired(Player player, Level level) {
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.getBoolean(BONUS_ACTIVE_TAG)) {
            long activationTime = persistentData.getLong(ACTIVATION_TIME_TAG);
            if (level.getGameTime() - activationTime >= DURATION_TICKS) {
                var item = (SevenLeafPuddingItem) net.v_black_cat.goetydelight.init.ModItems.SEVEN_LEAF_PUDDING.get();
                item.removeBonusAttributes(player);
                return true;
            }
        }
        return false;
    }

    /**
     * 玩家死亡时移除加成
     */
    public static void onPlayerDeath(Player player) {
        if (player.getPersistentData().getBoolean(BONUS_ACTIVE_TAG)) {
            var item = (SevenLeafPuddingItem) net.v_black_cat.goetydelight.init.ModItems.SEVEN_LEAF_PUDDING.get();
            item.removeBonusAttributes(player);
        }
    }
}
