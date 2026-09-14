package net.v_black_cat.goetydelight.events;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayPlayerSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.MathHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.v_black_cat.goetydelight.GoetyDelight;
import net.v_black_cat.goetydelight.init.ModAttachments;
import net.v_black_cat.goetydelight.init.ModServerConfig;
import net.v_black_cat.goetydelight.item.FalseProverbsItem;
import net.v_black_cat.goetydelight.util.FoodState;
import vectorwing.farmersdelight.common.item.enchantment.BackstabbingEnchantment;

import java.util.UUID;

@EventBusSubscriber(modid = GoetyDelight.MODID)
public class FalseProverbsEvents {

    private static final ResourceLocation SHIFT_SPEED_MODIFIER_ID = ResourceLocation.withDefaultNamespace("shift_speed");

    /** shift 状态机节拍：5 tick = 250ms，切换延迟不可感知。 */
    private static final int SHIFT_STATE_INTERVAL = 5;

    /** 解析一次就存下来的附件类型（状态机每 5 tick 每玩家都要读，没必要每次走 Supplier#get()）。 */
    private static AttachmentType<FoodState> foodStateType;

    /** 同理缓存粒子类型：每次起手都要用，原来每次 get() 一次。 */
    private static SimpleParticleType cultSpellParticle;

    /**
     * 玩家 tick：<b>只剩 shift 状态机</b>。背部槽位已经完全事件驱动，这个 handler 不再碰它
     * （见 {@link #SLOT_WATCHER} / {@link #onContainerOpen} / {@link #onPlayerLoggedIn} /
     * {@link #onPlayerRespawn} / {@link #onPlayerClone} / {@link #onChangedDimension}，
     * 以及 {@code ServerGamePacketListenerImplMixin} 提供的快捷栏切格信号）。
     *
     * <p>为什么 shift 状态机还留着轮询：原版/NeoForge 没有「按下/松开潜行」的事件
     * （那个信息在 {@code ServerGamePacketListenerImpl#handlePlayerCommand} 里处理
     * {@code PRESS_SHIFT_KEY} / {@code RELEASE_SHIFT_KEY}，要拿到只能 mixin），
     * 为它加一个 mixin 的收益远小于风险，所以保持 5 tick 轮询。
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (player.tickCount % SHIFT_STATE_INTERVAL != 0) return;

        UUID playerUUID = player.getUUID();

        if (player.getMainHandItem().getItem() instanceof FalseProverbsItem) {
            FoodState state = player.getData(foodStateType());

            if (player.isShiftKeyDown()) {
                if (!state.isFalseProverbsShift()) {
                    // 第一次按下Shift
                    addBonusAttributes(player);
                    state.setFalseProverbsShift(true);

                    // 一次查表写完「原位 + 传送状态」
                    FalseProverbsItem.beginTeleport(playerUUID, player.position());

                    player.setInvisible(true);

                    // 优化粒子效果
                    spawnShiftParticles(player);
                    ModNetwork.sendTo(player, new SPlayPlayerSoundPacket(ModSounds.END_WALK.get(), 0.5F, 1.0F));
                }
            } else {
                // Shift释放时的处理
                if (state.isFalseProverbsShift()) {
                    resetShiftState(player);
                }
            }
        } else {
            // 主手不是FalseProverbsItem，但仍有Shift状态
            if (player.getData(foodStateType()).isFalseProverbsShift()) {
                resetShiftState(player);
            }
        }
    }

    // ==================== 背部槽位：只监听「真的变了」 ====================

    /**
     * 挂在玩家容器菜单上的槽位监听器。这就是替代 {@code Item#inventoryTick} 轮询的东西：
     * 原版 {@code AbstractContainerMenu#triggerSlotListeners} 每 tick 拿 {@code lastSlots}
     * 与当前内容比对，<b>只在 {@code ItemStack.matches} 为假（内容真的变了）时才回调</b> ——
     * 比对是原版本来就要做的活，所以我们既不轮询也不额外付比对成本。
     *
     * <p>只认玩家自己背包（{@link Inventory}）的槽位：箱子格、合成格的变化不直接触发重算，
     * 但它们导致的背包变化会各自触发。丢物品、塞箱子、/clear、死亡掉落、别的 mod 搬物品，
     * 走的都是「某个背包槽位内容变了」，全都会到这里。
     */
    private static final ContainerListener SLOT_WATCHER = new ContainerListener() {
        @Override
        public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack) {
            if (slotIndex < 0 || slotIndex >= menu.slots.size()) return;
            if (menu.getSlot(slotIndex).container instanceof Inventory inventory) {
                FalseProverbsItem.refreshBackSlot(inventory.player);
            }
        }

        @Override
        public void dataChanged(AbstractContainerMenu menu, int slotIndex, int value) {
        }
    };

    /** 幂等地挂监听（先摘再挂，避免重复注册导致重复回调）。 */
    private static void watchSlots(AbstractContainerMenu menu) {
        menu.removeSlotListener(SLOT_WATCHER);
        menu.addSlotListener(SLOT_WATCHER);
    }

    /** 挂上玩家自己的物品栏菜单，并重算一次；实体/菜单重建后（登录、重生、换维度）都要重挂。 */
    private static void watchInventory(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            watchSlots(serverPlayer.inventoryMenu);
        }
        FalseProverbsItem.refreshBackSlot(player);
    }

    /** 打开任意容器（箱子 / 工作台 / 熔炉……）时也挂上：这些菜单同样包含玩家背包的槽位。 */
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        watchSlots(event.getContainer());
    }

    /**
     * 死亡重生：把 shift 状态复位并清掉传送数据。
     *
     * <p>{@code FOOD_STATE} 带了 {@code copyOnDeath()}，死亡时若正按着 shift，
     * 标志位（和加速修饰符）会跟着新实体走，不在这里复位就会卡住；
     * 传送数据是按 UUID 存的静态表，不清的话重生的玩家可能被传回死亡前的位置。
     *
     * <p>换维度也会触发 {@code Clone}，那种情况不该复位 shift 状态，所以判 {@code isWasDeath()}。
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        Player newPlayer = event.getEntity();
        resetShiftState(newPlayer);
        FalseProverbsItem.clearPlayerData(newPlayer.getUUID());
        watchInventory(newPlayer);
    }

    /** 换维度：跨维度的「原位」没有意义，直接丢掉；实体重建了，监听也要重挂。 */
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        FalseProverbsItem.setOriginalPosition(player.getUUID(), null);
        watchInventory(player);
    }

    /** 登录：挂上物品栏监听并重算一次（槽位附件是从存档读回来的，背包可能早就变了）。 */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        watchInventory(player);
    }

    /** 重生：实体和菜单都是新的，重新挂监听并重算。 */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        watchInventory(player);
    }

    private static AttachmentType<FoodState> foodStateType() {
        AttachmentType<FoodState> type = foodStateType;
        if (type == null) {
            foodStateType = type = ModAttachments.FOOD_STATE.get();
        }
        return type;
    }

    private static SimpleParticleType cultSpellParticle() {
        SimpleParticleType type = cultSpellParticle;
        if (type == null) {
            cultSpellParticle = type = (SimpleParticleType) ModParticleTypes.CULT_SPELL.get();
        }
        return type;
    }

    private static void spawnShiftParticles(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player.tickCount % 20 != 0) return; // 每20tick生成一次粒子，优化性能

        // 这三个颜色参数是常量，提到循环外算一次（原来在循环里每次重算）
        double d0 = MathHelper.rgbToSpeed(96.0F);
        double d1 = MathHelper.rgbToSpeed(62.0F);
        double d2 = MathHelper.rgbToSpeed(92.0F);

        SimpleParticleType particleType = cultSpellParticle();
        for (int i = 0; i < 2; ++i) { // 减少粒子数量，1.20.1版本优化
            serverLevel.sendParticles(particleType,
                    player.getRandomX(1.0F), player.getRandomY(), player.getRandomZ(1.0F),
                    0, d0, d1, d2, 0.5F);
        }
    }

    private static void resetShiftState(Player player) {
        UUID playerUUID = player.getUUID();
        player.getData(foodStateType()).setFalseProverbsShift(false);
        removeBonusAttributes(player);
        FalseProverbsItem.setOriginalPosition(playerUUID, null);
        FalseProverbsItem.setPlayerTeleportStatus(playerUUID, false);
        player.setInvisible(false);
    }

    private static void addBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(SHIFT_SPEED_MODIFIER_ID) == null) {
            AttributeModifier modifier = new AttributeModifier(
                    SHIFT_SPEED_MODIFIER_ID,
                    ModServerConfig.getShiftSpeedMultiplier(),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            speedAttribute.addTransientModifier(modifier);
        }
    }

    private static void removeBonusAttributes(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SHIFT_SPEED_MODIFIER_ID);
        }
    }

    // 统一的伤害处理
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (player.isUsingItem()) return;

        float amount = event.getAmount();
        UUID playerUUID = player.getUUID();

        if (amount > 0.0F) {
            if (player.isShiftKeyDown()) {
                if (FalseProverbsItem.getPlayerTeleportStatus(playerUUID)) {
                    // 传送状态下的背刺检查
                    if (!BackstabbingEnchantment.isLookingBehindTarget(event.getEntity(), player.getEyePosition())) {
                        event.setAmount(amount * ModServerConfig.getFalseProverbsShiftDamageMultiplier());
                    }
                    // 如果是背刺，在onLivingDamage中处理
                } else {
                    // 非传送状态下的Shift伤害
                    event.setAmount(amount * ModServerConfig.getFalseProverbsShiftDamageMultiplier());
                }
            } else {
                // 普通攻击
                event.setAmount(amount * ModServerConfig.getFalseProverbsNormalDamageMultiplier());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;
        if (player.isUsingItem()) return;

        UUID playerUUID = player.getUUID();

        if (FalseProverbsItem.getPlayerTeleportStatus(playerUUID) && player.isShiftKeyDown()) {
            if (event.getOriginalDamage() > 0.0F) {
                // 背刺额外伤害
                if (BackstabbingEnchantment.isLookingBehindTarget(event.getEntity(), player.getEyePosition())) {
                    event.setNewDamage(event.getOriginalDamage() * ModServerConfig.getFalseProverbsBackstabDamageMultiplier());
                }

                // 传送回原位
                Vec3 originalPos = FalseProverbsItem.getOriginalPosition(playerUUID);
                if (originalPos != null) {
                    player.teleportTo(originalPos.x, originalPos.y, originalPos.z);
                }

                // 清除传送状态
                FalseProverbsItem.removePlayerTeleportStatus(playerUUID);
                player.setInvisible(false);
            }
        }
    }

    // 玩家登出清理
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FalseProverbsItem.clearPlayerData(event.getEntity().getUUID());
    }

    @EventBusSubscriber(modid = GoetyDelight.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onPlayerRenderPre(RenderLivingEvent.Pre event) {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!(player.level() instanceof ClientLevel)) return;
            if (!(player.getMainHandItem().getItem() instanceof FalseProverbsItem)) return;

            if (player.isShiftKeyDown()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void renderArm(RenderArmEvent event) {
            AbstractClientPlayer player = event.getPlayer();
            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof FalseProverbsItem)) return;
            if (!player.isShiftKeyDown()) return;

            if (mainHand.isEmpty() && event.getArm() == player.getMainArm()) {
                event.setCanceled(true);
            } else if (player.getOffhandItem().isEmpty() && event.getArm() != player.getMainArm()) {
                event.setCanceled(true);
            }
        }
    }
}
