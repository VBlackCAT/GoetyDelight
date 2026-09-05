//package net.v_black_cat.goetydelight.mixin;
//
//import com.Polarice3.Goety.common.entities.util.SummonApostle;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.network.chat.Component;
//import net.minecraft.ChatFormatting;
//import net.minecraft.world.level.Level;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.Redirect;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import net.v_black_cat.goetydelight.item.ModItems;
//
//@Mixin(SummonApostle.class)
//public abstract class GoetyDelightSummonApostleMixin extends Entity {
//
//    protected GoetyDelightSummonApostleMixin(EntityType<?> entityType, Level level) {
//        super(entityType, level);
//    }
//
//    @Inject(
//            method = "tick", // 使用 Mojang 映射名称
//            at = @At("HEAD")
//    )
//    private void checkPlayerItems(CallbackInfo ci) {
//        if (!this.level().isClientSide()) {
//            ServerLevel serverLevel = (ServerLevel) this.level();
//            Player nearestPlayer = serverLevel.getNearestPlayer(this, 64.0);
//
//            if (nearestPlayer != null) {
//                // 检查 DOOM_COOKIE - 强制生成 Apollyon
//                if (hasItem(nearestPlayer, ModItems.DOOM_COOKIE.get())) {
//                    consumeItem(nearestPlayer, ModItems.DOOM_COOKIE.get(), 1);
//                    nearestPlayer.displayClientMessage(
//                            Component.translatable("message.goetydelight.doom_cookie_consumed")
//                                    .withStyle(ChatFormatting.RED),
//                            true
//                    );
//                    this.getPersistentData().putBoolean("ForceApollyon", true);
//                }
//
//                // 检查 AtonementVoucherWrapedCodItem - 阻止生成 Apollyon
//                if (hasItem(nearestPlayer, ModItems.ATONEMENT_VOUCHER_WRAPED_COD.get())) {
//                    consumeItem(nearestPlayer, ModItems.ATONEMENT_VOUCHER_WRAPED_COD.get(), 1);
//                    nearestPlayer.displayClientMessage(
//                            Component.translatable("message.goetydelight.atonement_voucher_consumed")
//                                    .withStyle(ChatFormatting.GREEN),
//                            true
//                    );
//                    this.getPersistentData().putBoolean("CancelApollyon", true);
//                }
//            }
//        }
//    }
//
//    @Redirect(
//            method = "tick", // 使用 Mojang 映射名称
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z" // 使用 Mojang 映射名称
//            )
//    )
//    private boolean modifyAddFreshEntity(ServerLevel instance, Entity entity) {
//        // 检查是否有强制标记
//        if (this.getPersistentData().getBoolean("ForceApollyon")) {
//            this.getPersistentData().remove("ForceApollyon");
//            // 强制生成 Apollyon，返回 true
//            return true;
//        }
//
//        // 检查是否有取消标记
//        if (this.getPersistentData().getBoolean("CancelApollyon")) {
//            this.getPersistentData().remove("CancelApollyon");
//            // 阻止生成 Apollyon，返回 false
//            return false;
//        }
//
//        // 默认行为：调用原始的 addFreshEntity
//        return instance.addFreshEntity(entity);
//    }
//
//    private boolean hasItem(Player player, net.minecraft.world.item.Item item) {
//        for (ItemStack stack : player.getInventory().items) {
//            if (stack.is(item)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void consumeItem(Player player, net.minecraft.world.item.Item item, int count) {
//        int remaining = count;
//        for (ItemStack stack : player.getInventory().items) {
//            if (stack.is(item) && remaining > 0) {
//                int consume = Math.min(stack.getCount(), remaining);
//                stack.shrink(consume);
//                remaining -= consume;
//                if (remaining <= 0) {
//                    break;
//                }
//            }
//        }
//    }
//}