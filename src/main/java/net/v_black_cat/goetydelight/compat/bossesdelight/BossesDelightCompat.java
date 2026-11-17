package net.v_black_cat.goetydelight.compat.bossesdelight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.v_black_cat.goetydelight.block.BoatStuffedRoastedWardenBlock;

/**
 * BOMD 乐事（Bosses Delight）花蜜果冻冰兼容。
 * <p>
 * BOMD 的 {@code NectarJelly} 会把被右击的单个方块替换为 {@code defaultBlockState()} 来“回满”。
 * 这对普通单方块 FeastBlock 没问题，但烤全监是 9 格多方块：点中外围幽灵方块时，
 * 该格会被替换成 CENTER，结构校验失败后整盘被拆掉；点中中心时也只更新中心一格。
 * <p>
 * BOMD 自己还会在 {@code InteractEvents} 里把 FeastBlock 的 {@code UseBlock} 设为 DENY，
 * 所以这里以 LOWEST 优先级覆盖回 ALLOW，让 {@link BoatStuffedRoastedWardenBlock#use}
 * 能对整盘执行回满逻辑。
 * <p>
 * 该类只在检测到 {@code bosses_delight} 后由 {@code CompatRegistry} 注册，
 * 未安装 BOMD 时不会加载本类，也不会有任何右键事件开销。
 */
public final class BossesDelightCompat {

    private static final ResourceLocation NECTAR_JELLY_ID =
            new ResourceLocation("bosses_delight", "nectar_jelly");

    /** 只解析一次，避免每次右键都走 ForgeRegistries 的 key 查找。 */
    private static Item nectarJelly;
    private static boolean nectarJellyResolved;

    private BossesDelightCompat() {
    }

    /** 仅在 BOMD 已加载时调用。 */
    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.register(BossesDelightCompat.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof BoatStuffedRoastedWardenBlock)) {
            return;
        }

        if (!isNectarJelly(event.getItemStack())) {
            return;
        }

        event.setUseBlock(Event.Result.ALLOW);
        event.setUseItem(Event.Result.DENY);
    }

    private static boolean isNectarJelly(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!nectarJellyResolved) {
            nectarJellyResolved = true;
            nectarJelly = ForgeRegistries.ITEMS.getValue(NECTAR_JELLY_ID);
        }
        return nectarJelly != null && stack.getItem() == nectarJelly;
    }
}
