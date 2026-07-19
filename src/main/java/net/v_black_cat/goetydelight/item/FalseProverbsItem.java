package net.v_black_cat.goetydelight.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.v_black_cat.goetydelight.renderer.FalseProverbsItemRender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FalseProverbsItem extends SwordItem {
    public static final String SHIFT_KEY_TAG = "IsShift";
    public static Vec3 originalPosition = null;
    public static Level worldLevel = null;

    private static final Map<UUID, Boolean> playerTeleportStatus = new HashMap<>();
    private static final Map<UUID, Boolean> playerBackModelStatus = new HashMap<>();

    private static FalseProverbsItemRender renderer = null;

    public FalseProverbsItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    // ★★★ 关键：重写 getClientExtensions（不加 @Override）★★★
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
                    System.out.println("[FalseProverbsItem] BEWLR 渲染器创建成功");
                }
                return renderer;
            }
        };
    }

    // ===== 状态管理（不变） =====
    public static boolean getPlayerTeleportStatus(UUID playerUUID) {
        return playerTeleportStatus.getOrDefault(playerUUID, false);
    }

    public static void setPlayerTeleportStatus(UUID playerUUID, boolean status) {
        playerTeleportStatus.put(playerUUID, status);
    }

    public static void removePlayerTeleportStatus(UUID playerUUID) {
        playerTeleportStatus.remove(playerUUID);
    }

    public static boolean getPlayerBackModelStatus(UUID playerUUID) {
        return playerBackModelStatus.getOrDefault(playerUUID, false);
    }

    public static void setPlayerBackModelStatus(UUID playerUUID, boolean status) {
        playerBackModelStatus.put(playerUUID, status);
    }

    public static void removePlayerBackModelStatus(UUID playerUUID) {
        playerBackModelStatus.remove(playerUUID);
    }

    public static boolean shouldShowBackModel(Player player) {
        Inventory inventory = player.getInventory();
        int count = 0;
        boolean hasMainHand = player.getMainHandItem().getItem() instanceof FalseProverbsItem;
        boolean hasOffHand = player.getOffhandItem().getItem() instanceof FalseProverbsItem;

        for (int i = 0; i < 36; i++) {
            if (i == inventory.selected) continue;
            if (inventory.getItem(i).getItem() instanceof FalseProverbsItem) count++;
        }
        for (int i = 36; i < 40; i++) {
            if (inventory.getItem(i).getItem() instanceof FalseProverbsItem) count++;
        }

        if (hasMainHand) count++;
        if (hasOffHand) count++;

        return hasOffHand ? false : (count > 1 || (count == 1 && !hasMainHand));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}