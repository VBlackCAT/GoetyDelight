package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.v_black_cat.goetydelight.init.ModAttachments;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FalseProverbsItem extends SwordItem {

    /** 主背包槽位数（0-35）；36-39 为盔甲槽，40 为副手槽。 */
    private static final int MAIN_INVENTORY_SIZE = 36;

    /** 附件值：没有背在背上。 */
    public static final int NO_BACK_SLOT = -1;

    // 玩家数据管理（传送状态）。
    // 仍用 ConcurrentHashMap：伤害事件客户端也会触发预测，不保证只有服务端线程碰它。
    private static final Map<UUID, PlayerFalseProverbsData> playerDataMap = new ConcurrentHashMap<>();

    private static final float ADDED_DAMAGE = 0.0f;

    /**
     * 解析一次就存下来的附件类型。{@code ModAttachments.FALSE_PROVERBS_BACK_SLOT} 声明成
     * {@code Supplier}（运行时是 {@code DeferredHolder}），每次 {@code get()} 都要走一层虚拟调用；
     * 而 {@code refreshBackSlot} 在每次背包变化 / 切格时都要读它，缓存掉这层 Supplier 解析。
     * 附件类型在 mod 加载时注册完就不再变，缓存是安全的。
     */
    private static AttachmentType<Integer> backSlotType;

    // 玩家数据封装类
    private static class PlayerFalseProverbsData {
        boolean teleportStatus = false;
        Vec3 originalPosition = null;

        void clearPosition() {
            originalPosition = null;
        }
    }

    public FalseProverbsItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(
                ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("base_attack_damage"),
                                        tier.getAttackDamageBonus(),
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("base_attack_speed"),
                                        -2.0,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("false_proverbs_boost"),
                                        ADDED_DAMAGE,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.MAINHAND)
                        .build()
        ));
    }

    public FalseProverbsItem(Tier tier, int attackDamageModifier, float attackSpeed, Properties properties) {
        this(tier, properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.DARK_ALLOY_INGOT.get());
    }

    // ==================== 玩家数据（传送状态） ====================
    // 读路径一律走 Map#get：原来读也调 computeIfAbsent，等于每次读都做一次「可能写入」的
    // 加锁/装箱流程，而 getPlayerTeleportStatus / getOriginalPosition 会被伤害事件和每 5 tick
    // 的状态机反复调用（spark 里最热的那条 mod 链就是 setOriginalPosition → getPlayerData）。
    // 写路径才建对象，避免给「只是砍了一刀」的玩家留下空记录。

    /** 写路径：确实要写的时候才建对象。 */
    private static PlayerFalseProverbsData dataForWrite(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerFalseProverbsData());
    }

    /**
     * Shift 起手：一次查表把「原位 + 传送状态」全写完。
     * 原来是 setOriginalPosition / setPlayerTeleportStatus 两次独立调用，
     * 每次都要一次 ConcurrentHashMap 操作，而这是每次起手都会走的路径。
     *
     * <p>维度不再记录：换维度由 {@code FalseProverbsEvents#onChangedDimension} 事件
     * 直接把原位清掉，不需要在这里存世界对象、再每隔几 tick 去比对。
     */
    public static void beginTeleport(UUID playerUUID, Vec3 position) {
        PlayerFalseProverbsData data = dataForWrite(playerUUID);
        data.originalPosition = position;
        data.teleportStatus = true;
    }

    public static boolean getPlayerTeleportStatus(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        return data != null && data.teleportStatus;
    }

    public static void setPlayerTeleportStatus(UUID playerUUID, boolean status) {
        dataForWrite(playerUUID).teleportStatus = status;
    }

    public static void removePlayerTeleportStatus(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        if (data != null) {
            data.teleportStatus = false;
            data.clearPosition();
        }
    }

    public static Vec3 getOriginalPosition(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        return data != null ? data.originalPosition : null;
    }

    /** 传 null = 清掉原位记录；没有记录时什么都不做（不建对象）。 */
    public static void setOriginalPosition(UUID playerUUID, Vec3 position) {
        if (position == null) {
            PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
            if (data != null) {
                data.clearPosition();
            }
            return;
        }
        dataForWrite(playerUUID).originalPosition = position;
    }

    // ==================== 背部模型状态 ====================
    // 这里<b>不重写 Item#inventoryTick</b>：那个钩子是原版每 tick 对背包里每个非空格子调一次，
    // 属于「物品被动接受轮询」。改成只在背包真的发生变化时重算，信号全部来自原版 / 事件：
    //   1. 槽位内容变化 —— 玩家容器菜单上的 ContainerListener#slotChanged。原版
    //      AbstractContainerMenu#triggerSlotListeners 每 tick 比对 lastSlots，只在 ItemStack.matches
    //      为假时才回调；比对本身是原版本来就要做的活，我们零额外开销。任何菜单（自己的物品栏、
    //      箱子、工作台……）都包含玩家背包，所以覆盖没有死角。
    //   2. 快捷栏选中格变化 —— 不改变任何槽位内容，原版不会通知，由 ServerGamePacketListenerImplMixin
    //      在那条上行包处理完时补一次。
    //   3. 登录 / 重生 / 换维度 —— 实体与菜单都会重建，重新挂监听并重算一次。
    // 值放在同步附件里交给 NeoForge 传输，客户端只读它。

    private static AttachmentType<Integer> backSlotType() {
        AttachmentType<Integer> type = backSlotType;
        if (type == null) {
            backSlotType = type = ModAttachments.FALSE_PROVERBS_BACK_SLOT.get();
        }
        return type;
    }

    /**
     * 从背包重算「背上那把剑」的槽位并写回附件；只在上面三类信号到来时调用。
     *
     * <p>代价是一次最多 36 格的扫描，但只在背包真的变了才跑；结果没变就不写附件
     * （{@code setData} 会触发同步包）。已经记着的那把仍然合法就沿用，背包里有两把时不会来回跳。
     */
    public static void refreshBackSlot(Player player) {
        int current = player.getData(backSlotType());
        int found = findBackSlot(player, current);
        if (found != current) {
            setBackSlot(player, found);
        }
    }

    /**
     * 规则：主背包（0-35）里、不在当前选中格的那把剑。
     *
     * <p>副手 / 主手拿着剑都不影响：副手那把与背包里这把是两个不同的栈，副手照旧渲染副手模型，
     * 背包里那把仍然背在背上；主手拿着的那把只体现在「那一格是选中格」这一条上。
     */
    private static int findBackSlot(Player player, int current) {
        Inventory inventory = player.getInventory();
        if (isBackSlot(inventory, current)) {
            return current;
        }
        for (int slot = 0; slot < MAIN_INVENTORY_SIZE; slot++) {
            if (slot != inventory.selected && inventory.getItem(slot).getItem() instanceof FalseProverbsItem) {
                return slot;
            }
        }
        return NO_BACK_SLOT;
    }

    /** 这一格算不算「背在背上」：在主背包（0-35）、不是当前选中格、且那一格里确实是把剑。 */
    private static boolean isBackSlot(Inventory inventory, int slot) {
        return slot >= 0 && slot < MAIN_INVENTORY_SIZE
                && slot != inventory.selected
                && inventory.getItem(slot).getItem() instanceof FalseProverbsItem;
    }

    private static void setBackSlot(Player player, int slot) {
        // Entity#setData 内部会自动调用 AttachmentSync.syncEntityUpdate（含玩家本人）
        player.setData(backSlotType(), slot);
    }

    /** 玩家登出 / 清理 */
    public static void clearPlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}
