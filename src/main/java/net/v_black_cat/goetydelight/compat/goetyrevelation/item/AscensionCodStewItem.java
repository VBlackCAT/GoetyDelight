package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.GoetyDelight;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public class AscensionCodStewItem extends Item {

    /** 鳕鱼强化窗口的过期时间（游戏 tick），存在玩家持久化数据里 */
    public static final String TAG_COD_BOOST_EXPIRE = "goetydelight:cod_boost_expire";

    /** 胸饰槽 +1 的固定 UUID，保证多次调用只覆盖不叠加 */
    private static final UUID CHEST_SLOT_UUID =
            UUID.nameUUIDFromBytes("goetydelight:cod_boost_chest".getBytes());

    public AscensionCodStewItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag data = player.getPersistentData();
            long expire = level.getGameTime() + 20L * 60L * 20L; // 20 分钟
            data.putLong(TAG_COD_BOOST_EXPIRE, expire);
            CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
                inventory.addPermanentSlotModifiers(ImmutableMultimap.of(
                        "chest",
                        new AttributeModifier(
                                CHEST_SLOT_UUID,
                                "goetydelight:cod_boost_chest",
                                1.0,
                                AttributeModifier.Operation.ADDITION
                        )
                ));
            });
        }
        return result;
    }

    @SubscribeEvent
    public static void onFinishItem(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ItemStack used = event.getItem();
        if (!used.is(Items.COD) && !used.is(Items.COOKED_COD)) return;

        CompoundTag data = player.getPersistentData();
        long expire = data.getLong(TAG_COD_BOOST_EXPIRE);
        long now = player.level().getGameTime();
        if (expire <= 0L || now >= expire) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3 * 60 * 20, 3));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3 * 60 * 20, 4));
    }
}