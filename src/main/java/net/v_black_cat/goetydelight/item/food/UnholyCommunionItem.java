package net.v_black_cat.goetydelight.item.food;

import com.Polarice3.Goety.common.items.ModItems;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.v_black_cat.goetydelight.effect.ModEffects;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public class UnholyCommunionItem extends Item {

    private static final String TAG_EXTRA_HEAD = "goetydelight:extra_head_slot";
    private static final UUID EXTRA_HEAD_UUID = UUID.nameUUIDFromBytes("goetydelight:extra_head_slot".getBytes());

    public UnholyCommunionItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player) {
            CompoundTag data = player.getPersistentData();
            if (!data.getBoolean(TAG_EXTRA_HEAD)) {
                data.putBoolean(TAG_EXTRA_HEAD, true);

                CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
                    inventory.addPermanentSlotModifiers(ImmutableMultimap.of(
                            "head",
                            new AttributeModifier(
                                    EXTRA_HEAD_UUID,
                                    "goetydelight:extra_head_slot",
                                    1.0,
                                    AttributeModifier.Operation.ADDITION
                            )
                    ));
                });
            }
            MobEffectInstance instance = player.getEffect(ModEffects.THE_PALE_MESSRNGER.get());
            if (instance != null) {
                MobEffectInstance doubled = new MobEffectInstance(
                        ModEffects.THE_PALE_MESSRNGER.get(),
                        instance.getDuration() * 2,
                        instance.getAmplifier(),
                        instance.isAmbient(),
                        instance.isVisible(),
                        instance.showIcon()
                );
                player.removeEffect(ModEffects.THE_PALE_MESSRNGER.get());
                player.addEffect(doubled);
            }
            if (player.getAbilities().instabuild) {
                return result;
            }
            if (result.isEmpty()) {
                return new ItemStack(ModItems.UNHOLY_HAT.get());
            } else if (!player.getInventory().add(new ItemStack(ModItems.UNHOLY_HAT.get()))) {
                player.drop(new ItemStack(ModItems.UNHOLY_HAT.get()), false);
            }
        }
        return result;
    }
}