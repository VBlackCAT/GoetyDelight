package net.v_black_cat.goetydelight.item.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.v_black_cat.goetydelight.item.ModItems;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class HaoZiItem extends Item {

    public static final String TAG_ENCHANTED = "HaoZiEnchanted";

    public HaoZiItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.goetydelight.hao_zi"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isEnchantedHaoZi(stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return isEnchantedHaoZi(stack) ? Rarity.RARE : Rarity.COMMON;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        boolean enchanted = isEnchantedHaoZi(stack);
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (enchanted && !level.isClientSide) {
            releaseWardenSonicBoom(level, entity);
        }
        return result;
    }

    public static boolean isEnchantedHaoZi(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_ENCHANTED);
    }

    public static void makeEnchanted(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(TAG_ENCHANTED, true);
    }

    private void releaseWardenSonicBoom(Level level, LivingEntity source) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        double radius = 5.0D;
        Vec3 start = source.position().add(0, source.getBbHeight() * 0.5D, 0);

        level.playSound(null, source.getX(), source.getY(), source.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.0F);

        AABB area = new AABB(
                source.getX() - radius, source.getY() - radius, source.getZ() - radius,
                source.getX() + radius, source.getY() + radius, source.getZ() + radius
        );

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : targets) {
            if (target == source) continue;
            if (!target.isAlive()) continue;
            if (target instanceof Player p && p.isCreative()) continue;

            Vec3 end = target.position().add(0, target.getBbHeight() * 0.5D, 0);

            spawnSonicBoomTrail(serverLevel, start, end);
            target.hurt(level.damageSources().sonicBoom(source), 10.0F);

            Vec3 dir = end.subtract(start).normalize();
            target.push(dir.x * 5D, 2D, dir.z * 5D);
            target.hurtMarked = true;
        }
    }

    private void spawnSonicBoomTrail(net.minecraft.server.level.ServerLevel serverLevel,
                                     Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        double step = 0.5D; // 每 0.5 格刷一个，连成一条线
        int count = Math.max(1, (int) Math.ceil(distance / step));

        Vec3 direction = end.subtract(start).normalize();

        for (int i = 0; i <= count; i++) {
            Vec3 p = start.add(direction.scale(i * step));
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,
                    p.x, p.y, p.z,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        String name = player.getGameProfile().getName();
        if (!"Player__VI".equals(name) && !"vanilla_haozi".equals(name)) return;

        ItemStack haoZi = new ItemStack(ModItems.HAO_ZI.get());
        HaoZiItem.makeEnchanted(haoZi);

        ItemEntity drop = new ItemEntity(
                player.level(),
                player.getX(), player.getY(), player.getZ(),
                haoZi
        );
        drop.setDefaultPickUpDelay();
        player.level().addFreshEntity(drop);
    }
}