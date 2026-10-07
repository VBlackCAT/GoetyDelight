package net.v_black_cat.goetydelight.compat.goetyrevelation.item;

import com.mega.endinglib.api.time.TimeStopAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ApollyonCakeSliceItem extends Item {

    public static final String TAG_IS_THE_END = "SliceIsTheEnd";

    /** 时停持续 tick 数，按需调整 */
    private static final int TIME_STOP_DURATION = 200;

    /** 是否强制覆盖已有时间停止 */
    private static final boolean TIME_STOP_FORCE = false;

    /** 是否播放音效；true 走 use，false 走 useWithoutSoundEffect */
    private static final boolean TIME_STOP_PLAY_SOUND = true;

    public ApollyonCakeSliceItem(Properties properties) {
        super(properties);
    }

    /** 由蛋糕方块调用，生成带标记的切片 */
    public static ItemStack createStack(Item item, boolean isTheEnd) {
        ItemStack stack = new ItemStack(item);
        if (isTheEnd) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(TAG_IS_THE_END, true);
        }
        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_IS_THE_END);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        CompoundTag tag = stack.getTag();
        boolean isTheEnd = tag != null && tag.getBoolean(TAG_IS_THE_END);

        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && isTheEnd) {
            if (TIME_STOP_PLAY_SOUND) {
                TimeStopAPI.use(true, entity, TIME_STOP_FORCE, TIME_STOP_DURATION);
            } else {
                TimeStopAPI.useWithoutSoundEffect(true, entity, TIME_STOP_FORCE, TIME_STOP_DURATION);
            }
        }

        return result;
    }
}