package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.renderer.FalseProverbsItemRender;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FalseProverbsItem extends SwordItem {

    /** 主背包槽位数（0-35）；36-39 为盔甲槽，40 为副手槽。 */
    private static final int MAIN_INVENTORY_SIZE = 36;

    /** 附件值：没有背在背上。 */
    public static final int NO_BACK_SLOT = -1;

    // 玩家数据管理（传送状态）
    private static final Map<UUID, PlayerFalseProverbsData> playerDataMap = new ConcurrentHashMap<>();

    private static FalseProverbsItemRender renderer = null;
    private static final float ADDED_DAMAGE = 0.0f;

    // 玩家数据封装类
    private static class PlayerFalseProverbsData {
        boolean teleportStatus = false;
        Vec3 originalPosition = null;
        WeakReference<Level> worldLevel = null;

        void clearPosition() {
            originalPosition = null;
            worldLevel = null;
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

    // 玩家数据访问方法
    private static PlayerFalseProverbsData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerFalseProverbsData());
    }

    public static boolean getPlayerTeleportStatus(UUID playerUUID) {
        return getPlayerData(playerUUID).teleportStatus;
    }

    public static void setPlayerTeleportStatus(UUID playerUUID, boolean status) {
        getPlayerData(playerUUID).teleportStatus = status;
    }

    public static void removePlayerTeleportStatus(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        if (data != null) {
            data.teleportStatus = false;
            data.clearPosition();
        }
    }

    public static Vec3 getOriginalPosition(UUID playerUUID) {
        return getPlayerData(playerUUID).originalPosition;
    }

    public static void setOriginalPosition(UUID playerUUID, Vec3 position) {
        getPlayerData(playerUUID).originalPosition = position;
    }

    public static void setWorldLevel(UUID playerUUID, Level level) {
        getPlayerData(playerUUID).worldLevel = new WeakReference<>(level);
    }

    public static Level getWorldLevel(UUID playerUUID) {
        PlayerFalseProverbsData data = playerDataMap.get(playerUUID);
        return data != null && data.worldLevel != null ? data.worldLevel.get() : null;
    }

    // ==================== 背部模型状态 ====================
    // 旧实现：每 5 tick 扫描背包 + 静态缓存 + 自定义同步包。
    // 新实现：状态由物品自身的 inventoryTick 驱动（剑在背包里才会被调用，
    // 剑不在背包时服务器端零开销），值放在同步附件里交给 NeoForge 传输。

    /**
     * 「背在背上」= 剑在主背包（0-35）且不是当前选中格、且副手没有拿剑。
     * 与旧逻辑等价：副手持剑不显示；主手选中格不算「背着」；
     * 主手拿着剑但背包里还有一把时，背包里那把依然背在背上。
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || isSelected) return;
        // 排除盔甲槽（36-39）与副手槽（40）：NeoForge 的 Inventory#tick 传入全局槽位
        if (slotId < 0 || slotId >= MAIN_INVENTORY_SIZE) return;
        if (!(entity instanceof Player player)) return;
        updateBackSlot(player, slotId);
    }

    /** 记录背上的剑所在槽位；值没变化就不写附件（避免每 tick 写数据 + 同步）。 */
    private static void updateBackSlot(Player player, int slotId) {
        if (player.getOffhandItem().getItem() instanceof FalseProverbsItem) return;
        int current = player.getData(ModAttachments.FALSE_PROVERBS_BACK_SLOT.get());
        // 已经记着自己，或者已经记着另一把合法的剑 → 不动，避免同一 tick 内互相覆盖
        if (current == slotId || isBackSlotValid(player, current)) return;
        setBackSlot(player, slotId);
    }

    /**
     * 服务端每 tick 调用一次兜底校验：剑被丢弃 / 移进容器 / 换到手上 / 死亡掉落时
     * 都不会再触发 inventoryTick，必须在这里把附件清掉。
     * 没有背着剑时只读一次附件就返回，开销可忽略。
     */
    public static void validateBackSlot(Player player) {
        int slot = player.getData(ModAttachments.FALSE_PROVERBS_BACK_SLOT.get());
        if (slot < 0 || isBackSlotValid(player, slot)) return;
        setBackSlot(player, NO_BACK_SLOT);
    }

    private static boolean isBackSlotValid(Player player, int slot) {
        if (slot < 0 || slot >= MAIN_INVENTORY_SIZE) return false;
        Inventory inventory = player.getInventory();
        if (inventory.selected == slot) return false;
        if (player.getOffhandItem().getItem() instanceof FalseProverbsItem) return false;
        return inventory.getItem(slot).getItem() instanceof FalseProverbsItem;
    }

    private static void setBackSlot(Player player, int slot) {
        // Entity#setData 内部会自动调用 AttachmentSync.syncEntityUpdate（含玩家本人）
        player.setData(ModAttachments.FALSE_PROVERBS_BACK_SLOT.get(), slot);
    }

    /** 玩家登出 / 清理 */
    public static void clearPlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    @OnlyIn(Dist.CLIENT)
    public IClientItemExtensions getClientExtensions() {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    var mc = Minecraft.getInstance();
                    renderer = new FalseProverbsItemRender(
                            mc.getBlockEntityRenderDispatcher(),
                            mc.getEntityModels()
                    );
                }
                return renderer;
            }
        };
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}