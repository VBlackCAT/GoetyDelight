package net.v_black_cat.goetydelight.mixin;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BrushableBlockEntity.class)
public abstract class BrushableBlockEntityMixin {

    @Shadow
    private ResourceLocation lootTable;

    @Shadow
    private long lootTableSeed;
    @Shadow
    private int brushCount;

    // 添加一个字段来存储当前刷扫的玩家
    private Player currentBrushingPlayer;

    @Inject(
            method = "brush",
            at = @At("HEAD")
    )
    private void onBrushStart(long pStartTick, Player pPlayer, Direction pHitDirection, CallbackInfoReturnable<Boolean> cir) {
        // 存储当前刷扫的玩家
        this.currentBrushingPlayer = pPlayer;
    }

    @Inject(
            method = "brush",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BrushableBlockEntity;getCompletionState()I",
                    ordinal = 0,  // 第一个 getCompletionState() 调用之后
                    shift = At.Shift.AFTER
            )
    )
    private void beforeCompletionCheck(long pStartTick, Player pPlayer, Direction pHitDirection, CallbackInfoReturnable<Boolean> cir) {
        if (this.currentBrushingPlayer != null) {


            if(currentBrushingPlayer.getMainHandItem().getItem() == ModItems.CURSED_METAL_BRUSH.get()){

                if(this.brushCount==3){
                    this.brushCount=4;
                }
                if(this.brushCount==6){
                    this.brushCount=7;
                }
                if(this.brushCount==8){
                    this.brushCount=10;
                }
            }
            if(currentBrushingPlayer.getMainHandItem().getItem() == ModItems.DARK_BRUSH.get()){

                if (this.brushCount % 2 == 1 && this.brushCount < 10) {
                    this.brushCount++;
                }
            }

        }


    }


    @Inject(
            method = "dropContent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onDropContent(Player player, CallbackInfo ci) {
        BrushableBlockEntity blockEntity = (BrushableBlockEntity) (Object) this;



        // 只在服务器端执行，并且有战利品表时
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel && this.lootTable != null) {
            // 检查玩家是否使用自定义刷子
            if (isUsingCustomBrush(this.currentBrushingPlayer)) {
                // 获取战利品表
                LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(this.lootTable);

                // 创建战利品上下文
                LootParams.Builder lootContextBuilder = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockEntity.getBlockPos()))
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withLuck(player.getLuck());

                // 从战利品表获取额外掉落物
                List<ItemStack> extraDrops = lootTable.getRandomItems(lootContextBuilder.create(LootContextParamSets.CHEST));

                // 掉落额外物品
                for (ItemStack extraDrop : extraDrops) {
                    BlockPos pos = blockEntity.getBlockPos();
                    serverLevel.addFreshEntity(new ItemEntity(
                            serverLevel,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            extraDrop
                    ));
                }
            }
        }

        // 清除存储的玩家
        this.currentBrushingPlayer = null;
    }

    // 检查玩家是否使用自定义刷子
    private boolean isUsingCustomBrush(Player player) {
        if (player == null) {
            return false;
        }

        // 获取玩家当前手中的物品
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        // 检查是否是您的自定义刷子
        // 假设您的自定义刷子注册为 ModItems.CUSTOM_BRUSH
        return false;
//        return mainHandItem.getItem() == ModItems.BRUSH.get() ||
//                offHandItem.getItem() == ModItems.BRUSH.get();
    }
}