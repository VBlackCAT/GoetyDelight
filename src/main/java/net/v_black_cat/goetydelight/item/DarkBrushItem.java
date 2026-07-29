package net.v_black_cat.goetydelight.item;

import com.Polarice3.Goety.api.items.ISoulRepair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.v_black_cat.goetydelight.init.ModItems;

public class DarkBrushItem extends BrushItem implements ISoulRepair {

    private final int ACCELERATION_FACTOR;
    private static final String ACCUMULATED_OFFSET_TAG = "AccumulatedOffset";

    public DarkBrushItem(Properties pProperties, int accelerationFactor) {
        super(pProperties);
        this.ACCELERATION_FACTOR = accelerationFactor;
    }

    // ========== 复制原版的粒子相关 ==========
    private static final class DustParticlesDelta {
        private final double xd;
        private final double zd;

        private DustParticlesDelta(double xd, double zd) {
            this.xd = xd;
            this.zd = zd;
        }

        public double xd() { return xd; }
        public double zd() { return zd; }

        public static DustParticlesDelta fromDirection(Vec3 viewVec, Direction hitDirection) {
            double xd = -viewVec.x;
            double zd = -viewVec.z;
            if (hitDirection == Direction.WEST) {
                xd = -1.0;
                zd = 0.0;
            } else if (hitDirection == Direction.EAST) {
                xd = 1.0;
                zd = 0.0;
            } else if (hitDirection == Direction.NORTH) {
                xd = 0.0;
                zd = -1.0;
            } else if (hitDirection == Direction.SOUTH) {
                xd = 0.0;
                zd = 1.0;
            }
            return new DustParticlesDelta(xd, zd);
        }
    }

    private void spawnDustParticles(Level level, BlockHitResult hitResult, BlockState state, Vec3 viewVec, HumanoidArm arm) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        int count = level.getRandom().nextInt(7, 12);
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);
        Direction direction = hitResult.getDirection();
        DustParticlesDelta delta = DustParticlesDelta.fromDirection(viewVec, direction);
        Vec3 location = hitResult.getLocation();

        for (int i = 0; i < count; i++) {
            level.addParticle(
                particle,
                location.x - (direction == Direction.WEST ? 1.0E-6 : 0.0),
                location.y,
                location.z - (direction == Direction.NORTH ? 1.0E-6 : 0.0),
                delta.xd() * side * 3.0 * level.getRandom().nextDouble(),
                0.0,
                delta.zd() * side * 3.0 * level.getRandom().nextDouble()
            );
        }
    }

    // ========== ISoulRepair 实现 ==========
    @Override
    public void repairTick(ItemStack stack, Entity entityIn, boolean isSelected) {
        if (stack.getItem() instanceof DarkBrushItem) {
            ISoulRepair.super.repairTick(stack, entityIn, isSelected);
        }
    }

    // ========== 辅助方法 ==========
    private HitResult calculateHitResult(Player player) {
        return ProjectileUtil.getHitResultOnViewVector(player,
                (e) -> !e.isSpectator() && e.isPickable(),
                player.blockInteractionRange());
    }

    // ========== 核心刷扫逻辑（带加速） ==========
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (remainingUseDuration >= 0 && livingEntity instanceof Player player) {
            HitResult hitresult = this.calculateHitResult(player);
            if (hitresult instanceof BlockHitResult blockhitresult) {
                if (hitresult.getType() == HitResult.Type.BLOCK) {
                    // 计算已使用时间 (tick)
                    int useTime = this.getUseDuration(stack, livingEntity) - remainingUseDuration + 1;

                    // ★ 原版粒子触发条件：每10 tick触发一次，在 useTime % 10 == 5 时
                    // 为了保持与原版一致，我们沿用此条件，但将加速逻辑放在这里
                    boolean shouldTrigger = useTime % 10 == 5;

                    if (shouldTrigger) {
                        BlockPos blockpos = blockhitresult.getBlockPos();
                        BlockState blockstate = level.getBlockState(blockpos);
                        HumanoidArm arm = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND
                                ? player.getMainArm()
                                : player.getMainArm().getOpposite();

                        // 1. 生成粒子（仅当方块可产生粒子）
                        if (blockstate.shouldSpawnTerrainParticles() && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                            this.spawnDustParticles(level, blockhitresult, blockstate,
                                    livingEntity.getViewVector(0.0F), arm);
                        }

                        // 2. 播放音效
                        Block block = blockstate.getBlock();
                        SoundEvent soundevent = (block instanceof BrushableBlock brushable)
                                ? brushable.getBrushSound()
                                : SoundEvents.BRUSH_GENERIC;
                        level.playSound(player, blockpos, soundevent, SoundSource.BLOCKS);

                        // 3. 加速刷扫：执行多次 brush 调用
                        if (!level.isClientSide()) {
                            BlockEntity blockentity = level.getBlockEntity(blockpos);
                            if (blockentity instanceof BrushableBlockEntity brushableBE) {
                                // 从 NBT 获取累计偏移量
                                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                                CompoundTag tag = (customData != null) ? customData.copyTag() : new CompoundTag();
                                int accumulatedOffset = tag.getInt(ACCUMULATED_OFFSET_TAG);
                                long baseTime = level.getGameTime();

                                boolean finished = false;
                                // 循环执行 ACCELERATION_FACTOR 次刷扫（每次用不同的时间戳）
                                for (int j = 0; j < ACCELERATION_FACTOR; j++) {
                                    long fakeTime = baseTime + accumulatedOffset + j;
                                    if (brushableBE.brush(fakeTime, player, blockhitresult.getDirection())) {
                                        finished = true;
                                        break;
                                    }
                                }

                                // 更新累计偏移量
                                accumulatedOffset += ACCELERATION_FACTOR;
                                tag.putInt(ACCUMULATED_OFFSET_TAG, accumulatedOffset);
                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                                if (finished) {
                                    EquipmentSlot slot = stack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND))
                                            ? EquipmentSlot.OFFHAND
                                            : EquipmentSlot.MAINHAND;
                                    stack.hurtAndBreak(1, livingEntity, slot);
                                    // 刷扫完成，重置偏移量
                                    tag.remove(ACCUMULATED_OFFSET_TAG);
                                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                }
                            }
                        }
                    }
                    return;
                }
            }
            livingEntity.releaseUsingItem();
        } else {
            livingEntity.releaseUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(ACCUMULATED_OFFSET_TAG)) {
                tag.remove(ACCUMULATED_OFFSET_TAG);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }
}