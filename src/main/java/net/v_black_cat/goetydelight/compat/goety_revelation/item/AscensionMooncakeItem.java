package net.v_black_cat.goetydelight.compat.goety_revelation.item;

import com.mega.endinglib.api.client.text.TextColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.compat.goety_revelation.RevelationCompatRegistry;
import net.v_black_cat.goetydelight.item.ModItems;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Mod.EventBusSubscriber
public class AscensionMooncakeItem extends Item {
    private static final boolean IS_MID_AUTUMN =
            isMidAutumn(LocalDate.now(ZoneId.systemDefault()));

    public AscensionMooncakeItem(Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!IS_MID_AUTUMN) return;

        Entity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide()) return;

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null || !entityId.toString().equals("goety:apostle"))
            return;

        CompoundTag rootNbt = entity.saveWithoutId(new CompoundTag());
        if (!rootNbt.contains("isApollyon", CompoundTag.TAG_BYTE) || rootNbt.getByte("isApollyon") != 1)
            return;

        ItemStack stack = new ItemStack(RevelationCompatRegistry.ASCENSION_MOONCAKE.get());
        ItemEntity itemEntity = new ItemEntity(
                level,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                stack
        );
        level.addFreshEntity(itemEntity);
    }
    private static boolean isMidAutumn(LocalDate date) {
        int year = date.getYear();
        LocalDate midAutumn = switch (year) {
            case 2026 -> LocalDate.of(2026, 9, 25);
            case 2027 -> LocalDate.of(2027, 9, 15);
            case 2028 -> LocalDate.of(2028, 10, 3);
            case 2029 -> LocalDate.of(2029, 9, 22);
            case 2030 -> LocalDate.of(2030, 9, 12);
            case 2031 -> LocalDate.of(2031, 10, 1);
            case 2032 -> LocalDate.of(2032, 9, 19);
            case 2033 -> LocalDate.of(2033, 9, 8);
            case 2034 -> LocalDate.of(2034, 9, 27);
            case 2035 -> LocalDate.of(2035, 9, 16);
            default -> null;
        };
        return midAutumn != null && midAutumn.equals(date);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.goetydelight.ascension_mooncake.tooltip.1")
                .withStyle(TextColorUtils.MIDDLE)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}